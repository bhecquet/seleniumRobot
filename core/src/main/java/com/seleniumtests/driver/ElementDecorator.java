package com.seleniumtests.driver;

import com.seleniumtests.customexception.CustomSeleniumTestsException;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.decorators.Decorated;
import org.openqa.selenium.support.decorators.DefaultDecorated;
import org.openqa.selenium.support.decorators.WebDriverDecorator;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

/**
 * This decorator helps keeping compatibility with internet explorer
 * atoms evolve with selenium versions, but IE javascript engine throws errors since 4.43 (for IE11 compatibility mode) and 4.51 for all modes
 * So for IE only, we have retrieved the old atoms to use them
 */
public class ElementDecorator extends WebDriverDecorator {

    private String isDisplayedJs;
    private String getAttributeJs;
    private final BrowserType browserType;

    public ElementDecorator(BrowserType browserType) {
        try {
            isDisplayedJs = IOUtils.toString(ElementDecorator.class.getResourceAsStream("/ieAtoms/isDisplayed.js"), StandardCharsets.UTF_8);
            getAttributeJs = IOUtils.toString(ElementDecorator.class.getResourceAsStream("/ieAtoms/getAttribute.js"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CustomSeleniumTestsException("Error loading atoms", e);
        }
        this.browserType = browserType;

    }

    public Decorated<WebElement> createDecorated(WebElement original) {
        return new DefaultDecorated<>(original, this) {

            @Override
            public Object call(Method method, Object[] args) throws Throwable {
                String methodName = method.getName();

                if (BrowserType.INTERNET_EXPLORER.equals(browserType)) {

                    JavascriptExecutor executor = (JavascriptExecutor) getDecoratedDriver().getOriginal();

                    // inspired by W3CHttpCommandCodec
                    if ("isDisplayed".equals(methodName)) {
                        return executor.executeScript(String.format("/* %s */return (%s).apply(null, arguments);", "isDisplayed", isDisplayedJs), getOriginal());
                    } else if ("getAttribute".equals(methodName)) {
                        return executor.executeScript(String.format("/* %s */return (%s).apply(null, arguments);", "getAttribute", getAttributeJs), getOriginal(), args[0]);
                    } else {
                        return super.call(method, args);
                    }
                }

                 else {
                    return super.call(method, args);
                }
            }
        };
    }
}
