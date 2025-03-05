package com.seleniumtests.customexception;

/**
 * An application error is used when the application under test is in error
 * This tells SeleniumRobot not to replay test so that application error is not masked by a retry
 * Should be used by tests when application does not behave as expected (except for element not present)
 * - an error message is displayed
 * - a page does not display
 */
public class ApplicationError  extends RuntimeException {

    public ApplicationError(String message) {
        super(message);
    }
}
