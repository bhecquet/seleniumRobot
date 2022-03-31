package com.seleniumtests.ut.core.runner;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.it.reporter.ReporterTest;

public class TestSeleniumTestPlan extends ReporterTest {

	
	@Test(groups={"ut"})
	public void testWithStandardDataProviderNoFile() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testStandardDataProviderNoFile"});
		String result = readTestMethodResultFile("testStandardDataProviderNoFile");
		
		// first line / header has been skipped
		Assert.assertTrue(result.matches(".*com\\.seleniumtests\\.customexception\\.ConfigurationException\\: Dataset file.*testStandardDataProviderNoFile.csv or .*testStandardDataProviderNoFile.xlsx does not exist.*"));
	}

	@Test(groups={"ut"})
	public void testWithStandardDataProvider() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testStandardDataProvider"});
		String logs = readSeleniumRobotLogFile();
		
		// first line / header has not been skipped => 2 tests
		Assert.assertTrue(logs.contains("r1c1,r1c2"));
		Assert.assertTrue(logs.contains("r2c1,r2c2"));
	}
	
	@Test(groups={"ut"})
	public void testWithStandardXlsxDataProvider() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testStandardXlsxDataProvider"});
		String logs = readSeleniumRobotLogFile();
		
		// first line / header has not been skipped => 2 tests
		Assert.assertTrue(logs.contains("r1c1x,r1c2x"));
		Assert.assertTrue(logs.contains("r2c1x,r2c2x"));
	}
	
	@Test(groups={"ut"})
	public void testWithStandardDataProviderSemicolon() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testStandardDataProviderSemicolon"});
		String logs = readSeleniumRobotLogFile();
		
		// first line / header has not been skipped => 2 tests
		Assert.assertTrue(logs.contains("r1c1b,r1c2b"));
		Assert.assertTrue(logs.contains("r2c1b,r2c2b"));
		Assert.assertTrue(logs.contains("SeleniumRobotTestListener: Start method testStandardDataProviderSemicolon-1"));
	}
	
	@Test(groups={"ut"})
	public void testWithStandardDataProviderWithHeader() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testStandardDataProviderWithHeader"});
		String logs = readSeleniumRobotLogFile();
		
		// first line / header has not been skipped => 1 test
		Assert.assertFalse(logs.contains("r1c1d,r1c2d")); // first line skipped as considered as header
		Assert.assertTrue(logs.contains("r2c1d,r2c2d"));
	}
	
	@Test(groups={"ut"})
	public void testWithStandardDataProviderSemicolonWithHeader() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testStandardDataProviderSemicolonWithHeader"});
		String logs = readSeleniumRobotLogFile();
		
		// first line / header has not been skipped => 1 test
		Assert.assertFalse(logs.contains("r1c1c,r1c2c")); // first line skipped as considered as header
		Assert.assertTrue(logs.contains("r2c1c,r2c2c"));
		Assert.assertFalse(logs.contains("SeleniumRobotTestListener: Start method testStandardDataProviderSemicolon-1"));
	}
}
