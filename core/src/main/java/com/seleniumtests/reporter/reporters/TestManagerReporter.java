package com.seleniumtests.reporter.reporters;

import java.util.List;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.xml.XmlSuite;

import com.seleniumtests.connectors.tms.TestManager;
import com.seleniumtests.core.SeleniumTestsContextManager;

/**
 * Class for sending test reports to test managers
 * @author behe
 *
 */
public class TestManagerReporter implements IReporter {

	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		
		// issue #81: use global context instead
		TestManager testManager = SeleniumTestsContextManager.getGlobalContext().getTms();

		if (testManager == null) {
			return;
		} else {
			testManager.login();
			testManager.recordResult();
			testManager.recordResultFiles();
			testManager.logout();
		}
	}


}
