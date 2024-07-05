package com.seleniumtests.ut.core;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.it.reporter.ReporterTest;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.*;

import java.io.IOException;
import java.util.*;

/**
 * This class aims at testing execution of tests when data are stored in resources instead of 'data/<app>/' folder
 */
public class TestLaunchNoData {

    @Test(groups={"ut"})
    public void testConfigurationInResources() throws IOException {
        try {
            System.setProperty("applicationName", "core2"); // force usage of data in resources

            executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, XmlSuite.ParallelMode.NONE, new String[]{"testAndSubActions"});
            String logs = ReporterTest.readSeleniumRobotLogFile();
            Assert.assertTrue(logs.contains("Test is OK"));
        } finally {
            System.clearProperty("applicationName");
        }
    }

    @Test(groups = {"ut"})
    public void testCucumberStart2() throws Exception {
        try {
            System.setProperty("applicationName", "core2"); // force usage of features in resources
            executeSubCucumberTests("core_8", 1);

            String mainReportContent = ReporterTest.readSummaryFile();
            Assert.assertTrue(mainReportContent.matches(".*<a href\\='core_8/TestReport\\.html'.*?>core_7</a>.*"));

        } finally {
            System.clearProperty("applicationName");
        }
    }

    @Test(groups={"ut"})
    public void testWithStandardDataProvider() throws Exception {
        try {
            System.setProperty("applicationName", "core2"); // force usage of dataset in resources
            executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, XmlSuite.ParallelMode.METHODS, new String[]{"testStandardDataProvider"});
            String logs = ReporterTest.readSeleniumRobotLogFile();

            // first line / header has not been skipped => 2 tests
            Assert.assertTrue(logs.contains("_r1c1_,_r1c2_"));
            Assert.assertTrue(logs.contains("_r2c1_,_r2c2_"));
        } finally {
            System.clearProperty("applicationName");
        }
    }

    @Test(groups={"ut"})
    public void testWithStandardXlsxDataProvider() throws Exception {
        try {
            System.setProperty("applicationName", "core2"); // force usage of dataset in resources
            executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, XmlSuite.ParallelMode.METHODS, new String[] {"testStandardXlsxDataProvider"});
            String logs = ReporterTest.readSeleniumRobotLogFile();

            // first line / header has not been skipped => 2 tests
            Assert.assertTrue(logs.contains("_r1c1x_,_r1c2x_"));
            Assert.assertTrue(logs.contains("_r2c1x_,_r2c2x_"));
        } finally {
            System.clearProperty("applicationName");
        }
    }

    private static List<String> executeSubTest(int threadCount, String[] testClasses, XmlSuite.ParallelMode parallelMode, String[] methods) throws IOException {

        List<String> testList = new ArrayList<>();

        XmlSuite suite = new XmlSuite();
        suite.setName("TmpSuite");
        suite.setParallel(XmlSuite.ParallelMode.NONE);
        suite.setFileName("home/test/testLoggging.xml");
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
        tng.run();

        return testList;
    }

    /**
     *
     * @param cucumberTests 	cucumber test param as it would be passed in XML file
     * @return
     * @throws IOException
     */
    private static XmlSuite executeSubCucumberTests(String cucumberTests, int threadCount) throws IOException {

        XmlSuite suite = new XmlSuite();
        suite.setName("TmpSuite");
        suite.setFileName("/home/test/seleniumRobot/testng/testLoggging.xml");
        Map<String, String> suiteParameters = new HashMap<>();
        suiteParameters.put("cucumberPackage", "com.seleniumtests");
        suiteParameters.put("softAssertEnabled", "false");
        suite.setParameters(suiteParameters);
        List<XmlSuite> suites = new ArrayList<>();
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
        tng.run();

        return suite;
    }
}
