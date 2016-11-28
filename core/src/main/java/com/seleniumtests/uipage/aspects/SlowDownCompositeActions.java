package com.seleniumtests.uipage.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

import com.seleniumtests.util.helper.WaitHelper;

@Aspect
public class SlowDownCompositeActions {

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
}
