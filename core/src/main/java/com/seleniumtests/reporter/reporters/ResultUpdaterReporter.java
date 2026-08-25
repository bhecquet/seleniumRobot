package com.seleniumtests.reporter.reporters;

import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.screenshots.SnapshotComparisonBehaviour;
import org.testng.*;

import java.util.Map;
import java.util.Set;

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
        if (SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerCompareSnapshotBehaviour() == SnapshotComparisonBehaviour.CHANGE_TEST_RESULT && snapshotComparisonResult == ITestResult.FAILURE ) {
            testResult.setStatus(ITestResult.FAILURE);
            testResult.setThrowable(new ScenarioException("Snapshot comparison failed"));
        }
    }
}
