package com.seleniumtests;

import java.net.Inet4Address;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.Logger;

import com.seleniumtests.it.driver.support.server.WebServer;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class WebTestPageServer {
	
	private final Logger logger = SeleniumRobotLogger.getLogger(WebTestPageServer.class);

	private WebServer server;
	protected String localAddress;

	public WebTestPageServer() {
		
	}

	/**
	 * Method for returning mapping of files stored in resources, with path on server
	 * @return
	 */
	private Map<String, String> getPageMapping() {
		Map<String, String> mapping = new HashMap<>();
		mapping.put("/tu/test.html", "/test.html");
		mapping.put("/tu/testScrolling.html", "/testScrolling.html");
		mapping.put("/tu/testWithoutFixedPattern.html", "/testWithoutFixedPattern.html");
		mapping.put("/tu/testIFrame.html", "/testIFrame.html");
		mapping.put("/tu/testAngularIFrame.html", "/testAngularIFrame.html");
		mapping.put("/tu/testIFrame2.html", "/testIFrame2.html");
		mapping.put("/tu/testIFrame3.html", "/testIFrame3.html");
		mapping.put("/tu/ffLogo1.png", "/ffLogo1.png");
		mapping.put("/tu/ffLogo2.png", "/ffLogo2.png");
		mapping.put("/tu/googleSearch.png", "/googleSearch.png");
		mapping.put("/tu/images/bouton_enregistrer.png", "/images/bouton_enregistrer.png");
		mapping.put("/tu/jquery.min.js", "/jquery.min.js");

		// web pages for IA tests
		mapping.put("/tu/pagesApp/test.html", "/testIA.html");
		mapping.put("/tu/pagesApp/testCharging.html", "/testCharging.html");
		mapping.put("/tu/pagesApp/testMissingElement.html", "/testMissingElement.html");

		
		// angular app v9
		mapping.put("/tu/angularAppv9/index.html", "/angularApp/index.html");
		mapping.put("/tu/angularAppv9/runtime-es2015.js", "/angularApp/runtime-es2015.js");
		mapping.put("/tu/angularAppv9/runtime-es5.js", "/angularApp/runtime-es5.js");
		mapping.put("/tu/angularAppv9/main-es5.js", "/angularApp/main-es5.js");
		mapping.put("/tu/angularAppv9/main-es2015.js", "/angularApp/main-es2015.js");
		mapping.put("/tu/angularAppv9/polyfills-es2015.js", "/angularApp/polyfills-es2015.js");
		mapping.put("/tu/angularAppv9/polyfills-es5.js", "/angularApp/polyfills-es5.js");
		mapping.put("/tu/angularAppv9/styles.css", "/angularApp/styles.css");
		
		return mapping;
	}
	
	public void exposeTestPage() throws Exception {

		localAddress = Inet4Address.getLocalHost().getHostAddress();
//		localAddress = Inet4Address.getByName("localhost").getHostAddress();
        server = new WebServer(localAddress, getPageMapping());
        server.expose();
        logger.info(String.format("exposing server on http://%s:%d", localAddress, server.getServerHost().getPort()));

	}
	
	public HttpHost getServerHost() {
		return server.getServerHost();
	}
	
	public void stopServer() throws Exception {
		if (server != null) {
			logger.info("stopping web server");
			server.stop();
		}
		localAddress = null;
	}

	public String getLocalAddress() {
		return localAddress;
	}
}