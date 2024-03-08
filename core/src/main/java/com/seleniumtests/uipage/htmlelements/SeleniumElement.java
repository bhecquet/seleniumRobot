package com.seleniumtests.uipage.htmlelements;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.lang.reflect.Field;

/**
 * Class that holds a Selenium element (select or WebElement) to ease reporting
 */
public class SeleniumElement extends Element {

    private WebElement webElement;
    private Select select;

    public SeleniumElement(WebElement webElement) {
        super(null);
        this.webElement = webElement;
    }

    public SeleniumElement(Select select) {
        super(null);
        this.select = select;
    }

    @Override
    public String getName() {
        String name = super.getName();
        if (name != null) {
            return name;
        } else if (select != null) {
            try {
                Field elementField = Select.class.getDeclaredField("element");
                elementField.setAccessible(true);
                WebElement element = (WebElement) elementField.get(select);
                return "Select " + getElementLocator(element);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else if (webElement != null) {
            return getElementLocator(webElement);
        }
        return null;
    }

    private String getElementLocator(WebElement el) {
        // handle: Decorated {<button id="button2" name="resetButton" onclick="javascript:addText('text2', '');javascript:resetState();">} ??

        if (el.toString().contains("->")) {
            try {
                return el.toString().split("->")[1].replace("]", "").replace("}", "");
            } catch (IndexOutOfBoundsException e) {
                // we should never go here
            }
        }
        return  el.toString();
    }

    @Override
    protected void findElement(boolean waitForVisibility) {

    }

    @Override
    public void click() {

    }

    @Override
    public void sendKeys(CharSequence... text) {

    }

    @Override
    public boolean isElementPresent() {
        return false;
    }
}
