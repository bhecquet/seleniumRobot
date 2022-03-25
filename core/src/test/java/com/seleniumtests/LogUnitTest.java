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
package com.seleniumtests;

import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;

@Aspect
public class LogUnitTest {
	
	private static final Logger logger = Logger.getRootLogger();

	@Before("execution(@org.testng.annotations.Test public * com.seleniumtests..* (..))")
	public void logTestStart(JoinPoint joinPoint)  { 
		logger.info("executing " + joinPoint.getSignature()); 
	}
	
	@After("execution(@org.testng.annotations.Test public * com.seleniumtests..* (..)) "
			+ "&& !execution(@org.testng.annotations.Test public * com.seleniumtests.it.stubclasses..* (..))"
			+ "&& !execution(@org.testng.annotations.Test public * com.seleniumtests.core.runner..* (..))")
	public void checkTestEnd(JoinPoint joinPoint)  { 
		
		// handle case where thread context is not defined (we get a ConfigurationException)
		try {
			SeleniumTestsContextManager.getThreadContext(); 
		} catch (ConfigurationException e) {
			return;
		}
		
		if (SeleniumTestsContextManager.getThreadContext() != null && SeleniumTestsContextManager.getThreadContext().isSoftAssertEnabled()) {
			throw new ConfigurationException("Soft assert is enabled. In case this is wanted by the test itself, then add a finally block which restores it to 'false'");
		}
	}
}
