package com.seleniumtests.it.util;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.it.reporter.ReporterTest;

public class TestRepeatFilter extends ReporterTest {

	@Test(groups={"it"})
	public void testRepeatFiltered() throws Exception {
		
		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testLogSameInfoMultipleTimes"});
		
		// no bullet as no snapshot comparison is done
		String logs = readSeleniumRobotLogFile();
		Assert.assertEquals(StringUtils.countMatches(logs, "something interesting"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "something else interesting"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "... repeated 20 times until"), 1);
		Assert.assertTrue(logs.matches(".*20 times until \\d+:\\d+:\\d+\\.\\d+ ...*")); // check end time of repeat is present
		Assert.assertEquals(StringUtils.countMatches(logs, "... repeated 2 times until"), logs.contains("seleniumRobotServerActive key not found or set to false") ? 2: 1); 
							// "seleniumRobotServerActive key not found or set to false, or url key seleniumRobotServerUrl has not been set" => not written when executed by maven 
							// AND "something else interesting"
		Assert.assertEquals(StringUtils.countMatches(logs, "... repeated"), logs.contains("seleniumRobotServerActive key not found or set to false") ? 3: 2);
		
	}
	
	/**
	 * Test the case where repeat lasts more that 60 secs, message should be displayed at least once every 60 secs
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testRepeatFilteredLong() throws Exception {
		
		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testLogSameInfoMultipleTimesLong"});
		
		// no bullet as no snapshot comparison is done
		String logs = readSeleniumRobotLogFile();
		Assert.assertEquals(StringUtils.countMatches(logs, "something interesting"), 2); // 1 time for each period of 60 secs, and test lasts 75 secs
		Assert.assertEquals(StringUtils.countMatches(logs, "... repeated 12 times until"), 1); // first 60 secs
		Assert.assertTrue(logs.matches(".*12 times until \\d+:\\d+:\\d+\\.\\d+ ...*")); // check end time of repeat is present
		Assert.assertEquals(StringUtils.countMatches(logs, "... repeated 3 times until"), 1);  // last 15 secs
		
		// check same logs are in the HTML result file
		String htmlLogs = readTestMethodResultFile("testLogSameInfoMultipleTimes");
		Assert.assertEquals(StringUtils.countMatches(htmlLogs, "something interesting"), 2); // 1 time for each period of 60 secs, and test lasts 75 secs
		Assert.assertEquals(StringUtils.countMatches(htmlLogs, "... repeated 12 times until"), 1); // first 60 secs
		Assert.assertTrue(htmlLogs.matches(".*12 times until \\d+:\\d+:\\d+\\.\\d+ ...*")); // check end time of repeat is present
		Assert.assertEquals(StringUtils.countMatches(htmlLogs, "... repeated 3 times until"), 1);  // last 15 secs
	}
	
	/**
	 * Check that with several threads, each repeat filter works independently
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testRepeatFilteredMultithread() throws Exception {
		
		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(2, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testLogSameInfoMultipleTimes", "testLogSameInfoMultipleTimes2"});
		
		// no bullet as no snapshot comparison is done
		String logs = readSeleniumRobotLogFile();
		Assert.assertEquals(StringUtils.countMatches(logs, "something interesting"), 2);
		Assert.assertEquals(StringUtils.countMatches(logs, "something else interesting"), 2);
		Assert.assertEquals(StringUtils.countMatches(logs, "... repeated 20 times"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "... repeated 15 times"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "... repeated 3 times"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "... repeated 2 times"), logs.contains("seleniumRobotServerActive key not found or set to false") ? 3: 1); // "seleniumRobotServerActive key not found or set to false, or url key seleniumRobotServerUrl has not been set" 
																														// AND "something else interesting"
		Assert.assertEquals(StringUtils.countMatches(logs, "... repeated"), logs.contains("seleniumRobotServerActive key not found or set to false") ? 6: 4);
		
		String htmlLogs2 = readTestMethodResultFile("testLogSameInfoMultipleTimes2");
		Assert.assertEquals(StringUtils.countMatches(htmlLogs2, "... repeated 15 times"), 1);
		Assert.assertEquals(StringUtils.countMatches(htmlLogs2, "... repeated 3 times"), 1);
		
		String htmlLogs = readTestMethodResultFile("testLogSameInfoMultipleTimes");
		Assert.assertEquals(StringUtils.countMatches(htmlLogs, "... repeated 20 times"), 1);
		Assert.assertEquals(StringUtils.countMatches(htmlLogs, "... repeated 2 times"), 1);
		
	}
}
