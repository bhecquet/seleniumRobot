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
    protected String priority;
    List<String> components = new ArrayList<>();
    Map<String, String> customFields = new HashMap<>();
	
	public JiraBean(String id, String summary, String description, String issueType, String priority) {
		this(summary, description, priority, issueType, null, null, null, null, null, null, null, null);
		this.id = id;
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
		super(id, summary, description, testName, testStep, assignee, reporter, screenshots, detailedResultFile);
    	this.issueType = issueType;
    	
        if (components != null) {
        	this.components = components;
        }
        this.priority = priority;
        
        if (fields != null) {
        	this.customFields = fields;
        }
        

		if (priority == null) {
			throw new ConfigurationException(String.format("'bugtracker.priority' parameter not set"));
		}
		if (issueType == null) {
			throw new ConfigurationException("'bugtracker.jira.issueType' parameter has not been set, it's mandatory (type of the issue that will be created. E.g: 'Bug'");
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

	public String getPriority() {
		return priority;
	}

	public Map<String, String> getCustomFields() {
		return customFields;
	}

}
