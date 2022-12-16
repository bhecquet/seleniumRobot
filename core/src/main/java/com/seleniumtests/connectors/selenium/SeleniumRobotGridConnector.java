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
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.customexception.SeleniumGridException;
import com.seleniumtests.util.FileUtility;

import io.appium.java_client.remote.MobileCapabilityType;
import kong.unirest.GetRequest;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.UnirestInstance;

public class SeleniumRobotGridConnector extends SeleniumGridConnector {

	private static final String ONLY_MAIN_SCREEN = "onlyMainScreen";
	private static final String Y_FIELD = "y";
	private static final String X_FIELD = "x";
	private static final String NAME_FIELD = "name";
	private static final String SESSION_FIELD = "session";
	private static final String OUTPUT_FIELD = "output";
	private static final String FILE_FIELD = "file";
	private static final String UPLOAD_FOLDER = "upload";
	private static final String ACTION_FIELD = "action";
	public static final String NODE_TASK_SERVLET = "/extra/NodeTaskServlet";
	public static final String FILE_SERVLET = "/extra/FileServlet";
	public static final String GUI_SERVLET = "/grid/admin/GuiServlet"; 
	
	private String nodeServletUrl;
	private URL hubServletUrl;
	
	public SeleniumRobotGridConnector(String url) {
		super(url);
		
	}
	
	
	@Override
	public void getSessionInformationFromGrid(RemoteWebDriver driver, long driverCreationDuration) {
		super.getSessionInformationFromGrid(driver, driverCreationDuration);
		setNodeUrl(nodeUrl);
	}
	
	/**
	 * In case an app is required on the node running the test, upload it to the grid hub
	 * This will then be made available through HTTP GET URL to the node (appium will receive an url instead of a file)
	 * 
	 */
	@Override
	public void uploadMobileApp(Capabilities caps) {
		
		String appPath = (String)caps.getCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + MobileCapabilityType.APP);
		
		// check whether app is given and app path is a local file
		if (appPath != null && new File(appPath).isFile()) {
			
			try (CloseableHttpClient client = HttpClients.createDefault();) {
				// zip file
				List<File> appFiles = new ArrayList<>();
				appFiles.add(new File(appPath));
				File zipFile = FileUtility.createZipArchiveFromFiles(appFiles);
				
				HttpHost serverHost = new HttpHost(hubServletUrl.getHost(), hubServletUrl.getPort());
				URIBuilder builder = new URIBuilder();
	        	builder.setPath("/grid/admin/FileServlet");
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
		        	((DesiredCapabilities)caps).setCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + MobileCapabilityType.APP, IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8) + "/" + appFiles.get(0).getName());
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
			HttpRequestWithBody req = Unirest.post(String.format("%s%s", nodeServletUrl, FILE_SERVLET))
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
	
	@Override
	public File downloadFileFromNode(String filePath) {
		
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot download file before driver has been created and corresponding node instanciated");
		}
		
		if (!filePath.startsWith(UPLOAD_FOLDER)) {
			throw new ScenarioException(String.format("File path %s is invalid, only path in 'upload' folder are allowed", filePath));
		}
		
		Path targetFile = Paths.get(FileUtils.getTempDirectoryPath(), new File(filePath).getName());
		
		logger.info("downloading file from node: {}", filePath);
		try {
			HttpResponse<File> response = Unirest.get(String.format("%s%s", nodeServletUrl, FILE_SERVLET))
					.queryString(FILE_FIELD, "file:" + filePath)
					.asFile(targetFile.toString(), StandardCopyOption.REPLACE_EXISTING)
					;

			if (response.getStatus() != 200) {
				throw new SeleniumGridException(String.format("Error downloading file %s: %s", filePath, response.getBody()));
			} else {
				return response.getBody();
			}
		} catch (UnirestException e) {
			throw new SeleniumGridException(String.format("Cannot download file: %s", filePath, e.getMessage()));
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

			HttpHost serverHost = new HttpHost(hubServletUrl.getHost(), hubServletUrl.getPort());
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
		
		logger.info("writing text: " + text.substring(0, Math.min(2, text.length())) + "****");
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
				.queryString(NAME_FIELD, program);
			
			if (getSessionId() != null) {
				req = req.queryString(SESSION_FIELD, getSessionId().toString());
			}
			
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
					.trim()
					.split(","));
				return pidListStr.stream()
						.filter(p -> !p.isEmpty())
						.map(Integer::parseInt).collect(Collectors.toList());
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
	 * For TESTING only
	 */
	public void setNullNodeUrl() {
		super.setNodeUrl(null);
	}
	

	@Override
	public void setNodeUrl(String nodeUrl) {
		super.setNodeUrl(nodeUrl);
		int nodePort;
		try {
			nodePort = new URL(nodeUrl).getPort();
		} catch (MalformedURLException e) {
			throw new ConfigurationException("Node URL is invalid: " + nodeUrl);
		}
		nodeServletUrl = nodeUrl.replace(Integer.toString(nodePort), Integer.toString(nodePort + 10));
	}

	@Override
	protected void setHubUrl(String hubUrl) {
		super.setHubUrl(hubUrl);
		
		try {
			hubServletUrl = new URL(hubUrl.replace(Integer.toString(hubPort), Integer.toString(hubPort + 10)));
		} catch (MalformedURLException e) {
			throw new ConfigurationException("Hub URL is invalid: " + nodeUrl);
		}
	}

}
