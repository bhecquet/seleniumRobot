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
package com.seleniumtests.ut.uipage.htmlelements;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.DriverExceptionListener;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.uipage.htmlelements.HtmlElement;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.remote.AppiumCommandExecutor;

/**
 * Test class for checking calls to a standard HTMLElement without using any driver
 * Here, we'll concentrate on verifying that refresh calls are done before any action on element
 * @author behe
 *
 */
@PrepareForTest({WebUIDriver.class, AppiumDriver.class, RemoteWebDriver.class})
public class TestHtmlElement extends MockitoTest {
	
	@Mock
	private RemoteWebDriver driver;
	
	private AppiumDriver mobileDriver;
	
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
	private HtmlElement el1 = new HtmlElement("element", By.id("el1"), el);
	
	private EventFiringWebDriver eventDriver;
	
	@BeforeMethod(groups={"ut"})
	private void init() throws WebDriverException, IOException {
		// mimic sub elements of the HtmlElement
		List<WebElement> subElList = new ArrayList<WebElement>();
		subElList.add(subElement1);
		subElList.add(subElement2);
		
		// list of elements correspond
		List<WebElement> elList = new ArrayList<WebElement>();
		elList.add(element);
		
		// add DriverExceptionListener to reproduce driver behavior
		eventDriver = spy(new CustomEventFiringWebDriver(driver).register(new DriverExceptionListener()));
		
		PowerMockito.mockStatic(WebUIDriver.class);
		when(WebUIDriver.getWebDriver()).thenReturn(eventDriver);
		when(driver.findElement(By.id("el"))).thenReturn(element);
		when(driver.findElements(By.name("subEl"))).thenReturn(subElList);
		when(driver.findElement(By.name("subEl"))).thenReturn(subElement1);
		when(driver.findElements(By.id("el"))).thenReturn(elList);
		when(driver.getKeyboard()).thenReturn(keyboard);
		when(driver.getMouse()).thenReturn(mouse);
		when(driver.switchTo()).thenReturn(locator);

		when(element.findElement(By.name("subEl"))).thenReturn(subElement1);
		when(element.findElements(By.name("subEl"))).thenReturn(subElList);
		when(element.getAttribute(anyString())).thenReturn("attribute");
		when(element.getSize()).thenReturn(new Dimension(10, 10));
		when(element.getLocation()).thenReturn(new Point(5, 5));
		when(element.getTagName()).thenReturn("h1");
		when(element.getText()).thenReturn("text");
		when(element.isDisplayed()).thenReturn(true);
		when(element.isEnabled()).thenReturn(true);

		when(subElement1.isDisplayed()).thenReturn(true);
		when(subElement2.isDisplayed()).thenReturn(true);
		
		when(mobileElement.getCenter()).thenReturn(new Point(2, 2));
		when(mobileElement.getLocation()).thenReturn(new Point(1, 1));
		when(mobileElement.isDisplayed()).thenReturn(true);
		when(mobileElement.getId()).thenReturn("12");
		
		// init for mobile tests
		AppiumCommandExecutor ce = Mockito.mock(AppiumCommandExecutor.class);
		Response response = new Response(new SessionId("1"));
		response.setValue(new HashMap<String, Object>());
		Response findResponse = new Response(new SessionId("1"));
		findResponse.setValue(mobileElement);

		// newSession, getSession, getSession, findElement
		when(ce.execute(anyObject())).thenReturn(response, response, response, findResponse);
		mobileDriver = Mockito.spy(new AppiumDriver(ce, new DesiredCapabilities()));
		
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
	}
	
	private void finalCheck(boolean findElement) throws Exception {
		// check we called getDriver before using it
		PowerMockito.verifyPrivate(el).invoke("updateDriver");
		
		// isElementPresent does not call findElement as we use WebDriverWait
		if (findElement) {
			PowerMockito.verifyPrivate(el).invoke("findElement", anyBoolean(), anyBoolean());
		}
	}
	
	@Test(groups={"ut"})
	public void testToString() throws Exception {
		Assert.assertEquals(el1.toString(), "HtmlElement element, by={By.id: el1}, sub-element of HtmlElement element, by={By.id: el}");
	}

	@Test(groups={"ut"})
	public void testClick() throws Exception {
		el.click();
		finalCheck(true);
		
		// check handled are updated on click
		verify((CustomEventFiringWebDriver)eventDriver).updateWindowsHandles();
	}
	
	@Test(groups={"ut"})
	public void testSimulateClick() throws Exception {
		el.simulateClick();
		finalCheck(true);
		
		// check handled are updated on click
		verify((CustomEventFiringWebDriver)eventDriver).updateWindowsHandles();
		
	}
	
	@Test(groups={"ut"})
	public void testSimulateSendKeys() throws Exception {
		el.simulateSendKeys();
		finalCheck(true);
	}

	@Test(groups={"ut"})
	public void testSimulateMoveToElement() throws Exception {
		el.simulateMoveToElement(1, 1);
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testFindElementsBy() throws Exception {
		Assert.assertEquals(el.findElements(By.name("subEl")).size(), 2);
		finalCheck(true);
	}
	
	/**
	 * Check we get the first sub-element of our root element "el"
	 */
	@Test(groups={"ut"})
	public void testFindSubElement() throws Exception {
		HtmlElement subEl = el.findElement(By.name("subEl"));
		Assert.assertEquals(subEl.getElement().toString(), "subElement1");
		finalCheck(true);
	}
	
	/**
	 * Check we get the Nth sub-element of our root element "el"
	 */
	@Test(groups={"ut"})
	public void testFindNthSubElement() throws Exception {
		HtmlElement subEl = el.findElement(By.name("subEl"), 1);
		Assert.assertEquals(subEl.getElement().toString(), "subElement2");
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testFindElements() throws Exception {
		Assert.assertEquals(el.findElements().size(), 1);
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testGetAttribute() throws Exception {
		Assert.assertEquals(el.getAttribute("attr"), "attribute");
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testGetHeight() throws Exception {
		Assert.assertEquals(el.getHeight(), 10);
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testGetWidth() throws Exception {
		Assert.assertEquals(el.getWidth(), 10);
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testLocation() throws Exception {
		Assert.assertEquals(el.getLocation().getX(), 5);
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testTagName() throws Exception {
		Assert.assertEquals(el.getTagName(), "h1");
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testText() throws Exception {
		Assert.assertEquals(el.getText(), "text");
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testValue() throws Exception {
		Assert.assertEquals(el.getValue(), "attribute");
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testIsDisplayed() throws Exception {
		Assert.assertEquals(el.isDisplayed(), true);
		finalCheck(true);
	}
	
	/**
	 * Check exception handling and action replay
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testIsDisplayedException() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		when(element.isDisplayed()).thenThrow(new WebDriverException("error"));
		Assert.assertFalse(el.isDisplayed());
		
		// updateDriver is called on every replay, so if we have 2 invocations, it means that replay has been done
		PowerMockito.verifyPrivate(el, atLeast(2)).invoke("updateDriver");
		
		verify(el, times(1)).isDisplayedRetry();
	}
	
	/**
	 * Check that when using isElementPresent, step is not marked as failed. So report will show step as green
	 * check correction of issue #103
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testIsPresentExceptionDoNotSetStepFailed() throws Exception {
		TestStep step = new TestStep("step 1", null, new ArrayList<>());
		TestLogging.setParentTestStep(step);
		
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		when(driver.findElements(By.id("el"))).thenThrow(new NoSuchElementException(""));
		Assert.assertEquals(el.isElementPresent(1), false);
		
		Assert.assertFalse(step.getFailed());
	}
	
	/**
	 * check correction of issue #103: step should be failed for all other actions but waitForPresent
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testIsDisplayedExceptionSetStepFailed() throws Exception {
		TestStep step = new TestStep("step 1", null, new ArrayList<>());
		TestLogging.setParentTestStep(step);
		
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		when(element.isDisplayed()).thenThrow(new WebDriverException("error"));
		Assert.assertFalse(el.isDisplayed());
		
		Assert.assertTrue(step.getFailed());
	}
	
	@Test(groups={"ut"})
	public void testIsElementPresent() throws Exception {
		Assert.assertEquals(el.isElementPresent(1), true);
		finalCheck(false);
	}
	
	@Test(groups={"ut"})
	public void testIsEnabled() throws Exception {
		Assert.assertEquals(el.isEnabled(), true);
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testIsTextPresentPattern() throws Exception {
		Assert.assertEquals(el.isTextPresent("\\w+xt"), true);
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testIsTextPresentText() throws Exception {
		Assert.assertEquals(el.isTextPresent("text"), true);
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testMouseDown() throws Exception {
		el.mouseDown();
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testMouseOver() throws Exception {
		el.mouseOver();
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testMouseUp() throws Exception {
		el.mouseUp();
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testSimulateMouseOver() throws Exception {
		el.simulateMouseOver();
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testSendKeys() throws Exception {
		el.sendKeys("someText");
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testSendKeysWithException() throws Exception {
		when(element.getAttribute("type")).thenThrow(WebDriverException.class);
		el.sendKeys("someText");
		verify(element).clear();
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testFindPatternInText() throws Exception {
		Assert.assertEquals(el.findPattern(Pattern.compile("\\w(\\w+)\\w"), "text"), "ex");
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testFindPatternInAttr() throws Exception {
		Assert.assertEquals(el.findPattern(Pattern.compile("\\w(\\w+)\\w"), "attr"), "ttribut");
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testFindPatternInAttrNoMatch() throws Exception {
		Assert.assertEquals(el.findPattern(Pattern.compile("\\w(\\d+)\\w"), "attr"), "");
		finalCheck(true);
	}
	
	/**
	 * A desktop driver makes exception raise
	 * @throws Exception
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testPinchWithDesktopDriver() throws Exception {
		el.pinch();
		finalCheck(true);
	}
	
	@Test(groups={"ut"})
	public void testPinch() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_WEB_ANDROID);
		when(WebUIDriver.getWebDriver()).thenReturn(new CustomEventFiringWebDriver(mobileDriver));
		doNothing().when(el).findElement(anyBoolean(), anyBoolean());
		el.setElement(mobileElement);
		el.pinch();
		PowerMockito.verifyPrivate(el).invoke("checkForMobile");
	}
	
	@Test(groups={"ut"})
	public void testGetCenter() throws Exception {
		
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_WEB_ANDROID);
		when(WebUIDriver.getWebDriver()).thenReturn(new CustomEventFiringWebDriver(mobileDriver));
		doNothing().when(el).findElement(anyBoolean(), anyBoolean());
		el.setElement(mobileElement);
		el.getCenter();
		PowerMockito.verifyPrivate(el).invoke("checkForMobile");
	}
	
	@Test(groups={"ut"})
	public void testSwipe1() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_WEB_ANDROID);
		when(WebUIDriver.getWebDriver()).thenReturn(new CustomEventFiringWebDriver(mobileDriver));
		doNothing().when(el).findElement(anyBoolean(), anyBoolean());
		el.setElement(mobileElement);
		el.swipe(0, 0, 0, 10);
		PowerMockito.verifyPrivate(el).invoke("checkForMobile");
	}
	
	@Test(groups={"ut"})
	public void testTap() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_WEB_ANDROID);
		when(WebUIDriver.getWebDriver()).thenReturn(new CustomEventFiringWebDriver(mobileDriver));
		doNothing().when(el).findElement(anyBoolean(), anyBoolean());
		el.setElement(mobileElement);
		el.tap(2, 2);
		PowerMockito.verifyPrivate(el).invoke("checkForMobile");
	}
	
	@Test(groups={"ut"})
	public void testZoom() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_WEB_ANDROID);
		when(WebUIDriver.getWebDriver()).thenReturn(new CustomEventFiringWebDriver(mobileDriver));
		doNothing().when(el).findElement(anyBoolean(), anyBoolean());
		el.setElement(mobileElement);
		el.zoom();
		PowerMockito.verifyPrivate(el).invoke("checkForMobile");
	}

	
}
