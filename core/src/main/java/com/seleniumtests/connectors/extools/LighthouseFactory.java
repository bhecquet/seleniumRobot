package com.seleniumtests.connectors.extools;


import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.Browser;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.driver.WebUIDriver;

public class LighthouseFactory {

	private static final Pattern DEBUG_PORT_PATTERN = Pattern.compile("localhost:(\\d+)");
	
	public static Lighthouse getInstance() {
		WebDriver driver = WebUIDriver.getWebDriver(false);
		
		// check for remote debugging port
		Capabilities caps = ((CustomEventFiringWebDriver) driver).getCapabilities();
		
		String cdpOption = (String) caps.getCapability("se:cdp");
		if (cdpOption == null || cdpOption.isEmpty()) {
			throw new ConfigurationException("Lighthouse can only be used on chromium browsers");
		}
		
		// compute output directory
		String outputDir;
		if (SeleniumTestsContextManager.getThreadContext().getRunMode() == DriverMode.LOCAL) {
			outputDir = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "lighthouseOut").toString();
			
		// expect that working directory of grid is the root folder
		} else if (SeleniumTestsContextManager.getThreadContext().getRunMode() == DriverMode.GRID) {
			outputDir = "upload/lighthouseOut";
		} else {
			throw new ConfigurationException("Lighthouse can only be run on local or on SeleniumRobot grid");
		}
		
		// create output directory
		Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "lighthouse").toFile().mkdirs();
		
		String debuggerAddress = "";
		try {
			if (Browser.CHROME.is(caps)) {
				debuggerAddress = ((Map<?,?>) caps.asMap().get(ChromeOptions.CAPABILITY)).get("debuggerAddress").toString();
			} else if (Browser.EDGE.is(caps)) {
				debuggerAddress = ((Map<?,?>) caps.asMap().get(EdgeOptions.CAPABILITY)).get("debuggerAddress").toString();
			}
		} catch (NullPointerException e) {
			debuggerAddress = "";
		}
		
		
		Matcher portMatcher = DEBUG_PORT_PATTERN.matcher(debuggerAddress);
		if (portMatcher.matches()) {
			return new Lighthouse(Integer.parseInt(portMatcher.group(1)), outputDir);
		} else {
			throw new ConfigurationException("Lighthouse cannot be used with the browser, Chrome dev tools not present");
		}
	}
}
