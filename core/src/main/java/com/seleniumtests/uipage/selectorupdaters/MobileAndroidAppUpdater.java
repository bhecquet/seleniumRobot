package com.seleniumtests.uipage.selectorupdaters;

import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class MobileAndroidAppUpdater implements SelectorUpdater {

    private final boolean isWebViewTest;
    private final boolean isAppTest;
    private final String platform;
    private final String packageName;

    /**
     *
     * @param platform          platform name: 'android', 'ios', 'windows', ...
     * @param packageName       the package name for android
     * @param isWebViewTest     true is it's a web browser test or web view test inside mobile app
     * @param isAppTest         true is this is a mobile application test
     */
    public MobileAndroidAppUpdater(String platform, String packageName, boolean isWebViewTest, boolean isAppTest) {
        this.platform = platform;
        this.packageName = packageName;
        this.isWebViewTest = isWebViewTest;
        this.isAppTest = isAppTest;
    }

    /**
     * Adds the package name of the application if By.id or any of its derivatives are used
     * Only applies to mobile native android application test
     * By.id("foo") => By.id("com.mobile.app.package:id/foo")
     * @param element
     */
    @Override
    public void update(HtmlElement element) {
        if (!isAppTest
                || isWebViewTest
                || !"android".equalsIgnoreCase(platform)
        ) {
            return;
        }

        By by = element.getBy();

        if ((by instanceof By.ById || by instanceof AppiumBy.ById)
                && !by.toString().split(":", 2)[1].contains(":")
        ) {
            by = By.id(packageName + ":id/" + by.toString().split(":", 2)[1].trim());
        } else if (by instanceof By.ByTagName || by instanceof AppiumBy.ByTagName) {
            by = By.cssSelector(by.toString().split(":", 2)[1].trim());
        } else if (by instanceof By.ByName || by instanceof AppiumBy.ByName) {
            by = By.cssSelector(String.format("[name=%s]", by.toString().split(":")[1].trim()));
        }
        element.setBy(by);

    }
}
