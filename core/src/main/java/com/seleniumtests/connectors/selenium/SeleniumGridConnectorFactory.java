/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.seleniumtests.core.utils.SystemClock;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Class for getting an instance of grid connector
 * Check whether we have a simple Selenium Grid or a SeleniumRobot Grid which has more features
 * @author behe
 *
 */
public class SeleniumGridConnectorFactory {
	
	private static ThreadLocal<SeleniumGridConnector> seleniumGridConnector = new ThreadLocal<>();
	public static final int DEFAULT_RETRY_TIMEOUT = 180; // timeout in seconds. 3 minutes to wait for grid hub to be there
	private static int retryTimeout = DEFAULT_RETRY_TIMEOUT;
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumGridConnector.class);
	
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
		SystemClock clock = new SystemClock();
		long end = clock.laterBy(retryTimeout * 1000L);
		Exception currentException = null;
		
		while (clock.isNowBefore(end)) {
			try {
				HttpResponse<String> response = Unirest.get(String.format("http://%s:%s%s", hubUrl.getHost(), hubUrl.getPort(), SeleniumGridConnector.CONSOLE_SERVLET)).asString();
				if (response.getStatus() == 200) {
					HttpResponse<String> responseGuiServlet = Unirest.get(String.format("http://%s:%s%s", hubUrl.getHost(), hubUrl.getPort(), SeleniumRobotGridConnector.GUI_SERVLET)).asString();
					if (responseGuiServlet.getStatus() == 200) {
						seleniumGridConnector.set(new SeleniumRobotGridConnector(url));
	        		} else {
	        			seleniumGridConnector.set(new SeleniumGridConnector(url));
	        		}
	        		return seleniumGridConnector.get();
	        	} else {
	        		throw new ConfigurationException("Cannot connect to the grid hub at " + url);
	        	}
			} catch (Exception ex) {
				WaitHelper.waitForMilliSeconds(500);
				currentException = ex;
				continue;
			}
		}

    	throw new ConfigurationException("Cannot connect to the grid hub at " + url, currentException);
	}

	public static int getRetryTimeout() {
		return retryTimeout;
	}

	/**
	 * set retry timeout in seconds
	 * @param retryTimeout
	 */
	public static void setRetryTimeout(int retryTimeout) {
		SeleniumGridConnectorFactory.retryTimeout = retryTimeout;
	}
}
