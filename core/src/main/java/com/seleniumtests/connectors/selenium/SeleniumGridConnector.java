package com.seleniumtests.connectors.selenium;

import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class SeleniumGridConnector {

	protected URL hubUrl;
	protected String hubHost;
	protected int hubPort;
	protected SessionId sessionId;
	protected String nodeUrl;
	protected static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumGridConnector.class);
	
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
	 * Take screenshot of the full desktop
	 * @return
	 */
	public BufferedImage captureDesktopToBuffer() {
		return null;
	}
	
	public void runTest(RemoteWebDriver driver) {
		
        // logging node ip address:
        try (CloseableHttpClient client = HttpClients.createDefault()) {
        	HttpHost serverHost = new HttpHost(hubUrl.getHost(), hubUrl.getPort());
        	URIBuilder builder = new URIBuilder();
        	builder.setPath("/grid/api/testsession/");
        	builder.addParameter("session", driver.getSessionId().toString());
        	HttpPost httpPost = new HttpPost(builder.build());
	        CloseableHttpResponse response = client.execute(serverHost, httpPost);

            String responseContent = EntityUtils.toString(response.getEntity());
            
            JSONObject object = new JSONObject(responseContent);
            nodeUrl = (String) object.get("proxyId");
            String node = nodeUrl.split("//")[1].split(":")[0];
            String browserName = driver.getCapabilities().getBrowserName();
            String version = driver.getCapabilities().getVersion();
            sessionId = driver.getSessionId();
            TestLogging.info("WebDriver is running on node " + node + ", " + browserName + " " + version + ", session " + sessionId);
            
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

	public String getNodeUrl() {
		return nodeUrl;
	}

	public void setNodeUrl(String nodeUrl) {
		this.nodeUrl = nodeUrl;
	}
}
