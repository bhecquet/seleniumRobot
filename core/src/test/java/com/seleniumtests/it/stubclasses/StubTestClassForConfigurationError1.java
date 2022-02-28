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
package com.seleniumtests.it.stubclasses;

import java.lang.reflect.Method;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;

/**
 * Test class to product a BeforeXXX configuration error and look for logs in report
 * @author s047432
 *
 */
public class StubTestClassForConfigurationError1 extends StubParentClass {

	
	@BeforeMethod
	public void beforeMethod(Method method) {
		SeleniumTestsContextManager.getThreadContext().setManualTestSteps(true);
		throw new ConfigurationException("Some error before method");
	}
	
	@Test
	public void testWithABeforeMethodError(XmlTest xmlTest) {
		addStep("step 1");
		logger.info("some info");
	}
	
	

}
