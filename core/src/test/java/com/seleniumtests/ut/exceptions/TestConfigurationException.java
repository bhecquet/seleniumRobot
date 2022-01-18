package com.seleniumtests.ut.exceptions;

public class TestConfigurationException extends RuntimeException {

	public TestConfigurationException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
    public TestConfigurationException(final String message) {
    	super(message);
    }
}
