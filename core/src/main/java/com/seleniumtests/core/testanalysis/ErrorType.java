package com.seleniumtests.core.testanalysis;


public enum ErrorType {
	ERROR_MESSAGE,			// error message displayed
	ERROR_IN_FIELD,			// some field shows an error (it's coloured in red)
	APPLICATION_CHANGED,	// compared to the page we expect, the page we are on is slightly different
	SELENIUM_ERROR,			// we are not on the right page to perform our actions, it may be due to a problem when clicking during previous step
	UNKNOWN_PAGE			// Page is unknown, maybe a scripting error ?
}
