/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.it.driver.support.pages.DriverSubTestPage;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;

/**
 * Test PageObject
 * @author behe
 *
 */
public class TestPageObject extends GenericTest {
	
	private static DriverTestPage testPage;

	@BeforeMethod(groups= {"it"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		testPage = new DriverTestPage(true);
	}
	
	@Test(groups= {"it"})
	public void testPageParam() {
		Assert.assertEquals(DriverTestPage.param("variable1"), "value3");
	}

	/**
	 * open 3 pages and check that when we close the last one, we go to the previous, not the first one 
	 * @throws Exception 
	 */
	@Test(groups= {"it"})
	public void testCloseLastTab() throws Exception {
		testPage._goToNewPage();
		testPage.getFocus();
		DriverSubTestPage subPage2 = testPage._goToNewPage();
		subPage2.close();
		
		// check we are on the seconde page (an instance of the DriverSubTestPage)
		// next line will produce error if we are not on the page
		new DriverSubTestPage();
	}
	
	/**
	 * open 2 pages and check that when we close the first one, we remain on the second one
	 * @throws Exception 
	 */
	@Test(groups= {"it"})
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
	@Test(groups= {"it"})
	public void testCloseFirstTabAndCheck() throws Exception {
		testPage._goToNewPage();
		testPage.getFocus().close(DriverSubTestPage.class);
	}
}
