package com.seleniumtests.ut.uipage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.SnapshotTarget;
import com.seleniumtests.uipage.htmlelements.select.NativeSelect;
import com.seleniumtests.uipage.htmlelements.select.NgSelect;
import com.seleniumtests.ut.core.runner.cucumber.PageForActions;

//@PrepareForTest({ WebUIDriver.class })
public class TestSelectList2 extends MockitoTest {

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

	@BeforeMethod(groups = { "ut" })
	private void init() throws IOException {

		SeleniumTestsContextManager.getGlobalContext()
				.setCucumberImplementationPackage("com.seleniumtests.ut.core.runner.cucumber");
		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setImplicitWaitTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(2);

		// add capabilities to allow augmenting driver
		when(driver.getCapabilities()).thenReturn(new FirefoxOptions()); 
		eventDriver = spy(new CustomEventFiringWebDriver(driver));

		when(eventDriver.switchTo()).thenReturn(target);
		when(driver.findElement(By.id("select"))).thenReturn(element);
		when(driver.navigate()).thenReturn(navigation);
		when(driver.getCurrentUrl()).thenReturn("http://foo");
		when(driver.manage()).thenReturn(driverOptions);
		when(element.getTagName()).thenReturn("select");
		when(driverOptions.timeouts()).thenReturn(timeouts);
		when(element.getSize()).thenReturn(new Dimension(10, 10));
		when(element.getLocation()).thenReturn(new Point(5, 5));

		when(targetLocator.alert()).thenReturn(alert);
		when(alert.getText()).thenReturn("alert text");
		when(driver.switchTo()).thenReturn(targetLocator);

//		PowerMockito.mockStatic(WebUIDriver.class);
		when(WebUIDriver.getCurrentWebUiDriverName()).thenReturn("main");
		when(WebUIDriver.getWebDriver(anyBoolean(), eq(BrowserType.FIREFOX), eq("main"), isNull()))
				.thenReturn(eventDriver);
		when(WebUIDriver.getWebDriver(anyBoolean())).thenReturn(eventDriver);
		when(eventDriver.executeScript("if (document.readyState === \"complete\") { return \"ok\"; }"))
				.thenReturn("ok");
		when(eventDriver.getBrowserInfo()).thenReturn(new BrowserInfo(BrowserType.FIREFOX, "78.0"));
		when(screenshotUtil.capture(any(SnapshotTarget.class), ArgumentMatchers.<Class<ScreenShot>>any()))
				.thenReturn(screenshot);
		when(screenshot.getHtmlSourcePath()).thenReturn("foo");


	}
	

	/**
	 * Check the first implementation search is done depending on declaration in the page
	 */
	@Test(groups = { "ut" })
	public void testWithValidUiLibrary() {
		new PageForActions(Arrays.asList("Angular"));
		PageForActions.select.isMultiple();
		Assert.assertEquals(PageForActions.select.getImplementationList().get(0), NgSelect.class);
		new PageForActions(Arrays.asList("html"));
		PageForActions.select.isMultiple();
		Assert.assertEquals(PageForActions.select.getImplementationList().get(0), NativeSelect.class);
	}
}
