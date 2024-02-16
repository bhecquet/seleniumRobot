package com.seleniumtests.uipage.selectorupdaters;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

public class MobileWebViewUpdater implements SelectorUpdater {

    private final boolean isWebViewTest;
    private final boolean isAppTest;

    /**
     * This updater only applies on WebView mobile tests, not browser tests
     * @param isWebViewTest     is test inside a mobile app webview
     * @param isAppTest         is it an application test
     */
    public MobileWebViewUpdater(boolean isWebViewTest, boolean isAppTest) {
        this.isWebViewTest = isWebViewTest;
        this.isAppTest = isAppTest;
    }

    /**
     * Only a sub set of selectors are supported for webview (only the W3C ones)
     * Move all unsupported to cssSelector
     * @param element
     */
    @Override
    public void update(HtmlElement element) {
        if (!isAppTest
            || !isWebViewTest
        ) {
            return;
        }

        By by = element.getBy();

        // in mobile webviews id, name, classname are not supported
        String locatorValue = by.toString().split(":", 2)[1].trim();
        if ((by instanceof By.ById || by instanceof AppiumBy.ById)) {
            by = By.cssSelector("#" + locatorValue);
        } else if (by instanceof By.ByName || by instanceof AppiumBy.ByName) {
            by = By.cssSelector(String.format("*[name='%s']", locatorValue.replace("'", "\\'")));
        } else if (by instanceof By.ByClassName || by instanceof AppiumBy.ByClassName) {
            by = By.cssSelector("." + locatorValue);
        }
        element.setBy(by);


    }
}
