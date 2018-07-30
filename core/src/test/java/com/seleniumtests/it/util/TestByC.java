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
package com.seleniumtests.it.util;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.uipage.ByC;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.TextFieldElement;

public class TestByC extends GenericTest {
	
	

	private static WebDriver driver;
	private static DriverTestPage testPage;
	
	public TestByC() throws Exception {
	}
	
	public TestByC(WebDriver driver, DriverTestPage testPage) throws Exception {
		TestByC.driver = driver;
		TestByC.testPage = testPage;
	}
	
	@BeforeClass(groups={"it"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		SeleniumTestsContextManager.getThreadContext().setBrowser("*chrome");
		SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(false);
//		SeleniumTestsContextManager.getThreadContext().setWebDriverGrid("http://127.0.0.1:4444/wd/hub");
//		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
	}
	
	
	@AfterClass(groups={"it"})
	public void closeBrowser() {
		WebUIDriver.cleanUp();
		WebUIDriver.cleanUpWebUIDriver();
	}
	
	@Test(groups={"it"})
	public void testFindElementByLabelForward() {
		new TextFieldElement("", ByC.labelForward("By id forward", "input")).sendKeys("element found by label");
		Assert.assertEquals(testPage.textSelectedId.getValue(), "element found by label");
	}
	
	@Test(groups={"it"})
	public void testFindElementByCustomLabelForward() {
		new TextFieldElement("", ByC.labelForward("Test select", "input", "h3")).sendKeys("element found by h3 tag");
		Assert.assertEquals(testPage.textSelectedId.getValue(), "element found by h3 tag");
	}
	
	@Test(groups={"it"})
	public void testFindElementByLabelForwardWithoutTagName() {
		new TextFieldElement("", ByC.labelForward("By id forward")).sendKeys("element found by label without tagname");
		Assert.assertEquals(testPage.textSelectedId.getValue(), "element found by label without tagname");
	}
	
	@Test(groups={"it"})
	public void testFindElementsByLabelForward() {
		Assert.assertTrue(new TextFieldElement("", ByC.labelForward("By id forward", "input")).findElements().size() > 3);
	}

	@Test(groups={"it"})
	public void testFindElementByPartialLabelForward() {
		new TextFieldElement("", ByC.partialLabelForward("By id for", "input")).sendKeys("element found by partial label");
		Assert.assertEquals(testPage.textSelectedId.getValue(), "element found by partial label");
	}

	@Test(groups={"it"})
	public void testFindElementByCustomPartialLabelForward() {
		new TextFieldElement("", ByC.partialLabelForward("Test sele", "input", "h3")).sendKeys("element found by partial h3 tag");
		Assert.assertEquals(testPage.textSelectedId.getValue(), "element found by partial h3 tag");
	}
	
	@Test(groups={"it"})
	public void testFindElementByLabelBackward() {
		new TextFieldElement("", ByC.labelBackward("By id backward", "input")).sendKeys("element found by label backward");
		Assert.assertEquals(testPage.textSelectedText.getValue(), "element found by label backward");
	}

	@Test(groups={"it"})
	public void testFindElementByCustomLabelBackward() {
		new TextFieldElement("", ByC.labelBackward("Test select Multiple", "input", "h3")).sendKeys("element found by h3 tag backward");
		Assert.assertEquals(testPage.textSelectedText.getValue(), "element found by h3 tag backward");
	}
	
	@Test(groups={"it"})
	public void testFindElementByLabelBackwardWithoutTagName() {
		new TextFieldElement("", ByC.labelBackward("By id backward")).sendKeys("element found by label backward without tagname");
		Assert.assertEquals(testPage.textSelectedText.getValue(), "element found by label backward without tagname");
	}
	
	@Test(groups={"it"})
	public void testFindElementsByLabelBackward() {
		Assert.assertTrue(new TextFieldElement("", ByC.labelBackward("By id backward", "input")).findElements().size() > 3);
	}

	@Test(groups={"it"})
	public void testFindElementByPartialLabelBackward() {
		new TextFieldElement("", ByC.partialLabelBackward("By id back", "input")).sendKeys("element found by partial label backward");
		Assert.assertEquals(testPage.textSelectedText.getValue(), "element found by partial label backward");
	}

	@Test(groups={"it"})
	public void testFindElementByPartialCustomLabelBackward() {
		new TextFieldElement("", ByC.partialLabelBackward("Test select Mult", "input", "h3")).sendKeys("element found by h3 partial tag backward");
		Assert.assertEquals(testPage.textSelectedText.getValue(), "element found by h3 partial tag backward");
	}

	@Test(groups={"it"})
	public void testFindElementByAttribute() {
		new TextFieldElement("", ByC.attribute("attr", "attribute")).sendKeys("element found by attribute");
		Assert.assertEquals(testPage.textSelectedId.getValue(), "element found by attribute");
	}
	
	@Test(groups={"it"}, expectedExceptions=IllegalArgumentException.class)
	public void testFindElementByNullAttribute() {
		new TextFieldElement("", ByC.attribute(null, "attribute")).sendKeys("element found by attribute");
	}
	
	@Test(groups={"it"}, expectedExceptions=IllegalArgumentException.class)
	public void testFindElementByNullAttributeValue() {
		new TextFieldElement("", ByC.attribute("attr", null)).sendKeys("element found by attribute");
	}

	@Test(groups={"it"})
	public void testFindElementByText() { 
		Assert.assertEquals(new TextFieldElement("", ByC.text("Test IFrame", "*")).getTagName(), "h3");
	}
	
	@Test(groups={"it"})
	public void testFindElementsByText() { 
		Assert.assertEquals(new TextFieldElement("", ByC.text("Test IFrame", "*")).findElements().size(), 1);
	}
	
	@Test(groups={"it"})
	public void testFindElementByPartialText() { 
		Assert.assertEquals(new TextFieldElement("", ByC.partialText("Test IF", "*")).getTagName(), "h3");
	}
	
	@Test(groups={"it"})
	public void testFindElementsByPartialText() { 
		Assert.assertTrue(new TextFieldElement("", ByC.partialText("Test select", "*")).findElements().size() >=  3);
	}

	@Test(groups={"it"}, expectedExceptions=IllegalArgumentException.class)
	public void testFindElementByNullText() { 
		new TextFieldElement("", ByC.text(null, "*")).getTagName();
	}
	
	@Test(groups={"it"}, expectedExceptions=IllegalArgumentException.class)
	public void testFindElementByNullTagName() { 
		new TextFieldElement("", ByC.text("Test select", null)).getTagName();
	}
	
	/**
	 * issue #166: Search subElement by attribute
	 */
	@Test(groups={"it"})
	public void testFindElementRelativeByAttribute() {
		Assert.assertEquals(new HtmlElement("", By.id("parentDiv")).findRadioButtonElement(ByC.attribute("name", "radioClick")).getAttribute("id"), "radioClickParent");
	}
	@Test(groups={"it"})
	public void testFindElementsRelativeByAttribute() {
		Assert.assertEquals(new HtmlElement("", By.id("parentDiv")).findRadioButtonElement(ByC.attribute("name", "radioClick")).findElements().size(), 1);
	}
	
	/**
	 * issue #166: check we get the first element inside the parent one when searching by label, partial or not
	 */
	@Test(groups={"it"})
	public void testFindElementRelativeByPartialLabel() {
		Assert.assertEquals(new HtmlElement("", By.id("parent")).findElement(ByC.partialLabelForward("findElement", "div")).getAttribute("id"), "child1");
	}
	@Test(groups={"it"})
	public void testFindElementsRelativeByPartialLabel() {
		List<WebElement> elements = new HtmlElement("", By.id("parent")).findElement(ByC.partialLabelForward("findElement", "div")).findElements();
		Assert.assertTrue(elements.size() >= 3);
		Assert.assertEquals(elements.get(0).getAttribute("id"), "child1");
	}
	@Test(groups={"it"})
	public void testFindElementRelativeByLabel() {
		Assert.assertEquals(new HtmlElement("", By.id("parent")).findElement(ByC.labelForward("findElement", "div")).getAttribute("id"), "child1");
	}
	@Test(groups={"it"})
	public void testFindElementsRelativeByLabel() {
		List<WebElement> elements = new HtmlElement("", By.id("parent")).findElement(ByC.labelForward("findElement", "div")).findElements();
		Assert.assertTrue(elements.size() >= 3);
		Assert.assertEquals(elements.get(0).getAttribute("id"), "child1");
	}
	
	/**
	 * issue #166: check we get the first element inside the parent one when searching by text, partial or not
	 */
	@Test(groups={"it"})
	public void testFindElementRelativeByPartialText() {
		Assert.assertEquals(new HtmlElement("", By.id("parent")).findElement(ByC.partialText("first chi", "div")).getAttribute("id"), "child1");
	}
	@Test(groups={"it"})
	public void testFindElementsRelativeByPartialText() {
		List<WebElement> elements = new HtmlElement("", By.id("parent")).findElement(ByC.partialText("first chi", "div")).findElements();
		Assert.assertEquals(elements.size(), 1);
		Assert.assertEquals(elements.get(0).getAttribute("id"), "child1");
	}
	@Test(groups={"it"})
	public void testFindElementRelativeByText() {
		Assert.assertEquals(new HtmlElement("", By.id("parent")).findElement(ByC.text("first child", "div")).getAttribute("id"), "child1");
	}
	@Test(groups={"it"})
	public void testFindElementsRelativeByText() {
		List<WebElement> elements = new HtmlElement("", By.id("parent")).findElement(ByC.text("first child", "div")).findElements();
		Assert.assertEquals(elements.size(), 1);Assert.assertEquals(elements.size(), 1);
		Assert.assertEquals(elements.get(0).getAttribute("id"), "child1");
	}
	
	
}
