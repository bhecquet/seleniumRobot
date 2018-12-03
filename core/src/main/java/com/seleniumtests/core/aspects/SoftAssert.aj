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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.testng.Reporter;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.logger.TestLogging;

@Aspect
public class SoftAssert {

	@Around("(call(public * org.testng.Assert..* (..)) " +
			"|| call(public * org.hamcrest.MatcherAssert..* (..))) " +
			"&& !call(public * org.testng.Assert.fail (..)) "
    		)
	public Object interceptAssertException(ProceedingJoinPoint joinPoint) throws Throwable {
		try {
			return joinPoint.proceed(joinPoint.getArgs());
		} catch (AssertionError e) {
			if (SeleniumTestsContextManager.getThreadContext().isSoftAssertEnabled()) {
				SeleniumTestsContextManager.getThreadContext().addVerificationFailures(Reporter.getCurrentTestResult(), e);
		        TestLogging.error("!!!FAILURE ALERT!!! - Assertion Failure: " + e.getMessage());
		        return null;
			} else {
				throw e;
			}
		}
	}
}
