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

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPageObjectFatory;

/**
 * Test class for checking seleniumNativeAction override when Selenium PageObjectFactory is used
 * When this option is true, we should have the selenium standard behavior + replay and logging (checked elsewhere) 
 * @author behe
 *
 */
public class TestOverridePageObjectFactory extends MockitoTest {


	protected static DriverTestPageObjectFatory testPage;
	protected static WebDriver realDriver;
	
	@BeforeClass(groups={"ut"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		
		testPage = new DriverTestPageObjectFatory(true);
		realDriver = WebUIDriver.getWebDriver(true);
	}
	
	@AfterClass(groups={"ut"}, alwaysRun=true)
	public void stop() throws Exception {

		if (WebUIDriver.getWebDriver(false) != null) {
			WebUIDriver.cleanUp();
		}
	}
	
	@BeforeMethod(groups={"ut"})
	public void overrideNativeActions() {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(15);
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(true);
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
	}
	
	@AfterMethod(groups={"ut"}, alwaysRun=true)
	public void reset() {
		
		// call testPage switchTo().defaultContent() so that it can be intercepted and seleniumRobot internal state can be reset
		// issue #275: state is not automatically reset because the driver is the same
		if (testPage != null) {
			testPage.switchDefaultContent();
			testPage.reset();
		}
	}
	
	/**
	 * Check we have an HtmlElement and not a WebElement
	 */
	@Test(groups={"ut"})
	public void testFindElementOverride() {
		Assert.assertEquals(testPage.textElement.toString(), "HtmlElement , by={By.id: text2}");
	}
	
	/**
	 * There is no way to know how the element has been searched (through HtmlElement or WebElement). We only test it's possible to get the list
	 */
	@Test(groups={"ut"})
	public void testFindElementsOverride() {
		Assert.assertEquals(testPage.inputElements.get(0).getTagName(), "input");
	}
	
	@Test(groups={"ut"})
	public void testSendKeys() {
		testPage.sendKeys();
		Assert.assertEquals(realDriver.findElement(By.id("text2")).getDomProperty("value"), "some text");
	}
	
	@Test(groups={"ut"})
	public void testFindAll() {
		Assert.assertEquals(testPage.getFindAllElements().size(), 2);
	}
	
	@Test(groups={"ut"})
	public void testFindBys() {
		Assert.assertEquals(testPage.getFindBysElements().size(), 3);
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
			Assert.assertTrue(LocalDateTime.now().minusSeconds(13).isAfter(start));
			throw e;
		}
	}
	
	

	/**
	 * Check that select behaviour remains the same as native selenium
	 */
	@Test(groups={"ut"})
	public void testSelect() {
		testPage.select();
		Assert.assertEquals(new Select(realDriver.findElement(By.id("select"))).getFirstSelectedOption().getText(), "option1");
	}
	
	/**
	 * Test that we can find an element inside frame, using standard Selenium writing (switchTo().frame())
	 */
	@Test(groups= {"ut"})
	public void testSingleFrame() {
		testPage.switchToFirstFrameByElement();
		WebElement el = testPage.getElementInsideFrame();
		Assert.assertEquals(el.getDomProperty("value"), "a value");
	}
	
	/**
	 * Test that we can find an element inside frame by index, using standard Selenium writing (switchTo().frame())
	 */
	@Test(groups= {"ut"})
	public void testSingleFrameByIndex() {
		testPage.switchToFirstFrameByIndex();
		WebElement el = testPage.getElementInsideFrame();
		Assert.assertEquals(el.getDomProperty("value"), "a value");
	}
	
	/**
	 * Test that we can find an element inside frame by index, using standard Selenium writing (switchTo().frame())
	 */
	@Test(groups= {"ut"})
	public void testSingleFrameByNameOrId() {
		testPage.switchToFirstFrameByNameOrId();
		WebElement el = testPage.getElementInsideFrame();
		Assert.assertEquals(el.getDomProperty("value"), "a value");
	}
	
	/**
	 * Test that we can find an element inside several frames
	 */
	@Test(groups= {"ut"})
	public void testSeveralFrames() {
		testPage.switchToFirstFrameByElement();
		testPage.switchToSecondFrameByElement();
		WebElement el = testPage.getElementInsideFrameOfFrame();
		Assert.assertEquals(el.getDomProperty("value"), "an other value in iframe");
	}
	

	/**
	 * Check that with override, behavior is correct
	 */
	@Test(groups={"ut"})
	public void testExpectedConditionsForSwitchingFrame() {
		testPage.switchToFrameWithExpectedConditionsById();
		WebElement el = testPage.getElementInsideFrame();
		Assert.assertEquals(el.getDomProperty("value"), "a value");
	}
	
	/**
	 * Check that with override, behavior is correct
	 */
	@Test(groups={"ut"})
	public void testExpectedConditionsForSwitchingFrameByName() {
		testPage.switchToFrameWithExpectedConditionsByName();
		WebElement el = testPage.getElementInsideFrame();
		Assert.assertEquals(el.getDomProperty("value"), "a value");
	}
	
	/**
	 * Check that with override, behavior is correct
	 */
	@Test(groups={"ut"})
	public void testExpectedConditionsForSwitchingFrameIndex() {
		testPage.switchToFrameWithExpectedConditionsByIndex();
		WebElement el = testPage.getElementInsideFrame();
		Assert.assertEquals(el.getDomProperty("value"), "a value");
	}
	
	/**
	 * Check that without override, behavior is correct
	 */
	@Test(groups={"ut"})
	public void testExpectedConditionsForSwitchingFrameWithoutOverride() {

		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(false);
		testPage.switchToFrameWithExpectedConditionsById();
		WebElement el = testPage.getElementInsideFrame();
		Assert.assertEquals(el.getDomProperty("value"), "a value");
	}
	
	/**
	 * Test that we can find several element inside several frames. We check frame state is not reset between findElement calls
	 */
	@Test(groups= {"ut"})
	public void testFrameStateNotReset() {

		testPage.switchToFirstFrameByIndex();
		WebElement el = testPage.getElementInsideFrame();
		Assert.assertEquals(el.getDomProperty("value"), "a value");
		WebElement el2 = testPage.getElementInsideFrame();
		Assert.assertEquals(el2.getDomProperty("value"), "a value");
	}
	
	/**
	 * Test switching to default context resets frame state
	 */
	@Test(groups= {"ut"})
	public void testDefaultContent() {
		testPage.switchToFirstFrameByIndex();
		WebElement el = testPage.getElementInsideFrame();
		Assert.assertEquals(el.getDomProperty("value"), "a value");
		testPage.switchDefaultContent();

		testPage.sendKeys();
		Assert.assertEquals(realDriver.findElement(By.id("text2")).getDomProperty("value"), "some text");
	}
	
	/**
	 * Test switching to default context resets frame state
	 */
	@Test(groups= {"ut"})
	public void testSwitchParentFrame() {
		testPage.switchToFirstFrameByElement();
		testPage.switchToSecondFrameByElement();
		WebElement el = testPage.getElementInsideFrameOfFrame();
		Assert.assertEquals(el.getDomProperty("value"), "an other value in iframe");
		testPage.switchParentFrame();
		WebElement el1 = testPage.getElementInsideFrame();
		Assert.assertEquals(el1.getDomProperty("value"), "a value");
	}
	
	@Test(groups={"ut"})
	public void testSwitchParentWithoutParentFrame() {
		testPage.switchParentFrame();
		testPage.select();
		Assert.assertEquals(new Select(realDriver.findElement(By.id("select"))).getFirstSelectedOption().getText(), "option1");
	}
	
	/**
	 * Test that findElements call is also intercepted
	 */
	@Test(groups= {"ut"})
	public void testFindElements() {
		testPage.switchToFirstFrameByIndex();
		Assert.assertEquals(testPage.getElementsInsideFrame().size(), 4);

	}
	
}
