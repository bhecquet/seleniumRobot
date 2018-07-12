package com.seleniumtests.it.driver.screenshots;

import java.util.List;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.it.reporter.ReporterTest;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.reporter.logger.TestStep;

public class TestScreenshotUtil extends ReporterTest {

	/**
	 * check that duration of screenshots is logged into TestStep
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testScreenshotDurationIsLogged(ITestContext testContext) throws Exception {

		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShort"});

		for (ITestResult testResult: TestLogging.getTestsSteps().keySet()) {
			if (testResult.getMethod().isTest()) {
				List<TestStep> steps = TestLogging.getTestsSteps().get(testResult);
				
				for (TestStep step: steps) {
					List<Snapshot> snapshots = step.getSnapshots();
					
					if (!snapshots.isEmpty()) {
						Assert.assertTrue(step.getDurationToExclude() > 0);
						Assert.assertEquals(snapshots.get(0).getScreenshot().getDuration(), step.getDurationToExclude());
					}
				}
			}
		}
	}
}
