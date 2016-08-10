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

package com.seleniumtests.core.aspects;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.seleniumtests.core.runner.SeleniumRobotRunner;
import com.seleniumtests.reporter.TestAction;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.reporter.TestStep;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.HtmlElement;

/**
 * Aspect to intercept calls to methods of HtmlElement. It allows to retry discovery and action 
 * when something goes wrong with the driver
 * 
 * @author behe
 *
 */
@Aspect
public class LogAction {
	
	private TestStep currentRootTestStep = null;
	private TestStep parentTestStep = null;

	/**
	 * Intercept actions done through browser
	 * @param joinPoint
	 * @throws Throwable 
	 */
	@Around("(execution(public * com.seleniumtests.uipage.htmlelements..*Element..* (..)) "
    		+ "|| execution(public * com.seleniumtests.uipage.htmlelements.SelectList..* (..)) "
    		+ "|| execution(public * com.seleniumtests.uipage.htmlelements.Table..* (..))) "
    		+ "&& !execution(* com.seleniumtests.uipage.htmlelements.HtmlElement.toString (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.htmlelements.HtmlElement.get* (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.htmlelements.HtmlElement.set* (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.htmlelements.HtmlElement.find* (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.htmlelements.HtmlElement.wait* (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.htmlelements.HtmlElement.toHTML (..))"
    		)
	public Object logAction(ProceedingJoinPoint joinPoint) throws Throwable {
		HtmlElement element = (HtmlElement)joinPoint.getTarget();
		Object reply = null;
		boolean actionFailed = false;
		
		try {
			reply = joinPoint.proceed(joinPoint.getArgs());
		} catch (Exception e) {
			actionFailed = true;
			throw e;
		} finally {
			if (isHtmlElementDirectlyCalled(Thread.currentThread().getStackTrace()) && parentTestStep != null) {
				String actionName = String.format("%s on %s %s", joinPoint.getSignature().getName(), element, buildArgString(joinPoint));
				parentTestStep.addAction(new TestAction(actionName, actionFailed));
			}		
		}
		return reply;
	}
	
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
		
		try {
			reply = joinPoint.proceed(joinPoint.getArgs());
		} catch (Exception e) {
			actionFailed = true;
			throw e;
		} finally {
			if (parentTestStep != null) {
				parentTestStep.addAction(new TestAction(actionName, actionFailed));
			}
		}
		return reply;
	}
	
	/**
	 * Log any call to test steps (page object calls inside a SeleniumTestPlan subclass)
	 * @param joinPoint
	 * @throws Throwable 
	 */
	@Around("this(com.seleniumtests.uipage.PageObject) && " +
			"(call(public * com.seleniumtests.uipage.PageObject+.* (..))"
			+ "&& !call(public * com.seleniumtests.uipage.PageObject.* (..)))"			
			)
	public Object logSubTestStep(ProceedingJoinPoint joinPoint) throws Throwable {
		Object reply = null;
		
		String stepName = String.format("%s %s", joinPoint.getSignature().getName(), buildArgString(joinPoint));
		TestStep currentStep = new TestStep(stepName);
		TestStep previousParent = parentTestStep;
		if (parentTestStep != null) {
			parentTestStep.addStep(currentStep);
			parentTestStep = currentStep;
		}
		
		try {
			reply = joinPoint.proceed(joinPoint.getArgs());
		} catch (Exception e) {
			currentStep.setFailed(true);
			throw e;
		} finally {
			if (parentTestStep != null && previousParent != null) {
				parentTestStep = previousParent;
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
	@Pointcut("execution(@cucumber.api.java.en..* public * * (..)) && if()")
	public static boolean isCucumberTest(ProceedingJoinPoint joinPoint) {
		return SeleniumRobotRunner.isCucumberTest();
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
			"(!this(com.seleniumtests.uipage.PageObject) && " +
			"(call(public * com.seleniumtests.uipage.PageObject+.* (..))"
			+ "&& !call(public * com.seleniumtests.uipage.PageObject.* (..)))"
			+ "|| call(private * com.seleniumtests.uipage.PageObject.openPage (..)))"
			)
	public Object logNonCucumberTestStep(ProceedingJoinPoint joinPoint) throws Throwable {
		return logTestStep(joinPoint);
	}
	
	private Object logTestStep(ProceedingJoinPoint joinPoint)  throws Throwable {
		Object reply = null;
		boolean rootStep = false;
		TestStep previousParent = null;
		TestStep currentStep = new TestStep(buildRootStepName(joinPoint));
		
		// check if any root step is already registered
		// happens when using cucumber where a cucumber method can call an other method intercepted by this pointcut
		// ex: Given (url "www.somesite.com") calls "open(url)"
		// In this case, open becomes a child of Given
		if (currentRootTestStep == null) {
			currentRootTestStep = currentStep;
			parentTestStep = currentRootTestStep;
			rootStep = true;
		} else {
			parentTestStep.addStep(currentStep);
			parentTestStep = currentStep;
			previousParent = parentTestStep;
		}
		
		try {
			reply = joinPoint.proceed(joinPoint.getArgs());
		} catch (Exception e) {
			currentStep.setFailed(true);
			throw e;
		} finally {
			if (rootStep) {
				TestLogging.logTestStep(currentRootTestStep);	
				currentRootTestStep = null;
				parentTestStep = null;
			} else {
				parentTestStep = previousParent;
			}
		}
		return reply;
	}
	
	@Around("execution(public * com.seleniumtests.reporter.TestLogging.logWebOutput (..))"
			+ " || execution(public * com.seleniumtests.reporter.TestLogging.logWebStep (..))")
	public Object interceptLogging(ProceedingJoinPoint joinPoint) throws Throwable {
		if (parentTestStep != null) {
			Boolean actionFailed;
			try {
				actionFailed = (Boolean)joinPoint.getArgs()[1];
			} catch (ClassCastException | IndexOutOfBoundsException e) {
				actionFailed = false;
			}
			parentTestStep.addAction(new TestAction(joinPoint.getArgs()[0].toString(), actionFailed));
			return null;
		} else {
			return joinPoint.proceed(joinPoint.getArgs());
		}
	}
	
	/**
	 * Build argument string of the join point
	 * @param joinPoint
	 * @return
	 */
	private String buildArgString(JoinPoint joinPoint) {
		String argString = "";
		if (joinPoint.getArgs().length > 0) {
			argString = "with args: (";
			for (Object arg: joinPoint.getArgs()) {
				argString += (arg == null ? "null": arg.toString()) + ", ";
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
	
	/**
	 * Check whether this action has directly been performed on the HtmlElement (e.g: click)
	 * or through an other type of element (e.g: clic on LinkElement, redirected to HtmlElement)
	 * In this last case, do not log action as it has already been logged by the specific type of 
	 * element
	 * @param stack
	 * @return
	 */
	private boolean isHtmlElementDirectlyCalled(StackTraceElement[] stack) {
		String stackClass = null;
		boolean specificElementFound = false;
		boolean htmlElementFound = false;
		
		for(int i=0; i < stack.length; i++){
			 stackClass = stack[i].getClassName();
			 if (stackClass.equals("com.seleniumtests.uipage.htmlelements.HtmlElement")) {
				 htmlElementFound = true;
			 } else if (stackClass.startsWith("com.seleniumtests.uipage.htmlelements.")) {
				 specificElementFound = true;
			 }
		}
		if (htmlElementFound && specificElementFound) {
			return false;
		} else {
			return true;
		}
	}
}
