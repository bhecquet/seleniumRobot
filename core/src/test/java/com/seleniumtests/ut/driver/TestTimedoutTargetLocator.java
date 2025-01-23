package com.seleniumtests.ut.driver;

import com.seleniumtests.GenericTest;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.customexception.WebSessionEndedException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.TimedoutTargetLocator;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

import org.openqa.selenium.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestTimedoutTargetLocator extends MockitoTest {


    @Mock
    WebDriver.TargetLocator targetLocator;

    @Mock
    CustomEventFiringWebDriver driver;

    @Mock
    WebElement element;

    @Mock
    Alert alert;

    @Mock
    WindowType windowType;

    @Test(groups="ut")
    public void testFrameInTimeout() {
        when(targetLocator.frame(anyInt())).thenThrow(new TimeoutException());
        Assert.assertThrows(WebSessionEndedException.class, () -> new TimedoutTargetLocator(targetLocator, driver).frame(0));
        verify(driver).setDriverExited();
    }
    @Test(groups="ut")
    public void testFrame() {
        when(targetLocator.frame(anyInt())).thenReturn(driver);
        new TimedoutTargetLocator(targetLocator, driver).frame(0);
        verify(driver, never()).setDriverExited();
    }
    @Test(groups="ut")
    public void testFrame2InTimeout() {
        when(targetLocator.frame(anyString())).thenThrow(new TimeoutException());
        Assert.assertThrows(WebSessionEndedException.class, () -> new TimedoutTargetLocator(targetLocator, driver).frame("name"));
        verify(driver).setDriverExited();
    }
    @Test(groups="ut")
    public void testFrame2() {
        when(targetLocator.frame(anyString())).thenReturn(driver);
        new TimedoutTargetLocator(targetLocator, driver).frame("name");
        verify(driver, never()).setDriverExited();
    }
    @Test(groups="ut")
    public void testFrame3InTimeout() {
        when(targetLocator.frame(any(WebElement.class))).thenThrow(new TimeoutException());
        Assert.assertThrows(WebSessionEndedException.class, () -> new TimedoutTargetLocator(targetLocator, driver).frame(element));
        verify(driver).setDriverExited();
    }
    @Test(groups="ut")
    public void testFrame3() {
        when(targetLocator.frame(any(WebElement.class))).thenReturn(driver);
        new TimedoutTargetLocator(targetLocator, driver).frame(element);
        verify(driver, never()).setDriverExited();
    }
    @Test(groups="ut")
    public void testParentFrameInTimeout() {
        when(targetLocator.parentFrame()).thenThrow(new TimeoutException());
        Assert.assertThrows(WebSessionEndedException.class, () -> new TimedoutTargetLocator(targetLocator, driver).parentFrame());
        verify(driver).setDriverExited();
    }
    @Test(groups="ut")
    public void testParentFrame() {
        when(targetLocator.parentFrame()).thenReturn(driver);
        new TimedoutTargetLocator(targetLocator, driver).parentFrame();
        verify(driver, never()).setDriverExited();
    }
    @Test(groups="ut")
    public void testWindowInTimeout() {
        when(targetLocator.window(anyString())).thenThrow(new TimeoutException());
        Assert.assertThrows(WebSessionEndedException.class, () -> new TimedoutTargetLocator(targetLocator, driver).window(""));
        verify(driver).setDriverExited();
    }
    @Test(groups="ut")
    public void testWindow() {
        when(targetLocator.window(anyString())).thenReturn(driver);
        new TimedoutTargetLocator(targetLocator, driver).window("");
        verify(driver, never()).setDriverExited();
    }
    @Test(groups="ut")
    public void testDefaultContentInTimeout() {
        when(targetLocator.defaultContent()).thenThrow(new TimeoutException());
        Assert.assertThrows(WebSessionEndedException.class, () -> new TimedoutTargetLocator(targetLocator, driver).defaultContent());
        verify(driver).setDriverExited();
    }
    @Test(groups="ut")
    public void testDefaultContent() {
        when(targetLocator.defaultContent()).thenReturn(driver);
        new TimedoutTargetLocator(targetLocator, driver).defaultContent();
        verify(driver, never()).setDriverExited();
    }
    @Test(groups="ut")
    public void testAlertInTimeout() {
        when(targetLocator.alert()).thenThrow(new TimeoutException());
        Assert.assertThrows(WebSessionEndedException.class, () -> new TimedoutTargetLocator(targetLocator, driver).alert());
        verify(driver).setDriverExited();
    }
    @Test(groups="ut")
    public void testAlert() {
        when(targetLocator.alert()).thenReturn(alert);
        new TimedoutTargetLocator(targetLocator, driver).alert();
        verify(driver, never()).setDriverExited();
    }
    @Test(groups="ut")
    public void testActiveElementInTimeout() {
        when(targetLocator.activeElement()).thenThrow(new TimeoutException());
        Assert.assertThrows(TimeoutException.class, () -> new TimedoutTargetLocator(targetLocator, driver).activeElement());
        verify(driver, never()).setDriverExited();
    }
    @Test(groups="ut")
    public void testActiveElement() {
        when(targetLocator.activeElement()).thenReturn(element);
        new TimedoutTargetLocator(targetLocator, driver).activeElement();
        verify(driver, never()).setDriverExited();
    }
    @Test(groups="ut")
    public void testNewWindowInTimeout() {
        when(targetLocator.newWindow(any(WindowType.class))).thenThrow(new TimeoutException());
        Assert.assertThrows(WebSessionEndedException.class, () -> new TimedoutTargetLocator(targetLocator, driver).newWindow(windowType));
        verify(driver).setDriverExited();
    }
    @Test(groups="ut")
    public void testNewWindow() {
        when(targetLocator.newWindow(any(WindowType.class))).thenReturn(driver);
        new TimedoutTargetLocator(targetLocator, driver).newWindow(windowType);
        verify(driver, never()).setDriverExited();
    }
}
