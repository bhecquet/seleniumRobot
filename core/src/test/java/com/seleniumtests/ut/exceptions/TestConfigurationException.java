package com.seleniumtests.ut.exceptions;

public class TestConfigurationException extends RuntimeException {


	private static final long serialVersionUID = 1564531354L;
	
	public TestConfigurationException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
    public TestConfigurationException(final String message) {
    	super(message);
    }
}
