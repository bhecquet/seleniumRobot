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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

import com.seleniumtests.core.StatisticsStorage.DriverUsage;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

public class SeleniumGridConnector implements ISeleniumGridConnector {

	protected URL hubUrl;
	protected String hubHost;
	protected int hubPort;
	protected SessionId sessionId;
	protected String nodeUrl;
	protected String nodeHost;
	
	public static final String CONSOLE_SERVLET = "/grid/console/";
	public static final String API_TEST_SESSSION = "/grid/api/testsession/";
	protected static Logger logger = SeleniumRobotLogger.getLogger(SeleniumGridConnector.class);
	
	public SeleniumGridConnector(String url) {
		try {
			hubUrl = new URL(url);
		} catch (MalformedURLException e1) {
			throw new ConfigurationException(String.format("Hub url '%s' is invalid: %s", url, e1.getMessage()));
		}
		hubHost = hubUrl.getHost();
        hubPort = hubUrl.getPort();
	}
	
	/**
	 * Do nothing as we are not a SeleniumRobotGrid
	 * @param caps
	 */
	public void uploadMobileApp(Capabilities caps) {
		logger.warn("application upload is only available with seleniumRobot grid");
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
		HttpResponse<String> response;
		try {
			response = Unirest.get(String.format("http://%s:%s%s", hubUrl.getHost(), hubUrl.getPort(), SeleniumGridConnector.CONSOLE_SERVLET)).asString();
			
			if (response.getStatus() != 200) {
	    		logger.warn("Error connecting to the grid hub at " + hubUrl);
	    		return false;
	    	} else {
	    		return true;
	    	}
		} catch (UnirestException e) {
			logger.warn("Cannot connect to the grid hub at " + hubUrl);
			return false;
		}
		
	}
	
	/**
	 * Display running step
	 * @param stepName
	 */
	public void displayRunningStep(String stepName) {
		logger.warn("displayRunningStep is only available with seleniumRobot grid");
	}
	
	/**
	 * Write text to desktop using keyboard
	 * @param text
	 */
	public void writeText(String text) {
		logger.warn("writeText is only available with seleniumRobot grid");
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
        	object = Unirest.get(String.format("http://%s:%d%s", hubUrl.getHost(), hubUrl.getPort(), API_TEST_SESSSION))
        		.queryString("session", driver.getSessionId().toString())
        		.asJson()
        		.getBody()
        		.getObject();
        } catch (Exception e) {
        	throw new SessionNotCreatedException(String.format("Could not get session information from grid: %s", e.getMessage()));
        }
        	
    	// if we have an error (session not found), raise an exception
    	try {
    		nodeUrl = (String) object.get("proxyId");
    	} catch(JSONException e) {
    		// {"msg": "Cannot find test slot running session 7ef50edc-ce51-40dd-98b6-0a369bff38b in the registry."," + 
			//  "success": false"
    		// }, 
    		throw new SessionNotCreatedException(object.getString("msg"));
    	}
        	
        try {
        	nodeHost = nodeUrl.split("//")[1].split(":")[0];
            String browserName = driver.getCapabilities().getBrowserName();
            String version = driver.getCapabilities().getVersion();

            // setting sessionId ensures that this connector is the active one
            // issue #242: check if sessionId has already been set by a previous driver in this test session
            // 				if so, keep the previous sessionId so that recordings are correctly handled
            if (sessionId == null) {
            	setSessionId(driver.getSessionId());
            }
            
            // store some information about driver creation
            MutableCapabilities caps = (MutableCapabilities)driver.getCapabilities();
            caps.setCapability(DriverUsage.GRID_HUB, hubUrl);
            caps.setCapability(DriverUsage.SESSION_ID, sessionId);
            caps.setCapability(DriverUsage.GRID_NODE, nodeHost);
            
            logger.info(String.format("Brower %s (%s) created in %.1f secs on node %s [%s] with session %s", browserName, version, driverCreationDuration / 1000.0, nodeHost, hubUrl, sessionId).replace(",", "."));
            
        } catch (Exception ex) {
        	throw new SessionNotCreatedException(ex.getMessage());
        } 
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
		this.nodeUrl = nodeUrl;
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
