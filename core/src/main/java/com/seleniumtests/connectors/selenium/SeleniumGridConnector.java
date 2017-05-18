package com.seleniumtests.connectors.selenium;

import java.io.IOException;
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
import org.openqa.selenium.remote.RemoteWebDriver;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class SeleniumGridConnector {

	protected URL hubUrl;
	protected String hubHost;
	protected int hubPort;
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
	public void uploadMobileApp(RemoteWebDriver driver) {
		return;
	}
	
	public void runTest(RemoteWebDriver driver) {
		

        // logging node ip address:
        CloseableHttpClient client = HttpClients.createDefault();
        try {
        	HttpHost serverHost = new HttpHost(hubUrl.getHost(), hubUrl.getPort());
        	URIBuilder builder = new URIBuilder();
        	builder.setPath("/grid/api/testsession?session=" + driver.getSessionId());
        	HttpPost httpPost = new HttpPost(builder.build());
	        CloseableHttpResponse response = client.execute(serverHost, httpPost);

//            HttpHost host = new HttpHost(hubHost, hubPort);
//            
//            String sessionUrl = "http://" + hub + ":" + port + "/grid/api/testsession?session=";
//            URL session = new URL(sessionUrl + driver.getSessionId());
//            BasicHttpEntityEnclosingRequest req;
//            req = new BasicHttpEntityEnclosingRequest("POST", session.toExternalForm());
//
//            org.apache.http.HttpResponse response = client.execute(host, req);
            String responseContent = EntityUtils.toString(response.getEntity());
            
            JSONObject object = new JSONObject(responseContent);
            String proxyId = (String) object.get("proxyId");
            String node = proxyId.split("//")[1].split(":")[0];
            String browserName = driver.getCapabilities().getBrowserName();
            String version = driver.getCapabilities().getVersion();
            TestLogging.info("WebDriver is running on node " + node + ", " + browserName + " " + version + ", session " + driver.getSessionId());
            
        } catch (Exception ex) {
        	logger.error(ex);
        } finally {
        	try {
				client.close();
			} catch (IOException e) {
			}
        }
	}

	public URL getHubUrl() {
		return hubUrl;
	}
}
