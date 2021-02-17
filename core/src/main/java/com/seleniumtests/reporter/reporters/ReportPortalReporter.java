package com.seleniumtests.reporter.reporters;

import java.io.File;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;

import org.testng.ITestContext;
import org.testng.ITestResult;

import com.epam.reportportal.listeners.ItemStatus;
import com.epam.reportportal.listeners.LogLevel;
import com.epam.reportportal.service.ReportPortal;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.seleniumtests.connectors.tms.reportportal.ReportPortalService;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestStep;

public class ReportPortalReporter extends CommonReporter {
	
	ReportPortalService reportPortalService;

	public ReportPortalReporter(ReportPortalService reportPortalService) {
		this.reportPortalService = reportPortalService;
	}
	
	public void generateReport(ITestResult testResult) {
		SeleniumTestsContext testContext = TestNGResultUtils.getSeleniumRobotTestContext(testResult);
		if (testContext == null) {
			return;
		}
		
		// get files referenced by the steps
		for (TestStep testStep: testContext.getTestStepManager().getTestSteps()) {
			sendStep(testStep);
		}
		
// code pour ajouter des données à un test
// https://github.com/reportportal/agent-java-testNG/blob/develop/src/test/java/com/epam/reportportal/testng/integration/feature/callback/CallbackReportingTest.java		
//		ItemTreeUtils.retrieveLeaf(testResult, ITEM_TREE).ifPresent(itemLeaf -> {
//			for (Entry<ItemTreeKey, TestItemLeaf> entry: itemLeaf.getChildItems().entrySet()) {
//				System.out.println(entry.getKey().getName());
//			}
//			if ("firstTest".equals(testResult.getName())) {
//				sendFinishRequest(itemLeaf, "PASSED", "firstTest");
//			}
//
//			if ("secondTest".equals(testResult.getName())) {
//				sendFinishRequest(itemLeaf, "FAILED", "secondTest");
//				attachLog(itemLeaf);
//			}
//		});
//
	}
//	
//	private void sendFinishRequest(TestItemTree.TestItemLeaf testResultLeaf, String status, String description) {
//		FinishTestItemRQ finishTestItemRQ = new FinishTestItemRQ();
//		finishTestItemRQ.setDescription(description);
//		finishTestItemRQ.setStatus(status);
//		finishTestItemRQ.setEndTime(Calendar.getInstance().getTime());
//		ItemTreeReporter.finishItem(TestNGService.getReportPortal().getClient(), finishTestItemRQ, ITEM_TREE.getLaunchId(), testResultLeaf)
//				.cache()
//				.blockingGet();
//	}
//
//	private void attachLog(TestItemTree.TestItemLeaf testItemLeaf) {
//		ItemTreeReporter.sendLog(
//				TestNGService.getReportPortal().getClient(),
//				"ERROR",
//				"Error message",
//				Calendar.getInstance().getTime(),
//				ITEM_TREE.getLaunchId(),
//				testItemLeaf
//		);
//	}

	private void sendStep(TestStep testStep) {
		if (Boolean.FALSE.equals(testStep.getFailed())) {
			reportPortalService.getLaunch().getStepReporter().sendStep(ItemStatus.PASSED, 
					testStep.getName(),
					testStep.getAllAttachments(true).toArray(new File[] {}));
		} else {
			reportPortalService.getLaunch().getStepReporter().sendStep(ItemStatus.FAILED, 
					testStep.getName(),
					testStep.getActionException(),
					testStep.getAllAttachments(true).toArray(new File[] {}));
		}
		
		
		
		
		
		for (TestAction action: testStep.getStepActions()) {
			if (Boolean.FALSE.equals(testStep.getFailed())) {
				ReportPortal.emitLog(itemId -> buildSaveLogRequest(itemId, action.getName(), LogLevel.INFO));
			} else {
				ReportPortal.emitLog(itemId -> buildSaveLogRequest(itemId, action.getName(), LogLevel.ERROR));
			}
			/*if (action instanceof TestStep) {
				sendStep((TestStep) action);
			} else {
				reportPortalService.getLaunch().getStepReporter().sendStep(action.getFailed() == false ? ItemStatus.FAILED: ItemStatus.PASSED, 
						action.getName());
				reportPortalService.getLaunch().getStepReporter().finishPreviousStep();
			}*/
		}

		reportPortalService.getLaunch().getStepReporter().finishPreviousStep();
		
	}
	
	private SaveLogRQ buildSaveLogRequest(String itemId, String message, LogLevel level) {
		SaveLogRQ rq = new SaveLogRQ();
		rq.setItemUuid(itemId);
		rq.setMessage(message);
		rq.setLevel(level.name());
		rq.setLogTime(Calendar.getInstance().getTime());
		return rq;
	}
	
	@Override
	protected void generateReport(Map<ITestContext, Set<ITestResult>> resultSet, String outdir, boolean optimizeReport,
			boolean finalGeneration) {
		// nothing to do
	}
}
