package com.seleniumtests.connectors.selenium;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.customexception.SeleniumGridException;
import com.seleniumtests.util.FileUtility;

import io.appium.java_client.remote.MobileCapabilityType;

public class SeleniumRobotGridConnector extends SeleniumGridConnector {

	public static final String NODE_TASK_SERVLET = "/extra/NodeTaskServlet";
	
	public SeleniumRobotGridConnector(String url) {
		super(url);
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
	        	builder.addParameter("output", "app");
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
		        	((DesiredCapabilities)caps).setCapability(MobileCapabilityType.APP, IOUtils.toString(response.getEntity().getContent()) + "/" + appFiles.get(0).getName());
		        }
		        
			} catch (IOException | URISyntaxException e) {
				throw new SeleniumGridException("could not upload application file", e);
			}
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
        	builder.addParameter("output", "app");
        	HttpPost httpPost = new HttpPost(builder.build());
	        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString());
	        FileInputStream fileInputStream = new FileInputStream(zipFile);
            InputStreamEntity entity = new InputStreamEntity(fileInputStream);
            httpPost.setEntity(entity);
	        
	        CloseableHttpResponse response = client.execute(serverHost, httpPost);
	        if (response.getStatusLine().getStatusCode() != 200) {
	        	throw new SeleniumGridException("could not upload file: " + response.getStatusLine().getReasonPhrase());
	        } else {
	        	// TODO call remote API
	        	throw new NotImplementedException("call remote Robot to really upload file");
	        }
	        
		} catch (IOException | URISyntaxException e) {
			throw new SeleniumGridException("could not upload file", e);
		}
	}
	
	/**
	 * Left clic on desktop at x,y
	 * @param x		x coordinate
	 * @param y		y coordinate
	 */
	@Override
	public void leftClic(int x, int y) {
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot clic left before driver has been created and corresponding node instanciated");
		}
		
		logger.info(String.format("clic left: %d,%d", x, y));
		try {
			Unirest.post(String.format("%s%s", nodeUrl, NODE_TASK_SERVLET))
				.queryString("action", "leftClic")
				.queryString("x", x)
				.queryString("y", y)
				.asString();
		} catch (UnirestException e) {
			logger.warn(String.format("Could not clic left: %s", e.getMessage()));
		}
	}
	
	/**
	 * right clic on desktop at x,y
	 * @param x		x coordinate
	 * @param y		y coordinate
	 */
	@Override
	public void rightClic(int x, int y) {
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot clic right before driver has been created and corresponding node instanciated");
		}
		
		logger.info(String.format("clic right: %d,%d", x, y));
		try {
			Unirest.post(String.format("%s%s", nodeUrl, NODE_TASK_SERVLET))
				.queryString("action", "rightClic")
				.queryString("x", x)
				.queryString("y", y)
				.asString();
		} catch (UnirestException e) {
			logger.warn(String.format("Could not clic right: %s", e.getMessage()));
		}
	}
	
	/**
	 * Take screenshot of the full desktop
	 * @return a string with base64 content of the image
	 */
	@Override
	public String captureDesktopToBuffer() {
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot take screenshot before driver has been created and corresponding node instanciated");
		}
		
		logger.info("capturing desktop");
		try {
			return Unirest.get(String.format("%s%s", nodeUrl, NODE_TASK_SERVLET))
				.queryString("action", "screenshot")
				.asString().getBody();
			
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
			Unirest.post(String.format("%s%s", nodeUrl, NODE_TASK_SERVLET))
			.queryString("action", "uploadFile")
			.queryString("content", base64Content)
			.queryString("name", fileName)
			.asString();
		} catch (UnirestException e) {
			logger.warn(String.format("Could send keys: %s", e.getMessage()));
		}
	}
	
	/**
	 * Send keys to desktop
	 * @param keys
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
			Unirest.post(String.format("%s%s", nodeUrl, NODE_TASK_SERVLET))
				.queryString("action", "sendKeys")
				.queryString("keycodes", keyCodeString).asString();
		} catch (UnirestException e) {
			logger.warn(String.format("Could send keys: %s", e.getMessage()));
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
			Unirest.post(String.format("%s%s", nodeUrl, NODE_TASK_SERVLET))
				.queryString("action", "writeText")
				.queryString("text", text).asString();
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
			Unirest.post(String.format("%s%s", nodeUrl, NODE_TASK_SERVLET))
				.queryString("action", "kill")
				.queryString("process", processName).asString();
		} catch (UnirestException e) {
			logger.warn(String.format("Could not kill process %s: %s", processName, e.getMessage()));
		}
	}
	
	@Override
	public void startVideoCapture() {
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot start video capture before driver has been created and corresponding node instanciated");
		}
		
		logger.info("starting capture");
		try {
			Unirest.get(String.format("%s%s", nodeUrl, NODE_TASK_SERVLET))
				.queryString("action", "startVideoCapture")
				.queryString("session", sessionId).asString();
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
			HttpResponse<InputStream> videoResponse = Unirest.get(String.format("%s%s", nodeUrl, NODE_TASK_SERVLET))
				.queryString("action", "stopVideoCapture")
				.queryString("session", sessionId).asBinary();
			InputStream videoI = videoResponse.getBody();
			
			File videoFile = new File(outputFile);
			FileOutputStream os = new FileOutputStream(videoFile);
			IOUtils.copy(videoI, os);
			os.close();
			
			return videoFile;
			
		} catch (UnirestException | IOException e) {
			logger.warn(String.format("Could not stop video capture: %s", e.getMessage()));
			return null;
		}
	}

}
