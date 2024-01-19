package com.seleniumtests.ut;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Pdf;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.federatedcredentialmanagement.FederatedCredentialManagementDialog;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.print.PrintOptions;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.CommandPayload;
import org.openqa.selenium.remote.ErrorHandler;
import org.openqa.selenium.remote.ExecuteMethod;
import org.openqa.selenium.remote.FileDetector;
import org.openqa.selenium.remote.JsonToWebElementConverter;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.virtualauthenticator.VirtualAuthenticator;
import org.openqa.selenium.virtualauthenticator.VirtualAuthenticatorOptions;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.logging.Level;

public class MockWebDriver extends RemoteWebDriver {

    RemoteWebDriver mockedDriver;

    public MockWebDriver() {
        super();
    }

    public MockWebDriver(RemoteWebDriver mockedDriver) {
        super();
        this.mockedDriver = mockedDriver;
    }

    public MockWebDriver(Capabilities capabilities) {
        super(capabilities);
    }

    public MockWebDriver(Capabilities capabilities, boolean enableTracing) {
        super(capabilities, enableTracing);
    }

    public MockWebDriver(URL remoteAddress, Capabilities capabilities) {
        super(remoteAddress, capabilities);
    }

    public MockWebDriver(URL remoteAddress, Capabilities capabilities, boolean enableTracing) {
        super(remoteAddress, capabilities, enableTracing);
    }

    public MockWebDriver(CommandExecutor executor, Capabilities capabilities) {
        super(executor, capabilities);
    }

    @Override
    public SessionId getSessionId() {
        return mockedDriver.getSessionId();
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return mockedDriver.getErrorHandler();
    }

    @Override
    public void setErrorHandler(ErrorHandler handler) {
        mockedDriver.setErrorHandler(handler);
    }

    @Override
    public CommandExecutor getCommandExecutor() {
        return mockedDriver.getCommandExecutor();
    }

    @Override
    public Capabilities getCapabilities() {
        return mockedDriver.getCapabilities();
    }

    @Override
    public void get(String url) {
        mockedDriver.get(url);
    }

    @Override
    public String getTitle() {
        return mockedDriver.getTitle();
    }

    @Override
    public String getCurrentUrl() {
        return mockedDriver.getCurrentUrl();
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> outputType) throws WebDriverException {
        return mockedDriver.getScreenshotAs(outputType);
    }

    @Override
    public Pdf print(PrintOptions printOptions) throws WebDriverException {
        return mockedDriver.print(printOptions);
    }

    @Override
    public WebElement findElement(By locator) {
        return mockedDriver.findElement(locator);
    }

    @Override
    public List<WebElement> findElements(By locator) {
        return mockedDriver.findElements(locator);
    }

    @Override
    public List<WebElement> findElements(SearchContext context, BiFunction<String, Object, CommandPayload> findCommand, By locator) {
        return mockedDriver.findElements(context, findCommand, locator);
    }

    @Override
    public String getPageSource() {
        return mockedDriver.getPageSource();
    }

    @Override
    public void close() {
        mockedDriver.close();
    }

    @Override
    public void quit() {
        mockedDriver.quit();
    }

    @Override
    public Set<String> getWindowHandles() {
        return mockedDriver.getWindowHandles();
    }

    @Override
    public String getWindowHandle() {
        return mockedDriver.getWindowHandle();
    }

    @Override
    public Object executeScript(String script, Object... args) {
        return mockedDriver.executeScript(script, args);
    }

    @Override
    public Object executeAsyncScript(String script, Object... args) {
        return mockedDriver.executeAsyncScript(script, args);
    }

    @Override
    public TargetLocator switchTo() {
        return mockedDriver.switchTo();
    }

    @Override
    public Navigation navigate() {
        return mockedDriver.navigate();
    }

    @Override
    public Options manage() {
        return mockedDriver.manage();
    }

    @Override
    public void setLogLevel(Level level) {
        mockedDriver.setLogLevel(level);
    }

    @Override
    public void perform(Collection<Sequence> actions) {
        mockedDriver.perform(actions);
    }

    @Override
    public void resetInputState() {
        mockedDriver.resetInputState();
    }

    @Override
    public VirtualAuthenticator addVirtualAuthenticator(VirtualAuthenticatorOptions options) {
        return mockedDriver.addVirtualAuthenticator(options);
    }

    @Override
    public void removeVirtualAuthenticator(VirtualAuthenticator authenticator) {
        mockedDriver.removeVirtualAuthenticator(authenticator);
    }

    @Override
    public void setDelayEnabled(boolean enabled) {
        mockedDriver.setDelayEnabled(enabled);
    }

    @Override
    public void resetCooldown() {
        mockedDriver.resetCooldown();
    }

    @Override
    public FederatedCredentialManagementDialog getFederatedCredentialManagementDialog() {
        return mockedDriver.getFederatedCredentialManagementDialog();
    }

    @Override
    public FileDetector getFileDetector() {
        return mockedDriver.getFileDetector();
    }

    @Override
    public void setFileDetector(FileDetector detector) {
        mockedDriver.setFileDetector(detector);
    }

    @Override
    public String toString() {
        return mockedDriver.toString();
    }
}
