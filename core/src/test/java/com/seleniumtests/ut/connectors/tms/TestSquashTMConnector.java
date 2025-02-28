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
package com.seleniumtests.ut.connectors.tms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import io.github.bhecquet.SquashTMApi;
import io.github.bhecquet.entities.*;
import io.github.bhecquet.exceptions.SquashTmException;
import org.json.JSONObject;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.CustomAttribute;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.tms.squash.SquashTMConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;

public class TestSquashTMConnector extends MockitoTest {
	
	@Mock
	private SquashTMApi api;
	
	@Mock
	private ITestNGMethod testMethod;
	
	@Mock
	private ITestResult testResult;

	@Mock
	private ITestResult testResult2;
	
	@Mock
	private Campaign campaign;
	
	@Mock
	private Iteration iteration;

	@Mock
	private Project project;
	
	@Mock
	private IterationTestPlanItem iterationTestPlanItem;

	CustomAttribute testIdAttr = new CustomAttribute() {
		@Override
		public Class<? extends Annotation> annotationType() {
			return null;
		}

		@Override
		public String[] values() {
			return new String[] {"1"};
		}

		@Override
		public String name() {
			return "testId";
		}
	};

	MockedStatic mockedCampaign;
	MockedStatic mockedIteration;
	MockedStatic mockedProject;
	JSONObject connect;

	@BeforeMethod(groups = "ut")
	public void init(ITestContext testContext) {
		mockedCampaign = mockStatic(Campaign.class);
		mockedIteration = mockStatic(Iteration.class);
		mockedProject = mockStatic(Project.class);

		mockedCampaign.when(() -> Campaign.create(any(Project.class), anyString(), anyString(), anyMap())).thenReturn(campaign);
		mockedIteration.when(() -> Iteration.create(eq(campaign), anyString())).thenReturn(iteration);
		mockedProject.when(() -> Project.get(anyString())).thenReturn(project);

		connect = new JSONObject();
		connect.put(SquashTMConnector.TMS_SERVER_URL, "http://myServer");
		connect.put(SquashTMConnector.TMS_PROJECT, "project");
		connect.put(SquashTMConnector.TMS_USER, "user");
		connect.put(SquashTMConnector.TMS_PASSWORD, "password");

		when(testResult.getMethod()).thenReturn(testMethod);
		when(testResult.getName()).thenReturn("MyTest");
		when(testResult.getTestContext()).thenReturn(testContext);
		when(testResult.getParameters()).thenReturn(new Object[] {});
		when(testResult.getAttribute("testContext")).thenReturn(SeleniumTestsContextManager.getThreadContext());
	}

	@AfterMethod(groups={"ut"}, alwaysRun = true)
	public void reset() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().clear();
		mockedCampaign.close();
		mockedIteration.close();
		mockedProject.close();
	}

	
	@Test(groups={"ut"})
	public void testInitWithAllParameters() {
		
		SquashTMConnector squash = new SquashTMConnector();
		squash.init(connect);
		Assert.assertTrue(squash.getInitialized());
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testInitWithoutUrlParameter() {
		connect.remove(SquashTMConnector.TMS_SERVER_URL);
		
		SquashTMConnector squash = new SquashTMConnector();
		squash.init(connect);
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testInitWithoutProjectParameter() {
		connect.remove(SquashTMConnector.TMS_PROJECT);
		
		SquashTMConnector squash = new SquashTMConnector();
		squash.init(connect);
	}

	/**
	 * Case with token
	 */
	@Test(groups={"ut"})
	public void testInitWithoutUserParameter() {
		connect.remove(SquashTMConnector.TMS_USER);
		
		SquashTMConnector squash = new SquashTMConnector();
		squash.init(connect);
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testInitWithoutPasswordParameter() {
		connect.remove(SquashTMConnector.TMS_PASSWORD);
		
		SquashTMConnector squash = new SquashTMConnector();
		squash.init(connect);
	}

	@Test(groups={"ut"})
	public void testRecordResultTestInSuccess(ITestContext testContext) {

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		// customize test result so that it has attributes
		when(testResult.isSuccess()).thenReturn(true);
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[]{testIdAttr});
		when(iteration.addTestCase(1, null)).thenReturn(iterationTestPlanItem);

		squash.recordResult(testResult);

		// check we call all necessary API methods to record the result
		mockedCampaign.verify(() -> Campaign.create(any(Project.class), eq("Selenium " + testContext.getName()), eq(""), eq(new HashMap<>())));
		mockedIteration.verify(() -> Iteration.create(campaign, SeleniumTestsContextManager.getThreadContext().getApplicationVersion()));
		verify(iteration).addTestCase(1, null);
		verify(api).setExecutionResult(iterationTestPlanItem, TestPlanItemExecution.ExecutionStatus.SUCCESS);
	}

	@Test(groups={"ut"})
	public void testRecordResultTestInSuccessWithDataset(ITestContext testContext) {

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		CustomAttribute datasetIdAttr = new CustomAttribute() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return null;
			}

			@Override
			public String[] values() {
				return new String[] {"10"};
			}

			@Override
			public String name() {
				return "datasetId";
			}
		};
		// customize test result so that it has attributes
		when(testResult.isSuccess()).thenReturn(true);
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {testIdAttr, datasetIdAttr});
		when(iteration.addTestCase(1, 10)).thenReturn(iterationTestPlanItem);

		squash.recordResult(testResult);

		// check we call all necessary API methods to record the result
		mockedCampaign.verify(() -> Campaign.create(any(Project.class), eq("Selenium " + testContext.getName()), eq(""), eq(new HashMap<>())));
		mockedIteration.verify(() -> Iteration.create(campaign, SeleniumTestsContextManager.getThreadContext().getApplicationVersion()));
		verify(iteration).addTestCase(1, 10);
		verify(api).setExecutionResult(iterationTestPlanItem, TestPlanItemExecution.ExecutionStatus.SUCCESS);
	}

	/**
	 * Check result is not recorded when testId is not available
	 * @param testContext
	 */
	@Test(groups={"ut"})
	public void testRecordResultTestInSuccessNoId(ITestContext testContext) {

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		// customize test result so that it has attributes
		when(testMethod.getMethodName()).thenReturn("myTestMethod");
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {});
		when(testResult.isSuccess()).thenReturn(true);

		squash.recordResult(testResult);

		// check we call all necessary API methods to record the result
		mockedCampaign.verify(() -> Campaign.create(any(Project.class), eq("Selenium " + testContext.getName()), eq(""), eq(new HashMap<>())), never());
		mockedIteration.verify(() -> Iteration.create(campaign, SeleniumTestsContextManager.getThreadContext().getApplicationVersion()), never());
		verify(iteration, never()).addTestCase(1, null);
	}

	/**
	 * Check cache is used when 2 tests records on the same campaign
	 * @param testContext
	 */
	@Test(groups={"ut"})
	public void testRecordResultTestInSuccessOnSameCampaign(ITestContext testContext) {

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		// customize test result so that it has attributes
		when(testResult.isSuccess()).thenReturn(true);
		when(testResult2.getMethod()).thenReturn(testMethod);
		when(testResult2.isSuccess()).thenReturn(true);
		when(testResult2.getName()).thenReturn("MyTest");
		when(testResult2.getTestContext()).thenReturn(testContext);
		when(testResult2.getParameters()).thenReturn(new Object[] {});
		when(testResult2.getAttribute("testContext")).thenReturn(SeleniumTestsContextManager.getThreadContext());
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {testIdAttr});
		when(iteration.addTestCase(1, null)).thenReturn(iterationTestPlanItem);

		squash.recordResult(testResult);
		squash.recordResult(testResult2);

		// check we call all necessary API methods to record the result
		mockedCampaign.verify(() -> Campaign.create(any(Project.class), eq("Selenium " + testContext.getName()), eq(""), eq(new HashMap<>())));
		mockedIteration.verify(() -> Iteration.create(campaign, SeleniumTestsContextManager.getThreadContext().getApplicationVersion()));
		verify(iteration, times(2)).addTestCase(1, null);
		verify(api, times(2)).setExecutionResult(iterationTestPlanItem, TestPlanItemExecution.ExecutionStatus.SUCCESS);
	}

	/**
	 * Check that if the testID is not valid, we raise an error
	 * @param testContext
	 */
	@Test(groups={"ut"})
	public void testRecordResultTestWrongTestId(ITestContext testContext) {

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		CustomAttribute wrongTestIdAttr = new CustomAttribute() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return null;
			}

			@Override
			public String[] values() {
				return new String[] {"A"};
			}

			@Override
			public String name() {
				return "testId";
			}
		};

		// customize test result so that it has attributes
		when(testResult.isSuccess()).thenReturn(true);
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {wrongTestIdAttr});

		squash.recordResult(testResult);

		// check we call all necessary API methods to record the result
		mockedCampaign.verify(() -> Campaign.create(any(Project.class), eq("Selenium " + testContext.getName()), eq(""), eq(new HashMap<>())), never());
	}

	@Test(groups={"ut"})
	public void testRecordResultTestInError(ITestContext testContext) {

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		// customize test result so that it has attributes
		when(testResult.isSuccess()).thenReturn(false);
		when(testResult.getStatus()).thenReturn(2);
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {testIdAttr});
		when(iteration.addTestCase(1, null)).thenReturn(iterationTestPlanItem);

		squash.recordResult(testResult);

		// check we call all necessary API methods to record the result
		verify(api).setExecutionResult(iterationTestPlanItem, TestPlanItemExecution.ExecutionStatus.FAILURE, null);
	}

	/**
	 * Same as above, adding a comment to result, from raised exception
	 * @param testContext
	 */
	@Test(groups={"ut"})
	public void testRecordResultTestInErrorWithComment(ITestContext testContext) {

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		// customize test result so that it has attributes
		when(testResult.isSuccess()).thenReturn(false);
		when(testResult.getStatus()).thenReturn(2);
		when(testResult.getThrowable()).thenReturn(new WebDriverException("error from driver"));
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {testIdAttr});
		when(iteration.addTestCase(1, null)).thenReturn(iterationTestPlanItem);

		squash.recordResult(testResult);

		verify(api).setExecutionResult(eq(iterationTestPlanItem), eq(TestPlanItemExecution.ExecutionStatus.FAILURE), contains("error from driver"));
	}

	@Test(groups={"ut"})
	public void testRecordResultTestSkipped(ITestContext testContext) {

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		// customize test result so that it has attributes
		when(testResult.isSuccess()).thenReturn(false);
		when(testResult.getStatus()).thenReturn(3);
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {testIdAttr});
		when(iteration.addTestCase(1, null)).thenReturn(iterationTestPlanItem);

		squash.recordResult(testResult);

		// check we call all necessary API methods to record the result
		verify(api).setExecutionResult(iterationTestPlanItem, TestPlanItemExecution.ExecutionStatus.BLOCKED);
	}

	/**
	 * Check that if any error occurs during result recording, it does not raise any exception, only message will be displayed
	 * @param testContext
	 */
	@Test(groups={"ut"})
	public void testNoExceptionWhenErrorInRecording(ITestContext testContext) {

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		// customize test result so that it has attributes
		when(testResult.isSuccess()).thenReturn(true);
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {testIdAttr});
		mockedCampaign.when(() -> Campaign.create(any(Project.class), anyString(), anyString(), anyMap())).thenThrow(new SquashTmException("error"));
		when(iteration.addTestCase(1, null)).thenReturn(iterationTestPlanItem);

		squash.recordResult(testResult);

		// check we do not call API as testId is not provided
		mockedCampaign.verify(() -> Campaign.create(any(Project.class), eq("Selenium " + testContext.getName()), eq(""), eq(new HashMap<>())));
		mockedIteration.verify(() -> Iteration.create(campaign, SeleniumTestsContextManager.getThreadContext().getApplicationVersion()), never());
		verify(iteration, never()).addTestCase(1, 10);
	}


	/**
	 * With a custom campaign name, check it's created if it does not exist
	 * @param testContext
	 */
	@Test(groups={"ut"})
	public void testRecordResultTestInSuccessCustomCampaign(ITestContext testContext) {

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		// customize test result so that it has attributes
		when(testResult.isSuccess()).thenReturn(true);
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[]{});
		SeleniumTestsContextManager.getThreadContext().testManager().setTestId(23);
		SeleniumTestsContextManager.getThreadContext().setTestManagerInstance(squash);
		SeleniumTestsContextManager.getThreadContext().testManager().setCampaignName("My Campaign");

		squash.recordResult(testResult);

		mockedCampaign.verify(() -> Campaign.create(any(Project.class), eq("My Campaign"), eq(""), eq(new HashMap<>())));

	}

	/**
	 * With a custom iteration name, check it's created if it does not exist
	 * @param testContext
	 */
	@Test(groups={"ut"})
	public void testRecordResultTestInSuccessCustomIteration(ITestContext testContext) {

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		// customize test result so that it has attributes
		when(testResult.isSuccess()).thenReturn(true);
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[]{});
		SeleniumTestsContextManager.getThreadContext().testManager().setTestId(23);
		SeleniumTestsContextManager.getThreadContext().setTestManagerInstance(squash);
		SeleniumTestsContextManager.getThreadContext().testManager().setIterationName("My Iteration");

		squash.recordResult(testResult);

		mockedIteration.verify(() -> Iteration.create(campaign, "My Iteration"));
	}

	@Test(groups={"ut"})
	public void testRecordResultTestInSuccessCustomCampaignFolder(ITestContext testContext) {

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();


		// customize test result so that it has attributes
		when(testResult.isSuccess()).thenReturn(true);
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[]{});
		SeleniumTestsContextManager.getThreadContext().testManager().setTestId(23);
		SeleniumTestsContextManager.getThreadContext().setTestManagerInstance(squash);
		SeleniumTestsContextManager.getThreadContext().testManager().setCampaignFolderPath("folder1/folder2");

		squash.recordResult(testResult);
		mockedCampaign.verify(() -> Campaign.create(any(Project.class), eq("Selenium " + testContext.getName()), eq("folder1/folder2"), eq(new HashMap<>())));
	}
}
