package com.seleniumtests.browserfactory;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.util.StringUtility;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chromium.ChromiumOptions;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ChromiumCapabilitiesFactory extends IDesktopCapabilityFactory {

    protected static final String USER_DATA_DIR_OPTION = "--user-data-dir=";
    public static final String IGNORE = "ignore";


    protected ChromiumCapabilitiesFactory(DriverConfig webDriverConfig) {
        super(webDriverConfig);
    }

    /**
     * Returns the chrome / edge startup options defined by user
     */
    protected abstract String getUserStartupOptions();

    protected abstract String getChromiumProfilePath();

    protected abstract String getChromiumProfileCapabilityName();

    /**
     * Set capabilities that are common to all chromium browsers
     * @param options   the options / capabilities to complete
     */
    protected void addChromiumDriverOptions(ChromiumOptions<?> options) {
        if (webDriverConfig.getUserAgentOverride() != null) {
            // ISSUE #705 - In order to give the maximum of data available to customize the User Agent,
            // we need to pass the testName which is not in the context at this moment
            String testName = "";
            try {
                testName = TestNGResultUtils.getVisualTestName(webDriverConfig.getTestContext().getTestNGResult());
            } catch (Exception e) {
                testName = TestNGResultUtils.getTestName(webDriverConfig.getTestContext().getTestNGResult());
            }
            webDriverConfig.getTestContext().setAttribute(SeleniumTestsContext.TEST_NAME, testName);
            options.addArguments("--user-agent=" + StringUtility.interpolateString(webDriverConfig.getUserAgentOverride(), webDriverConfig.getTestContext()));
        }


        List<String> startupOptions = new ArrayList<>(List.of("--disable-translate",
                "--disable-web-security",
                "--no-sandbox",
                "--disable-site-isolation-trials",
                // https://github.com/GoogleChrome/chrome-launcher/blob/main/docs/chrome-flags-for-tools.md
                "--disable-search-engine-choice-screen",
                // list of features: https://chromium.googlesource.com/chromium/src/+/refs/heads/main/chrome/common/chrome_features.cc
                // https://gist.github.com/rihardn/47b8e6170dc8f57a998c90b12a3e01bb
                "--disable-features=IsolateOrigins,site-per-process,PrivacySandboxSettings4,HttpsUpgrades",
                // workaround for https://github.com/SeleniumHQ/selenium/issues/11750 on chrome >= 111
                "--remote-allow-origins=*"));


        if (webDriverConfig.isHeadlessBrowser()) {
            logger.info("setting chrome in headless mode");
            startupOptions.add("--headless");
            startupOptions.add("--window-size=1280,1024");
            startupOptions.add("--disable-gpu");
        }

        List<String> excludeSwitches = new ArrayList<>();
        if (BrowserInfo.DEFAULT_BROWSER_PRODFILE.equals(getChromiumProfilePath())) {
            excludeSwitches.add("disable-background-networking");
        }

        if (getUserStartupOptions() != null) {
            for (String option: getUserStartupOptions().split(" ")) {
                if ("++enable-automation".equals(option.trim())) {
                    // remove option "--enable-automation" as, from chrome 132, it blocks tests that attach a new chrome tab to the current chrome process (https://issues.chromium.org/issues/371112535)
                    excludeSwitches.add("enable-automation");
                } else	if (option.startsWith("++")) {
                    startupOptions.remove(option.replace("++", "--"));
                } else {
                    startupOptions.add(option);
                }
            }
        }

        if (!excludeSwitches.isEmpty()) {
            options.setExperimentalOption("excludeSwitches", excludeSwitches);
        }
        options.addArguments(startupOptions);
        options.setPageLoadStrategy(webDriverConfig.getPageLoadStrategy());
    }

    protected void enableBidi(ChromiumOptions<?> options) {

        // https://github.com/SeleniumHQ/selenium/issues/16230
        options.setCapability("webSocketUrl", true);
        Map<String, String> userPromptHandler = Map.of(
                "alert", IGNORE,
                "beforeUnload", IGNORE,
                "confirm", IGNORE,
                "default", IGNORE,
                "file", IGNORE,
                "prompt", IGNORE);
        options.setCapability(
                "unhandledPromptBehavior",
                userPromptHandler);
    }

    /**
     * Used in local mode only
     * @param options	driver options / capabilities
     */
    @Override
    protected void updateOptionsWithSelectedBrowserInfo(MutableCapabilities options) {
        ((ChromiumOptions<?>)options).setBinary(selectedBrowserInfo.getPath());
        String profilePath = getChromiumProfilePath();

        if (profilePath != null) {
            if (!BrowserInfo.DEFAULT_BROWSER_PRODFILE.equals(profilePath) && (profilePath.contains("/") || profilePath.contains("\\"))) {
                ((ChromiumOptions<?>)options).addArguments(USER_DATA_DIR_OPTION + profilePath); // e.g: C:\\Users\\MyUser\\AppData\\Local\\Google\\Chrome\\User Data
            } else if (BrowserInfo.DEFAULT_BROWSER_PRODFILE.equals(profilePath)) {
                Path tempProfile = copyDefaultProfile(selectedBrowserInfo);
                ((ChromiumOptions<?>)options).addArguments(USER_DATA_DIR_OPTION + tempProfile);
            } else {
                logger.warn("{} profile {} could not be set", getBrowserType(), profilePath);
            }
        }
    }


    @Override
    protected void updateGridOptionsWithSelectedBrowserInfo(MutableCapabilities options) {
        String profilePath = getChromiumProfilePath();
        if (profilePath != null) {
            if (!BrowserInfo.DEFAULT_BROWSER_PRODFILE.equals(profilePath) && (profilePath.contains("/") || profilePath.contains("\\"))) {
                ((ChromiumOptions<?>)options).addArguments(USER_DATA_DIR_OPTION + profilePath); // e.g: C:\\Users\\MyUser\\AppData\\Local\\Google\\Chrome\\User Data
            } else if (BrowserInfo.DEFAULT_BROWSER_PRODFILE.equals(profilePath)) {
                options.setCapability(getChromiumProfileCapabilityName(), BrowserInfo.DEFAULT_BROWSER_PRODFILE);
            } else {
                logger.warn("{} profile {} could not be set", getBrowserType(), profilePath);
            }
        }
    }
}
