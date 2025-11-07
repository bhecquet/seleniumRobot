package com.seleniumtests.util;

import org.openqa.selenium.WebDriverException;

public class ExceptionUtility {

	private ExceptionUtility() {
		// nothing to do
	}
	
	/**
	 * return the exception message
	 * Remove extra information in case of WebDriverException
	 * @param exception	the exception to modify
	 */
	public static String getExceptionMessage(Throwable exception) {
		if (exception == null) {
			return "";
		}
		String message = exception.getClass().toString() + ": " + exception.getMessage();
		if (exception instanceof WebDriverException) {
			message = message.split("For documentation on this error")[0];
		} 
		return message;
	}
	

	/**
	 * Method to generate the formated stacktrace
	 * @param exception		Exception to format
	 * @param title			title of the exception
	 * @param contentBuffer	buffer
	 * @param format		format to use to encode ('html', 'csv', 'xml', 'json')
	 */
	public static void generateTheStackTrace(Throwable exception, String title, StringBuilder contentBuffer, String format) {
		contentBuffer.append(exception.getClass()).append(": ")
				.append(StringUtility.encodeString(title, format))
				.append("\n");

		StackTraceElement[] s1 = exception.getStackTrace();
		Throwable t2 = exception.getCause();
		if (t2 == exception) {
			t2 = null;
		}
        for (StackTraceElement stackTraceElement : s1) {
            String message = filterStackTrace(stackTraceElement.toString());
            if (message != null) {
                contentBuffer.append("\nat ")
                        .append(StringUtility.encodeString(message, format));
            }
        }

		if (t2 != null) {
			generateTheStackTrace(t2, "Caused by " + t2.getLocalizedMessage(), contentBuffer, format);
		}
	}
	

	/**
	 * Remove useless stacktrace elements (e.g: sun.reflect)
	 * @param message	the stacktrace element
	 * @return null if we filter the message or the message ifself
	 */
	public static String filterStackTrace(String message) {
		if (message.startsWith("sun.reflect.")
			|| message.startsWith("java.lang.reflect")
			|| message.startsWith("org.testng.")
			|| message.startsWith("java.lang.Thread")
			|| message.startsWith("java.util.concurrent.")
			|| message.startsWith("org.aspectj.runtime.reflect")
			|| message.startsWith("org.apache.maven.")
			|| message.startsWith("java.base/")
				) {
			return null;
		}
		return message;
	}
}
