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
package com.seleniumtests.browserfactory.mobile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSCommand;
import com.vdurmont.semver4j.Semver;

public class LocalAppiumLauncher implements AppiumLauncher {

	private String appiumVersion;
	private String appiumHome;
	private String nodeVersion;
	private String nodeCommand;
	private Process appiumProcess;
	private long appiumPort;
	private String logFile = null;
	private String optionString = "";

	private static Logger logger = SeleniumRobotLogger.getLogger(LocalAppiumLauncher.class);
	
	public LocalAppiumLauncher() {
		this(null);
	}

	public LocalAppiumLauncher(String logDirectory) {

		if (logDirectory != null) {
			new File(logDirectory).mkdirs();
			if (new File(logDirectory).isDirectory()) {
				logFile = Paths.get(logDirectory, "appium.log").toString();
			}
		}
		
		checkInstallation();
		generateOptions();
		appiumPort = 4723 + Math.round(Math.random() * 1000);
	}
	
	public Process getAppiumProcess() {
		return appiumProcess;
	}

	public String getNodeVersion() {
		return nodeVersion;
	}
	
	public long getAppiumPort() {
		return appiumPort;
	}
	
	public void setAppiumPort(long appiumPort) {
		this.appiumPort = appiumPort;
	}
	
	/**
	 * Method for generating options passed to appium (e.g: logging)
	 */
	private void generateOptions() {
		if (logFile != null) {
			optionString += String.format(" --log %s", logFile);
		}
	}

	private void checkAppiumVersion() {
		try {
			File packageFile = Paths.get(appiumHome, "node_modules", "appium", "package.json").toFile();
			String appiumConfig = FileUtils.readFileToString(packageFile);
			JSONObject packages = new JSONObject(appiumConfig);
			if (!packages.getString("name").equals("appium")) {
				throw new ConfigurationException(String.format("package.json file found in %s is not for appium, check path", packageFile.getAbsolutePath()));
			}
			
			appiumVersion = packages.getString("version");

		} catch (IOException e) {
			throw new ConfigurationException("File package.json not found, appium does not seem to be installed in " + appiumHome, e);
		}
	}

	/**
	 * Check that node and appium are installed
	 */
	private void checkInstallation() {
		appiumHome = System.getenv("APPIUM_HOME");
		if (appiumHome != null) {
			if (Paths.get(appiumHome, "node").toFile().exists()
					|| Paths.get(appiumHome, "node.exe").toFile().exists()) {
				nodeCommand = Paths.get(appiumHome, "node").toString();
			} else {
				nodeCommand = "node";
			}
		} else {
			throw new ConfigurationException("APPIUM_HOME environment variable not set");
		}
		
		// get appium version
		checkAppiumVersion();
		
		// get version for node
		String reply = OSCommand.executeCommandAndWait(nodeCommand + " -v").trim();
		if (!reply.matches("v\\d+\\.\\d+.*")) {
			throw new ConfigurationException("Node does not seem to be installed, is environment variable APPIUM_HOME set ?");
		} else {
			nodeVersion = reply;
		}
	}
	
	/**
	 * Call /wd/hub/sessions to see if appium is started
	 */
	private void waitAppiumAlive() {
		
		for (int i=0; i< 10; i++) {
			try {
				HttpGet request = new HttpGet(getAppiumServerUrl() + "sessions");
		        CloseableHttpClient client = HttpClients.createDefault();
		        CloseableHttpResponse response = client.execute(request);
		        client.close();
		        if (response.getStatusLine().getStatusCode() == 200) {
		        	break;
		        }
			} catch (IOException e) {
				logger.info("appium not started");
			}
			WaitHelper.waitForSeconds(1);
		}
	}
	
	/**
	 * Returns the local appium URL
	 * @return
	 */
	public String getAppiumServerUrl() {
		return String.format("http://localhost:%d/wd/hub/", appiumPort);
	}
	
	public void startAppiumWithWait() {
		startAppiumWithoutWait();
		
		// wait for startup
		waitAppiumAlive();
	}
	
	public void startAppiumWithoutWait() {
		Semver appiumVers = new Semver(appiumVersion);
		if (appiumVers.isGreaterThan("1.6.0") || appiumVers.isEqualTo("1.6.0")) {
			appiumProcess = OSCommand.executeCommand(String.format("%s %s/node_modules/appium/ --port %d %s", 
					nodeCommand, 
					appiumHome, 
					appiumPort,
					optionString));
		} else {
			appiumProcess = OSCommand.executeCommand(String.format("%s %s/node_modules/appium/bin/appium.js --port %d %s", 
										nodeCommand, 
										appiumHome, 
										appiumPort,
										optionString));
		}
	}
	
	/**
	 * Start appium process
	 */
	@Override
	public void startAppium() {
		startAppiumWithWait();
	}

	/**
	 * Stops appium process if it has been started, else, raise a ScenarioException
	 */
	@Override
	public void stopAppium() {
		if (appiumProcess == null) {
			throw new ScenarioException("Appium process has never been started");
		}
		appiumProcess.destroy();
		
	}
	
	public String getAppiumVersion() {
		return appiumVersion;
	}

}
