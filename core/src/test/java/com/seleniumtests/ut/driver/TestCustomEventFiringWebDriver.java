package com.seleniumtests.ut.driver;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

import java.io.IOException;
import java.util.Arrays;

import org.mockito.Mock;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.DriverExceptionListener;
import com.seleniumtests.driver.DriverMode;

public class TestCustomEventFiringWebDriver extends MockitoTest {


	@Mock
	private RemoteWebDriver driver;
	
	@Mock
	private Options options;
	
	@Mock
	private Window window;
	
	private EventFiringWebDriver eventDriver;

	@BeforeMethod(groups={"ut"})
	private void init() throws WebDriverException, IOException {
		
		// add DriverExceptionListener to reproduce driver behavior
		eventDriver = spy(new CustomEventFiringWebDriver(driver).register(new DriverExceptionListener()));
		
		when(driver.manage()).thenReturn(options);
		when(options.window()).thenReturn(window);
		when(window.getSize()).thenReturn(new Dimension(100, 100));
	}
	
	/**
	 * check that if an error occurs during javascript invocation, window size is returned (issue #161)
	 */
	@Test(groups = {"ut"})
	public void testNullJavascriptReplyForContentDimension() {
		Dimension dim = ((CustomEventFiringWebDriver)eventDriver).getContentDimension();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 100);
		Assert.assertEquals(dim.width, 100);
	}
	
	/**
	 * Check standard web case where dimension comes from javascript call
	 */
	@Test(groups = {"ut"})
	public void testContentDimension() {
		when(driver.executeScript(anyString())).thenReturn(Arrays.asList(120L, 80L));
		Dimension dim = ((CustomEventFiringWebDriver)eventDriver).getContentDimension();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 80);
		Assert.assertEquals(dim.width, 120);
	}
	
	/**
	 * issue #165: Check case where browser is not at 100% zoom and so, double reply is returned
	 */
	@Test(groups = {"ut"})
	public void testContentDimensionWithZoomFactor() {
		when(driver.executeScript(anyString())).thenReturn(Arrays.asList(120.5, 80.67));
		Dimension dim = ((CustomEventFiringWebDriver)eventDriver).getContentDimension();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 80);
		Assert.assertEquals(dim.width, 120);
	}
	
	/**
	 * For non web test, dimension is returned from driver call, not javascript
	 */
	@Test(groups = {"ut"})
	public void testContentDimensionNonWebTest() {
		eventDriver = spy(new CustomEventFiringWebDriver(driver, null, null, false, DriverMode.LOCAL, null, null).register(new DriverExceptionListener()));
		when(driver.executeScript(anyString())).thenReturn(Arrays.asList(120L, 80L));
		Dimension dim = ((CustomEventFiringWebDriver)eventDriver).getContentDimension();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 100);
		Assert.assertEquals(dim.width, 100);
	}
	
	/**
	 * check that if an error occurs during javascript invocation, window size is returned (issue #161)
	 */
	@Test(groups = {"ut"})
	public void testNullJavascriptReplyForContentDimensionWithoutScrollbar() {
		Dimension dim = ((CustomEventFiringWebDriver)eventDriver).getViewPortDimensionWithoutScrollbar();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 100);
		Assert.assertEquals(dim.width, 100);
	}
	
	/**
	 * Check standard web case where dimension comes from javascript call
	 */
	@Test(groups = {"ut"})
	public void testViewPortDimensionWithoutScrollbar() {
		when(driver.executeScript(anyString())).thenReturn(Arrays.asList(120L, 80L));
		Dimension dim = ((CustomEventFiringWebDriver)eventDriver).getViewPortDimensionWithoutScrollbar();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 80);
		Assert.assertEquals(dim.width, 120);
	}

	/**
	 * issue #165: Check case where browser is not at 100% zoom and so, double reply is returned
	 */
	@Test(groups = {"ut"})
	public void testViewPortDimensionWithoutScrollbarWithZoomFactor() {
		when(driver.executeScript(anyString())).thenReturn(Arrays.asList(120.5, 80.67));
		Dimension dim = ((CustomEventFiringWebDriver)eventDriver).getViewPortDimensionWithoutScrollbar();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 80);
		Assert.assertEquals(dim.width, 120);
	}

	/**
	 * For non web test, dimension is returned from driver call, not javascript
	 */
	@Test(groups = {"ut"})
	public void testContentDimensionWithoutScrollbarNonWebTest() {
		eventDriver = spy(new CustomEventFiringWebDriver(driver, null, null, false, DriverMode.LOCAL, null, null).register(new DriverExceptionListener()));
		when(driver.executeScript(anyString())).thenReturn(Arrays.asList(120L, 80L));
		Dimension dim = ((CustomEventFiringWebDriver)eventDriver).getViewPortDimensionWithoutScrollbar();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 100);
		Assert.assertEquals(dim.width, 100);
	}
	
	/**
	 * check that if an error occurs during javascript invocation, window size is returned (issue #161)
	 */
	@Test(groups = {"ut"})
	public void testNullJavascriptReplyForScrollPosition() {
		Point point = ((CustomEventFiringWebDriver)eventDriver).getScrollPosition();
		
		// check we get the default position: (0,0)
		Assert.assertEquals(point.x, 0);
		Assert.assertEquals(point.y, 0);
	}
	
	/**
	 * Check standard web case where dimension comes from javascript call
	 */
	@Test(groups = {"ut"})
	public void testScrollPosition() {
		when(driver.executeScript(anyString())).thenReturn(Arrays.asList(120L, 80L));
		Point point = ((CustomEventFiringWebDriver)eventDriver).getScrollPosition();
		
		// check we get the scroll position
		Assert.assertEquals(point.x, 120);
		Assert.assertEquals(point.y, 80);
	}

	/**
	 * issue #165: Check case where browser is not at 100% zoom and so, double reply is returned
	 */
	@Test(groups = {"ut"})
	public void testScrollPositionWithZoomFactor() {
		when(driver.executeScript(anyString())).thenReturn(Arrays.asList(120.5, 80.67));
		Point point = ((CustomEventFiringWebDriver)eventDriver).getScrollPosition();
		
		// check we get the window dimension
		Assert.assertEquals(point.x, 120);
		Assert.assertEquals(point.y, 80);
	}
	
	/**
	 * For non web test, dimension is returned from driver call, not javascript
	 */
	@Test(groups = {"ut"}, expectedExceptions=WebDriverException.class)
	public void testScrollPositionNonWebTest() {
		eventDriver = spy(new CustomEventFiringWebDriver(driver, null, null, false, DriverMode.LOCAL, null, null).register(new DriverExceptionListener()));
		when(driver.executeScript(anyString())).thenReturn(Arrays.asList(120L, 80L));
		((CustomEventFiringWebDriver)eventDriver).getScrollPosition();

	}
	
}
