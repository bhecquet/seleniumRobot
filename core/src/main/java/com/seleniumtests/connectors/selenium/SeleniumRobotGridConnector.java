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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.customexception.SeleniumGridException;
import com.seleniumtests.util.FileUtility;

import io.appium.java_client.remote.MobileCapabilityType;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.UnirestInstance;
import kong.unirest.json.JSONObject;

public class SeleniumRobotGridConnector extends SeleniumGridConnector {

	private static final String ONLY_MAIN_SCREEN = "onlyMainScreen";
	private static final String Y_FIELD = "y";
	private static final String X_FIELD = "x";
	private static final String NAME_FIELD = "name";
	private static final String SESSION_FIELD = "session";
	private static final String OUTPUT_FIELD = "output";
	private static final String ACTION_FIELD = "action";
	public static final String NODE_TASK_SERVLET = "/extra/NodeTaskServlet";
	public static final String FILE_SERVLET = "/extra/FileServlet";
	public static final String STATUS_SERVLET = "/status";
	public static final String GUI_SERVLET = "/grid/admin/GuiServlet"; 
	
	private String nodeServletUrl;
	
	public SeleniumRobotGridConnector(String url) {
		super(url);
		
	}
	
	@Override
	public void getSessionInformationFromGrid(RemoteWebDriver driver, long driverCreationDuration) {
		super.getSessionInformationFromGrid(driver, driverCreationDuration);
		
		int nodePort;
		try {
			nodePort = new URL(nodeUrl).getPort();
		} catch (MalformedURLException e) {
			throw new ConfigurationException("Node URL is invalid: " + nodeUrl);
		}
		nodeServletUrl = nodeUrl.replace(Integer.toString(nodePort), Integer.toString(nodePort + 10));
	}
	
	/**
	 * In case an app is required on the node running the test, upload it to the grid hub
	 * This will then be made available through HTTP GET URL to the node (appium will receive an url instead of a file)
	 * 
	 */
	@Override
	public void uploadMobileApp(Capabilities caps) {
		
		String appPath = (String)caps.getCapability(MobileCapabilityType.APP);
		
		// check whether app is given and app path is a local file
		if (appPath != null && new File(appPath).isFile()) {
			
			try (CloseableHttpClient client = HttpClients.createDefault();) {
				// zip file
				List<File> appFiles = new ArrayList<>();
				appFiles.add(new File(appPath));
				File zipFile = FileUtility.createZipArchiveFromFiles(appFiles);
				
				HttpHost serverHost = new HttpHost(hubUrl.getHost(), hubUrl.getPort());
				URIBuilder builder = new URIBuilder();
	        	builder.setPath("/grid/admin/FileServlet/");
	        	builder.addParameter(OUTPUT_FIELD, "app");
	        	HttpPost httpPost = new HttpPost(builder.build());
		        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString());
		        FileInputStream fileInputStream = new FileInputStream(zipFile);
	            InputStreamEntity entity = new InputStreamEntity(fileInputStream);
	            httpPost.setEntity(entity);
		        
		        CloseableHttpResponse response = client.execute(serverHost, httpPost);
		        if (response.getStatusLine().getStatusCode() != 200) {
		        	throw new SeleniumGridException("could not upload application file: " + response.getStatusLine().getReasonPhrase());
		        } else {
		        	// set path to the mobile application as an URL on the grid hub
		        	((DesiredCapabilities)caps).setCapability(MobileCapabilityType.APP, IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8) + "/" + appFiles.get(0).getName());
		        }
		        
			} catch (IOException | URISyntaxException e) {
				throw new SeleniumGridException("could not upload application file", e);
			}
		}
	}
	
	/**
	 * Upload file to node
	 * @param filePath			the file to upload
	 * @param returnLocalFile	if true, returned path will be the local path on grid node. If false, we get file://upload/file/<uuid>/
	 * @return
	 */
	@Override
	public String uploadFileToNode(String filePath, boolean returnLocalFile) {
		
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot upload file to browser before driver has been created and corresponding node instanciated");
		}
		
		// zip file
		File zipFile = null;
		try {
			List<File> appFiles = new ArrayList<>();
			appFiles.add(new File(filePath));
			zipFile = FileUtility.createZipArchiveFromFiles(appFiles);
		} catch (IOException e1) {
			throw new SeleniumGridException("Error in uploading file, when zipping: " + e1.getMessage());
		}

		logger.info("uploading file to node: " + zipFile.getName());
		try {
			HttpRequestWithBody req = Unirest.post(String.format("%s%s", nodeUrl, FILE_SERVLET))
					.header(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString())
					.queryString(OUTPUT_FIELD, "file");
			if (returnLocalFile) {
				req = req.queryString("localPath", "true");
			}
		
			HttpResponse<String> response = req.field("upload", zipFile)
					.asString();
			
			if (response.getStatus() != 200) {
				throw new SeleniumGridException(String.format("Error uploading file: %s", response.getBody()));
			} else {
				return response.getBody();
			}
		} catch (UnirestException e) {
			throw new SeleniumGridException(String.format("Cannot upload file: %s", e.getMessage()));
		}

	}
	
	/**
	 * Upload a file given file path
	 * @param filePath
	 */
	@Override
	public void uploadFile(String filePath) {
		try (CloseableHttpClient client = HttpClients.createDefault();) {
			// zip file
			List<File> appFiles = new ArrayList<>();
			appFiles.add(new File(filePath));
			File zipFile = FileUtility.createZipArchiveFromFiles(appFiles);

			HttpHost serverHost = new HttpHost(hubUrl.getHost(), hubUrl.getPort());
			URIBuilder builder = new URIBuilder();
        	builder.setPath("/grid/admin/FileServlet/");
        	builder.addParameter(OUTPUT_FIELD, "app");
        	HttpPost httpPost = new HttpPost(builder.build());
	        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString());
	        FileInputStream fileInputStream = new FileInputStream(zipFile);
            InputStreamEntity entity = new InputStreamEntity(fileInputStream);
            httpPost.setEntity(entity);
	        
	        CloseableHttpResponse response = client.execute(serverHost, httpPost);
	        if (response.getStatusLine().getStatusCode() != 200) {
	        	throw new SeleniumGridException("could not upload file: " + response.getStatusLine().getReasonPhrase());
	        } else {
	        	throw new NotImplementedException("call remote Robot to really upload file");
	        }
	        
		} catch (IOException | URISyntaxException e) {
			throw new SeleniumGridException("could not upload file", e);
		}
	}
	

	/**
	 * Get position of mouse pointer
	 * @return
	 */
	@Override
	public Point getMouseCoordinates() {
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot get mouse coordinates before driver has been created and corresponding node instanciated");
		}
		
		String responseBody = null;
		logger.info("Mouse coordinates");
		try {
			// we get a string with x,y
			HttpResponse<String> response = Unirest.get(String.format("%s%s", nodeServletUrl, NODE_TASK_SERVLET))
					.queryString(ACTION_FIELD, "mouseCoordinates")
					.asString();
			if (response.getStatus() != 200) {
				logger.error(String.format("Mouse coordinates error: %s", response.getBody()));
				return new Point(0,0);
			}
			responseBody = response.getBody();
			String[] coords = responseBody.split(",");
			return new Point(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
			
			
		} catch (UnirestException e) {
			logger.warn(String.format("Could not get mouse coordinates: %s", e.getMessage()));
		} catch (IndexOutOfBoundsException | NumberFormatException e) {
			logger.error(String.format("mouse coordinates '%s' are invalid", responseBody));
		}
		return new Point(0,0);
	}

	/**
	 * Left click on desktop at x,y
	 * @param x		x coordinate
	 * @param y		y coordinate
	 */
	@Override
	public void leftClic(int x, int y) {
		leftClic(false, x, y);
	}
		
	/**
	 * Left click on desktop at x,y
	 * @param x		x coordinate
	 * @param y		y coordinate
	 * @param onlyMainScreen	if true, click coordinates are on the main screen
	 */
	@Override
	public void leftClic(boolean onlyMainScreen, int x, int y) {
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot click left before driver has been created and corresponding node instanciated");
		}
		
		logger.info(String.format("click left: %d,%d", x, y));
		try {
			HttpResponse<String> response = Unirest.post(String.format("%s%s", nodeServletUrl, NODE_TASK_SERVLET))
				.queryString(ACTION_FIELD, "leftClic")
				.queryString(X_FIELD, x)
				.queryString(Y_FIELD, y)
				.queryString(ONLY_MAIN_SCREEN, onlyMainScreen)
				.asString();
			if (response.getStatus() != 200) {
				logger.error(String.format("Left click error: %s", response.getBody()));
			}
		} catch (UnirestException e) {
			logger.warn(String.format("Could not click left: %s", e.getMessage()));
		}
	}
	
	
	/**
	 * Double click on desktop at x,y
	 * @param x		x coordinate
	 * @param y		y coordinate
	 */
	@Override
	public void doubleClick(int x, int y) {
		doubleClick(false, x, y);
	}
	
	/**
	 * Double click on desktop at x,y
	 * @param x		x coordinate
	 * @param y		y coordinate
	 * @param onlyMainScreen	if true, click coordinates are on the main screen
	 */
	@Override
	public void doubleClick(boolean onlyMainScreen, int x, int y) {
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot double click before driver has been created and corresponding node instanciated");
		}
		
		logger.info(String.format("double click: %d,%d", x, y));
		try {
			HttpResponse<String> response = Unirest.post(String.format("%s%s", nodeServletUrl, NODE_TASK_SERVLET))
			.queryString(ACTION_FIELD, "doubleClick")
			.queryString(X_FIELD, x)
			.queryString(Y_FIELD, y)
			.queryString(ONLY_MAIN_SCREEN, onlyMainScreen)
			.asString();
			if (response.getStatus() != 200) {
				logger.error(String.format("Double click error: %s", response.getBody()));
			}
		} catch (UnirestException e) {
			logger.warn(String.format("Could not double click: %s", e.getMessage()));
		}
	}
	
	/**
	 * right click on desktop at x,y
	 * @param x		x coordinate
	 * @param y		y coordinate
	 */
	@Override
	public void rightClic(int x, int y) {
		rightClic(false, x, y);
	}
	
	/**
	 * right click on desktop at x,y
	 * @param x		x coordinate
	 * @param y		y coordinate
	 * @param onlyMainScreen	if true, click coordinates are on the main screen
	 */
	@Override
	public void rightClic(boolean onlyMainScreen, int x, int y) {
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot click right before driver has been created and corresponding node instanciated");
		}
		
		logger.info(String.format("clic right: %d,%d", x, y));
		try {
			HttpResponse<String> response = Unirest.post(String.format("%s%s", nodeServletUrl, NODE_TASK_SERVLET))
				.queryString(ACTION_FIELD, "rightClic")
				.queryString(X_FIELD, x)
				.queryString(Y_FIELD, y)
				.queryString(ONLY_MAIN_SCREEN, onlyMainScreen)
				.asString();
			if (response.getStatus() != 200) {
				logger.error(String.format("Right click error: %s", response.getBody()));
			}
		} catch (UnirestException e) {
			logger.warn(String.format("Could not click right: %s", e.getMessage()));
		}
	}
	
	/**
	 * Take screenshot of the full desktop
	 * @return a string with base64 content of the image
	 */
	@Override
	public String captureDesktopToBuffer() {
		return captureDesktopToBuffer(false);
	}
	
	@Override
	public String captureDesktopToBuffer(boolean onlyMainScreen) {
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot take screenshot before driver has been created and corresponding node instanciated");
		}
		
		logger.info("capturing desktop");
		try {
			HttpResponse<String> response =  Unirest.get(String.format("%s%s", nodeServletUrl, NODE_TASK_SERVLET))
				.queryString(ACTION_FIELD, "screenshot")
				.queryString(ONLY_MAIN_SCREEN, onlyMainScreen)
				.asString();
			
			if (response.getStatus() != 200) {
				logger.error(String.format("capture desktop error: %s", response.getBody()));
			} else {
				return response.getBody();
			}
			
		} catch (UnirestException e) {
			logger.warn(String.format("Could not capture desktop: %s", e.getMessage()));
		}
		return "";
	}
	
	/**
	 * upload file to browser
	 * @param fileName		name of the file to upload
	 * @param base64Content	content of the file, encoded in base 64
	 */
	@Override
	public void uploadFileToBrowser(String fileName, String base64Content) {
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot upload file to browser before driver has been created and corresponding node instanciated");
		}
		
		

		logger.info("uploading file to browser: " + fileName);
		try {
			byte[] byteArray = base64Content.getBytes();
			byte[] decodeBuffer = Base64.decodeBase64(byteArray);
		
			HttpResponse<String> response = Unirest.post(String.format("%s%s", nodeServletUrl, NODE_TASK_SERVLET))
				.header(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString())
				.queryString(ACTION_FIELD, "uploadFile")
				.queryString(NAME_FIELD, fileName)
				.body(decodeBuffer)
				.asString();
			
			if (response.getStatus() != 200) {
				logger.error(String.format("Error uploading file: %s", response.getBody()));
			}
		} catch (UnirestException e) {
			logger.warn(String.format("Cannot upload file: %s", e.getMessage()));
		} 
	}
	
	/**
	 * Send keys to desktop
	 * @param keys		List of KeyEvent 
	 */
	@Override
	public void sendKeysWithKeyboard(List<Integer> keyCodes) {
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot use keyboard before driver has been created and corresponding node instanciated");
		}
		
		String keyCodeString = String.join(",", keyCodes
								.stream()
								.map(k -> Integer.toString(k))
								.collect(Collectors.toList()));
		
		logger.info("sending keys: " + keyCodes);
		try {
			HttpResponse<String> response = Unirest.post(String.format("%s%s", nodeServletUrl, NODE_TASK_SERVLET))
				.queryString(ACTION_FIELD, "sendKeys")
				.queryString("keycodes", keyCodeString).asString();
			
			if (response.getStatus() != 200) {
				logger.error(String.format("Send keys error: %s", response.getBody()));
			}
		} catch (UnirestException e) {
			logger.warn(String.format("Could send keys: %s", e.getMessage()));
		}
	}
	
	/**
	 * Display running step
	 * @param text
	 */
	@Override
	public void displayRunningStep(String stepName) {
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot display running step before driver has been created and corresponding node instanciated");
		}
		
		try {
			HttpResponse<String> response = Unirest.post(String.format("%s%s", nodeServletUrl, NODE_TASK_SERVLET))
					.queryString(ACTION_FIELD, "displayRunningStep")
					.queryString("stepName", stepName)
					.queryString(SESSION_FIELD, sessionId)
					.asString();
			
			if (response.getStatus() != 200) {
				logger.error(String.format("display running step error: %s", response.getBody()));
			}
		} catch (UnirestException e) {
			logger.warn(String.format("Could not display running step: %s", e.getMessage()));
		}
	}
	
	/**
	 * Write text to desktop using keyboard
	 * @param text
	 */
	@Override
	public void writeText(String text) {
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot write text before driver has been created and corresponding node instanciated");
		}
		
		logger.info("writing text: " + text);
		try {
			HttpResponse<String> response = Unirest.post(String.format("%s%s", nodeServletUrl, NODE_TASK_SERVLET))
				.queryString(ACTION_FIELD, "writeText")
				.queryString("text", text).asString();
		
			if (response.getStatus() != 200) {
				logger.error(String.format("Write text error: %s", response.getBody()));
			}
		} catch (UnirestException e) {
			logger.warn(String.format("Could not write text: %s", e.getMessage()));
		}
	}
	
	
	/**
	 * Kill process
	 * @param processName
	 */
	@Override
	public void killProcess(String processName) {
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot kill a remote process before driver has been created and corresponding node instanciated");
		}
		
		logger.info("killing process: " + processName);
		try {
			HttpResponse<String> response = Unirest.post(String.format("%s%s", nodeServletUrl, NODE_TASK_SERVLET))
				.queryString(ACTION_FIELD, "kill")
				.queryString("process", processName).asString();
			
			if (response.getStatus() != 200) {
				logger.error(String.format("kill process error: %s", response.getBody()));
			}
		} catch (UnirestException e) {
			logger.warn(String.format("Could not kill process %s: %s", processName, e.getMessage()));
		}
	}
	

	/**
	 * Execute command
	 * @param program	name of the program
	 * @param args		arguments of the program
	 */
	@Override
	public String executeCommand(String program, String ... args) {
		return executeCommand(program, null, args);
	}
	
	/**
	 * Execute command with timeout
	 * @param program	name of the program
	 * @param timeout	if null, default timeout will be applied
	 * @param args		arguments of the program
	 */
	@Override
	public String executeCommand(String program, Integer timeout, String ... args) {
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot execute a remote process before driver has been created and corresponding node instanciated");
		}
		
		logger.info("execute program: " + program);
		try (UnirestInstance unirest = Unirest.spawnInstance()
				
				) {
			
			HttpRequestWithBody req = unirest.post(String.format("%s%s", nodeServletUrl, NODE_TASK_SERVLET))
				.queryString(ACTION_FIELD, "command")
				.queryString(NAME_FIELD, program)
				.queryString(SESSION_FIELD, getSessionId().toString());
			
			int i = 0;
			for (String arg: args) {
				req = req.queryString("arg" + i, arg);
				i++;
			}
			if (timeout != null) {
				unirest.config().socketTimeout((timeout + 5 ) * 1000);
				req = req.queryString("timeout", timeout);
			}
			
			HttpResponse<String> response = req.asString();
			return response.getBody();
		} catch (UnirestException e) {
			logger.warn(String.format("Could not execute process %s: %s", program, e.getMessage()));
			return "";
		}
	}
	
	/**
	 * returns the list of processes, on the node, whose name without extension is the requested one
	 * e.g: getProcessList("WINWORD")
	 * Case will be ignored
	 */
	@Override
	public List<Integer> getProcessList(String processName) {
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot get a remote process before driver has been created and corresponding node instanciated");
		}
		
		logger.info("getting process list for: " + processName);
		try {
			HttpResponse<String> response =  Unirest.get(String.format("%s%s", nodeServletUrl, NODE_TASK_SERVLET))
				.queryString(ACTION_FIELD, "processList")
				.queryString(NAME_FIELD, processName)
				.asString();
			
			if (response.getStatus() != 200) {
				logger.error(String.format("get process list error: %s", response.getBody()));
			} else {
				List<String> pidListStr = Arrays.asList(
					response.getBody()
					.split(","));
				return pidListStr.stream().map(Integer::valueOf).collect(Collectors.toList());
			}
		} catch (UnirestException e) {
			logger.warn(String.format("Could not get process list of %s: %s", processName, e.getMessage()));
		}
		
		return new ArrayList<>();
	}
	
	@Override
	public void startVideoCapture() {
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot start video capture before driver has been created and corresponding node instanciated");
		}
		
		logger.info("starting capture");
		try {
			HttpResponse<String> response = Unirest.get(String.format("%s%s", nodeServletUrl, NODE_TASK_SERVLET))
				.queryString(ACTION_FIELD, "startVideoCapture")
				.queryString(SESSION_FIELD, sessionId)
				.asString();
			
			if (response.getStatus() != 200) {
				logger.error(String.format("start video capture error: %s", response.getBody()));
			}
			
		} catch (UnirestException e) {
			logger.warn(String.format("Could start video capture: %s", e.getMessage()));
		}
	}
	
	@Override
	public File stopVideoCapture(String outputFile) {
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot stop video capture before driver has been created and corresponding node instanciated");
		}
		
		logger.info("stopping capture");
		try {
			
			// delete file if it exists as '.asFile()' will not overwrite it
			deleteExistingVideo(outputFile);
				
			HttpResponse<File> videoResponse = Unirest.get(String.format("%s%s", nodeServletUrl, NODE_TASK_SERVLET))
				.queryString(ACTION_FIELD, "stopVideoCapture")
				.queryString(SESSION_FIELD, sessionId)
				.asFile(outputFile);
			
			if (videoResponse.getStatus() != 200) {
				logger.error(String.format("stop video capture error: %s", videoResponse.getBody()));
				return null;
			} else {				
				return videoResponse.getBody();
			}
			
		} catch (UnirestException e) {
			logger.warn(String.format("Could not stop video capture: %s", e.getMessage()));
			return null;
		}
	}
	
	private void deleteExistingVideo(String outputFile) {
		if (new File(outputFile).exists()) {
			try {
				Files.delete(Paths.get(outputFile));
					
			} catch (Exception e) {
				logger.warn("Error deleting previous video file, there may be a problem getting the new one: " + e.getMessage());
			}
		}
	}
	
	/**
	 * @return 	true: if grid hub is active and and there is at least 1 node
	 * 			false: if no node is present
	 * 
	 */
	@Override
	public boolean isGridActive() {
		boolean gridActive = super.isGridActive();
		if (!gridActive) {
			return false;
		}
		
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

}
