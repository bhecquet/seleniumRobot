package com.seleniumtests.connectors.bugtracker;

import java.util.List;

import com.seleniumtests.driver.screenshots.ScreenShot;

public class FakeBugTracker extends BugTracker {

	@Override
	public IssueBean issueAlreadyExists(IssueBean issue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateIssue(String issueId, String messageUpdate, List<ScreenShot> screenShots) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createIssue(IssueBean issueBean) {
		// TODO Auto-generated method stub

	}

}
