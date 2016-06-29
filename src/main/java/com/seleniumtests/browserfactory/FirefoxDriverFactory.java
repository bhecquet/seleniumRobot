/*
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seleniumtests.browserfactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.MarionetteDriver;

import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.util.helper.WaitHelper;

public class FirefoxDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {
    private long timeout = 60;

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
    	
    	
    	try {
	    	Process p = Runtime.getRuntime().exec("firefox -v | more");
			BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			StringBuilder output = new StringBuilder();
			while ((line = bri.readLine()) != null) {
			    System.out.println(line);
			    output.append(line);
			}
			bri.close();
			p.waitFor();
			
			return useFirefoxVersion(output);
			
    	} catch (IOException | InterruptedException e) {
    		return false;
    	}
    }
    
    /**
     * use firefox if version is below 47
     * @param versionString
     * @return
     */
    public boolean useFirefoxVersion(StringBuilder versionString) {
    	Pattern regMozilla = Pattern.compile("^Mozilla .* (\\d+)\\..*");
    	Matcher versionMatcher = regMozilla.matcher(versionString);
		if (versionMatcher.matches()) {
			String version = versionMatcher.group(1);
			if (Integer.parseInt(version) >= 47) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
    }

    /**
     * create native driver instance, designed for unit testing.
     *
     * @return	the driver
     */
    protected WebDriver createNativeDriver() {
    	
    	if (useFirefoxDriver()) {
    		return new FirefoxDriver(new FirefoxCapabilitiesFactory().createCapabilities(webDriverConfig));
    	} else {
    		return new MarionetteDriver(new MarionetteCapabilitiesFactory().createCapabilities(webDriverConfig));
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
     * It's designed for shorten tiemout in unit testing.
     *
     * @return  timeout
     */
    protected long getTimeout() {
        return timeout;
    }

    protected void setPageLoadTimeout(final long timeout) {
        driver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.SECONDS);
    }
}
