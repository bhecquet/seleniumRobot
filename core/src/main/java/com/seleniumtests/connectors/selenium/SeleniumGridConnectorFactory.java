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

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Class for getting an instance of grid connector
 * Check whether we have a simple Selenium Grid or a SeleniumRobot Grid which has more features
 * @author behe
 *
 */
public class SeleniumGridConnectorFactory {
	
	private static ThreadLocal<SeleniumGridConnector> seleniumGridConnector = new ThreadLocal<>();
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumGridConnector.class);
	
	public static final String GUI_SERVLET = "/grid/admin/GuiServlet/";
	
	private SeleniumGridConnectorFactory() {
		// nothing to do
	}
	
	public static SeleniumGridConnector getInstance() {
		if (seleniumGridConnector.get() == null) {
			throw new ConfigurationException("getInstance() should be called after getInstance(String url) has been called once");
		}
		return seleniumGridConnector.get();
	}

	public synchronized static SeleniumGridConnector getInstance(String url) {
		
		URL hubUrl;
		try {
			hubUrl = new URL(url);
		} catch (MalformedURLException e1) {
			throw new ConfigurationException(String.format("Hub url '%s' is invalid: %s", url, e1.getMessage()));
		}
		
		try {
			HttpResponse<String> response = Unirest.get(String.format("http://%s:%s%s", hubUrl.getHost(), hubUrl.getPort(), GUI_SERVLET)).asString();
			if (response.getStatus() == 200) {
				if (response.getBody().contains("default monitoring page")) {
        			seleniumGridConnector.set(new SeleniumGridConnector(url));
        		} else {
        			seleniumGridConnector.set(new SeleniumRobotGridConnector(url));
        		}
        		return seleniumGridConnector.get();
        	} else {
        		throw new ConfigurationException("Cannot connect to the grid hub at " + url);
        	}
		} catch (Exception ex) {
        	throw new ConfigurationException("Cannot connect to the grid hub at " + url, ex);
		}
	}
}
