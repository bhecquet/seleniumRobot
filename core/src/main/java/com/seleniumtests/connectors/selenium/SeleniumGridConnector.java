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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class SeleniumGridConnector {

	protected URL hubUrl;
	protected String hubHost;
	protected int hubPort;
	protected SessionId sessionId;
	protected String nodeUrl;
	
	public static final String CONSOLE_SERVLET = "/grid/console/";
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
	 * @param driver
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
	 * Kill process
	 * @param processName
	 */
	public void killProcess(String processName) {
		logger.warn("kill is only available with seleniumRobot grid");
	}

	/**
	 * Upload a file to a browser uplpoad window
	 * @param filePath
	 */
	public void uploadFileToBrowser(String fileName, String base64Content) {
		logger.warn("file upload to browser is only available with seleniumRobot grid");
	}
	
	/**
	 * Left clic on desktop at x,y
	 * @param x		x coordinate
	 * @param y		y coordinate
	 */
	public void leftClic(int x, int y) {
		logger.warn("left clic is only available with seleniumRobot grid");
	}
	
	/**
	 * double clic on desktop at x,y
	 * @param x		x coordinate
	 * @param y		y coordinate
	 */
	public void doubleClick(int x, int y) {
		logger.warn("left clic is only available with seleniumRobot grid");
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
	 * Send keys to desktop
	 * @param keys
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
        try {
        	JSONObject object = Unirest.get(String.format("http://%s:%d/grid/api/testsession/", hubUrl.getHost(), hubUrl.getPort()))
        		.queryString("session", driver.getSessionId().toString())
        		.asJson()
        		.getBody()
        		.getObject();
        	
            nodeUrl = (String) object.get("proxyId");
            String node = nodeUrl.split("//")[1].split(":")[0];
            String browserName = driver.getCapabilities().getBrowserName();
            String version = driver.getCapabilities().getVersion();
            
            // setting sessionId ensures that this connector is the active one
            // issue #242: check if sessionId has already been set by a previous driver in this test session
            // 				if so, keep the previous sessionId so that recordings are correctly handled
            if (sessionId == null) {
            	setSessionId(driver.getSessionId());
            }
            logger.info(String.format("Brower %s (%s) created in %.1f secs on node %s [%s] with session %s", browserName, version, driverCreationDuration / 1000.0, node, hubUrl, sessionId).replace(",", "."));
            
        } catch (Exception ex) {
        	logger.error(ex);
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
}
