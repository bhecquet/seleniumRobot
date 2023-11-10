package com.seleniumtests.ut.connectors.tms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;

//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.connectors.tms.squash.SquashTMApi;
import com.seleniumtests.connectors.tms.squash.entities.Campaign;
import com.seleniumtests.connectors.tms.squash.entities.CampaignFolder;
import com.seleniumtests.connectors.tms.squash.entities.Iteration;
import com.seleniumtests.connectors.tms.squash.entities.IterationTestPlanItem;
import com.seleniumtests.connectors.tms.squash.entities.Project;
import com.seleniumtests.connectors.tms.squash.entities.TestCase;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.GetRequest;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

//@PrepareForTest({Project.class, CampaignFolder.class, Campaign.class, Iteration.class, Unirest.class, TestCase.class})
public class TestSquashTMApi extends ConnectorsTest {
	
	private Project project1;
	private Project project2;
	private Campaign campaign1;
	private Campaign campaign2;
	private CampaignFolder campaignFolder1;
	private CampaignFolder campaignFolder2;
	private Iteration iteration1;
	private Iteration iteration2;
	private IterationTestPlanItem testPlanItem1;
	private IterationTestPlanItem testPlanItem2;
	private TestCase testCase1;
	private TestCase testCase2;

	@BeforeMethod(groups={"ut"})
	public void init() {
//		PowerMockito.mockStatic(Project.class);
//		PowerMockito.mockStatic(Campaign.class);
//		PowerMockito.mockStatic(CampaignFolder.class);
//		PowerMockito.mockStatic(Iteration.class);
//		PowerMockito.mockStatic(TestCase.class);
		
		// server is present by default
		createServerMock("GET", "/api/rest/latest/projects", 200, "{}");
		
		project1 = spy(new Project("http://localhost:4321/projects/1", 1, "project1"));
		project2 = spy(new Project("http://localhost:4321/projects/2", 2, "project2"));
		campaign1 = spy(new Campaign("http://localhost:4321/campaigns/1", 1, "campaign1"));
		campaign2 = spy(new Campaign("http://localhost:4321/campaigns/2", 2, "campaign2"));
		campaignFolder1 = spy(new CampaignFolder("http://localhost:4321/campaigns-folders/1", 1, "campaignFolder1", project1, null));
		campaignFolder2 = spy(new CampaignFolder("http://localhost:4321/campaigns-folders/2", 2, "campaignFolder2", project1, campaignFolder1));
		iteration1 = spy(new Iteration("http://localhost:4321/iterations/1", 1, "iteration1"));
		iteration2 = spy(new Iteration("http://localhost:4321/iterations/2", 2, "iteration2"));
		testCase1 = spy(new TestCase(1));
		testCase2 = spy(new TestCase(2));
		testPlanItem1 = spy(new IterationTestPlanItem("http://localhost:4321/iteration-test-plan-items/1", 1, testCase1));
		testPlanItem2 = spy(new IterationTestPlanItem("http://localhost:4321/iteration-test-plan-items/1", 1, testCase2));
	}
	
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class)
	public void testServerInError() {
		createServerMock("GET", "/api/rest/latest/projects", 500, "{}");
//		PowerMockito.when(Project.get("project1")).thenReturn(project1);
		SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
	}
	
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class)
	public void testServerInError2() {
		GetRequest getRequest = (GetRequest)createServerMock("GET", "/api/rest/latest/projects", 200, "{}");
		when(getRequest.asJson()).thenThrow(UnirestException.class);
		
//		PowerMockito.when(Project.get("project1")).thenReturn(project1);
		SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
	}
	
	@Test(groups={"ut"})
	public void testGetExistingProject() {
//		PowerMockito.when(Project.get("project1")).thenReturn(project1);
		SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
		Assert.assertEquals(api.getCurrentProject(), project1);
	}
	
	/**
	 * Project does not exist on Squash TM => raise error
	 */
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class)
	public void testGetNoExistingProject() {
//		PowerMockito.when(Project.get("project3")).thenThrow(new ConfigurationException("Project does not exist"));
		SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project3");
	}
	
	@Test(groups={"ut"})
	public void testCreateUnexistingCampaign() {
//		PowerMockito.when(Project.get("project1")).thenReturn(project1);
		doReturn(Arrays.asList(campaign1, campaign2)).when(project1).getCampaigns();
		
		
		Campaign myCampaign = new Campaign("http://localhost:4321/campaigns/3", 3, "mycampaign");
//		PowerMockito.when(Campaign.create(project1, "mycampaign", null)).thenReturn(myCampaign);
		
		SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
		Campaign newCampaign = api.createCampaign("mycampaign", null);
		Assert.assertEquals(newCampaign, myCampaign);
		
		// check campaign creation has been called
//		PowerMockito.verifyStatic(Campaign.class);
		Campaign.create(project1, "mycampaign", null);
		
		// check no folder has been created
//		PowerMockito.verifyStatic(CampaignFolder.class, never());
		CampaignFolder.create(eq(project1), any(), anyString());
	}
	
	/**
	 * Check that we do not recreate a campaign if it already exist
	 */
	@Test(groups={"ut"})
	public void testDoNotCreateExistingCampaign() {
//		PowerMockito.when(Project.get("project1")).thenReturn(project1);
		doReturn(Arrays.asList(campaign1, campaign2)).when(project1).getCampaigns();
		
		SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
		Campaign newCampaign = api.createCampaign("campaign1", "");
		Assert.assertEquals(newCampaign, campaign1);
		
		// check campaign creation has been called
//		PowerMockito.verifyStatic(Campaign.class, never());
		Campaign.create(project1, "campaign1", null);

		// check no folder has been created
//		PowerMockito.verifyStatic(CampaignFolder.class, never());
		CampaignFolder.create(eq(project1), any(), anyString());
	}

	/**
	 * Check that folders are created when they to not exist for this project
	 */
	@Test(groups={"ut"})
	public void testCreateCampaignWithFolder() {
//		PowerMockito.when(Project.get("project1")).thenReturn(project1);
		doReturn(Arrays.asList(campaign1, campaign2)).when(project1).getCampaigns();
//		PowerMockito.when(CampaignFolder.getAll(project1)).thenReturn(Arrays.asList(campaignFolder1, campaignFolder2));
		
		Campaign myCampaign = new Campaign("http://localhost:4321/campaigns/3", 3, "mycampaign");
//		PowerMockito.when(Campaign.create(project1, "mycampaign", null)).thenReturn(myCampaign);
		
		SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
		Campaign newCampaign = api.createCampaign("mycampaign", "myFolder/folder1");
		Assert.assertEquals(newCampaign, myCampaign);
		
		// check campaign creation has been called
//		PowerMockito.verifyStatic(Campaign.class);
		Campaign.create(project1, "mycampaign", null);
		
		// check 2 folders has been created
//		PowerMockito.verifyStatic(CampaignFolder.class);
		CampaignFolder.create(eq(project1), any(), eq("myFolder"));
//		PowerMockito.verifyStatic(CampaignFolder.class);
		CampaignFolder.create(eq(project1), any(), eq("folder1"));
	}
	
	@Test(groups={"ut"})
	public void testCreateCampaignWithExistingFolder() {
//		PowerMockito.when(Project.get("project1")).thenReturn(project1);
		doReturn(Arrays.asList(campaign1, campaign2)).when(project1).getCampaigns();
//		PowerMockito.when(CampaignFolder.getAll(project1)).thenReturn(Arrays.asList(campaignFolder1, campaignFolder2));
		
		Campaign myCampaign = new Campaign("http://localhost:4321/campaigns/3", 3, "mycampaign");
//		PowerMockito.when(Campaign.create(project1, "mycampaign", campaignFolder2)).thenReturn(myCampaign);
		
		SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
		Campaign newCampaign = api.createCampaign("mycampaign", "campaignFolder1/campaignFolder2");
		Assert.assertEquals(newCampaign, myCampaign);
		
		// check campaign creation has been called
//		PowerMockito.verifyStatic(Campaign.class);
		Campaign.create(project1, "mycampaign", campaignFolder2);
		
		// check the 2 folders has not been created
//		PowerMockito.verifyStatic(CampaignFolder.class, never());
		CampaignFolder.create(eq(project1), any(), eq("campaignFolder1"));
//		PowerMockito.verifyStatic(CampaignFolder.class, never());
		CampaignFolder.create(eq(project1), any(), eq("campaignFolder2"));
	}
	

	@Test(groups={"ut"})
	public void testCreateIteration() {
//		PowerMockito.when(Project.get("project1")).thenReturn(project1);
		SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
		doReturn(Arrays.asList(iteration1, iteration2)).when(campaign1).getIterations();
		
		Iteration iteration3 = spy(new Iteration("http://localhost:4321/iterations/3", 3, "myIteration"));
//		PowerMockito.when(Iteration.create(campaign1, "myIteration")).thenReturn(iteration3);
		Assert.assertEquals(api.createIteration(campaign1, "myIteration"), iteration3);

		// check campaign creation has been called
//		PowerMockito.verifyStatic(Iteration.class);
		Iteration.create(campaign1, "myIteration");
	}
	
	/**
	 * Iteration already exist, do not recreate it
	 */
	@Test(groups={"ut"})
	public void testDoNotCreateIteration() {
//		PowerMockito.when(Project.get("project1")).thenReturn(project1);
		SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
		doReturn(Arrays.asList(iteration1, iteration2)).when(campaign1).getIterations();
		
//		PowerMockito.when(Iteration.create(campaign1, "myIteration")).thenReturn(iteration1);
		Assert.assertEquals(api.createIteration(campaign1, "iteration1"), iteration1);
		
		// check campaign creation has been called
//		PowerMockito.verifyStatic(Iteration.class, never());
		Iteration.create(campaign1, "iteration1");
	}
	

	@Test(groups={"ut"})
	public void testAddTestCaseInIteration() {
//		PowerMockito.when(Project.get("project1")).thenReturn(project1);
		SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
		doReturn(Arrays.asList(testPlanItem1, testPlanItem2)).when(iteration1).getAllTestCases();
//		PowerMockito.when(TestCase.get(3)).thenReturn(testCase1);
		doReturn(testPlanItem1).when(iteration1).addTestCase(testCase1);
		
		IterationTestPlanItem itpi = api.addTestCaseInIteration(iteration1, 3);
		Assert.assertEquals(itpi, testPlanItem1);
		
		// check test case has been added
		verify(iteration1).addTestCase(any(TestCase.class));
		
	}
	
	/**
	 * Check exception is raised if test case does not exist
	 */
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class)
	public void testCannotAddTestCaseDoesNotExist() {
//		PowerMockito.when(Project.get("project1")).thenReturn(project1);
		SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
		doReturn(Arrays.asList(testPlanItem1, testPlanItem2)).when(iteration1).getAllTestCases();
//		PowerMockito.doThrow(new ScenarioException("")).when(TestCase.class);
		TestCase.get(3);
		doReturn(testPlanItem1).when(iteration1).addTestCase(any(TestCase.class));
		
		api.addTestCaseInIteration(iteration1, 3);

		
	}
	
	/**
	 * Iteration already exist, do not recreate it
	 */
	@Test(groups={"ut"})
	public void testDoNotAddTestCase() {
//		PowerMockito.when(Project.get("project1")).thenReturn(project1);
		SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
		doReturn(Arrays.asList(testPlanItem1, testPlanItem2)).when(iteration1).getAllTestCases();
//		PowerMockito.when(TestCase.get(3)).thenReturn(testCase1);
		doReturn(testPlanItem1).when(iteration1).addTestCase(testCase1);

		IterationTestPlanItem itpi = api.addTestCaseInIteration(iteration1, 2);
		Assert.assertEquals(itpi, testPlanItem2);
		
		// check test case has been added
		verify(iteration1, never()).addTestCase(any(TestCase.class));
	}
}
