package com.seleniumtests.it.connector.bugtracker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.bugtracker.jira.JiraConnector;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.FileUtility;

public class TestJiraConnector extends GenericTest {

	private ScreenShot screenshot;
	private TestStep step1;
	private TestStep step2;
	private TestStep stepEnd;

	@BeforeMethod(groups= {"no-ti"})
	public void init() throws IOException {
		File tmpImg = File.createTempFile("img", ".png");
		File tmpHtml = File.createTempFile("html", ".html");
		
		screenshot = new ScreenShot();
		screenshot.setImagePath("screenshot/" + tmpImg.getName());
		screenshot.setHtmlSourcePath("htmls/" + tmpHtml.getName());
		FileUtility.copyFile(tmpImg, new File(screenshot.getFullImagePath()));
		FileUtility.copyFile(tmpHtml, new File(screenshot.getFullHtmlPath()));
		
		step1 = new TestStep("step 1", null, new ArrayList<>(), false);
		step1.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.FULL), 1, null);
		
		step2 = new TestStep("step 2", null, new ArrayList<>(), false);
		step2.setFailed(true);
		step2.addAction(new TestAction("action1", false, new ArrayList<>()));
		step2.addAction(new TestAction("action2", false, new ArrayList<>()));
		step2.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.FULL), 1, null);
		
		stepEnd = new TestStep("Test end", null, new ArrayList<>(), false);
		stepEnd.addSnapshot(new Snapshot(screenshot, "end", SnapshotCheckType.FULL), 1, null);
		stepEnd.addSnapshot(new Snapshot(screenshot, "end2", SnapshotCheckType.FULL), 1, null);
	}
	
	@Test(groups="no-ti", enabled = true)
	public void testJira() {

		Map<String, String> jiraOptions = new HashMap<>();
		jiraOptions.put("jira.openStates", "Open,To Do");
		jiraOptions.put("jira.closeTransition", "Prêt/Démarrer/Livrer/A valider/Validation effectuée/Clore");
		jiraOptions.put("jira.issueType", "Bogue");
		jiraOptions.put("priority", "Important (P3)");
		
		jiraOptions.put("jira.field.Ano-Affecte la version", "VERS.MAJ.22.06");
		jiraOptions.put("jira.field.Libellé tâche ITBM", "-");
		
		JiraConnector jiraConnector = new JiraConnector(System.getProperty("server"), "JGEPV", System.getProperty("user"), System.getProperty("password"), jiraOptions);
		jiraConnector.createIssue(null, null, "core", "testJira", "testng  http://confluence.covea.priv/display/portailSM/Selenium+-+Descriptions+des+scenarios", Arrays.asList(step1, step2, stepEnd), jiraOptions);
//		jiraConnector.issueAlreadyExists(new JiraBean(null, "[Selenium][core][DEV][testng] test myTest KO", "", "Bogue"));
//		jiraConnector.updateIssue("FFC-573", "commentaire", Arrays.asList(screenshot));
//		jiraConnector.closeIssue("FFC-837", "Terminé");
	}
}
