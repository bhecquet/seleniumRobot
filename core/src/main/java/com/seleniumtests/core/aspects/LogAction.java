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
package com.seleniumtests.core.aspects;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.openqa.selenium.support.ui.Select;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.SeleniumRobotTestPlan;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.StringUtility;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.core.har.Har;

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
    		+ "&& !execution(* com.seleniumtests.uipage.PageObject.addStep* (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.PageObject.capture*Snapshot (..))"
    		)
	public Object logPageObjectAction(ProceedingJoinPoint joinPoint) throws Throwable {
		PageObject page = (PageObject)joinPoint.getTarget();
		String pageName = page == null ? "": "on page " + page.getClass().getSimpleName();

    	return logAction(joinPoint, pageName);
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
		
		return commonLogTestStep(joinPoint, "", false);
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
		return logTestStep(joinPoint, "", false);
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
		return logTestStep(joinPoint, "", false);
	}
	
	/**
	 * Log any  \@BeforeTest \@BeforeClass \@BeforeMethod annotated method (and their \@After counterpart
	 * They will be used in reporting (ReporterControler) and added to regular test steps
	 * @param joinPoint
	 * @return
	 * @throws Throwable
	 */
	@Around("this(com.seleniumtests.core.runner.SeleniumRobotTestPlan) && "
			+ "(execution(@org.testng.annotations.BeforeMethod public * * (..))"
			+ "|| execution(@org.testng.annotations.BeforeClass public * * (..))"
			+ "|| execution(@org.testng.annotations.BeforeTest public * * (..))"
			+ ")")
	public Object logBeforeMethods(ProceedingJoinPoint joinPoint) throws Throwable {
		return logTestStep(joinPoint, "Pre test step: ", true);
	}
	
	@Around("this(com.seleniumtests.core.runner.SeleniumRobotTestPlan) && "
			+ "(execution(@org.testng.annotations.AfterMethod public * * (..))"
			+ "|| execution(@org.testng.annotations.AfterClass public * * (..))"
			+ "|| execution(@org.testng.annotations.AfterTest public * * (..))"
			+ ")")
	public Object logAfterMethods(ProceedingJoinPoint joinPoint) throws Throwable {
		return logTestStep(joinPoint, "Post test step: ", true);
	}
	
	/**
	 * Log this method call as a test step
	 * @param joinPoint			the join point
	 * @param stepNamePrefix	string to add before step name
	 * @param configStep		is this method call a TestNG configuration method (\@BeforeXXX or \@AfterXXX)
	 * @return
	 * @throws Throwable
	 */
	private Object logTestStep(ProceedingJoinPoint joinPoint, String stepNamePrefix, boolean configStep)  throws Throwable {
		
		// skip test logging when manual steps are active. This avoid having steps logged twice.
		// do not skip configuration step logging so that debugging remains easy
		if ((SeleniumTestsContextManager.getThreadContext().isManualTestSteps() && !configStep)
				// skip internal configuration steps
				|| joinPoint.getSignature().getDeclaringTypeName().startsWith("com.seleniumtests.core")
				) {
			return joinPoint.proceed(joinPoint.getArgs());
		}
		
		return commonLogTestStep(joinPoint, stepNamePrefix, configStep);
	}
	

	/**
	 * Log composite action when they are declared
	 * @param joinPoint
	 */
	@After("this(com.seleniumtests.uipage.PageObject) && " +	
			"call(public org.openqa.selenium.interactions.Actions org.openqa.selenium.interactions.Actions.* (..))")
	public void logCompositeAction(JoinPoint joinPoint)  {

    	List<String> pwdToReplace = new ArrayList<>();
		String actionName = String.format("%s %s", joinPoint.getSignature().getName(), buildArgString(joinPoint, pwdToReplace));
		TestAction currentAction = new TestAction(actionName, false, pwdToReplace);
		
		if (TestLogging.getParentTestStep() != null) {
			TestLogging.getParentTestStep().addAction(currentAction);
		}
	}
	
	/**
	 * Log native action only when we do not override them. Else, it' HTMLElement logging which is used
	 * @param joinPoint
	 * @return
	 */
	@Pointcut("(this(com.seleniumtests.uipage.PageObject) && " +	
			"(call(public * org.openqa.selenium.WebElement+.* (..)) "
			+ "&& !call(public * com.seleniumtests.uipage.htmlelements.HtmlElement+.* (..))" // correction of issue #88
			+ "|| call(public * org.openqa.selenium.support.ui.Select.* (..)))) && if()")
	public static boolean isNoNativeActionOverride(ProceedingJoinPoint joinPoint) {
		return !SeleniumTestsContextManager.getThreadContext().getOverrideSeleniumNativeAction();
	}
	
	@Around("isNoNativeActionOverride(joinPoint)")
	public Object logNativeAction(ProceedingJoinPoint joinPoint) throws Throwable {
		
		// build the name of the element
		String targetName = joinPoint.getTarget().toString();
		if (joinPoint.getTarget() instanceof Select) {
			targetName = "Select";
		} else if (targetName.contains("->")) {
			try {
				targetName = "Element located by" + targetName.split("->")[1].replace("]", "");
			} catch (IndexOutOfBoundsException e) {}
		}
		
		return logAction(joinPoint, targetName);
	}
	
	/**
	 * Build argument string of the join point
	 * If one of the arguments is a password (name contains 'password'), it's replaced and replacement is stored so that any subsequent call to this
	 * string is also replaced
	 * @param joinPoint
	 * @param stringToReplace	an empty list containing all strings to replace so that passwords cannot be visible
	 * @return
	 */
	public static String buildArgString(JoinPoint joinPoint, List<String> stringToReplace) {
		StringBuilder argString = new StringBuilder();
		if (joinPoint.getArgs().length > 0) {
			argString.append("with args: (");
			
			int paramIdx = 0;
			for (Object arg: joinPoint.getArgs()) {
				
				String argName = "";
				try {
					argName = ((MethodSignature)joinPoint.getSignature()).getParameterNames()[paramIdx];
				} catch (ClassCastException | IndexOutOfBoundsException e) {}
				
				// store the value of the argument containing a password
				if (arg != null && (argName.toLowerCase().contains("password") || argName.toLowerCase().contains("pwd") || argName.toLowerCase().contains("passwd"))) {
					if (arg instanceof CharSequence[]) {
						for (Object obj: (CharSequence[])arg) {
							stringToReplace.add(obj.toString());
						}
					} else if (arg instanceof List) {
						for (Object obj: (List<?>)arg) {
							stringToReplace.add(obj.toString());
						}
					} else {
						stringToReplace.add(arg.toString());
					}
				} 
				
				// add arguments to the name of the method
				if (arg instanceof CharSequence[]) {
					argString.append("[");
					for (Object obj: (CharSequence[])arg) {
						argString.append(obj.toString() + ",");
					}
					argString.append("]");
				} else {
					argString.append((arg == null ? "null": arg.toString()) + ", ");
				}
				paramIdx++;
			}
			argString.append(")");
		}
		return argString.toString();
	}
	
	/**
	 * Returns step with name depending on step type
	 * In case of cucumber step, get the annotation value. 
	 * /!\ THIS WORKS ONLY IF
	 * 	parameters of the annotated method are the Object version ones. Use 'Integer' instead of 'int' for example, when declaring 
	 * a cucumber method which uses an integer as parameter. Else method discovery won't find it and step name will fall back to method name
	 * 
	 * Else, get method name
	 * @param joinPoint
	 * @param returnArgs	if true, returns method arguments
	 * @return
	 */
	private TestStep buildRootStep(JoinPoint joinPoint, String stepNamePrefix, boolean returnArgs) {
		String stepName;
		List<String> pwdToReplace = new ArrayList<>();
		if (returnArgs) {
			stepName = String.format("%s %s", joinPoint.getSignature().getName(), buildArgString(joinPoint, pwdToReplace));
		} else {
			stepName = joinPoint.getSignature().getName();
		}
		
		// Get the method called by this joinPoint
		Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
		
		for (Annotation cucumberAnnotation: method.getAnnotations()) {
			if (cucumberAnnotation.annotationType().getCanonicalName().contains("cucumber.api.java.en")) {
				stepName = getAnnotationValue(cucumberAnnotation);
				pwdToReplace.clear();
				stepName += " " + buildArgString(joinPoint, pwdToReplace);
				break;
			}
		}
		return new TestStep(stepNamePrefix + stepName, TestLogging.getCurrentTestResult(), pwdToReplace);
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
	 * Log an action inside a TestStep
	 * @param joinPoint		the joinPoint
	 * @param targetName	target on which action is done (page or element)
	 * @return
	 * @throws Throwable 
	 */
	private Object logAction(ProceedingJoinPoint joinPoint, String targetName) throws Throwable {
		List<String> pwdToReplace = new ArrayList<>();
		String actionName = String.format("%s on %s %s", joinPoint.getSignature().getName(), targetName, buildArgString(joinPoint, pwdToReplace));
		Object reply = null;
		boolean actionFailed = false;
		TestAction currentAction = new TestAction(actionName, false, pwdToReplace);
		
		// log action before its started. By default, it's OK. Then result may be overwritten if step fails
		// order of steps is the right one (first called is first displayed)	
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
	 * Log a TestStep, inside a parent TestStep or not
	 * Common method used for all test step logging
	 * @return
	 * @throws Throwable 
	 */
	private Object commonLogTestStep(ProceedingJoinPoint joinPoint, String stepNamePrefix, boolean configStep) throws Throwable {
		Object reply = null;
		boolean rootStep = false;
		TestStep previousParent = null;
		
		// step name will contain method arguments only if it's not a configuration method (as they are generic)
		TestStep currentStep = buildRootStep(joinPoint, stepNamePrefix, !configStep);
		
		BrowserMobProxy mobProxy = WebUIDriver.getBrowserMobProxy();
		
		// check if any root step is already registered (a main step)
		// happens when using cucumber where a cucumber method can call an other method intercepted by this pointcut
		// ex: Given (url "www.somesite.com") calls "open(url)"
		// In this case, open becomes a child of Given
		// if rootStep is null, parent step is also null
		if (TestLogging.getCurrentRootTestStep() == null) {
			TestLogging.setCurrentRootTestStep(currentStep); // will also set parent step
			rootStep = true;
			
			if (mobProxy != null) {
				mobProxy.newPage(currentStep.getName());
			}
			
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

}
