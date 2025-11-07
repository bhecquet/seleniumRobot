/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.connectors.selenium;

import com.seleniumtests.browserfactory.BrowserStackCapabilitiesFactory;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.util.logging.DebugMode;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import kong.unirest.core.*;
import kong.unirest.core.json.JSONException;
import kong.unirest.core.json.JSONObject;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;


/***
 * Class representing a connector to a browserstack grid node
 * A single scenario will always execute on the same node even if multiple browsers are requested. They will always be created on that node
 */
public class SeleniumBrowserstackGridConnector extends SeleniumGridConnector {

	protected static Logger logger = SeleniumRobotLogger.getLogger(SeleniumBrowserstackGridConnector.class);
	private static final String BROWSERSTACK_UPLOAD_URL = "https://api-cloud.browserstack.com/app-automate/upload";

	private String username;
	private String key;
	private String videoUrl;
	private String appiumLogsUrl;
	private String harLogsUrl;
	private String seleniumLogsUrl;
	private String logsUrl;

	public SeleniumBrowserstackGridConnector(URL url) {
		super(url.toString());

		String authenticationString = BrowserStackCapabilitiesFactory.getAuthenticationString(url.toString());

		this.username = authenticationString.split(":")[0];
		this.key = authenticationString.split(":")[1];
		videoUrl = null;
		appiumLogsUrl = null;
		harLogsUrl = null;
		seleniumLogsUrl = null;
		logsUrl = null;
	}

	private void configureProxy(UnirestInstance unirest) {
		String proxyHost = System.getProperty("https.proxyHost");
		String proxyPort = System.getProperty("https.proxyPort");
		if (proxyHost != null && proxyPort != null) {
			unirest.config().proxy(proxyHost, Integer.parseInt(proxyPort));
		}
	}

	/**
	 * Call API to see if browserstack is reachable
	 * @return true if browserstack is ready
	 */
	@Override
	public boolean isGridActive() {
		try (UnirestInstance unirest = Unirest.spawnInstance()) {
			configureProxy(unirest);

			HttpResponse<JsonNode> response = unirest.get("https://api.browserstack.com/automate/projects.json").basicAuth(username, key).asJson();
			return response.isSuccess();
		} catch (UnirestException e) {
			return false;
		}
	}

	/**
	 * Upload mobile application to browserstack
	 */
	@Override
	public MutableCapabilities uploadMobileApp(Capabilities caps) {
		MutableCapabilities capabilities = new MutableCapabilities(caps);

		Optional<String> applicationOption = getApp(caps);
		if (applicationOption.isEmpty()) {
			return capabilities;
		}

		// extract user name and password from getWebDriverGrid
		String authenticationString = BrowserStackCapabilitiesFactory.getAuthenticationString(SeleniumTestsContextManager.getThreadContext().getWebDriverGrid().get(0));
		String user = authenticationString.split(":")[0];
		String key = authenticationString.split(":")[1];

		try (UnirestInstance unirest = Unirest.spawnInstance();){

			String proxyHost = System.getProperty("https.proxyHost");
			String proxyPort = System.getProperty("https.proxyPort");
			if (proxyHost != null && proxyPort != null) {
				unirest.config().proxy(proxyHost, Integer.parseInt(proxyPort));
			}

			HttpResponse<JsonNode> jsonResponse = unirest.post(BROWSERSTACK_UPLOAD_URL)
					.basicAuth(user, key)
					.field("file", new File(applicationOption.get()))
					.asJson()
					.ifFailure(response -> {
						throw new ConfigurationException(String.format("Application file upload failed: %s", response.getStatusText()));
					})
					.ifSuccess(response -> logger.info("Application successfuly uploaded to Saucelabs"));
			capabilities = setApp(capabilities, jsonResponse.getBody().getObject().getString("app_url"));

		} catch (UnirestException e) {
			throw new ConfigurationException("Application file upload failed: " + e.getMessage());
		}

		return capabilities;
	}

	@Override
	public void startVideoCapture() {
		// started by default
	}

	@Override
	public File stopVideoCapture(String outputFile) {

		logger.info("stopping capture");
		try (UnirestInstance unirest = Unirest.spawnInstance();
			 ExecutorService executor = Executors.newSingleThreadExecutor()) {

			configureProxy(unirest);

			// delete file if it exists as '.asFile()' will not overwrite it
			deleteExistingVideo(outputFile);

			long start = System.currentTimeMillis();

			Future<File> future = executor.submit(() -> {
				GetRequest getRequest = unirest.get(videoUrl).basicAuth(username, key);
				if (SeleniumTestsContextManager.getGlobalContext().getDebug().contains(DebugMode.NETWORK)) {
					getRequest = getRequest
							.downloadMonitor((b, fileName, bytesWritten, totalBytes) -> logger.info("File {}: {}/{}", fileName, bytesWritten, totalBytes));
				}
				HttpResponse<File> videoResponse = getRequest
						.requestTimeout(60000)
						.asFile(outputFile);

				if (videoResponse.getStatus() != 200) {
					logger.error("stop video capture error: {}", videoResponse.getBody());
					return null;
				} else {
					logger.info("Video file downloaded ({} kb in {} ms)", videoResponse.getBody().length() / 1000, System.currentTimeMillis() - start);

					// Browserstack provides mp4 file
					File videoFile = videoResponse.getBody();
					if (videoFile.exists()) {
						File renamedVideoFile = new File(FilenameUtils.removeExtension(videoFile.getAbsolutePath()) + ".mp4");
						videoFile.renameTo(renamedVideoFile);
						videoFile = renamedVideoFile;
					}

					return videoFile;
				}
			});
			File videoFile = future.get(60, TimeUnit.SECONDS);
			future.cancel(true);
			return videoFile;

		} catch (UnirestException e) {
			logger.warn("Could not stop video capture: {}", e.getMessage());
			return null;
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			logger.warn("Video file not get due to {}", e.getClass().getName());
			return null;
		}
	}
	
	/**
	 * Retrieves session information about the created driver
	 */
	@Override
	public void getSessionInformationFromGrid(RemoteWebDriver driver, long driverCreationDuration) {


		// get information from browserstack
		String buildId = getCurrentBuildId(driver);
		getCurrentSession(driver, buildId);
		String browserName = driver.getCapabilities().getBrowserName();
		String version = driver.getCapabilities().getBrowserVersion();

		if (sessionId == null) {
			setSessionId(driver.getSessionId());
		}

		// store some information about driver creation
		MutableCapabilities caps = (MutableCapabilities)driver.getCapabilities();
		caps.setCapability(SeleniumRobotCapabilityType.GRID_HUB, hubUrl);
		caps.setCapability(SeleniumRobotCapabilityType.SESSION_ID, sessionId); // store the scenario session (sessionID of the first created browser to group browsers in logs

		// log will display the actual driver session ID and node URL so that it reflects the real driver creation (whereas nodeUrl and sessionId variables only store the first driver information for the test)
		logger.info(String.format("Browser %s (%s) created in %.1f secs on browserstack  with session %s", browserName, version, driverCreationDuration / 1000.0, driver.getSessionId()).replace(",", "."));
	}


	/**
	 * Returns Build ID from the unique name of build
	 */
	private String getCurrentBuildId(RemoteWebDriver driver) {
		try (UnirestInstance unirest = Unirest.spawnInstance()) {
			configureProxy(unirest);

			MutableCapabilities caps = (MutableCapabilities) driver.getCapabilities();
			HttpResponse<JsonNode> response = unirest.get("https://api.browserstack.com/automate/builds.json").basicAuth(username, key).asJson();
			for (JSONObject build: (List<JSONObject>)response.getBody().getArray().toList()) {
				JSONObject automationBuild = build.getJSONObject("automation_build");
				if (automationBuild.getString("name").equals(caps.getCapability(SeleniumRobotCapabilityType.GLOBAL_SESSION_ID))) {
					return automationBuild.getString("hashed_id");
				}
			}

			throw new ScenarioException("Could not find build ID");
		} catch (UnirestException | JSONException e) {
			throw new ScenarioException("Couldn't get build id", e);
		}
	}

	/**
	 * Returns the test session
	 */
	private void getCurrentSession(RemoteWebDriver driver, String buildId) {
		try (UnirestInstance unirest = Unirest.spawnInstance()) {
			configureProxy(unirest);

			MutableCapabilities caps = (MutableCapabilities) driver.getCapabilities();
			HttpResponse<JsonNode> response = unirest.get(String.format("https://api.browserstack.com/automate/builds/%s/sessions.json?status=running", buildId)).basicAuth(username, key).asJson();
			for (JSONObject build: (List<JSONObject>)response.getBody().getArray().toList()) {
				JSONObject automationSession = build.getJSONObject("automation_session");
				if (automationSession.getString("name").equals(caps.getCapability(SeleniumRobotCapabilityType.TEST_ID))) {
					logsUrl = automationSession.getString("logs");
					appiumLogsUrl = automationSession.optString("appium_logs_url", null);
					videoUrl = automationSession.getString("video_url");
					harLogsUrl = automationSession.optString("har_logs_url", null);
					seleniumLogsUrl = automationSession.getString("selenium_logs_url");
					return;
				}
			}

			throw new ScenarioException("Could not find session");
		} catch (UnirestException | JSONException e) {
			throw new ScenarioException("Couldn't find session", e);
		}
	}

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		SeleniumBrowserstackGridConnector.logger = logger;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public String getAppiumLogsUrl() {
		return appiumLogsUrl;
	}

	public String getHarLogsUrl() {
		return harLogsUrl;
	}

	public String getSeleniumLogsUrl() {
		return seleniumLogsUrl;
	}

	public String getLogsUrl() {
		return logsUrl;
	}
}
