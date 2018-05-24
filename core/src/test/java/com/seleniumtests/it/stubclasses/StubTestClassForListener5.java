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
package com.seleniumtests.it.stubclasses;

import java.lang.reflect.Method;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;

/**
 * Stubclass for testing issue #136 and driver creation blocking
 * @author s047432
 *
 */
public class StubTestClassForListener5 extends StubTestClassForListenerParent {
	
	private void startDriver() {
		WebDriver driver = WebUIDriver.getWebDriver();
		driver.get("file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile());
	}
	
	@BeforeSuite(groups={"stub1"})
	public void beforeSuite() {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		if ("beforeSuite".equals(System.getProperty("startLocation"))) {
			startDriver();
		}
		logger.info("start suite");
	}
	
	@BeforeGroups(groups={"stub1"})
	public void beforeGroup() {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		if ("beforeGroup".equals(System.getProperty("startLocation"))) {
			startDriver();
		}
		logger.info("start group");
	}

	@BeforeTest(groups={"stub1"})
	public void beforeTest(XmlTest xmlTest) {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		if ("beforeTest".equals(System.getProperty("startLocation"))) {
			startDriver();
		}
		logger.info("start test");
	}

	@BeforeClass(groups={"stub1"})
	public void beforeClass() {
		if ("beforeClass".equals(System.getProperty("startLocation"))) {
			startDriver();
		}
		logger.info("start class");
	}

	@BeforeMethod(groups={"stub1"})
	public void beforeMethod(Method method) {
		if ("beforeMethod".equals(System.getProperty("startLocation"))) {
			startDriver();
		}
		logger.info("start method");
	}
	
	@Test(groups="stub1")
	public void test1Listener5(XmlTest xmlTest) {
		if ("test".equals(System.getProperty("startLocation"))) {
			startDriver();
		}
		startDriver();
		logger.info("test 1");
	}
	
	@Test(groups="stub1")
	public void test2Listener5() {
		if ("test".equals(System.getProperty("startLocation"))) {
			startDriver();
		}
		logger.info("test 2");
	}
	
	@AfterMethod(groups={"stub1"})
	public void afterMethod(Method method, XmlTest xmlTest) {
		if ("afterMethod".equals(System.getProperty("startLocation"))) {
			startDriver();
		}
		logger.info("end method");
	}

	@AfterClass(groups={"stub1"})
	public void afterClass(XmlTest xmlTest) {
		if ("afterClass".equals(System.getProperty("startLocation"))) {
			startDriver();
		}
		logger.info("end class");
	}
	
	@AfterTest(groups={"stub1"})
	public void afterTest(XmlTest xmlTest) {
		if ("afterTest".equals(System.getProperty("startLocation"))) {
			startDriver();
		}
		logger.info("end test");
	}
	
	@AfterGroups(groups={"stub1"})
	public void afterGroup() {
		if ("afterGroup".equals(System.getProperty("startLocation"))) {
			startDriver();
		}
		logger.info("end group");
	}
	
	@AfterSuite(groups={"stub1"})
	public void afterSuite() {
		if ("afterSuite".equals(System.getProperty("startLocation"))) {
			startDriver();
		}
		logger.info("end suite");
	}

}
