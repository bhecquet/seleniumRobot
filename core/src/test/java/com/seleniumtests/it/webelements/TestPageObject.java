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
package com.seleniumtests.it.webelements;

import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.GenericTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverSubTestPage;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;

/**
 * Test PageObject
 * @author behe
 *
 */
public class TestPageObject extends GenericTest {
	
	private static DriverTestPage testPage;

	@BeforeMethod(groups= {"it", "pageobject"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {

		OSUtilityFactory.getInstance().killProcessByName("chrome", true);
		
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		testPage = new DriverTestPage(true);
	}
	
	@AfterMethod(groups= {"it", "pageobject"}, alwaysRun=true)
	public void destroyDriver() {
		WebUIDriver.cleanUp();

		GenericTest.resetTestNGREsultAndLogger();
	}
	
	/**
	 * Depends on TestNG XML file so it won't work when launched from IDE 
	 */
	@Test(groups= {"it", "pageobject"})
	public void testPageParam() {
		Assert.assertEquals(DriverTestPage.param("variable1"), "value3");
	}

	/**
	 * open 3 pages and check that when we close the last one, we go to the previous, not the first one 
	 * @throws Exception 
	 */
	@Test(groups= {"it", "pageobject"})
	public void testCloseLastTab() throws Exception {
		testPage._goToNewPage();
		testPage.getFocus();
		DriverSubTestPage subPage2 = testPage._goToNewPage();
		subPage2.close();
		
		// check we are on the second page (an instance of the DriverSubTestPage)
		// next line will produce error if we are not on the page
		new DriverSubTestPage();
	}
	
	/**
	 * open 2 pages and check that when we close the first one, we remain on the second one
	 * @throws Exception 
	 */
	@Test(groups= {"it", "pageobject"})
	public void testCloseFirstTab() throws Exception {
		testPage._goToNewPage();
		testPage.getFocus().close();
		
		// check we are on the seconde page (an instance of the DriverSubTestPage)
		// next line will produce error if we are not on the page
		new DriverSubTestPage();
	}
	
	/**
	 * open 2 pages and check that when we close the first one, we remain on the second one
	 * Use the embedded check inside close method
	 * @throws Exception 
	 */
	@Test(groups= {"it", "pageobject"})
	public void testCloseFirstTabAndCheck() throws Exception {
		testPage._goToNewPage();
		testPage.getFocus().close(DriverSubTestPage.class);
	}
	
	/**
	 * issue #324: check handles are not null. We cannot directly reproduce problem because we should have a test site which creates a second window when opened
	 * @throws Exception
	 */
	@Test(groups= {"it", "pageobject"})
	public void testPageObjectForExternalDriver() throws Exception {
		
		try {
			SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
			
			Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
			String path = browsers.get(BrowserType.CHROME).get(0).getPath();
			int port = GenericDriverTest.findFreePort();
			
			// create chrome browser with the right option
			OSCommand.executeCommand(new String[] {path, "--remote-allow-origins=*",  "--remote-debugging-port=" + port, "about:blank"});
			
			DriverTestPage secondPage = new DriverTestPage(BrowserType.CHROME, port);
			Assert.assertNotNull(secondPage.getCurrentHandles());
		} finally {
			
			// switch back to main driver to avoid annoying other tests
			testPage.switchToDriver("main");
		}
		
	}
}
