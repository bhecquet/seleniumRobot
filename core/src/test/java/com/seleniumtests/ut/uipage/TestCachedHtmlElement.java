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
package com.seleniumtests.ut.uipage;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
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

	private static WebDriver driver;
	private static DriverTestPage testPage;
	
	@BeforeClass(groups={"ut"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
	}
	
	@AfterClass(groups={"ut"})
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testClick() {
		new CachedHtmlElement(testPage.selectList.getElement()).click();
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testSubmit() {
		new CachedHtmlElement(testPage.selectList.getElement()).submit();
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testSendKeys() {
		new CachedHtmlElement(testPage.textElement.getElement()).sendKeys("foo");
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testClear() {
		new CachedHtmlElement(testPage.textElement.getElement()).clear();
	}

	@Test(groups={"ut"})
	public void testTagName() {
		Assert.assertEquals(new CachedHtmlElement(testPage.selectList.getElement()).getTagName(), "select");
	}

	@Test(groups={"ut"})
	public void testGetText() {
		Assert.assertEquals(new CachedHtmlElement(testPage.selectList.getElement()).getText(), "option1 option2 option numero 3");
	}
	
	@Test(groups={"ut"})
	public void testGetAttribute() {
		Assert.assertEquals(new CachedHtmlElement(testPage.selectList.getElement()).getAttribute("name"), "select");
	}
	
	@Test(groups={"ut"})
	public void testIsSelected() {
		Assert.assertFalse(new CachedHtmlElement(testPage.selectList.getElement()).isSelected());
	}
	
	@Test(groups={"ut"})
	public void testIsOptionSelected() {
		testPage.selectList.selectByIndex(0);
		Assert.assertTrue(new CachedHtmlElement(testPage.selectList.findElement(By.tagName("option"))).isSelected());
	}
	
	@Test(groups={"ut"})
	public void testIsRadioSelected() {
		try {
			testPage.radioElement.click();
			Assert.assertTrue(new CachedHtmlElement(testPage.radioElement.getElement()).isSelected());
		} finally {
			testPage.resetButton.click();
		}
	}
	
	@Test(groups={"ut"})
	public void testIsRadioNotSelected() {
		try {
			Assert.assertFalse(new CachedHtmlElement(testPage.radioElement.getElement()).isSelected());
		} finally {
			testPage.resetButton.click();
		}
	}
	
	@Test(groups={"ut"})
	public void testIsCheckboxSelected() {
		try {
			testPage.checkElement.click();
			Assert.assertTrue(new CachedHtmlElement(testPage.checkElement.getElement()).isSelected());
		} finally {
			testPage.resetButton.click();
		}
	}
	
	@Test(groups={"ut"})
	public void testIsCheckboxNotSelected() {
		try {
			Assert.assertFalse(new CachedHtmlElement(testPage.checkElement.getElement()).isSelected());
		} finally {
			testPage.resetButton.click();
		}
	}
	
	@Test(groups={"ut"})
	public void testIsEnabled() {
		Assert.assertTrue(new CachedHtmlElement(testPage.selectList.getElement()).isEnabled());
	}
	
	@Test(groups={"ut"})
	public void testFindElementByTagName() {
		Assert.assertEquals(new CachedHtmlElement(testPage.selectList.getElement()).findElement(By.tagName("option")).getAttribute("value"), "opt1");
	}
	
	@Test(groups={"ut"})
	public void testFindElementById() {
		Assert.assertEquals(new CachedHtmlElement(testPage.parent.getElement()).findElement(By.id("child2")).getText(), "second child");
	}
	
	@Test(groups={"ut"})
	public void testFindElementByClassName() {
		Assert.assertEquals(new CachedHtmlElement(testPage.parent.getElement()).findElement(By.className("myClass")).getText(), "first child");
	}
	
	@Test(groups={"ut"})
	public void testFindElementByName() {
		Assert.assertEquals(new CachedHtmlElement(testPage.parent.getElement()).findElement(By.name("child4Name")).getText(), "fourth child");
	}
	
	@Test(groups={"ut"})
	public void testFindElementByLinkText() {
		Assert.assertEquals(new CachedHtmlElement(testPage.parentDiv.getElement()).findElement(By.linkText("My link Parent")).getAttribute("name"), "googleLink");
	}
	
	@Test(groups={"ut"})
	public void testFindElementsByTagName() {
		Assert.assertEquals(new CachedHtmlElement(testPage.selectList.getElement()).findElements(By.tagName("option")).size(), 3);
	}

	@Test(groups={"ut"})
	public void testIsDisplayed() {
		Assert.assertTrue(new CachedHtmlElement(testPage.selectList.getElement()).isDisplayed());
	}
	
	/**
	 * Check cached location is the same as the real element location
	 */
	@Test(groups={"ut"})
	public void testGetLocation() {
		Assert.assertEquals(new CachedHtmlElement(testPage.selectList.getElement()).getLocation(), testPage.selectList.getElement().getLocation());
	}
	
	@Test(groups={"ut"})
	public void testGetSize() {
		Assert.assertEquals(new CachedHtmlElement(testPage.selectList.getElement()).getSize(), testPage.selectList.getElement().getSize());
	}
	
	@Test(groups={"ut"})
	public void testGetRectangle() {
		// depends on where we execute the test, rectangle may throw an exception
		Rectangle rect;
		try {
			rect = testPage.selectList.getElement().getRect();
		} catch (WebDriverException e) {
			rect = new Rectangle(testPage.selectList.getElement().getLocation(), testPage.selectList.getElement().getSize());
		}
		Assert.assertEquals(new CachedHtmlElement(testPage.selectList.getElement()).getRect(), rect);
	}
	
	
}
