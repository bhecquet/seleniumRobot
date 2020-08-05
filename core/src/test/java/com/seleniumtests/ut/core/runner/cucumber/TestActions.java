package com.seleniumtests.ut.core.runner.cucumber;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.cucumber.Actions;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverExceptionListener;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.uipage.htmlelements.FrameElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.remote.AppiumCommandExecutor;

@PrepareForTest({ WebUIDriver.class, AppiumDriver.class, RemoteWebDriver.class })
public class TestActions extends MockitoTest {



	@Mock
	private RemoteWebDriver driver;
	@Mock
	private RemoteWebElement element;
	@Mock
	private RemoteWebElement subElement1;
	@Mock
	private RemoteWebElement subElement2;
	@Mock
	private RemoteWebElement frameElement;

	@Mock
	private Mouse mouse;

	@Mock
	private Keyboard keyboard;

	@Mock
	private TargetLocator locator;
	
	@Mock
	private Navigation navigation;

	@Mock
	private WebUIDriver uiDriver;

	@Mock
	private DriverConfig driverConfig;

	@Spy
	private HtmlElement el = new HtmlElement("element", By.id("el"));
	private HtmlElement el1 = new HtmlElement("element", By.id("el1"), el);
	
	@Spy
	private FrameElement frame = new FrameElement("frame", By.id("frame"));
	
	// issue #325

	private EventFiringWebDriver eventDriver;

	@BeforeMethod(groups = { "ut" })
	private void init() throws WebDriverException, IOException {
		SeleniumTestsContextManager.getGlobalContext().setCucumberImplementationPackage("com.seleniumtests.ut.core.runner.cucumber");
		
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
		when(WebUIDriver.getWebDriver(anyBoolean())).thenReturn(eventDriver);
		when(WebUIDriver.getWebDriver(anyBoolean(), any(BrowserType.class), isNull(), isNull())).thenReturn(eventDriver);
		when(WebUIDriver.getWebUIDriver(anyBoolean())).thenReturn(uiDriver);
		when(driver.navigate()).thenReturn(navigation);
//		when(driver.findElement(By.id("el"))).thenReturn(element);
//		when(driver.findElement(By.id("frame"))).thenReturn(frameElement);
//		when(driver.findElements(By.id("frame"))).thenReturn(Arrays.asList(frameElement));
//		when(driver.findElements(By.name("subEl"))).thenReturn(subElList);
//		when(driver.findElement(By.name("subEl"))).thenReturn(subElement1);
//		when(driver.findElements(By.id("el"))).thenReturn(elList);
//		when(driver.getKeyboard()).thenReturn(keyboard);
//		when(driver.getMouse()).thenReturn(mouse);
		when(driver.switchTo()).thenReturn(locator);
		when(eventDriver.switchTo()).thenReturn(locator);
//		when(driver.executeScript(anyString())).thenReturn(Arrays.asList(100, 100));
//
//		when(uiDriver.getConfig()).thenReturn(driverConfig);
//		when(driverConfig.getBrowserType()).thenReturn(BrowserType.HTMLUNIT);
//		when(driverConfig.getMajorBrowserVersion()).thenReturn(1);
//
//		when(element.findElement(By.name("subEl"))).thenReturn(subElement1);
//		when(element.findElements(By.name("subEl"))).thenReturn(subElList);
//		when(element.getAttribute(anyString())).thenReturn("attribute");
//		when(element.getSize()).thenReturn(new Dimension(10, 10));
//		when(element.getLocation()).thenReturn(new Point(5, 5));
//		when(frame.getLocation()).thenReturn(new Point(5, 5));
//		when(element.getTagName()).thenReturn("h1");
//		when(element.getText()).thenReturn("text");
//		when(element.isDisplayed()).thenReturn(true);
//		when(element.isEnabled()).thenReturn(true);
//
//		when(subElement1.isDisplayed()).thenReturn(true);
//		when(subElement2.isDisplayed()).thenReturn(true);
//		when(subElement1.getLocation()).thenReturn(new Point(5, 5));
//		when(subElement2.getLocation()).thenReturn(new Point(5, 5));

		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
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
