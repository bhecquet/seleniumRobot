package com.seleniumtests.customexception;

public class ImageSearchException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImageSearchException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
    
    public ImageSearchException(final String message) {
    	super(message);
    }
}
