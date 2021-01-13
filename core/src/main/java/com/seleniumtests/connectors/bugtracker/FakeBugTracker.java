package com.seleniumtests.connectors.bugtracker;

import java.util.List;

import com.seleniumtests.driver.screenshots.ScreenShot;

public class FakeBugTracker extends BugTracker {

	@Override
	public IssueBean issueAlreadyExists(IssueBean issue) {
		return null;
	}

	@Override
	public void updateIssue(String issueId, String messageUpdate, List<ScreenShot> screenShots) {
		logger.info("issue updated");
	}

	@Override
	public void createIssue(IssueBean issueBean) {
		issueBean.setId("1234");
		logger.info("issue created");
	}

	@Override
	public void closeIssue(String issueId, String closingMessage) {
		logger.info("issue closed");
		
	}

}
