/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.it.reporter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.testng.ITestContext;
import org.testng.TestNG;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.StatisticsStorage;
import com.seleniumtests.it.driver.support.server.WebServer;

public class ReporterTest extends ConnectorsTest {
	

	protected String serverUrl;
	protected WebServer server;
	
	protected Map<String, String> getPageMapping() {
		Map<String, String> mapping = new HashMap<>();
		mapping.put("/tu/test.html", "/test.html");
		return mapping;
	}
	
	@AfterClass(groups={"it", "ut"}, alwaysRun=true)
	public void stop() throws Exception {
		if (server != null) {
			logger.info("stopping web server");
			server.stop();
		}
	}

	public void exposeWebServer() throws Exception {
		String localAddress = Inet4Address.getLocalHost().getHostAddress();
		//localAddress = Inet4Address.getByName("localhost").getHostAddress();
        server = new WebServer(localAddress, getPageMapping());
        server.expose();

		serverUrl = String.format("http://%s:%d/test.html", localAddress, server.getServerHost().getPort());
        logger.info(String.format("exposing server on http://%s:%d", localAddress, server.getServerHost().getPort()));
	}
	
	@BeforeMethod(groups={"it"})
	public void setLogs(Method method, ITestContext context) throws IOException {
		GenericTest.resetTestNGREsultAndLogger();
	
		SeleniumTestsContext.resetOutputFolderNames();
		FileUtils.deleteQuietly(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()));
		StatisticsStorage.reset();
	}
	
	protected List<String> executeSubTest(String[] testClasses) throws IOException {
		return executeSubTest(1, testClasses);
	}
	
	/**
	 * Execute stub tests using TestNG runner and make SeleniumTestsReporter a listener so that 
	 * a report is generated
	 * @throws IOException 
	 */
	protected List<String> executeSubTest(int threadCount, String[] testClasses) throws IOException {
		return executeSubTest(threadCount, testClasses, XmlSuite.ParallelMode.METHODS, new String[] {});
	}
	
	/**
	 * Execute SeleniumTestPlan tests
	 * @param threadCount
	 * @param testClasses
	 * @param parallelMode
	 * @param methods			If methods is not empty, then testClasses must contain only one element.
	 * @return
	 * @throws IOException
	 */
	public static List<String> executeSubTest(int threadCount, String[] testClasses, XmlSuite.ParallelMode parallelMode, String[] methods) throws IOException {
//		TestListener testListener = new TestListener();
		
		List<String> testList = new ArrayList<>();
		
		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		suite.setParallel(ParallelMode.NONE);
		suite.setFileName(SeleniumTestsContextManager.getRootPath() + "/data/core/testng/testLoggging.xml");
		Map<String, String> suiteParameters = new HashMap<>();
		suiteParameters.put("softAssertEnabled", "false");
		suite.setParameters(suiteParameters);
//		suite.setConfigFailurePolicy(FailurePolicy.CONTINUE);
		List<XmlSuite> suites = new ArrayList<XmlSuite>();
		suites.add(suite);
		
		
		if (threadCount > 1) {
			suite.setThreadCount(threadCount);
			suite.setParallel(parallelMode);
		}
		
		for (String testClass: testClasses) {
			XmlTest test = new XmlTest(suite);
			test.setName(String.format("%s_%d", testClass.substring(testClass.lastIndexOf(".") + 1), new Random().nextInt()));
			testList.add(test.getName());
			test.addParameter(SeleniumTestsContext.BROWSER, "none");
			List<XmlClass> classes = new ArrayList<XmlClass>();
			XmlClass xmlClass = new XmlClass(testClass);
			if (methods.length > 0) {
				List<XmlInclude> includes = new ArrayList<>();
				for (String method: methods) {
					includes.add(new XmlInclude(method));
				}
				xmlClass.setIncludedMethods(includes);
			}
			classes.add(xmlClass);
			test.setXmlClasses(classes) ;
		}		
		
		TestNG tng = new TestNG(false);
		tng.setXmlSuites(suites);
		tng.setUseDefaultListeners(false);
		tng.setOutputDirectory(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		tng.run(); 
		
		return testList;
	}
	
	public static TestNG executeMultiSuites(String[] testClasses, String[] methods) throws IOException {

		List<XmlSuite> suites = new ArrayList<XmlSuite>();
		
		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		suite.setParallel(ParallelMode.NONE);
		suite.setFileName("/home/test/seleniumRobot/data/core/testng/testLoggging.xml");
		Map<String, String> suiteParameters = new HashMap<>();
		suiteParameters.put("softAssertEnabled", "false");
		suite.setParameters(suiteParameters);
		suites.add(suite);
		
		XmlSuite suite2 = new XmlSuite();
		suite2.setName("TmpSuite2");
		suite2.setParallel(ParallelMode.NONE);
		suite2.setFileName("/home/test/seleniumRobot/data/core/testng/testLoggging2.xml");
		Map<String, String> suiteParameters2 = new HashMap<>();
		suiteParameters2.put("softAssertEnabled", "false");
		suite2.setParameters(suiteParameters2);
		suites.add(suite2);
		
		for (String testClass: testClasses) {
			XmlTest test = new XmlTest(suite);
			test.setName(String.format("%s_%d", testClass.substring(testClass.lastIndexOf(".") + 1), new Random().nextInt()));
			test.addParameter(SeleniumTestsContext.BROWSER, "none");
			List<XmlClass> classes = new ArrayList<XmlClass>();
			XmlClass xmlClass = new XmlClass(testClass);
			if (methods.length > 0) {
				List<XmlInclude> includes = new ArrayList<>();
				for (String method: methods) {
					includes.add(new XmlInclude(method));
				}
				xmlClass.setIncludedMethods(includes);
			}
			classes.add(xmlClass);
			test.setXmlClasses(classes) ;
			
			XmlTest test2 = new XmlTest(suite2);
			test2.setName(String.format("%s_%d", testClass.substring(testClass.lastIndexOf(".") + 1), new Random().nextInt()));
			test2.addParameter(SeleniumTestsContext.BROWSER, "none");
			List<XmlClass> classes2 = new ArrayList<XmlClass>();
			XmlClass xmlClass2 = new XmlClass(testClass);
			if (methods.length > 0) {
				List<XmlInclude> includes = new ArrayList<>();
				for (String method: methods) {
					includes.add(new XmlInclude(method));
				}
				xmlClass2.setIncludedMethods(includes);
			}
			classes2.add(xmlClass2);
			test2.setXmlClasses(classes2) ;
		}		
		
		TestNG tng = new TestNG(false);
		tng.setXmlSuites(suites);
		tng.setUseDefaultListeners(false);
		tng.setOutputDirectory(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		tng.run(); 
		
		return tng;
	}
	
	/**
	 * Execute SeleniumTestPlan and cucumber tests
	 * Each test method is put in its own TestNG test
	 * @param threadCount
	 * @param testMethods
	 * @param cucumberTests
	 * @return
	 * @throws IOException
	 */
	public TestNG executeSubTest(int threadCount, String[] testMethods, String cucumberTests, String group) throws IOException {

		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		suite.setParallel(ParallelMode.NONE);
		suite.setFileName("/home/test/seleniumRobot/testng/testLoggging.xml");
		Map<String, String> suiteParameters = new HashMap<>();
		suiteParameters.put("softAssertEnabled", "false");
		suiteParameters.put("cucumberPackage", "com.seleniumtests");
		suite.setParameters(suiteParameters);
		List<XmlSuite> suites = new ArrayList<XmlSuite>();
		suites.add(suite);
		
		if (threadCount > 1) {
			suite.setThreadCount(threadCount);
			suite.setParallel(XmlSuite.ParallelMode.TESTS);
		}
		
		// TestNG tests
		for (String testMethod: testMethods) {
			String className = testMethod.substring(0, testMethod.lastIndexOf("."));
			String methodName = testMethod.substring(testMethod.lastIndexOf(".") + 1);

			XmlTest test = new XmlTest(suite);
			test.setName(String.format("%s_%d", methodName, new Random().nextInt()));
			
			if (group != null) {
				test.addIncludedGroup(group);
			}
			
			test.addParameter(SeleniumTestsContext.BROWSER, "none");
			List<XmlClass> classes = new ArrayList<XmlClass>();
			XmlClass xmlClass = new XmlClass(className);
			
			List<XmlInclude> include = new ArrayList<>();
			include.add(new XmlInclude(methodName));
			xmlClass.setIncludedMethods(include);
			classes.add(xmlClass);
			test.setXmlClasses(classes) ;
		}	
		
		// cucumber tests
		if (!cucumberTests.isEmpty()) {
			XmlTest test = new XmlTest(suite);
			test.setName(String.format("cucumberTest_%d", new Random().nextInt()));
			XmlPackage xmlPackage = new XmlPackage("com.seleniumtests.core.runner.*");
			test.setXmlPackages(Arrays.asList(xmlPackage));
			Map<String, String> parameters = new HashMap<>();
			parameters.put("cucumberTests", cucumberTests);
			parameters.put("cucumberTags", "");
			test.setParameters(parameters);
		}
		
		TestNG tng = new TestNG(false);
		tng.setXmlSuites(suites);
		tng.setUseDefaultListeners(false);
		tng.setOutputDirectory(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		tng.run(); 
		
		return tng;
	}
	
	/**
	 * 
	 * @param cucumberTests 	cucumber test param as it would be passed in XML file
	 * @return
	 * @throws IOException 
	 */
	public static XmlSuite executeSubCucumberTests(String cucumberTests, int threadCount) throws IOException {

		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		suite.setFileName("/home/test/seleniumRobot/testng/testLoggging.xml");
		Map<String, String> suiteParameters = new HashMap<>();
		suiteParameters.put("cucumberPackage", "com.seleniumtests");
		suiteParameters.put("softAssertEnabled", "false");
		suite.setParameters(suiteParameters);
		List<XmlSuite> suites = new ArrayList<XmlSuite>();
		suites.add(suite);
		

		if (threadCount > 1) {
			suite.setThreadCount(threadCount);
			suite.setParallel(XmlSuite.ParallelMode.METHODS);
		}
		
		XmlTest test = new XmlTest(suite);
		test.setName(String.format("cucumberTest_%d", new Random().nextInt()));
		XmlPackage xmlPackage = new XmlPackage("com.seleniumtests.core.runner.*");
		test.setXmlPackages(Arrays.asList(xmlPackage));
		Map<String, String> parameters = new HashMap<>();
		parameters.put("cucumberTests", cucumberTests);
		parameters.put("cucumberTags", "");
		test.setParameters(parameters);
		
		TestNG tng = new TestNG(false);
		tng.setXmlSuites(suites);
		tng.setUseDefaultListeners(false);
		tng.setOutputDirectory(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		tng.run(); 
		
		return suite;
	}
	
	/**
	 * Reads the TestReport.html file for the given test name
	 * line breaks are removed
	 * @param object
	 * @return
	 * @throws IOException 
	 */
	public static String readTestMethodResultFile(String testName) throws IOException {
		String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), testName, "TestReport.html").toFile(), StandardCharsets.UTF_8);
		return detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
	}
	
	public static String readTestMethodPerfFile(String testName) throws IOException {
		String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), testName, "PERF-result.xml").toFile(), StandardCharsets.UTF_8);
		return detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
	}
	
	public static String readJUnitFile(String suiteName) throws IOException {
		String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "junitreports", String.format("TEST-%s.xml", suiteName)).toFile(), StandardCharsets.UTF_8);
		return detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
	}
	
	public static String readSummaryFile() throws IOException {
		String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "SeleniumTestReport.html").toFile(), StandardCharsets.UTF_8);
		return detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
	}
	
	public static String readSeleniumRobotLogFile() throws IOException {
		String logContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "seleniumRobot.log").toFile(), StandardCharsets.UTF_8);
		return logContent.replace("\n", "").replace("\r",  "");
	}
}
