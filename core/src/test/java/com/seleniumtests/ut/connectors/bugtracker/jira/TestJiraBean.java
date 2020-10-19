package com.seleniumtests.ut.connectors.bugtracker.jira;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.bugtracker.jira.JiraBean;
import com.seleniumtests.customexception.ConfigurationException;

public class TestJiraBean extends GenericTest {

	@Test(groups="ut")
	public void testBeanCreation() {
		Map<String, String> customFieldsValues = new HashMap<>();
		new JiraBean("summary",
				"description",
				"P1",
				"Bug",
				"testName",
				null,
				"assignee",
				"reporter",
				new ArrayList<>(),
				new File(""),
				customFieldsValues,
				Arrays.asList("comp1", "comp2"));
	}
	@Test(groups="ut", expectedExceptions=ConfigurationException.class)
	public void testBeanCreationNoRpiority() {
		Map<String, String> customFieldsValues = new HashMap<>();
		new JiraBean("summary",
				"description",
				null,
				"Bug",
				"testName",
				null,
				"assignee",
				"reporter",
				new ArrayList<>(),
				new File(""),
				customFieldsValues,
				Arrays.asList("comp1", "comp2"));
	}
	
	@Test(groups="ut", expectedExceptions=ConfigurationException.class)
	public void testBeanCreationNoIssueType() {
		Map<String, String> customFieldsValues = new HashMap<>();
		new JiraBean("summary",
				"description",
				"P1",
				null,
				"testName",
				null,
				"assignee",
				"reporter",
				new ArrayList<>(),
				new File(""),
				customFieldsValues,
				Arrays.asList("comp1", "comp2"));
	}
}
