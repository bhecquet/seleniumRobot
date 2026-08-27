package com.seleniumtests.reporter.reporters;

import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.screenshots.SnapshotComparisonBehaviour;
import org.testng.*;

import java.util.Map;
import java.util.Set;

/**
 * This 'reporter' class aims at updating test result , based on criteria
 * - set test result to failure when snapshot comparison is performed, is KO and behavior is set to 'changeTestResult'
 * <p>
 * This class MUST be executed after results are recorded onto seleniumRobot server as it uses the ID of the test case
 * <p>
 * By modifying test result here, after result recording on server, we have a difference between status of test
 * - in static files (XML / JSON) where test is KO
 * - on server where test is OK with comparison KO => server uses both information to determine whether test is OK or KO
 *
 */
public class ResultUpdaterReporter extends CommonReporter implements IReporter {

    private static final Object lock = new Object();

    @Override
    protected void generateReport(Map<ITestContext, Set<ITestResult>> resultSet, String outdir, boolean optimizeReport, boolean finalGeneration) {


        if (!(SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerActive()
                && SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerCompareSnapshot())) {
            return;
        }

        SeleniumRobotSnapshotServerConnector snapshotServer = SeleniumRobotSnapshotServerConnector.getInstance();

        synchronized (lock) { // be sure we don't update test result several times
            for (Map.Entry<ITestContext, Set<ITestResult>> entry : resultSet.entrySet()) {

                for (ITestResult testResult : entry.getValue()) {

                    if (testResult.getStatus() == ITestResult.FAILURE) {
                        logger.info("Not comparing snapshots as test is already KO");
                        continue;
                    }

                    if (TestNGResultUtils.isUpdateResultReportExecuted(testResult)) {
                        continue;
                    }


                    // check if we have an id from snapshot server
                    Integer testCaseInSessionId = TestNGResultUtils.getSnapshotTestCaseInSessionId(testResult);
                    if (testCaseInSessionId == null) {
                        continue;
                    }

                    StringBuilder errorMessage = new StringBuilder();
                    int snapshotComparisonResult = snapshotServer.getTestCaseInSessionComparisonResult(testCaseInSessionId, errorMessage);

                    // update snapshot comparison result of the run test.
                    TestNGResultUtils.setSnapshotComparisonResult(testResult, snapshotComparisonResult);

                    changeTestResultWithSnapshotComparison(testResult, snapshotComparisonResult);
                    TestNGResultUtils.setUpdateResultReportExecuted(testResult, true);
                }
            }
        }

    }

    /**
     * Change the test result based on snapshot comparison result if required by test configuration
     * If test is OK but comparison fails, then test will be set to "KO" if 'changeTestResult' is set
     * @param testResult				the test result to update
     * @param snapshotComparisonResult	snapshot comparison result which may update the test result
     */
    private void changeTestResultWithSnapshotComparison(ITestResult testResult, int snapshotComparisonResult) {
        // based on snapshot comparison flag, change test result only if comparison is KO
        if (SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerCompareSnapshotBehaviour() == SnapshotComparisonBehaviour.CHANGE_TEST_RESULT
                && snapshotComparisonResult == ITestResult.FAILURE ) {
            testResult.setStatus(ITestResult.FAILURE);
            testResult.setThrowable(new ScenarioException("Snapshot comparison failed"));

            logger.info("Setting test status to KO due to image comparison error");

            // expect 'Test end' step to be present (which should be the case as this reporter is executed after test has finished
            TestStepManager.logThrowableToTestEndStep(testResult);
        }
    }
}
