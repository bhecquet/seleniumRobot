package com.seleniumtests.ut.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector.SnapshotComparisonResult;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.core.runner.CucumberScenarioWrapper;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.info.StringInfo;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestStep;

public class TestTestNGResultUtil extends MockitoTest {

	@Mock
	ITestResult testResult;
	
	@Mock
	ITestNGMethod testNGMethod;
	
	@Mock
	ITestContext testContext;
	
	@Mock
	SeleniumRobotSnapshotServerConnector snapshotServerConnector;
	

	@Mock
	private IResultMap failedTests; 
	@Mock
	private IResultMap skippedTests; 
	@Mock
	private IResultMap passedTests; 
	
	
	@Mock
	ISuite suite;
	
	@Mock
	CucumberScenarioWrapper cucumberScenarioWrapper;
	
	@BeforeMethod(alwaysRun = true)
	private void init() {
		
		when(testContext.getName()).thenReturn("a test");
		when(testContext.getFailedTests()).thenReturn(failedTests);
		when(testContext.getSkippedTests()).thenReturn(skippedTests);
		when(testContext.getPassedTests()).thenReturn(passedTests);
		
		when(testResult.getTestContext()).thenReturn(testContext);
		when(testResult.getMethod()).thenReturn(testNGMethod);
		when(testNGMethod.clone()).thenReturn(testNGMethod);

		when(testContext.getSuite()).thenReturn(suite);
		when(suite.getName()).thenReturn("mySuite");
		when(testContext.getName()).thenReturn("myTest");

		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerUrl(SERVER_URL);
	}
	
	@Test(groups={"ut"})
	public void testCopy() throws NoSuchFieldException, IllegalAccessException {

		ITestResult tr = Reporter.getCurrentTestResult(); 
		TestNGResultUtils.setSeleniumRobotTestContext(tr, SeleniumTestsContextManager.getThreadContext());
		TestNGResultUtils.setUniqueTestName(tr, "UniqueName");
		TestNGResultUtils.setRetry(tr, 2);
		TestNGResultUtils.setHtmlReportCreated(tr, true);
		TestNGResultUtils.setCustomReportCreated(tr, true);
		TestNGResultUtils.setSnapshotComparisonResult(tr, ITestResult.STARTED);
		
		ITestResult newTestResult = TestNGResultUtils.copy(Reporter.getCurrentTestResult(), "newName", "newDescription");
		
		Assert.assertEquals(newTestResult.getEndMillis(), tr.getEndMillis());
		Assert.assertEquals(newTestResult.getMethod().getDescription(), "newDescription");
		
		Assert.assertEquals(TestNGResultUtils.getRetry(newTestResult), (Integer)2); // check parameters are copied
		Assert.assertEquals(TestNGResultUtils.getUniqueTestName(newTestResult), "newName"); 
		
		//  check report flags are reset
		Assert.assertFalse(TestNGResultUtils.isHtmlReportCreated(newTestResult));
		Assert.assertFalse(TestNGResultUtils.isCustomReportCreated(newTestResult));
		Assert.assertEquals((Integer)TestNGResultUtils.getSnapshotComparisonResult(newTestResult), (Integer)ITestResult.SKIP);
		
		Assert.assertNotEquals(TestNGResultUtils.getSeleniumRobotTestContext(newTestResult), TestNGResultUtils.getSeleniumRobotTestContext(tr));
	}
	

	@Test(groups={"ut"})
	public void testTestNameWithoutCucumber() {
		when(testResult.getParameters()).thenReturn(new Object[] {});
		when(testResult.getMethod()).thenReturn(testNGMethod);
		when(testNGMethod.isBeforeMethodConfiguration()).thenReturn(false);
		when(testNGMethod.getMethodName()).thenReturn("testMethod");
		
		Assert.assertEquals(TestNGResultUtils.getTestName(testResult), "testMethod");
	}
	
	@Test(groups={"ut"})
	public void testBeforeTestNameWithoutCucumber() throws NoSuchMethodException, SecurityException {
		when(testResult.getParameters()).thenReturn(new Object[] {TestTestNGResultUtil.class.getDeclaredMethod("testBeforeTestNameWithoutCucumber")});
		when(testResult.getMethod()).thenReturn(testNGMethod);
		when(testNGMethod.isBeforeMethodConfiguration()).thenReturn(true);
		when(testNGMethod.getMethodName()).thenReturn("testMethod");
		
		Assert.assertEquals(TestNGResultUtils.getTestName(testResult), "before-testBeforeTestNameWithoutCucumber");
	}
	
	@Test(groups={"ut"})
	public void testTestNameWithCucumber() {
		when(testResult.getParameters()).thenReturn(new Object[] {cucumberScenarioWrapper});
		when(cucumberScenarioWrapper.toString()).thenReturn("some test");
		
		Assert.assertEquals(TestNGResultUtils.getTestName(testResult), "some test");
	}
	
	@Test(groups={"ut"})
	public void testTestNameWithNull() {
		when(testResult.getParameters()).thenReturn(new Object[] {cucumberScenarioWrapper});
		when(cucumberScenarioWrapper.toString()).thenReturn("some test");
		
		Assert.assertNull(TestNGResultUtils.getTestName(null));
	}
	

	@Test(groups={"ut"})
	public void testHashWithTestMethod() {
		when(testNGMethod.getRealClass()).thenReturn(TestTestNGResultUtil.class);
		when(testResult.getParameters()).thenReturn(new Object[] {});
		when(testNGMethod.isBeforeMethodConfiguration()).thenReturn(false);
		when(testNGMethod.getMethodName()).thenReturn("testMethod");
		
		Assert.assertTrue(TestNGResultUtils.getHashForTest(testResult).startsWith("mySuite-myTest-com.seleniumtests.ut.util.TestTestNGResultUtil-testMethod-1-org.mockito.codegen."));
		
	}

	
	@Test(groups={"ut"})
	public void testHashWithBeforeTestMethod() throws NoSuchMethodException, SecurityException {
		when(testNGMethod.getRealClass()).thenReturn(TestTestNGResultUtil.class);
		when(testResult.getParameters()).thenReturn(new Object[] {TestTestNGResultUtil.class.getDeclaredMethod("testHashWithBeforeTestMethod")});
		when(testNGMethod.isBeforeMethodConfiguration()).thenReturn(true);
		when(testNGMethod.getMethodName()).thenReturn("testHashWithBeforeTestMethod");
		
		Assert.assertTrue(TestNGResultUtils.getHashForTest(testResult).startsWith("mySuite-myTest-com.seleniumtests.ut.util.TestTestNGResultUtil-before-testHashWithBeforeTestMethod-1-org.mockito.codegen."));
		
	}
	
	@Test(groups={"ut"})
	public void testHashWithNullResult() throws NoSuchMethodException, SecurityException {
		Assert.assertEquals(TestNGResultUtils.getHashForTest(null), "null-null-null-null-0");
		
	}
	
	@Test(groups={"ut"})
	public void testUniqueName() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		TestNGResultUtils.setUniqueTestName(tr, "foo");
		Assert.assertEquals(TestNGResultUtils.getUniqueTestName(tr), "foo");
	}
	@Test(groups={"ut"})
	public void testUniqueNameNull() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		Assert.assertNull(TestNGResultUtils.getTestMethodName(tr));
	}
	
	@Test(groups={"ut"})
	public void testMethodName() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		TestNGResultUtils.setTestMethodName(tr, "foo");
		Assert.assertEquals(TestNGResultUtils.getTestMethodName(tr), "foo");
	}
	@Test(groups={"ut"})
	public void testMethodNameNull() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		Assert.assertNull(TestNGResultUtils.getUniqueTestName(tr));
	}
	
	@Test(groups={"ut"})
	public void testLinkedTestMethod() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		TestNGResultUtils.setLinkedTestMethod(tr, tr.getMethod());
		Assert.assertEquals(TestNGResultUtils.getLinkedTestMethod(tr), tr.getMethod());
	}
	@Test(groups={"ut"})
	public void testLinkedTestMethodNull() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		Assert.assertNull(TestNGResultUtils.getLinkedTestMethod(tr));
	}
	
	@Test(groups={"ut"})
	public void testSeleniumTestContext() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		TestNGResultUtils.setSeleniumRobotTestContext(tr, SeleniumTestsContextManager.getThreadContext());
		Assert.assertEquals(TestNGResultUtils.getSeleniumRobotTestContext(tr), SeleniumTestsContextManager.getThreadContext());
	}
	@Test(groups={"ut"})
	public void testSeleniumTestContextNull() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		Assert.assertNull(TestNGResultUtils.getSeleniumRobotTestContext(tr));
	}

	@Test(groups={"ut"})
	public void testRetry() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		TestNGResultUtils.setRetry(tr, 2);
		Assert.assertEquals(TestNGResultUtils.getRetry(tr), (Integer)2);
	}
	@Test(groups={"ut"})
	public void testRetryNull() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		Assert.assertNull(TestNGResultUtils.getRetry(tr));
	}
	
	@Test(groups={"ut"})
	public void testSnapshotTestCaseInSessionId() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		TestNGResultUtils.setSnapshotTestCaseInSessionId(tr, 2);
		Assert.assertEquals(TestNGResultUtils.getSnapshotTestCaseInSessionId(tr), (Integer)2);
	}
	@Test(groups={"ut"})
	public void testSnapshotTestCaseInSessionIdNull() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		Assert.assertNull(TestNGResultUtils.getSnapshotTestCaseInSessionId(tr));
	}
	
	@Test(groups={"ut"})
	public void testSnapshotComparisonResult() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		TestNGResultUtils.setSnapshotComparisonResult(tr, ITestResult.STARTED);
		Assert.assertEquals(TestNGResultUtils.getSnapshotComparisonResult(tr), (Integer)ITestResult.STARTED);
	}
	@Test(groups={"ut"})
	public void testSnapshotComparisonResultNull() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		Assert.assertNull(TestNGResultUtils.getSnapshotComparisonResult(tr));
	}
	
	@Test(groups={"ut"})
	public void testSeleniumServerReportCreated() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		TestNGResultUtils.setSeleniumServerReportCreated(tr, true);
		Assert.assertTrue(TestNGResultUtils.isSeleniumServerReportCreated(tr));
	}
	@Test(groups={"ut"})
	public void testSeleniumServerReportCreatedNull() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		Assert.assertFalse(TestNGResultUtils.isSeleniumServerReportCreated(tr));
	}
	
	@Test(groups={"ut"})
	public void testManagerReportCreated() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		TestNGResultUtils.setTestManagereportCreated(tr, true);
		Assert.assertTrue(TestNGResultUtils.isTestManagerReportCreated(tr));
	}
	@Test(groups={"ut"})
	public void testManagerReportCreatedNull() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		Assert.assertFalse(TestNGResultUtils.isTestManagerReportCreated(tr));
	}
	
	@Test(groups={"ut"})
	public void testBugtrackerReportCreated() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		TestNGResultUtils.setBugtrackerReportCreated(tr, true);
		Assert.assertTrue(TestNGResultUtils.isBugtrackerReportCreated(tr));
	}
	@Test(groups={"ut"})
	public void testBugtrackerReportCreatedNull() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		Assert.assertFalse(TestNGResultUtils.isBugtrackerReportCreated(tr));
	}
	
	@Test(groups={"ut"})
	public void testHtmlReportCreated() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		TestNGResultUtils.setHtmlReportCreated(tr, true);
		Assert.assertTrue(TestNGResultUtils.isHtmlReportCreated(tr));
	}
	@Test(groups={"ut"})
	public void testHtmlReportCreatedNull() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		Assert.assertFalse(TestNGResultUtils.isHtmlReportCreated(tr));
	}
	
	@Test(groups={"ut"})
	public void testCustomReportCreated() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		TestNGResultUtils.setCustomReportCreated(tr, true);
		Assert.assertTrue(TestNGResultUtils.isCustomReportCreated(tr));
	}
	@Test(groups={"ut"})
	public void testCustomReportCreatedNull() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		Assert.assertFalse(TestNGResultUtils.isCustomReportCreated(tr));
	}
	
	@Test(groups={"ut"})
	public void testErrorCauseSearchedInLastStep() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		TestNGResultUtils.setErrorCauseSearchedInLastStep(tr, true);
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInLastStep(tr));
	}
	@Test(groups={"ut"})
	public void testErrorCauseSearchedInLastStepNull() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		Assert.assertFalse(TestNGResultUtils.isErrorCauseSearchedInLastStep(tr));
	}
	
	@Test(groups={"ut"})
	public void testErrorCauseSearchedInReferencePicture() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		TestNGResultUtils.setErrorCauseSearchedInReferencePicture(tr, true);
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(tr));
	}
	@Test(groups={"ut"})
	public void testErrorCauseSearchedInReferencePictureNull() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		Assert.assertFalse(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(tr));
	}
	
	@Test(groups={"ut"})
	public void testNoMoreRetry() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		TestNGResultUtils.setNoMoreRetry(tr, true);
		Assert.assertTrue(TestNGResultUtils.getNoMoreRetry(tr));
	}
	@Test(groups={"ut"})
	public void testNoMoreRetryNull() {
		ITestResult tr = Reporter.getCurrentTestResult(); 
		Assert.assertNull(TestNGResultUtils.getNoMoreRetry(tr));
	}
	
	@Test(groups={"ut"})
	public void testTestInfo() {
		ITestResult tr = Reporter.getCurrentTestResult();
		TestNGResultUtils.setTestInfo(tr, "key", new StringInfo("value"));
		Assert.assertEquals(TestNGResultUtils.getTestInfo(tr).size(), 1);
		Assert.assertEquals(TestNGResultUtils.getTestInfo(tr).get("key").getInfo(), "value");
	}
	
	@Test(groups={"ut"})
	public void testTestInfoEncoded() {
		ITestResult tr = Reporter.getCurrentTestResult();
		TestNGResultUtils.setTestInfo(tr, "key", new StringInfo("value<"));
		Assert.assertEquals(TestNGResultUtils.getTestInfoEncoded(tr, "html").size(), 1);
		Assert.assertEquals(TestNGResultUtils.getTestInfoEncoded(tr, "html").get("key"), "value&lt;");
	}
	
	@Test(groups={"ut"})
	public void testTestInfoEmpty() {
		ITestResult tr = Reporter.getCurrentTestResult();
		Assert.assertEquals(TestNGResultUtils.getTestInfo(tr).size(), 0);
	}
	
	/**
	 * Test setting description with a method parameter
	 * @param db
	 */
	@Parameters("db")
	@Test(groups={"ut"}, description = "my DB ${arg0}")
	public void testDescription(@Optional("mysql")String db) {
		ITestResult tr = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(tr, SeleniumTestsContextManager.getThreadContext());
		Assert.assertEquals(TestNGResultUtils.getTestDescription(tr), "my DB mysql");
	}
	
	/**
	 * Test setting description with a "null" method parameter
	 * @param db
	 */
	@Parameters("db")
	@Test(groups={"ut"}, description = "my DB ${arg0}")
	public void testDescriptionNullParameter(@Optional()String db) {
		ITestResult tr = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(tr, SeleniumTestsContextManager.getThreadContext());
		Assert.assertEquals(TestNGResultUtils.getTestDescription(tr), "my DB null");
	}
	
	/**
	 * Only test method name is available => we take unique test name
	 */
	@Test(groups={"ut"})
	public void testVisualTestName() {
		
		ITestResult tr = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(tr, SeleniumTestsContextManager.getThreadContext());
		TestNGResultUtils.setUniqueTestName(tr, "testVisualTestName-1");
		Assert.assertEquals(TestNGResultUtils.getVisualTestName(tr), "testVisualTestName-1");
	}
	@Test(groups={"ut"})
	public void testVisualTestNameNull() {
		
		ITestResult tr = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(tr, SeleniumTestsContextManager.getThreadContext());
		TestNGResultUtils.setUniqueTestName(tr, "testVisualTestName-1");
		Assert.assertNull(TestNGResultUtils.getVisualTestName(null));
	}
	
	/**
	 * Test name is defined in annotation, return it
	 */
	@Test(groups={"ut"}, testName = "My Test")
	public void testVisualTestNameWithTestName() {
		
		ITestResult tr = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(tr, SeleniumTestsContextManager.getThreadContext());
		TestNGResultUtils.setUniqueTestName(tr, "testVisualTestName-1");
		Assert.assertEquals(TestNGResultUtils.getVisualTestName(tr), "My Test");
	}
	
	/**
	 * Allow placeholder in test name, referring to variable
	 */
	@Test(groups={"ut"}, testName = "My Test ${key}")
	public void testVisualTestNameWithTestNameAndParameters() {
		
		ITestResult tr = Reporter.getCurrentTestResult();
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("key", new TestVariable("key", "value"));
		TestNGResultUtils.setSeleniumRobotTestContext(tr, SeleniumTestsContextManager.getThreadContext());
		TestNGResultUtils.setUniqueTestName(tr, "testVisualTestName-1");
		Assert.assertEquals(TestNGResultUtils.getVisualTestName(tr), "My Test value");
	}
	
	/**
	 * Allow placeholder in test name, referring to method parameter
	 * @param db
	 */
	@Parameters("db")
	@Test(groups={"ut"}, testName = "My Test ${arg0}")
	public void testVisualTestNameWithTestNameAndParameters2(@Optional("mysql")String db) {
		
		ITestResult tr = Reporter.getCurrentTestResult();
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("key", new TestVariable("key", "value"));
		TestNGResultUtils.setSeleniumRobotTestContext(tr, SeleniumTestsContextManager.getThreadContext());
		TestNGResultUtils.setUniqueTestName(tr, "testVisualTestName-1");
		Assert.assertEquals(TestNGResultUtils.getVisualTestName(tr), "My Test mysql");
	}
	
	/**
	 * Check that test result is set to KO if comparison fails
	 * Comparison behaviour is set to "changeTestResult"
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testChangeTestResultWithSnapshotComparison() throws IOException {
		testChangeTestResultWithSnapshot("main", SnapshotCheckType.FULL, SnapshotComparisonResult.KO);
		

		// check that test result has been changed
		verify(testResult).setStatus(ITestResult.FAILURE);
		verify(passedTests).removeResult(testResult);
		verify(failedTests).addResult(testResult);
	}
	
	/**
	 * When snapshot has no name, comparison should not be done
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testChangeTestResultWithSnapshotComparisonNoName() throws IOException {
		testChangeTestResultWithSnapshot("", SnapshotCheckType.FULL, SnapshotComparisonResult.KO);
		

		// check that test result has been changed
		verify(testResult, never()).setStatus(ITestResult.FAILURE);
		verify(passedTests, never()).removeResult(testResult);
		verify(failedTests, never()).addResult(testResult);
		verify(snapshotServerConnector, never()).checkSnapshotHasNoDifferences(any(Snapshot.class), anyString(), anyString());
	}
	@Test(groups={"ut"})
	public void testChangeTestResultWithSnapshotComparisonNullName() throws IOException {
		testChangeTestResultWithSnapshot(null, SnapshotCheckType.FULL, SnapshotComparisonResult.KO);
		
		
		// check that test result has been changed
		verify(testResult, never()).setStatus(ITestResult.FAILURE);
		verify(passedTests, never()).removeResult(testResult);
		verify(failedTests, never()).addResult(testResult);
		verify(snapshotServerConnector, never()).checkSnapshotHasNoDifferences(any(Snapshot.class), anyString(), anyString());
	}
	
	/**
	 * When snapshot is not intended to be compared, do not compare
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testChangeTestResultWithSnapshotComparisonNoComparison() throws IOException {
		testChangeTestResultWithSnapshot("main", SnapshotCheckType.FALSE, SnapshotComparisonResult.KO);
		
		
		// check that test result has been changed
		verify(testResult, never()).setStatus(ITestResult.FAILURE);
		verify(passedTests, never()).removeResult(testResult);
		verify(failedTests, never()).addResult(testResult);
		verify(snapshotServerConnector, never()).checkSnapshotHasNoDifferences(any(Snapshot.class), anyString(), anyString());
	}
	
	private void testChangeTestResultWithSnapshot(String snapshotName, SnapshotCheckType snapshotCheckType, SnapshotComparisonResult comparisonResult) throws IOException {
		// create a step with snapshot that should be compared
		TestStep step1 = new TestStep("step 1", Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		File tmpImg = File.createTempFile("img", "_with_very_very_very_long_name_to_be_shortened.png");
		File tmpHtml = File.createTempFile("html", "_with_very_very_very_long_name_to_be_shortened.html");
		
		ScreenShot screenshot = new ScreenShot(tmpImg, tmpHtml);
		step1.addSnapshot(new Snapshot(screenshot, snapshotName, snapshotCheckType), 1, null);
		

		SeleniumTestsContext context = SeleniumTestsContextManager.getThreadContext();
		TestStepManager.logTestStep(step1);
		
		when(testResult.getStatus()).thenReturn(ITestResult.SUCCESS);
		when(testResult.getAttribute("testContext")).thenReturn(context);

		// make this test successful, it will be changed to failed
		List<ITestNGMethod> methods = new ArrayList<>();
		methods.add(testNGMethod);
		when(passedTests.getAllMethods()).thenReturn(methods);
		
		// be sure we will do comparison
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerCompareSnapshotBehaviour("changeTestResult");
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerActive(true);
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerCompareSnapshot(true);
		

		try (MockedStatic mockedSnapshotServer = mockStatic(SeleniumRobotSnapshotServerConnector.class)) {
			mockedSnapshotServer.when(() -> SeleniumRobotSnapshotServerConnector.getInstance()).thenReturn(snapshotServerConnector);
			when(snapshotServerConnector.checkSnapshotHasNoDifferences(any(Snapshot.class), anyString(), anyString())).thenReturn(comparisonResult);

			TestNGResultUtils.changeTestResultWithSnapshotComparison(testResult);
		}
	}
	
	@Test(groups={"ut"})
	public void testChangeTestResultWithSnapshotComparisonAlreadyFailed() {
		when(testResult.getStatus()).thenReturn(ITestResult.FAILURE);
		
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerCompareSnapshotBehaviour("changeTestResult");
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerActive(true);
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerCompareSnapshot(true);

		try (MockedStatic mockedSnapshotServer = mockStatic(SeleniumRobotSnapshotServerConnector.class)) {
			mockedSnapshotServer.when(() -> SeleniumRobotSnapshotServerConnector.getInstance()).thenReturn(snapshotServerConnector);

			TestNGResultUtils.changeTestResultWithSnapshotComparison(testResult);
			verify(snapshotServerConnector, never()).checkSnapshotHasNoDifferences(any(Snapshot.class), anyString(), anyString());
		}
	}
	
	@Test(groups={"ut"})
	public void testChangeTestResultWithSnapshotComparisonNoComparisonRequested() {
		when(testResult.getStatus()).thenReturn(ITestResult.SUCCESS);
		
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerCompareSnapshotBehaviour("changeTestResult");
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerActive(true);
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerCompareSnapshot(false);
		

		try (MockedStatic mockedSnapshotServer = mockStatic(SeleniumRobotSnapshotServerConnector.class)) {
			mockedSnapshotServer.when(() -> SeleniumRobotSnapshotServerConnector.getInstance()).thenReturn(snapshotServerConnector);
			TestNGResultUtils.changeTestResultWithSnapshotComparison(testResult);
			verify(snapshotServerConnector, never()).checkSnapshotHasNoDifferences(any(Snapshot.class), anyString(), anyString());
		}
	}
	
	/**
	 * Test result not changed if server is inactive
	 */
	@Test(groups={"ut"})
	public void testChangeTestResultWithSnapshotComparisonServerInactive() {
		when(testResult.getStatus()).thenReturn(ITestResult.SUCCESS);
		
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerCompareSnapshotBehaviour("changeTestResult");
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerActive(false);
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerCompareSnapshot(true);
		

		try (MockedStatic mockedSnapshotServer = mockStatic(SeleniumRobotSnapshotServerConnector.class)) {
			mockedSnapshotServer.when(() -> SeleniumRobotSnapshotServerConnector.getInstance()).thenReturn(snapshotServerConnector);
			TestNGResultUtils.changeTestResultWithSnapshotComparison(testResult);
			verify(snapshotServerConnector, never()).checkSnapshotHasNoDifferences(any(Snapshot.class), anyString(), anyString());
		}
	}
	
	/**
	 * Test result not changed when behaviour is "addTestResult"
	 */
	@Test(groups={"ut"})
	public void testChangeTestResultWithSnapshotComparisonBehaviourAddResult() {
		when(testResult.getStatus()).thenReturn(ITestResult.SUCCESS);
		
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerCompareSnapshotBehaviour("addTestResult");
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerActive(true);
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerCompareSnapshot(true);

		try (MockedStatic mockedSnapshotServer = mockStatic(SeleniumRobotSnapshotServerConnector.class)) {
			mockedSnapshotServer.when(() -> SeleniumRobotSnapshotServerConnector.getInstance()).thenReturn(snapshotServerConnector);
			TestNGResultUtils.changeTestResultWithSnapshotComparison(testResult);
			verify(snapshotServerConnector, never()).checkSnapshotHasNoDifferences(any(Snapshot.class), anyString(), anyString());
		}
	}

	/**
	 * Test result not changed when behaviour is "displayOnly"
	 */
	@Test(groups={"ut"})
	public void testChangeTestResultWithSnapshotComparisonBehaviourDisplayOnly() {
		when(testResult.getStatus()).thenReturn(ITestResult.SUCCESS);
		
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerCompareSnapshotBehaviour("displayOnly");
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerActive(true);
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerCompareSnapshot(true);

		try (MockedStatic mockedSnapshotServer = mockStatic(SeleniumRobotSnapshotServerConnector.class)) {
			mockedSnapshotServer.when(() -> SeleniumRobotSnapshotServerConnector.getInstance()).thenReturn(snapshotServerConnector);
			TestNGResultUtils.changeTestResultWithSnapshotComparison(testResult);
			verify(snapshotServerConnector, never()).checkSnapshotHasNoDifferences(any(Snapshot.class), anyString(), anyString());
		}
	}
	
	
	@Test(groups={"ut"})
	public void testChangeTestResultWithSoftAssertionWithoutKo() {

		TestNGResultUtils.changeTestResultWithSoftAssertion(testResult);
		
		// check setStatus has not been called as no verification failure has been provided
		verify(testResult, never()).setStatus(2);
	}
	
	@Test(groups={"ut"})
	public void testChangeTestResultWithSoftAssertionWithOneKo() {
		
		Throwable ex = new WebDriverException("test exception");
		SeleniumTestsContextManager.getThreadContext().addVerificationFailures(Reporter.getCurrentTestResult(), ex);
		TestNGResultUtils.changeTestResultWithSoftAssertion(testResult);
		
		// check setStatus has not been called as no verification failure has been provided
		verify(testResult).setStatus(2);
		verify(testResult).setThrowable(ex);
	}
	
	@Test(groups={"ut"})
	public void testChangeTestResultWithSoftAssertionWithSeveralKo() {
		
		List<Throwable> throwables = new ArrayList<>();
		throwables.add(new WebDriverException("test exception"));
		throwables.add(new WebDriverException("test exception 2"));
		
		// make this test successful, it will be changed to failed
		List<ITestNGMethod> methods = new ArrayList<>();
		methods.add(testNGMethod);
		when(passedTests.getAllMethods()).thenReturn(methods);
		
		SeleniumTestsContextManager.getThreadContext().addVerificationFailures(Reporter.getCurrentTestResult(), throwables);
		TestNGResultUtils.changeTestResultWithSoftAssertion(testResult);
		
		// check that the throwable associated to result is not the first declared one
		verify(testResult).setStatus(2);
		verify(testResult, never()).setThrowable(throwables.get(0));
		
		// check that test result has been changed
		verify(passedTests).removeResult(testResult);
		verify(failedTests).addResult(testResult);
	}

	@Test(groups={"ut"})
	public void testGetTestStatusStringCreated() {
		when(testResult.getStatus()).thenReturn(-1);
		Assert.assertEquals(TestNGResultUtils.getTestStatusString(testResult), "CREATED");
	}
	@Test(groups={"ut"})
	public void testGetTestStatusStringSuccess() {
		when(testResult.getStatus()).thenReturn(1);
		Assert.assertEquals(TestNGResultUtils.getTestStatusString(testResult), "SUCCESS");
	}
	@Test(groups={"ut"})
	public void testGetTestStatusStringFailure() {
		when(testResult.getStatus()).thenReturn(2);
		Assert.assertEquals(TestNGResultUtils.getTestStatusString(testResult), "FAILURE");
	}
	@Test(groups={"ut"})
	public void testGetTestStatusStringSkip() {
		when(testResult.getStatus()).thenReturn(3);
		Assert.assertEquals(TestNGResultUtils.getTestStatusString(testResult), "SKIP");
	}
	@Test(groups={"ut"})
	public void testGetTestStatusStringPercentageFailure() {
		when(testResult.getStatus()).thenReturn(4);
		Assert.assertEquals(TestNGResultUtils.getTestStatusString(testResult), "SUCCESS_PERCENTAGE_FAILURE");
	}
	@Test(groups={"ut"})
	public void testGetTestStatusStringPercentageStarted() {
		when(testResult.getStatus()).thenReturn(16);
		Assert.assertEquals(TestNGResultUtils.getTestStatusString(testResult), "STARTED");
	}
	@Test(groups={"ut"})
	public void testGetTestStatusStringPercentageUnknown() {
		when(testResult.getStatus()).thenReturn(5);
		Assert.assertEquals(TestNGResultUtils.getTestStatusString(testResult), "UNKNOWN");
	}
}
