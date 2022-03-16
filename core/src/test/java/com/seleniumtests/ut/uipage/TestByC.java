package com.seleniumtests.ut.uipage;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.mails.Email;
import com.seleniumtests.connectors.mails.EmailClientSelector;
import com.seleniumtests.connectors.mails.EmailServer;
import com.seleniumtests.uipage.ByC;
import org.mockito.Mock;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.FindsByXPath;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.powermock.api.mockito.PowerMockito;
import org.testng.Assert;
import org.testng.TestException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.validation.constraints.Null;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.*;

public class TestByC extends MockitoTest {
    @Mock
    private WebElement element1;
    @Mock
    private WebElement element2;

    @Mock
    private RemoteWebDriver driver;

    @Mock
    private By.ById id;

    @Mock
    private By.ByName name;

    @Mock
    private WebElement elements;


    @BeforeMethod(groups = {"ut"})
    public void init() throws Exception {
        when(id.findElements(driver)).thenReturn(Arrays.asList(element1));
        when(name.findElements(driver)).thenReturn(Arrays.asList(element1, element2));
    }

    /**
     * Tests getEffectiveXPath
     */

    // ByAttribute
    @Test(groups = {"ut"})
    public void testXPathByAttributeQuote() {
        ByC.ByAttribute byAttribute = new ByC.ByAttribute("'", "'");
        String stringPath = byAttribute.getEffectiveXPath();
        assertEquals(stringPath, ".//*[@'=co)]");
    }

    @Test(groups = {"ut"})
    public void testXPathByAttribute() {
        ByC.ByAttribute byAttribute = new ByC.ByAttribute("span", "myJellyfish");
        String stringPath = byAttribute.getEffectiveXPath();
        assertEquals(stringPath, ".//*[@span='myJellyfish']");
    }

    @Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
    public void testXPathByAttributeNameNull() {
        ByC.ByAttribute byAttribute = new ByC.ByAttribute(null, "myJellyfish");
        byAttribute.getEffectiveXPath();
    }

    @Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
    public void testXPathByAttributeValue() {
        ByC.ByAttribute byAttribute = new ByC.ByAttribute("span", null);
        byAttribute.getEffectiveXPath();
    }

    // ByLabelBackward
    @Test(groups = {"ut"})
    public void testXPathByLabelBackwardQuote() {
        ByC.ByLabelBackward byLabelBackward = new ByC.ByLabelBackward("'", "source", true, "'");
        String stringPath = byLabelBackward.getEffectiveXPath();
        assertEquals(stringPath, ".//'[contains(text(),co))]/preceding::source");
    }

    @Test(groups = {"ut"})
    public void testXPathByLabelBackward() {
        ByC.ByLabelBackward byLabelBackward = new ByC.ByLabelBackward("universalMusic", "kbd", false, "universalMusicJellyfish");
        String stringPath = byLabelBackward.getEffectiveXPath();
        assertEquals(stringPath, ".//universalMusicJellyfish[text() = 'universalMusic']/preceding::kbd");
    }

    @Test(groups = {"ut"})
    public void testXPathByLabelBackwardPartialTrue() {
        ByC.ByLabelBackward byLabelBackward = new ByC.ByLabelBackward("universalMusic", "track", true, "universalMusicJellyfish");
        String stringPath = byLabelBackward.getEffectiveXPath();
        assertEquals(stringPath, ".//universalMusicJellyfish[contains(text(),'universalMusic')]/preceding::track");
    }

    @Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
    public void testXPathByLabelBackwardLabelNull() {
        ByC.ByLabelBackward byLabelBackward = new ByC.ByLabelBackward(null, "noscript", true, "universalMusicJellyfish");
        byLabelBackward.getEffectiveXPath();
    }

    @Test(groups = {"ut"})
    public void testXPathByLabelBackwardTagNameNull() {
        ByC.ByLabelBackward byLabelBackward = new ByC.ByLabelBackward("universalMusic", null, true, "universalMusicJellyfish");
        String stringPath = byLabelBackward.getEffectiveXPath();
        assertEquals(stringPath, ".//universalMusicJellyfish[contains(text(),'universalMusic')]/preceding::input");
    }

    @Test(groups = {"ut"})
    public void testXPathByLabelBackwardTagNameNullPartialFalse() {
        ByC.ByLabelBackward byLabelBackward = new ByC.ByLabelBackward("universalMusic", null, false, "universalMusicJellyfish");
        String stringPath = byLabelBackward.getEffectiveXPath();
        assertEquals(stringPath, ".//universalMusicJellyfish[text() = 'universalMusic']/preceding::input");
    }

    @Test(groups = {"ut"})
    public void testXPathByLabelBackwardLabelTagNameNull() {
        ByC.ByLabelBackward byLabelBackward = new ByC.ByLabelBackward("universalMusic", "link", true, null);
        String stringPath = byLabelBackward.getEffectiveXPath();
        assertEquals(stringPath, ".//label[contains(text(),'universalMusic')]/preceding::link");
    }

    @Test(groups = {"ut"})
    public void testXPathByLabelBackwardLabelTagNameNullPartialFalse() {
        ByC.ByLabelBackward byLabelBackward = new ByC.ByLabelBackward("universalMusic", "button", false, null);
        String stringPath = byLabelBackward.getEffectiveXPath();
        assertEquals(stringPath, ".//label[text() = 'universalMusic']/preceding::button");
    }

    // ByLabelForward
    @Test(groups = {"ut"})
    public void testXPathByLabelForwardQuote() {
        ByC.ByLabelForward byLabelForward = new ByC.ByLabelForward("'", "table", true, "'");
        String stringPath = byLabelForward.getEffectiveXPath();
        assertEquals(stringPath, ".//'[contains(text(),co))]/following::table");
    }

    @Test(groups = {"ut"})
    public void testXPathByLabelForward() {
        ByC.ByLabelForward byLabelForward = new ByC.ByLabelForward("span", "header", true, "redBanksy");
        String stringPath = byLabelForward.getEffectiveXPath();
        assertEquals(stringPath, ".//redBanksy[contains(text(),'span')]/following::header");
    }

    @Test(groups = {"ut"})
    public void testXPathByLabelForwardPartialFalse() {
        ByC.ByLabelForward byLabelForward = new ByC.ByLabelForward("span", "col-6", false, "redBanksy");
        String stringPath = byLabelForward.getEffectiveXPath();
        assertEquals(stringPath, ".//redBanksy[text() = 'span']/following::col-6");
    }

    @Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
    public void testXPathByLabelForwardLabelNull() {
        ByC.ByLabelForward byLabelForward = new ByC.ByLabelForward(null, "canvas", true, "redBanksy");
        byLabelForward.getEffectiveXPath();
    }

    @Test(groups = {"ut"})
    public void testXPathByLabelForwardTagNameNull() {
        ByC.ByLabelForward byLabelForward = new ByC.ByLabelForward("span", null, true, "redBanksy");
        String stringPath = byLabelForward.getEffectiveXPath();
        assertEquals(stringPath, ".//redBanksy[contains(text(),'span')]/following::input");
    }

    @Test(groups = {"ut"})
    public void testXPathByLabelForwardLabelTagNameNull() {
        ByC.ByLabelForward byLabelForward = new ByC.ByLabelForward("span", "form", true, null);
        String stringPath = byLabelForward.getEffectiveXPath();
        assertEquals(stringPath, ".//label[contains(text(),'span')]/following::form");
    }

    // ByText
    @Test(groups = {"ut"})
    public void testXPathByTextQuote() {
        ByC.ByText byText = new ByC.ByText("'", "'", true);
        String stringPath = byText.getEffectiveXPath();
        assertEquals(stringPath, ".//'[text() = co)]");
    }

    @Test(groups = {"ut"})
    public void testXPathByText() {
        ByC.ByText byText = new ByC.ByText("scyphozoa", "td", false);
        String stringPath = byText.getEffectiveXPath();
        assertEquals(stringPath, ".//td[text() = 'scyphozoa']");
    }

    @Test(groups = {"ut"})
    public void testXPathByTextPartialTrue() {
        ByC.ByText byText = new ByC.ByText("scyphozoa", "td", true);
        String stringPath = byText.getEffectiveXPath();
        assertEquals(stringPath, ".//td[text() = 'scyphozoa']");
    }

    @Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
    public void testXPathByTextNull() {
        ByC.ByText byText = new ByC.ByText(null, "td", false);
        byText.getEffectiveXPath();
    }

    @Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
    public void testXPathByTextTagNameNull() {
        ByC.ByText byText = new ByC.ByText("scyphozoa", null, false);
        byText.getEffectiveXPath();
    }

    // ByxClassName
    @Test(groups = {"ut"})
    public void testXPathByXClassNameQuote() {
        ByC.ByXClassName byXClassName = new ByC.ByXClassName("'");
        String stringPath = byXClassName.getEffectiveXPath();
        assertEquals(stringPath, ".//*[contains(concat(' ',normalize-space(@class),' '),' ' ')]");
    }

    @Test(groups = {"ut"})
    public void testXPathByXClassName() {
        ByC.ByXClassName byXClassName = new ByC.ByXClassName("aequorea");
        String stringPath = byXClassName.getEffectiveXPath();
        assertEquals(stringPath, ".//*[contains(concat(' ',normalize-space(@class),' '),' aequorea ')]");
    }

    @Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
    public void testXPathByXClassNameNull() {
        ByC.ByXClassName byXClassName = new ByC.ByXClassName(null);
        byXClassName.getEffectiveXPath();
    }

    // ByXTagName
    @Test(groups = {"ut"})
    public void testXPathByXTagName() {
        ByC.ByXTagName byXTagName = new ByC.ByXTagName("span");
        String stringPath = byXTagName.getEffectiveXPath();
        assertEquals(stringPath, ".//span");
    }

    @Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
    public void testXPathByXTagNameNull() {
        ByC.ByXTagName byXTagName = new ByC.ByXTagName(null);
        String stringPath = byXTagName.getEffectiveXPath();
    }

    /**
     * Tests to findElement(s) (XPath)
     */

    // ByAttribute
    @Test(groups = {"ut"})
    public void testFindElementByAttribute() {
        ByC.ByAttribute byAttribute = spy(new ByC.ByAttribute("name", "value"));
        byAttribute.findElement(driver);
        verify((FindsByXPath) driver).findElementByXPath(".//*[@name='value']");
    }

    @Test(groups = {"ut"})
    public void testFindElementsByAttribute() {
        ByC.ByAttribute byAttribute = spy(new ByC.ByAttribute("jellyfish", "sweety"));
        byAttribute.findElements(driver);
        verify((FindsByXPath) driver).findElementsByXPath(".//*[@jellyfish='sweety']");
    }

    @Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
    public void testFindElementsByAttributeNameNull() {
        ByC.ByAttribute byAttribute = spy(new ByC.ByAttribute(null, "douce"));
        byAttribute.findElements(driver);
        verify((FindsByXPath) driver).findElementsByXPath(".//*[@meduse='douce']");
    }

    @Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
    public void testFindElementsByAttributeValueNull() {
        ByC.ByAttribute byAttribute = spy(new ByC.ByAttribute("meduse", null));
        byAttribute.findElements(driver);
    }

    // ByLabelBackward
    @Test(groups = {"ut"})
    public void testFindElementByLabelBackward() {
        ByC.ByLabelBackward byLabelBackward = spy(new ByC.ByLabelBackward("Physalia", "div", true, "label"));
        when(((FindsByXPath) driver).findElementsByXPath(anyString())).thenReturn(Arrays.asList(element1, element2));
        WebElement el = byLabelBackward.findElement(driver);
        Assert.assertEquals(el, element2);
        verify((FindsByXPath) driver).findElementsByXPath(".//label[contains(text(),'Physalia')]/preceding::div");
    }

    @Test(groups = {"ut"})
    public void testFindElementsByLabelBackward() {
        ByC.ByLabelBackward byLabelBackward = spy(new ByC.ByLabelBackward("badaboum", "table", true, "label"));
        byLabelBackward.findElements(driver);
        verify((FindsByXPath) driver).findElementsByXPath(".//label[contains(text(),'badaboum')]/preceding::table");
    }

    @Test(groups = {"ut"})
    public void testFindElementsByLabelBackwardPartialFalse() {
        ByC.ByLabelBackward byLabelBackward = spy(new ByC.ByLabelBackward("badaboum", "table", false, "label"));
        byLabelBackward.findElements(driver);
        verify((FindsByXPath) driver).findElementsByXPath(".//label[text() = 'badaboum']/preceding::table");
    }

    @Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
    public void testFindElementByLabelBackwardLabelNull() {
        ByC.ByLabelBackward byLabelBackward = spy(new ByC.ByLabelBackward(null, "div", true, "label"));
        when(((FindsByXPath) driver).findElementsByXPath(anyString())).thenReturn(Arrays.asList(element1, element2));
        WebElement el = byLabelBackward.findElement(driver);
    }

    @Test(groups = {"ut"})
    public void testFindElementByLabelBackwarTagNameNull() {
        ByC.ByLabelBackward byLabelBackward = spy(new ByC.ByLabelBackward("badaboum", null, true, "label"));
        when(((FindsByXPath) driver).findElementsByXPath(anyString())).thenReturn(Arrays.asList(element1, element2));
        WebElement el = byLabelBackward.findElement(driver);
        Assert.assertEquals(el, element2);
        verify((FindsByXPath) driver).findElementsByXPath(".//label[contains(text(),'badaboum')]/preceding::input");
    }

    @Test(groups = {"ut"})
    public void testFindElementByLabelBackwarLabelTagNameNull() {
        ByC.ByLabelBackward byLabelBackward = spy(new ByC.ByLabelBackward("badaboum", "div", true, null));
        when(((FindsByXPath) driver).findElementsByXPath(anyString())).thenReturn(Arrays.asList(element1, element2));
        WebElement el = byLabelBackward.findElement(driver);
        Assert.assertEquals(el, element2);
        verify((FindsByXPath) driver).findElementsByXPath(".//label[contains(text(),'badaboum')]/preceding::div");
    }

    // ByLabelForward
    @Test(groups = {"ut"})
    public void testFindElementByLabelForward() {
        ByC.ByLabelForward byLabelForward = spy(new ByC.ByLabelForward("Chrysaora", "frame", true, "label"));
        when(((FindsByXPath) driver).findElementsByXPath(anyString())).thenReturn(Arrays.asList(element1, element2));
        byLabelForward.findElement(driver);
        verify((FindsByXPath) driver).findElementByXPath(".//label[contains(text(),'Chrysaora')]/following::frame");
    }

    @Test(groups = {"ut"})
    public void testFindElementsByLabelForward() {
        ByC.ByLabelBackward byLabelForward = spy(new ByC.ByLabelBackward("Cotylorhiza", "td", true, "label"));
        byLabelForward.findElements(driver);
        verify((FindsByXPath) driver).findElementsByXPath(".//label[contains(text(),'Cotylorhiza')]/preceding::td");
    }

    @Test(groups = {"ut"})
    public void testFindElementByLabelForwardPartialFalse() {
        ByC.ByLabelForward byLabelForward = spy(new ByC.ByLabelForward("Chrysaora", "frame", false, "label"));
        when(((FindsByXPath) driver).findElementsByXPath(anyString())).thenReturn(Arrays.asList(element1, element2));
        byLabelForward.findElement(driver);
        verify((FindsByXPath) driver).findElementByXPath(".//label[text() = 'Chrysaora']/following::frame");
    }

    @Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
    public void testFindElementByLabelForwardLabelNull() {
        ByC.ByLabelForward byLabelForward = spy(new ByC.ByLabelForward(null, "frame", true, "label"));
        when(((FindsByXPath) driver).findElementsByXPath(anyString())).thenReturn(Arrays.asList(element1, element2));
        byLabelForward.findElement(driver);
    }

    @Test(groups = {"ut"})
    public void testFindElementByLabelForwardTagNameNull() {
        ByC.ByLabelForward byLabelForward = spy(new ByC.ByLabelForward("Chrysaora", null, true, "label"));
        when(((FindsByXPath) driver).findElementsByXPath(anyString())).thenReturn(Arrays.asList(element1, element2));
        byLabelForward.findElement(driver);
        verify((FindsByXPath) driver).findElementByXPath(".//label[contains(text(),'Chrysaora')]/following::input");
    }

    @Test(groups = {"ut"})
    public void testFindElementByLabelForwardLabelTagNameNull() {
        ByC.ByLabelForward byLabelForward = spy(new ByC.ByLabelForward("Chrysaora", "frame", true, null));
        when(((FindsByXPath) driver).findElementsByXPath(anyString())).thenReturn(Arrays.asList(element1, element2));
        byLabelForward.findElement(driver);
        verify((FindsByXPath) driver).findElementByXPath(".//label[contains(text(),'Chrysaora')]/following::frame");
    }

    // ByText
    @Test(groups = {"ut"})
    public void testFindElementByText() {
        ByC.ByText byText = spy(new ByC.ByText("Pelagia", "li", true));
        byText.findElement(driver);
        verify((FindsByXPath) driver).findElementByXPath(".//li[contains(text(),'Pelagia')]");
    }

    @Test(groups = {"ut"})
    public void testFindElementsByText() {
        ByC.ByText byText = spy(new ByC.ByText("Aequorea", "ol", true));
        byText.findElements(driver);
        verify((FindsByXPath) driver).findElementsByXPath(".//ol[contains(text(),'Aequorea')]");
    }

    @Test(groups = {"ut"})
    public void testFindElementsByTextPartialFalse() {
        ByC.ByText byText = spy(new ByC.ByText("Aequorea", "ol", false));
        byText.findElements(driver);
        verify((FindsByXPath) driver).findElementsByXPath(".//ol[text() = 'Aequorea']");
    }

    @Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
    public void testFindElementByTextNull() {
        ByC.ByText byText = spy(new ByC.ByText(null, "li", true));
        byText.findElement(driver);
    }

    @Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
    public void testFindElementByTextTagNameNull() {
        ByC.ByText byText = spy(new ByC.ByText("Aequorea", null, true));
        byText.findElement(driver);
    }

    // ByXClassName
    @Test(groups = {"ut"})
    public void testFindElementByXClassName() {
        ByC.ByXClassName byXClassName = spy(new ByC.ByXClassName("Carybdea"));
        byXClassName.findElement(driver);
        verify((FindsByXPath) driver).findElementByXPath(".//*[contains(concat(' ',normalize-space(@class),' '),' Carybdea ')]");
    }

    @Test(groups = {"ut"})
    public void testFindElementsByXClassName() {
        ByC.ByXClassName byXClassName = spy(new ByC.ByXClassName("Rhizostoma"));
        byXClassName.findElements(driver);
        verify((FindsByXPath) driver).findElementsByXPath(".//*[contains(concat(' ',normalize-space(@class),' '),' Rhizostoma ')]");
    }

    @Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
    public void testFindElementsByXClassNameNull() {
        ByC.ByXClassName byXClassName = spy(new ByC.ByXClassName(null));
        byXClassName.findElements(driver);
    }

    // ByXTagName
    @Test(groups = {"ut"})
    public void testFindElementByXTagName() {
        ByC.ByXTagName byXTagName = spy(new ByC.ByXTagName("img"));
        byXTagName.findElement(driver);
        verify((FindsByXPath) driver).findElementByXPath(".//img");
    }

    @Test(groups = {"ut"})
    public void testFindElementsByXTagName() {
        ByC.ByXTagName byXTagName = spy(new ByC.ByXTagName("p"));
        byXTagName.findElements(driver);
        verify((FindsByXPath) driver).findElementsByXPath(".//p");
    }

    @Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
    public void testFindElementByXTagNameNull() {
        ByC.ByXTagName byXTagName = spy(new ByC.ByXTagName(null));
        byXTagName.findElement(driver);
    }

    /**
     * Tests to findElement(s) (Elements)
     */

    // ByAnd
    @Test(groups = {"ut"})
    public void testFindElementByAnd() {
        ByC.And byAnd = new ByC.And(id, name);
        elements = byAnd.findElement(driver);
        Assert.assertEquals(elements, element1);
    }

    @Test(groups = {"ut"})
    public void testFindElementsByAnd() {
        ByC.And byAnd = new ByC.And(id, name);
        List<WebElement> elements = byAnd.findElements(driver);
        assertEquals(elements.size(), 1);
    }

    @Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
    public void testFindElementByAndIdNull() {
        ByC.And byAnd = new ByC.And(null, name);
        elements = byAnd.findElement(driver);
    }

    @Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
    public void testFindElementByAndNameNull() {
        ByC.And byAnd = new ByC.And(id, null);
        elements = byAnd.findElement(driver);
    }

    // ByOr
    @Test(groups = {"ut"})
    public void testFindElementByOr() {
        ByC.Or byOr = new ByC.Or(id, name);
        elements = byOr.findElement(driver);
        Assert.assertEquals(elements, element1);
    }

    @Test(groups = {"ut"})
    public void testFindElementsByOr() {
        ByC.Or byOr = new ByC.Or(id, name);
        List<WebElement> elements = byOr.findElements(driver);
        assertFalse(elements.isEmpty());
    }

    @Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
    public void testFindElementsByOrIdNull() {
        ByC.Or byOr = new ByC.Or(null, name);
        List<WebElement> elements = byOr.findElements(driver);
        assertFalse(elements.isEmpty());
    }

    @Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
    public void testFindElementsByOrNameNull() {
        ByC.Or byOr = new ByC.Or(id, null);
        List<WebElement> elements = byOr.findElements(driver);
        assertFalse(elements.isEmpty());
    }
}
