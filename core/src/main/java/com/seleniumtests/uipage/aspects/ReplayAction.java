/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
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
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.InputSource;
import org.openqa.selenium.interactions.Interaction;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.remote.UnreachableBrowserException;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.core.aspects.LogAction;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.DatasetException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.uipage.ReplayOnError;
import com.seleniumtests.uipage.htmlelements.GenericPictureElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.SelectList;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.ScenarioLogger;

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

	private static Clock systemClock = Clock.systemUTC();
	private static final ScenarioLogger scenarioLogger = ScenarioLogger.getScenarioLogger(ReplayAction.class);
	
	private Integer getActionDelay() {
		return SeleniumTestsContextManager.getThreadContext().getActionDelay();
	}
	
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

    	Object reply = null;

    	
    	// update driver reference of the element
    	// corrects bug of waitElementPresent which threw a SessionNotFoundError because driver reference were not
    	// updated before searching element (it used the driver reference of an old test session)
    	HtmlElement element = (HtmlElement)joinPoint.getTarget();
    	element.setDriver(WebUIDriver.getWebDriver(false));
		String targetName = joinPoint.getTarget().toString();
		
		Instant end = systemClock.instant().plusSeconds(element.getReplayTimeout());
    	
		TestAction currentAction = null;
    	String methodName = joinPoint.getSignature().getName();
    	if (!methodName.equals("getCoordinates")) {
    		List<String> pwdToReplace = new ArrayList<>();
    		String actionName = String.format("%s on %s %s", methodName, targetName, LogAction.buildArgString(joinPoint, pwdToReplace, new HashMap<>()));
    		currentAction = new TestAction(actionName, false, pwdToReplace);
    	}

		// log action before its started. By default, it's OK. Then result may be overwritten if step fails
		// order of steps is the right one (first called is first displayed)
		if (currentAction != null && TestStepManager.getParentTestStep() != null) {
			TestStepManager.getParentTestStep().addAction(currentAction);
		}	
		
		boolean actionFailed = false;
		boolean ignoreFailure = false;
		Throwable currentException = null;
		
		try {
	    	while (end.isAfter(systemClock.instant())) {

	    		// in case we have switched to an iframe for using previous webElement, go to default content
	    		if (element.getDriver() != null && SeleniumTestsContextManager.isWebTest()) {
	    			element.getDriver().switchTo().defaultContent(); // TODO: error when clic is done, closing current window
	    		}
		    	
		    	try {
		    		reply = joinPoint.proceed(joinPoint.getArgs());
		    		
		    		// wait will be done only if action annotation request it
		    		if (replay.waitAfterAction()) {
		    			WaitHelper.waitForMilliSeconds(getActionDelay());
		    		}
		    		break;
		    	} catch (UnhandledAlertException e) {
		    		throw e;
		    	} catch (MoveTargetOutOfBoundsException | InvalidElementStateException e) {
		    		
		    		// if click has been intercepted, it means element could not be interacted, so allow auto scrolling for further retries
		    		// to avoid trying always the same method, we try without scrolling, then with scrolling, then without, ...
		    		
		    		if (element.isScrollToElementBeforeAction()) {
		    			element.setScrollToElementBeforeAction(false);
		    		} else {
		    			element.setScrollToElementBeforeAction(true);
		    		}
		    	} catch (WebDriverException e) { 
		    		// don't prevent TimeoutException to be thrown when coming from waitForPresent
		    		// only check that cause is the not found element and not an other error (NoSucheSessionError for example)
		    		if ((e instanceof TimeoutException 
		    				&& joinPoint.getSignature().getName().equals("waitForPresent") 
		    				&& e.getCause() instanceof NoSuchElementException) // issue #104: do not log error when waitForPresent raises TimeoutException
		    			|| (e instanceof NotFoundException
		    				&& isFromExpectedConditions(Thread.currentThread().getStackTrace())) // issue #194: return immediately if the action has been performed from ExpectedConditions class
		    																					 //   This way, we let the FluentWait process to retry or re-raise the exception
		    			) 
		    		{
	    				ignoreFailure = true;  
	    				throw e;
		    		}
	
		    		if (end.minusMillis(replay.replayDelayMs() + 100L).isAfter(systemClock.instant())) {
		    			WaitHelper.waitForMilliSeconds(replay.replayDelayMs());
					} else {
						if (e instanceof NoSuchElementException) {
							if (element instanceof SelectList && e.getMessage().contains("option")) {
								throw new NoSuchElementException(String.format("'%s' from page '%s': %s", element, element.getOrigin(), e.getMessage()));
							} else {
								throw new NoSuchElementException(String.format("Searched element [%s] from page '%s' could not be found", element, element.getOrigin()));
							}
						} else if (e instanceof UnreachableBrowserException) {
							throw new WebDriverException("Browser did not reply, it may have frozen");
						}
						throw e;
					}
		    	} 
				
	    	}
	    	return reply;
		} catch (Throwable e) {
			if (e instanceof NoSuchElementException 
					&& joinPoint.getTarget() instanceof HtmlElement
					&& (joinPoint.getSignature().getName().equals("findElements")
							|| joinPoint.getSignature().getName().equals("findHtmlElements"))) {
				return new ArrayList<WebElement>();
			} else {
				if (!ignoreFailure) {
					actionFailed = true;
					currentException = e;
				}
				throw e;
			}
		} finally {
			if (currentAction != null && TestStepManager.getParentTestStep() != null) {
				currentAction.setFailed(actionFailed);
				scenarioLogger.logActionError(currentException);
			}	
			
			// restore element scrolling flag for further uses
    		element.setScrollToElementBeforeAction(false);
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
		
		Instant end = systemClock.instant().plusSeconds(SeleniumTestsContextManager.getThreadContext().getReplayTimeout());
		Object reply = null;
		
		String targetName = joinPoint.getTarget().toString();
		TestAction currentAction = null;

		if (joinPoint.getTarget() instanceof GenericPictureElement) {
	    	String methodName = joinPoint.getSignature().getName();
	    	List<String> pwdToReplace = new ArrayList<>();
			String actionName = String.format("%s on %s %s", methodName, targetName, LogAction.buildArgString(joinPoint, pwdToReplace, new HashMap<>()));
			currentAction = new TestAction(actionName, false, pwdToReplace);
	
			// log action before its started. By default, it's OK. Then result may be overwritten if step fails
			// order of steps is the right one (first called is first displayed)
			if (TestStepManager.getParentTestStep() != null) {
				TestStepManager.getParentTestStep().addAction(currentAction);
			}
		}
		
		boolean actionFailed = false;
		Throwable currentException = null;
		
		try {
			while (end.isAfter(systemClock.instant())) {
				
				// chrome automatically scrolls to element before interacting but it may scroll behind fixed header and no error is 
				// raised if action cannot be performed
				if (((CustomEventFiringWebDriver)WebUIDriver.getWebDriver(false)).getBrowserInfo().getBrowser() == BrowserType.CHROME
						|| ((CustomEventFiringWebDriver)WebUIDriver.getWebDriver(false)).getBrowserInfo().getBrowser() == BrowserType.EDGE) {
					updateScrollFlagForElement(joinPoint, true, null);
				}
				
				try {
					reply = joinPoint.proceed(joinPoint.getArgs());
					WaitHelper.waitForMilliSeconds(200);
					break;
					
				// do not replay if error comes from scenario
				} catch (ScenarioException | ConfigurationException | DatasetException e) {
					throw e;
				} catch (MoveTargetOutOfBoundsException | InvalidElementStateException e) {
					updateScrollFlagForElement(joinPoint, null, e);
				} catch (Throwable e) {
	
					if (end.minusMillis(200).isAfter(systemClock.instant())) {
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
			currentException = e;
			throw e;
		} finally {
			if (currentAction != null && TestStepManager.getParentTestStep() != null) {
				currentAction.setFailed(actionFailed);
				scenarioLogger.logActionError(currentException);
				
				if (joinPoint.getTarget() instanceof GenericPictureElement) {
					currentAction.setDurationToExclude(((GenericPictureElement)joinPoint.getTarget()).getActionDuration());
				}
			}		
		}
	}
	
	/**
	 * Replays the composite action in case any error occurs
	 * When the composite action is played inside an HtmlElement, and the calling method is annotated with {@code @ReplayOnError}, we have 2 replays
	 * 
	 *  method replay => ReplayOnError annotation
	 *  	composite action replay
	 *  
	 *  This does not seem to be a problem because if 'composite action replay' takes to much time, then 'method replay' will not effectively replay
	 *  
	 * @param joinPoint
	 */
	@Around("execution(public void org.openqa.selenium.interactions.Actions.BuiltAction.perform ())")
	public Object replayCompositeAction(ProceedingJoinPoint joinPoint) throws Throwable {
		return replay(joinPoint, null);
	}
	
	/**
	 * Updates the scrollToelementBeforeAction flag of HtmlElement for CompositeActions
	 * Therefore, it looks at origin field of PointerInput$Move CompositeAction and update the flag
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private void updateScrollFlagForElement(ProceedingJoinPoint joinPoint, Boolean forcedValue, WebDriverException parentException) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Object actions = joinPoint.getTarget();
		
		// the calling method 'replay(ProceedingJoinPoint joinPoint, ReplayOnError replay)' may be called from GenericPictureElement.class or from Selenium 'Composite Actions'
		// Only the later case is covered here
		if (!actions.getClass().toString().contains("BuiltAction")) {
			return;
		}
		
		Field sequencesField = actions.getClass().getDeclaredField("sequences");
		sequencesField.setAccessible(true);
		Map<InputSource, Sequence> sequences = (Map<InputSource, Sequence>) sequencesField.get(actions);
		
		for (Sequence sequence: sequences.values()) {
			Field actionsField = Sequence.class.getDeclaredField("actions");
			actionsField.setAccessible(true);
			
			LinkedList<Interaction> actionsList = (LinkedList<Interaction>)actionsField.get(sequence);
			
			for (Interaction action: actionsList) {
				if (action.getClass().getName().contains("PointerInput$Move")) {
					Field originField = action.getClass().getDeclaredField("origin");
					originField.setAccessible(true);
					try {
						PointerInput.Origin origin = (PointerInput.Origin) originField.get(action);
						
						// we can change 'scrollToelementBeforeAction' flag only for HtmlElement objects. For RemoteWebElement, this cannot be done so we rethrow the exception
						// so that it can be treated elsewhere (mainly inside replayHtmlElement())
						if (origin.asArg() instanceof HtmlElement) {
							HtmlElement element = (HtmlElement) origin.asArg();
							if (forcedValue == null) {
								if (element.isScrollToElementBeforeAction()) {
					    			element.setScrollToElementBeforeAction(false);
					    		} else {
					    			element.setScrollToElementBeforeAction(true);
					    		}
							} else {
								element.setScrollToElementBeforeAction(forcedValue);
							}
						} else if (origin.asArg() instanceof RemoteWebElement && parentException != null) {
							throw parentException;
						}
					} catch (ClassCastException e1) {
						// nothing
					}
				}
			}
		}
	}
	
	/**
	 * issu #194: Returns true if the call to element action has been done from the org.openqa.selenium.support.ui.ExpectedConditions selenium class
	 *
	 * @param stack
	 * @return
	 */
	private boolean isFromExpectedConditions(StackTraceElement[] stack) {
		
		for(int i=0; i < stack.length; i++) {
			
			// when using aspects, class name may contain a "$", remove everything after that symbol
			String stackClass = stack[i].getClassName().split("\\$")[0];
			if (stackClass.equals("org.openqa.selenium.support.ui.ExpectedConditions")) {
				return true;
			}

		}
		return false;

	}
	
	/**
	 * Returns true if the call to element is made inside a subclass of HtmlElement
	 * @param stack
	 * @return
	 */
	private boolean isFromHtmlElement(StackTraceElement[] stack) {
		
		for(int i=0; i < stack.length; i++) {
			
			// when using aspects, class name may contain a "$", remove everything after that symbol
			String stackClass = stack[i].getClassName().split("\\$")[0];
			if (stackClass.startsWith("com.seleniumtests.uipage.htmlelements")) {
				return true;
			}
			
		}
		return false;
		
	}
	
    
}
