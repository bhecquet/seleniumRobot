package com.seleniumtests.ut.uipage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebDriver.TargetLocator;
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

@PrepareForTest({WebUIDriver.class})
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
	private Alert alert;

	private CustomEventFiringWebDriver eventDriver;
	private PageObject page;

	@BeforeMethod(groups={"ut"})
	private void init() throws IOException {

		SeleniumTestsContextManager.getGlobalContext().setCucumberImplementationPackage("com.seleniumtests.ut.core.runner.cucumber");
		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setImplicitWaitTimeout(1);
		
		eventDriver = spy(new CustomEventFiringWebDriver(driver));
		

		when(eventDriver.switchTo()).thenReturn(target);
		when(driver.findElement(By.id("el"))).thenReturn(element);
		when(driver.navigate()).thenReturn(navigation);
		when(driver.getCurrentUrl()).thenReturn("http://foo");
		when(element.getSize()).thenReturn(new Dimension(10, 10));
		when(element.getLocation()).thenReturn(new Point(5, 5));

		when(targetLocator.alert()).thenReturn(alert);
		when(alert.getText()).thenReturn("alert text");
		when(driver.switchTo()).thenReturn(targetLocator);
		
		PowerMockito.mockStatic(WebUIDriver.class);
		when(WebUIDriver.getCurrentWebUiDriverName()).thenReturn("main");
		when(WebUIDriver.getWebDriver(anyBoolean(), eq(BrowserType.FIREFOX), eq("main"), isNull())).thenReturn(eventDriver);
		when(WebUIDriver.getWebDriver(anyBoolean())).thenReturn(eventDriver);
		when(eventDriver.executeScript("if (document.readyState === \"complete\") { return \"ok\"; }")).thenReturn("ok");
		when(eventDriver.getBrowserInfo()).thenReturn(new BrowserInfo(BrowserType.FIREFOX, "78.0"));
		when(screenshotUtil.capture(any(SnapshotTarget.class), ArgumentMatchers.<Class<ScreenShot>>any())).thenReturn(screenshot);
		when(screenshot.getHtmlSourcePath()).thenReturn("foo");

		page = new PageForActions();
		
	}
	
	@Test(groups= {"ut"})
	public void testCapturePageSnapshotNoArguments() throws IOException {
		
		page.setScreenshotUtil(screenshotUtil);
		String htmlFilePath = page.getHtmlFilePath();
		page.capturePageSnapshot();
		
		// check capture has been done on the second call (a first capture is done at PageObject init)
		Assert.assertFalse(page.getHtmlFilePath().equals(htmlFilePath));
	}
	
	/**
	 * Capture page snapshot and sends it to selenium server
	 * @throws IOException
	 */
	@Test(groups= {"ut"})
	public void testCapturePageSnapshotWithCheck() throws IOException {
		
		page.setScreenshotUtil(screenshotUtil);
		String htmlFilePath = page.getHtmlFilePath();
		page.capturePageSnapshot("img", SnapshotCheckType.TRUE);
		
		// check capture has been done on the second call (a first capture is done at PageObject init)
		Assert.assertFalse(page.getHtmlFilePath().equals(htmlFilePath));
	}
	
	/**
	 * Capture page snapshot and sends it to selenium server. No name is provided
	 * ScenarioException should be raised
	 * @throws IOException
	 */
	@Test(groups= {"ut"}, expectedExceptions = ScenarioException.class)
	public void testCapturePageSnapshotWithCheckNoName() throws IOException {
		
		page.setScreenshotUtil(screenshotUtil);
		page.capturePageSnapshot("", SnapshotCheckType.TRUE);
		
	}
	
	/**
	 * Capture element snapshot and sends it to selenium server
	 * @throws IOException
	 */
	@Test(groups= {"ut"})
	public void testCaptureElementSnapshotWithCheck() throws IOException {
		
		page.setScreenshotUtil(screenshotUtil);
		String htmlFilePath = page.getHtmlFilePath();
		page.captureElementSnapshot("img", new HtmlElement("", By.id("el")), SnapshotCheckType.TRUE);
		
		// check capture has been done on the second call (a first capture is done at PageObject init)
		Assert.assertFalse(page.getHtmlFilePath().equals(htmlFilePath));
	}
	
	/**
	 * Capture element snapshot and sends it to selenium server. No name is provided
	 * ScenarioException should be raised
	 * @throws IOException
	 */
	@Test(groups= {"ut"}, expectedExceptions = ScenarioException.class)
	public void testCaptureElementSnapshotWithCheckNoName() throws IOException {
		
		page.setScreenshotUtil(screenshotUtil);
		page.captureElementSnapshot("", new HtmlElement("", By.id("el")), SnapshotCheckType.TRUE);
		
	}

	/**
	 * check we return to default content
	 * @throws IOException 
	 */
	@Test(groups={"ut"})
	public void testAcceptAlert() throws IOException {
		page.acceptAlert();
		Mockito.verify(alert).accept();
		Mockito.verify(targetLocator).defaultContent();
	}
	
	@Test(groups={"ut"})
	public void testDismissAlert() throws IOException {
		page.cancelConfirmation();
		Mockito.verify(alert, times(2)).dismiss();
		Mockito.verify(targetLocator).defaultContent();
	}
	
	
	@Test(groups={"ut"})
	public void testGoBack() throws IOException {
		page.goBack();
		verify(navigation).back();
	}
	
	@Test(groups={"ut"})
	public void testGoForward() throws IOException {
		page.goForward();
		verify(navigation).forward();
	}

	@Test(groups={"ut"})
	public void testSendKeysToField() throws IOException {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		page.sendKeysToField("textField", "foo");
		verify(element).sendKeys("foo");
	}
	
	@Test(groups={"ut"})
	public void testSendRandomKeysToField() throws IOException {
		ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		
		when(driver.findElement(By.id("text"))).thenReturn(element);
		page.sendRandomKeysToField(12, "textField");
		verify(element).sendKeys(stringCaptor.capture());
		Assert.assertEquals(stringCaptor.getValue().length(), 12);
	}

	@Test(groups={"ut"})
	public void testClearHtmlElement() throws IOException {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		page.clear("textField");
		verify(element).clear();
	}
	
	/**
	 * Only HtmlElement is supported
	 * @throws IOException
	 */
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testClearPictureElement() throws IOException {
		page.clear("screenZone");
	}

	@Test(groups={"ut"})
	public void testSelectOption() throws IOException {
		when(driver.findElement(By.id("select"))).thenReturn(element);
		when(element.getTagName()).thenReturn("select");
		when(element.isDisplayed()).thenReturn(true);
		when(element.findElements(By.tagName("option"))).thenReturn(Arrays.asList(option1));
		when(element.findElements(By.xpath(".//option[normalize-space(.) = \"foo\"]"))).thenReturn(Arrays.asList(option1));
		page.selectOption("select", "foo");
		verify(option1).click();
	}
	
	/**
	 * Only SelectList is supported
	 * @throws IOException
	 */
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testSelectOptionNoSelectList() throws IOException {
		page.selectOption("textField", "foo");
	}

	@Test(groups={"ut"})
	public void testClick() throws IOException {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		page.click("textField");
		verify(element).click();
	}
	
	@Test(groups={"ut"})
	public void testDoubleClick() throws IOException {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		page.doubleClick("textField");
		verify((Interactive)eventDriver).perform(any());
	}
	
	@Test(groups={"ut"})
	public void testDoubleClickScreenZone() throws IOException {
		page.doubleClick("screenZone");
		verify(PageForActions.screenZone).doubleClick();
	}
	

	@Test(groups={"ut"})
	public void testClickTableCell() throws IOException {
		when(driver.findElement(By.id("table"))).thenReturn(element);
		when(element.findElements(By.tagName("tr"))).thenReturn(Arrays.asList(row1));
		when(row1.findElements(By.xpath(".//descendant::*[name()=\"th\" or name()=\"td\"]"))).thenReturn(Arrays.asList(column1));
		
		page.clickTableCell(0, 0, "table");
		verify(column1).click();
	}

	/**
	 * Only Table is supported
	 * @throws IOException
	 */
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testClickTableCellNoTableElement() throws IOException {
		page.clickTableCell(0, 0, "textField");
	}
	
	/**
	 * SwitchToNewWindow with a new window available
	 */
	@Test(groups={"ut"})
	public void testSwitchToNewWindow() {
		when(eventDriver.getWindowHandles()).thenReturn(new HashSet<>(Arrays.asList("123"))).thenReturn(new HashSet<>(Arrays.asList("123", "456")));
		eventDriver.getCurrentHandles();
		when(eventDriver.getWindowHandle()).thenReturn("123");
		page.switchToNewWindow(1);
		verify(targetLocator).window("456");
	}
	
	/**
	 * SwitchToNewWindow without a new window available
	 */
	@Test(groups={"ut"}, expectedExceptions = CustomSeleniumTestsException.class)
	public void testSwitchToNewWindowNoNewWindow() {
		when(eventDriver.getWindowHandles()).thenReturn(new HashSet<>(Arrays.asList("123"))).thenReturn(new HashSet<>(Arrays.asList("123")));
		eventDriver.getCurrentHandles();
		when(eventDriver.getWindowHandle()).thenReturn("123");
		page.switchToNewWindow(1);
	}
	
	/**
	 * SwitchToNewWindow without a new window available, no wait
	 */
	@Test(groups={"ut"})
	public void testSwitchToNewWindowNoNewWindowNoWait() {
		when(eventDriver.getWindowHandles()).thenReturn(new HashSet<>(Arrays.asList("123"))).thenReturn(new HashSet<>(Arrays.asList("123")));
		eventDriver.getCurrentHandles();
		when(eventDriver.getWindowHandle()).thenReturn("123");
		page.switchToNewWindow(0);
	}
	
	/**
	 * SwitchToNewWindow with a new window available
	 */
	@Test(groups={"ut"})
	public void testSwitchToMainWindow() {
		when(eventDriver.getWindowHandles()).thenReturn(new HashSet<>(Arrays.asList("123"))).thenReturn(new HashSet<>(Arrays.asList("123", "456")));
		eventDriver.getCurrentHandles();
		when(eventDriver.getWindowHandle()).thenReturn("456");
		page.switchToMainWindow();
		verify(targetLocator).window("123");
	}
	
	/**
	 * SwitchToWindow with index
	 */
	@Test(groups={"ut"})
	public void testSwitchToNthWindow() {
		when(eventDriver.getWindowHandles()).thenReturn(new HashSet<>(Arrays.asList("123"))).thenReturn(new HashSet<>(Arrays.asList("123", "456")));
		eventDriver.getCurrentHandles();
		when(eventDriver.getWindowHandle()).thenReturn("123");
		page.switchToWindow(1);
		verify(targetLocator).window("456");
	}

	@Test(groups={"ut"})
	public void testRefresh() throws IOException {
		page.refresh();
		verify(navigation).refresh();
	}
	

	@Test(groups={"ut"})
	public void waitForPresent() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		page.waitForPresent("textField");
		verify(driver).findElement(By.id("text"));
	}
	
	@Test(groups={"ut"}, expectedExceptions = TimeoutException.class)
	public void waitForPresentNotPresent() {
		when(driver.findElement(By.id("text"))).thenThrow(new NoSuchElementException("not found"));
		page.waitForPresent("textField");
	}
	
	@Test(groups={"ut"})
	public void waitForPresentScreenZone() {
		page.waitForPresent("screenZone");
		verify(PageForActions.screenZone).isElementPresent(1000);
	}
	
	@Test(groups={"ut"})
	public void waitForVisible() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.isDisplayed()).thenReturn(true);
		page.waitForVisible("textField");
		verify(driver).findElement(By.id("text"));
	}
	
	@Test(groups={"ut"}, expectedExceptions = TimeoutException.class)
	public void waitForVisibleNotVisible() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.isDisplayed()).thenReturn(false);
		page.waitForVisible("textField");
		verify(driver).findElement(By.id("text"));
	}
	
	@Test(groups={"ut"})
	public void waitForVisibleScreenZone() {
		page.waitForPresent("screenZone");
		verify(PageForActions.screenZone).isElementPresent(1000);
	}

	@Test(groups={"ut"})
	public void waitForNotPresent() {
		when(driver.findElement(By.id("text"))).thenThrow(new NoSuchElementException("not found"));
		page.waitForNotPresent("textField");
	}
	
	@Test(groups={"ut"}, expectedExceptions = TimeoutException.class)
	public void waitForNotPresentPresent() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		page.waitForNotPresent("textField");
		verify(driver).findElement(By.id("text"));
	}

	@Test(groups={"ut"})
	public void waitForNotVisible() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.isDisplayed()).thenReturn(false);
		page.waitForInvisible("textField");
		verify(driver).findElement(By.id("text"));
	}
	
	@Test(groups={"ut"}, expectedExceptions = TimeoutException.class)
	public void waitForNotVisibleVisible() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.isDisplayed()).thenReturn(true);
		page.waitForInvisible("textField");
		verify(driver).findElement(By.id("text"));
	}
	
	@Test(groups={"ut"})
	public void waitForValueViaValueAttribute() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.getAttribute("value")).thenReturn("foo");
		page.waitForValue("textField", "foo");
	}
	
	@Test(groups={"ut"})
	public void waitForValueViaText() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.getText()).thenReturn("foo");
		page.waitForValue("textField", "foo");
	}
	
	@Test(groups={"ut"}, expectedExceptions = TimeoutException.class)
	public void waitForWrongValue() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		when(element.getText()).thenReturn("bar");
		when(element.getAttribute("value")).thenReturn("bar2");
		page.waitForValue("textField", "foo");
	}

	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void waitForValueScreenZone() {
		page.waitForValue("screenZone", "foo");
	}

	@Test(groups={"ut"})
	public void testWaitForTableCellValue() throws IOException {
		when(driver.findElement(By.id("table"))).thenReturn(element);
		when(element.findElements(By.tagName("tr"))).thenReturn(Arrays.asList(row1));
		when(row1.findElements(By.xpath(".//descendant::*[name()=\"th\" or name()=\"td\"]"))).thenReturn(Arrays.asList(column1));
		when(column1.getText()).thenReturn("foo");
		
		page.waitTableCellValue(0, 0, "table", "foo");
	}
	
	@Test(groups={"ut"}, expectedExceptions = TimeoutException.class)
	public void testWaitForTableCellWrongValue() throws IOException {
		when(driver.findElement(By.id("table"))).thenReturn(element);
		when(element.findElements(By.tagName("tr"))).thenReturn(Arrays.asList(row1));
		when(row1.findElements(By.xpath(".//descendant::*[name()=\"th\" or name()=\"td\"]"))).thenReturn(Arrays.asList(column1));
		when(column1.getText()).thenReturn("foo");
		
		page.waitTableCellValue(0, 0, "table", "bar");
	}

	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testWaitForTableCellValueScreenZone() {
		page.waitTableCellValue(0, 0, "screenZone", "bar");
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testWaitForTableCellValueNoTable() {
		page.waitTableCellValue(0, 0, "textField", "bar");
	}

	
}
