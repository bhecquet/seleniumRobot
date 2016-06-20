/*
 * Copyright 2016 www.infotel.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seleniumtests.webelements;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.SystemClock;

import com.seleniumtests.helper.WaitHelper;

/**
 * Aspect to intercept calls to methods of HtmlElement. It allows to retry discovery and action 
 * when something goes wrong with the driver
 * 
 * @author behe
 *
 */
@Aspect
public class ReplayAction {

	private static SystemClock systemClock = new SystemClock();
	
	/**
	 * 
	 * @param joinPoint
	 * @throws Throwable
	 */
    @Around("execution(public * com.seleniumtests.webelements.HtmlElement..* (..)) "
    		+ "&& !execution(* com.seleniumtests.webelements.HtmlElement.toString (..))"
    		+ "&& !execution(* com.seleniumtests.webelements.HtmlElement.getBy (..))"
    		+ "&& !execution(* com.seleniumtests.webelements.HtmlElement.getLabel (..))"
    		+ "&& !execution(* com.seleniumtests.webelements.HtmlElement.getLocator (..))"
    		+ "&& !execution(* com.seleniumtests.webelements.HtmlElement.toHTML (..))"
    		+ "|| execution(public * com.seleniumtests.webelements.SelectList..* (..)) "
    		)
    public Object replay(ProceedingJoinPoint joinPoint) throws Throwable {
    	
    	long end = systemClock.laterBy(30000);
    	Object reply = null;
    	
    	while (systemClock.isNowBefore(end)) {
	    	
	    	try {
	    		reply = joinPoint.proceed(joinPoint.getArgs());
	    		WaitHelper.waitForMilliSeconds(200);
	    		break;
	    	} catch (UnhandledAlertException e) {
	    		throw e;
	    	} catch (WebDriverException e) {
	    		if (systemClock.isNowBefore(end)) {
	    			WaitHelper.waitForMilliSeconds(100);
					continue;
				} else {
					if (e instanceof NoSuchElementException) {
						throw new NoSuchElementException("Searched element could not be found");
					} else if (e instanceof UnreachableBrowserException) {
						throw new WebDriverException("Browser did not reply, it may have frozen");
					}
					throw e;
				}
	    	} 
			
    	}
    	return reply;
   }
    
    
}
