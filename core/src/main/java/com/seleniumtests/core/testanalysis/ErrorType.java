package com.seleniumtests.core.testanalysis;


public enum ErrorType {
	
	ERROR_MESSAGE("Error message displayed"),				// error message displayed
	ERROR_IN_FIELD("Field in error"),						// some field shows an error (it's coloured in red)
	APPLICATION_CHANGED("The application has been modified"),	// compared to the page we expect, the page we are on is slightly different
	SELENIUM_ERROR("Error in selenium operation"),			// we are not on the right page to perform our actions, it may be due to a problem when clicking during previous step
	UNKNOWN_PAGE("This page has never been encountered");	// Page is unknown, maybe a scripting error ?
	

	private String description;
	
	ErrorType(String description) {
		this.description = description;
	}
	
	public String toString() {
		return description;
	}
}
