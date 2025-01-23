package com.seleniumtests.driver;

import com.seleniumtests.customexception.WebSessionEndedException;
import org.openqa.selenium.*;

/**
 * Class that tell seleniumRobot driver/browser has died, so that further operation do not wait too long
 * As switchTo.defaultContent() is done for almost any action, it's possible to detect quickly the problem
 */
public class TimedoutTargetLocator implements WebDriver.TargetLocator {

    WebDriver.TargetLocator targetLocator;
    CustomEventFiringWebDriver driver;

    public TimedoutTargetLocator(WebDriver.TargetLocator targetLocator, CustomEventFiringWebDriver driver) {
        this.targetLocator = targetLocator;
        this.driver = driver;
    }

    @Override
    public WebDriver frame(int index) {
        try {
            return targetLocator.frame(index);
        } catch (TimeoutException e) {
            driver.setDriverExited();
            throw new WebSessionEndedException("Timeout switching to frame, suspicion of staled driver/browser => stop here");
        }
    }

    @Override
    public WebDriver frame(String nameOrId) {
        try {
            return targetLocator.frame(nameOrId);
        } catch (TimeoutException e) {
            driver.setDriverExited();
            throw new WebSessionEndedException("Timeout switching to frame, suspicion of staled driver/browser => stop here");
        }
    }

    @Override
    public WebDriver frame(WebElement frameElement) {
        try {
            return targetLocator.frame(frameElement);
        } catch (TimeoutException e) {
            driver.setDriverExited();
            throw new WebSessionEndedException("Timeout switching to frame, suspicion of staled driver/browser => stop here");
        }
    }

    @Override
    public WebDriver parentFrame() {
        try {
            return targetLocator.parentFrame();
        } catch (TimeoutException e) {
            driver.setDriverExited();
            throw new WebSessionEndedException("Timeout switching to parent frame, suspicion of staled driver/browser => stop here");
        }
    }

    @Override
    public WebDriver window(String nameOrHandle) {
        try {
            return targetLocator.window(nameOrHandle);
        } catch (TimeoutException e) {
            driver.setDriverExited();
            throw new WebSessionEndedException("Timeout switching to window, suspicion of staled driver/browser => stop here");
        }
    }

    @Override
    public WebDriver newWindow(WindowType typeHint) {
        try {
            return targetLocator.newWindow(typeHint);
        } catch (TimeoutException e) {
            driver.setDriverExited();
            throw new WebSessionEndedException("Timeout switching to new window, suspicion of staled driver/browser => stop here");
        }
    }

    @Override
    public WebDriver defaultContent() {
        try {
            return targetLocator.defaultContent();
        } catch (TimeoutException e) {
            driver.setDriverExited();
            throw new WebSessionEndedException("Timeout switching to default content, suspicion of staled driver/browser => stop here");
        }
    }

    @Override
    public WebElement activeElement() {
        return targetLocator.activeElement();
    }

    @Override
    public Alert alert() {
        try {
            return targetLocator.alert();
        } catch (TimeoutException e) {
            driver.setDriverExited();
            throw new WebSessionEndedException("Timeout switching to alert, suspicion of staled driver/browser => stop here");
        }
    }
}
