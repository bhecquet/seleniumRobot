package com.seleniumtests.ut.core.runner.cucumber;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import java.io.IOException;

import com.seleniumtests.uipage.htmlelements.HtmlElement;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.cucumber.Actions;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.uipage.htmlelements.FrameElement;

public class TestActions extends MockitoTest {



	@Mock
	private RemoteWebDriver driver;
	@Mock
	private RemoteWebElement element;

	@Mock
	private TargetLocator locator;
	
	@Mock
	private Navigation navigation;

	@Mock
	private WebUIDriver uiDriver;

	@Spy
	private HtmlElement el = new HtmlElement("element", By.id("el"));

	@Spy
	private FrameElement frame = new FrameElement("frame", By.id("frame"));
	
	// issue #325

	private CustomEventFiringWebDriver eventDriver;

	private MockedStatic mockedWebUIDriver;

	@BeforeMethod(groups = { "ut" })
	private void init() throws WebDriverException, IOException {

		SeleniumTestsContextManager.getGlobalContext().setCucumberImplementationPackage("com.seleniumtests.ut.core.runner.cucumber");

		when(driver.getCapabilities()).thenReturn(new DesiredCapabilities()); // add capabilities to allow augmenting driver
		
		// add DriverExceptionListener to reproduce driver behavior
		eventDriver = spy(new CustomEventFiringWebDriver(driver));

		mockedWebUIDriver = mockStatic(WebUIDriver.class);
		mockedWebUIDriver.when(() -> WebUIDriver.getWebDriver(anyBoolean())).thenReturn(eventDriver);
		mockedWebUIDriver.when(() -> WebUIDriver.getWebDriver(anyBoolean(), any(BrowserType.class), isNull(), isNull())).thenReturn(eventDriver);
		mockedWebUIDriver.when(() -> WebUIDriver.getWebUIDriver(anyBoolean())).thenReturn(uiDriver);
		when(driver.navigate()).thenReturn(navigation);
		when(driver.switchTo()).thenReturn(locator);
		when(eventDriver.switchTo()).thenReturn(locator);

		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
	}

	@AfterMethod(groups = { "ut" }, alwaysRun = true)
	private void closeMocks() {
		mockedWebUIDriver.close();
	}
	
	@Test(groups={"ut"})
	public void testOpenPage() throws IOException {
		new Actions().openPage("http://www.foo.com");
		verify(navigation).to("http://www.foo.com");
	}
	
	@Test(groups={"ut"})
	public void testSendKeys() {
		when(driver.findElement(By.id("text"))).thenReturn(element);
		new Actions().sendKeysToField("textField", "foo");
		verify(element).sendKeys("foo");
	}
	
}
