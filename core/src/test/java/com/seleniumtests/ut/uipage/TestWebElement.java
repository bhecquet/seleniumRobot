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
package com.seleniumtests.ut.uipage;

import org.mockito.Mock;
import org.mockito.Spy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.Select;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPageNativeActions;
import com.seleniumtests.uipage.htmlelements.HtmlElement;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;

/**
 * Test class for checking seleniumNativeAction override
 * When this option is true, we should have the selenium standard behavior + replay and logging (checked elsewhere) 
 * @author behe
 *
 */
@PrepareForTest({WebUIDriver.class, AppiumDriver.class, RemoteWebDriver.class})
public class TestWebElement extends MockitoTest {
	
	@Mock
	private RemoteWebDriver driver;
	
	@Mock
	private MobileElement mobileElement;
	@Mock
	private RemoteWebElement element;
	@Mock
	private RemoteWebElement subElement1;
	@Mock
	private RemoteWebElement subElement2;
	
	@Mock
	private Mouse mouse;
	
	@Mock
	private Keyboard keyboard;
	
	@Mock
	private TargetLocator locator;
	
	@Spy
	private HtmlElement el = new HtmlElement("element", By.id("el"));

	private static DriverTestPageNativeActions testPage;
	private static WebDriver realDriver;
	
	@BeforeClass(groups={"ut"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		
		testPage = new DriverTestPageNativeActions(true);
		realDriver = WebUIDriver.getWebDriver(true);
	}
	
	@BeforeMethod(groups={"ut"})
	public void overrideNativeActions() {
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(true);
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
	}
	
	@AfterMethod(groups={"ut"}, alwaysRun=true)
	public void reset() {
		
		// call testPage switchTo().defaultContent() so that it can be intercepted and seleniumRobot internal state can be reset
		if (testPage != null) {
			testPage.switchDefaultContent();
		}
	}
	
	@Test(groups={"ut"})
	public void testFindElementOverride() {
		Assert.assertTrue(testPage.getElement() instanceof HtmlElement);
	}
	
	@Test(groups={"ut"})
	public void testSendKeys() {
		try {
			testPage.sendKeys();
			Assert.assertEquals(realDriver.findElement(By.id("text2")).getAttribute("value"), "some text");
		} finally {
			testPage.reset();
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
		Assert.assertEquals(el.getAttribute("value"), "a value");
	}
	
	/**
	 * Test that we can find an element inside frame by index, using standard Selenium writing (switchTo().frame())
	 */
	@Test(groups= {"ut"})
	public void testSingleFrameByIndex() {
		testPage.switchToFirstFrameByIndex();
		WebElement el = testPage.getElementInsideFrame();
		Assert.assertEquals(el.getAttribute("value"), "a value");
	}
	
	/**
	 * Test that we can find an element inside frame by index, using standard Selenium writing (switchTo().frame())
	 */
	@Test(groups= {"ut"})
	public void testSingleFrameByNameOrId() {
		testPage.switchToFirstFrameByNameOrId();
		WebElement el = testPage.getElementInsideFrame();
		Assert.assertEquals(el.getAttribute("value"), "a value");
	}
	
	/**
	 * Test that we can find an element inside several frames
	 */
	@Test(groups= {"ut"})
	public void testSeveralFrames() {
		testPage.switchToFirstFrameByElement();
		testPage.switchToSecondFrameByElement();
		WebElement el = testPage.getElementInsideFrameOfFrame();
		Assert.assertEquals(el.getAttribute("value"), "an other value in iframe");
	}
	

	/**
	 * Check that with override, behavior is correct
	 */
	@Test(groups={"ut"})
	public void testExpectedConditionsForSwitchingFrame() {
		testPage.switchToFrameWithExpectedConditionsById();
		WebElement el = testPage.getElementInsideFrame();
		Assert.assertEquals(el.getAttribute("value"), "a value");
	}
	
	/**
	 * Check that with override, behavior is correct
	 */
	@Test(groups={"ut"})
	public void testExpectedConditionsForSwitchingFrameByName() {
		testPage.switchToFrameWithExpectedConditionsByName();
		WebElement el = testPage.getElementInsideFrame();
		Assert.assertEquals(el.getAttribute("value"), "a value");
	}
	
	/**
	 * Check that with override, behavior is correct
	 */
	@Test(groups={"ut"})
	public void testExpectedConditionsForSwitchingFrameIndex() {
		testPage.switchToFrameWithExpectedConditionsByIndex();
		WebElement el = testPage.getElementInsideFrame();
		Assert.assertEquals(el.getAttribute("value"), "a value");
	}
	
	/**
	 * Check that without override, behavior is correct
	 */
	@Test(groups={"ut"})
	public void testExpectedConditionsForSwitchingFrameWithoutOverride() {

		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(false);
		testPage.switchToFrameWithExpectedConditionsById();
		WebElement el = testPage.getElementInsideFrame();
		Assert.assertEquals(el.getAttribute("value"), "a value");
	}
	
	/**
	 * Test that we can find several element inside several frames. We check frame state is not reset between findElement calls
	 */
	@Test(groups= {"ut"})
	public void testFrameStateNotReset() {

		testPage.switchToFirstFrameByIndex();
		WebElement el = testPage.getElementInsideFrame();
		Assert.assertEquals(el.getAttribute("value"), "a value");
		WebElement el2 = testPage.getElementInsideFrame();
		Assert.assertEquals(el2.getAttribute("value"), "a value");
	}
	
	/**
	 * Test switching to default context resets frame state
	 */
	@Test(groups= {"ut"})
	public void testDefaultContent() {
		testPage.switchToFirstFrameByIndex();
		WebElement el = testPage.getElementInsideFrame();
		Assert.assertEquals(el.getAttribute("value"), "a value");
		testPage.switchDefaultContent();
		
		try {
			testPage.sendKeys();
			Assert.assertEquals(realDriver.findElement(By.id("text2")).getAttribute("value"), "some text");
		} finally {
			testPage.reset();
		}
	}
	
	/**
	 * Test switching to default context resets frame state
	 */
	@Test(groups= {"ut"})
	public void testSwitchParentFrame() {
		testPage.switchToFirstFrameByElement();
		testPage.switchToSecondFrameByElement();
		WebElement el = testPage.getElementInsideFrameOfFrame();
		Assert.assertEquals(el.getAttribute("value"), "an other value in iframe");
		testPage.switchParentFrame();
		WebElement el1 = testPage.getElementInsideFrame();
		Assert.assertEquals(el1.getAttribute("value"), "a value");
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
