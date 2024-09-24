package com.seleniumtests.it.driver;

import com.seleniumtests.GenericTest;
import com.seleniumtests.browserfactory.mobile.AppiumLauncher;
import com.seleniumtests.browserfactory.mobile.ExistingAppiumLauncher;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

public class TestWindowsDriver extends GenericTest {

    @BeforeClass(groups = "it")
    public void init() {
        if (!OSUtility.isWindows()) {
            throw new SkipException("Test only runs on Windows");
        }

        String out = OSCommand.executeCommandAndWait(new String[] {"_USE_PATH_appium",  "driver", "list"});

        // appium driver list returns
        // √ Listing available drivers
        //- flaui@0.0.4 [installed (npm)]
        //- uiautomator2 [not installed]
        //- xcuitest [not installed]
        if (!out.replace("\n", "").replaceAll("\u001B\\[[;\\d]*m", "").matches(".*flaui\\@.*?\\d \\[installed \\(npm\\)\\].*")) {
            throw new SkipException("FlaUI driver not installed");
        }

        if (System.getenv("APPIUM_FLAUI_PATH") == null) {
            throw new SkipException("APPIUM_FLAUI_PATH not set => path to the FlaUI driver)");
        }

        if (!new File(System.getenv("APPIUM_FLAUI_PATH")).exists()) {
            throw new SkipException("FlaUI driver cannot be found in " + System.getenv("APPIUM_FLAUI_PATH"));
        }
    }

    /**
     * Test that it's possible to execute a test on notepad on windows
     * This test requires appium to be available with appium-flaui-driver
     */
    @Test(groups={"it"})
    public void testNotepadAutomation() {

        long appiumPort = 4723 + Math.round(Math.random() * 1000);
        String appiumServerUrl = String.format("http://localhost:%d/", appiumPort);

        SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_WINDOWS);
        SeleniumTestsContextManager.getThreadContext().setApp("C:\\Windows\\System32\\notepad.exe");
        SeleniumTestsContextManager.getThreadContext().setAppiumServerUrl(appiumServerUrl);

        String appiumCmd = new OSCommand(List.of("appium")).searchInWindowsPath("appium");
        Process process = OSCommand.executeCommand(new String[] {"cmd", "/C", "start", "/MIN", "cmd", "/C", appiumCmd,  "--port=" + appiumPort, "--log=d:/appium.log", "--log-level=debug:debug"});
        try {

            WebDriver driver = WebUIDriver.getWebDriver(true);
            driver.findElement(By.className("Edit")).sendKeys("hello notepad");
            Assert.assertEquals(driver.findElement(By.className("Edit")).getText(), "hello notepad");

        } finally {
            process.destroyForcibly();
            closeBrowser();
            OSUtilityFactory.getInstance().killProcessByName("node", true);
        }
    }

    @Test(groups={"it"})
    public void testNotepadAutomationAlreadyStarted() {

        long appiumPort = 4723 + Math.round(Math.random() * 1000);
        String appiumServerUrl = String.format("http://localhost:%d/", appiumPort);

        SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_WINDOWS);
        SeleniumTestsContextManager.getThreadContext().setAppActivity(".*Bloc-notes");
        SeleniumTestsContextManager.getThreadContext().setAppiumServerUrl(appiumServerUrl);

        String appiumCmd = new OSCommand(List.of("appium")).searchInWindowsPath("appium");
        Process processNotepad = OSCommand.executeCommand(new String[] {"notepad"});
        Process process = OSCommand.executeCommand(new String[] {"cmd", "/C", "start", "/MIN", "cmd", "/C", appiumCmd,  "--port=" + appiumPort, "--log=d:/appium.log", "--log-level=debug:debug"});
        try {

            WebDriver driver = WebUIDriver.getWebDriver(true);
            driver.findElement(By.className("Edit")).sendKeys("hello notepad");
            Assert.assertEquals(driver.findElement(By.className("Edit")).getText(), "hello notepad");

        } finally {
            process.destroyForcibly();
            processNotepad.destroyForcibly();
            OSUtilityFactory.getInstance().killProcessByName("node", true);
        }
    }
}
