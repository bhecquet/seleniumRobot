package com.seleniumtests.connectors.selenium;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Class for getting an instance of grid connector
 * Check whether we have a simple Selenium Grid or a SeleniumRobot Grid which has more features
 * @author behe
 *
 */
public class SeleniumGridConnectorFactory {
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumGridConnector.class);
	
	private SeleniumGridConnectorFactory() {
		// othing to do
	}

	public static SeleniumGridConnector getInstance(String url) {
		URL hubUrl;
		try {
			hubUrl = new URL(url);
		} catch (MalformedURLException e1) {
			throw new ConfigurationException(String.format("Hub url '%s' is invalid: %s", url, e1.getMessage()));
		}
		
		CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpHost serverHost = new HttpHost(hubUrl.getHost(), hubUrl.getPort());
            URIBuilder builder = new URIBuilder();
        	builder.setPath("/grid/admin/GuiServlet/");
            HttpGet httpGet = new HttpGet(builder.build());
        	CloseableHttpResponse response = client.execute(serverHost, httpGet);
        	String content = IOUtils.toString(response.getEntity().getContent());
        	
        	if (response.getStatusLine().getStatusCode() == 200) {
        		if (content.contains("default monitoring page")) {
        			return new SeleniumGridConnector(url);
        		} else {
        			return new SeleniumRobotGridConnector(url);
        		}
        	} else {
        		throw new ConfigurationException("Cannot connect to the grid hub at " + url);
        	}
        	
        } catch (Exception ex) {
        	throw new ConfigurationException("Cannot connect to the grid hub at " + url, ex);
        } finally {
        	try {
				client.close();
			} catch (IOException e) {
				logger.error("could not close client", e);
			}
        }
	}
}
