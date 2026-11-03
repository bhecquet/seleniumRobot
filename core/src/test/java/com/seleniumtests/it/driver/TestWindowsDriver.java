package com.seleniumtests.it.driver;

import com.seleniumtests.CaptureVideo;
import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.uipage.ByC;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
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

@CaptureVideo
public class TestWindowsDriver extends GenericTest {

    private static final int DRIVER_INIT_RETRIES = 4;
    private static final int ELEMENT_FIND_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 1000;
	
    @BeforeClass(groups = "it")
    public void init() {
        if (!OSUtility.isWindows()) {
            throw new SkipException("Test only runs on Windows");
        }

        String out = OSCommand.executeCommandAndWait(new String[] {"_USE_PATH_appium",  "driver", "list"});

        // appium driver list returns
        // √ Listing available drivers
        //- novawindows@1.4.3 [installed (npm)]
        //- uiautomator2 [not installed]
        //- xcuitest [not installed]
        if (!out.replace("\n", "").replaceAll("\u001B\\[[;\\d]*m", "").matches(".*novawindows\\@.*?\\d \\[installed \\(npm\\)\\].*")) {
            throw new SkipException("NovaWindows driver not installed");
        }

        if (System.getenv("APPIUM_NOVAWINDOWS_PATH") == null) {
            throw new SkipException("APPIUM_NOVAWINDOWS_PATH not set => path to the NovaWindows driver)");
        }

        if (!new File(System.getenv("APPIUM_NOVAWINDOWS_PATH")).exists()) {
            throw new SkipException("NovaWindows driver cannot be found in " + System.getenv("APPIUM_NOVAWINDOWS_PATH"));
        }
    }

    /**
     * Test that it's possible to execute a test on notepad on windows
     * This test requires appium to be available with appium-novawindows-driver
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
            new HtmlElement("", By.className("RichEditD2DPT")).sendKeys("hello from HtmlElement");
            Assert.assertEquals(driver.findElement(By.className("RichEditD2DPT")).getText(), "hello from HtmlElement");

        } finally {
            process.destroyForcibly();
            closeBrowser();
            OSUtilityFactory.getInstance().killProcessByName("Notepad", true);
            OSUtilityFactory.getInstance().killProcessByName("node", true);
        }
    }

    @Test(groups={"it"})
    public void testNotepadAutomationAlreadyStarted() {

        long appiumPort = 4723 + Math.round(Math.random() * 1000);
        String appiumServerUrl = String.format("http://localhost:%d/", appiumPort);

        SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_WINDOWS);

        if ("fr".equals(System.getProperty("user.language"))) {
            SeleniumTestsContextManager.getThreadContext().setAppActivity(".*Bloc-notes");
        } else {
            SeleniumTestsContextManager.getThreadContext().setAppActivity(".*Notepad");
        }
        SeleniumTestsContextManager.getThreadContext().setAppiumServerUrl(appiumServerUrl);

        String appiumCmd = new OSCommand(List.of("appium")).searchInWindowsPath("appium");
        Process processNotepad = OSCommand.executeCommand(new String[] {"notepad"});
        Process process = OSCommand.executeCommand(new String[] {"cmd", "/C", "start", "/MIN", "cmd", "/C", appiumCmd,  "--port=" + appiumPort, "--log=d:/appium.log", "--log-level=debug:debug"});
        try {

            WebDriver driver = WebUIDriver.getWebDriver(true);
            driver.findElement(By.className("RichEditD2DPT")).sendKeys("hello notepad");
            Assert.assertEquals(driver.findElement(By.className("RichEditD2DPT")).getText(), "hello notepad");

        } finally {
            process.destroyForcibly();
            processNotepad.destroyForcibly();
            OSUtilityFactory.getInstance().killProcessByName("Notepad", true);
            OSUtilityFactory.getInstance().killProcessByName("node", true);
        }
    }
    

    @Test(groups = {"it"})
    public void testCalculatorAutomationAlreadyStarted() {

        long appiumPort = 4723 + Math.round(Math.random() * 1000);
        String appiumServerUrl = String.format("http://localhost:%d/", appiumPort);

        SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_WINDOWS);
        SeleniumTestsContextManager.getThreadContext().setAppiumServerUrl(appiumServerUrl);

        String appiumCmd = new OSCommand(List.of("appium")).searchInWindowsPath("appium");
        Process processNotepad = OSCommand.executeCommand(new String[]{"calc"});
        String notepadTitleRegex = "fr".equals(System.getProperty("user.language")) ? ".*Calculatrice.*" : ".*Calculator.*";
        SeleniumTestsContextManager.getThreadContext().setAppActivity(notepadTitleRegex);

        Process process = OSCommand.executeCommand(new String[]{"cmd", "/C", "start", "/MIN", "cmd", "/C", appiumCmd, "--port=" + appiumPort, "--log=d:/appium.log", "--log-level=debug:debug"});

        try {
            WebDriver driver = WebUIDriver.getWebDriver(true);
            driver.findElement(ByC.attribute("AutomationId", "num7Button")).click();
            driver.findElement(ByC.attribute("AutomationId", "multiplyButton")).click();
            driver.findElement(ByC.attribute("AutomationId", "num3Button")).click();
            driver.findElement(ByC.attribute("AutomationId", "equalButton")).click();
            Assert.assertEquals(driver.findElement(ByC.attribute("AutomationId", "CalculatorResults")).getText(), "L’affichage est 21");

        } finally {
            process.destroyForcibly();
            processNotepad.destroyForcibly();
            OSUtilityFactory.getInstance().killProcessByName("CalculatorApp", true);
            OSUtilityFactory.getInstance().killProcessByName("node", true);
        }
    }

}
