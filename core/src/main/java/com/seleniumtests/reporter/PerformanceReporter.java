package com.seleniumtests.reporter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;

public class PerformanceReporter extends CommonReporter implements IReporter {
	
	private List<String> generatedFiles;


	public List<String> getGeneratedFiles() {
		return generatedFiles;
	}

	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		generatedFiles = new ArrayList<>();
		
		for (ISuite suite: suites) {
			for (String suiteString: suite.getResults().keySet()) {
				ISuiteResult suiteResult = suite.getResults().get(suiteString);
				Set<ITestResult> resultSet = new HashSet<>(); 
				resultSet.addAll(suiteResult.getTestContext().getFailedTests().getAllResults());
				resultSet.addAll(suiteResult.getTestContext().getPassedTests().getAllResults());
				
				VelocityEngine ve;
				try {
					ve = initVelocityEngine();
				} catch (Exception e) {
					throw new ScenarioException("Error generating test results");
				}
				
				for (ITestResult testResult: resultSet) {
					generateTestReport(ve, testResult);
				}
			}
		}
	}
	
	/**
	 * Generates report for a single test
	 * @param ve
	 * @param testResult
	 */
	private void generateTestReport(VelocityEngine ve, ITestResult testResult) {
		try {
			Template t = ve.getTemplate( "reporter/templates/report.perf.vm" );
			VelocityContext context = new VelocityContext();
			
			Long testDuration = 0L;
			Integer errors = 0;
			List<TestStep> testSteps = TestLogging.getTestsSteps().get(testResult);
			if (testSteps != null) {
				for (TestStep step: testSteps) {
					testDuration += step.getDuration();
					if (step.getFailed()) {
						errors++;
					}
				}
			}
	
			context.put("errors", 0);
			context.put("failures", errors);
			context.put("hostname", testResult.getHost() == null ? "": testResult.getHost());
			context.put("suiteName", testResult.getName());
			context.put("className", testResult.getTestClass().getName());
			context.put("tests", testSteps == null ? 0: testSteps.size());
			context.put("duration", testDuration / 1000.0);
			context.put("time", testResult.getStartMillis());	
			context.put("testSteps", testSteps);	
			context.put("logs", 0);	
			
			StringWriter writer = new StringWriter();
			t.merge( context, writer );
			
			String fileName = "PERF-" + testResult.getTestClass().getName() + "." + testResult.getName()
											.replace(" ",  "_")
											.replace("'", "")
											.replace("\"", "")
											.replace("/", "")
											.replace(" ", "_")
											.replace(":", "-")
											.replace("*", ".")
											.replace("?", ".")
											.replace("|", "")
											.replace("<", "-")
											.replace(">", "-")
											.replace("\\", "_")
											+ ".xml";
			PrintWriter fileWriter = createWriter(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), fileName);
			fileWriter.write(writer.toString());
			fileWriter.flush();
			fileWriter.close();
			generatedFiles.add(fileName);
		} catch (Exception e) {
			logger.error(String.format("Error generating test result %s: %s", testResult.getName(), e.getMessage()));
		}
	}
}
