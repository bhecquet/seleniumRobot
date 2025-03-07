/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.it.reporter;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.github.bhecquet.SquashTMApi;
import io.github.bhecquet.entities.*;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.connectors.tms.squash.SquashTMConnector;
import com.seleniumtests.core.contexts.TestManagerContext;
import com.seleniumtests.customexception.ConfigurationException;

public class TestTestManagerReporter extends ReporterTest {


	@Mock
	private SquashTMApi api;
	
	@Mock
	private Campaign campaign;
	
	@Mock
	private Iteration iteration;

	@Mock
	private Project project;
	
	@Mock
	private IterationTestPlanItem iterationTestPlanItem;
	

	private SquashTMConnector squash;
	MockedStatic mockedCampaign;
	MockedStatic mockedIteration;
	MockedStatic mockedProject;

	@BeforeMethod(groups={"it"})
	public void initTestManager() throws Exception {

		mockedCampaign = mockStatic(Campaign.class);
		mockedIteration = mockStatic(Iteration.class);
		mockedProject = mockStatic(Project.class);

		mockedProject.when(() -> Project.get(anyString())).thenReturn(project);
		mockedCampaign.when(() -> Campaign.create(any(Project.class), anyString(), anyString(), anyMap())).thenReturn(campaign);
		mockedIteration.when(() -> Iteration.create(eq(campaign), anyString())).thenReturn(iteration);
		when(iteration.addTestCase(1, null)).thenReturn(iterationTestPlanItem);
		when(iteration.addTestCase(eq(1), anyInt())).thenReturn(iterationTestPlanItem);

	}

	@AfterMethod(groups = "it", alwaysRun = true)
	public void reset() {
		mockedCampaign.close();
		mockedIteration.close();
		mockedProject.close();
	}

	/**
	 * Record tests results with tests that have test ID, and other that have testId and datasetId
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testResultIsRecorded() throws Exception {
		try (MockedConstruction mockedSquash = mockConstruction(SquashTMConnector.class,
				withSettings().defaultAnswer(CALLS_REAL_METHODS),
				(mock, context) -> {
			doReturn(api).when(mock).getApi();
		})) {
			System.setProperty(TestManagerContext.TMS_TYPE, "squash");
			System.setProperty(TestManagerContext.TMS_URL, "http://localhost:1234");
			System.setProperty(TestManagerContext.TMS_PROJECT, "Project");
			System.setProperty(TestManagerContext.TMS_USER, "squash");
			System.setProperty(TestManagerContext.TMS_PASSWORD, "squash");

			when(iteration.addTestCase(anyInt(), isNull())).thenReturn(iterationTestPlanItem);
			when(iteration.addTestCase(anyInt(), anyInt())).thenReturn(iterationTestPlanItem);

			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestManager"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testWithAssert", "testInError", "testSkipped"});
			
			// check we have only one result recording for each test method
			verify(api).setExecutionResult(iterationTestPlanItem, TestPlanItemExecution.ExecutionStatus.SUCCESS);
			verify(api).setExecutionResult(iterationTestPlanItem, TestPlanItemExecution.ExecutionStatus.FAILURE, "error");
			verify(api).setExecutionResult(eq(iterationTestPlanItem), eq(TestPlanItemExecution.ExecutionStatus.FAILURE), contains("fail"));
			verify(api).setExecutionResult(iterationTestPlanItem, TestPlanItemExecution.ExecutionStatus.BLOCKED);
			
		} finally {
			System.clearProperty(TestManagerContext.TMS_TYPE);
			System.clearProperty(TestManagerContext.TMS_PROJECT);
			System.clearProperty(TestManagerContext.TMS_URL);
			System.clearProperty(TestManagerContext.TMS_USER);
			System.clearProperty(TestManagerContext.TMS_PASSWORD);
		}
	}
	
	@Test(groups={"it"})
	public void testResultIsNotRecordedServerUnavailable() throws Exception {
		try (MockedConstruction mockedSquash = mockConstruction(SquashTMConnector.class,
				withSettings().defaultAnswer(CALLS_REAL_METHODS),
				(mock, context) -> {
					doThrow(new ConfigurationException("Cannot contact Squash TM server API")).when(mock).getApi();
				})) {
			System.setProperty(TestManagerContext.TMS_TYPE, "squash");
			System.setProperty(TestManagerContext.TMS_URL, "http://localhost:1234");
			System.setProperty(TestManagerContext.TMS_PROJECT, "Project");
			System.setProperty(TestManagerContext.TMS_USER, "squash");
			System.setProperty(TestManagerContext.TMS_PASSWORD, "squash");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestManager"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			// check no result has been recorded
			verify(api, never()).setExecutionResult(eq(iterationTestPlanItem), any());
			
		} finally {
			System.clearProperty(TestManagerContext.TMS_TYPE);
			System.clearProperty(TestManagerContext.TMS_PROJECT);
			System.clearProperty(TestManagerContext.TMS_URL);
			System.clearProperty(TestManagerContext.TMS_USER);
			System.clearProperty(TestManagerContext.TMS_PASSWORD);
		}
	}
	
	@Test(groups={"it"})
	public void testResultIsNotRecordedServerNotConfigured() throws Exception {
		try (MockedConstruction mockedSquash = mockConstruction(SquashTMConnector.class,
				withSettings().defaultAnswer(CALLS_REAL_METHODS),
				(mock, context) -> {
					doReturn(api).when(mock).getApi();
				})) {
			System.setProperty(TestManagerContext.TMS_URL, "http://localhost:1234");
			System.setProperty(TestManagerContext.TMS_PROJECT, "Project");
			System.setProperty(TestManagerContext.TMS_USER, "squash");
			System.setProperty(TestManagerContext.TMS_PASSWORD, "squash");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestManager"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			// check we do not try to access squash
			Assert.assertEquals(mockedSquash.constructed().size(), 0);
			
		} finally {
			System.clearProperty(TestManagerContext.TMS_TYPE);
			System.clearProperty(TestManagerContext.TMS_PROJECT);
			System.clearProperty(TestManagerContext.TMS_URL);
			System.clearProperty(TestManagerContext.TMS_USER);
			System.clearProperty(TestManagerContext.TMS_PASSWORD);
		}
	}
	
	@Test(groups={"it"})
	public void testResultIsNotRecordedWrongTestId() throws Exception {
		try (MockedConstruction mockedSquash = mockConstruction(SquashTMConnector.class,
				withSettings().defaultAnswer(CALLS_REAL_METHODS),
				(mock, context) -> {
					doReturn(api).when(mock).getApi();
					doThrow(new ConfigurationException("Wrong Test ID")).when(iteration).addTestCase(anyInt(), isNull());
				})) {
			System.setProperty(TestManagerContext.TMS_TYPE, "squash");
			System.setProperty(TestManagerContext.TMS_URL, "http://localhost:1234");
			System.setProperty(TestManagerContext.TMS_PROJECT, "Project");
			System.setProperty(TestManagerContext.TMS_USER, "squash");
			System.setProperty(TestManagerContext.TMS_PASSWORD, "squash");

			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestManager"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			// check no result has been recorded
			verify(api, never()).setExecutionResult(eq(iterationTestPlanItem), any());
			
		} finally {
			System.clearProperty(TestManagerContext.TMS_TYPE);
			System.clearProperty(TestManagerContext.TMS_PROJECT);
			System.clearProperty(TestManagerContext.TMS_URL);
			System.clearProperty(TestManagerContext.TMS_USER);
			System.clearProperty(TestManagerContext.TMS_PASSWORD);
		}
	}
	
}
