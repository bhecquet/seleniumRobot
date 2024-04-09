package com.seleniumtests.ut.uipage.htmlelements;

import static org.mockito.Mockito.when;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.uipage.htmlelements.SeleniumElement;
import org.mockito.Mock;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestSeleniumElement extends MockitoTest {

    @Mock
    private RemoteWebElement element;



    @Test(groups="ut")
    public void testGetNameWebElement() {
        when(element.toString()).thenReturn("Decorated {[[ChromeDriver: chrome on windows (83c6e18cbc09be7060ef3f92feda6877)] -> id: button2]}");
        Assert.assertEquals(new SeleniumElement(element).getName(), "id: button2");
    }

    @Test(groups="ut")
    public void testGetNameSelect() {
        when(element.getTagName()).thenReturn("select");
        when(element.toString()).thenReturn("Decorated {[[ChromeDriver: chrome on windows (83c6e18cbc09be7060ef3f92feda6877)] -> id: button2]}");
        Assert.assertEquals(new SeleniumElement(new Select(element)).getName(), "Select id: button2");
    }

    @Test(groups="ut")
    public void testGetNameHtmlUnitElement() {
        when(element.toString()).thenReturn("Decorated {<button id=\"button2\" name=\"resetButton\" onclick=\"javascript:addText('text2', '');javascript:resetState();\">}");
        Assert.assertEquals(new SeleniumElement(element).getName(), "Decorated {<button id=\"button2\" name=\"resetButton\" onclick=\"javascript:addText('text2', '');javascript:resetState();\">}");
    }

    @Test(groups="ut")
    public void testGetNameWebElementNull() {
        Assert.assertNull(new SeleniumElement((WebElement) null).getName());
    }

}
