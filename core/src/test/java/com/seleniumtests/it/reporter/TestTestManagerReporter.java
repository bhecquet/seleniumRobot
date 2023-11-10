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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.connectors.tms.squash.SquashTMApi;
import com.seleniumtests.connectors.tms.squash.SquashTMConnector;
import com.seleniumtests.connectors.tms.squash.entities.Campaign;
import com.seleniumtests.connectors.tms.squash.entities.CampaignFolder;
import com.seleniumtests.connectors.tms.squash.entities.Iteration;
import com.seleniumtests.connectors.tms.squash.entities.IterationTestPlanItem;
import com.seleniumtests.connectors.tms.squash.entities.Project;
import com.seleniumtests.connectors.tms.squash.entities.TestCase;
import com.seleniumtests.connectors.tms.squash.entities.TestPlanItemExecution.ExecutionStatus;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.contexts.TestManagerContext;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.reporter.reporters.CommonReporter;

import kong.unirest.Unirest;

//@PrepareForTest({SquashTMConnector.class, Project.class, CampaignFolder.class, Campaign.class, Iteration.class, Unirest.class, TestCase.class, CommonReporter.class, SeleniumTestsContext.class})
public class TestTestManagerReporter extends ReporterTest {


	@Mock
	private SquashTMApi api;
	
	@Mock
	private Campaign campaign;
	
	@Mock
	private Iteration iteration;
	
	@Mock
	private IterationTestPlanItem iterationTestPlanItem;
	
	private SquashTMConnector squash;
	
	@BeforeMethod(groups={"it"})
	public void initTestManager() throws Exception {

		squash = spy(new SquashTMConnector());
//		PowerMockito.whenNew(SquashTMConnector.class).withNoArguments().thenReturn(squash);
		doReturn(api).when(squash).getApi();
		when(api.createCampaign(anyString(), anyString())).thenReturn(campaign);
		when(api.createIteration(any(Campaign.class), anyString())).thenReturn(iteration);
		when(api.addTestCaseInIteration(eq(iteration), anyInt())).thenReturn(iterationTestPlanItem);
		
	}
	
	@Test(groups={"it"})
	public void testResultIsRecorded() throws Exception {
		try {
			System.setProperty(TestManagerContext.TMS_TYPE, "squash");
			System.setProperty(TestManagerContext.TMS_URL, "http://localhost:1234");
			System.setProperty(TestManagerContext.TMS_PROJECT, "Project");
			System.setProperty(TestManagerContext.TMS_USER, "squash");
			System.setProperty(TestManagerContext.TMS_PASSWORD, "squash");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestManager"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testWithAssert", "testInError", "testSkipped"});
			
			// check we have only one result recording for each test method
			verify(api).setExecutionResult(iterationTestPlanItem, ExecutionStatus.SUCCESS);
			verify(api).setExecutionResult(iterationTestPlanItem, ExecutionStatus.FAILURE, "error");
			verify(api).setExecutionResult(eq(iterationTestPlanItem), eq(ExecutionStatus.FAILURE), contains("fail"));
			verify(api).setExecutionResult(iterationTestPlanItem, ExecutionStatus.BLOCKED);
			
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
		try {
			System.setProperty(TestManagerContext.TMS_TYPE, "squash");
			System.setProperty(TestManagerContext.TMS_URL, "http://localhost:1234");
			System.setProperty(TestManagerContext.TMS_PROJECT, "Project");
			System.setProperty(TestManagerContext.TMS_USER, "squash");
			System.setProperty(TestManagerContext.TMS_PASSWORD, "squash");
			doThrow(new ConfigurationException("Cannot contact Squash TM server API")).when(squash).getApi();
			
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
		try {
			System.setProperty(TestManagerContext.TMS_URL, "http://localhost:1234");
			System.setProperty(TestManagerContext.TMS_PROJECT, "Project");
			System.setProperty(TestManagerContext.TMS_USER, "squash");
			System.setProperty(TestManagerContext.TMS_PASSWORD, "squash");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestManager"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			// check we do not try to access squash
			verify(squash, never()).getApi();
			
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
		try {
			System.setProperty(TestManagerContext.TMS_TYPE, "squash");
			System.setProperty(TestManagerContext.TMS_URL, "http://localhost:1234");
			System.setProperty(TestManagerContext.TMS_PROJECT, "Project");
			System.setProperty(TestManagerContext.TMS_USER, "squash");
			System.setProperty(TestManagerContext.TMS_PASSWORD, "squash");
			
			doThrow(new ConfigurationException("Wrong Test ID")).when(api).addTestCaseInIteration(eq(iteration), anyInt());
			
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
