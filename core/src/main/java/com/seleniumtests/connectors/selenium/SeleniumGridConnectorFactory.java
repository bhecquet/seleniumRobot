/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.connectors.selenium;

import java.net.*;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import kong.unirest.UnirestException;
import org.apache.logging.log4j.Logger;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
 * Class for getting an instance of grid connector
 * Check whether we have a simple Selenium Grid or a SeleniumRobot Grid which has more features
 * @author behe
 *
 */
public class SeleniumGridConnectorFactory {
	
	public static final int DEFAULT_RETRY_TIMEOUT = 180; // timeout in seconds. 3 minutes to wait for grid hub to be there
	private static int retryTimeout = DEFAULT_RETRY_TIMEOUT;
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumGridConnectorFactory.class);
	
	private SeleniumGridConnectorFactory() {
		// nothing to do
	}

	/**
	 * Returns the list of available grid connectors
	 * From a list of grid URL, look for grids which are available at the time of request
	 * If all grids are available, returns all
	 * If none are available, wait for at least one to be there
	 * @param urls	list of all grid connectors
	 */
	public static synchronized List<SeleniumGridConnector> getInstances(List<String> urls) {
		
		if (urls.isEmpty()) {
			throw new ConfigurationException("cannot create grid, no address provided");
		}
		
		// check addresses
		List<URL> hubUrls = new ArrayList<>();
		for (String url: urls) {
			try {
				hubUrls.add(new URI(url).toURL());
			} catch (MalformedURLException | URISyntaxException e1) {
				throw new ConfigurationException(String.format("Hub url '%s' is invalid: %s", url, e1.getMessage()));
			}
        }
		
		Clock clock = Clock.systemUTC();
		Instant end = clock.instant().plusSeconds(retryTimeout);
		Exception currentException = null;
		
		while (end.isAfter(clock.instant())) {
			
			List<SeleniumGridConnector> seleniumGridConnectors = new ArrayList<>();
			
			for (URL hubUrl: hubUrls) {
				
				if (hubUrl.getHost().contains("browserstack")) {
					seleniumGridConnectors.add(new BrowserStackGridConnector(hubUrl.toString()));
					break;
				}
				currentException = connectHub(currentException, seleniumGridConnectors, hubUrl);
			}
			
			// if at least one hub replies, continue
			if (!seleniumGridConnectors.isEmpty()) {
				return seleniumGridConnectors;
			}
		}
				
    	throw new ConfigurationException("Cannot connect to the grid hubs at " + urls, currentException);
	}

	/**
	 * @param seleniumGridConnectors	list of grid connectors
	 * @param hubUrl					the hub to connect to
	 * @return an exception that may occur
	 */
	private static Exception connectHub(Exception currentException, List<SeleniumGridConnector> seleniumGridConnectors,
			URL hubUrl) {
				// connect to console page to see if grid replies
				try {
					HttpResponse<String> response = Unirest.get(String.format("http://%s:%s%s", hubUrl.getHost(), hubUrl.getPort(), SeleniumGridConnector.CONSOLE_SERVLET)).asString();
					if (response.getStatus() == 200) {
						
						// try to connect to a SeleniumRobot grid specific servlet. If it replies, we are on a seleniumRobot grid
						try {
							HttpResponse<String> responseGuiServlet = Unirest.get(String.format("http://%s:%s%s", hubUrl.getHost(), hubUrl.getPort() + 10, SeleniumRobotGridConnector.GUI_SERVLET)).asString();
							if (responseGuiServlet.getStatus() == 200) {
								seleniumGridConnectors.add(new SeleniumRobotGridConnector(hubUrl.toString()));
							}
						} catch (UnirestException e) {
							if (e.getCause() instanceof SocketException) {
								seleniumGridConnectors.add(new SeleniumGridConnector(hubUrl.toString()));
							} else {
								throw e;
							}
		        		}
		        	} else {
		        		logger.error("Cannot connect to the grid hub at {}", hubUrl);
		        	}
				} catch (Exception ex) {
					WaitHelper.waitForMilliSeconds(500);
					currentException = ex;
				}
		return currentException;
			}
			
	public static int getRetryTimeout() {
		return retryTimeout;
	}

	/**
	 * set retry timeout in seconds
	 * @param retryTimeout	the retry timeout
	 */
	public static void setRetryTimeout(int retryTimeout) {
		SeleniumGridConnectorFactory.retryTimeout = retryTimeout;
	}
}
