package com.seleniumtests.ut.uipage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.SnapshotTarget;

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
	private TargetLocator targetLocator;

	@Mock
	private Navigation navigation;

	@Mock
	private Options driverOptions;

	@Mock
	private Timeouts timeouts;

	@Mock
	private Alert alert;

    private MockedStatic<WebUIDriver> mockedWebUiDriver;

	@BeforeMethod(groups = { "ut" })
	private void init() {

		SeleniumTestsContextManager.getGlobalContext()
				.setCucumberImplementationPackage("com.seleniumtests.ut.core.runner.cucumber");
		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setImplicitWaitTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(2);

		// add capabilities to allow augmenting driver
		when(driver.getCapabilities()).thenReturn(new FirefoxOptions());
        CustomEventFiringWebDriver eventDriver = spy(new CustomEventFiringWebDriver(driver));

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

		mockedWebUiDriver = mockStatic(WebUIDriver.class);
		mockedWebUiDriver.when(WebUIDriver::getCurrentWebUiDriverName).thenReturn("main");
		mockedWebUiDriver.when(() -> WebUIDriver.getWebDriver(anyBoolean(), eq(BrowserType.FIREFOX), eq("main"), isNull()))
				.thenReturn(eventDriver);
		mockedWebUiDriver.when(() -> WebUIDriver.getWebDriver(anyBoolean())).thenReturn(eventDriver);
		when(eventDriver.executeScript("if (document.readyState === \"complete\") { return \"ok\"; }"))
				.thenReturn("ok");
		when(eventDriver.getBrowserInfo()).thenReturn(new BrowserInfo(BrowserType.FIREFOX, "78.0"));
		when(screenshotUtil.capture(any(SnapshotTarget.class), ArgumentMatchers.<Class<ScreenShot>>any()))
				.thenReturn(screenshot);
		when(screenshot.getHtmlSourcePath()).thenReturn("foo");

	}

	@AfterMethod(groups = {"ut"})
	public void closeMocks() {
		mockedWebUiDriver.close();
	}
}
