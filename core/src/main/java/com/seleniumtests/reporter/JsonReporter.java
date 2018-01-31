package com.seleniumtests.reporter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.xml.XmlSuite;

import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;

public class JsonReporter extends CommonReporter implements IReporter {

	/**
	 * Generate all test reports
	 */
	@Override
	public void generateReport(final List<XmlSuite> xml, final List<ISuite> suites, final String outdir) {
		ITestContext testCtx = SeleniumTestsContextManager.getGlobalContext().getTestNGContext();
		
		if (testCtx == null) {
			logger.error("Looks like your class does not extend from SeleniumTestPlan!");
			return;
		}
		
		Map<String, Integer> results = new HashMap<>();
		results.put("pass", 0);
		results.put("fail", 0);
		results.put("skip", 0);
		results.put("total", 0);
		
		for (ISuite suite : suites) {
			Map<String, ISuiteResult> tests = suite.getResults();
			
			for (ISuiteResult r : tests.values()) {
				ITestContext context = r.getTestContext();
				
				results.put("fail", results.get("fail") + context.getFailedTests().getAllResults().size());
				results.put("pass", results.get("pass") + context.getPassedTests().getAllResults().size());
				results.put("skip", results.get("skip") + context.getSkippedTests().getAllResults().size());
				
				Integer total = results.get("pass") + results.get("fail") + results.get("skip");
				results.put("total", total);

			}
		}

		PrintWriter fileWriter;
		try {
			fileWriter = createWriter(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "results.json");
			fileWriter.write(new JSONObject(results).toString());
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			logger.error(String.format("Error generating JSON result: %s", e.getMessage()));
		}
		
	
	}
	


}
