package com.seleniumtests.reporter.reporters;

import java.io.File;
import java.util.List;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.xml.XmlSuite;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.FileUtility;

/**
 * This reporter controls the execution of all other reporter because TestNG
 * @author s047432
 *
 */
public class ReporterControler implements IReporter {

	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {

		try {
			new SeleniumTestsReporter2().generateReport(xmlSuites, suites, outputDirectory);
		} catch (Exception e) {}
		
		try {
			new CustomReporter().generateReport(xmlSuites, suites, outputDirectory);
		} catch (Exception e) {}
		
		try {
			new SeleniumRobotServerTestRecorder().generateReport(xmlSuites, suites, outputDirectory);
		} catch (Exception e) {}
		
		try {
			new TestManagerReporter().generateReport(xmlSuites, suites, outputDirectory);
		} catch (Exception e) {}
		
		archiveResults();
	}
	
	/**
	 * Archive results if requested
	 */
	private void archiveResults() {
		if (SeleniumTestsContextManager.getGlobalContext().getArchiveToFile() != null) {
			FileUtility.zipFolder(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()), 
								  new File(SeleniumTestsContextManager.getGlobalContext().getArchiveToFile()));
		}
		
	}

}
