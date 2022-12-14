package com.seleniumtests.ut.uipage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.driver.screenshots.SnapshotTarget;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.ut.core.runner.cucumber.PageForActions;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.osutility.OSCommand;

@PrepareForTest({ WebUIDriver.class })
public class TestPageObject2 extends MockitoTest {

	@Mock
	private RemoteWebDriver driver;

	@Mock
	private TargetLocator target;

	@Mock
	private ScreenShot screenshot;

	@Mock
	private ScreenshotUtil screenshotUtil;

	@Mock
	private WebElement element;

	@Mock
	private WebElement option1;

	@Mock
	private WebElement row1;

	@Mock
	private WebElement column1;

	@Mock
	private TargetLocator targetLocator;

	@Mock
	private Navigation navigation;

	@Mock
	private Options driverOptions;

	@Mock
	private Timeouts timeouts;

	@Mock
	private Alert alert;

	private CustomEventFiringWebDriver eventDriver;
	private PageObject page;

	@BeforeMethod(groups = { "ut" })
	private void init() throws IOException {

		SeleniumTestsContextManager.getGlobalContext()
				.setCucumberImplementationPackage("com.seleniumtests.ut.core.runner.cucumber");
		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setImplicitWaitTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(2);

		eventDriver = spy(new CustomEventFiringWebDriver(driver));

		when(eventDriver.switchTo()).thenReturn(target);
		when(driver.findElement(By.id("el"))).thenReturn(element);
		when(driver.navigate()).thenReturn(navigation);
		when(driver.getCurrentUrl()).thenReturn("http://foo");
		when(driver.manage()).thenReturn(driverOptions);
		when(driverOptions.timeouts()).thenReturn(timeouts);
		when(element.getSize()).thenReturn(new Dimension(10, 10));
		when(element.getLocation()).thenReturn(new Point(5, 5));

		when(targetLocator.alert()).thenReturn(alert);
		when(alert.getText()).thenReturn("alert text");
		when(driver.switchTo()).thenReturn(targetLocator);

		PowerMockito.mockStatic(WebUIDriver.class);
		when(WebUIDriver.getCurrentWebUiDriverName()).thenReturn("main");
		when(WebUIDriver.getWebDriver(anyBoolean(), eq(BrowserType.FIREFOX), eq("main"), isNull()))
				.thenReturn(eventDriver);
		when(WebUIDriver.getWebDriver(anyBoolean())).thenReturn(eventDriver);
		when(eventDriver.executeScript("if (document.readyState === \"complete\") { return \"ok\"; }"))
				.thenReturn("ok");
		when(eventDriver.getBrowserInfo()).thenReturn(new BrowserInfo(BrowserType.FIREFOX, "78.0"));
		when(screenshotUtil.capture(any(SnapshotTarget.class), ArgumentMatchers.<Class<ScreenShot>>any()))
				.thenReturn(screenshot);
		when(screenshotUtil.capture(any(SnapshotTarget.class), ArgumentMatchers.<Class<ScreenShot>>any(), anyInt()))
				.thenReturn(screenshot);
		when(screenshot.getHtmlSourcePath()).thenReturn("foo");
		when(screenshot.getImagePath()).thenReturn("image");

		page = new PageForActions();

	}

	@Test(groups = { "ut" })
	public void testCapturePageSnapshotNoArguments() throws IOException {

		page.setScreenshotUtil(screenshotUtil);
		String htmlFilePath = page.getHtmlFilePath();
		page.capturePageSnapshot();

		// check capture has been done on the second call (a first capture is done at
		// PageObject init)
		Assert.assertFalse(page.getHtmlFilePath().equals(htmlFilePath));
	}

	/**
	 * Capture page snapshot and sends it to selenium server
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testCapturePageSnapshotWithCheck() throws IOException {

		page.setScreenshotUtil(screenshotUtil);
		String htmlFilePath = page.getHtmlFilePath();
		page.capturePageSnapshot("img", SnapshotCheckType.TRUE);

		// check capture has been done on the second call (a first capture is done at
		// PageObject init)
		Assert.assertFalse(page.getHtmlFilePath().equals(htmlFilePath));
		
		// check scroll delay is not applied
		verify(screenshotUtil).capture(SnapshotTarget.PAGE, ScreenShot.class, 0);
	}
	
	@Test(groups = { "ut" })
	public void testCapturPageSnapshotWithCheckAndDelay() throws IOException {
	
		SeleniumTestsContextManager.getThreadContext().setSnapshotScrollDelay(100);
		
		page.setScreenshotUtil(screenshotUtil);
		page.capturePageSnapshot("img", SnapshotCheckType.TRUE);
		
		// check scroll delay is applied
		verify(screenshotUtil).capture(SnapshotTarget.PAGE, ScreenShot.class, 100);
	}
	
	/**
	 * scrollDelay is set, but snapshot is not performed for control
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testCapturePageSnapshotWithoutCheckAndDelay() throws IOException {
		
		SeleniumTestsContextManager.getThreadContext().setSnapshotScrollDelay(100);
		
		page.setScreenshotUtil(screenshotUtil);
		page.capturePageSnapshot("img", SnapshotCheckType.FALSE);

		// check scroll delay is 0 
		verify(screenshotUtil).capture(SnapshotTarget.PAGE, ScreenShot.class, 0);
	}

	/**
	 * Capture page snapshot and sends it to selenium server. No name is provided
	 * ScenarioException should be raised
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testCapturePageSnapshotWithCheckNoName() throws IOException {

		page.setScreenshotUtil(screenshotUtil);
		page.capturePageSnapshot("", SnapshotCheckType.TRUE);

	}
	
	@Test(groups = { "ut" })
	public void testCaptureViewportSnapshotNoArguments() throws IOException {
		
		page.setScreenshotUtil(screenshotUtil);
		String htmlFilePath = page.getHtmlFilePath();
		page.captureViewportSnapshot();
		
		// check capture has been done on the second call (a first capture is done at
		// PageObject init)
		Assert.assertFalse(page.getHtmlFilePath().equals(htmlFilePath));
	}
	
	/**
	 * Capture viewport snapshot and sends it to selenium server
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testCaptureViewportSnapshotWithCheck() throws IOException {
		
		page.setScreenshotUtil(screenshotUtil);
		String htmlFilePath = page.getHtmlFilePath();
		page.captureViewportSnapshot("img", SnapshotCheckType.TRUE);
		
		// check capture has been done on the second call (a first capture is done at
		// PageObject init)
		Assert.assertFalse(page.getHtmlFilePath().equals(htmlFilePath));
		
		// check scroll delay is not applied
		verify(screenshotUtil).capture(SnapshotTarget.VIEWPORT, ScreenShot.class);
	}
	
	/**
	 * Capture page snapshot and sends it to selenium server. No name is provided
	 * ScenarioException should be raised
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testCaptureViewportSnapshotWithCheckNoName() throws IOException {
		
		page.setScreenshotUtil(screenshotUtil);
		page.captureViewportSnapshot("", SnapshotCheckType.TRUE);
		
	}
	
	@Test(groups = { "ut" })
	public void testCaptureDesktopSnapshotNoArguments() throws IOException {
		
		page.setScreenshotUtil(screenshotUtil);
		String imageFilePath = page.getImageFilePath();
		page.captureDesktopSnapshot();
		
		// check capture has been done on the second call (a first capture is done at
		// PageObject init)
		Assert.assertFalse(page.getImageFilePath().equals(imageFilePath));
	}
	
	/**
	 * Capture viewport snapshot and sends it to selenium server
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testCaptureDesktopSnapshotWithCheck() throws IOException {
		
		page.setScreenshotUtil(screenshotUtil);
		String imageFilePath = page.getImageFilePath();
		page.captureDesktopSnapshot("img", SnapshotCheckType.TRUE);
		
		// check capture has been done on the second call (a first capture is done at
		// PageObject init)
		Assert.assertFalse(page.getImageFilePath().equals(imageFilePath));
		
		// check scroll delay is not applied
		verify(screenshotUtil).capture(SnapshotTarget.MAIN_SCREEN, ScreenShot.class);
	}
	
	/**
	 * Capture page snapshot and sends it to selenium server. No name is provided
	 * ScenarioException should be raised
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testCaptureDesktopSnapshotWithCheckNoName() throws IOException {
		
		page.setScreenshotUtil(screenshotUtil);
		page.captureDesktopSnapshot("", SnapshotCheckType.TRUE);
		
	}

	/**
	 * Capture element snapshot and sends it to selenium server
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testCaptureElementSnapshotWithCheck() throws IOException {

		page.setScreenshotUtil(screenshotUtil);
		String htmlFilePath = page.getHtmlFilePath();
		page.captureElementSnapshot("img", new HtmlElement("", By.id("el")), SnapshotCheckType.TRUE);

		// check capture has been done on the second call (a first capture is done at
		// PageObject init)
		Assert.assertFalse(page.getHtmlFilePath().equals(htmlFilePath));
		
		// check scroll delay is 0 by default
		verify(screenshotUtil).capture(any(SnapshotTarget.class), eq(ScreenShot.class), eq(0));
	}
	
	@Test(groups = { "ut" })
	public void testCaptureElementSnapshotWithCheckAndDelay() throws IOException {
	
		SeleniumTestsContextManager.getThreadContext().setSnapshotScrollDelay(100);
		
		page.setScreenshotUtil(screenshotUtil);
		page.captureElementSnapshot("img", new HtmlElement("", By.id("el")), SnapshotCheckType.TRUE);
		
		// check scroll delay is applied
		verify(screenshotUtil).capture(any(SnapshotTarget.class), eq(ScreenShot.class), eq(100));
	}
	
	/**
	 * scrollDelay s set, but snapshot is not performed for control
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testCaptureElementSnapshotWithoutCheckAndDelay() throws IOException {
		
		SeleniumTestsContextManager.getThreadContext().setSnapshotScrollDelay(100);
		
		page.setScreenshotUtil(screenshotUtil);
		page.captureElementSnapshot("img", new HtmlElement("", By.id("el")), SnapshotCheckType.FALSE);

		// check scroll delay is 0 by default
		verify(screenshotUtil).capture(any(SnapshotTarget.class), eq(ScreenShot.class), eq(0));
	}

	/**
	 * Capture element snapshot and sends it to selenium server. No name is provided
	 * ScenarioException should be raised
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testCaptureElementSnapshotWithCheckNoName() throws IOException {

		page.setScreenshotUtil(screenshotUtil);
		page.captureElementSnapshot("", new HtmlElement("", By.id("el")), SnapshotCheckType.TRUE);
	}

	@Test(groups = { "ut" })
	public void testGetCookieByName() {
		when(driverOptions.getCookieNamed("foo")).thenReturn(new Cookie("foo", "barfoobar"));
		Assert.assertEquals(page.getCookieByName("foo"), "barfoobar");
	}

	@Test(groups = { "ut" })
	public void testGetCookieByNameNoCookie() {
		when(driverOptions.getCookieNamed("foo")).thenReturn(null);
		Assert.assertNull(page.getCookieByName("foo"));
	}

	/**
	 * check we return to default content
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testAcceptAlert() throws IOException {
		page.acceptAlert();
		Mockito.verify(alert).accept();
		Mockito.verify(targetLocator).defaultContent();
	}

	@Test(groups = { "ut" })
	public void testDismissAlert() throws IOException {
		page.cancelConfirmation();
		Mockito.verify(alert, times(2)).dismiss();
		Mockito.verify(targetLocator).defaultContent();
	}

	@Test(groups = { "ut" })
	public void testGoBack() throws IOException {
		page.goBack();
		verify(navigation).back();
	}

	@Test(groups = { "ut" })
	public void testGoForward() throws IOException {
		page.goForward();
		verify(navigation).forward();
	}

	@Test(groups = { "ut" })
	public void testSendKeysToField() throws IOException {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		page.sendKeysToField("textField", "foo");
		verify(element).sendKeys("foo");
	}

	@Test(groups = { "ut" })
	public void testSendRandomKeysToField() throws IOException {
		ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

		when(driver.findElement(By.id("text"))).thenReturn(element);
		page.sendRandomKeysToField(12, "textField");
		verify(element).sendKeys(stringCaptor.capture());
		Assert.assertEquals(stringCaptor.getValue().length(), 12);
	}

	@Test(groups = { "ut" })
	public void testClearHtmlElement() throws IOException {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		page.clear("textField");
		verify(element).clear();
	}

	/**
	 * Only HtmlElement is supported
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testClearPictureElement() throws IOException {
		page.clear("screenZone");
	}

	@Test(groups = { "ut" })
	public void testSelectOption() throws IOException {
		when(driver.findElement(By.id("select"))).thenReturn(element);
		when(element.getTagName()).thenReturn("select");
		when(element.isDisplayed()).thenReturn(true);
		when(element.findElements(By.tagName("option"))).thenReturn(Arrays.asList(option1));
		when(element.findElements(By.xpath(".//option[normalize-space(.) = \"foo\"]")))
				.thenReturn(Arrays.asList(option1));
		page.selectOption("select", "foo");
		verify(option1).click();
	}

	/**
	 * Only SelectList is supported
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testSelectOptionNoSelectList() throws IOException {
		page.selectOption("textField", "foo");
	}

	@Test(groups = { "ut" })
	public void testClickTo() throws IOException {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		PageObject nextPage = page.clickAndChangeToPage("textField", PageForActions.class);
		verify(element).click();
		Assert.assertNotEquals(page, nextPage);
	}

	@Test(groups = { "ut" })
	public void testChangePage() throws IOException {
		PageObject nextPage = page.changeToPage(PageForActions.class);
		Assert.assertNotEquals(page, nextPage);
	}

	@Test(groups = { "ut" })
	public void testClick() throws IOException {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		PageObject nextPage = page.click("textField");
		verify(element).click();
		Assert.assertEquals(page, nextPage);
	}

	@Test(groups = { "ut" })
	public void testDoubleClick() throws IOException {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		page.doubleClick("textField");
		verify((Interactive) eventDriver).perform(any());
	}

	@Test(groups = { "ut" })
	public void testDoubleClickScreenZone() throws IOException {
		page.doubleClick("screenZone");
		verify(PageForActions.screenZone).doubleClick();
	}

	@Test(groups = { "ut" })
	public void testClickTableCell() throws IOException {
		when(driver.findElement(By.id("table"))).thenReturn(element);
		when(element.findElements(By.tagName("tr"))).thenReturn(Arrays.asList(row1));
		when(row1.findElements(By.xpath(".//descendant::*[name()=\"th\" or name()=\"td\"]")))
				.thenReturn(Arrays.asList(column1));

		page.clickTableCell(0, 0, "table");
		verify(column1).click();
	}

	/**
	 * Only Table is supported
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testClickTableCellNoTableElement() throws IOException {
		page.clickTableCell(0, 0, "textField");
	}

	/**
	 * SwitchToNewWindow with a new window available
	 */
	@Test(groups = { "ut" })
	public void testSwitchToNewWindow() {
		when(eventDriver.getWindowHandles()).thenReturn(new HashSet<>(Arrays.asList("123")))
				.thenReturn(new HashSet<>(Arrays.asList("123", "456")));
		eventDriver.getCurrentHandles();
		when(eventDriver.getWindowHandle()).thenReturn("123").thenReturn("456");
		page.switchToNewWindow(1);
		verify(targetLocator).window("456");
	}

	/**
	 * SwitchToNewWindow without a new window available
	 */
	@Test(groups = { "ut" }, expectedExceptions = CustomSeleniumTestsException.class)
	public void testSwitchToNewWindowNoNewWindow() {
		when(eventDriver.getWindowHandles()).thenReturn(new HashSet<>(Arrays.asList("123")))
				.thenReturn(new HashSet<>(Arrays.asList("123")));
		eventDriver.getCurrentHandles();
		when(eventDriver.getWindowHandle()).thenReturn("123");
		page.switchToNewWindow(1);
	}

	/**
	 * SwitchToNewWindow without a new window available, no wait
	 */
	@Test(groups = { "ut" })
	public void testSwitchToNewWindowNoNewWindowNoWait() {
		when(eventDriver.getWindowHandles()).thenReturn(new HashSet<>(Arrays.asList("123")))
				.thenReturn(new HashSet<>(Arrays.asList("123")));
		eventDriver.getCurrentHandles();
		when(eventDriver.getWindowHandle()).thenReturn("123");
		page.switchToNewWindow(0);
	}

	/**
	 * SwitchToNewWindow with a new window available
	 */
	@Test(groups = { "ut" })
	public void testSwitchToMainWindow() {
		when(eventDriver.getWindowHandles()).thenReturn(new HashSet<>(Arrays.asList("123")))
				.thenReturn(new HashSet<>(Arrays.asList("123", "456")));
		eventDriver.getCurrentHandles();
		when(eventDriver.getWindowHandle()).thenReturn("456");
		page.switchToMainWindow();
		verify(targetLocator).window("123");
	}

	/**
	 * SwitchToWindow with index
	 */
	@Test(groups = { "ut" })
	public void testSwitchToNthWindow() {
		when(eventDriver.getWindowHandles()).thenReturn(new HashSet<>(Arrays.asList("123")))
				.thenReturn(new HashSet<>(Arrays.asList("123", "456")));
		eventDriver.getCurrentHandles();
		when(eventDriver.getWindowHandle()).thenReturn("123");
		page.switchToWindow(1);
		verify(targetLocator).window("456");
	}

	@Test(groups = { "ut" })
	public void testRefresh() throws IOException {
		page.refresh();
		verify(navigation).refresh();
	}

	@Test(groups = { "ut" })
	public void testWaitForPresent() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		page.waitForPresent("textField");
		verify(driver).findElement(By.id("text"));
	}

	@Test(groups = { "ut" }, expectedExceptions = TimeoutException.class)
	public void testWaitForPresentNotPresent() {
		when(driver.findElement(By.id("text"))).thenThrow(new NoSuchElementException("not found"));
		page.waitForPresent("textField");
	}

	@Test(groups = { "ut" })
	public void testWaitForPresentScreenZone() {
		page.waitForPresent("screenZone");
		verify(PageForActions.screenZone).isElementPresent(1000);
	}

	@Test(groups = { "ut" })
	public void testWaitForVisible() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.isDisplayed()).thenReturn(true);
		page.waitForVisible("textField");
		verify(driver).findElement(By.id("text"));
	}

	@Test(groups = { "ut" }, expectedExceptions = TimeoutException.class)
	public void testWaitForVisibleNotVisible() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.isDisplayed()).thenReturn(false);
		page.waitForVisible("textField");
		verify(driver).findElement(By.id("text"));
	}

	@Test(groups = { "ut" })
	public void testWaitForVisibleScreenZone() {
		page.waitForPresent("screenZone");
		verify(PageForActions.screenZone, atLeastOnce()).isElementPresent(1000);
	}

	@Test(groups = { "ut" })
	public void testWaitForNotPresent() {
		when(driver.findElement(By.id("text"))).thenThrow(new NoSuchElementException("not found"));
		page.waitForNotPresent("textField");
	}

	@Test(groups = { "ut" }, expectedExceptions = TimeoutException.class)
	public void testWaitForNotPresentPresent() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		page.waitForNotPresent("textField");
		verify(driver).findElement(By.id("text"));
	}

	@Test(groups = { "ut" })
	public void testWaitForNotVisible() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.isDisplayed()).thenReturn(false);
		page.waitForInvisible("textField");
		verify(driver).findElement(By.id("text"));
	}

	@Test(groups = { "ut" }, expectedExceptions = TimeoutException.class)
	public void testWaitForNotVisibleVisible() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.isDisplayed()).thenReturn(true);
		page.waitForInvisible("textField");
		verify(driver).findElement(By.id("text"));
	}

	@Test(groups = { "ut" })
	public void testWaitForValueViaValueAttribute() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.getDomProperty("value")).thenReturn("foo");
		page.waitForValue("textField", "foo");
	}

	@Test(groups = { "ut" })
	public void testWaitForValueViaText() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.getText()).thenReturn("foo");
		page.waitForValue("textField", "foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = TimeoutException.class)
	public void testWaitForWrongValue() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.getText()).thenReturn("bar");
		when(element.getDomProperty("value")).thenReturn("bar2");
		page.waitForValue("textField", "foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testWaitForValueScreenZone() {
		page.waitForValue("screenZone", "foo");
	}

	@Test(groups = { "ut" })
	public void testWaitForTableCellValue() throws IOException {
		when(driver.findElement(By.id("table"))).thenReturn(element);
		when(element.findElements(By.tagName("tr"))).thenReturn(Arrays.asList(row1));
		when(row1.findElements(By.xpath(".//descendant::*[name()=\"th\" or name()=\"td\"]")))
				.thenReturn(Arrays.asList(column1));
		when(column1.getText()).thenReturn("foo");

		page.waitTableCellValue(0, 0, "table", "foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = TimeoutException.class)
	public void testWaitForTableCellWrongValue() throws IOException {
		when(driver.findElement(By.id("table"))).thenReturn(element);
		when(element.findElements(By.tagName("tr"))).thenReturn(Arrays.asList(row1));
		when(row1.findElements(By.xpath(".//descendant::*[name()=\"th\" or name()=\"td\"]")))
				.thenReturn(Arrays.asList(column1));
		when(column1.getText()).thenReturn("foo");

		page.waitTableCellValue(0, 0, "table", "bar");
	}

	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testWaitForTableCellValueScreenZone() {
		page.waitTableCellValue(0, 0, "screenZone", "bar");
	}

	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testWaitForTableCellValueNoTable() {
		page.waitTableCellValue(0, 0, "textField", "bar");
	}

	@Test(groups = { "ut" })
	public void testAssertVisible() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.isDisplayed()).thenReturn(true);
		page.assertForVisible("textField");
		verify(driver, atLeastOnce()).findElement(By.id("text"));
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertVisibleNotPresent() {
		when(driver.findElement(By.id("text"))).thenThrow(new NoSuchElementException("not found"));
		when(element.isDisplayed()).thenReturn(true);
		page.assertForVisible("textField");
		verify(driver).findElement(By.id("text"));
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertVisibleNotVisible() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.isDisplayed()).thenReturn(false);
		page.assertForVisible("textField");
		verify(driver).findElement(By.id("text"));
	}

	@Test(groups = { "ut" })
	public void testAssertVisibleScreenZone() {
		page.assertForVisible("screenZone");
		verify(PageForActions.screenZone).isElementPresent();
	}

	@Test(groups = { "ut" })
	public void testAssertNotVisible() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.isDisplayed()).thenReturn(false);
		page.assertForInvisible("textField");
		verify(driver, atLeastOnce()).findElement(By.id("text"));
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertNotVisibleNotPresent() {
		when(driver.findElement(By.id("text"))).thenThrow(new NoSuchElementException("not found"));
		when(element.isDisplayed()).thenReturn(false);
		page.assertForInvisible("textField");
		verify(driver).findElement(By.id("text"));
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertNotVisibleVisible() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.isDisplayed()).thenReturn(true);
		page.assertForInvisible("textField");
		verify(driver).findElement(By.id("text"));
	}

	@Test(groups = { "ut" })
	public void testAssertEnabled() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.isEnabled()).thenReturn(true);
		page.assertForEnabled("textField");
		verify(driver, atLeastOnce()).findElement(By.id("text"));
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertEnabledNotEnabled() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.isEnabled()).thenReturn(false);
		page.assertForEnabled("textField");
		verify(driver).findElement(By.id("text"));
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertEnabledNotPresent() {
		when(driver.findElement(By.id("text"))).thenThrow(new NoSuchElementException("not found"));
		when(element.isEnabled()).thenReturn(false);
		page.assertForEnabled("textField");
		verify(driver).findElement(By.id("text"));
	}

	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testAssertEnabledScreenZone() {
		page.assertForEnabled("screenZone");
		verify(PageForActions.screenZone).isElementPresent();
	}

	@Test(groups = { "ut" })
	public void testAssertDisabled() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.isEnabled()).thenReturn(true);
		page.assertForEnabled("textField");

		// check element is searched
		verify(driver, atLeastOnce()).findElement(By.id("text"));
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertDisabledNotPresent() {
		when(driver.findElement(By.id("text"))).thenThrow(new NoSuchElementException("not found"));
		when(element.isEnabled()).thenReturn(true);
		page.assertForEnabled("textField");

		// check element is searched
		verify(driver).findElement(By.id("text"));
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertDisabledNotDisabled() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.isEnabled()).thenReturn(false);
		page.assertForEnabled("textField");
		verify(driver).findElement(By.id("text"));
	}

	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testAssertDisabledScreenZone() {
		page.assertForEnabled("screenZone");
		verify(PageForActions.screenZone).isElementPresent();
	}

	@Test(groups = { "ut" })
	public void testAssertForValueViaValueAttribute() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.getText()).thenReturn("");
		when(element.getDomProperty("value")).thenReturn("foo");
		page.assertForValue("textField", "foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertForValueViaValueAttributeNotPresent() {
		when(driver.findElement(By.id("text"))).thenThrow(new NoSuchElementException("not found"));
		when(element.getText()).thenReturn("");
		when(element.getDomProperty("value")).thenReturn("foo");
		page.assertForValue("textField", "foo");
	}

	@Test(groups = { "ut" })
	public void testAssertForValueViaText() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.getText()).thenReturn("foo");
		when(element.getDomProperty("value")).thenReturn("");
		page.assertForValue("textField", "foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertForWrongValue() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.getText()).thenReturn("bar");
		when(element.getDomProperty("value")).thenReturn("bar2");
		page.assertForValue("textField", "foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testAssertForValueScreenZone() {
		page.assertForValue("screenZone", "foo");
	}

	@Test(groups = { "ut" })
	public void testAssertForEmptyValueViaValueAttribute() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.getDomProperty("value")).thenReturn("");
		page.assertForEmptyValue("textField");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertForEmptyValueViaValueAttributeNotPresent() {
		when(driver.findElement(By.id("text"))).thenThrow(new NoSuchElementException("not found"));
		when(element.getDomProperty("value")).thenReturn("");
		page.assertForEmptyValue("textField");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertForEmptyValueViaValueAttributeNotEmpty() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.getDomProperty("value")).thenReturn("foo");
		page.assertForEmptyValue("textField");
	}

	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testAssertForEmptyValueScreenZone() {
		page.assertForEmptyValue("screenZone");
	}

	@Test(groups = { "ut" })
	public void testAssertForNonEmptyValueViaValueAttribute() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.getDomProperty("value")).thenReturn("bar");
		page.assertForNonEmptyValue("textField");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertForNonEmptyValueViaValueAttributeNotPresent() {
		when(driver.findElement(By.id("text"))).thenThrow(new NoSuchElementException("not found"));
		when(element.getDomProperty("value")).thenReturn("bar");
		page.assertForNonEmptyValue("textField");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertForNonEmptyValueViaValueAttributeNotEmpty() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.getDomProperty("value")).thenReturn("");
		page.assertForNonEmptyValue("textField");
	}

	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testAssertForNonEmptyValueScreenZone() {
		page.assertForNonEmptyValue("screenZone");
	}

	@Test(groups = { "ut" })
	public void testAssertForMatchingValueViaValueAttribute() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.getText()).thenReturn("");
		when(element.getDomProperty("value")).thenReturn("barfoobar");
		page.assertForMatchingValue("textField", "foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertForMatchingValueViaValueAttributeNotPresent() {
		when(driver.findElement(By.id("text"))).thenThrow(new NoSuchElementException("not found"));
		when(element.getText()).thenReturn("");
		when(element.getDomProperty("value")).thenReturn("barfoobar");
		page.assertForMatchingValue("textField", "foo");
	}

	@Test(groups = { "ut" })
	public void testAssertForMatchingValueViaText() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.getText()).thenReturn("barfoobar");
		when(element.getDomProperty("value")).thenReturn("");
		page.assertForMatchingValue("textField", "foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertForWrongMatchingValue() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.getText()).thenReturn("bar");
		when(element.getDomProperty("value")).thenReturn("bar2");
		page.assertForMatchingValue("textField", "foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testAssertForMatchingValueScreenZone() {
		page.assertForMatchingValue("screenZone", "foo");
	}

	@Test(groups = { "ut" })
	public void testAssertSelectedOption() throws IOException {
		when(driver.findElement(By.id("select"))).thenReturn(element);
		when(element.getTagName()).thenReturn("select");
		when(element.isDisplayed()).thenReturn(true);
		when(element.findElements(By.tagName("option"))).thenReturn(Arrays.asList(option1));
		when(element.findElements(By.xpath(".//option[normalize-space(.) = \"foo\"]")))
				.thenReturn(Arrays.asList(option1));
		when(option1.isSelected()).thenReturn(true);
		when(option1.getText()).thenReturn("foo");
		page.assertSelectedOption("select", "foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertSelectedOptionNotPresent() throws IOException {
		when(driver.findElement(By.id("select"))).thenThrow(new NoSuchElementException("not found"));

		page.assertSelectedOption("select", "foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertSelectedOptionNoOption() throws IOException {
		when(driver.findElement(By.id("select"))).thenReturn(element);
		when(element.getTagName()).thenReturn("select");
		when(element.isDisplayed()).thenReturn(true);
		when(element.findElements(By.tagName("option"))).thenReturn(new ArrayList<>());
		page.assertSelectedOption("select", "foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertSelectedOptionNotSelected() throws IOException {
		when(driver.findElement(By.id("select"))).thenReturn(element);
		when(element.getTagName()).thenReturn("select");
		when(element.isDisplayed()).thenReturn(true);
		when(element.findElements(By.tagName("option"))).thenReturn(Arrays.asList(option1));
		when(element.findElements(By.xpath(".//option[normalize-space(.) = \"foo\"]")))
				.thenReturn(Arrays.asList(option1));
		when(option1.isSelected()).thenReturn(false);
		when(option1.getText()).thenReturn("foo");
		page.assertSelectedOption("select", "foo");
	}

	/**
	 * Only SelectList is supported
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testAssertSelectedOptionNoSelect() throws IOException {
		page.assertSelectedOption("textField", "foo");
	}

	@Test(groups = { "ut" })
	public void testAssertChecked() {
		when(driver.findElement(By.id("checkbox"))).thenReturn(element);
		when(element.getTagName()).thenReturn("input");
		when(element.isSelected()).thenReturn(true);
		page.assertChecked("checkbox");
	}

	@Test(groups = { "ut" })
	public void testAssertRadioChecked() {
		when(driver.findElement(By.id("radio"))).thenReturn(element);
		when(element.getTagName()).thenReturn("input");
		when(element.isSelected()).thenReturn(true);
		page.assertChecked("radio");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertCheckedNotChecked() {
		when(driver.findElement(By.id("checkbox"))).thenReturn(element);
		when(element.getTagName()).thenReturn("input");
		when(element.isSelected()).thenReturn(false);
		page.assertChecked("checkbox");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertCheckedNotPresent() {
		when(driver.findElement(By.id("checkbox"))).thenThrow(new NoSuchElementException("not found"));
		page.assertChecked("checkbox");
	}

	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testAssertCheckedNotcheckbox() throws IOException {
		page.assertChecked("textField");
	}

	@Test(groups = { "ut" })
	public void testAssertNotChecked() {
		when(driver.findElement(By.id("checkbox"))).thenReturn(element);
		when(element.getTagName()).thenReturn("input");
		when(element.isSelected()).thenReturn(false);
		page.assertNotChecked("checkbox");
	}

	@Test(groups = { "ut" })
	public void testAssertRadioNotChecked() {
		when(driver.findElement(By.id("radio"))).thenReturn(element);
		when(element.getTagName()).thenReturn("input");
		when(element.isSelected()).thenReturn(false);
		page.assertNotChecked("radio");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertNotCheckedChecked() {
		when(driver.findElement(By.id("checkbox"))).thenReturn(element);
		when(element.getTagName()).thenReturn("input");
		when(element.isSelected()).thenReturn(true);
		page.assertNotChecked("checkbox");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertNotCheckedNotPresent() {
		when(driver.findElement(By.id("checkbox"))).thenThrow(new NoSuchElementException("not found"));
		page.assertNotChecked("checkbox");
	}

	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testAssertNotCheckedNotcheckbox() throws IOException {
		page.assertNotChecked("textField");
	}

	@Test(groups = { "ut" })
	public void testAssertForTableCellValue() throws IOException {
		when(driver.findElement(By.id("table"))).thenReturn(element);
		when(element.findElements(By.tagName("tr"))).thenReturn(Arrays.asList(row1));
		when(row1.findElements(By.xpath(".//descendant::*[name()=\"th\" or name()=\"td\"]")))
				.thenReturn(Arrays.asList(column1));
		when(column1.getText()).thenReturn("foo");

		page.assertTableCellValue(0, 0, "table", "foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertForTableCellValueNotPresent() throws IOException {
		when(driver.findElement(By.id("table"))).thenThrow(new NoSuchElementException("not found"));

		page.assertTableCellValue(0, 0, "table", "foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertForTableCellWrongValue() throws IOException {
		when(driver.findElement(By.id("table"))).thenReturn(element);
		when(element.findElements(By.tagName("tr"))).thenReturn(Arrays.asList(row1));
		when(row1.findElements(By.xpath(".//descendant::*[name()=\"th\" or name()=\"td\"]")))
				.thenReturn(Arrays.asList(column1));
		when(column1.getText()).thenReturn("bar");

		page.assertTableCellValue(0, 0, "table", "foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testAssertForTableCellNotTable() throws IOException {
		page.assertTableCellValue(0, 0, "textField", "foo");
	}

	@Test(groups = { "ut" })
	public void testAssertTextPresentInPage() {
		when(driver.findElement(By.tagName("body"))).thenReturn(element);
		when(element.getText()).thenReturn("<html>foo</html>");
		page.assertTextPresentInPage("foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertTextPresentNotInPage() {
		when(driver.findElement(By.tagName("body"))).thenReturn(element);
		when(element.getText()).thenReturn("<html>bar</html>");
		page.assertTextPresentInPage("foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertTextPresentInPageNoBody() {
		when(driver.findElement(By.tagName("body"))).thenThrow(new NoSuchElementException("not found"));
		page.assertTextPresentInPage("foo");
	}

	@Test(groups = { "ut" })
	public void testAssertTextNotPresentInPage() {
		when(driver.findElement(By.tagName("body"))).thenReturn(element);
		when(element.getText()).thenReturn("<html>bar</html>");
		page.assertTextNotPresentInPage("foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertTextNotPresentNotInPage() {
		when(driver.findElement(By.tagName("body"))).thenReturn(element);
		when(element.getText()).thenReturn("<html>foo</html>");
		page.assertTextNotPresentInPage("foo");
	}

	@Test(groups = { "ut" })
	public void testAssertTextNotPresentInPageNoBody() {
		when(driver.findElement(By.tagName("body"))).thenThrow(new NoSuchElementException("not found"));
		page.assertTextNotPresentInPage("foo");
	}

	@Test(groups = { "ut" })
	public void testAssertCookiePresent() {
		when(driverOptions.getCookieNamed("foo")).thenReturn(new Cookie("foo", "barfoobar"));
		page.assertCookiePresent("foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertCookieNotPresent() {
		when(driverOptions.getCookieNamed("foo")).thenReturn(null);
		page.assertCookiePresent("foo");
	}

	@Test(groups = { "ut" })
	public void testAssertElementCount() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(driver.findElements(By.id("text"))).thenReturn(Arrays.asList(element));
		page.assertElementCount("textField", 1);
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertElementWrongCount() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(driver.findElements(By.id("text"))).thenReturn(Arrays.asList(element));
		page.assertElementCount("textField", 2);
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertElementCountNotPresent() {
		when(driver.findElement(By.id("text"))).thenThrow(new NoSuchElementException("not found"));
		when(driver.findElements(By.id("text"))).thenReturn(Arrays.asList());
		page.assertElementCount("textField", 1);
	}

	@Test(groups = { "ut" })
	public void testAssertTextPresentInHtml() {
		when(driver.getPageSource()).thenReturn("<html>foo</html>");
		page.assertHtmlSource("foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = AssertionError.class)
	public void testAssertTextPresentNotInHtml() {
		when(driver.getPageSource()).thenReturn("<html>bar</html>");
		page.assertHtmlSource("foo");
	}
	
	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = ".*AngularMaterial.*")
	public void testWithInvalidUiLibrary() {
		new PageForActions(Arrays.asList("invalid"));
	}
	
	@Test(groups = { "ut" })
	public void testWithValidUiLibrary() {
		new PageForActions(Arrays.asList("AngularMaterial"));
		Assert.assertTrue(PageForActions.getUiLibraries(PageForActions.class.getCanonicalName()).contains("AngularMaterial"));
		Assert.assertFalse(PageForActions.getUiLibraries(PageObject.class.getCanonicalName()).contains("AngularMaterial"));
	}
	
	@Test(groups = { "ut" })
	public void testWithEmptyUiLibrary() {
		new PageForActions();
		Assert.assertTrue(PageForActions.getUiLibraries(PageForActions.class.getCanonicalName()).isEmpty());
	}
	
	/**
	 * No error should be raised
	 */
	@Test(groups = { "ut" })
	public void testUiLibraryWithoutPage() {
		Assert.assertTrue(PageForActions.getUiLibraries(PageForActions.class.getCanonicalName()).isEmpty());
	}
	
	@Test(groups = { "ut" })
	public void testCallingPageSetToPictureElement() {
		PageForActions p1 = new PageForActions();
		Assert.assertNotNull(p1.getPicture().getCallingPage());
		Assert.assertNotNull(p1.getScreenZone().getCallingPage());
		Assert.assertNull(p1.getTextField().getCallingPage());
	}
	
	@Test(groups = { "ut" })
	public void testCallingPageDifferentPages() {
		PageForActions p1 = new PageForActions();
		PageObject p1e = p1.getPicture().getCallingPage();
		PageForActions p2 = new PageForActions();
		PageObject p2e = p1.getPicture().getCallingPage();
		
		// check that a single page of same type is kept for eache thread
		Assert.assertEquals(p1.getPicture().getCallingPage(), p2.getPicture().getCallingPage());
		
		// check that callingPage evolves when a new instance of the same page is created
		Assert.assertNotEquals(p1e,  p2e);
	}
	
	/**
	 * Check that each thread records a different instance of page
	 */
	@Test(groups = { "ut" })
	public void testCallingPageSetForEachThread() {
		PageLauncher p1 = new PageLauncher();
		p1.start();
		PageLauncher p2 = new PageLauncher();
		p2.start();
		while (p1.otherPage == null || p1.otherPage == null) {
			WaitHelper.waitForMilliSeconds(200);
		}
		
		// check that for each thread, a different page is recorded
		Assert.assertNotEquals(p1.getPictureElementPage(), p2.getPictureElementPage());
	}

	private class PageLauncher extends Thread {
		
		public PageForActions otherPage;
		public PageObject callingPage;
		
		public void run() {
			SeleniumTestsContextManager.setThreadContext(SeleniumTestsContextManager.getGlobalContext());
			SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
			otherPage = new PageForActions();
			callingPage = otherPage.getPicture().getCallingPage();
		}
		
		public PageObject getPictureElementPage() {
			return callingPage;
		}
	}
	
	
}
