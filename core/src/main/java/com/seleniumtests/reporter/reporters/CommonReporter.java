/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.reporter.reporters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.util.StringUtility;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public abstract class CommonReporter implements IReporter {
	
	private static final String RESOURCE_LOADER_PATH = "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader";
	private String uuid = new GregorianCalendar().getTime().toString();
	protected static Logger logger = SeleniumRobotLogger.getLogger(CommonReporter.class);
	
	protected static final String FAILED_TEST = "failed";
	protected static final String SKIPPED_TEST = "skipped";
	protected static final String PASSED_TEST = "passed";
	protected static final String RESOURCES_DIR = "resources";

	public static CommonReporter getInstance(Class<?> reporterClass) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return (CommonReporter) reporterClass.getConstructor().newInstance();
	}
	
	protected abstract void generateReport(Map<ITestContext, Set<ITestResult>> resultSet, String outdir, boolean optimizeReport);
	
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
	 * @param exception		Exception to format
	 * @param title			title of the exception
	 * @param contentBuffer	
	 * @param format		format to use to encode ('html', 'csv', 'xml', 'json')
	 */
	public static void generateTheStackTrace(final Throwable exception, final String title, final StringBuilder contentBuffer, String format) {
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
	 * Returns the visible test name. It depends if test has been skipped or not
	 * skipped tests has never been executed and so attribute (set in TestListener) has not been applied
	 * @param testResult
	 * @return
	 */
	public static String getTestName(ITestResult testResult) {
		if (testResult == null) {
			return "N-A";
		}
		
		if (TestNGResultUtils.getUniqueTestName(testResult) != null) {
			return TestNGResultUtils.getUniqueTestName(testResult);
		}
		
		// when test is skipped, UNIQUE_METHOD_NAME may have not been generated
		if (testResult.getStatus() == ITestResult.SKIP) {
			return testResult.getName();
		} else {
			return "N-A";
		}
	}
	

	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		generateReport(getResultMapFromSuites(suites), outputDirectory, SeleniumTestsContextManager.getGlobalContext().getOptimizeReports());
	}
	
	/**
	 * Returns a Map from resultsSet (per testContext) from list of suites
	 * @param suites
	 * @return
	 */
	public Map<ITestContext, Set<ITestResult>> getResultMapFromSuites(List<ISuite> suites) {
		Map<ITestContext, Set<ITestResult>> methodResultsMap = new LinkedHashMap<>();
		
		for (ISuite suite : suites) {
			Map<String, ISuiteResult> tests = suite.getResults();
			for (ISuiteResult r : tests.values()) {
				ITestContext context = r.getTestContext();

				Set<ITestResult> methodResults = new HashSet<>();
				methodResults.addAll(context.getFailedTests().getAllResults());
				methodResults.addAll(context.getPassedTests().getAllResults());
				methodResults.addAll(context.getSkippedTests().getAllResults());
				
				methodResultsMap.put(context, methodResults);
			}
		}
		
		return methodResultsMap;
	}
	
	public String getClassName(final ITestResult testResult) {
		if (testResult.getTestClass() != null) {
			return testResult.getTestClass().getName();
		} else {
			return "noclass";
		}
	}
}
