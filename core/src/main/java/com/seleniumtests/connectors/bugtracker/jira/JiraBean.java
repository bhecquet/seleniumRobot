package com.seleniumtests.connectors.bugtracker.jira;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.seleniumtests.connectors.bugtracker.IssueBean;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.logger.TestStep;

public class JiraBean extends IssueBean {

	public JiraBean(String id, String summary, String description) {
		super(id, summary, description);
	}
	
	public JiraBean(
    		String summary,
    		String description,
    		String priority,
    	    String issueType,
    		String testName,
    		TestStep testStep,
    		String assignee,
    		String reporter,
    		List<ScreenShot> screenshots,
    		File detailedResultFile,
    		Map<String, String> fields,
    		List<String> components) {
    	super(null, summary, description, priority, issueType, testName, testStep, assignee, reporter, screenshots, detailedResultFile, fields, components);
    }
	
	public JiraBean(String id,
			String summary,
            String description,
            String priority,
    	    String issueType,
            String testName,
            TestStep testStep,
            String assignee,
            String reporter,
            List<ScreenShot> screenshots,
            File detailedResultFile,
            Map<String, String> fields,
            List<String> components) {
		super(id, summary, description, priority, issueType, testName, testStep, assignee, reporter, screenshots, detailedResultFile, fields, components);
	}

}
