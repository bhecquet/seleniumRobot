package com.seleniumtests.customexception;

/**
 * Exception raised when some tools are not installed
 * @author behe
 *
 */
public class DisabledConnector extends RuntimeException {

	private static final long serialVersionUID = 1L;
	public DisabledConnector(final String message, final Throwable throwable) {
        super(message, throwable);
    }
    public DisabledConnector(final String message) {
    	super(message);
    }
}
