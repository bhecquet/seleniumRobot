package com.seleniumtests.connectors.tms.squash;

import com.seleniumtests.connectors.tms.squash.entities.Campaign;
import com.seleniumtests.connectors.tms.squash.entities.CampaignFolder;
import com.seleniumtests.connectors.tms.squash.entities.Entity;
import com.seleniumtests.connectors.tms.squash.entities.Iteration;
import com.seleniumtests.connectors.tms.squash.entities.IterationTestPlanItem;
import com.seleniumtests.connectors.tms.squash.entities.Project;
import com.seleniumtests.connectors.tms.squash.entities.TestCase;
import com.seleniumtests.customexception.ConfigurationException;

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
		this.url.replace("//", "/"); // in case of double '/' in URL
		this.user = user;
		this.password = password;
		
		Entity.configureEntity(user, password, this.url);
		testConnection();
		
		currentProject = getProject(projectName);
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

	public Project getProject(String projectName) {
		for (Project project: Project.getAll()) {
			if (project.getName().equals(projectName)) {
				return project;
			}
		}
		throw new ConfigurationException(String.format("Cannot find project %s on SquashTM. Maybe user %s has not rights to see it", projectName, user));
	}

	
	public void getIterations() {
		
	}
	
	/**
	 * Creates a campaign if it does not exist
	 * @param project		project in which this campaign will be created
	 * @param campaignName	name of the campaign to create
	 * @param folder		folder to which campaign will be created
	 */
	public Campaign createCampaign(String campaignName, String folder) {
		

		// create folder where campaign will be located
		CampaignFolder parentFolder = null;
		for (String folderName: folder.split("/")) {
			parentFolder = CampaignFolder.create(currentProject, parentFolder, folderName);
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
	
	public IterationTestPlanItem addTestCaseInIteration(Iteration iteration, int testCaseId) {
		
		for (IterationTestPlanItem testPlanItem: iteration.getAllTestCases()) {
			if (testCaseId == testPlanItem.getTestCase().getId()) {
				return testPlanItem;
			}
		}
		
		return iteration.addTestCase(new TestCase(testCaseId));
	}
	
	public void addResultToExecution(String result, String executionId) {
		/*http://localhost:8080/squash/api/rest/latest/executions/84
		
		{
			  "_type" : "execution",
			  "execution_status" : "SUCCESS"
			}*/
	}
}
