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
package com.seleniumtests.core.aspects;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.uipage.BasePage;
import com.seleniumtests.uipage.ReplayOnError;
import com.seleniumtests.uipage.htmlelements.Element;
import com.seleniumtests.uipage.htmlelements.GenericPictureElement;
import com.seleniumtests.uipage.htmlelements.SeleniumElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;

import com.neotys.selenium.proxies.NLWebDriver;
import com.seleniumtests.core.Mask;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.Step;
import com.seleniumtests.core.Step.RootCause;
import com.seleniumtests.core.StepName;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.core.runner.SeleniumRobotTestPlan;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.util.logging.ScenarioLogger;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.video.VideoRecorder;

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
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(LogAction.class);
	private static final ScenarioLogger scenarioLogger = ScenarioLogger.getScenarioLogger(LogAction.class);
	private static Map<Thread, Integer> indent = Collections.synchronizedMap(new HashMap<>());

	/**
	 * Intercept actions and log them only if a step is already defined
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
    	return logAction(joinPoint, page);
	}
	
	@Around("execution(public * com.seleniumtests.uipage.PageObject+.* (..)) "
			+ "|| execution(public * com.seleniumtests.uipage.htmlelements.HtmlElement+.* (..))")
	public Object logDebug(ProceedingJoinPoint joinPoint) throws Throwable {
		if (LogAction.indent.get(Thread.currentThread()) == null) {
			LogAction.indent.put(Thread.currentThread(), 0);
		}
		
		String currentIndent = StringUtils.repeat(" ", LogAction.indent.get(Thread.currentThread()));
		logger.debug(String.format("%sEntering %s", currentIndent, joinPoint.getSignature()));
		Object reply = null;
		try {
			LogAction.indent.put(Thread.currentThread(), LogAction.indent.get(Thread.currentThread()) + 2);
			reply = joinPoint.proceed(joinPoint.getArgs());
		} catch (Throwable e) {
			logger.debug(String.format("%sError in %s: %s - %s", currentIndent, joinPoint.getSignature(), e.getClass().getName(), e.getMessage()));
			throw e;
		} finally {
			LogAction.indent.put(Thread.currentThread(), LogAction.indent.get(Thread.currentThread()) - 2);
			logger.debug(String.format("%sFinishing %s: %s", currentIndent, joinPoint.getSignature(), buildReplyValues(reply)));
		}
		return reply;
	}
	
	private String buildReplyValues(Object reply) {
		List<String> replyList = new ArrayList<>();
		if (reply == null) {
			return "null";
		}
		if (reply instanceof CharSequence[]) {
			for (Object obj: (CharSequence[])reply) {
				replyList.add(obj.toString());
			}
		} else if (reply instanceof List) {
			for (Object obj: (List<?>)reply) {
				replyList.add(obj.toString());
			}
		} else {
			replyList.add(reply.toString());
		}
		return replyList.toString();
	}
	
	/**
	 * Log any call to test steps (page object calls inside a PageObject subclass)
	 * @param joinPoint
	 * @throws Throwable 
	 */
	@Around("this(com.seleniumtests.uipage.PageObject) && " // caller is a PageObject
			+ "(" 
			+ 		"(call(public * com.seleniumtests.uipage.PageObject+.* (..))"
			+ 		"&& !call(public * com.seleniumtests.uipage.PageObject.* (..))" // do not log PageObject internal methods		
			+ 		")"
			+ "|| execution(@com.seleniumtests.uipage.GenericStep public * * (..))"	// log PageObject methods annotated with GenericStep
			+ ")"	
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
	@Pointcut("(execution(@io.cucumber.java.en.When public * * (..)) "
				+ "|| execution(@io.cucumber.java.en.Given public * * (..))"
				+ "|| execution(@io.cucumber.java.fr.Soit public * * (..))"
				+ "|| execution(@io.cucumber.java.fr.Lorsque public * * (..))) "
			+ "&& if()")
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
				|| (joinPoint.getSignature().getDeclaringTypeName().startsWith("com.seleniumtests.core") 
						// do not skip generic cucumber steps
						&& !joinPoint.getSignature().getDeclaringTypeName().startsWith("com.seleniumtests.core.runner.cucumber"))
				) {
			return joinPoint.proceed(joinPoint.getArgs());
		}
		
		return commonLogTestStep(joinPoint, stepNamePrefix, configStep);
	}
	
	/**
	 * Log native action only when we do not override them. Else, it's HTMLElement logging which is used
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
		Element target = null;
		if (joinPoint.getTarget() instanceof Select) {
			target = new SeleniumElement((Select)joinPoint.getTarget());
		} else if (joinPoint.getTarget() instanceof WebElement) {
			target = new SeleniumElement((WebElement)joinPoint.getTarget());
		}

		target.setCallingPage((PageObject) joinPoint.getThis());

		
		return logAction(joinPoint, target);
	}
	
	/**
	 * Build argument string of the join point
	 * If one of the arguments is a password (name contains 'password'), it's replaced and replacement is stored so that any subsequent call to this
	 * string is also replaced
	 * @param joinPoint
	 * @param stringToReplace	an empty list containing all strings to replace so that passwords cannot be visible
	 * @param argValues			an empty map which will be filled with argument name and value
	 * @return
	 */
	public static String buildArgString(JoinPoint joinPoint, List<String> stringToReplace, Map<String, String> argValues) {
		StringBuilder argString = new StringBuilder();
		if (joinPoint.getArgs().length > 0) {
			argString.append("with args: (");
			
			int paramIdx = 0;
			for (Object arg: joinPoint.getArgs()) {
				
				String argName = "";
				try {
					argName = ((MethodSignature)joinPoint.getSignature()).getParameterNames()[paramIdx];
				} catch (ClassCastException | IndexOutOfBoundsException e) {
					argName = "_";
				}

				// store the value of the argument containing a password
				addPasswordsToReplacements(joinPoint, stringToReplace, paramIdx, arg, argName); 
				
				StringBuilder argValue = new StringBuilder();
				// add arguments to the name of the method
				if (arg instanceof Object[]) {
					argValue.append("[");
					for (Object obj : (Object[]) arg) {
						argValue.append(obj.toString() + ",");
					}
					argValue.append("]");
				} else if (arg instanceof Element) {
					argValue.append(((Element) arg).toString());
				} else if (arg instanceof WebElement) {
					argValue.append(new SeleniumElement((WebElement)arg).getName());
				} else {
					argValue.append((arg == null ? "null": arg.toString()));
				}
				argString.append(String.format("%s, ", argValue.toString()));
				argValues.put(argName, argValue.toString());
				paramIdx++;
			}
			argString.append(")");
		}
		return argString.toString();
	}

	/**
	 * @param joinPoint
	 * @param stringToReplace
	 * @param paramIdx
	 * @param arg
	 * @param argName
	 */
	private static void addPasswordsToReplacements(JoinPoint joinPoint, List<String> stringToReplace, int paramIdx,
			Object arg, String argName) {
		if (arg != null && (argName.toLowerCase().contains("password") 
				|| argName.toLowerCase().contains("pwd") 
				|| argName.toLowerCase().contains("passwd")
				|| ((MethodSignature)joinPoint.getSignature()).getMethod().getParameters()[paramIdx].getAnnotationsByType(Mask.class).length > 0)) {
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
		Map<String, String> arguments = new HashMap<>();
		String argumentString = buildArgString(joinPoint, pwdToReplace, arguments);
		if (returnArgs) {
			stepName = String.format("%s %s", joinPoint.getSignature().getName(), argumentString);
		} else {
			stepName = joinPoint.getSignature().getName();
		}
		
		// Get the method called by this joinPoint
		Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
		RootCause errorCause = RootCause.NONE;
		String errorCauseDetails = null;
		boolean disableBugtracker = false;
		
		
		for (Annotation annotation: method.getAnnotations()) {
			if ((annotation.annotationType().getCanonicalName().contains("io.cucumber.java.en") 
				|| annotation.annotationType().getCanonicalName().contains("io.cucumber.java.fr")) 
				&& SeleniumRobotTestPlan.isCucumberTest()) {
				stepName = getAnnotationValue(annotation) + " " + argumentString;
				break;
			} else if (annotation instanceof StepName) {
				stepName = ((StepName)annotation).value();
				
				// replaces argument placeholders with values
				for (Entry<String, String> entry: arguments.entrySet()) {
					stepName = stepName.replaceAll(String.format("\\$\\{%s\\}",  entry.getKey()), entry.getValue().replace("$", "\\$"));
				}
				break;
			} else if (annotation instanceof Step) {
				stepName = ((Step)annotation).name();
				errorCause = ((Step)annotation).errorCause();
				errorCauseDetails = ((Step)annotation).errorCauseDetails();
				disableBugtracker = ((Step)annotation).disableBugTracker();
				
				// replaces argument placeholders with values
				for (Entry<String, String> entry: arguments.entrySet()) {
					stepName = stepName.replaceAll(String.format("\\$\\{%s\\}",  entry.getKey()), entry.getValue().replace("$", "\\$"));
				}
				break;
			}
		}
		return new TestStep(stepNamePrefix + stepName, 
				Reporter.getCurrentTestResult(), 
				pwdToReplace, 
				SeleniumTestsContextManager.getThreadContext().getMaskedPassword(),
				errorCause,
				errorCauseDetails, 
				disableBugtracker);
	}
	
	/**
	 * Returns the value of cucumber annotation to get corresponding text
	 * @param annotation
	 * @return
	 */
	private String getAnnotationValue(Annotation annotation) {
		return StringEscapeUtils.unescapeJava(annotation.toString().replaceFirst("timeout=\\d+", "")
				.replace("@" + annotation.annotationType().getCanonicalName() + "(\"", "")
				.replaceFirst(",?\\s?value=", "")
				.replaceFirst("\"\\)$", ""));
	}
	
	/**
	 * Log an action inside a TestStep
	 * @param joinPoint		the joinPoint
	 * @param target		target on which action is done (page or element)
	 * @return
	 * @throws Throwable 
	 */
	private Object logAction(ProceedingJoinPoint joinPoint, Object target) throws Throwable {
		List<String> pwdToReplace = new ArrayList<>();
		Object reply = null;
		boolean actionFailed = false;
		TestAction currentAction = null;

		if (target instanceof PageObject) {
			String pageName = target == null ? "": " on page " + target.getClass().getSimpleName();
			String actionName = String.format("%s%s %s", joinPoint.getSignature().getName(), pageName, buildArgString(joinPoint, pwdToReplace, new HashMap<>()));
			currentAction = new TestAction(actionName, false, pwdToReplace, joinPoint.getSignature().getName(), (Class<? extends PageObject>) target.getClass());
		} else if (target instanceof Element) {
			String actionName = String.format("%s on Element located by %s %s", joinPoint.getSignature().getName(), ((Element) target).getName(), buildArgString(joinPoint, pwdToReplace, new HashMap<>()));
			currentAction = new TestAction(actionName, false, pwdToReplace, joinPoint.getSignature().getName(), (Element)target);
		} else {
			currentAction = new TestAction(joinPoint.getSignature().getName(), false, pwdToReplace);
		}
		Throwable currentException = null;
		
		// log action before its started. By default, it's OK. Then result may be overwritten if step fails
		// order of steps is the right one (first called is first displayed)	
		if (TestStepManager.getParentTestStep() != null) {
			TestStepManager.getParentTestStep().addAction(currentAction);
		}
		
		try {
			reply = joinPoint.proceed(joinPoint.getArgs());
		} catch (Throwable e) {
			actionFailed = true;
			currentException = e;
			throw e;
		} finally {
			if (TestStepManager.getParentTestStep() != null) {
				currentAction.setFailed(actionFailed);
				scenarioLogger.logActionError(currentException);
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
		
		if ("openPage".equals(joinPoint.getSignature().getName()) && joinPoint.getTarget() instanceof PageObject) {
			PageObject page = (PageObject)joinPoint.getTarget();
			currentStep.addAction(new TestAction(String.format("Opening page %s",  page.getClass().getSimpleName()), false, new ArrayList<>(), "openPage", page.getClass()));
		}

		NLWebDriver neoloadDriver = WebUIDriver.getNeoloadDriver();
		VideoRecorder videoRecorder = WebUIDriver.getThreadVideoRecorder();
		
		// check if any root step is already registered (a main step)
		// happens when using cucumber where a cucumber method can call an other method intercepted by this pointcut
		// ex: Given (url "www.somesite.com") calls "open(url)"
		// In this case, open becomes a child of Given
		// if rootStep is null, parent step is also null
		if (TestStepManager.getCurrentRootTestStep() == null) {
			TestStepManager.setCurrentRootTestStep(currentStep); // will also set parent step
			rootStep = true;

			if (videoRecorder != null) {
				CustomEventFiringWebDriver.displayStepOnScreen(currentStep.getName(), 
						SeleniumTestsContextManager.getThreadContext().getRunMode(), 
						SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector(), 
						videoRecorder);
			}
			if (neoloadDriver != null) {
				neoloadDriver.startTransaction(currentStep.getName());
			}
			
			
			
		} else {
			TestStepManager.getParentTestStep().addStep(currentStep);
			previousParent = TestStepManager.getParentTestStep();
			TestStepManager.setParentTestStep(currentStep);
		}
		
		// set the start date once step is initialized so that when we get the video frame associated to step, step name displayed on screen is the same as the running step name
		currentStep.setStartDate();
		
		try {
			reply = joinPoint.proceed(joinPoint.getArgs());	
		} catch (Throwable e) {
			currentStep.setFailed(true);
			currentStep.setActionException(e);
			
			// issue #287 (https://github.com/cbeust/testng/issues/2148): is method an @AfterMethod. Then do not rethrow exception
			MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
			if (methodSignature.getMethod().getAnnotation(AfterMethod.class) != null) {
				scenarioLogger.error(String.format("Error in @AfterMethod %s: %s", methodSignature, e.getMessage()));
			} else {
				throw e;
			}
		} finally {
			if (rootStep) {
				TestStepManager.getCurrentRootTestStep().updateDuration();
				TestStepManager.logTestStep(TestStepManager.getCurrentRootTestStep());
				
				if (neoloadDriver != null) {
					neoloadDriver.stopTransaction();
				}
			} else {
				TestStepManager.setParentTestStep(previousParent);
			}
		}
		return reply;
	}
	/**
	 * Log actions on generic picture elements
	 * @param joinPoint
	 * @param replay
	 * @return
	 * @throws Throwable
	 */
	@Around("execution(public * com.seleniumtests.uipage.htmlelements.GenericPictureElement+.* (..))"
			+ "&& execution(@com.seleniumtests.uipage.ReplayOnError public * * (..)) && @annotation(replay)")
	public Object logGenericPictureElements(ProceedingJoinPoint joinPoint, ReplayOnError replay) throws Throwable {

		// #667: code moved to ReplayAction aspect but method kept here for external use (to avoid recompilation)
		return joinPoint.proceed(joinPoint.getArgs());

	}

	/**
	 * Log composite action when they are declared
	 * This will create a step that will enclose all actions (click, pause, ...). This step will be closed when perform will be closed
	 * @param joinPoint
	 */
	@After("this(com.seleniumtests.uipage.PageObject) && " +
			"call(public org.openqa.selenium.interactions.Actions org.openqa.selenium.interactions.Actions.* (..))")
	public void logCompositeAction(JoinPoint joinPoint)  {

		TestStep compositeActionStep = TestStepManager.getParentTestStep();

		// check if we already have a step created for storing composite actions
		if (compositeActionStep != null && !compositeActionStep.getName().startsWith(TestStep.COMPOSITE_STEP_PREFIX)) {
			compositeActionStep = new TestStep(TestStep.COMPOSITE_STEP_PREFIX);
			TestStepManager.getParentTestStep().addStep(compositeActionStep);
			TestStepManager.setParentTestStep(compositeActionStep);
		}

		List<String> pwdToReplace = new ArrayList<>();
		String actionName = String.format("%s %s", joinPoint.getSignature().getName(), buildArgString(joinPoint, pwdToReplace, new HashMap<>()));

		Element targetElement = null;
		Class<? extends PageObject> targetClass = null;
		// for HtmlElement / GenericPictureElement
		if (joinPoint.getArgs().length > 0 && (joinPoint.getArgs()[0] instanceof Element)) {
			targetElement = (Element) joinPoint.getArgs()[0];
			targetClass = targetElement.getOriginClass();
		// for Selenium WebElement
		} else if (joinPoint.getArgs().length > 0 && (joinPoint.getArgs()[0] instanceof WebElement)) {
			targetElement = new SeleniumElement((WebElement)joinPoint.getArgs()[0]);
			targetClass = ((PageObject)joinPoint.getThis()).getClass();
		}

		TestAction currentAction = new TestAction(actionName, false, pwdToReplace, joinPoint.getSignature().getName(), targetElement, targetClass);

		if (compositeActionStep != null) {
			compositeActionStep.addAction(currentAction);
		}
	}

	/**
	 * Log composite actions when perform is done
	 * We expect some actions have been logged in a special TestStep. This step will be closed
	 * @param joinPoint
	 * @return
	 * @throws Throwable
	 */
	@Around("execution(public void org.openqa.selenium.interactions.Actions.BuiltAction.perform ())")
	public Object logPerformCompositeAction(ProceedingJoinPoint joinPoint) throws Throwable {

		TestStep currentStep = TestStepManager.getParentTestStep();
		TestStep compositeActionStep = null;

		// check if we have a step that stored composite actions
		// if yes, then co
		if (currentStep != null && currentStep.getName().startsWith(TestStep.COMPOSITE_STEP_PREFIX)) {
			compositeActionStep = currentStep;
		}

		boolean actionFailed = false;
		Throwable currentException = null;

		try {
			return joinPoint.proceed(joinPoint.getArgs());
		} catch (Throwable e) {
			actionFailed = true;
			currentException = e;
			throw e;
		} finally {
			if (compositeActionStep != null) {
				compositeActionStep.setFailed(actionFailed);

				// change the name of composite action
				StringBuilder name = new StringBuilder(TestStep.COMPOSITE_STEP_PREFIX);
				Element mainElement = null; // the last element on which we act

				for (TestAction action: compositeActionStep.getStepActions()) {
					name.append(action.getAction() + ",");
					if (action.getElement() != null) {
						mainElement = action.getElement();
					}
					action.setFailed(actionFailed);
				}
				if (mainElement != null) {
					String elementName = mainElement instanceof SeleniumElement ? mainElement.getName() : mainElement.toString();
					name.append("on element '" + elementName + "'");
				}
				compositeActionStep.setName(name.toString());

				scenarioLogger.logActionError(currentException);
				TestStepManager.setParentTestStep((TestStep)compositeActionStep.getParent());
			}
		}
	}
}
