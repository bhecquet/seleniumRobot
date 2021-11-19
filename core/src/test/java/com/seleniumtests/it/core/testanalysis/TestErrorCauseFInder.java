package com.seleniumtests.it.core.testanalysis;

import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.it.reporter.ReporterTest;

public class TestErrorCauseFInder extends GenericTest {
	
	@Test(groups={"it"})
	public void testMultiThreadTests(ITestContext testContext) throws Exception {
		
		try {
			//System.setProperty(SeleniumTestsContext.IMAGE_FIELD_DETECTOR_SERVER_URL, "http://localhost:5000");
			
			ReporterTest.executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.NONE,  new String[] {"testImageDetection"});
			
			
		} finally {
			System.clearProperty(SeleniumTestsContext.IMAGE_FIELD_DETECTOR_SERVER_URL);
		}
		
	}

}
