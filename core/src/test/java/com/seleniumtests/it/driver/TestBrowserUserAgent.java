package com.seleniumtests.it.driver;

import com.seleniumtests.GenericDriverTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

import java.io.IOException;

import static com.seleniumtests.it.reporter.ReporterTest.executeSubTest;
import static com.seleniumtests.it.reporter.ReporterTest.readTestMethodResultFile;

public class TestBrowserUserAgent extends GenericDriverTest {
	
	@Test(groups = {"it"})
	public void testSetCustomUserAgent() {
		try {
			System.setProperty("userAgent", "SeleniumRobot ${browser} - somePath/someValue/${testName}");
			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, XmlSuite.ParallelMode.METHODS, new String[]{"testUserAgent"});
			String tuaReport = readTestMethodResultFile("testUserAgent-1");
			Assert.assertTrue(tuaReport.contains("User-Agent = SeleniumRobot CHROME - somePath/someValue/testUserAgent"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			System.clearProperty("userAgent");
		}
	}
	
	@Test(groups = {"it"})
	public void testNoCustomUserAgent() {
		try {
			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, XmlSuite.ParallelMode.METHODS, new String[]{"testUserAgent"});
			String tuaReport = readTestMethodResultFile("testUserAgent");
			Assert.assertTrue(tuaReport.matches(".*User-Agent = Mozilla/.*AppleWebKit/.*Chrome/.*Safari/.*"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test(groups = {"it"})
	public void testSetCustomUserAgentWithWrongVariables() {
		try {
			System.setProperty("userAgent", "SeleniumRobot ${bowser} - somePath/someValue/testName");
			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, XmlSuite.ParallelMode.METHODS, new String[]{"testUserAgent"});
			String tuaReport = readTestMethodResultFile("testUserAgent-2");
			Assert.assertTrue(tuaReport.contains("User-Agent = SeleniumRobot ${bowser} - somePath/someValue/testName"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			System.clearProperty("userAgent");
		}
	}
	
}
