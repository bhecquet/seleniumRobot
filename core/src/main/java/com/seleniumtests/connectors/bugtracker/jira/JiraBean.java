package com.seleniumtests.connectors.bugtracker.jira;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seleniumtests.connectors.bugtracker.IssueBean;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.logger.TestStep;

public class JiraBean extends IssueBean {

	private String issueType;
    List<String> components = new ArrayList<>();
	
	public JiraBean(String id, String summary, String description, String issueType) {
		this(summary, description, null, issueType, null, null, null, null, null, null, null, null);
	}
	
	/**
     * 
     * @param summary
     * @param description
     * @param priority
     * @param issueType
     * @param testName
     * @param testStep
     * @param assignee
     * @param reporter
     * @param screenshots
     * @param detailedResultFile
     * @param fields				All custom fields that should be filled
     * @param components			All components this issue belongs to
     */
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
    	this(null, summary, description, priority, issueType, testName, testStep, assignee, reporter, screenshots, detailedResultFile, fields, components);

    }
	
	/**
     * @param id					id of the issue on bugtracker. May ben null if the issue has not been created
     * @param summary
     * @param description
     * @param priority
     * @param issueType
     * @param testName
     * @param testStep
     * @param assignee
     * @param reporter
     * @param screenshots
     * @param detailedResultFile
     * @param fields				All custom fields that should be filled
     * @param components			All components this issue belongs to
     */
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
		super(id, summary, description, priority, testName, testStep, assignee, reporter, screenshots, detailedResultFile, fields);
    	this.issueType = issueType;
    	
        if (components != null) {
        	this.components = components;
        }
        

    	if (getIssueType() == null) {
    		throw new ConfigurationException("Issue type must be provided through 'bugtracker.issueType' variable");
    	}
	}
	

    public List<String> getComponents() {
        return components;
    }	

    public String getIssueType() {
		return issueType;
	}

	public void setIssueType(String issueType) {
		this.issueType = issueType;
	}

}
