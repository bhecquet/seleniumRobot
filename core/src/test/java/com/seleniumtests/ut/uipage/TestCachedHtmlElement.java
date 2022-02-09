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

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.uipage.htmlelements.CachedHtmlElement;

public class TestCachedHtmlElement extends GenericTest {
	
	@BeforeClass(groups={"ut"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		new DriverTestPage(true); // start displaying page
	}
	
	@AfterClass(groups={"ut"}, alwaysRun=true)
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testClick() {
		new CachedHtmlElement(DriverTestPage.selectList.getElement()).click();
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testSubmit() {
		new CachedHtmlElement(DriverTestPage.selectList.getElement()).submit();
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testSendKeys() {
		new CachedHtmlElement(DriverTestPage.textElement.getElement()).sendKeys("foo");
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testClear() {
		new CachedHtmlElement(DriverTestPage.textElement.getElement()).clear();
	}

	@Test(groups={"ut"})
	public void testTagName() {
		Assert.assertEquals(new CachedHtmlElement(DriverTestPage.selectList.getElement()).getTagName(), "select");
	}

	@Test(groups={"ut"})
	public void testGetText() {
		Assert.assertEquals(new CachedHtmlElement(DriverTestPage.selectList.getElement()).getText(), "option1 option2 option numero 3");
	}
	
	@Test(groups={"ut"})
	public void testGetAttribute() {
		Assert.assertEquals(new CachedHtmlElement(DriverTestPage.selectList.getElement()).getAttribute("name"), "select");
	}
	
	@Test(groups={"ut"})
	public void testIsSelected() {
		Assert.assertFalse(new CachedHtmlElement(DriverTestPage.selectList.getElement()).isSelected());
	}
	
	@Test(groups={"ut"})
	public void testIsOptionSelected() {
		DriverTestPage.selectList.selectByIndex(0);
		Assert.assertTrue(new CachedHtmlElement(DriverTestPage.selectList.findElement(By.tagName("option"))).isSelected());
	}
	
	@Test(groups={"ut"})
	public void testIsRadioSelected() {
		try {
			DriverTestPage.radioElement.click();
			Assert.assertTrue(new CachedHtmlElement(DriverTestPage.radioElement.getElement()).isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
	
	@Test(groups={"ut"})
	public void testIsRadioNotSelected() {
		try {
			Assert.assertFalse(new CachedHtmlElement(DriverTestPage.radioElement.getElement()).isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
	
	@Test(groups={"ut"})
	public void testIsCheckboxSelected() {
		try {
			DriverTestPage.checkElement.click();
			Assert.assertTrue(new CachedHtmlElement(DriverTestPage.checkElement.getElement()).isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
	
	@Test(groups={"ut"})
	public void testIsCheckboxNotSelected() {
		try {
			Assert.assertFalse(new CachedHtmlElement(DriverTestPage.checkElement.getElement()).isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
	
	@Test(groups={"ut"})
	public void testIsEnabled() {
		Assert.assertTrue(new CachedHtmlElement(DriverTestPage.selectList.getElement()).isEnabled());
	}
	
	@Test(groups={"ut"})
	public void testFindElementByTagName() {
		Assert.assertEquals(new CachedHtmlElement(DriverTestPage.selectList.getElement()).findElement(By.tagName("option")).getAttribute("value"), "opt1");
	}
	
	@Test(groups={"ut"})
	public void testFindElementById() {
		Assert.assertEquals(new CachedHtmlElement(DriverTestPage.parent.getElement()).findElement(By.id("child2")).getText(), "second child");
	}
	
	@Test(groups={"ut"})
	public void testFindElementByClassName() {
		Assert.assertEquals(new CachedHtmlElement(DriverTestPage.parent.getElement()).findElement(By.className("myClass")).getText(), "first child");
	}
	
	@Test(groups={"ut"})
	public void testFindElementByName() {
		Assert.assertEquals(new CachedHtmlElement(DriverTestPage.parent.getElement()).findElement(By.name("child4Name")).getText(), "fourth child");
	}
	
	@Test(groups={"ut"})
	public void testFindElementByLinkText() {
		Assert.assertEquals(new CachedHtmlElement(DriverTestPage.parentDiv.getElement()).findElement(By.linkText("My link Parent")).getAttribute("name"), "googleLink");
	}
	
	@Test(groups={"ut"})
	public void testFindElementsByTagName() {
		Assert.assertEquals(new CachedHtmlElement(DriverTestPage.selectList.getElement()).findElements(By.tagName("option")).size(), 3);
	}

	@Test(groups={"ut"})
	public void testIsDisplayed() {
		Assert.assertTrue(new CachedHtmlElement(DriverTestPage.selectList.getElement()).isDisplayed());
	}
	
	/**
	 * Check cached location, size and rectangle are set to 0 (for speed: issue #382)
	 */
	@Test(groups={"ut"})
	public void testGetLocation() {
		Assert.assertEquals(new CachedHtmlElement(DriverTestPage.selectList.getElement()).getLocation(), new Point(0, 0));
	}
	
	@Test(groups={"ut"})
	public void testGetSize() {
		Assert.assertEquals(new CachedHtmlElement(DriverTestPage.selectList.getElement()).getSize(), new Dimension(0, 0));
	}
	
	@Test(groups={"ut"})
	public void testGetRectangle() {
		// depends on where we execute the test, rectangle may throw an exception
//		Rectangle rect;
//		try {
//			rect = DriverTestPage.selectList.getElement().getRect();
//		} catch (WebDriverException e) {
//			rect = new Rectangle(DriverTestPage.selectList.getElement().getLocation(), DriverTestPage.selectList.getElement().getSize());
//		}
		Assert.assertEquals(new CachedHtmlElement(DriverTestPage.selectList.getElement()).getRect(), new Rectangle(0, 0, 0, 0));
	}
	
	
}
