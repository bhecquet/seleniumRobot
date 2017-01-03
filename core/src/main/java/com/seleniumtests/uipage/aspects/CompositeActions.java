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
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.ClickAction;
import org.openqa.selenium.interactions.CompositeAction;

import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.util.helper.WaitHelper;

@Aspect
public class CompositeActions {
	
	/**
	 * Slows down any action performed through CompositeActions by 200 ms
	 * It requires to use EventFiringWebDriver because we intercept the "perform()" method of any {@link org.openqa.selenium.interactions.Action}
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
}
