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
package com.seleniumtests.it.util;

import org.openqa.selenium.Dimension;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;

public class TestPageObject extends GenericDriverTest {

	
	@Test(groups={"it"})
	public void testResizeWindow() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setBrowser("*firefox");
		driver = WebUIDriver.getWebDriver(true);
		new DriverTestPage(true).resizeTo(600, 400);
		Dimension viewPortSize = ((CustomEventFiringWebDriver)driver).getViewPortDimensionWithoutScrollbar();
		Assert.assertEquals(viewPortSize.width, 600);
		Assert.assertEquals(viewPortSize.height, 400);
	}
	
	/**
	 * issue #421: check snapshot is not done when user set captureSnapshot=false
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testSnapshotNotLogged() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setBrowser("*firefox");
		SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(false);
		driver = WebUIDriver.getWebDriver(true);
		new DriverTestPage(true);
		
		// 0 capture because capture snapshot is set to false
		Assert.assertTrue(TestStepManager.getCurrentOrPreviousStep().getAllAttachments(true, null).isEmpty());

	}
	@Test(groups={"it"})
	public void testSnapshotLogged() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setBrowser("*firefox");
		SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(true);
		driver = WebUIDriver.getWebDriver(true);
		new DriverTestPage(true);
		
		// one capture, due to opening page
		Assert.assertFalse(TestStepManager.getCurrentOrPreviousStep().getAllAttachments(true, null).isEmpty());
		Assert.assertEquals(TestStepManager.getCurrentOrPreviousStep().getAllAttachments(true, null).size(), 1);
		
	}
	
	@Test(groups={"it"})
	public void testResizeWindowHeadless() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setBrowser("*htmlunit");
		driver = WebUIDriver.getWebDriver(true);
		new DriverTestPage(true);
		new DriverTestPage(true).resizeTo(600, 400);
		Dimension viewPortSize = ((CustomEventFiringWebDriver)driver).getViewPortDimensionWithoutScrollbar();
		Assert.assertEquals(viewPortSize.width, 600);
		Assert.assertEquals(viewPortSize.height, 400);
	}

}
