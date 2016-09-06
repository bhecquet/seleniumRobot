package com.seleniumtests.ut.uipage.htmlelements;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.uipage.htmlelements.ImageElement;


@PrepareForTest(WebUIDriver.class)
public class TestImageElement extends MockitoTest {
	
	@Mock
	private WebDriver driver;
	
	@Mock
	private WebElement element;

	@Test(groups={"ut"})
	public void testImageElement() throws Exception {
		PowerMockito.mockStatic(WebUIDriver.class);
		Mockito.when(WebUIDriver.getWebDriver()).thenReturn(driver);
		Mockito.when(driver.findElement(By.id("img"))).thenReturn(element);
		Mockito.when(element.getSize()).thenReturn(new Dimension(10,10));
		Mockito.when(element.getAttribute("src")).thenReturn("http://nowhere.com/jpg");
		
		ImageElement el = Mockito.spy(new ImageElement("image", By.id("img")));
	
		Assert.assertEquals(el.getHeight(), 10);
		Assert.assertEquals(el.getWidth(), 10);
		Assert.assertEquals(el.getUrl(), "http://nowhere.com/jpg");
		
		// check we called getDriver before using it
		PowerMockito.verifyPrivate(el, Mockito.times(3)).invoke("getDriver");
	}
}
