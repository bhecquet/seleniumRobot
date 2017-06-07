package com.seleniumtests.customexception;

public class SeleniumGridException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SeleniumGridException(final String message) {
        super(message);
    }

    public SeleniumGridException(final Throwable cause) {
        super(cause);
    }

    public SeleniumGridException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
