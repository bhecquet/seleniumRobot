/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.core.aspects;

import com.seleniumtests.reporter.logger.Check;
import com.seleniumtests.uipage.PageObject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.testng.Reporter;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.logging.ScenarioLogger;

import java.util.List;

@Aspect
public class SoftAssert {

	private static ScenarioLogger logger = ScenarioLogger.getScenarioLogger(SoftAssert.class);

	@Around("(call(public * org.testng.Assert..* (..)) " +
			"|| call(public * org.hamcrest.MatcherAssert..* (..))) " +
			"&& !call(public * org.testng.Assert.fail (..)) "
    		)
	public Object interceptAssertException(ProceedingJoinPoint joinPoint) throws Throwable {

		boolean rootCall = joinPoint.getThis() != null;


		try {
			joinPoint.proceed(joinPoint.getArgs());
			recordAssertionCheck(joinPoint, true, rootCall);
		} catch (AssertionError e) {
			recordAssertionCheck(joinPoint, false, rootCall);
			logAssertion(e, rootCall);
		}
		return null;
	}

	/**
	 * Record assertion check to TestStep
	 * If a message is provided, add it, else, only provide type of check
	 * @param assertStatus	true if assertion is OK
	 */
	private void recordAssertionCheck(ProceedingJoinPoint joinPoint, boolean assertStatus, boolean rootCall) {
		if (!rootCall) {
			return;
		}

		// in case assertion is done is scenario, assertion will be recorded as a root sub-step, as we are not in a step anymore
		TestStep currentTestStep = joinPoint.getThis() instanceof PageObject ? TestStepManager.getParentTestStep(): TestStepManager.getCurrentOrPreviousStep();
		try {
			List<String> parameters = List.of(((MethodSignature) joinPoint.getSignature()).getParameterNames());
			int messageParamIndex = parameters.indexOf("message"); // for testng
			if (messageParamIndex < 0) {
				messageParamIndex = parameters.indexOf("reason"); // for hamcrest
			}
			String message = "No Check message provided";
			try {
				message = (String) joinPoint.getArgs()[messageParamIndex];
			} catch (Exception e) {
				// ignore
			}
			if (currentTestStep != null) { // inside unit tests, currentTestStep is null
				currentTestStep.addCheck(new Check("Check: " + message, !assertStatus));
			}
		} catch (Exception e) {
			logger.error("Error recording assertion to test step", e);
		}

	}

	/**
	 *
	 * @param e					The exception
	 * @param rootCall			if true, it means that Assertion has been called from scenario or page, not from Assert class
	 */
	private void logAssertion(Throwable e, boolean rootCall) throws Throwable {
		TestStep currentTestStep = TestStepManager.getCurrentOrPreviousStep();
		try {
			
			if (SeleniumTestsContextManager.hasThreadContext()
					&& SeleniumTestsContextManager.getThreadContext().isSoftAssertEnabled()
					&& rootCall) { // log only when caller is not Assert class itself so that assertion are not logged twice
				SeleniumTestsContextManager.getThreadContext().addVerificationFailures(Reporter.getCurrentTestResult(), e);

				logger.error("!!!FAILURE ALERT!!! - Assertion Failure: " + e.getMessage());
				TestTasks.capturePageSnapshot();

			} else {
				if (rootCall) {
					logger.error("Assertion Failure: " + e.getMessage());
				}
				throw e;
			}
		} finally {
			if (currentTestStep != null) {
				// assertion should be clearly seen in report
				currentTestStep.setFailed(true);
				currentTestStep.setActionException(e);
			}
		}
	}
}
