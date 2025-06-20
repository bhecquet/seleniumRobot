/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
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
package com.seleniumtests.connectors.selenium;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.util.helper.WaitHelper;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.io.Zip;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

/***
 * Class representing a connector to a grid node
 * A single scenario will always execute on the same node even if multiple browsers are requested. They will always be created on that node
 */
public class SeleniumGridConnector implements ISeleniumGridConnector {

	protected URL hubUrl;
	protected String hubHost;
	protected int hubPort;
	protected SessionId sessionId;
	protected String nodeUrl;
	protected String nodeHost;
	
	public static final String CONSOLE_SERVLET = "/ui/";
	public static final String STATUS_SERVLET = "/status";
	protected static Logger logger = SeleniumRobotLogger.getLogger(SeleniumGridConnector.class);
	
	public SeleniumGridConnector(String url) {
		setHubUrl(url);
	}
	
	protected void setHubUrl(String hubUrl) {
		try {
			this.hubUrl = new URL(hubUrl);
		} catch (MalformedURLException e1) {
			throw new ConfigurationException(String.format("Hub url '%s' is invalid: %s", hubUrl, e1.getMessage()));
		}
		hubHost = this.hubUrl.getHost();
        hubPort = this.hubUrl.getPort();
	}
	
	/**
	 * Do nothing as we are not a SeleniumRobotGrid
	 * @param caps
	 */
	public MutableCapabilities uploadMobileApp(Capabilities caps) {
		logger.warn("application upload is only available with seleniumRobot grid");
		return (MutableCapabilities) caps;
	}
	
	/**
	 * Upload a file given file path
	 * @param filePath
	 */
	public void uploadFile(String filePath) {
		logger.warn("file upload is only available with seleniumRobot grid");
	}
	
	/**
	 * Upload a file given file path
	 * @param filePath
	 */
	public String uploadFileToNode(String filePath, boolean returnLocalFile) {
		logger.warn("file upload is only available with seleniumRobot grid");
		return null;
	}
	
	/**
	 * Download a file given file path
	 * @param filePath
	 */
	public File downloadFileFromNode(String filePath) {
		logger.warn("file download is only available with seleniumRobot grid");
		return null;
	}

	/**
	 * Returns the list of files that are present on grid node
	 * @return
	 */
	public List<String> listFilesToDownload() {
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot download file before driver has been created and corresponding node instanciated");
		}
		try {
			JSONObject fileList = Unirest.get(String.format("http://%s:%d/session/%s/se/files", hubUrl.getHost(), hubUrl.getPort(), sessionId))
					.asJson()
					.getBody()
					.getObject();
			return fileList.getJSONObject("value").getJSONArray("names").toList();

		} catch (Exception e) {
			logger.error("Cannot get list of files to download: " + e.getMessage());
			return new ArrayList<>();
		}
	}

	/**
	 * Download file from grid node, using full name
	 * @param name
	 * @return
	 */
	public File downloadFileFromName(String name, File downloadDir) {
		try {
			JSONObject fileList = Unirest.post(String.format("http://%s:%d/session/%s/se/files", hubUrl.getHost(), hubUrl.getPort(), sessionId))
					.header("Content-Type", "application/json; charset=utf-8")
					.body(String.format("{\"name\":\"%s\"}", name))
					.asJson()
					.getBody()
					.getObject();
			JSONObject fileJson = fileList.getJSONObject("value");
			if (fileJson.has("filename")) {
				String content = fileJson.getString("contents");
				Zip.unzip(content, downloadDir);
				logger.info(String.format("File %s downloaded to %s", name, downloadDir));
				// Read the file contents
				return Optional.ofNullable(downloadDir.listFiles()).orElse(new File[]{null})[0];
			} else if (fileJson.has("message")) {
				logger.warn("Error downloading file: " + fileJson.getString("message"));
				return null;
			} else {
				logger.warn("No file found with name " + name);
				return null;
			}

		} catch (UnirestException e) {
			logger.error(String.format("Cannot download file %s: %s", name, e.getMessage()));
			return null;
		} catch (IOException e) {
			logger.error("Error reading file content: " + e.getMessage());
            return null;
        } catch (Exception e) {
			logger.error("Error downloading file: " + e.getMessage());
			return null;
		}
    }
	
	/**
	 * Kill process
	 * @param processName
	 */
	public void killProcess(String processName) {
		logger.warn("kill is only available with seleniumRobot grid");
	}
	
	/**
	 * Execute command
	 * @param program	name of the program
	 * @param args		arguments of the program
	 */
	public String executeCommand(String program, String ... args) {
		logger.warn("executeCommand is only available with seleniumRobot grid");
		return "";
	}
	/**
	 * Execute command with timeout
	 * @param program	name of the program
	 * @param timeout	if null, default timeout will be applied
	 * @param args		arguments of the program
	 */
	public String executeCommand(String program, Integer timeout, String ... args) {
		logger.warn("executeCommand is only available with seleniumRobot grid");
		return "";
	}

	/**
	 * Upload a file to a browser uplpoad window
	 * @param fileName
	 * @param base64Content
	 */
	public void uploadFileToBrowser(String fileName, String base64Content) {
		logger.warn("file upload to browser is only available with seleniumRobot grid");
	}
	
	/**
	 * Left clic on desktop at x,y
	 * @param x		x coordinate
	 * @param y		y coordinate
	 */
	public void leftClic(boolean onlyMainScreen, int x, int y) {
		logger.warn("left clic is only available with seleniumRobot grid");
	}
	
	/**
	 * Left clic on desktop at x,y
	 * @param x		x coordinate
	 * @param y		y coordinate
	 * if true, click coordinates are on the main screen
	 */
	public void leftClic(int x, int y) {
		logger.warn("left clic is only available with seleniumRobot grid");
	}
	
	public Point getMouseCoordinates() {
		logger.warn("getMouseCoordinates is only available with seleniumRobot grid");
		return new Point(0, 0);
	}
	
	/**
	 * double clic on desktop at x,y
	 * @param x		x coordinate
	 * @param y		y coordinate
	 * @param onlyMainScreen	if true, click coordinates are on the main screen
	 */
	public void doubleClick(boolean onlyMainScreen, int x, int y) {
		logger.warn("double clic is only available with seleniumRobot grid");
	}
	/**
	 * double clic on desktop at x,y
	 * @param x		x coordinate
	 * @param y		y coordinate
	 */
	public void doubleClick(int x, int y) {
		logger.warn("double clic is only available with seleniumRobot grid");
	}
	
	/**
	 * right clic on desktop at x,y
	 * @param x		x coordinate
	 * @param y		y coordinate
	 * @param onlyMainScreen	if true, click coordinates are on the main screen
	 */
	public void rightClic(boolean onlyMainScreen, int x, int y) {
		logger.warn("right clic is only available with seleniumRobot grid");
	}
	
	
	/**
	 * right clic on desktop at x,y
	 * @param x		x coordinate
	 * @param y		y coordinate
	 */
	public void rightClic(int x, int y) {
		logger.warn("right clic is only available with seleniumRobot grid");
	}
	
	/**
	 * Take screenshot of the full desktop and return a base64 string of the image
	 * @return
	 */
	public String captureDesktopToBuffer() {
		logger.warn("captureDesktopToBuffer is only available with seleniumRobot grid");
		return null;
	}
	

	/**
	 * Take screenshot of the full desktop and return a base64 string of the image
	 * @param onlyMainScreen	if true, only take screenshot of the default screen
	 * @return
	 */
	public String captureDesktopToBuffer(boolean onlyMainScreen) {
		logger.warn("captureDesktopToBuffer is only available with seleniumRobot grid");
		return null;
	}
	
	/**
	 * Send keys to desktop
	 * @param keyCodes
	 */
	public void sendKeysWithKeyboard(List<Integer> keyCodes) {
		logger.warn("send keys is only available with seleniumRobot grid");
	}
	
	public void startVideoCapture() {
		logger.warn("video capture is only available with seleniumRobot grid");
	}
	
	public File stopVideoCapture(String outputFile) {
		logger.warn("video capture is only available with seleniumRobot grid");
		return null;
	}

	public List<Integer> getProcessList(String processName) {
		logger.warn("process list is only available with seleniumRobot grid");
		return new ArrayList<>();
	}
	
	/**
	 * 
	 * @return true if grid is active. Raises an exception if it's not there anymore
	 */
	public boolean isGridActive() {
		
		HttpResponse<JsonNode> response;
		try {
			response = Unirest.get(String.format("http://%s:%s%s", hubUrl.getHost(), hubUrl.getPort(), STATUS_SERVLET)).asJson();
			if (response.getStatus() != 200) {
	    		logger.warn("Error connecting to the grid hub at " + hubUrl);
	    		return false;
	    	} 
		} catch (UnirestException e) {
			logger.warn("Cannot connect to the grid hub at " + hubUrl);
			return false;
		}
		
		try {
			JSONObject hubStatus = response.getBody().getObject();
			
			return hubStatus.getJSONObject("value").getBoolean("ready");	
		} catch (JSONException | NullPointerException e) {
			return false;
		}		
	}
	
	/**
	 * Display running step
	 * @param stepName
	 */
	public long displayRunningStep(String stepName) {
		logger.warn("displayRunningStep is only available with seleniumRobot grid");
		return 0;
	}
	
	/**
	 * Write text to desktop using keyboard
	 * @param text
	 */
	public void writeText(String text) {
		logger.warn("writeText is only available with seleniumRobot grid");
	}
	
	/**
	 * Returns the session object form status
	 * @param driver
	 * @return
	 */
	private JSONObject getCurrentSessionObject(RemoteWebDriver driver) {

		// sometimes, session is not immediately reported in hub status, wait a bit
		for (int i = 0; i < 15; i++) {
			JSONObject status = Unirest.get(String.format("http://%s:%d%s", hubUrl.getHost(), hubUrl.getPort(), STATUS_SERVLET))
					.asJson()
					.getBody()
					.getObject();

			JSONArray nodes = status.getJSONObject("value").getJSONArray("nodes");

			for (JSONObject node : (List<JSONObject>) nodes.toList()) {
				for (JSONObject slot : (List<JSONObject>) node.getJSONArray("slots").toList()) {
					if (slot.optJSONObject("session") != null && slot.getJSONObject("session").getString("sessionId").equals(driver.getSessionId().toString())) {
						return slot.getJSONObject("session");
					}
				}
			}
			WaitHelper.waitForSeconds(1);
			logger.info("Retry get session information from grid");
		}
		throw new SessionNotCreatedException("Could not get session information from grid");
	}
	
	/**
	 * Retrieves session information about the created driver
	 * @param driver
	 */
	public void getSessionInformationFromGrid(RemoteWebDriver driver) {
		getSessionInformationFromGrid(driver, 0);
	}
	public void getSessionInformationFromGrid(RemoteWebDriver driver, long driverCreationDuration) {

		// logging node ip address:
		JSONObject object;
		try {
			object = getCurrentSessionObject(driver);
		} catch (SessionNotCreatedException e) {
			throw e;
		} catch (Exception e) {
        	throw new SessionNotCreatedException(String.format("Could not get session information from grid: %s", e.getMessage()));
        }
 
        try {
			// setting sessionId ensures that this connector is the active one
			// issue #242: check if sessionId has already been set by a previous driver in this test session
			// 				if so, keep the previous sessionId so that video recordings are correctly handled
			// issue #612: Also keep the previous nodeId as all driver of the same test should start on the same node
			if (sessionId == null) {
				setSessionId(driver.getSessionId());
				setNodeUrl((String)object.get("uri"));
				nodeHost = nodeUrl.split("//")[1].split(":")[0];
			}
   
			String driverNodeHost = ((String)object.get("uri")).split("//")[1].split(":")[0];
			
            String browserName = driver.getCapabilities().getBrowserName();
            String version = driver.getCapabilities().getBrowserVersion();

            
            // store some information about driver creation
            MutableCapabilities caps = (MutableCapabilities)driver.getCapabilities();
            caps.setCapability(SeleniumRobotCapabilityType.GRID_HUB, hubUrl);
            caps.setCapability(SeleniumRobotCapabilityType.SESSION_ID, sessionId); // store the scenario session (sessionID of the first created browser to group browsers in logs
            caps.setCapability(SeleniumRobotCapabilityType.GRID_NODE, nodeHost);
            caps.setCapability(SeleniumRobotCapabilityType.GRID_NODE_URL, nodeUrl);
            
            // log will display the actual driver session ID and node URL so that it reflects the real driver creation (whereas nodeUrl and sessionId variables only store the first driver information for the test)
            logger.info(String.format("Brower %s (%s) created in %.1f secs on node %s [%s] with session %s", browserName, version, driverCreationDuration / 1000.0, driverNodeHost, hubUrl, driver.getSessionId()).replace(",", "."));
            
        } catch (Exception ex) {
        	throw new SessionNotCreatedException(ex.getMessage());
        } 
	}
	
	/**
	 * Stop the session calling the node URL
	 * /!\ this will not work if a secret has been defined 
	 * @param sessionId
	 */
	public boolean stopSession(String sessionId) {
		HttpResponse<String> response = Unirest.delete(String.format("%s/se/grid/node/session/%s", nodeUrl, sessionId))
		.header("X-REGISTRATION-SECRET", "")
		.asString();
		
		return response.getStatus() == 200;
	}

	public URL getHubUrl() {
		return hubUrl;
	}

	public SessionId getSessionId() {
		return sessionId;
	}

	public void setSessionId(SessionId sessionId) {
		this.sessionId = sessionId;
	}

	public String getNodeUrl() {
		return nodeUrl;
	}

	public void setNodeUrl(String nodeUrl) {
		this.nodeUrl = nodeUrl == null ? nodeUrl: nodeUrl.toLowerCase();
	}

	public String getNodeHost() {
		return nodeHost;
	}

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		SeleniumGridConnector.logger = logger;
	}
}
