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
package com.seleniumtests.driver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.seleniumtests.customexception.RetryableDriverException;
import com.seleniumtests.util.har.*;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.testng.ITestResult;

import com.neotys.selenium.proxies.NLWebDriver;
import com.neotys.selenium.proxies.NLWebDriverFactory;
import com.seleniumtests.browserfactory.AppiumDriverFactory;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.browserfactory.BrowserStackDriverFactory;
import com.seleniumtests.browserfactory.ChromeDriverFactory;
import com.seleniumtests.browserfactory.EdgeDriverFactory;
import com.seleniumtests.browserfactory.FirefoxDriverFactory;
import com.seleniumtests.browserfactory.HtmlUnitDriverFactory;
import com.seleniumtests.browserfactory.IEDriverFactory;
import com.seleniumtests.browserfactory.IWebDriverFactory;
import com.seleniumtests.browserfactory.PerfectoDriverFactory;
import com.seleniumtests.browserfactory.SafariDriverFactory;
import com.seleniumtests.browserfactory.SauceLabsDriverFactory;
import com.seleniumtests.browserfactory.SeleniumGridDriverFactory;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.core.testretry.TestRetryAnalyzer;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.driver.screenshots.SnapshotTarget;
import com.seleniumtests.reporter.info.ImageLinkInfo;
import com.seleniumtests.reporter.info.Info;
import com.seleniumtests.reporter.info.MultipleInfo;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.ScenarioLogger;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.video.VideoCaptureMode;
import com.seleniumtests.util.video.VideoRecorder;

//import net.lightbody.bmp.BrowserMobProxy;
//import net.lightbody.bmp.core.har.Har;

/**
 * This class provides factory to create webDriver session.
 */
public class WebUIDriver {

	public static final String DEFAULT_DRIVER_NAME = "main";
	private static final Logger logger = SeleniumRobotLogger.getLogger(WebUIDriver.class);
	private static ScenarioLogger scenarioLogger = ScenarioLogger.getScenarioLogger(TestRetryAnalyzer.class);
	
    private static ThreadLocal<Map<String, WebUIDriver>> uxDriverSession = new ThreadLocal<>();
    private static ThreadLocal<VideoRecorder> videoRecorder = new ThreadLocal<>();
    private static ThreadLocal<String> currentWebUiDriverName = new ThreadLocal<>();
    private String name;
    private DriverConfig config;
    private WebDriver driver;
    private IWebDriverFactory webDriverBuilder;
    private static final Object createDriverLock = new Object();

	public WebUIDriver(String name) {
    	if (SeleniumTestsContextManager.getThreadContext() == null) {
            return;
        }
    	this.name = name;
    }

	public String getName() {
		return name;
	}

	/**
	 * For test only
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

    /**
     * prepare driver:
     * - create it
     * - add listeners
     * - create and start video capture
     * - create and start network capture proxy
     * - record driver and browser pid so that they can be deleted at the end of test session
     * @return
     */
	public WebDriver createRemoteWebDriver()  {
        
		webDriverBuilder = getWebDriverBuilderFactory();
        

        // synchronizing is only useful in local mode, as we need to get the PID of browser and driver
        // in grid mode, PID list will be empty
        // Moreover, keeping synchronization in grid mode will block driver of all tests
        synchronized (config.getMode() == DriverMode.LOCAL ? createDriverLock: new Object()) {

            logger.info("driver mode: "+ config.getMode());
        	
    		// get browser info used to start this driver. It will be used then for managing pids
        	BrowserInfo browserInfo = webDriverBuilder.getSelectedBrowserInfo();
        	List<Long> existingPids = new ArrayList<>();

    		// get pid pre-existing the creation of this driver. This helps filtering drivers launched by other tests or users
    		if (browserInfo != null) {
        		existingPids.addAll(browserInfo.getDriverAndBrowserPid(new ArrayList<>()));
        	}
        	
    		TestStep cuurrentTestStep = TestStepManager.getCurrentRootTestStep();
    		long start = new Date().getTime();
    		long duration;
    		
    		try {
    			
    			driver = webDriverBuilder.createWebDriver();
    		} finally {

    			duration = new Date().getTime() - start;
    			if (cuurrentTestStep != null) {
    				cuurrentTestStep.setDurationToExclude(duration);
    			}
    			scenarioLogger.info(String.format("driver creation took: %.1f secs", duration / 1000.0));
    		}
 
            WaitHelper.waitForSeconds(2);
            
            List<Long> driverPids = new ArrayList<>();
            
            // get the created PIDs
            if (browserInfo != null) {
    			driverPids = browserInfo.getDriverAndBrowserPid(existingPids);
    		}
            
            // issue #280: we use 'webDriverBuilder.getSelectedBrowserInfo()' as 'browserInfo' variable is null for grid, whereas, 'webDriverBuilder.getSelectedBrowserInfo()'
            // gets an updated version once the driver has been created on grid
            driver = handleListeners(driver, webDriverBuilder.getSelectedBrowserInfo(), driverPids);
            
            if (driver != null) { // driver is a CustomEventFiringWebDriver here
    			MutableCapabilities caps = ((CustomEventFiringWebDriver)driver).getInternalCapabilities();
    			caps.setCapability(SeleniumRobotCapabilityType.STARTUP_DURATION, duration);
                caps.setCapability(SeleniumRobotCapabilityType.START_TIME, start);

                // testName is added here, once driver has been created, even if this capability has already been added in IDestkopCapabilitiesFactory. Reason is that 
                // capability from IDestkopCapabilitiesFactory is not available when we request capabilities from driver.
                if (config.getTestContext() != null && config.getTestContext().getTestNGResult() != null) {
	                String testName = TestNGResultUtils.getTestName(config.getTestContext().getTestNGResult());
	                caps.setCapability(SeleniumRobotCapabilityType.TEST_NAME, testName);
                }
			}
    	

//			if (config.getBrowserMobProxy() != null) {
//				config.getBrowserMobProxy().newHar(SeleniumTestsContextManager.getThreadContext().getRelativeOutputDir());
//			}
			
			if (config.getVideoCapture() != VideoCaptureMode.FALSE && videoRecorder.get() == null) {
				try {
					VideoRecorder recorder = CustomEventFiringWebDriver.startVideoCapture(SeleniumTestsContextManager.getThreadContext().getRunMode(), 
																						SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector(),
																						new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()),
																						"videoCapture.avi");
					videoRecorder.set(recorder);
					TestStepManager.setVideoStartDate();
				} catch (ScenarioException e) {
					logger.warn("Video capture won't start: " + e.getMessage());
				}
			}
        }

        

        return driver;
    }

	/**
	 * Get the driver factory according to run mode and driver
	 */
	private IWebDriverFactory getWebDriverBuilderFactory() {
		if (config.getMode() == DriverMode.GRID) {
            return new SeleniumGridDriverFactory(this.config);
        } else if (config.getMode() == DriverMode.SAUCELABS) {
        	return new SauceLabsDriverFactory(this.config);
        } else if (config.getMode() == DriverMode.BROWSERSTACK) {
        	return new BrowserStackDriverFactory(this.config);
        } else if (config.getMode() == DriverMode.PERFECTO) {
        	return new PerfectoDriverFactory(this.config);
               
        	
        // local mode
        } else {
        	if (useAppium()) {
        		return new AppiumDriverFactory(this.config);
        	} else {
	            return getLocalWebDriverBuilderFactory();
        	}
        }
	}

	/**
	 * @return
	 */
	private IWebDriverFactory getLocalWebDriverBuilderFactory() {
		if (config.getBrowserType() == BrowserType.FIREFOX) {
		    return new FirefoxDriverFactory(this.config);
		} else if (config.getBrowserType() == BrowserType.INTERNET_EXPLORER) {
		    return new IEDriverFactory(this.config);
		} else if (config.getBrowserType() == BrowserType.EDGE) {
			return new EdgeDriverFactory(this.config);
		} else if (config.getBrowserType() == BrowserType.CHROME) {
		    return new ChromeDriverFactory(this.config);
		} else if (config.getBrowserType() == BrowserType.HTMLUNIT) {
		    return new HtmlUnitDriverFactory(this.config);
		} else if (config.getBrowserType() == BrowserType.SAFARI) {
		    return new SafariDriverFactory(this.config);
		} else {
		    throw new DriverExceptions("Unsupported browser: " + config.getBrowserType().toString());
		}
	}

    /**
     * Clean all WebUIDriver for this thread
     */
    public static void cleanUp() {
    	
    	if (uxDriverSession.get() == null) {
    		return;
    	}
    	
    	for (WebUIDriver webuiDriver: uxDriverSession.get().values()) {
    		if (webuiDriver != null) {
    			webuiDriver.clean();
    		}
    	}
    	
		cleanUpWebUIDriver();

    }
    
    /**
     * logs the current state of the driver
     * Used when terminating test, before closing browser
     * extract the video file recorded by test
     */
 
    public static File logFinalDriversState(ITestResult testResult) {
    	if (uxDriverSession.get() != null) {
    		for (WebUIDriver webuiDriver: uxDriverSession.get().values()) {
        		if (webuiDriver != null) {
        			webuiDriver.logFinalDriverState(testResult);
        		}
        	}
    	}
    	
    	// kept for compatibility
    	return null;

    }
    
    /**
     * Stop video capture
     */
    public static File stopVideoCapture() {
    	if (videoRecorder.get() != null) {
			
			try {
				return CustomEventFiringWebDriver.stopVideoCapture(SeleniumTestsContextManager.getThreadContext().getRunMode(), 
																		SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector(),
																		WebUIDriver.getVideoRecorder().get());

			} catch (IOException e) {
				logger.error("cannot attach video capture", e);
			} catch (Exception e) {
				logger.error("Error stopping video capture: " + e.getMessage());
			} finally {
				videoRecorder.remove();
			}
		}
    	
    	return null;
    }
    
    /**
     * Logs current state of the browser
     */
    private void logFinalDriverState(ITestResult testResult) {
    	if (driver != null && driver instanceof CustomEventFiringWebDriver && ((CustomEventFiringWebDriver)driver).isDriverExited()) {
    		driver = null;
    	}
    	
    	if (driver != null) {
			try {

				if (driver instanceof CustomEventFiringWebDriver) {

					// #539: mobile tests do not support switch to default content
					if (((CustomEventFiringWebDriver)driver).isWebTest()) {
						// issue #414: capture the whole screen
						driver.switchTo().defaultContent();
					}

					// force screenshotUtil to use the driver of this WebUiDriver, not the currently selected one
					// do it only when driver is a regular CustomEventFiringWebdriver (#619)
					for (ScreenShot screenshot : new ScreenshotUtil(driver).capture(SnapshotTarget.PAGE, ScreenShot.class, true, true)) {
						scenarioLogger.logScreenshot(screenshot, null, name, SnapshotCheckType.FALSE);

						// add the last screenshots to TestInfo so that there is a quicklink on reports
						Info lastStateInfo = TestNGResultUtils.getTestInfo(testResult).get(TestStepManager.LAST_STATE_NAME);
						if (lastStateInfo != null) {
							((MultipleInfo) lastStateInfo).addInfo(new ImageLinkInfo(screenshot.getImage()));
						}
					}
				}
			} catch (Exception e) {
				scenarioLogger.log("Error while logging: " + e.getMessage());
			}
		}
		
//		try {
//	    	// stop HAR capture
//			if (config.getBrowserMobProxy() != null) {
//				Har har = config.getBrowserMobProxy().endHar();
//				scenarioLogger.logNetworkCapture(har, name);
//			}
//
//
//		} catch (Exception e) {
//			scenarioLogger.log("Error while logging: " + e.getMessage());
//		} finally {
//			config.setBrowserMobProxy(null);
//		}
    }
    
    class DriverAliveTask implements Callable<String> {
    	
    	private WebDriver drv;
    	
    	public DriverAliveTask(WebDriver drv) {
			this.drv = drv;
		}
    	
        @Override
        public String call() throws Exception {
        	drv.manage().timeouts().getImplicitWaitTimeout();
            return "OK";
        }
    }
    	
    /**
     * Cleans the driver created by this class: 
     * quit  browser
     * remove pids
     * stop appium
     * dereference driver in this WebUIDriver
     */
    private void clean() {
    	
    	if (driver != null && driver instanceof CustomEventFiringWebDriver && ((CustomEventFiringWebDriver)driver).isDriverExited()) {
    		driver = null;
    	}

    	if (driver != null) {
    		ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> future = executor.submit(new DriverAliveTask(driver));
            try {
				future.get(3, TimeUnit.SECONDS);
				
				// write logs
	    		try {
	        		for (String logType: driver.manage().logs().getAvailableLogTypes()) {
						retrieveLogs(logType);
	        		}
	            } catch (Exception e) {
	            	e.printStackTrace();
	            }
	    		
	    		
	    		try {
	    			scenarioLogger.log("quiting webdriver " + Thread.currentThread().getId());
		            driver.quit();
	        	} catch (Exception ex) {
	        		scenarioLogger.error("Exception encountered when quiting driver:" + ex.getMessage());
	        	}
			} catch (InterruptedException | ExecutionException | TimeoutException e1) {
				future.cancel(true);
				logger.warn("driver not available");
			}

    		driver = null;
        }
		
        if (webDriverBuilder != null) {
        	webDriverBuilder.cleanUp();
        } 
        
        // in case of mobile test with appium, stop appium server
        try {
	        if (webDriverBuilder instanceof AppiumDriverFactory) {
	        	((AppiumDriverFactory) webDriverBuilder).getAppiumLauncher().stopAppium();
	        }
        } catch (Exception e) {
        	logger.error("Error stopping Appium: " + e.getMessage());
        }
    
        // stop HAR capture in case it has not already been done by SeleniumRobotTestListener. This may be the case when a driver is created in @AfterMethod
//		try {
//	        if (config.getBrowserMobProxy() != null) {
//	        	config.getBrowserMobProxy().endHar();
//			}
//		} catch (Exception e) {
//			logger.error("Error stopping browsermob proxy: " + e.getMessage());
//		} finally {
//			config.setBrowserMobProxy(null);
//		}
		
		// stop video capture
		stopVideoCapture();
		
    }

	/**
	 * @param logType
	 */
	private void retrieveLogs(String logType) {
		try (PrintWriter writer = new PrintWriter(Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), 
																		String.format("driver-log-%s.txt", logType)).toFile().getAbsolutePath(),
												"UTF-8")) {

			List<LogEntry> logEntries = driver.manage().logs().get(logType).getAll();
			if ("performance".equalsIgnoreCase(logType)) {
				parsePerformanceLogs(logEntries);
			}
			for (LogEntry line: logEntries) {
				writer.println(line.toString());
			}

		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// ignore errors
		}
	}

	private void parsePerformanceLogs(List<LogEntry> logEntries) {
		if (driver != null && driver instanceof CustomEventFiringWebDriver && ((CustomEventFiringWebDriver)driver).getBrowserInfo().getBrowser() == BrowserType.CHROME ) {
			parseChromePerformanceLogs(logEntries);
		}
	}

	private void parseChromePerformanceLogs(List<LogEntry> logEntries) {
		Map<String, HashMap<String, Object>> requests = new LinkedHashMap<>();

		Har har = new Har();
		Log log = har.getLog();

		for (LogEntry line: logEntries) {
			JSONObject jsonObject = null;

			// only read "Network.responseReceived" messages as they contain everything
			try {
				jsonObject = new JSONObject(line.getMessage());
				JSONObject messageObject = jsonObject.getJSONObject("message");
				String method = messageObject.getString("method");
				switch (method) {
					// message format: {"message":{"method":"Network.requestWillBeSent","params":{"documentURL":"http://10.25.4.70:53669/test.html","frameId":"145E40AEF6F7A76C61973C3946CA0992","hasUserGesture":false,"initiator":{"columnNumber":180,"lineNumber":59,"type":"parser","url":"http://10.25.4.70:53669/test.html"},"loaderId":"6AAED31A84393CDE33A22E12ACA3924B","redirectHasExtraInfo":false,"request":{"headers":{"Referer":"http://10.25.4.70:53669/test.html","User-Agent":"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"},"initialPriority":"Medium","isSameSite":true,"method":"GET","mixedContentType":"none","referrerPolicy":"strict-origin-when-cross-origin","url":"http://10.25.4.70:53669/googleSearch.png"},"requestId":"34060.2","timestamp":171300.445026,"type":"Image","wallTime":1739344558.303674}},"webview":"145E40AEF6F7A76C61973C3946CA0992"}
					case "Network.requestWillBeSent" -> {
						String requestId = messageObject.getJSONObject("params").getString("requestId");
						requests.put(requestId, new HashMap<>());
						requests.get(requestId).put("requestWillBeSent", messageObject);
					}
					// message format: {"message":{"method":"Network.responseReceived","params":{"frameId":"8C5E01A9EE0BD7556C532FCFBE04EE5D","hasExtraInfo":true,"loaderId":"3149B492109FC8E15361AE661A188A05","requestId":"3149B492109FC8E15361AE661A188A05","response":{"alternateProtocolUsage":"unspecifiedReason","charset":"","connectionId":109,"connectionReused":true,"encodedDataLength":101,"fromDiskCache":false,"fromPrefetchCache":false,"fromServiceWorker":false,"headers":{"Content-Length":"124","Date":"Tue, 11 Feb 2025 09:02:20 GMT","Server":"Jetty(11.0.24)"},"mimeType":"text/html","protocol":"http/1.1","remoteIPAddress":"10.200.38.44","remotePort":51230,"responseTime":1.739264540255014e+12,"securityState":"insecure","status":200,"statusText":"OK","timing":{"connectEnd":-1,"connectStart":-1,"dnsEnd":-1,"dnsStart":-1,"proxyEnd":4.019,"proxyStart":2.804,"pushEnd":0,"pushStart":0,"receiveHeadersEnd":11.977,"receiveHeadersStart":5.365,"requestTime":91274.541644,"sendEnd":4.238,"sendStart":4.129,"sslEnd":-1,"sslStart":-1,"workerFetchStart":-1,"workerReady":-1,"workerRespondWithSettled":-1,"workerStart":-1},"url":"http://10.200.38.44:51230/testIFrame3.html"},"timestamp":91274.558756,"type":"Document"}},"webview":"0327E68CEF262C8D77818DC5C8B14339"}
					case "Network.responseReceived" -> {
						String requestId = messageObject.getJSONObject("params").getString("requestId");
						requests.get(requestId).put("responseReceived", messageObject);
					}
					// {"message":{"method":"Network.loadingFinished","params":{"encodedDataLength":924,"requestId":"28364.2","timestamp":192318.813115}},"webview":"D6FD686EEEFF1CF429E60E5D8F6A8D71"}
					case "Network.loadingFinished" -> {
						String requestId = messageObject.getJSONObject("params").getString("requestId");
						requests.get(requestId).put("loadingFinished", messageObject);
					}
					case "Network.loadingFailed" -> {
						String requestId = messageObject.getJSONObject("params").getString("requestId");
						requests.get(requestId).put("loadingFailed", messageObject);
					}
					// {"method":"Network.responseReceivedExtraInfo","params":{"blockedCookies":[],"cookiePartitionKey":{"hasCrossSiteAncestor":false,"topLevelSite":"http://127.0.0.1"},"cookiePartitionKeyOpaque":false,"exemptedCookies":[],"headers":{"Content-Length":"2538","Date":"Fri, 14 Feb 2025 15:42:13 GMT","Server":"Jetty(11.0.24)"},"headersText":"HTTP/1.1 200 OK\r\nDate: Fri, 14 Feb 2025 15:42:13 GMT\r\nContent-Length: 2538\r\nServer: Jetty(11.0.24)\r\n\r\n","requestId":"28364.5","resourceIPAddressSpace":"Local","statusCode":200}},"webview":"D6FD686EEEFF1CF429E60E5D8F6A8D71"}
					// use to get the real status code (in case of cache loading)
					case "Network.responseReceivedExtraInfo" -> {
						String requestId = messageObject.getJSONObject("params").getString("requestId");
						requests.get(requestId).put("responseReceivedExtraInfo", messageObject);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (Map.Entry<String, HashMap<String, Object>> requestsEntry: requests.entrySet()) {
			String requestId = requestsEntry.getKey();
			JSONObject jsonRequest = (JSONObject) requestsEntry.getValue().get("requestWillBeSent");
			JSONObject jsonRequestHeaders = jsonRequest.getJSONObject("params").getJSONObject("request").getJSONObject("headers");

			Request request = new Request(0,
					jsonRequest.getJSONObject("params").getJSONObject("request").getString("method"),
					jsonRequest.getJSONObject("params").getJSONObject("request").getString("url"),
					"HTTP N/A",
					jsonRequestHeaders.keySet().stream().map(key -> new Header(key, jsonRequestHeaders.getString(key))).collect(Collectors.toList()),
					new ArrayList<>(), // cookies
					new ArrayList<>(),
					0
			);

			JSONObject jsonResponse = (JSONObject) requestsEntry.getValue().get("responseReceived");;
			JSONObject jsonResponseHeaders = jsonResponse.getJSONObject("params").getJSONObject("response").getJSONObject("headers");

			int statusCode = jsonResponse.getJSONObject("params").getJSONObject("response").getInt("status");
			if (requestsEntry.getValue().get("responseReceivedExtraInfo") != null) {
				JSONObject jsonResponseExtraInfo = (JSONObject) requestsEntry.getValue().get("responseReceivedExtraInfo");
				statusCode = jsonResponseExtraInfo.getJSONObject("params").getInt("statusCode");
			}

			Response response = new Response(
					statusCode,
					jsonResponse.getJSONObject("params").getJSONObject("response").getString("statusText"),
					jsonResponse.getJSONObject("params").getJSONObject("response").getString("protocol"),
					jsonResponseHeaders.keySet().stream().map(key -> new Header(key, jsonResponseHeaders.getString(key))).collect(Collectors.toList()),
					new ArrayList<>(), // cookies
					new Content(
							jsonResponse.getJSONObject("params").getJSONObject("response").getString("mimeType"),
							jsonResponseHeaders.optInt("Content-Length", 0),
							"_masked_"
					),
					"",
					jsonResponse.getJSONObject("params").getJSONObject("response").getInt("encodedDataLength"),
					jsonResponseHeaders.optInt("Content-Length", 0)
			);

			JSONObject jsonTimings = jsonResponse.getJSONObject("params").getJSONObject("response").getJSONObject("timing");
			double startLoadingTimestamp = jsonTimings.getDouble("requestTime");
			double endLoadingTimestamp = jsonResponse.getJSONObject("params").getDouble("timestamp");
			if (requestsEntry.getValue().get("loadingFinished") != null) {
				JSONObject jsonLoadingFinished = (JSONObject) requestsEntry.getValue().get("loadingFinished");
				endLoadingTimestamp = jsonLoadingFinished.getJSONObject("params").getDouble("timestamp");
			}

			// for details about timings: https://chromedevtools.github.io/devtools-protocol/tot/Network/
			Timing timings = new Timing(
					// assume that 'blocked' is the time between 'requestTime' and start of proxy negociation
					Math.min(jsonTimings.getDouble("proxyStart"), jsonTimings.getDouble("sendStart")),
					jsonTimings.getDouble("dnsEnd") == -1 ? -1: jsonTimings.getDouble("dnsEnd") - jsonTimings.getDouble("dnsStart"),
					jsonTimings.getDouble("connectEnd") == -1 ? -1: jsonTimings.getDouble("connectEnd") - jsonTimings.getDouble("connectStart"),
					jsonTimings.getDouble("sslEnd") == -1 ? -1: jsonTimings.getDouble("sslEnd") - jsonTimings.getDouble("sslStart"),
					jsonTimings.getDouble("sendEnd") - jsonTimings.getDouble("sendStart"),
					jsonTimings.getDouble("receiveHeadersStart") - jsonTimings.getDouble("sendEnd"),
					(endLoadingTimestamp - startLoadingTimestamp) * 1000 - jsonTimings.getDouble("receiveHeadersEnd")
			);

			double duration = (endLoadingTimestamp - jsonRequest.getJSONObject("params").getDouble("timestamp")) * 1000;

			Entry entry = new Entry(LocalDateTime.ofEpochSecond((int)jsonRequest.getJSONObject("params").getDouble("wallTime"), (int)((jsonRequest.getJSONObject("params").getDouble("wallTime") - (int)jsonRequest.getJSONObject("params").getDouble("wallTime")) + 1000000), ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME),
					request,
					response,
					timings,
					(int)duration);
			log.addEntry(entry);
		}

		// use of kong jsonObject so that sub-objects are serialized
		logger.info(new kong.unirest.json.JSONObject(har).toString());
	}
    
    /**
     * dereference this WebUIDriver from the thread
     */
    private static void cleanUpWebUIDriver() {
        uxDriverSession.remove();
        currentWebUiDriverName.remove();
    }

	/**
     * Returns native WebDriver which can be converted to RemoteWebDriver.
     *
     * @return  webDriver
     */
    public static WebDriver getNativeWebDriver() {

		WebDriver driver = getWebDriver(true);
    	if (driver != null && driver instanceof CustomEventFiringWebDriver) {
    		return ((CustomEventFiringWebDriver)driver).getOriginalDriver();
    	} else {
    		return null;
    	}
    }

//	public static BrowserMobProxy getBrowserMobProxy() {
//		CustomEventFiringWebDriver driver = (CustomEventFiringWebDriver)WebUIDriver.getWebDriver(false);
//		BrowserMobProxy mobProxy = null;
//		if (driver != null) {
//			mobProxy = driver.getMobProxy();
//		}
//		return mobProxy;
//	}
	
	/**
	 * Returns the Neoload driver for the current running driver
	 * @return
	 */
	public static NLWebDriver getNeoloadDriver() {
		WebDriver driver = getWebDriver(false);
		if (driver != null && driver instanceof CustomEventFiringWebDriver && ((CustomEventFiringWebDriver)driver).getNeoloadDriver() != null) {
			return ((CustomEventFiringWebDriver)driver).getNeoloadDriver();
		} else {
			return null;
		}
	}

    public IWebDriverFactory getWebDriverBuilder() {
		return webDriverBuilder;
	}

	/**
     * Returns WebDriver instance Creates a new WebDriver Instance if it is null and isCreate is true.
     *
     * @param   isCreate  create webdriver or not
     *
     * @return
     */
    public static WebDriver getWebDriver(final Boolean isCreate) {
    	return getWebDriver(isCreate, null, getCurrentWebUiDriverName(), null);
    }
    
    /**
     * Returns WebDriver instance Creates a new WebDriver Instance if it is null and isCreate is true.
     *
     * @param   isCreate  					create webdriver or not
     * @param	browserType					the new browser type to create. If null, and creation is requested, then the browser configured by user or configuration will be created
     * @param	driverName					a logical name to give to the created driver
     * @param	attachExistingDriverPort 	if we need to attach to an existing browser instead of creating one, then specify the port here
     *
     * @return
     */
    public static WebDriver getWebDriver(Boolean isCreate, BrowserType browserType, String driverName, Integer attachExistingDriverPort) {
    	
    	if (driverName == null) {
    		throw new ScenarioException("A name must be given to the driver");
    	}
    	
    	String previousDriverName = getCurrentWebUiDriverName();
    	boolean createDriver = false;
    	
    	// issue #304: should we try to create the driver
    	if (Boolean.TRUE.equals(isCreate) && !SeleniumTestsContextManager.isNonGuiTest()) {
	    	if (uxDriverSession.get() == null 
	        		|| uxDriverSession.get().get(driverName) == null 
	        		|| uxDriverSession.get().get(driverName).driver == null
					|| !(uxDriverSession.get().get(driverName).driver instanceof CustomEventFiringWebDriver)) {
	    		createDriver = true;
	    	
	    	// we have a driver referenced for this name, is it still available (not closed)
	    	} else if (uxDriverSession.get().get(driverName).driver instanceof CustomEventFiringWebDriver
					&& ((CustomEventFiringWebDriver) uxDriverSession.get().get(driverName).driver).isBrowserOrAppClosed()) {
    			uxDriverSession.get().remove(driverName);
    			createDriver = true;
	    	}
    	} 
    	
        if (createDriver) {
        	
         	WebUIDriver uiDriver = getWebUIDriver(true, driverName);
        	if (uiDriver != null) {
	        	uiDriver.config.setAttachExistingDriverPort(attachExistingDriverPort);
	        	
	        	if (browserType == null) {
	        		uiDriver.config.setBrowserType(SeleniumTestsContextManager.getThreadContext().getBrowser());
	        	} else {
	        		uiDriver.config.setBrowserType(browserType);
	        	}
	        	
	        	// expect the new driver to run on same node as the previous ones
	        	if (uxDriverSession.get() != null && uxDriverSession.get().size() > 1 && uiDriver.config.getSeleniumGridConnector() != null) {
	        		uiDriver.config.setRunOnSameNode(uiDriver.getConfig().getSeleniumGridConnector().getNodeUrl());
	        	}

				int maxRetry = 2;
				for (int i = 0; i < maxRetry; i++) {
					try {
						uiDriver.createWebDriver();
						break;

					} catch (Exception e) {
						// clean and retry
						if (e instanceof RetryableDriverException && i < maxRetry - 1) {
							logger.warn("Driver creation failed: " + e.getMessage() + " => retrying");
							uiDriver.clean();
						} else {

							// in case driver fails to start, remove any reference to its name so that we cannot switch to it
							setCurrentWebUiDriverName(previousDriverName);
							if (driverName != DEFAULT_DRIVER_NAME) {
								uxDriverSession.get().remove(driverName);
							}
							switchToDriver(previousDriverName);
							throw e;
						}
					}
				}
        	}
        } else {
        	setCurrentWebUiDriverName(driverName);
        }

        try {
        	return uxDriverSession.get().get(driverName).driver;
        } catch (NullPointerException e) {
        	return null;
        }
        
    }
    
    /**
     * Switch to one of the created driver, referenced by name
     * @param driverName	the driver name to return
     */
    public static void switchToDriver(String driverName) {
    	if (uxDriverSession.get() == null || !uxDriverSession.get().containsKey(driverName)) {
    		throw new ScenarioException(String.format("driver with name %s has not been created", driverName));
    	}

		if (uxDriverSession.get().get(driverName).driver != null && !(uxDriverSession.get().get(driverName).driver instanceof CustomEventFiringWebDriver)) {
			logger.warn("Cannot switch to driver, this is not a CustomEventFiringWebDriver");
			return;
		}

		if (uxDriverSession.get().get(driverName).driver != null
				&& uxDriverSession.get().get(driverName).driver instanceof CustomEventFiringWebDriver
				&& ((CustomEventFiringWebDriver)(uxDriverSession.get().get(driverName).driver)).isBrowserOrAppClosed()) {
    		throw new ScenarioException("Cannot switch to a closed driver");
    	}

    	setCurrentWebUiDriverName(driverName);
    	
    	scenarioLogger.info(String.format("Switching to driver named '%s'", driverName));
    }

    /**
     * Returns current WebUIDriver instance
     * @param create	create instance if it does not exist in this thread. Beware that this instance will have to be deleted at the end of test
     * 					(for regular seleniumRobot tests, this is done in <code>SeleniumRobotTestPlan.finishTestMethod</code>
     *
     * @return
     */
    public static WebUIDriver getWebUIDriver(boolean create) {
    	return getWebUIDriver(create, getCurrentWebUiDriverName());
    }
    
    /**
     * Returns current WebUIDriver instance for given name
     * If a new WebUIDriver instance is created, this become the current one
     * @param create	create instance if it does not exist in this thread. Beware that this instance will have to be deleted at the end of test
     * 					(for regular seleniumRobot tests, this is done in <code>SeleniumRobotTestPlan.finishTestMethod</code>
     * @param name		the name of the driver instance to retrieve (default is 'main', the first created driver)
     * @return
     */
    public static WebUIDriver getWebUIDriver(boolean create, String name) {
        if ((uxDriverSession.get() == null || uxDriverSession.get().get(name) == null) && create) {
            WebUIDriver uiDriver = WebUIDriverFactory.getInstance(name);
			uiDriver.initInstance();
        }
        
        try {
        	return uxDriverSession.get().get(name);
        } catch (NullPointerException e) {
        	return null;
        }
    }

	public void initInstance() {
		setConfig(new DriverConfig(SeleniumTestsContextManager.getThreadContext()));

		if (WebUIDriver.getUxDriverSession().get() == null) {
			WebUIDriver.getUxDriverSession().set(new HashMap<>());
		}
		WebUIDriver.getUxDriverSession().get().put(name, this);

		WebUIDriver.switchToDriver(name);
	}

	/**
     * Lets user set their own driver This can be retrieved as WebUIDriver.getWebDriver().
     *
     * @param  driver
     */
    public static void setWebDriver(final WebDriver driver) {
        if (driver == null) {
        	if (uxDriverSession.get() != null && uxDriverSession.get().get(getCurrentWebUiDriverName()) != null) {
        		uxDriverSession.get().get(getCurrentWebUiDriverName()).driver = null;
        	}
        } else {
        	// create WebUiDriver if it does not exist
            getWebUIDriver(true);
            uxDriverSession.get().get(getCurrentWebUiDriverName()).driver = driver;
        }
    }

    protected WebDriver handleListeners(WebDriver driver, BrowserInfo browserInfo, List<Long> driverPids) {

    	// Use of Neoload
    	// we must configure the user path, and the proxy mode
    	// In this case, the read driver is "replaced" by the proxy to the driver
    	if (config.isNeoloadActive()) {
    		driver = NLWebDriverFactory.newNLWebDriver(driver, config.getNeoloadUserPath());
    	}
    	
    	WebDriver listeningDriver = new CustomEventFiringWebDriver(driver, 
																	driverPids, 
																	browserInfo, 
																	SeleniumTestsContextManager.getThreadContext().getTestType(),
																	SeleniumTestsContextManager.getThreadContext().getRunMode(),
																	SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector(),
																	config.getAttachExistingDriverPort(), 
																	config.getWebDriverListeners());

        

        return listeningDriver;
    }
    
    private void checkBrowserRunnable() {
		if (config.getMode() == DriverMode.LOCAL && !useAppium()) {
			Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion(config.getBetaBrowser());
			if (!browsers.containsKey(config.getBrowserType()) || browsers.get(config.getBrowserType()).isEmpty()) {
				throw new ConfigurationException(String.format("Browser %s is not available. Available browsers are %s",
						config.getBrowserType(), browsers));
			}

			boolean browserFound = false;
			for (BrowserInfo browserInfo : browsers.get(config.getBrowserType())) {

				if (config.getBetaBrowser().equals(browserInfo.getBeta())) {
					browserFound = true;
					break;
				}
			}
			if (!browserFound) {
				throw new ConfigurationException(String.format("Browser %s %s is not available. Available browsers are %s",
						config.getBrowserType(), config.getBetaBrowser() ? "beta" : "", browsers));

			}
		}
	}

    /**
     * Get version from browser capabilities and display it
     */
    private void displayBrowserVersion() {
    	if (driver == null || !(driver instanceof CustomEventFiringWebDriver)) {
    		return;
    	}
    	Capabilities caps = ((CustomEventFiringWebDriver) driver).getCapabilities();
        String browserName = caps.getBrowserName();
        String browserVersion = caps.getBrowserVersion(); 
        
        Integer majorVersion;
        try {
        	majorVersion = Integer.parseInt(browserVersion.split("\\.")[0]);
        } catch (NumberFormatException e) {
        	majorVersion = 1;
        }
        config.setMajorBrowserVersion(majorVersion);
        logger.info(String.format("Browser is: %s %s", browserName, browserVersion));
    }

    /**
     * create the driver
     * @return the driver
     * @throws ScenarioException in case we are not allowed to create it (if we are in @BeforeMethod and after @AfterMethod)
     */
    public WebDriver createWebDriver() {
    	
    	if (SeleniumTestsContextManager.getThreadContext().isDriverCreationBlocked()) {
    		throw new ScenarioException("Driver creation forbidden before @BeforeMethod and after @AfterMethod execution");
    	}
    	
    	if (useAppium()) {
    		logger.info("Start creating appium driver");
    	} else {
    		logger.info(String.format("Start creating %s driver", config.getBrowserType().getBrowserType()));
    	}
    	checkBrowserRunnable();
        driver = createRemoteWebDriver();

		if (config.getTestType().family().equals(TestType.WEB)) {
			displayBrowserVersion();
		}
 
        if (useAppium()) {
    		logger.info("Finished creating appium driver");
    	} else {
    		logger.info(String.format("Finished creating %s driver", config.getBrowserType().getBrowserType()));
    	}

        return driver;
    }

	/**
	 * Use appium for mobile web or any application test
	 * @return
	 */
	private boolean useAppium() {
		return config.getTestType().isMobile() || config.getTestType().family().equals(TestType.APP);
	}

	public static void main(final String[] args) {
        logger.info(DriverExceptionListener.class.getName());
    }

    public boolean isSetAcceptUntrustedCertificates() {
        return config.isSetAcceptUntrustedCertificates();
    }

    public boolean isSetAssumeUntrustedCertificateIssuer() {
        return config.isSetAssumeUntrustedCertificateIssuer();
    }
    
	public DriverConfig getConfig() {
		return config;
	}

	/**
	 * Use only with tests
	 * @param config
	 */
	public void setConfig(DriverConfig config) {
		this.config = config;
	}

	public static ThreadLocal<VideoRecorder> getVideoRecorder() {
		return videoRecorder;
	}
	
	/**
	 * Get the video recorder for this thread
	 * May be null if no driver has been initialized
	 * @return
	 */
	public static VideoRecorder getThreadVideoRecorder() {
		if (videoRecorder != null) {
			return videoRecorder.get();
		}
		return null;
	}
	
	
	public static void resetCurrentWebUiDriverName() {
		setCurrentWebUiDriverName(DEFAULT_DRIVER_NAME);
	}
	
	public static void setCurrentWebUiDriverName(String name) {
		currentWebUiDriverName.set(name);
	}

	public static String getCurrentWebUiDriverName() {
		if (currentWebUiDriverName.get() == null) {
			setCurrentWebUiDriverName(DEFAULT_DRIVER_NAME);
		}
		return currentWebUiDriverName.get();
	}

	/**
	 * Gets the driver created inside this WebUIDriver
	 * @return
	 */
	public WebDriver getDriver() {
		return driver;
	}

	/**
	 * FOR TEST ONLY
	 * @param driver
	 */
	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}

	public static ThreadLocal<Map<String, WebUIDriver>> getUxDriverSession() {
		return uxDriverSession;
	}
}
