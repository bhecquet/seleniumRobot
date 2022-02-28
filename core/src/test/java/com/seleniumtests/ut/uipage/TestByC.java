package com.seleniumtests.ut.uipage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.mockito.Mock;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.uipage.ByC;

public class TestByC extends MockitoTest {

    /**
     * Tests to get Xpath
     */

    @Mock
    private RemoteWebDriver driver;

    @Mock
    private WebElement element1;

    @Mock
    private WebElement element2;

    @Test
    public void testXPathByAttributeQuote() {
        ByC.ByAttribute byAttribute = new ByC.ByAttribute("'", "'");
        String stringPath = byAttribute.getEffectiveXPath();
        assertEquals(stringPath, ".//*[@'=co)]");
    }

    @Test
    public void testXPathByAttribute() {
        ByC.ByAttribute byAttribute = new ByC.ByAttribute("span", "maméduse");
        String stringPath = byAttribute.getEffectiveXPath();
        assertEquals(stringPath, ".//*[@span='maméduse']");
    }

    @Test
    public void testXPathByLabelBackwardQuote() {
        ByC.ByLabelBackward byLabelBackward = new ByC.ByLabelBackward("'", "td", true, "'");
        String stringPath = byLabelBackward.getEffectiveXPath();
        assertEquals(stringPath, ".//'[contains(text(),co))]/preceding::td");
    }

    @Test
    public void testXPathByLabelBackward() {
        ByC.ByLabelBackward byLabelBackward = new ByC.ByLabelBackward("universalMusic", "btn", false, "universalMusicMeduse");
        String stringPath = byLabelBackward.getEffectiveXPath();
        assertEquals(stringPath, ".//universalMusicMeduse[text() = 'universalMusic']/preceding::btn");
    }

    @Test
    public void testXPathByLabelForwardQuote() {
        ByC.ByLabelForward byLabelForward = new ByC.ByLabelForward("'", "table", true, "'");
        String stringPath = byLabelForward.getEffectiveXPath();
        assertEquals(stringPath, ".//'[contains(text(),co))]/following::table");
    }

    @Test
    public void testXPathByLabelForward() {
        ByC.ByLabelForward byLabelForward = new ByC.ByLabelForward("rouge", "id", true, "rougeBanksy");
        String stringPath = byLabelForward.getEffectiveXPath();
        assertEquals(stringPath, ".//rougeBanksy[contains(text(),'rouge')]/following::id");
    }

    @Test
    public void testXPathByTextQuote() {
        ByC.ByText byText = new ByC.ByText("'", "'", true);
        String stringPath = byText.getEffectiveXPath();
        assertEquals(stringPath, ".//'[text() = co)]");
    }

    @Test
    public void testXPathByText() {
        ByC.ByText byText = new ByC.ByText("scyphozoa", "td", false);
        String stringPath = byText.getEffectiveXPath();
        assertEquals(stringPath, ".//td[text() = 'scyphozoa']");
    }

    @Test
    public void testXPathByXClassNameQuote() {
        ByC.ByXClassName byXClassName = new ByC.ByXClassName("'");
        String stringPath = byXClassName.getEffectiveXPath();
        assertEquals(stringPath, ".//*[contains(concat(' ',normalize-space(@class),' '),' ' ')]");
    }

    @Test
    public void testXPathByXClassName() {
        ByC.ByXClassName byXClassName = new ByC.ByXClassName("aequorea");
        String stringPath = byXClassName.getEffectiveXPath();
        assertEquals(stringPath, ".//*[contains(concat(' ',normalize-space(@class),' '),' aequorea ')]");
    }

    @Test
    public void testXPathByXTagNameQuote() {
        ByC.ByXTagName byXTagName = new ByC.ByXTagName("'aureliaAurita'");
        String stringPath = byXTagName.getEffectiveXPath();
        assertEquals(stringPath, ".//'aureliaAurita'");
    }

    @Test
    public void testXPathByXTagName() {
        ByC.ByXTagName byXTagName = new ByC.ByXTagName("span");
        String stringPath = byXTagName.getEffectiveXPath();
        assertEquals(stringPath, ".//span");
    }


    /**
     * Tests to find element(s)
     */

    @Test
    public void testFindElementByAttribute() {
        ByC.ByAttribute byAttribute = spy(new ByC.ByAttribute("name", "value"));
        byAttribute.findElement(driver);
        verify(driver).findElement(By.xpath(".//*[@name='value']"));
    }

    @Test
    public void testFindElementsByAttribute() {
        ByC.ByAttribute byAttribute = spy(new ByC.ByAttribute("meduse", "douce"));
        byAttribute.findElements(driver);
        verify(driver).findElements(By.xpath(".//*[@meduse='douce']"));
    }

    @Test
    public void testFindElementByLabelBackward() {
        ByC.ByLabelBackward byLabelBackward = spy(new ByC.ByLabelBackward("Physalia", "div", true,"label"));
        when(driver.findElements(any(By.class))).thenReturn( Arrays.asList(element1, element2));
        WebElement el = byLabelBackward.findElement(driver);
        Assert.assertEquals(el, element2);
        verify(driver).findElements(By.xpath(".//label[contains(text(),'Physalia')]/preceding::div"));
    }

    @Test
    public void testFindElementsByLabelBackward() {
        ByC.ByLabelBackward byLabelBackward = spy(new ByC.ByLabelBackward("badaboum", "table", true,"label"));
        byLabelBackward.findElements(driver);
        verify(driver).findElements(By.xpath(".//label[contains(text(),'badaboum')]/preceding::table"));
    }

    @Test
    public void testFindElementByLabelForward() {
        ByC.ByLabelForward byLabelForward = spy(new ByC.ByLabelForward("Chrysaora", "frame", true,"label"));
        when(driver.findElements(any())).thenReturn( Arrays.asList(element1, element2));
        byLabelForward.findElement(driver);
        verify(driver).findElement(By.xpath(".//label[contains(text(),'Chrysaora')]/following::frame"));
    }

    @Test
    public void testFindElementsByLabelForward() {
        ByC.ByLabelBackward byLabelForward = spy(new ByC.ByLabelBackward("Cotylorhiza", "td", true,"label"));
        byLabelForward.findElements(driver);
        verify(driver).findElements(By.xpath(".//label[contains(text(),'Cotylorhiza')]/preceding::td"));
    }

    @Test
    public void testFindElementByText() {
        ByC.ByText byText = spy(new ByC.ByText("Pelagia", "li",true));
        byText.findElement(driver);
        verify(driver).findElement(By.xpath(".//li[contains(text(),'Pelagia')]"));
    }

    @Test
    public void testFindElementsByText() {
        ByC.ByText byText = spy(new ByC.ByText("Aequorea","ol",true));
        byText.findElements(driver);
        verify(driver).findElements(By.xpath(".//ol[contains(text(),'Aequorea')]"));
    }

    @Test
    public void testFindElementByXClassName() {
        ByC.ByXClassName byXClassName = spy(new ByC.ByXClassName("Carybdea"));
        byXClassName.findElement(driver);
        verify(driver).findElement(By.xpath(".//*[contains(concat(' ',normalize-space(@class),' '),' Carybdea ')]"));
    }

    @Test
    public void testFindElementsByXClassName() {
        ByC.ByXClassName byXClassName = spy(new ByC.ByXClassName("Rhizostoma"));
        byXClassName.findElements(driver);
        verify(driver).findElements(By.xpath(".//*[contains(concat(' ',normalize-space(@class),' '),' Rhizostoma ')]"));
    }

    @Test
    public void testFindElementByXTagName() {
        ByC.ByXTagName byXTagName = spy(new ByC.ByXTagName("Velella"));
        byXTagName.findElement(driver);
        verify(driver).findElement(By.xpath(".//Velella"));
    }

    @Test
    public void testFindElementsByXTagName() {
        ByC.ByXTagName byXTagName = spy(new ByC.ByXTagName("Turritopsis"));
        byXTagName.findElements(driver);
        verify(driver).findElements(By.xpath(".//Turritopsis"));
    }

}
