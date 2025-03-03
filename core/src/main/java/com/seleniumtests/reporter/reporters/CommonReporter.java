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
import java.nio.charset.StandardCharsets;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seleniumtests.driver.BrowserType;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.util.ExceptionUtility;
import com.seleniumtests.util.StringUtility;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public abstract class CommonReporter implements IReporter {
	
	private static final String RESOURCE_LOADER_PATH = "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader";
	private String uuid = new GregorianCalendar().getTime().toString();
	protected static Logger logger = SeleniumRobotLogger.getLogger(CommonReporter.class);
	
	protected static final String FAILED_TEST = "failed";
	protected static final String WARN_TEST = "warning";
	protected static final String SKIPPED_TEST = "skipped";
	protected static final String PASSED_TEST = "passed";
	protected static final String RESOURCES_DIR = "resources";

	public static CommonReporter getInstance(Class<?> reporterClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return (CommonReporter) reporterClass.getConstructor().newInstance();
	}
	
	protected void generateReport(Map<ITestContext, Set<ITestResult>> resultSet, String outdir, boolean optimizeReport) {
		generateReport(resultSet, outdir, optimizeReport, false);
	}

	/**
	 * This method is called only when all test suite has been executed
	 */
	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		generateReport(getResultMapFromSuites(suites), outputDirectory, SeleniumTestsContextManager.getGlobalContext().getOptimizeReports(), true);
	}
	
	/**
	 * Generate report on this reporter
	 * @param resultSet			the map of results
	 * @param outdir			where to write results
	 * @param optimizeReport	should we optimize reports. For HTML reporter, it means that size would be reduced if 'true' 
	 * @param finalGeneration	'true' if this report generation is done at the very end of all test suite execution
	 */
	protected abstract void generateReport(Map<ITestContext, Set<ITestResult>> resultSet, String outdir, boolean optimizeReport, boolean finalGeneration);
	
	/**
	 * Initializes the VelocityEngine
	 * @return
	 * @throws Exception
	 */
	protected VelocityEngine initVelocityEngine() {
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty("resource.loaders", "class");
		ve.setProperty("resource.loader.class.class", RESOURCE_LOADER_PATH);
		ve.setProperty("directive.set.null.allowed", true);
		ve.init();
		return ve;
	}
	

	/**
	 * create writer used for writing report file
	 * @param outDir
	 * @param fileName
	 * @return 
	 * @throws IOException
	 * @deprecated  using FileUtils instead
	 */
	@Deprecated
	protected PrintWriter createWriter(final String outDir, final String fileName) throws IOException {
		System.setProperty("file.encoding", "UTF8");
		uuid = uuid.replace(" ", "-").replace(":", "-");

		File f = new File(outDir, fileName);
		logger.info("generating report " + f.getAbsolutePath());

		try (OutputStream out = new FileOutputStream(f)) {
			Writer writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
			return new PrintWriter(writer); 
		}
		
	}
	
	protected void generateReport(File f, String content) throws IOException {

		logger.info("generating report " + f.getAbsolutePath());
		FileUtils.write(f, content, StandardCharsets.UTF_8);
	}
	
	
	/**
	 * Method to generate the formated stacktrace
	 * @param exception		Exception to format
	 * @param title			title of the exception
	 * @param contentBuffer	
	 * @param format		format to use to encode ('html', 'csv', 'xml', 'json')
	 * @deprecated use {@link ExceptionUtility.generateTheStackTrace()}
	 * 
	 */
	@Deprecated
	public static void generateTheStackTrace(final Throwable exception, final String title, final StringBuilder contentBuffer, String format) {
		contentBuffer.append(exception.getClass() + ": " + StringUtility.encodeString(title, format) + "\n");

		StackTraceElement[] s1 = exception.getStackTrace();
		Throwable t2 = exception.getCause();
		if (t2 == exception) {
			t2 = null;
		}
		for (int x = 0; x < s1.length; x++) {
			String message = ExceptionUtility.filterStackTrace(s1[x].toString());
			if (message != null) {
				contentBuffer.append("\nat " + StringUtility.encodeString(message, format));
			}
		}

		if (t2 != null) {
			generateTheStackTrace(t2, "Caused by " + t2.getLocalizedMessage(), contentBuffer, format);
		}
	}
	
	/**
	 * Returns the unique test name 
	 * It's based on unique name meaning that if same test has been executed several times through dataprovider, name is suffixed by "-1, -2, ...". 
	 * It depends also if test has been skipped or not
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
		
		// In case of an error occurring on the SeleniumRobot Server, we can reach this point with a test not skipped but
		// with a testName that we can return instead of N-A.
		return getTestResultName(testResult);
	}
	
	/**
	 * Returns the visual test name. This name should be used only when presenting test name to a human (HTML / JUnit report), not used for technical communication
	 * It's based on visual name meaning that if same test has been executed several times through dataprovider, name is suffixed by "-1, -2, ...". 
	 * It depends also if test has been skipped or not
	 * skipped tests has never been executed and so attribute (set in TestListener) has not been applied
	 * @param testResult
	 * @return
	 */
	public static String getVisualTestName(ITestResult testResult) {
		if (testResult == null) {
			return "N-A";
		}
		
		if (TestNGResultUtils.getVisualTestName(testResult) != null) {
			return TestNGResultUtils.getVisualTestName(testResult);
		}
		
		// In case of an error occurring on the SeleniumRobot Server, we can reach this point with a test not skipped but
		// with a testName that we can return instead of N-A.
		return getTestResultName(testResult);
		
	}
	
	/**
	 * Returns the ITestResult object's name. It's useful in case of an error occurring during test configuration.
	 * At this point, the test won't have a UniqueTestName nor a VisualTestName so this one will do the trick.
	 * If the test fails really early or is broken and can't be named, it returns N-A.
	 * @param testResult
	 * @return a String containing the testResult name
	 */
	public static String getTestResultName(ITestResult testResult) {		
		try {
			if (testResult.getName() != null && !testResult.getName().isEmpty()) {
				return testResult.getName();
			} else {
				return "N-A";
			}
		} catch (Exception e) {
			return "N-A";
		}
	}
	
	/**
	 * Returns the test method name. Contrary to getTestName() method, even if the test has been executed several time through data provider, name remains the same as it's based on method name. 
	 * It depends also if test has been skipped or not
	 * skipped tests has never been executed and so attribute (set in TestListener) has not been applied
	 * @param testResult
	 * @return
	 */
	public static String getTestCaseName(ITestResult testResult) {
		if (testResult == null) {
			return "N-A";
		}
		
		if (TestNGResultUtils.getTestMethodName(testResult) != null) {
			return TestNGResultUtils.getTestMethodName(testResult);
		}
		
		// when test is skipped, UNIQUE_METHOD_NAME may have not been generated
		if (testResult.getStatus() == ITestResult.SKIP) {
			return testResult.getName();
		} else {
			return "N-A";
		}
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
	public static String getBrowserOrApp() {
		String browserOrApp;
		if (SeleniumTestsContextManager.isAppTest()) {
			String appPath = SeleniumTestsContextManager.getGlobalContext().getApp() == null ? "noApp": SeleniumTestsContextManager.getGlobalContext().getApp();
			browserOrApp = "APP:" + new File(appPath).getName();
		} else {
			BrowserType browser = SeleniumTestsContextManager.getGlobalContext().getBrowser();
			browser = browser == null ? BrowserType.NONE : browser;
			browserOrApp = "BROWSER:" + browser;
		}
		return browserOrApp;
	}
}
