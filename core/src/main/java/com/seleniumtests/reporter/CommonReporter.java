package com.seleniumtests.reporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.internal.TestResult;
import org.testng.internal.Utils;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public abstract class CommonReporter {
	
	private static final String RESOURCE_LOADER_PATH = "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader";
	private String uuid = new GregorianCalendar().getTime().toString();
	protected static Logger logger = SeleniumRobotLogger.getLogger(CommonReporter.class);
	
	protected static final String FAILED_TEST = "failed";
	protected static final String SKIPPED_TEST = "skipped";
	protected static final String PASSED_TEST = "passed";
	protected static final String RESOURCES_DIR = "resources";

	/**
	 * Initializes the VelocityEngine
	 * @return
	 * @throws Exception
	 */
	protected VelocityEngine initVelocityEngine() throws Exception {
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty("resource.loader", "class");
		ve.setProperty("class.resource.loader.class", RESOURCE_LOADER_PATH);
		ve.init();
		return ve;
	}
	

	/**
	 * create writer used for writing report file
	 * @param outDir
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	protected PrintWriter createWriter(final String outDir, final String fileName) throws IOException {
		System.setProperty("file.encoding", "UTF8");
		uuid = uuid.replaceAll(" ", "-").replaceAll(":", "-");

		File f = new File(outDir, fileName);
		logger.info("generating report " + f.getAbsolutePath());

		OutputStream out = new FileOutputStream(f);
		Writer writer = new BufferedWriter(new OutputStreamWriter(out, "utf-8"));
		return new PrintWriter(writer);
	}
	
	/**
	 * Remove useless stacktrace elements (e.g: sun.reflect)
	 * @param message
	 * @return
	 */
	private static String filterStackTrace(String message) {
		if (message.startsWith("sun.reflect.")
			|| message.startsWith("java.lang.reflect")
			|| message.startsWith("org.testng.")
			|| message.startsWith("java.lang.Thread")
			|| message.startsWith("java.util.concurrent.")
			|| message.startsWith("org.aspectj.runtime.reflect")
				) {
			return null;
		}
		return message;
	}
	
	/**
	 * Method to generate the formated stacktrace
	 * @param exception
	 * @param title
	 * @param contentBuffer
	 */
	public static void generateTheStackTrace(final Throwable exception, final String title, final StringBuilder contentBuffer) {
		contentBuffer.append(exception.getClass() + ": " + title + "\n");

		StackTraceElement[] s1 = exception.getStackTrace();
		Throwable t2 = exception.getCause();
		if (t2 == exception) {
			t2 = null;
		}
		for (int x = 0; x < s1.length; x++) {
			String message = filterStackTrace(s1[x].toString());
			if (message != null) {
				contentBuffer.append("\nat " + message);
			}
		}

		if (t2 != null) {
			generateTheStackTrace(t2, "Caused by " + t2.getLocalizedMessage(), contentBuffer);
		}
	}
}
