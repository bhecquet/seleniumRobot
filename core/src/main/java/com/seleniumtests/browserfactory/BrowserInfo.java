package com.seleniumtests.browserfactory;

import com.seleniumtests.driver.BrowserType;

public class BrowserInfo {

	private String version;
	private String path;
	private BrowserType browser;
	
	public BrowserInfo(BrowserType browser, String version, String path) {
		this.browser = browser;
		this.path = path;
		this.version = version;
	}
	
	public String getVersion() {
		return version;
	}
	public String getPath() {
		return path;
	}

	public BrowserType getBrowser() {
		return browser;
	}

}
