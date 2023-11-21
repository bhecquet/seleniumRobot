package com.seleniumtests.customexception;

/**
 * A DriverException that tells WebUIDriver it's allowed to retry creation
 */
public class RetryableDriverException extends DriverExceptions {
    public RetryableDriverException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public RetryableDriverException(String message) {
        super(message);
    }
}
