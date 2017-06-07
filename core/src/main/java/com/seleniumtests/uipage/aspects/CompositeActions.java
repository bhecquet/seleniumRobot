/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.uipage.aspects;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.ClickAction;
import org.openqa.selenium.interactions.CompositeAction;
import org.openqa.selenium.interactions.Interaction;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.util.helper.WaitHelper;

@Aspect
public class CompositeActions {
	
	/**
	 * Slows down any action performed through CompositeActions by 200 ms
	 * It requires to use {@link EventFiringWebDriver} because we intercept the "perform()" method of any {@link org.openqa.selenium.interactions.Action}
	 * Eclipse project also need to have its Aspect build path configured with selenium-api artifact
	 * @param joinPoint
	 */
	@After("call(public * org.openqa.selenium.interactions.Action+.perform (..))")
    public void slowDown(JoinPoint joinPoint) {
		WaitHelper.waitForMilliSeconds(200);
	}
	
	/**
	 * Update window handles when a click is requested in a composite Action (to get the same behavior between native clicks
	 * and clicks in CompositeAction
	 * Capture is done on all Action sub-classes, else it would never be done
	 * @param joinPoint
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Before("call(public void org.openqa.selenium.interactions.Action+.perform ())")
	public void updateHandles(JoinPoint joinPoint) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if (!(joinPoint.getTarget() instanceof CompositeAction)) {
			return;
		}
		CompositeAction compositeAction = (CompositeAction)joinPoint.getTarget();
		Field actionListField = CompositeAction.class.getDeclaredField("actionsList");
		actionListField.setAccessible(true);
		List<Action> actionsList = (List<Action>)actionListField.get(compositeAction);
		
		boolean clickRequested = false;
		for (Action action: actionsList) {
			if (action instanceof ClickAction) {
				clickRequested = true;
			}
		}
		
		if (clickRequested) {
			((CustomEventFiringWebDriver)WebUIDriver.getWebDriver()).updateWindowsHandles();
		}
	}
	
	/**
	 * Intercept calls to {@link org.openqa.selenium.remote.RemoteWebDriver.perform(Collection<Sequence> actions)} method which handles
	 * the new way of sending composite actions
	 * @param joinPoint
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Before("call(public void org.openqa.selenium.remote.RemoteWebDriver.perform (..))")
	public void updateHandlesNewActions(JoinPoint joinPoint) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		Collection<Sequence> sequences = (Collection<Sequence>)joinPoint.getArgs()[0];
		for (Sequence sequence: sequences) {
		
			Field actionsField = Sequence.class.getDeclaredField("actions");
			actionsField.setAccessible(true);
			LinkedList<Interaction> actionsList = (LinkedList<Interaction>)actionsField.get(sequence);
			
			Boolean clic = null;
			boolean clickRequested = false;

			for (Interaction action: actionsList) {
				if (action.getClass().getName().contains("PointerInput$PointerPress")) {
					Field buttonField = action.getClass().getDeclaredField("button");
					buttonField.setAccessible(true);
					int button = buttonField.getInt(action);
					
					Field directionField = action.getClass().getDeclaredField("direction");
					directionField.setAccessible(true);
					String direction = directionField.get(action).toString();
					
					// only left button
					if (button != 0) {
						clic = null;
						continue;
					}
					
					// check we have a DOWN -> UP sequence
					if ("DOWN".equals(direction) && clic == null) {
						clic = true;
					} else if (clic && "UP".equals(direction)) {
						clic = null;
						clickRequested = true;
					} else {
						clic = null;
					}
					
				} else {
					clic = null;
				}
			}
			
			if (clickRequested) {
				((CustomEventFiringWebDriver)WebUIDriver.getWebDriver()).updateWindowsHandles();
			}
		}
	}
	
	
}
