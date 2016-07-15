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

package com.seleniumtests.uipage.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.SessionNotFoundException;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.SystemClock;

import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.util.helper.WaitHelper;

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
    @Around("execution(public * com.seleniumtests.uipage.htmlelements.HtmlElement..* (..)) "
    		+ "&& !execution(* com.seleniumtests.uipage.htmlelements.HtmlElement.toString (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.htmlelements.HtmlElement.getBy (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.htmlelements.HtmlElement.setDriver (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.htmlelements.HtmlElement.getDriver (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.htmlelements.HtmlElement.getLabel (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.htmlelements.HtmlElement.getLocator (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.htmlelements.HtmlElement.waitForPresent ())"
    		+ "&& !execution(* com.seleniumtests.uipage.htmlelements.HtmlElement.toHTML (..))"
    		+ "|| execution(public * com.seleniumtests.uipage.htmlelements.SelectList..* (..)) "
    		)
    public Object replay(ProceedingJoinPoint joinPoint) throws Throwable {
    	
    	long end = systemClock.laterBy(30000);
    	Object reply = null;
    	
    	// update driver reference of the element
    	// corrects bug of waitElementPresent which threw a SessionNotFoundError because driver reference were not
    	// updated before searching element (it used the driver reference of an old test session)
    	((HtmlElement)joinPoint.getTarget()).setDriver(WebUIDriver.getWebDriver());
    	
    	while (systemClock.isNowBefore(end)) {
	    	
	    	try {
	    		reply = joinPoint.proceed(joinPoint.getArgs());
	    		WaitHelper.waitForMilliSeconds(200);
	    		break;
	    	} catch (UnhandledAlertException e) {
	    		throw e;
	    	} catch (WebDriverException e) { 
	    		
	    		// don't prevent TimeoutException to be thrown when coming from waitForPresent
	    		// only check that cause is the not found element and not an other error (NoSucheSessionError for example)
	    		if (e instanceof TimeoutException && joinPoint.getSignature().getName().equals("waitForPresent")) {
	    			if (e.getCause() instanceof NoSuchElementException) {
	    				throw e;
	    			}
	    		}

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
