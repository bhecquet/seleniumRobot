package com.seleniumtests.ut.uipage.htmlelements;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.uipage.htmlelements.LabelElement;


@PrepareForTest(WebUIDriver.class)
public class TestLabelElement extends MockitoTest {
	
	@Mock
	private WebDriver driver;
	
	@Mock
	private WebElement element;

	@Test(groups={"ut"})
	public void testLabelElement() throws Exception {
		PowerMockito.mockStatic(WebUIDriver.class);
		Mockito.when(WebUIDriver.getWebDriver()).thenReturn(driver);
		Mockito.when(driver.findElement(By.id("label"))).thenReturn(element);
		Mockito.when(element.getText()).thenReturn("textual label");
		
		LabelElement el = Mockito.spy(new LabelElement("label", By.id("label")));
	
		Assert.assertEquals(el.getText(), "textual label");
		
		// check we called getDriver before using it
		PowerMockito.verifyPrivate(el, Mockito.times(1)).invoke("getDriver");
	}
}
