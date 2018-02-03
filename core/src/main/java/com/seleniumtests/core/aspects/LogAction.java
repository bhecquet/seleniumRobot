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
package com.seleniumtests.core.aspects;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.SeleniumRobotTestPlan;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.uipage.PageObject;

/**
 * Aspect to intercept calls to methods of HtmlElement. It allows to retry discovery and action 
 * when something goes wrong with the driver
 * 
 * Here, we log:
 * - main steps: either cucumber step if this mode is used, or any webpage method called from a Test class (sub-class of SeleniumTestPlan)
 * - sub steps: these are webpage methods called from other webpage methods
 * - actions: these are driver methods (open page, click, ...)
 * 
 * Tree call is: 
 * - logTestStep
 * 		- logSubTestStep
 * 			- logPageObjectAction
 * 			- logPageObjectAction
 * 		- logSubTestStep
 * 			- logPageObjectAction
 * 
 * to produce:
 *  * root (TestStep)
 * 	  +--- action1 (TestAction)
 *    +--+ sub-step1 (TestStep)
 *       +--- sub-action1
 *       +--- message (TestMessage)
 *       +--- sub-action2
 *    +--- action2
 * 
 * @author behe
 *
 */
@Aspect
public class LogAction {

	/**
	 * Intercept actions
	 * @param joinPoint
	 * @throws Throwable 
	 */
	@Around("execution(public * com.seleniumtests.uipage.PageObject..* (..)) "
    		+ "&& !execution(* com.seleniumtests.uipage.PageObject.get* (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.PageObject.close* (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.PageObject.param (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.PageObject.assert* (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.PageObject.capture*Snapshot (..))"
    		)
	public Object logPageObjectAction(ProceedingJoinPoint joinPoint) throws Throwable {
		PageObject page = (PageObject)joinPoint.getTarget();
		String pageName = page == null ? "": "on page " + page.getClass().getSimpleName();
		String actionName = String.format("%s %s %s", joinPoint.getSignature().getName(), pageName, buildArgString(joinPoint));
		Object reply = null;
		boolean actionFailed = false;
		TestAction currentAction = new TestAction(actionName, false);
		
		if (TestLogging.getParentTestStep() != null) {
			TestLogging.getParentTestStep().addAction(currentAction);
		}
		
		try {
			reply = joinPoint.proceed(joinPoint.getArgs());
		} catch (Throwable e) {
			actionFailed = true;
			throw e;
		} finally {
			if (TestLogging.getParentTestStep() != null) {
				currentAction.setFailed(actionFailed);
			}
		}
		return reply;
	}
	
	/**
	 * Log any call to test steps (page object calls inside a PageObject subclass)
	 * @param joinPoint
	 * @throws Throwable 
	 */
	@Around("this(com.seleniumtests.uipage.PageObject) && " +					// caller is a PageObject
			"(call(public * com.seleniumtests.uipage.PageObject+.* (..))"
			+ "&& !call(public * com.seleniumtests.uipage.PageObject.* (..)))"			
			)
	public Object logSubTestStep(ProceedingJoinPoint joinPoint) throws Throwable {
		if (SeleniumTestsContextManager.getThreadContext().isManualTestSteps()) {
			return joinPoint.proceed(joinPoint.getArgs());
		}
		
		Object reply = null;
		
		String stepName = String.format("%s %s", joinPoint.getSignature().getName(), buildArgString(joinPoint));
		TestStep currentStep = new TestStep(stepName, TestLogging.getCurrentTestResult());
		TestStep previousParent = TestLogging.getParentTestStep();
		if (TestLogging.getParentTestStep() != null) {
			TestLogging.getParentTestStep().addStep(currentStep);
			TestLogging.setParentTestStep(currentStep);
		}
		
		try {
			reply = joinPoint.proceed(joinPoint.getArgs());
		} catch (Throwable e) {
			currentStep.setFailed(true);
			currentStep.setActionException(e);
			throw e;
		} finally {
			if (TestLogging.getParentTestStep() != null && previousParent != null) {
				TestLogging.setParentTestStep(previousParent);
			}				
		}
		return reply;
	}
	
	/**
	 * Log calls to cucumber annotated methods
	 * In case we are not inside a cucumber test, this method won't intercept anything because action will already be performed
	 * by logNonCucumberTestStep
	 * @param joinPoint
	 * @return
	 * @throws Throwable
	 */
	@Pointcut("(execution(@cucumber.api.java.en.When public * * (..)) || execution(@cucumber.api.java.en.Given public * * (..))) && if()")
	public static boolean isCucumberTest(ProceedingJoinPoint joinPoint) {
		return SeleniumRobotTestPlan.isCucumberTest();
	}
	
	@Around("isCucumberTest(joinPoint)")
	public Object logCucumberTestStep(ProceedingJoinPoint joinPoint) throws Throwable {
		return logTestStep(joinPoint);
	}	
	
	/**
	 * Log any call to test steps (page object calls inside a SeleniumTestPlan subclass)
	 * @param joinPoint
	 * @throws Throwable 
	 */
	@Around(
			"(!this(com.seleniumtests.uipage.PageObject) && " +							// caller is not a PageObject
			"(call(public * com.seleniumtests.uipage.PageObject+.* (..))"				// calls to methods in subclasses of PageObject class
			+ "&& !call(public * com.seleniumtests.uipage.PageObject.* (..)))"			// not calls to methods inside PageObject class
			+ "|| call(private * com.seleniumtests.uipage.PageObject.openPage (..)))"
			)
	public Object logNonCucumberTestStep(ProceedingJoinPoint joinPoint) throws Throwable {
		return logTestStep(joinPoint);
	}
	
	private Object logTestStep(ProceedingJoinPoint joinPoint)  throws Throwable {
		if (SeleniumTestsContextManager.getThreadContext().isManualTestSteps()) {
			return joinPoint.proceed(joinPoint.getArgs());
		}
		
		Object reply = null;
		boolean rootStep = false;
		TestStep previousParent = null;
		TestStep currentStep = new TestStep(buildRootStepName(joinPoint), TestLogging.getCurrentTestResult());
		
		// check if any root step is already registered (a main step)
		// happens when using cucumber where a cucumber method can call an other method intercepted by this pointcut
		// ex: Given (url "www.somesite.com") calls "open(url)"
		// In this case, open becomes a child of Given
		if (TestLogging.getCurrentRootTestStep() == null) {
			TestLogging.setCurrentRootTestStep(currentStep); // will also set parent step
			rootStep = true;
		} else {
			TestLogging.getParentTestStep().addStep(currentStep);
			previousParent = TestLogging.getParentTestStep();
			TestLogging.setParentTestStep(currentStep);
		}
		
		try {
			reply = joinPoint.proceed(joinPoint.getArgs());
		} catch (Throwable e) {
			currentStep.setFailed(true);
			currentStep.setActionException(e);
			throw e;
		} finally {
			if (rootStep) {
				TestLogging.getCurrentRootTestStep().updateDuration();
				TestLogging.logTestStep(TestLogging.getCurrentRootTestStep());	
			} else {
				TestLogging.setParentTestStep(previousParent);
			}
		}
		return reply;
	}
	

	/**
	 * Log composite action when they are declared
	 * @param joinPoint
	 */
	@After("this(com.seleniumtests.uipage.PageObject) && " +	
			"call(public org.openqa.selenium.interactions.Actions org.openqa.selenium.interactions.Actions.* (..))")
	public void logCompositeAction(JoinPoint joinPoint)  {
		String actionName = String.format("%s %s", joinPoint.getSignature().getName(), buildArgString(joinPoint));
		TestAction currentAction = new TestAction(actionName, false);
		
		if (TestLogging.getParentTestStep() != null) {
			TestLogging.getParentTestStep().addAction(currentAction);
		}
	}
	
	/**
	 * Build argument string of the join point
	 * @param joinPoint
	 * @return
	 */
	public static String buildArgString(JoinPoint joinPoint) {
		String argString = "";
		if (joinPoint.getArgs().length > 0) {
			argString = "with args: (";
			for (Object arg: joinPoint.getArgs()) {
				if (arg instanceof CharSequence[]) {
					argString += "[";
					for (Object obj: (CharSequence[])arg) {
						argString += obj.toString() + ",";
					}
					argString += "]";
				} else {
					argString += (arg == null ? "null": arg.toString()) + ", ";
				}
			}
			argString += ")";
		}
		return argString;
	}
	
	/**
	 * Returns step name depending on step type
	 * In case of cucumber step, get the annotation value. 
	 * /!\ THIS WORKS ONLY IF
	 * 	parameters of the annotated method are the Object version ones. Use 'Integer' instead of 'int' for example, when declaring 
	 * a cucumber method which uses an integer as parameter. Else method discovery won't find it and step name will fall back to method name
	 * 
	 * Else, get method name
	 * @param joinPoint
	 * @return
	 */
	private String buildRootStepName(JoinPoint joinPoint) {
		String stepName = String.format("%s %s", joinPoint.getSignature().getName(), buildArgString(joinPoint));		
		
		List<Class<?>> parameters = new ArrayList<>();
		for (Object arg: joinPoint.getArgs()) {
			parameters.add(arg == null ? Object.class: arg.getClass());
		}
		Class<?>[] pType  = parameters.toArray(new Class<?>[0]);
		Method method;
		try {
			method = joinPoint.getSignature().getDeclaringType().getDeclaredMethod(joinPoint.getSignature().getName(), pType);
		} catch (NoSuchMethodException | SecurityException e) {
			return stepName;
		}
		
		for (Annotation cucumberAnnotation: method.getAnnotations()) {
			if (cucumberAnnotation.annotationType().getCanonicalName().contains("cucumber.api.java.en")) {
				stepName = getAnnotationValue(cucumberAnnotation);
				stepName += " " + buildArgString(joinPoint);
				break;
			}
		}
		return stepName;
	}
	
	/**
	 * Returns the value of cucumber annotation to get corresponding text
	 * @param annotation
	 * @return
	 */
	private String getAnnotationValue(Annotation annotation) {
		return annotation.toString().replaceFirst("timeout=\\d+", "")
			.replace("@" + annotation.annotationType().getCanonicalName() + "(", "")
			.replaceFirst(",?\\s?value=", "")
			.replaceFirst("\\)$", "");
	}

}
