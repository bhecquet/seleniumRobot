package com.seleniumtests.ut.uipage.testpages;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.Step;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.it.driver.support.pages.DriverSubTestPage;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.uipage.ByC;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.*;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;

import java.util.List;

import com.seleniumtests.uipage.htmlelements.TextFieldElement;

public class NoStaticFieldPage extends PageObject {

    public TextFieldElement textElement = new TextFieldElement("Text", By.id("text2"));

    private String openedPageUrl;

    public NoStaticFieldPage() {
        super(null);
    }


    public static String getPageUrl(BrowserType browserType) {
        if (browserType == BrowserType.FIREFOX) {
            return "file://" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile();
        } else {
            return "file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile();
        }
    }

    public String getOpenedPageUrl() {
        return openedPageUrl;
    }
}

