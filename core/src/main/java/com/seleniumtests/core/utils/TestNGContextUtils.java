package com.seleniumtests.core.utils;

import org.testng.ITestContext;

public class TestNGContextUtils {

	private static final String TEST_SESSION_ON_SERVER = "testSessionOnServer"; // ID of the TestSession when it has been created on the seleniumRobot server
	
	private TestNGContextUtils() {
		// nothing to do
	}
	
	/**
	 * Get attribute for test sessionId created on seleniumRobot server
	 * @param testContext
	 * @return
	 */
	public static Integer getTestSessionCreated(ITestContext testContext) {
		return (Integer) testContext.getAttribute(TEST_SESSION_ON_SERVER); 
    }
    
	/**
	 * Sets the 'sessionId' attribute for the context
	 * @param testContext
	 * @param sessionCreated
	 */
    public static void setTestSessionCreated(ITestContext testContext, Integer sessionId) {
    	testContext.setAttribute(TEST_SESSION_ON_SERVER, sessionId);
    }
}
