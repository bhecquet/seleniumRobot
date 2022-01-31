package com.seleniumtests.util;

import org.openqa.selenium.WebDriverException;

public class ExceptionUtility {

	private ExceptionUtility() {
		// nothing to do
	}
	
	/**
	 * return the exception message
	 * Remove extra information in case of WebDriverException
	 * @param exception
	 * @return
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
	 * @param contentBuffer	
	 * @param format		format to use to encode ('html', 'csv', 'xml', 'json')
	 */
	public static void generateTheStackTrace(Throwable exception, String title, StringBuilder contentBuffer, String format) {
		contentBuffer.append(exception.getClass() + ": " + StringUtility.encodeString(title, format) + "\n");

		StackTraceElement[] s1 = exception.getStackTrace();
		Throwable t2 = exception.getCause();
		if (t2 == exception) {
			t2 = null;
		}
		for (int x = 0; x < s1.length; x++) {
			String message = filterStackTrace(s1[x].toString());
			if (message != null) {
				contentBuffer.append("\nat " + StringUtility.encodeString(message, format));
			}
		}

		if (t2 != null) {
			generateTheStackTrace(t2, "Caused by " + t2.getLocalizedMessage(), contentBuffer, format);
		}
	}
	

	/**
	 * Remove useless stacktrace elements (e.g: sun.reflect)
	 * @param message
	 * @return
	 */
	public static String filterStackTrace(String message) {
		if (message.startsWith("sun.reflect.")
			|| message.startsWith("java.lang.reflect")
			|| message.startsWith("org.testng.")
			|| message.startsWith("java.lang.Thread")
			|| message.startsWith("java.util.concurrent.")
			|| message.startsWith("org.aspectj.runtime.reflect")
			|| message.startsWith("org.apache.maven.")
				) {
			return null;
		}
		return message;
	}
}
