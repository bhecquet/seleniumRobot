package com.seleniumtests.reporter.reporters;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.collections.Lists;
import org.testng.internal.Utils;
import org.testng.reporters.XMLConstants;
import org.testng.reporters.XMLStringBuffer;

import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.util.ExceptionUtility;
import com.seleniumtests.util.StringUtility;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class JUnitReporter extends CommonReporter {
	
	private static Logger logger = SeleniumRobotLogger.getLogger(JUnitReporter.class);
	
	@Override
	protected void generateReport(Map<ITestContext, Set<ITestResult>> resultSet, final String outdir, boolean optimizeReport, boolean finalGeneration) {
		for (Entry<ITestContext, Set<ITestResult>> entry: resultSet.entrySet()) {
			generateTestReport(entry.getKey(), entry.getValue(), outdir);
		}
	}
	
	private void generateTestReport(ITestContext testContext, Set<ITestResult> testResults, String outdir) {

		Properties p1 = new Properties();
		p1.setProperty(XMLConstants.ATTR_NAME, testContext.getName());
		p1.setProperty(XMLConstants.ATTR_TIMESTAMP, timeAsGmt());

		List<TestTag> testCases = Lists.newArrayList();
		int failures = 0;
		int errors = 0;
		int skipped = 0;
		int testCount = 0;
		float totalTime = 0;

		Collection<ITestResult> iTestResults = sort(testResults);

		for (ITestResult tr : iTestResults) {

			long time = tr.getEndMillis() - tr.getStartMillis();

			Throwable t = tr.getThrowable();
			switch (tr.getStatus()) {
				case ITestResult.SKIP:
				case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
					skipped++;
					break;
	
				case ITestResult.FAILURE:
					if (t instanceof AssertionError) {
						failures++;
					} else {
						errors++;
					}
					break;
				default:
					// nothing to do
			}

			totalTime += time;
			testCount++;
			TestTag testTag = createTestTagFor(tr, tr.getMethod().getTestClass().getName());
			testTag.properties.setProperty(XMLConstants.ATTR_TIME, "" + formatTime(time));
			testCases.add(testTag);
		}
		int ignored = getDisabledTestCount(testContext.getExcludedMethods());
		
		p1.setProperty(XMLConstants.ATTR_FAILURES, Integer.toString(failures));
		p1.setProperty(XMLConstants.ATTR_ERRORS, Integer.toString(errors));
		p1.setProperty(XMLConstants.SKIPPED, Integer.toString(skipped + ignored));
		p1.setProperty(XMLConstants.ATTR_NAME, testContext.getName());
		p1.setProperty(XMLConstants.ATTR_TESTS, Integer.toString(testCount + ignored));
		p1.setProperty(XMLConstants.ATTR_TIME, "" + formatTime(totalTime));
		try {
			p1.setProperty(XMLConstants.ATTR_HOSTNAME, InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			// ignore
		}

		//
		// Now that we have all the information we need, generate the file
		//
		XMLStringBuffer xsb = new XMLStringBuffer();

		xsb.push(XMLConstants.TESTSUITE, p1);
		for (TestTag testTag : testCases) {
			if (putElement(xsb, XMLConstants.TESTCASE, testTag.properties, testTag.childTag != null)) {
				Properties p = new Properties();
				safeSetProperty(p, XMLConstants.ATTR_MESSAGE, testTag.message);
				safeSetProperty(p, XMLConstants.ATTR_TYPE, testTag.type);

				if (putElement(xsb, testTag.childTag, p, testTag.stackTrace != null)) {
					xsb.addCDATA(testTag.stackTrace);
					xsb.pop(testTag.childTag);
				}
				if (putElement(xsb, testTag.logTag, p, testTag.logs != null)) {
					xsb.addCDATA(testTag.logs);
					xsb.pop(testTag.logTag);
				}
				xsb.pop(XMLConstants.TESTCASE);
			}
		}
		xsb.pop(XMLConstants.TESTSUITE);

		String outputDirectory = outdir + File.separator + "junitreports";
		logger.info(String.format("generated report: %s/%s", outputDirectory, getFileName(testContext.getName())));
		Utils.writeUtf8File(outputDirectory, getFileName(testContext.getName()), xsb.toXML());
	}
	
	private String timeAsGmt() {
	    return Long.toString(LocalDateTime.now().toEpochSecond(OffsetDateTime.now().getOffset()) * 1000);
	  }

	private static Collection<ITestResult> sort(Set<ITestResult> results) {
		List<ITestResult> sortedResults = new ArrayList<>(results);
		Collections.sort(sortedResults, (ITestResult o1, ITestResult o2) ->
				Integer.compare(o1.getMethod().getPriority(), o2.getMethod().getPriority())
		);
		return sortedResults;
	}

	private static int getDisabledTestCount(Collection<ITestNGMethod> methods) {
		int count = 0;
		for (ITestNGMethod method : methods) {
			if (!method.getEnabled()) {
				count = count + 1;
			}
		}
		return count;
	}

	private TestTag createTestTagFor(ITestResult tr, String className) {
		TestTag testTag = new TestTag();

		Properties p2 = new Properties();
		p2.setProperty(XMLConstants.ATTR_CLASSNAME, className);
		p2.setProperty(XMLConstants.ATTR_NAME, getVisualTestName(tr));
		int status = tr.getStatus();
		if (status == ITestResult.SKIP || status == ITestResult.SUCCESS_PERCENTAGE_FAILURE) {
			testTag.childTag = XMLConstants.SKIPPED;
		} else if (status == ITestResult.FAILURE) {
			handleFailure(testTag, tr);
		}
		handleLogs(testTag, tr);
		
		testTag.properties = p2;
		return testTag;
	}
	
	private static void handleLogs(TestTag testTag, ITestResult testResult) {
		String logs = SeleniumRobotLogger.getTestLogs().get(getTestName(testResult));
		try {
			testTag.logs = logs == null ? "Test skipped": StringUtility.encodeString(logs, "xml");
		} catch (CustomSeleniumTestsException e) {
			testTag.logs = logs;
		}
	}

	private static void handleFailure(TestTag testTag, ITestResult testResult) {
		testTag.childTag = testResult.getThrowable() instanceof AssertionError ? XMLConstants.FAILURE : XMLConstants.ERROR;
		if (testResult.getThrowable() != null) {
			

			StringBuilder stackString = new StringBuilder();
			ExceptionUtility.generateTheStackTrace(testResult.getThrowable(), testResult.getThrowable().getMessage(), stackString, "xml");

			testTag.message = testResult.getThrowable().getMessage();
			testTag.type = testResult.getThrowable().getClass().getName();
			testTag.stackTrace = stackString.toString();
		}
	}

	/**
	 * Put a XML start or empty tag to the XMLStringBuffer depending on
	 * hasChildElements parameter
	 */
	private boolean putElement(XMLStringBuffer xsb, String tagName, Properties attributes, boolean hasChildElements) {
		if (hasChildElements) {
			xsb.push(tagName, attributes);
		} else {
			xsb.addEmptyElement(tagName, attributes);
		}
		return hasChildElements;
	}

	/** Set property if value is non-null */
	private void safeSetProperty(Properties p, String key, String value) {
		if (value != null) {
			p.setProperty(key, value);
		}
	}

	protected String getFileName(String name) {
		return "TEST-" + name + ".xml";
	}

	private String formatTime(float time) {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		// JUnitReports wants points here, regardless of the locale
		symbols.setDecimalSeparator('.');
		DecimalFormat format = new DecimalFormat("#.###", symbols);
		format.setMinimumFractionDigits(3);
		return format.format(time / 1000.0f);
	}

	private static class TestTag {
		private Properties properties;
		private String message;
		private String type;
		private String logs = "";
		private String stackTrace;
		String childTag;
		String logTag = XMLConstants.SYSTEM_OUT;
		
	}

}
