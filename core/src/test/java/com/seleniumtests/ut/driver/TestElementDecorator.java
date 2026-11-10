package com.seleniumtests.ut.driver;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.driver.*;
import org.mockito.Mock;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestElementDecorator extends MockitoTest {


    @Mock
    private WebElement el;

    @Mock
    private InternetExplorerDriver realDriver;

    @Test(groups = {"ut"})
    public void testIEIsDisplayedUsesJavascript() {
        when(realDriver.findElement(any(By.class))).thenReturn(el);
        when(realDriver.executeScript(anyString(), any(Object[].class))).thenReturn(true);

        ElementDecorator decorator = new ElementDecorator(BrowserType.INTERNET_EXPLORER);
        // elements must be retrieved through the decorated driver so that the decorator can access the original driver
        WebDriver decoratedDriver = decorator.decorate(realDriver);
        WebElement decoratedEl = decoratedDriver.findElement(By.id("foo"));

        Assert.assertTrue(decoratedEl.isDisplayed());

        verify(el, never()).isDisplayed();
        verify(realDriver).executeScript(contains("isDisplayed"), eq(el));
    }

    @Test(groups = {"ut"})
    public void testIEGetAttributeUsesJavascript() {
        when(realDriver.findElement(any(By.class))).thenReturn(el);
        when(realDriver.executeScript(anyString(), any(Object[].class))).thenReturn("bar");

        ElementDecorator decorator = new ElementDecorator(BrowserType.INTERNET_EXPLORER);
        // elements must be retrieved through the decorated driver so that the decorator can access the original driver
        WebDriver decoratedDriver = decorator.decorate(realDriver);
        WebElement decoratedEl = decoratedDriver.findElement(By.id("foo"));

        Assert.assertEquals(decoratedEl.getAttribute("foo"), "bar");

        verify(el, never()).getAttribute(anyString());
        verify(realDriver).executeScript(contains("getAttribute"), eq(el), eq("foo"));
    }

    @Test(groups = {"ut"})
    public void testChromeIsDisplayedUsesElementMethod() {
        when(realDriver.findElement(any(By.class))).thenReturn(el);
        when(el.isDisplayed()).thenReturn(true);

        ElementDecorator decorator = new ElementDecorator(BrowserType.CHROME);
        WebDriver decoratedDriver = decorator.decorate(realDriver);
        WebElement decoratedEl = decoratedDriver.findElement(By.id("foo"));

        Assert.assertTrue(decoratedEl.isDisplayed());

        verify(el).isDisplayed();
        verify(realDriver, never()).executeScript(anyString(), any(Object[].class));
    }
}
