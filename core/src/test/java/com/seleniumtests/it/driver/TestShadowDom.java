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
package com.seleniumtests.it.driver;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPageShadowDom;
import com.seleniumtests.uipage.ByC;

public class TestShadowDom extends GenericTest {
	

	@BeforeClass(groups={"it"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		System.setProperty("applicationName", "core");
		initThreadContext(testNGCtx);
		setBrowser();
		new DriverTestPageShadowDom(true);
		WebUIDriver.getWebDriver(true);
	}
	
	@BeforeMethod(groups={"it"})
	public void initTimeouts(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(2);
	}
 
	public void setBrowser() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
	}

	/**
	 * Check elements in shadow DOM cannot be accessed
	 */
	@Test(groups={"it"}, expectedExceptions = NoSuchElementException.class)
	public void testAccessShadowElementDirectly() {
		DriverTestPageShadowDom.divPass1Direct.getText();
	}
	
	@Test(groups={"it"}) 
	public void testAccessShadowElement() {
		Assert.assertEquals(DriverTestPageShadowDom.divPass1Shadow.getText(), "This is a div with an ID of pass1Shadow");
	}
	
	@Test(groups={"it"})
	public void testAccessSubShadowElement() {
		Assert.assertEquals(DriverTestPageShadowDom.divPass1Shadow.getText(), "This is a div with an ID of pass1Shadow");
	}

	/**
	 * #636: searching a shadow root by tagName while already in a shadow root leads to
	 * org.openqa.selenium.JavascriptException: javascript error: b.getElementsByTagName is not a function
	 */
	@Test(groups={"it"})
	public void testAccessSubShadowElementByTagName() {
		Assert.assertEquals(DriverTestPageShadowDom.divPass3MultipleShadowTagName.getText(), "This is a DIV with the ID of pass3Shadow");
	}
	
	@Test(groups={"it"})
	public void testAccessMultipleSubShadowElement() {
		Assert.assertEquals(DriverTestPageShadowDom.divPass3MultipleShadow.getText(), "This is a DIV with the ID of pass3Shadow");
	}
	
	/**
	 * Shadow elements are located through the same locators, check that the first one is used
	 * Also check that By.tagName is supported as a direct child of shadow root
	 */
	@Test(groups={"it"})
	public void testAccessMultipleSameSubShadowElement() {
		Assert.assertEquals(DriverTestPageShadowDom.divMultipleShadowElements.findElement(By.tagName("div")).getText(), "This is a DIV with the ID of pass71Shadow");
	}

	/**
	 * Check that By.id is supported as a direct child of shadow root
	 */
	@Test(groups={"it"})
	public void testAccessMultipleSameSubShadowElementById() {
		Assert.assertEquals(DriverTestPageShadowDom.divMultipleShadowElements.findElement(By.id("pass71Shadow")).getText(), "This is a DIV with the ID of pass71Shadow");
	}
	
	/**
	 * Check that By.className is supported as a direct child of shadow root
	 */
	@Test(groups={"it"})
	public void testAccessMultipleSameSubShadowElementByClassName() {
		Assert.assertEquals(DriverTestPageShadowDom.divMultipleShadowElements.findElement(By.className("foo")).getText(), "This is a DIV with the ID of pass71Shadow");
	}
	
	/**
	 * Check that By.cssSelector is supported as a direct child of shadow root
	 */
	@Test(groups={"it"})
	public void testAccessMultipleSameSubShadowElementByCssSelector() {
		Assert.assertEquals(DriverTestPageShadowDom.divMultipleShadowElements.findElement(By.cssSelector("div")).getText(), "This is a DIV with the ID of pass71Shadow");
	}
	
	/**
	 * Check that By.name is supported as a direct child of shadow root
	 */
	@Test(groups={"it"})
	public void testAccessMultipleSameSubShadowElementByName() {
		Assert.assertEquals(DriverTestPageShadowDom.divMultipleShadowElements.findElement(By.name("pass71ShadowName")).getText(), "This is a DIV with the ID of pass71Shadow");
	}
	
	/**
	 * Check that By.linkText is supported as a direct child of shadow root
	 */
	@Test(groups={"it"})
	public void testAccessMultipleSameSubShadowElementByLinkText() {
		Assert.assertEquals(DriverTestPageShadowDom.divMultipleShadowElements.findElement(By.linkText("of pass71Shadow")).getText(), "of pass71Shadow");
	}
	
	/**
	 * Check that By.xpath is not supported as a direct child of shadow root
	 */
	@Test(groups={"it"}, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = ".*ByXPath is not supported as a direct child of a shadow DOM as it uses XPath.*")
	public void testAccessMultipleSameSubShadowElementByXPath() {
		DriverTestPageShadowDom.divMultipleShadowElements.findElement(By.xpath("//div")).getText();
	}
	
	/**
	 * Check that xpath is still supported if it's not a direct child of shadow root
	 */
	@Test(groups={"it"})
	public void testAccessMultipleSameSubShadowElementByXPath2() {
		Assert.assertEquals(DriverTestPageShadowDom.divMultipleShadowElements.findElement(By.tagName("div")).findElement(By.xpath("//a")).getText(), "of pass71Shadow");
	}
	
	/**
	 * Check search ByC.attribute with internally uses xpath is converted to a Css selector
	 */
	@Test(groups={"it"})
	public void testAccessMultipleSameSubShadowElementByCAttribute() {
		Assert.assertEquals(DriverTestPageShadowDom.divMultipleShadowElements.findElement(ByC.attribute("id", "pass71Shadow")).getText(), "This is a DIV with the ID of pass71Shadow");
	}
	
	
	/**
	 * Check search ByC.text fails as it uses XPath internally
	 */
	@Test(groups={"it"}, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = ".*ByText is not supported as a direct child of a shadow DOM as it uses XPath.*")
	public void testAccessMultipleSameSubShadowElementByCText() {
		DriverTestPageShadowDom.divMultipleShadowElements.findElement(ByC.text("This is a DIV with the ID", "div")).getText();
	}
	
	/**
	 * When element is not found under a shadow root
	 */
	@Test(groups={"it"}, expectedExceptions = NoSuchElementException.class)
	public void testAccessMultipleSameSubShadowElementNotFound() {
		Assert.assertEquals(DriverTestPageShadowDom.divMultipleShadowElements.findElement(By.tagName("span")).getText(), "This is a DIV with the ID of pass71Shadow");
	}

	@Test(groups={"it"}, expectedExceptions = NoSuchElementException.class)
	public void testAccessShadowElementNotFound() {
		DriverTestPageShadowDom.shadowElementNotFound.getText();
	}
	
	
	@Test(groups={"it"})
	public void testAccessShadowElementInScroll() {

		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(30);
		DriverTestPageShadowDom.textInScroll.sendKeys("hello!");
		Assert.assertEquals(DriverTestPageShadowDom.textInScroll.getValue(), "hello!");
	}
	
	/**
	 * Shadow element is in a frame
	 */
	@Test(groups={"it"})
	public void testAccessShadowElementInFrame() {
		Assert.assertEquals(DriverTestPageShadowDom.divPassInFrameAndShadow.getText(), "This is a div with an ID of pass1InFrameShadow");
	}
	
	/**
	 * Frame is in a shadow element
	 */
	@Test(groups={"it"})
	public void testAccessFrameInShadowElement() {
		Assert.assertEquals(DriverTestPageShadowDom.divPassInShadowAndFrame.getText(), "This is a DIV with the id of pass1");
	}
	/**
	 * Frame is in a shadow element
	 * This time, frame is initially searched by tagname which is forbidden under shadow root element, so check selector is rewritten
	 */
	@Test(groups={"it"})
	public void testAccessFrameInShadowElementWithSelectorRewrite() {
		Assert.assertEquals(DriverTestPageShadowDom.divPassInShadowAndFrame2.getText(), "This is a DIV with the id of pass1");
	}

	/**
	 * Check selector rewrite when it's not supported under shadow root
	 */
	@Test(groups = "it")
	public void testAccessElementByTagName() {
		Assert.assertEquals(DriverTestPageShadowDom.labelInShadowByTagName.getText(), "Panda Unicorn");
	}
}
