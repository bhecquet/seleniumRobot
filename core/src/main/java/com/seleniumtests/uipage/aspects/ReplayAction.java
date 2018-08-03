/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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

import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.UnreachableBrowserException;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.aspects.LogAction;
import com.seleniumtests.core.utils.SystemClock;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.DatasetException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.uipage.ReplayOnError;
import com.seleniumtests.uipage.htmlelements.GenericPictureElement;
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
@DeclarePrecedence("LogAction, ReplayAction")
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
			+ "&& execution(@com.seleniumtests.uipage.ReplayOnError public * * (..)) && @annotation(replay)")
    public Object replayHtmlElement(ProceedingJoinPoint joinPoint, ReplayOnError replay) throws Throwable {

    	long end = systemClock.laterBy(SeleniumTestsContextManager.getThreadContext().getReplayTimeout() * 1000);
    	Object reply = null;

    	
    	// update driver reference of the element
    	// corrects bug of waitElementPresent which threw a SessionNotFoundError because driver reference were not
    	// updated before searching element (it used the driver reference of an old test session)
    	HtmlElement element = (HtmlElement)joinPoint.getTarget();
    	element.setDriver(WebUIDriver.getWebDriver());
		String targetName = joinPoint.getTarget().toString();
    	
		TestAction currentAction = null;
    	String methodName = joinPoint.getSignature().getName();
    	if (methodName != "getCoordinates") {
    		List<String> pwdToReplace = new ArrayList<>();
    		String actionName = String.format("%s on %s %s", methodName, targetName, LogAction.buildArgString(joinPoint, pwdToReplace));
    		currentAction = new TestAction(actionName, false, pwdToReplace);
    	}

		// log action before its started. By default, it's OK. Then result may be overwritten if step fails
		// order of steps is the right one (first called is first displayed)
		if (currentAction != null && isHtmlElementDirectlyCalled(Thread.currentThread().getStackTrace()) && TestLogging.getParentTestStep() != null) {
			TestLogging.getParentTestStep().addAction(currentAction);
		}	
		
		boolean actionFailed = false;
		boolean ignoreFailure = false;
		
		try {
	    	while (systemClock.isNowBefore(end)) {

	    		// in case we have switched to an iframe for using previous webElement, go to default content
	    		if (element.getDriver() != null && SeleniumTestsContextManager.isWebTest()) {
	    			element.getDriver().switchTo().defaultContent(); // TODO: error when clic is done, closing current window
	    		}
		    	
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
		    				ignoreFailure = true;  // issue #103: do not log error when waitForPresent raises TimeoutException
		    				throw e;
		    			}
		    		}
	
		    		if (systemClock.isNowBefore(end - 200)) {
		    			WaitHelper.waitForMilliSeconds(replay.replayDelayMs());
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
		} catch (Throwable e) {
			actionFailed = true && !ignoreFailure;
			throw e;
		} finally {
			if (currentAction != null && isHtmlElementDirectlyCalled(Thread.currentThread().getStackTrace()) && TestLogging.getParentTestStep() != null) {
				currentAction.setFailed(actionFailed);
			}		
		}
   }
    
	/**
	 * Replay all actions annotated by ReplayOnError if the class is not a subclass of 
	 * HtmlElement
	 * @param joinPoint
	 * @throws Throwable
	 */
	@Around("!execution(public * com.seleniumtests.uipage.htmlelements.HtmlElement+.* (..))"
			+ "&& execution(@com.seleniumtests.uipage.ReplayOnError public * * (..)) && @annotation(replay)")
	public Object replay(ProceedingJoinPoint joinPoint, ReplayOnError replay) throws Throwable {
		
		int replayDelayMs = replay != null ? replay.replayDelayMs(): 100;
		
		long end = systemClock.laterBy(SeleniumTestsContextManager.getThreadContext().getReplayTimeout() * 1000);
		Object reply = null;
		
		String targetName = joinPoint.getTarget().toString();
		TestAction currentAction = null;

		if (joinPoint.getTarget() instanceof GenericPictureElement) {
	    	String methodName = joinPoint.getSignature().getName();
	    	List<String> pwdToReplace = new ArrayList<>();
			String actionName = String.format("%s on %s %s", methodName, targetName, LogAction.buildArgString(joinPoint, pwdToReplace));
			currentAction = new TestAction(actionName, false, pwdToReplace);
	
			// log action before its started. By default, it's OK. Then result may be overwritten if step fails
			// order of steps is the right one (first called is first displayed)
			if (isHtmlElementDirectlyCalled(Thread.currentThread().getStackTrace()) && TestLogging.getParentTestStep() != null) {
				TestLogging.getParentTestStep().addAction(currentAction);
			}
		}
		
		boolean actionFailed = false;
		
		try {
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
						WaitHelper.waitForMilliSeconds(replayDelayMs);
						continue;
					} else {
						throw e;
					}
				}
			}
			return reply;
		} catch (Throwable e) {
			actionFailed = true;
			throw e;
		} finally {
			if (currentAction != null && isHtmlElementDirectlyCalled(Thread.currentThread().getStackTrace()) && TestLogging.getParentTestStep() != null) {
				currentAction.setFailed(actionFailed);
				
				if (joinPoint.getTarget() instanceof GenericPictureElement) {
					currentAction.setDurationToExclude(((GenericPictureElement)joinPoint.getTarget()).getActionDuration());
				}
			}		
		}
	}
	
	/**
	 * Replays the composite action in case any error occurs
	 * @param joinPoint
	 */
	@Around("execution(public void org.openqa.selenium.interactions.Actions.BuiltAction.perform ())")
	public Object replayCompositeAction(ProceedingJoinPoint joinPoint) throws Throwable {
		return replay(joinPoint, null);

	}
	
	/**
	 * Check whether this action has directly been performed on the HtmlElement (e.g: click)
	 * or through an other type of element (e.g: clic on LinkElement, redirected to HtmlElement)
	 * In this last case, do not log action as it has already been logged by the specific type of 
	 * element
	 * @param stack
	 * @return
	 */
	private boolean isHtmlElementDirectlyCalled(StackTraceElement[] stack) {
		// disabled as action logging is now done only on @ReplayAction annotations. So there will not be 2 calls to the same action (effect of issue #62)
		// TODO: should be removed
		
		return true;
//		String stackClass = null;
//		boolean specificElementFound = false;
//		boolean htmlElementFound = false;
//		
//		for(int i=0; i < stack.length; i++) {
//			
//			// when using aspects, class name may contain a "$", remove everything after that symbol
//			stackClass = stack[i].getClassName().split("\\$")[0];
//			if (stackClass.equals("com.seleniumtests.uipage.htmlelements.HtmlElement")) {
//				htmlElementFound = true;
//			} else if (stackClass.startsWith("com.seleniumtests.uipage.htmlelements.")) {
//				specificElementFound = true;
//			}
//		}
//		if (htmlElementFound && specificElementFound) {
//			return false;
//		} else {
//			return true;
//		}
	}
	
    
}
