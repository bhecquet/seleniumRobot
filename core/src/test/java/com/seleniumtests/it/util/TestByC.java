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

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.uipage.ByC;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.RadioButtonElement;
import com.seleniumtests.uipage.htmlelements.TextFieldElement;

import static com.seleniumtests.uipage.ByC.*;

public class TestByC extends GenericTest {
	
	public TestByC() throws Exception {
	}
	
	@BeforeClass(groups={"it"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		System.setProperty("applicationName", "core");
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		SeleniumTestsContextManager.getThreadContext().setBrowser("*chrome");
		SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(false);
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
//		SeleniumTestsContextManager.getThreadContext().setWebDriverGrid("http://127.0.0.1:4444/wd/hub");
//		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
		new DriverTestPage(true); // start displaying page
	}
	
	
	@AfterClass(groups={"it"}, alwaysRun=true)
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
	@AfterMethod(groups={"it"})
	public void reset() {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		DriverTestPage.textSelectedId.clear();
		DriverTestPage.textSelectedText.clear();
	}
	
	public void testFindElementByLabelForward(String labelToSearch) {
		new TextFieldElement("", ByC.labelForward(labelToSearch, "input")).sendKeys("element found by label");
		Assert.assertEquals(DriverTestPage.textSelectedId.getValue(), "element found by label");
	}
	
	@Test(groups={"it"})
	public void testFindElementByLabelForwardTotal() {
		testFindElementByLabelForward("By id forward");
	}

	@Test(groups={"it"})
	public void testFindElementByByLabelForwardContaining() { 
		testFindElementByLabelForward("y id for*");
	}
	
	@Test(groups={"it"})
	public void testFindElementByByLabelForwardStarting() {
		testFindElementByLabelForward("By id for^");
	}
	
	@Test(groups={"it"})
	public void testFindElementByByLabelForwardEnding() { 
		testFindElementByLabelForward(" id forward$");
	}
	
	@Test(groups={"it"})
	public void testFindElementByCustomLabelForward() {
		new TextFieldElement("", ByC.labelForward("Test select", "input", "h3")).sendKeys("element found by h3 tag");
		Assert.assertEquals(DriverTestPage.textSelectedId.getValue(), "element found by h3 tag");
	}
	
	@Test(groups={"it"})
	public void testFindElementByLabelForwardWithoutTagName() {
		new TextFieldElement("", ByC.labelForward("By id forward")).sendKeys("element found by label without tagname");
		Assert.assertEquals(DriverTestPage.textSelectedId.getValue(), "element found by label without tagname");
	}
	
	@Test(groups={"it"})
	public void testFindElementsByLabelForward() {
		Assert.assertTrue(new TextFieldElement("", ByC.labelForward("By id forward", "input")).findElements().size() > 3);
	}

	@Test(groups={"it"})
	public void testFindElementByPartialLabelForward() {
		new TextFieldElement("", ByC.partialLabelForward("By id for", "input")).sendKeys("element found by partial label");
		Assert.assertEquals(DriverTestPage.textSelectedId.getValue(), "element found by partial label");
	}

	@Test(groups={"it"})
	public void testFindElementByCustomPartialLabelForward() {
		new TextFieldElement("", ByC.partialLabelForward("Test sele", "input", "h3")).sendKeys("element found by partial h3 tag");
		Assert.assertEquals(DriverTestPage.textSelectedId.getValue(), "element found by partial h3 tag");
	}
	
	@Test(groups={"it"})
	public void testFindElementByLabelBackward() {
		new TextFieldElement("", ByC.labelBackward("By id backward", "input")).sendKeys("element found by label backward");
		Assert.assertEquals(DriverTestPage.textSelectedText.getValue(), "element found by label backward");
	}

	@Test(groups={"it"})
	public void testFindElementByByLabelBackwardContaining() { 
		new TextFieldElement("", ByC.labelBackward("y id back*", "input")).sendKeys("element found by label backward*");
		Assert.assertEquals(DriverTestPage.textSelectedText.getValue(), "element found by label backward*");
	}
	
	@Test(groups={"it"})
	public void testFindElementByByLabelBackwardStarting() { 
		new TextFieldElement("", ByC.labelBackward("By id back^", "input")).sendKeys("element found by label backward^");
		Assert.assertEquals(DriverTestPage.textSelectedText.getValue(), "element found by label backward^");
	}
	
	@Test(groups={"it"})
	public void testFindElementByByLabelBackwardEnding() { 
		new TextFieldElement("", ByC.labelBackward(" id backward$", "input")).sendKeys("element found by label backward$");
		Assert.assertEquals(DriverTestPage.textSelectedText.getValue(), "element found by label backward$");
	}

	@Test(groups={"it"})
	public void testFindElementByCustomLabelBackward() {
		new TextFieldElement("", ByC.labelBackward("Test select Multiple", "input", "h3")).sendKeys("element found by h3 tag backward");
		Assert.assertEquals(DriverTestPage.textSelectedText.getValue(), "element found by h3 tag backward");
	}
	
	@Test(groups={"it"})
	public void testFindElementByLabelBackwardWithoutTagName() {
		new TextFieldElement("", ByC.labelBackward("By id backward")).sendKeys("element found by label backward without tagname");
		Assert.assertEquals(DriverTestPage.textSelectedText.getValue(), "element found by label backward without tagname");
	}
	
	@Test(groups={"it"})
	public void testFindElementsByLabelBackward() {
		Assert.assertTrue(new TextFieldElement("", ByC.labelBackward("By id backward", "input")).findElements().size() > 3);
	}

	@Test(groups={"it"})
	public void testFindElementByPartialLabelBackward() {
		new TextFieldElement("", ByC.partialLabelBackward("By id back", "input")).sendKeys("element found by partial label backward");
		Assert.assertEquals(DriverTestPage.textSelectedText.getValue(), "element found by partial label backward");
	}

	@Test(groups={"it"})
	public void testFindElementByPartialCustomLabelBackward() {
		new TextFieldElement("", ByC.partialLabelBackward("Test select Mult", "input", "h3")).sendKeys("element found by h3 partial tag backward");
		Assert.assertEquals(DriverTestPage.textSelectedText.getValue(), "element found by h3 partial tag backward");
	}

	@Test(groups={"it"})
	public void testFindElementByAttribute() {
		new TextFieldElement("", ByC.attribute("attr", "attribute")).sendKeys("element found by attribute");
		Assert.assertEquals(DriverTestPage.textSelectedId.getValue(), "element found by attribute");
	}
	
	@Test(groups={"it"})
	public void testFindElemenstByAttribute() {
		Assert.assertEquals(new RadioButtonElement("", ByC.attribute("name", "radioClick")).findElements().size(), 2);
	}
	
	// test attribute "*" syntax
	@Test(groups={"it"})
	public void testFindElementByPartialAttribute() {
		new TextFieldElement("", ByC.attribute("attr*", "ttribut")).sendKeys("element found by attribute");
		Assert.assertEquals(DriverTestPage.textSelectedId.getValue(), "element found by attribute");
	}
	
	
	
	@Test(groups={"it"})
	public void testFindElemenstByPartialAttribute() {
		Assert.assertEquals(new RadioButtonElement("", ByC.attribute("name*", "adioCli")).findElements().size(), 2);
	}
	
	// test attribute "^" syntax"
	@Test(groups={"it"})
	public void testFindElementByAttributeStartingWith() {
		new TextFieldElement("", ByC.attribute("attr^", "attribu")).sendKeys("element found by attribute");
		Assert.assertEquals(DriverTestPage.textSelectedId.getValue(), "element found by attribute");
	}
	
	@Test(groups={"it"})
	public void testFindElemenstByAttributeStartingWith() {
		Assert.assertEquals(new RadioButtonElement("", ByC.attribute("name^", "radioCli")).findElements().size(), 2);
	}
	
	// test attribute "$" syntax"
	@Test(groups={"it"})
	public void testFindElementByAttributeEndingWith() {
		new TextFieldElement("", ByC.attribute("attr$", "tribute")).sendKeys("element found by attribute");
		Assert.assertEquals(DriverTestPage.textSelectedId.getValue(), "element found by attribute");
	}

	@Test(groups={"it"})
	public void testFindElemenstByAttributeEndingWith() {
		Assert.assertEquals(new RadioButtonElement("", ByC.attribute("name$", "adioClick")).findElements().size(), 2);
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

	@Test(groups={"it"})
	public void testFindElementByTextContaining() { 
		Assert.assertEquals(new TextFieldElement("", ByC.text("est IF*", "*")).getTagName(), "h3");
	}
	
	@Test(groups={"it"})
	public void testFindElementByTextStarting() { 
		Assert.assertEquals(new TextFieldElement("", ByC.text("Test IF^", "*")).getTagName(), "h3");
	}
	
	@Test(groups={"it"})
	public void testFindElementByTextEnding() { 
		Assert.assertEquals(new TextFieldElement("", ByC.text("st IFrame$", "*")).getTagName(), "h3");
	}

	@Test(groups={"it"}, expectedExceptions=IllegalArgumentException.class)
	public void testFindElementByNullText() { 
		new TextFieldElement("", ByC.text(null, "*")).getTagName();
	}
	
	@Test(groups={"it"}, expectedExceptions=IllegalArgumentException.class)
	public void testFindElementByNullTagName() { 
		new TextFieldElement("", ByC.text("Test select", null)).getTagName();
	}

	@Test(groups={"it"})
	public void testFindElementByTextInsideChild() {
		Assert.assertEquals(new TextFieldElement("", ByC.textInside("child of child", "div")).getAttribute("id"), "child5");
	}
	@Test(groups={"it"})
	public void testFindElementByTextInsideChild2() {
		Assert.assertEquals(new TextFieldElement("", ByC.textInside("child of*", "div")).getAttribute("id"), "child5");
	}
	@Test(groups={"it"}, expectedExceptions = NoSuchElementException.class)
	public void testFindElementByTextInsideChildNotFound() {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(5);
		new TextFieldElement("", ByC.textInside("child ", "div")).getAttribute("id");
	}

	/**
	 * issue #166: Search subElement by attribute
	 */
	@Test(groups={"it"})
	public void testFindElementRelativeByAttribute() {
		Assert.assertEquals(new HtmlElement("", By.id("parentDiv")).findRadioButtonElement(ByC.attribute("name", "radioClick")).getDomAttribute("id"), "radioClickParent");
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
		Assert.assertEquals(new HtmlElement("", By.id("parent")).findElement(ByC.partialLabelForward("findElement", "div")).getDomAttribute("id"), "child1");
	}
	@Test(groups={"it"})
	public void testFindElementsRelativeByPartialLabel() {
		List<WebElement> elements = new HtmlElement("", By.id("parent")).findElement(ByC.partialLabelForward("findElement", "div")).findElements();
		Assert.assertTrue(elements.size() >= 3);
		Assert.assertEquals(elements.get(0).getDomAttribute("id"), "child1");
	}
	@Test(groups={"it"})
	public void testFindElementRelativeByLabel() {
		Assert.assertEquals(new HtmlElement("", By.id("parent")).findElement(ByC.labelForward("findElement", "div")).getDomAttribute("id"), "child1");
	}
	@Test(groups={"it"})
	public void testFindElementsRelativeByLabel() {
		List<WebElement> elements = new HtmlElement("", By.id("parent")).findElement(ByC.labelForward("findElement", "div")).findElements();
		Assert.assertTrue(elements.size() >= 3);
		Assert.assertEquals(elements.get(0).getDomAttribute("id"), "child1");
	}
	
	/**
	 * issue #166: check we get the first element inside the parent one when searching by text, partial or not
	 */
	@Test(groups={"it"})
	public void testFindElementRelativeByPartialText() {
		Assert.assertEquals(new HtmlElement("", By.id("parent")).findElement(ByC.partialText("first chi", "div")).getDomAttribute("id"), "child1");
	}
	@Test(groups={"it"})
	public void testFindElementsRelativeByPartialText() {
		List<WebElement> elements = new HtmlElement("", By.id("parent")).findElement(ByC.partialText("first chi", "div")).findElements();
		Assert.assertEquals(elements.size(), 1);
		Assert.assertEquals(elements.get(0).getDomAttribute("id"), "child1");
	}
	@Test(groups={"it"})
	public void testFindElementRelativeByText() {
		Assert.assertEquals(new HtmlElement("", By.id("parent")).findElement(ByC.text("first child", "div")).getDomAttribute("id"), "child1");
	}
	@Test(groups={"it"})
	public void testFindElementsRelativeByText() {
		List<WebElement> elements = new HtmlElement("", By.id("parent")).findElement(ByC.text("first child", "div")).findElements();
		Assert.assertEquals(elements.size(), 1);Assert.assertEquals(elements.size(), 1);
		Assert.assertEquals(elements.get(0).getDomAttribute("id"), "child1");
	}

	@Test(groups={"it"})
	public void testFindElementsBySeveralCriteria() { 
		Assert.assertEquals(new TextFieldElement("", ByC.and(By.tagName("input"), By.name("textField"))).findElements().size(), 2);
	}
	@Test(groups={"it"})
	public void testFindElementBySeveralCriteria() { 
		TextFieldElement el = new TextFieldElement("", ByC.and(By.tagName("input"), By.name("textField")));
		Assert.assertEquals(el.getTagName(), "input");
		Assert.assertEquals(el.getDomAttribute("name"), "textField");
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testFindElementBySeveralCriteriaNothingFound() { 
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(7);
		new TextFieldElement("", ByC.and(By.tagName("input"), By.name("noElement"))).getTagName();
	}
	
	@Test(groups={"it"})
	public void testFindElementsBySeveralCriteriaNothingFound() { 
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(7);
		Assert.assertEquals(new TextFieldElement("", ByC.and(By.tagName("input"), By.name("noElement"))).findElements().size(), 0);
	}
	
	@Test(groups={"it"})
	public void testFindElementsBySeveralCriteriaNothingFound2() { 
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(7);
		Assert.assertEquals(new TextFieldElement("", ByC.and(By.tagName("noElement"), By.name("textField"))).findElements().size(), 0);
	}

	@Test(groups={"it"})
	public void testFindElementById() { 
		Assert.assertEquals(new TextFieldElement("", ByC.xId("select")).getTagName(), "select");
	}
	@Test(groups={"it"})
	public void testFindElementsById() { 
		Assert.assertEquals(new TextFieldElement("", ByC.xId("buttonServerDelay")).findElements().size(), 1);
	}
	@Test(groups={"it"})
	public void testFindElementRelativeById() {
		Assert.assertEquals(new HtmlElement("", By.id("parentDiv")).findElement(ByC.xId("textSelectedTextParent")).getDomAttribute("name"), "textSelectedText");
	}
	@Test(groups={"it"})
	public void testFindElementsRelativeById() {
		Assert.assertEquals(new HtmlElement("", By.id("parentDiv")).findElement(ByC.xId("textSelectedTextParent")).findElements().size(), 1);
	}
	
	@Test(groups={"it"})
	public void testFindElementByName() { 
		Assert.assertEquals(new TextFieldElement("", ByC.xName("divFindName")).getTagName(), "div");
	}
	@Test(groups={"it"})
	public void testFindElementsByName() { 
		Assert.assertEquals(new TextFieldElement("", ByC.xName("divFindName")).findElements().size(), 2);
	}
	@Test(groups={"it"})
	public void testFindElementRelativeByName() {
		Assert.assertEquals(new HtmlElement("", By.id("parentDiv")).findElement(ByC.xName("radioClick")).getDomAttribute("id"), "radioClickParent");
	}
	@Test(groups={"it"})
	public void testFindElementsRelativeByName() {
		Assert.assertEquals(new HtmlElement("", By.id("parentDiv")).findElement(ByC.xName("option")).findElements().size(), 2);
	}

	@Test(groups={"it"})
	public void testFindElementByLinkText() { 
		Assert.assertEquals(new TextFieldElement("", ByC.xLinkText("My link")).getTagName(), "a");
	}
	@Test(groups={"it"})
	public void testFindElementsByLinkText() { 
		Assert.assertEquals(new TextFieldElement("", ByC.xLinkText("My link")).findElements().size(), 2);
	}
	@Test(groups={"it"})
	public void testFindElementRelativeByLinkText() {
		Assert.assertEquals(new HtmlElement("", By.id("parentDiv")).findElement(ByC.xLinkText("My link Parent")).getDomAttribute("id"), "linkParent");
	}
	@Test(groups={"it"})
	public void testFindElementsRelativeByLinkText() {
		Assert.assertEquals(new HtmlElement("", By.id("parentDiv")).findElement(ByC.xLinkText("My link Parent")).findElements().size(), 1);
	}
	
	@Test(groups={"it"})
	public void testFindElementByPartialLinkText() { 
		Assert.assertEquals(new TextFieldElement("", ByC.xPartialLinkText("My lin")).getTagName(), "a");
	}
	@Test(groups={"it"})
	public void testFindElementsByPartialLinkText() { 
		Assert.assertEquals(new TextFieldElement("", ByC.xPartialLinkText("My lin")).findElements().size(), 5);
	}
	@Test(groups={"it"})
	public void testFindElementRelativeByPartialLinkText() {
		Assert.assertEquals(new HtmlElement("", By.id("parentDiv")).findElement(ByC.xPartialLinkText("My link")).getDomAttribute("id"), "linkParent");
	}
	@Test(groups={"it"})
	public void testFindElementsRelativeByPartialLinkText() {
		Assert.assertEquals(new HtmlElement("", By.id("parentDiv")).findElement(ByC.xPartialLinkText("My link")).findElements().size(), 1);
	}

	@Test(groups={"it"})
	public void testFindElementByTagName() { 
		Assert.assertEquals(new TextFieldElement("", ByC.xTagName("div")).getTagName(), "div");
	}
	@Test(groups={"it"})
	public void testFindElementsByTagName() { 
		Assert.assertTrue(new TextFieldElement("", ByC.xTagName("h3")).findElements().size() > 18);
	}
	@Test(groups={"it"})
	public void testFindElementRelativeByTagName() {
		Assert.assertEquals(new HtmlElement("", By.id("parentDiv")).findElement(ByC.xTagName("button")).getDomAttribute("name"), "resetButton");
	}
	@Test(groups={"it"})
	public void testFindElementsRelativeByTagName() {
		Assert.assertEquals(new HtmlElement("", By.id("parentDiv")).findElement(ByC.xTagName("option")).findElements().size(), 2);
	}
	
	@Test(groups={"it"})
	public void testFindElementByClassName() { 
		Assert.assertEquals(new TextFieldElement("", ByC.xClassName("language_selector")).getTagName(), "input");
	}
	@Test(groups={"it"})
	public void testFindElementsByClassName() { 
		Assert.assertEquals(new TextFieldElement("", ByC.xClassName("language_selector")).findElements().size(), 1);
	}
	@Test(groups={"it"})
	public void testFindElementRelativeByClassName() {
		Assert.assertEquals(new HtmlElement("", By.id("parentDiv")).findElement(ByC.xClassName("myTable")).getTagName(), "table");
	}
	@Test(groups={"it"})
	public void testFindElementsRelativeByClassName() {
		Assert.assertEquals(new HtmlElement("", By.id("parentDiv")).findElement(ByC.xClassName("myTable")).findElements().size(), 1);
	}
	
	@Test(groups={"it"})
	public void testByOrWithAllLocatorsValid() {
		Assert.assertEquals(new HtmlElement("or", ByC.or(By.id("text2"), By.name("textField"))).findElements().size(), 1);
		
	}
	
	/**
	 * When first locator do not allow to find element, go to the next one
	 */
	@Test(groups={"it"})
	public void testByOrWithOneInvalidLocator() {
		Assert.assertEquals(new HtmlElement("or", ByC.or(By.id("text2Invalid"), By.name("textField"))).getTagName(), "input");
		
	}
	
	/**
	 * Test specific locator with non mobile platform. No element should be found
	 */
	@Test(groups={"it"}, expectedExceptions = NoSuchElementException.class)
	public void testByOrWithPlatformSpecificLocator() {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(3);
		new HtmlElement("or", ByC.or(android(By.id("text2")))).getTagName();
		
	}
	
	/**
	 * Test specific locator with mobile platform. Element should be found
	 */
	@Test(groups={"it"})
	public void testByOrWithPlatformSpecificLocatorAndAndroidPlatform() {
		SeleniumTestsContextManager.getThreadContext().setPlatform("ANDROID");
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(3);
		Assert.assertEquals(new HtmlElement("or", ByC.or(
				web(By.id("carre")),
				ios(By.name("textField")),
				android(By.id("text2")))).getTagName(),
				"input");
	}
	@Test(groups={"it"})
	public void testByOrWithPlatformSpecificLocatorAndAndroidPlatformWeb() {
		SeleniumTestsContextManager.getThreadContext().setPlatform("ANDROID");
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_WEB_ANDROID);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(3);
		Assert.assertEquals(new HtmlElement("or", ByC.or(
				web(By.id("carre")),
				ios(By.name("textField")),
				android(By.id("text2")))).getTagName(),
				"div");
	}
	/**
	 * Test specific locator with mobile platform. Element should be found
	 */
	@Test(groups={"it"})
	public void testByOrWithPlatformSpecificLocatorAndIosPlatform() {
		SeleniumTestsContextManager.getThreadContext().setPlatform("IOS");
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_IOS);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(3);
		Assert.assertEquals(new HtmlElement("or", ByC.or(
				web(By.id("carre")),
				ios(By.id("text2")),
				android(By.id("textField")))).getTagName(),
				"input");
		
	}
	@Test(groups={"it"})
	public void testByOrWithPlatformSpecificLocatorAndIosPlatformWeb() {
		SeleniumTestsContextManager.getThreadContext().setPlatform("IOS");
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_WEB_IOS);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(3);
		Assert.assertEquals(new HtmlElement("or", ByC.or(
				web(By.id("carre")),
				ios(By.id("text2")),
				android(By.id("textField")))).getTagName(),
				"div");

	}

	@Test(groups={"it"})
	public void testByOrWithPlatformSpecificLocatorAndWebPlatform() {
		SeleniumTestsContextManager.getThreadContext().setPlatform("ANY");
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(3);
		Assert.assertEquals(new HtmlElement("or", ByC.or(
						ios(By.id("carre")),
						web(By.id("text2")),
						android(By.id("textField")))).getTagName(),
				"input");

	}
	@Test(groups={"it"})
	public void testByOrWithPlatformSpecificLocatorAndWebPlatform2() {
		SeleniumTestsContextManager.getThreadContext().setPlatform("WIN10");
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(3);
		Assert.assertEquals(new HtmlElement("or", ByC.or(
						ios(By.id("carre")),
						web(By.id("text2")),
						android(By.id("textField")))).getTagName(),
				"input");

	}

	@Test(groups={"it"})
	public void testByLabelNameOk() {
		Assert.assertEquals(new HtmlElement("", ByC.label("By id backward")).findElements().size(), 1);
	}

	@Test(groups={"it"}, expectedExceptions = IllegalArgumentException.class)
	public void testByLabelNameLabelNull() {
		new HtmlElement("", ByC.label(null));
	}
}
