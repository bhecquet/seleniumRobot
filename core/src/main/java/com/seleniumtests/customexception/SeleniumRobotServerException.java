package com.seleniumtests.customexception;

public class SeleniumRobotServerException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SeleniumRobotServerException(final String message) {
        super(message);
    }

    public SeleniumRobotServerException(final Throwable cause) {
        super(cause);
    }

    public SeleniumRobotServerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
