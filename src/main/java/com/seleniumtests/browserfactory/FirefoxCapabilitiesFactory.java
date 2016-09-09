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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.browserfactory.customprofile.FireFoxProfileMarker;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.util.FileUtility;

public class FirefoxCapabilitiesFactory extends ICapabilitiesFactory {
    private static boolean isProfileCreated = false;
    private static Object lockProfile = new Object();

    protected void configProfile(final FirefoxProfile profile, final DriverConfig webDriverConfig) {
        profile.setAcceptUntrustedCertificates(webDriverConfig.isSetAcceptUntrustedCertificates());
        profile.setAssumeUntrustedCertificateIssuer(webDriverConfig.isSetAssumeUntrustedCertificateIssuer());

        if (webDriverConfig.getFirefoxBinPath() != null) {
            System.setProperty("webdriver.firefox.bin", webDriverConfig.getFirefoxBinPath());
        }

        if (webDriverConfig.getUserAgentOverride() != null) {
            profile.setPreference("general.useragent.override", webDriverConfig.getUserAgentOverride());
        }

        if (webDriverConfig.getNtlmAuthTrustedUris() != null) {
            profile.setPreference("network.automatic-ntlm-auth.trusted-uris", webDriverConfig.getNtlmAuthTrustedUris());
        }

        if (webDriverConfig.getBrowserDownloadDir() != null) {
            profile.setPreference("browser.download.dir", webDriverConfig.getBrowserDownloadDir());
            profile.setPreference("browser.download.folderList", 2);
            profile.setPreference("browser.download.manager.showWhenStarting", false);
            profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
                "application/octet-stream,text/plain,application/pdf,application/zip,text/csv,text/html");
        }

        // fix permission denied issues
        profile.setPreference("capability.policy.default.Window.QueryInterface", "allAccess");
        profile.setPreference("capability.policy.default.Window.frameElement.get", "allAccess");
        profile.setPreference("capability.policy.default.HTMLDocument.compatMode.get", "allAccess");
        profile.setPreference("capability.policy.default.Document.compatMode.get", "allAccess");
        profile.setEnableNativeEvents(false);
        profile.setPreference("dom.max_chrome_script_run_time", 0);
        profile.setPreference("dom.max_script_run_time", 0);
    }

    /**
     * Create firefox capabilities.
     */
    @Override
    public DesiredCapabilities createCapabilities(final DriverConfig webDriverConfig) {
        DesiredCapabilities capability;
        capability = new DesiredCapabilities();
        capability.setBrowserName(DesiredCapabilities.firefox().getBrowserName());

        FirefoxProfile profile = getFirefoxProfile(webDriverConfig);
        configProfile(profile, webDriverConfig);
        capability.setCapability(FirefoxDriver.PROFILE, profile);

        if (webDriverConfig.isEnableJavascript()) {
            capability.setJavascriptEnabled(true);
        } else {
            capability.setJavascriptEnabled(false);
        }

        capability.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
        capability.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);

        if (webDriverConfig.getBrowserVersion() != null) {
            capability.setVersion(webDriverConfig.getBrowserVersion());
        }

        if (webDriverConfig.getWebPlatform() != null) {
            capability.setPlatform(webDriverConfig.getWebPlatform());
        }

        capability.setCapability(CapabilityType.PROXY, webDriverConfig.getProxy());

        return capability;
    }

    protected FirefoxProfile createFirefoxProfile(final String path) {
        if (path != null) {
            return new FirefoxProfile(new File(path));
        } else {
            return new FirefoxProfile();
        }
    }

    /**
     * extractDefaultProfile to a folder.
     *
     * @param   profilePath  The folder to store the profile
     *
     * @throws  IOException	 when profile file is not found
     */
    protected void extractDefaultProfile(final String profilePath) throws IOException {
        synchronized (lockProfile) {
            try {
                if (!isProfileCreated) {
                    logger.info("start create profile");
                    FileUtils.deleteDirectory(new File(profilePath));
                    FileUtility.extractJar(profilePath, FireFoxProfileMarker.class);
                }
            } catch (Exception ex) {
            	logger.error(ex);
            }
            isProfileCreated = true;
        }

        
    }

    protected synchronized FirefoxProfile getFirefoxProfile(final DriverConfig webDriverConfig) {
        String path = webDriverConfig.getFirefoxProfilePath();
        FirefoxProfile profile;
        String realPath;
        if (webDriverConfig.isUseFirefoxDefaultProfile()) {
            realPath = getFirefoxProfilePath(path);
        } else {
            realPath = null;
        }

        profile = createFirefoxProfile(realPath);
        return profile;
    }

    protected String getFirefoxProfilePath(String path) {
        String realPath;
        
        if (path == null) {
        	try {
                String profilePath = this.getClass().getResource("/").getPath() + "ffprofile";
                profilePath = FileUtility.decodePath(profilePath);

                extractDefaultProfile(profilePath);
                realPath = profilePath +  "/profiles/customProfileDirCUSTFF";

            } catch (Exception e) {
            	logger.error(e);
                realPath = null;
            }
        } else {
        	realPath = path;
        	if (!new File(path).exists()) {
        		TestLogging.log("Firefox profile path:" + path + " not found, use default");
        	}
        }

        logger.info("Firefox Profile: " + realPath);
        return realPath;
    }
}
