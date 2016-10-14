package com.seleniumtests.reporter;

import java.util.List;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.xml.XmlSuite;

public class JMeterReporter implements IReporter {
	
	private List<String> generatedFiles;

	public List<String> getGeneratedFiles() {
		return generatedFiles;
	}

	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		
		for (ISuite suite: suites) {
			for (String suiteString: suite.getResults().keySet()) {
				ISuiteResult suiteResult = suite.getResults().get(suiteString);
				suiteResult.getTestContext().getFailedTests();
				suiteResult.getTestContext().getSkippedTests();
				suiteResult.getTestContext().getPassedTests();
				
				for (ITestResult result: suiteResult.getTestContext().getPassedTests().getAllResults()) {
					List<String> msgs = Reporter.getOutput(result);
				}
			}
		}
	}
}
