package com.seleniumtests.ut.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.WebDriverListener;

public class WebDriverListener1 implements WebDriverListener {

	
	public static boolean called = false;
	

	public void beforeGetSize(WebDriver.Window window) {
		WebDriverListener1.called = true;
	}


	public static void setCalled(boolean called) {
		WebDriverListener1.called = called;
	}


	public static boolean isCalled() {
		return called;
	}
	
	
}
