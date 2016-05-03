package com.seleniumtests.webpage;

import com.seleniumtests.core.Locator;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.webelements.PageObject;
import com.seleniumtests.webelements.TextFieldElement;

/**
 * Created by tarun on 3/22/16.
 */
public class GoogleHomePage extends PageObject {

    private static final TextFieldElement searchTextBox = new TextFieldElement("search Text Box", Locator.locateByName("q"));

    public GoogleHomePage() throws Exception {
        super(searchTextBox);
    }
    
    public GoogleHomePage(boolean openPageURL) throws Exception {
        super(searchTextBox, openPageURL ? SeleniumTestsContextManager.getThreadContext().getAppURL() : null);
    }
    
    public boolean isSearchBoxDisplayed() {
        return searchTextBox.isDisplayed();
    }
    
    public GoogleHomePage search(String text) {
    	searchTextBox.sendKeys(text);
    	return this;
    }
}
