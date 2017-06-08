/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.browserfactory;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;

public class FirefoxDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {
    private long timeout = 1;
    private static boolean marionetteMode = false;

    /**
     * @param  cfg  the configuration of the firefoxDriver
     */
    public FirefoxDriverFactory(final DriverConfig cfg) {
        super(cfg);
    }
    
    /**
     * below firefox 47, we use FirefoxDriver
     * above, we use Marionette, get version of firefox
     * By default, use Marionette
     * @return
     */
    public boolean useFirefoxDriver() {
    	String output = OSUtility.getInstalledBrowsersWithVersion().get(BrowserType.FIREFOX).getVersion();
		return useFirefoxVersion(output);
    }
    
    /**
     * use firefox if version is below 47
     * @param versionString
     * @return
     */
    public boolean useFirefoxVersion(String versionString) {
    	Pattern regMozilla = Pattern.compile(".*?(\\d+)\\..*");
    	Matcher versionMatcher = regMozilla.matcher(versionString);
		if (versionMatcher.matches()) {
			String version = versionMatcher.group(1);
			if (Integer.parseInt(version) < 48) {
				return true;
			}
		} 
		return false;
    }

    /**
     * create native driver instance, designed for unit testing.
     *
     * @return	the driver
     */
    @Override
    protected WebDriver createNativeDriver() {
    	
    	if (useFirefoxDriver()) {
    		return new FirefoxDriver(new FirefoxCapabilitiesFactory().createCapabilities(webDriverConfig));
    	} else {
    		marionetteMode = true;
    		return new FirefoxDriver(new MarionetteCapabilitiesFactory().createCapabilities(webDriverConfig));
    	}  
    }

    @Override
    public WebDriver createWebDriver() {
        DriverConfig cfg = this.getWebDriverConfig();
        driver = createWebDriverWithTimeout();

        // Implicit Waits to handle dynamic element. The default value is 5
        // seconds.
        setImplicitWaitTimeout(cfg.getImplicitWaitTimeout());
        if (cfg.getPageLoadTimeout() >= 0) {
            setPageLoadTimeout(cfg.getPageLoadTimeout());
        }

        this.setWebDriver(driver);
        return driver;
    }

    /**
     * Create webDriver, capture socket customexception and retry with timeout.
     *
     * @return  WebDriver
     */
    protected WebDriver createWebDriverWithTimeout() {
        long time = 0;
        while (time < getTimeout()) {
            try {
                driver = createNativeDriver();
                return driver;
            } catch (WebDriverException ex) {
                if (ex.getMessage().contains("SocketException")
                        || ex.getMessage().contains("Failed to connect to binary FirefoxBinary")
                        || ex.getMessage().contains("Unable to bind to locking port 7054 within 45000 ms")) {
                    WaitHelper.waitForSeconds(1);

                    time++;
                } else {
                    throw new DriverExceptions(ex.getMessage());
                }
            }
        }

        throw new DriverExceptions("Got customexception when creating webDriver with socket timeout 1 minute");
    }

    /**
     * Method designed for shorten timeout in unit testing.
     *
     * @return  timeout
     */
    protected long getTimeout() {
        return timeout;
    }

    @Override
    protected void setPageLoadTimeout(final long timeout) {
        driver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.SECONDS);
    }

	public static boolean isMarionetteMode() {
		return marionetteMode;
	}
}
