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
package com.seleniumtests.ut.uipage;

import java.time.LocalDateTime;

import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.TestType;

/**
 * Test class for checking seleniumNativeAction without override
 * When this option is false, we should have the selenium standard behavior
 * @author behe
 *
 */
public class TestNoOverrideSeleniumNativeSearch extends TestOverrideSeleniumNativeSearch {
	
	
	@BeforeMethod(groups={"ut"})
	public void overrideNativeActions() {
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(false);
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
	}
	
	
	@Test(groups={"ut"})
	public void testFindElementOverride() {
		// net.bytebuddy.renamed.java.lang.Object$ByteBuddy is the type of elements when driver is decorated
		Assert.assertTrue(testPage.getElement().getClass().getName().contains("net.bytebuddy.renamed.java.lang.Object$ByteBuddy"));
	}
	

	/**
	 * Test replay is done, we should be over 5 seconds which is the search time of driver
	 */
	@Test(groups={"ut"}, expectedExceptions = NoSuchElementException.class)
	public void testSendKeysElementAbsent() {
		LocalDateTime start = LocalDateTime.now();
		try {
			testPage.sendKeysFailed();
		} catch (NoSuchElementException e) {
			Assert.assertTrue(LocalDateTime.now().minusSeconds(8).isBefore(start));
			throw e;
		}
	}
	
}
