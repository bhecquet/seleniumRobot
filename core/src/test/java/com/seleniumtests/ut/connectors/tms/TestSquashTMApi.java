package com.seleniumtests.ut.connectors.tms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import com.seleniumtests.connectors.tms.squash.entities.*;
import com.seleniumtests.customexception.ScenarioException;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.connectors.tms.squash.SquashTMApi;
import com.seleniumtests.customexception.ConfigurationException;

import kong.unirest.GetRequest;
import kong.unirest.UnirestException;

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
	private IterationTestPlanItem testPlanItemWithDataset1;
	private IterationTestPlanItem testPlanItemWithDataset2;
	private TestCase testCase1;
	private TestCase testCase2;
	private Dataset dataset1;
	private Dataset dataset2;


	@BeforeMethod(groups={"ut"})
	public void init() {
		
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
		dataset1 = spy(new Dataset("", 4, "DS1", testCase1));
		dataset2 = spy(new Dataset("", 5, "DS2"));
		testPlanItem1 = spy(new IterationTestPlanItem("http://localhost:4321/iteration-test-plan-items/1", 1, testCase1, null));
		testPlanItem2 = spy(new IterationTestPlanItem("http://localhost:4321/iteration-test-plan-items/1", 1, testCase2, null));
		testPlanItemWithDataset1 = spy(new IterationTestPlanItem("http://localhost:4321/iteration-test-plan-items/1", 1, testCase1, dataset1));
		testPlanItemWithDataset2 = spy(new IterationTestPlanItem("http://localhost:4321/iteration-test-plan-items/1", 1, testCase2, dataset2));

	}
	
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class)
	public void testServerInError() {
		try (MockedStatic mockedProject = mockStatic(Project.class)) {
			createServerMock("GET", "/api/rest/latest/projects", 500, "{}");
			mockedProject.when(() -> Project.get("project1")).thenReturn(project1);
			SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
		}
	}
	
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class)
	public void testServerInError2() {
		try (MockedStatic mockedProject = mockStatic(Project.class)) {
			GetRequest getRequest = (GetRequest) createServerMock("GET", "/api/rest/latest/projects", 200, "{}");
			when(getRequest.asJson()).thenThrow(UnirestException.class);

			mockedProject.when(() -> Project.get("project1")).thenReturn(project1);
			SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
		}
	}
	
	@Test(groups={"ut"})
	public void testGetExistingProject() {
		try (MockedStatic mockedProject = mockStatic(Project.class)) {
			mockedProject.when(() -> Project.get("project1")).thenReturn(project1);
			SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
			Assert.assertEquals(api.getCurrentProject(), project1);
		}
	}
	
	/**
	 * Project does not exist on Squash TM => raise error
	 */
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class)
	public void testGetNoExistingProject() {
		try (MockedStatic mockedProject = mockStatic(Project.class)) {
			mockedProject.when(() -> Project.get("project3")).thenThrow(new ConfigurationException("Project does not exist"));
			SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project3");
		}
	}
	
	@Test(groups={"ut"})
	public void testCreateUnexistingCampaign() {
		try (MockedStatic mockedProject = mockStatic(Project.class);
			 MockedStatic mockedCampaign = mockStatic(Campaign.class);
			 MockedStatic mockedCampaignFolder = mockStatic(CampaignFolder.class);
			) {
			mockedProject.when(() -> Project.get("project1")).thenReturn(project1);

			doReturn(Arrays.asList(campaign1, campaign2)).when(project1).getCampaigns();

			Campaign myCampaign = new Campaign("http://localhost:4321/campaigns/3", 3, "mycampaign");
			mockedCampaign.when(() -> Campaign.create(project1, "mycampaign", null)).thenReturn(myCampaign);

			SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
			Campaign newCampaign = api.createCampaign("mycampaign", null);
			Assert.assertEquals(newCampaign, myCampaign);

			// check campaign creation has been called
			mockedCampaign.verify(() -> Campaign.create(project1, "mycampaign", null));

			// check no folder has been created
			mockedCampaignFolder.verify(() -> CampaignFolder.create(eq(project1), any(), anyString()), never());
		}
	}
	
	/**
	 * Check that we do not recreate a campaign if it already exist
	 */
	@Test(groups={"ut"})
	public void testDoNotCreateExistingCampaign() {
		try (MockedStatic mockedProject = mockStatic(Project.class);
			 MockedStatic mockedCampaign = mockStatic(Campaign.class);
			 MockedStatic mockedCampaignFolder = mockStatic(CampaignFolder.class);
		) {
			mockedProject.when(() -> Project.get("project1")).thenReturn(project1);
			doReturn(Arrays.asList(campaign1, campaign2)).when(project1).getCampaigns();

			SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
			Campaign newCampaign = api.createCampaign("campaign1", "");
			Assert.assertEquals(newCampaign, campaign1);

			// check campaign creation has not been called as it already exists
			mockedCampaign.verify(() -> Campaign.create(project1, "campaign1", null), never());

			// check no folder has been created
			mockedCampaignFolder.verify(() -> CampaignFolder.create(eq(project1), any(), anyString()), never());
		}
	}

	/**
	 * Check that folders are created when they to not exist for this project
	 */
	@Test(groups={"ut"})
	public void testCreateCampaignWithFolder() {
		try (MockedStatic mockedProject = mockStatic(Project.class);
			 MockedStatic mockedCampaign = mockStatic(Campaign.class);
			 MockedStatic mockedCampaignFolder = mockStatic(CampaignFolder.class);
		) {
			mockedProject.when(() -> Project.get("project1")).thenReturn(project1);
			doReturn(Arrays.asList(campaign1, campaign2)).when(project1).getCampaigns();
			mockedCampaignFolder.when(() -> CampaignFolder.getAll(project1)).thenReturn(Arrays.asList(campaignFolder1, campaignFolder2));

			Campaign myCampaign = new Campaign("http://localhost:4321/campaigns/3", 3, "mycampaign");
			mockedCampaign.when(() -> Campaign.create(project1, "mycampaign", null)).thenReturn(myCampaign);

			SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
			Campaign newCampaign = api.createCampaign("mycampaign", "myFolder/folder1");
			Assert.assertEquals(newCampaign, myCampaign);

			// check campaign creation has been called
			mockedCampaign.verify(() -> Campaign.create(project1, "mycampaign", null));

			// check 2 folders has been created
			mockedCampaignFolder.verify(() -> CampaignFolder.create(eq(project1), any(), eq("myFolder")));
			mockedCampaignFolder.verify(() -> CampaignFolder.create(eq(project1), any(), eq("folder1")));
		}
	}
	
	@Test(groups={"ut"})
	public void testCreateCampaignWithExistingFolder() {
		try (MockedStatic mockedProject = mockStatic(Project.class);
			 MockedStatic mockedCampaign = mockStatic(Campaign.class);
			 MockedStatic mockedCampaignFolder = mockStatic(CampaignFolder.class);
		) {
			mockedProject.when(() -> Project.get("project1")).thenReturn(project1);

			doReturn(Arrays.asList(campaign1, campaign2)).when(project1).getCampaigns();
			mockedCampaignFolder.when(() -> CampaignFolder.getAll(project1)).thenReturn(Arrays.asList(campaignFolder1, campaignFolder2));

			Campaign myCampaign = new Campaign("http://localhost:4321/campaigns/3", 3, "mycampaign");
			mockedCampaign.when(() -> Campaign.create(project1, "mycampaign", campaignFolder2)).thenReturn(myCampaign);

			SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
			Campaign newCampaign = api.createCampaign("mycampaign", "campaignFolder1/campaignFolder2");
			Assert.assertEquals(newCampaign, myCampaign);

			// check campaign creation has been called
			mockedCampaign.verify(() -> Campaign.create(project1, "mycampaign", campaignFolder2));

			// check the 2 folders has not been created
			mockedCampaignFolder.verify(() -> CampaignFolder.create(eq(project1), any(), eq("campaignFolder1")), never());
			mockedCampaignFolder.verify(() -> CampaignFolder.create(eq(project1), any(), eq("campaignFolder2")), never());
		}
	}
	

	@Test(groups={"ut"})
	public void testCreateIteration() {
		try (MockedStatic mockedProject = mockStatic(Project.class);
			 MockedStatic mockedIteration = mockStatic(Iteration.class);
		) {
			mockedProject.when(() -> Project.get("project1")).thenReturn(project1);
			SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
			doReturn(Arrays.asList(iteration1, iteration2)).when(campaign1).getIterations();

			Iteration iteration3 = spy(new Iteration("http://localhost:4321/iterations/3", 3, "myIteration"));
			mockedIteration.when(() -> Iteration.create(campaign1, "myIteration")).thenReturn(iteration3);
			Assert.assertEquals(api.createIteration(campaign1, "myIteration"), iteration3);

			// check campaign creation has been called
			mockedIteration.verify(() -> Iteration.create(campaign1, "myIteration"));
		}
	}
	
	/**
	 * Iteration already exist, do not recreate it
	 */
	@Test(groups={"ut"})
	public void testDoNotCreateIteration() {
		try (MockedStatic mockedProject = mockStatic(Project.class);
			 MockedStatic mockedIteration = mockStatic(Iteration.class);
		) {
			mockedProject.when(() -> Project.get("project1")).thenReturn(project1);
			SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
			doReturn(Arrays.asList(iteration1, iteration2)).when(campaign1).getIterations();

			mockedIteration.when(() -> Iteration.create(campaign1, "myIteration")).thenReturn(iteration1);
			Assert.assertEquals(api.createIteration(campaign1, "iteration1"), iteration1);

			// check campaign creation has been called
			mockedIteration.verify(() -> Iteration.create(campaign1, "iteration1"), never());
		}
	}
	

	@Test(groups={"ut"})
	public void testAddTestCaseInIteration() {
		try (MockedStatic mockedProject = mockStatic(Project.class);
			 MockedStatic mockedTestCase = mockStatic(TestCase.class);
		) {
			mockedProject.when(() -> Project.get("project1")).thenReturn(project1);
			SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
			doReturn(Arrays.asList(testPlanItem1, testPlanItem2)).when(iteration1).getAllTestCases();
			mockedTestCase.when(() -> TestCase.get(3)).thenReturn(testCase1);
			doReturn(testPlanItem1).when(iteration1).addTestCase(testCase1, null);

			IterationTestPlanItem itpi = api.addTestCaseInIteration(iteration1, 3, null);
			Assert.assertEquals(itpi, testPlanItem1);

			// check test case has been added
			verify(iteration1).addTestCase(any(TestCase.class), isNull());
		}
	}

	/**
	 * Add the test case associated to the dataset
	 */
	@Test(groups={"ut"})
	public void testAddTestCaseInIterationWithDataset() {
		try (MockedStatic mockedProject = mockStatic(Project.class);
			 MockedStatic mockedTestCase = mockStatic(TestCase.class);
			 MockedStatic mockedDataset = mockStatic(Dataset.class);
		) {
			mockedProject.when(() -> Project.get("project1")).thenReturn(project1);
			SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
			doReturn(Arrays.asList(testPlanItemWithDataset2)).when(iteration1).getAllTestCases();
			mockedTestCase.when(() -> TestCase.get(1)).thenReturn(testCase1);
			mockedDataset.when(() -> Dataset.get(4)).thenReturn(dataset1);
			doReturn(testPlanItem1).when(iteration1).addTestCase(testCase1, dataset1);

			IterationTestPlanItem itpi = api.addTestCaseInIteration(iteration1, 1, 4);
			Assert.assertEquals(itpi, testPlanItem1);

			// check test case has been added
			verify(iteration1).addTestCase(testCase1, dataset1);
		}
	}
	
	/**
	 * Check exception is raised if test case does not exist
	 */
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class)
	public void testCannotAddTestCaseDoesNotExist() {
		try (MockedStatic mockedProject = mockStatic(Project.class);
			 MockedStatic mockedTestCase = mockStatic(TestCase.class);
		) {
			mockedProject.when(() -> Project.get("project1")).thenReturn(project1);
			SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
			doReturn(Arrays.asList(testPlanItem1, testPlanItem2)).when(iteration1).getAllTestCases();
			mockedTestCase.when(() -> TestCase.get(3)).thenThrow(new ScenarioException(""));

			doReturn(testPlanItem1).when(iteration1).addTestCase(any(TestCase.class), isNull());

			api.addTestCaseInIteration(iteration1, 3, null);
		}
	}

	/**
	 * Check exception is raised if dataset does not exist
	 */
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "Dataset with id 4 does not exist in Squash")
	public void testCannotAddDatasetDoesNotExist() {
		try (MockedStatic mockedProject = mockStatic(Project.class);
			 MockedStatic mockedTestCase = mockStatic(TestCase.class);
			 MockedStatic mockedDataset = mockStatic(Dataset.class);
		) {
			mockedProject.when(() -> Project.get("project1")).thenReturn(project1);
			SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
			doReturn(Arrays.asList(testPlanItemWithDataset2)).when(iteration1).getAllTestCases();
			mockedTestCase.when(() -> TestCase.get(1)).thenReturn(testCase1);
			mockedDataset.when(() -> Dataset.get(4)).thenThrow(new ScenarioException(""));

			doReturn(testPlanItem1).when(iteration1).addTestCase(any(TestCase.class), any(Dataset.class));

			api.addTestCaseInIteration(iteration1, 1, 4);
		}
	}

	/**
	 * Check exception is raised if dataset does not belong to test case
	 */
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "Dataset with id 5 does not belong to Test case with id 1")
	public void testCannotAddDatasetDoesNotBelongToTestCAse() {
		try (MockedStatic mockedProject = mockStatic(Project.class);
			 MockedStatic mockedTestCase = mockStatic(TestCase.class);
			 MockedStatic mockedDataset = mockStatic(Dataset.class);
		) {
			mockedProject.when(() -> Project.get("project1")).thenReturn(project1);
			SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
			doReturn(Arrays.asList(testPlanItemWithDataset2)).when(iteration1).getAllTestCases();
			mockedTestCase.when(() -> TestCase.get(1)).thenReturn(testCase1);
			mockedDataset.when(() -> Dataset.get(5)).thenReturn(dataset2);

			doReturn(testPlanItem1).when(iteration1).addTestCase(any(TestCase.class), any(Dataset.class));

			api.addTestCaseInIteration(iteration1, 1, 5);
		}
	}
	
	/**
	 * Test case already exists in iteration already exist, do not recreate it
	 */
	@Test(groups={"ut"})
	public void testDoNotAddTestCase() {
		try (MockedStatic mockedProject = mockStatic(Project.class);
			 MockedStatic mockedTestCase = mockStatic(TestCase.class);
		) {
			mockedProject.when(() -> Project.get("project1")).thenReturn(project1);
			SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
			doReturn(Arrays.asList(testPlanItem1, testPlanItem2)).when(iteration1).getAllTestCases();
			mockedTestCase.when(() -> TestCase.get(3)).thenReturn(testCase1);
			doReturn(testPlanItem1).when(iteration1).addTestCase(testCase1, null);

			IterationTestPlanItem itpi = api.addTestCaseInIteration(iteration1, 2, null);
			Assert.assertEquals(itpi, testPlanItem2);

			// check test case has been added
			verify(iteration1, never()).addTestCase(any(TestCase.class), isNull());
		}
	}

	@Test(groups={"ut"})
	public void testDoNotAddTestCaseWithDataset() {
		try (MockedStatic mockedProject = mockStatic(Project.class);
			 MockedStatic mockedTestCase = mockStatic(TestCase.class);
			 MockedStatic mockedDataset = mockStatic(Dataset.class);
		) {
			mockedProject.when(() -> Project.get("project1")).thenReturn(project1);
			SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
			doReturn(Arrays.asList(testPlanItemWithDataset1, testPlanItemWithDataset2)).when(iteration1).getAllTestCases();

			IterationTestPlanItem itpi = api.addTestCaseInIteration(iteration1, 2, 5);
			Assert.assertEquals(itpi, testPlanItemWithDataset2);

			// check test case has been added
			verify(iteration1, never()).addTestCase(any(TestCase.class), isNull());
		}
	}
}
