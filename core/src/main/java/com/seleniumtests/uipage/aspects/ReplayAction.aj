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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.interactions.CompositeAction;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.SystemClock;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.DatasetException;
import com.seleniumtests.customexception.ScenarioException;
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
	 * Replay all HtmlElement actions annotated by ReplayOnError.
	 * Classes which are not subclass of HtmlElement won't go there 
	 * See javadoc of the annotation for details
	 * @param joinPoint
	 * @throws Throwable
	 */
	@Around("execution(public * com.seleniumtests.uipage.htmlelements.HtmlElement+.* (..))"
			+ "&& execution(@com.seleniumtests.uipage.ReplayOnError public * * (..))")
    public Object htmlElementReplay(ProceedingJoinPoint joinPoint) throws Throwable {

    	long end = systemClock.laterBy(SeleniumTestsContextManager.getThreadContext().getReplayTimeout() * 1000);
    	Object reply = null;
    	
    	// update driver reference of the element
    	// corrects bug of waitElementPresent which threw a SessionNotFoundError because driver reference were not
    	// updated before searching element (it used the driver reference of an old test session)
    	HtmlElement element = (HtmlElement)joinPoint.getTarget();
    	element.setDriver(WebUIDriver.getWebDriver());

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

	    		if (systemClock.isNowBefore(end - 200)) {
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
	    	} finally {
	    		// in case we have switched to an iframe for using webElement, go to default content
	    		if (element.getDriver() != null && SeleniumTestsContextManager.isWebTest()) {
	    			element.getDriver().switchTo().defaultContent();
	    		}
	    	}
			
    	}
    	return reply;
   }
    
	/**
	 * Replay all actions annotated by ReplayOnError if the class is not a subclass of 
	 * HtmlElement
	 * @param joinPoint
	 * @throws Throwable
	 */
	@Around("!execution(public * com.seleniumtests.uipage.htmlelements.HtmlElement+.* (..))"
			+ "&& execution(@com.seleniumtests.uipage.ReplayOnError public * * (..))")
	public Object replay(ProceedingJoinPoint joinPoint) throws Throwable {
		
		long end = systemClock.laterBy(SeleniumTestsContextManager.getThreadContext().getReplayTimeout() * 1000);
		Object reply = null;
		
		while (systemClock.isNowBefore(end)) {
			
			try {
				reply = joinPoint.proceed(joinPoint.getArgs());
				WaitHelper.waitForMilliSeconds(200);
				break;
			} catch (Throwable e) {
				
				// do not replay when error comes from test writing or configuration
				if (e instanceof ScenarioException 
						|| e instanceof ConfigurationException
						|| e instanceof DatasetException
						) {
					throw e;
				}

				if (systemClock.isNowBefore(end - 200)) {
					WaitHelper.waitForMilliSeconds(100);
					continue;
				} else {
					throw e;
				}
			}
		}
		return reply;
	}
	
	/**
	 * Replays the composite action in case any error occurs
	 * @param joinPoint
	 */
	@Around("execution(public void org.openqa.selenium.interactions.Actions.BuiltAction.perform ())")
	public Object replayCompositeAction(ProceedingJoinPoint joinPoint) throws Throwable {
		return replay(joinPoint);

	}
	
    
}
