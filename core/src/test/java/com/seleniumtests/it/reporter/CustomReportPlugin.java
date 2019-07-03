package com.seleniumtests.it.reporter;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.xml.XmlSuite;

public class CustomReportPlugin implements IReporter {

	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		try {
			FileUtils.writeStringToFile(Paths.get(outputDirectory, "customReport.txt").toFile(), "foo");
		} catch (IOException e) {
		}
	}

}
