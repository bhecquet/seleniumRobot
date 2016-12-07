package com.seleniumtests.uipage.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.ClickAction;
import org.openqa.selenium.interactions.CompositeAction;
import org.openqa.selenium.support.ui.SystemClock;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.DatasetException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.util.helper.WaitHelper;

@Aspect
public class CompositeActions {

	private static SystemClock systemClock = new SystemClock();
	
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
	 */
	@Before("call(public void org.openqa.selenium.interactions.Action+.perform ())")
	public void updateHandles(JoinPoint joinPoint) {
		if (!(joinPoint.getTarget() instanceof CompositeAction)) {
			return;
		}
		CompositeAction compositeAction = (CompositeAction)joinPoint.getTarget();
		
		boolean clickRequested = false;
		for (Action action: compositeAction.asList()) {
			if (action instanceof ClickAction) {
				clickRequested = true;
			}
		}
		
		if (clickRequested) {
			((CustomEventFiringWebDriver)WebUIDriver.getWebDriver()).updateWindowsHandles();
		}
	}
}
