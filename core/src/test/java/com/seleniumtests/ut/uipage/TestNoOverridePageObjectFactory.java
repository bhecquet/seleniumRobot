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
 * Test class for checking seleniumNativeAction not override when Selenium PageObjectFactory is used
 * When this option is false, standard selenium behaviour should be preserved
 * @author behe
 *
 */
public class TestNoOverridePageObjectFactory extends TestOverridePageObjectFactory {

	
	@BeforeMethod(groups={"ut"})
	public void overrideNativeActions() {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(15);
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(false);
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
	}
	
	/**
	 * 
	 */
	@Test(groups={"ut"})
	public void testFindElementOverride() {
		Assert.assertNotEquals(testPage.textElement.toString(), "HtmlElement , by={By.id: text2}");
	}
	

	/**
	 * Test replay is not done
	 */
	@Test(groups={"ut"}, expectedExceptions = NoSuchElementException.class)
	public void testSendKeysElementAbsent() {
		LocalDateTime start = LocalDateTime.now();
		try {
			testPage.sendKeysFailed();
		} catch (NoSuchElementException e) {
			Assert.assertTrue(LocalDateTime.now().minusSeconds(13).isBefore(start));
			throw e;
		}
	}
	
}
