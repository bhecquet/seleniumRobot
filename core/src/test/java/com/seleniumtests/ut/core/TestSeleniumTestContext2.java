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
package com.seleniumtests.ut.core;

import java.io.File;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.mockito.Mock;
import org.openqa.selenium.Proxy.ProxyType;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.TestRunner;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.GenericTest;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.connectors.tms.TestManager;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverMode;

/**
 * Test parsing of test options into SeleniumTestContext
 * Tests will only be done on ThreadContext
 * This test MUST be executed through the tu.xml file as this file defines some parameters used by this test
 * @author behe
 *
 */
@PrepareForTest({SeleniumRobotVariableServerConnector.class, SeleniumTestsContext.class})
public class TestSeleniumTestContext2 {

	@Test(groups="ut")
	public void testOutputDirectoryFromSystem(final ITestContext testNGCtx, final XmlTest xmlTest) {
		System.setProperty(SeleniumTestsContext.OUTPUT_DIRECTORY, "/home/user/test-output");
		Assert.assertEquals(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "/home/user/test-output");
	}

	
}
