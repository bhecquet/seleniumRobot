package com.seleniumtests.connectors.tms.squash;

import java.util.List;

import com.seleniumtests.connectors.tms.squash.entities.Campaign;
import com.seleniumtests.connectors.tms.squash.entities.CampaignFolder;
import com.seleniumtests.connectors.tms.squash.entities.Entity;
import com.seleniumtests.connectors.tms.squash.entities.Iteration;
import com.seleniumtests.connectors.tms.squash.entities.IterationTestPlanItem;
import com.seleniumtests.connectors.tms.squash.entities.Project;
import com.seleniumtests.connectors.tms.squash.entities.TestCase;
import com.seleniumtests.connectors.tms.squash.entities.TestPlanItemExecution;
import com.seleniumtests.connectors.tms.squash.entities.TestPlanItemExecution.ExecutionStatus;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

public class SquashTMApi {
	
	private String url;
	private String user;
	private String password;
	private Project currentProject;
	
	
	public SquashTMApi(String url, String user, String password, String projectName) {
		this.url = url + "/api/rest/latest/";
		this.url = this.url.replace("//", "/").replace(":/", "://"); // in case of double '/' in URL
		this.user = user;
		this.password = password;
		
		Entity.configureEntity(user, password, this.url);
		testConnection();
		
		currentProject = Project.get(projectName);
	}
	
	/**
	 * Test we can join Squash TM server
	 */
	public void testConnection() {
		try {
			HttpResponse<JsonNode> json = Unirest.get(url + Project.PROJECTS_URL)
				.basicAuth(user, password)
				.asJson();
			
			if (json.getStatus() != 200) {
				throw new ConfigurationException(String.format("Error when contactin Squash TM server API %s: %s", url, json.getStatusText()));
			}
		} catch (UnirestException e) {
			throw new ConfigurationException(String.format("Cannot contact Squash TM server API %s: %s", url, e.getMessage()));
		}
		
		
	}

	/**
	 * Creates a campaign if it does not exist
	 * @param project		project in which this campaign will be created
	 * @param campaignName	name of the campaign to create
	 * @param folder		folder to which campaign will be created
	 */
	public Campaign createCampaign(String campaignName, String folder) {
		
		if (folder == null) {
			folder = "";
		}
		
		List<CampaignFolder> campaignFolders = CampaignFolder.getAll(currentProject);
		
		// create folder where campaign will be located
		CampaignFolder parentFolder = null;
		for (String folderName: folder.split("/")) {
			
			if (folderName.isEmpty()) {
				continue;
			}
			
			boolean folderExists = false;
			for (CampaignFolder existingFolder: campaignFolders) {
				if (existingFolder.getName().equals(folderName) 
						&& (existingFolder.getProject() == null || existingFolder.getProject() != null && existingFolder.getProject().getId() == currentProject.getId())
						&& (existingFolder.getParent() == null 
							|| parentFolder == null && existingFolder.getParent() != null && existingFolder.getParent() instanceof Project
							|| (parentFolder != null && existingFolder.getParent() != null && existingFolder.getParent() instanceof CampaignFolder && existingFolder.getParent().getId() == parentFolder.getId()))) {
					folderExists = true;
					parentFolder = existingFolder;
					break;
				}
			}
			
			if (!folderExists) {
				parentFolder = CampaignFolder.create(currentProject, parentFolder, folderName);
			}
		}

		// do not create campaign if it exists
		for (Campaign campaign: currentProject.getCampaigns()) {
			if (campaign.getName().equals(campaignName)) {
				return campaign;
			}
		}
		return Campaign.create(currentProject, campaignName, parentFolder);		
	}
	
	/**
	 * Creates an interation in a campaign if it does not exist
	 * @param campaign		the campaign where iteration will be created
	 * @param iterationName	name of the iteration to create
	 * @return
	 */
	public Iteration createIteration(Campaign campaign, String iterationName) { 
		for (Iteration iteration: campaign.getIterations()) {
			if (iteration.getName().equals(iterationName)) {
				return iteration;
			}
		}
		return Iteration.create(campaign, iterationName);
	}
	
	/**
	 * Add a test case in the selected iteration if it's not already there. Dataset are not handled
	 * Also check that the test case exists
	 * @param iteration		iteration in which test case will be added
	 * @param testCaseId	id of the test case (can be found in Squash TM interface)
	 * @return
	 */
	public IterationTestPlanItem addTestCaseInIteration(Iteration iteration, int testCaseId) {
		
		for (IterationTestPlanItem testPlanItem: iteration.getAllTestCases()) {
			if (testCaseId == testPlanItem.getTestCase().getId()) {
				return testPlanItem;
			}
		}
		
		// check that TestCase is valid
		TestCase testCase;
		try {
			testCase = TestCase.get(testCaseId);
		} catch (ScenarioException e) {
			throw new ConfigurationException(String.format("Test case with id %d does not exist in Squash", testCaseId));
		}
		
		return iteration.addTestCase(testCase);
	}
	
	/**
	 * Add an execution result to the test case
	 * @param testPlanItem	the IterationTestPlanItem which has been executed
	 * @param result		Execution status of the test
	 */
	public void setExecutionResult(IterationTestPlanItem testPlanItem, ExecutionStatus result) {
		TestPlanItemExecution execution = testPlanItem.createExecution();
		execution.setResult(result);
	}

	public Project getCurrentProject() {
		return currentProject;
	}
}
