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
package com.seleniumtests.ut.uipage.htmlelements;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.SessionId;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.reporter.logger.TestStep.StepStatus;
import com.seleniumtests.uipage.ByC;
import com.seleniumtests.uipage.htmlelements.FrameElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.AppiumCommandExecutor;

/**
 * Test class for checking calls to a standard HTMLElement without using any
 * driver Here, we'll concentrate on verifying that refresh calls are done
 * before any action on element
 * 
 * @author behe
 *
 */
public class TestHtmlElement extends MockitoTest {

	@Mock
	private RemoteWebDriver driver;

	private AndroidDriver mobileDriver;

	@Mock
	private RemoteWebElement mobileElement;
	@Mock
	private RemoteWebElement element;
	@Mock
	private RemoteWebElement subElement1;
	@Mock
	private RemoteWebElement subElement2;
	@Mock
	private RemoteWebElement frameElement;
	
	@Mock
	private SearchContext searchContext;

	@Mock
	private TargetLocator locator;

	@Mock
	private WebUIDriver uiDriver;
	
	@Mock
	private Options options;
	
	@Mock
	private Timeouts timeouts;

	@Mock
	private DriverConfig driverConfig;

	@Spy
	private HtmlElement el = new HtmlElement("element", By.id("el"));
	private HtmlElement el1 = new HtmlElement("element", By.id("el1"), el);
	
	@Spy
	private FrameElement frame = new FrameElement("frame", By.id("frame"));
	
	// issue #325

	private CustomEventFiringWebDriver eventDriver;

	private MockedStatic mockedWebUIDriver;

	@BeforeMethod(groups = { "ut" })
	private void init() throws WebDriverException, IOException {

		// add capabilities to allow augmenting driver
		when(driver.getCapabilities()).thenReturn(new DesiredCapabilities()); 
		
		// mimic sub elements of the HtmlElement
		List<WebElement> subElList = new ArrayList<WebElement>();
		subElList.add(subElement1);
		subElList.add(subElement2);

		// list of elements correspond
		List<WebElement> elList = new ArrayList<WebElement>();
		elList.add(element);

		// add DriverExceptionListener to reproduce driver behavior
		eventDriver = spy(new CustomEventFiringWebDriver(driver));

		mockedWebUIDriver = mockStatic(WebUIDriver.class);
		when(WebUIDriver.getWebDriver(anyBoolean())).thenReturn(eventDriver);
		when(WebUIDriver.getWebUIDriver(anyBoolean())).thenReturn(uiDriver);
		when(driver.findElement(By.id("el"))).thenReturn(element);
		when(driver.findElement(By.id("frame"))).thenReturn(frameElement);
		when(driver.findElements(By.id("frame"))).thenReturn(Arrays.asList(frameElement));
		when(driver.findElements(By.name("subEl"))).thenReturn(subElList);
		when(driver.findElement(By.name("subEl"))).thenReturn(subElement1);
		when(driver.findElements(By.id("el"))).thenReturn(elList);
		when(driver.switchTo()).thenReturn(locator);
		when(driver.manage()).thenReturn(options);
		when(options.timeouts()).thenReturn(timeouts);
		when(driver.executeScript(anyString())).thenReturn(Arrays.asList(100, 100));

		when(uiDriver.getConfig()).thenReturn(driverConfig);
		when(driverConfig.getBrowserType()).thenReturn(BrowserType.HTMLUNIT);
		when(driverConfig.getMajorBrowserVersion()).thenReturn(1);
		
		when(eventDriver.getBrowserInfo()).thenReturn(new BrowserInfo(BrowserType.HTMLUNIT, "999.9"));

		when(element.findElement(By.name("subEl"))).thenReturn(subElement1);
		when(element.findElements(By.name("subEl"))).thenReturn(subElList);
		when(element.getAttribute(anyString())).thenReturn("attribute");
		when(element.getDomAttribute(anyString())).thenReturn("attribute");
		when(element.getDomProperty(anyString())).thenReturn("property");
		when(element.getSize()).thenReturn(new Dimension(10, 10));
		when(element.getLocation()).thenReturn(new Point(5, 5));
		when(frame.getLocation()).thenReturn(new Point(5, 5));
		when(element.getTagName()).thenReturn("h1");
		when(element.getText()).thenReturn("text");
		when(element.getRect()).thenReturn(new Rectangle(10, 10, 100, 200));
		when(element.getAriaRole()).thenReturn("role");
		when(element.getShadowRoot()).thenReturn(searchContext);
		when(element.getAccessibleName()).thenReturn("name");
		when(element.isDisplayed()).thenReturn(true);
		when(element.isEnabled()).thenReturn(true);

		when(subElement1.isDisplayed()).thenReturn(true);
		when(subElement2.isDisplayed()).thenReturn(true);
		when(subElement1.getLocation()).thenReturn(new Point(5, 5));
		when(subElement2.getLocation()).thenReturn(new Point(5, 5));

		when(mobileElement.getLocation()).thenReturn(new Point(1, 1));
		when(mobileElement.getSize()).thenReturn(new Dimension(8, 8));
		when(mobileElement.isDisplayed()).thenReturn(true);
		when(mobileElement.getId()).thenReturn("1234");

//		// init for mobile tests
		AppiumCommandExecutor ce = Mockito.mock(AppiumCommandExecutor.class);
		Response response = new Response(new SessionId("1"));
		response.setValue(new HashMap<>());
		Response findResponse = new Response(new SessionId("1"));
		findResponse.setValue(mobileElement);

		// newSession, getSession, getSession, findElement
		when(ce.execute(any())).thenReturn(response, response, response, findResponse);
		doReturn(response).when(ce).execute(argThat(command -> DriverCommand.NEW_SESSION.equals(command.getName())));
		doReturn(response).when(ce).execute(argThat(command -> DriverCommand.FIND_ELEMENT.equals(command.getName())));
		doReturn(response).when(ce).execute(argThat(command -> "getSession".equals(command.getName())));

		mobileDriver = Mockito.spy(new AndroidDriver(ce, new DesiredCapabilities()));
		doReturn("my.package").when(mobileDriver).getCurrentPackage();

		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
	}

	@AfterMethod(groups={"ut"}, alwaysRun = true)
	private void closeMocks() {
		mockedWebUIDriver.close();
	}

	private void finalCheck(boolean findElement) throws Exception {
		// check we called getDriver before using it
		verify(el, atLeastOnce()).updateDriver();

		// isElementPresent does not call findElement as we use WebDriverWait
		if (findElement) {
			verify(el).findElement(anyBoolean(), anyBoolean());
		}
	}

	@Test(groups = { "ut" })
	public void testToString() throws Exception {
		Assert.assertEquals(el1.toString(),
				"HtmlElement element, by={By.id: el1}, sub-element of HtmlElement element, by={By.id: el}");
	}

	@Test(groups = { "ut" })
	public void testClick() throws Exception {
		el.click();
		finalCheck(true);

		// check handled are updated on click
		verify( driver).getWindowHandles();
	}

	@Test(groups = { "ut" })
	public void testSimulateClick() throws Exception {
		el.simulateClick();
		finalCheck(true);
		
		verify(driver).executeScript("if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover', true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}", el.getRealElement(), new Object[] {});
		verify(driver).executeScript("if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('click', true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onclick');}", el.getRealElement(), new Object[] {});

		// check handled are updated on click
		verify((CustomEventFiringWebDriver) eventDriver).updateWindowsHandles();

	}

	@Test(groups = { "ut" })
	public void testSimulateSendKeys() throws Exception {
		el.simulateSendKeys("foo");
		verify(driver).executeScript("arguments[0].focus();", el.getRealElement(), new Object[] {});
		verify(driver).executeScript("arguments[0].value='foo';", el.getRealElement(), new Object[] {});
		finalCheck(true);
	}
	
	@Test(groups = { "ut" })
	public void testSimulateSendKeysClear() throws Exception {
		el.simulateSendKeys("");
		verify(driver).executeScript("arguments[0].focus();", el.getRealElement(), new Object[] {});
		verify(driver).executeScript("arguments[0].value='';", el.getRealElement(), new Object[] {});
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testSimulateMoveToElement() throws Exception {
		el.simulateMoveToElement(1, 1);
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testFindElementsBy() throws Exception {
		Assert.assertEquals(el.findElements(By.name("subEl")).size(), 2);
		finalCheck(true);
	}

	/**
	 * Check we get the first sub-element of our root element "el"
	 */
	@Test(groups = { "ut" })
	public void testFindSubElement() throws Exception {
		HtmlElement subEl = el.findElement(By.name("subEl"));
		Assert.assertEquals(subEl.getElement().toString(), "Decorated {subElement1}");
		finalCheck(true);
	}

	/**
	 * Check we get the Nth sub-element of our root element "el"
	 */
	@Test(groups = { "ut" })
	public void testFindNthSubElement() throws Exception {
		HtmlElement subEl = el.findElement(By.name("subEl"), 1);
		Assert.assertEquals(subEl.getElement().toString(), "Decorated {subElement2}");
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testFindElements() throws Exception {
		Assert.assertEquals(el.findElements().size(), 1);
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testGetAttribute() throws Exception {
		Assert.assertEquals(el.getAttribute("attr"), "attribute");
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testGetHeight() throws Exception {
		Assert.assertEquals(el.getHeight(), 10);
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testGetWidth() throws Exception {
		Assert.assertEquals(el.getWidth(), 10);
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testLocation() throws Exception {
		Assert.assertEquals(el.getLocation().getX(), 5);
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testTagName() throws Exception {
		Assert.assertEquals(el.getTagName(), "h1");
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testText() throws Exception {
		Assert.assertEquals(el.getText(), "text");
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testValue() throws Exception {
		Assert.assertEquals(el.getValue(), "property");
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testIsDisplayed() throws Exception {
		Assert.assertEquals(el.isDisplayed(), true);
		finalCheck(true);
	}

	/**
	 * Check exception handling and action replay
	 * 
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testIsDisplayedException() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		when(element.isDisplayed()).thenThrow(new WebDriverException("error"));
		Assert.assertFalse(el.isDisplayed());

		// updateDriver is called on every replay, so if we have 2 invocations, it means
		// that replay has been done
		verify(el, atLeast(2)).updateDriver();

		verify(el, times(1)).isDisplayedRetry();
	}
	
	/**
	 * #548: check that if a NullPointerException is raised during replay, this does not affect the logging
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testReplayActionWithNPE() throws Exception {
		TestStep step = new TestStep("step", Reporter.getCurrentTestResult(), new ArrayList<>(), false);
		
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setRootTestStep(step);// add a step so that we can reproduce the NPE bug in "ReplayAction" class
		when(element.isDisplayed()).thenThrow(new NullPointerException());
		try {
			el.isDisplayedRetry();
		} catch (NullPointerException e) {
			// ignore the error, as it's expected
		}
		
		Assert.assertEquals(step.getStepActions().size(), 2);
		Assert.assertEquals(step.getStepActions().get(1).getName(), "Warning: null"); // check message is logged
	}

	/**
	 * Check that when using isElementPresent, step is not marked as failed. So
	 * report will show step as green check correction of issue #104
	 * 
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testIsPresentExceptionDoNotSetStepFailed() throws Exception {
		TestStep step = new TestStep("step 1", null, new ArrayList<>(), true);
		TestStepManager.setParentTestStep(step);

		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		when(driver.findElement(By.id("el"))).thenThrow(new NoSuchElementException(""));
		Assert.assertEquals(el.isElementPresent(1), false);

		Assert.assertEquals(step.getStepStatus(), StepStatus.SUCCESS);
	}

	/**
	 * check correction of issue #10': step should be failed for all other actions
	 * but waitForPresent
	 * 
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testIsDisplayedExceptionSetStepFailed() throws Exception {
		TestStep step = new TestStep("step 1", null, new ArrayList<>(), true);
		TestStepManager.setParentTestStep(step);

		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		when(element.isDisplayed()).thenThrow(new WebDriverException("error"));
		Assert.assertFalse(el.isDisplayed());

		Assert.assertEquals(step.getStepStatus(), StepStatus.WARNING);
	}

	@Test(groups = { "ut" })
	public void testIsElementPresent() throws Exception {
		Assert.assertEquals(el.isElementPresent(1), true);
		finalCheck(false);
	}

	@Test(groups = { "ut" })
	public void testIsEnabled() throws Exception {
		Assert.assertEquals(el.isEnabled(), true);
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testIsTextPresentPattern() throws Exception {
		Assert.assertEquals(el.isTextPresent("\\w+xt"), true);
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testIsTextPresentText() throws Exception {
		Assert.assertEquals(el.isTextPresent("text"), true);
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testSimulateMouseOver() throws Exception {
		el.simulateMouseOver();
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testSendKeys() throws Exception {
		el.sendKeys("someText");
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testSendKeysWithPause() throws Exception {
		el.sendKeysAction(10, "toto", "titi", "meduse");
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testSendKeysWithException() throws Exception {
		when(element.getAttribute("type")).thenThrow(WebDriverException.class);
		el.sendKeys("someText");
		verify(element).clear();
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testFindPatternInText() throws Exception {
		Assert.assertEquals(el.findPattern(Pattern.compile("\\w(\\w+)\\w"), "text"), "ex");
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testFindPatternInAttr() throws Exception {
		Assert.assertEquals(el.findPattern(Pattern.compile("\\w(\\w+)\\w"), "attr"), "ttribut");
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testFindPatternInAttrNoMatch() throws Exception {
		Assert.assertEquals(el.findPattern(Pattern.compile("\\w(\\d+)\\w"), "attr"), "");
		finalCheck(true);
	}
	
	/**
	 * Check selenium method is called
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testGetDomAttribute() throws Exception {
		Assert.assertEquals(el.getDomAttribute("someAttribute"), "attribute");
		finalCheck(true);
	}
	@Test(groups = { "ut" })
	public void testGetDomProperty() throws Exception {
		Assert.assertEquals(el.getDomProperty("someAttribute"), "property");
		finalCheck(true);
	}
	@Test(groups = { "ut" })
	public void testGetAriaRole() throws Exception {
		Assert.assertEquals(el.getAriaRole(), "role");
		finalCheck(true);
	}
	@Test(groups = { "ut" })
	public void testGetAccessibilityName() throws Exception {
		Assert.assertEquals(el.getAccessibleName(), "name");
		finalCheck(true);
	}
	@Test(groups = { "ut" })
	public void testGetShadowRoot() throws Exception {
		Assert.assertEquals(el.getShadowRoot(), searchContext);
		finalCheck(true);
	}

	@Test(groups = { "ut" })
	public void testGetCenter() throws Exception {

		Point center = el.getCenter();
		Assert.assertEquals(center.getX(), 110);
		Assert.assertEquals(center.getY(), 60);
	}

	@Test(groups = { "ut" })
	public void testElementNotFoundDefaultTimeout() throws Exception {
		HtmlElement elNotPresent = new HtmlElement("element", By.id("notPresent"));
		when(driver.findElement(By.id("notPresent"))).thenThrow(new NoSuchElementException("Unable to locate element with ID: 'notPresent'"));
		LocalDateTime start = LocalDateTime.now();
		try {
			elNotPresent.getValue();
		} catch (NoSuchElementException e) {

		}
		Assert.assertTrue(LocalDateTime.now().minusSeconds(29).isAfter(start));
	}
	
	@Test(groups = { "ut" })
	public void testElementNotFoundLowTimeout() throws Exception {
		HtmlElement elNotPresent2 = new HtmlElement("element", By.id("notPresent"), (Integer)null, 5);
		when(driver.findElement(By.id("notPresent"))).thenThrow(new NoSuchElementException("Unable to locate element with ID: 'notPresent'"));
		LocalDateTime start = LocalDateTime.now();
		try {
			elNotPresent2.getValue();
		} catch (WebDriverException e) {
			
		}
		Assert.assertTrue(LocalDateTime.now().minusSeconds(6).isBefore(start));
		Assert.assertTrue(LocalDateTime.now().minusSeconds(4).isAfter(start));
	}

	/**
	 * issue #325: check NoSuchElementException exception is raised with index 0
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testElementNotFoundWithIndex() throws Exception {
		HtmlElement elNotPresent3 = new HtmlElement("element", By.id("notPresent"), 1, 3);
		when(driver.findElement(By.id("notPresent"))).thenThrow(new NoSuchElementException("Unable to locate element with ID: 'notPresent'"));
		when(driver.findElements(By.id("notPresent"))).thenThrow(new NoSuchElementException("Unable to locate element with ID: 'notPresent'"));
		try {
			elNotPresent3.getValue();
			throw new Exception("we should not be there");
		} catch (NoSuchElementException e) {
			// we expect not to have an IndexOutOfBoundsException
		}
	}
	

	@Test(groups = { "ut" })
	public void testIsElementPresentNotFound() throws Exception {
		HtmlElement elNotPresent = new HtmlElement("element", By.id("notPresent"));
		when(driver.findElement(By.id("notPresent"))).thenThrow(new NoSuchElementException("Unable to locate element with ID: 'notPresent'"));
		LocalDateTime start = LocalDateTime.now();
		
		boolean exceptionRaised = false;
		try {
			elNotPresent.waitForPresent(5);
		} catch (TimeoutException e) {
			exceptionRaised = true;
		}
		Assert.assertTrue(exceptionRaised);
		Assert.assertTrue(LocalDateTime.now().minusSeconds(5).isAfter(start));
	}
	
	@Test(groups = { "ut" })
	public void testIsElementPresentFound() throws Exception {
		HtmlElement present = new HtmlElement("element", By.id("present"));
		when(driver.findElement(By.id("present"))).thenReturn(el);
		LocalDateTime start = LocalDateTime.now();
		
		boolean exceptionRaised = false;
		try {
			present.waitForPresent(5);
		} catch (TimeoutException e) {
			exceptionRaised = true;
		}
		Assert.assertFalse(exceptionRaised);
		Assert.assertTrue(LocalDateTime.now().minusSeconds(5).isBefore(start));
	}
	
	/**
	 * Check that element is found with frame
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testIsElementPresentFoundWithFrame() throws Exception {
		HtmlElement present = new HtmlElement("element", By.id("present"), frame);
		when(driver.findElement(By.id("present"))).thenReturn(el);
		LocalDateTime start = LocalDateTime.now();
		
		boolean exceptionRaised = false;
		try {
			present.waitForPresent(5);
		} catch (TimeoutException e) {
			exceptionRaised = true;
		}
		verify(driver.switchTo()).frame(any(WebElement.class));
		Assert.assertFalse(exceptionRaised);
		Assert.assertTrue(LocalDateTime.now().minusSeconds(5).isBefore(start));
	}
	
	/**
	 * Check that element is not found if frame is not found
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testIsElementPresentNotFoundWithFrame() throws Exception {
		HtmlElement present = new HtmlElement("element", By.id("present"), frame);
		when(driver.findElement(By.id("present"))).thenReturn(el);
		when(driver.findElement(By.id("frame"))).thenThrow(NoSuchElementException.class);
		when(driver.findElements(By.id("frame"))).thenReturn(new ArrayList<>());
		LocalDateTime start = LocalDateTime.now();
	
		boolean exceptionRaised = false;
		try {
			present.waitForPresent(1);
		} catch (TimeoutException e) {
			exceptionRaised = true;
		}
		verify(driver.switchTo(), never()).frame(any(WebElement.class));
		Assert.assertTrue(exceptionRaised);
		Assert.assertTrue(LocalDateTime.now().minus(900, ChronoUnit.MILLIS).isAfter(start));
	}
	
	/**
	 * Check that element is found if frame is not found on first search
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testIsElementPresentFoundWithFrameNotFoundOnFirstTry() throws Exception {
		HtmlElement present = new HtmlElement("element", By.id("present"), frame);
		when(driver.findElement(By.id("present"))).thenReturn(el);
		when(driver.findElement(By.id("frame"))).thenThrow(NoSuchElementException.class).thenReturn(frameElement);
		when(driver.findElements(By.id("frame"))).thenReturn(new ArrayList<>()).thenReturn(Arrays.asList(frameElement));
		LocalDateTime start = LocalDateTime.now();
		
		boolean exceptionRaised = false;
		try {
			present.waitForPresent(3);
		} catch (TimeoutException e) {
			exceptionRaised = true;
		}
		verify(driver.switchTo()).frame(any(WebElement.class));
		Assert.assertFalse(exceptionRaised);
		Assert.assertTrue(LocalDateTime.now().minusSeconds(3).isBefore(start));
	}
	@Test(groups = { "ut" })
	public void testWaitForVisibilityFound() throws Exception {
		HtmlElement present = new HtmlElement("element", By.id("present"));
		when(driver.findElement(By.id("present"))).thenReturn(el);
		LocalDateTime start = LocalDateTime.now();
		
		boolean exceptionRaised = false;
		try {
			present.waitForVisibility(5);
		} catch (TimeoutException e) {
			exceptionRaised = true;
		}
		Assert.assertFalse(exceptionRaised);
		Assert.assertTrue(LocalDateTime.now().minusSeconds(5).isBefore(start));
	}
	

	@Test(groups = { "ut" })
	public void testWaitForVisibilityNotFound() throws Exception {
		HtmlElement elNotPresent = new HtmlElement("element", By.id("notPresent"));
		when(driver.findElement(By.id("notPresent"))).thenThrow(new NoSuchElementException("Unable to locate element with ID: 'notPresent'"));
		LocalDateTime start = LocalDateTime.now();
		
		boolean exceptionRaised = false;
		try {
			elNotPresent.waitForVisibility(5);
		} catch (TimeoutException e) {
			exceptionRaised = true;
		}
		Assert.assertTrue(exceptionRaised);
		Assert.assertTrue(LocalDateTime.now().minusSeconds(5).isAfter(start));
	}
	

	/**
	 * Check that element is visible with frame
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testWaitForVisibilityFoundWithFrame() throws Exception {
		HtmlElement present = new HtmlElement("element", By.id("present"), frame);
		when(driver.findElement(By.id("present"))).thenReturn(el);
		LocalDateTime start = LocalDateTime.now();
		
		boolean exceptionRaised = false;
		try {
			present.waitForVisibility(5);
		} catch (TimeoutException e) {
			exceptionRaised = true;
		}
		verify(driver.switchTo()).frame(any(WebElement.class));
		Assert.assertFalse(exceptionRaised);
		Assert.assertTrue(LocalDateTime.now().minusSeconds(5).isBefore(start));
	}
	
	/**
	 * Check selector is not replaced if we are not a child of shadow root
	 */
	@Test(groups = { "ut" })
	public void testReplaceSelector() {
		HtmlElement present = new HtmlElement("element", By.xpath("present"));
		present.replaceSelector();
		Assert.assertEquals(present.getBy(), By.xpath("present"));
	}
	
	/**
	 * Check xpath selector is not valid if we are a child of shadow root
	 */
	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testReplaceSelectorWithShadowRootAndXpath() {
		HtmlElement present = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(By.xpath("//div"));
		present.replaceSelector();
	}
	@Test(groups = { "ut" })
	public void testReplaceSelectorWithShadowRootAndTagName() {
		HtmlElement present = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(By.tagName("div"));
		present.replaceSelector();
		Assert.assertEquals(present.getBy(), By.cssSelector("div"));
	}
	@Test(groups = { "ut" })
	public void testReplaceSelectorWithShadowRootAndName() {
		HtmlElement present = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(By.name("foo"));
		present.replaceSelector();
		Assert.assertEquals(present.getBy(), By.cssSelector("[name=foo]"));
	}
	@Test(groups = { "ut" })
	public void testReplaceSelectorWithShadowRootAndId() {
		HtmlElement present = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(By.id("foo"));
		present.replaceSelector();
		Assert.assertEquals(present.getBy(), By.id("foo"));
	}
	@Test(groups = { "ut" })
	public void testReplaceSelectorWithShadowRootAndLinkText() {
		HtmlElement present = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(By.linkText("foo"));
		present.replaceSelector();
		Assert.assertEquals(present.getBy(), By.linkText("foo"));
	}
	@Test(groups = { "ut" })
	public void testReplaceSelectorWithShadowRootAndXId() {
		HtmlElement present = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(ByC.xId("foo"));
		present.replaceSelector();
		Assert.assertEquals(present.getBy(), ByC.attribute("id", "foo"));
	}
	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testReplaceSelectorWithShadowRootAndXClassName() {
		HtmlElement present = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(ByC.xClassName("foo"));
		present.replaceSelector();
	}
	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testReplaceSelectorWithShadowRootAndXTagName() {
		HtmlElement present = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(ByC.xTagName("foo"));
		present.replaceSelector();
	}
	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testReplaceSelectorWithShadowRootAndXLinkText() {
		HtmlElement present = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(ByC.xLinkText("foo"));
		present.replaceSelector();
	}
	@Test(groups = { "ut" })
	public void testReplaceSelectorWithShadowRootAndXName() {
		HtmlElement present = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(ByC.xName("foo"));
		present.replaceSelector();
		Assert.assertEquals(present.getBy(), ByC.attribute("name", "foo"));
	}
	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testReplaceSelectorWithShadowRootAndText() {
		HtmlElement present = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(ByC.text("foo", "div"));
		present.replaceSelector();
	}
	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testReplaceSelectorWithShadowRootAndLabelForward() {
		HtmlElement present = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(ByC.labelForward("foo", "div"));
		present.replaceSelector();
	}
	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testReplaceSelectorWithShadowRootAndLabelBackward() {
		HtmlElement present = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(ByC.labelBackward("foo", "div"));
		present.replaceSelector();
	}
	@Test(groups = { "ut" })
	public void testReplaceSelectorWithShadowRootAndAttribute() {
		HtmlElement present = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(ByC.attribute("foo", "bar"));
		present.replaceSelector();
		Assert.assertEquals(present.getBy(), ByC.attribute("foo", "bar"));
		Assert.assertTrue(((ByC.ByAttribute)present.getBy()).isUseCssSelector()); // check we use the Css Selector instead of XPath
	}
	
	/**
	 * #540: add automatically package name for id selector on android
	 */
	@Test(groups = { "ut" })
	public void testReplaceSelectorAndroidId() {
		
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);
		SeleniumTestsContextManager.getThreadContext().setPlatform("android");
		
		HtmlElement present = new HtmlElement("element", By.id("present"));
		present.setDriver(new CustomEventFiringWebDriver(mobileDriver)); // mimic the findElement call were we update driver before doing anything
		present.replaceSelector();
		Assert.assertEquals(present.getBy(), By.id("my.package:id/present"));
	}
	
	/**
	 * #540: do not add automatically package name for id selector on chrome android
	 */
	@Test(groups = { "ut" })
	public void testReplaceSelectorAndroidWebId() {
		
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_WEB_ANDROID);
		SeleniumTestsContextManager.getThreadContext().setPlatform("android");
		
		HtmlElement present = new HtmlElement("element", By.id("present"));
		present.setDriver(new CustomEventFiringWebDriver(mobileDriver)); // mimic the findElement call were we update driver before doing anything
		present.replaceSelector();
		Assert.assertEquals(present.getBy(), By.id("present"));
	}
	
	/**
	 * #540: do not add automatically package name for id selector on iOS
	 */
	@Test(groups = { "ut" })
	public void testReplaceSelectorIOsId() {
		
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);
		SeleniumTestsContextManager.getThreadContext().setPlatform("ios");
		
		HtmlElement present = new HtmlElement("element", By.id("present"));
		present.setDriver(new CustomEventFiringWebDriver(mobileDriver)); // mimic the findElement call were we update driver before doing anything
		present.replaceSelector();
		Assert.assertEquals(present.getBy(), By.id("present"));
	}
	
	/**
	 * #540: add automatically package name for AppiumBy.id selector on android
	 */
	@Test(groups = { "ut" })
	public void testReplaceSelectorAndroidAppiumId() {
		
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);
		SeleniumTestsContextManager.getThreadContext().setPlatform("android");
		
		HtmlElement present = new HtmlElement("element", AppiumBy.id("present"));
		present.setDriver(new CustomEventFiringWebDriver(mobileDriver)); // mimic the findElement call were we update driver before doing anything
		present.replaceSelector();
		Assert.assertEquals(present.getBy(), By.id("my.package:id/present"));
	}
	/**
	 * #540: add automatically package name for AppiumBy.id selector on android
	 */
	@Test(groups = { "ut" })
	public void testReplaceSelectorAndroidAppiumAccessibilityId() {
		
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);
		SeleniumTestsContextManager.getThreadContext().setPlatform("android");
		
		HtmlElement present = new HtmlElement("element", AppiumBy.accessibilityId("present"));
		present.setDriver(new CustomEventFiringWebDriver(mobileDriver)); // mimic the findElement call were we update driver before doing anything
		present.replaceSelector();
		Assert.assertEquals(present.getBy(), By.id("my.package:id/present"));
	}
	
	/**
	 * #540: If andoird package is already specified, do not add it
	 */
	@Test(groups = { "ut" })
	public void testReplaceSelectorAndroidIdPackageGiven() {
		
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);
		SeleniumTestsContextManager.getThreadContext().setPlatform("android");
		
		HtmlElement present = new HtmlElement("element", By.id("a.package:id/present"));
		present.setDriver(new CustomEventFiringWebDriver(mobileDriver)); // mimic the findElement call were we update driver before doing anything
		present.replaceSelector();
		Assert.assertEquals(present.getBy(), By.id("a.package:id/present"));
	}
	
	
	
	
	
	
	
	
}
	