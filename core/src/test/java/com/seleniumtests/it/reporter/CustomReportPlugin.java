package com.seleniumtests.it.reporter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import com.seleniumtests.reporter.reporters.CommonReporter;

public class CustomReportPlugin extends CommonReporter {

	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		try {
			FileUtils.writeStringToFile(Paths.get(outputDirectory, "customReport.txt").toFile(), "foo", StandardCharsets.UTF_8);
		} catch (IOException e) {
		}
	}

	@Override
	protected void generateReport(Map<ITestContext, Set<ITestResult>> resultSet, String outdir, boolean optimizeReport,
			boolean finalGeneration) {
		// nothing to do
		
	}

}
