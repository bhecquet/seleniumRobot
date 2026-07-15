/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.ut.connectors.tms;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.tms.squash.SquashTMConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestStep;
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

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

	@Mock
	private TestCase testCase;

	@Mock
	private io.github.bhecquet.entities.TestStep squashTestStep1;

	@Mock
	private io.github.bhecquet.entities.TestStep squashTestStep2;

	@Mock
	private io.github.bhecquet.entities.TestStep newSquashTestStep;

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

	CustomAttribute updateTestManagerAttr = new CustomAttribute() {
		@Override
		public Class<? extends Annotation> annotationType() {
			return null;
		}

		@Override
		public String[] values() {
			return new String[] {"true"};
		}

		@Override
		public String name() {
			return "updateTestManager";
		}
	};

	CustomAttribute updateTestManagerFalseAttr = new CustomAttribute() {
		@Override
		public Class<? extends Annotation> annotationType() {
			return null;
		}

		@Override
		public String[] values() {
			return new String[] {"false"};
		}

		@Override
		public String name() {
			return "updateTestManager";
		}
	};
	
	MockedStatic mockedCampaign;
	MockedStatic mockedIteration;
	MockedStatic mockedProject;
	MockedStatic<TestCase> mockedTestCase;
	MockedStatic<io.github.bhecquet.entities.TestStep> mockedSquashTestStep;
	JSONObject connect;

	@BeforeMethod(groups = "ut")
	public void init(ITestContext testContext) {
		mockedCampaign = mockStatic(Campaign.class);
		mockedIteration = mockStatic(Iteration.class);
		mockedProject = mockStatic(Project.class);
		mockedTestCase = mockStatic(TestCase.class);
		mockedSquashTestStep = mockStatic(io.github.bhecquet.entities.TestStep.class);

		mockedCampaign.when(() -> Campaign.create(any(Project.class), anyString(), anyString(), anyMap())).thenReturn(campaign);
		mockedIteration.when(() -> Iteration.create(eq(campaign), anyString())).thenReturn(iteration);
		mockedProject.when(() -> Project.get(anyString())).thenReturn(project);
		mockedTestCase.when(() -> TestCase.get(anyInt())).thenReturn(testCase);

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
		mockedTestCase.close();
		mockedSquashTestStep.close();
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
	
	/**
	 * Check that updateTestCase does nothing when updateTestManager attribute is not set
	 */
	@Test(groups={"ut"})
	public void testUpdateTestCaseNotCalledWhenUpdateTestManagerNotSet() {

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {testIdAttr});

		squash.updateTestCase(testResult);

		// TestCase.get should never be called
		mockedTestCase.verify(() -> TestCase.get(anyInt()), never());
	}

	/**
	 * Check that updateTestCase does nothing when updateTestManager is false
	 */
	@Test(groups={"ut"})
	public void testUpdateTestCaseNotCalledWhenUpdateTestManagerFalse() {

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {testIdAttr, updateTestManagerFalseAttr});

		squash.updateTestCase(testResult);

		mockedTestCase.verify(() -> TestCase.get(anyInt()), never());
	}

	/**
	 * Check that updateTestCase does nothing when testCaseId is not set
	 */
	@Test(groups={"ut"})
	public void testUpdateTestCaseNoTestCaseId() {

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {updateTestManagerAttr});

		squash.updateTestCase(testResult);

		mockedTestCase.verify(() -> TestCase.get(anyInt()), never());
	}

	/**
	 * Check that updateTestCase properly updates description and steps
	 */
	@Test(groups={"ut"})
	public void testUpdateTestCaseWithSteps() {

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {testIdAttr, updateTestManagerAttr});
		when(testMethod.getDescription()).thenReturn("Test description");

		// setup TestCase mock
		when(testCase.getId()).thenReturn(1);
		when(squashTestStep1.getId()).thenReturn(100);
		when(squashTestStep2.getId()).thenReturn(101);
		when(testCase.getTestSteps()).thenReturn(Arrays.asList(squashTestStep1, squashTestStep2));

		// setup test steps from SeleniumRobot context
		TestStep step1 = mock(TestStep.class);
		when(step1.getId()).thenReturn("aa");
		when(step1.getDescription()).thenReturn("Step 1 action");
		when(step1.getExpectedResult()).thenReturn("Step 1 expected");
		when(step1.getSnapshots()).thenReturn(new ArrayList<>());

		TestStep stepNoDescription = mock(TestStep.class);
		when(stepNoDescription.getId()).thenReturn("bb");
		when(stepNoDescription.getDescription()).thenReturn("");
		when(stepNoDescription.getExpectedResult()).thenReturn("Step 2 expected");
		when(stepNoDescription.getSnapshots()).thenReturn(new ArrayList<>());

		TestStep step2 = mock(TestStep.class);
		when(step2.getId()).thenReturn("cc");
		when(step2.getDescription()).thenReturn("Step 2 action");
		when(step2.getExpectedResult()).thenReturn("");
		when(step2.getSnapshots()).thenReturn(new ArrayList<>());

		List<TestStep> testSteps = Arrays.asList(step1, stepNoDescription, step2);
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().addAll(testSteps);

		mockedSquashTestStep.when(() -> io.github.bhecquet.entities.TestStep.create(eq(1), anyMap())).thenReturn(newSquashTestStep);
		when(newSquashTestStep.getId()).thenReturn(200);

		squash.updateTestCase(testResult);

		// verify test case was retrieved and completed
		mockedTestCase.verify(() -> TestCase.get(1));
		verify(testCase).completeDetails();

		// verify description updated
		verify(testCase).update(eq(1), anyMap());

		// verify old steps deleted
		mockedSquashTestStep.verify(() -> io.github.bhecquet.entities.TestStep.delete("100,101"));

		// verify new steps created
		mockedSquashTestStep.verify(() -> io.github.bhecquet.entities.TestStep.create(eq(1), eq(Map.of("action", "aa - Step 1 action", "expected_result", "Step 1 expected"))), times(1));
		mockedSquashTestStep.verify(() -> io.github.bhecquet.entities.TestStep.create(eq(1), eq(Map.of("action", "cc - Step 2 action", "expected_result", ""))), times(1));
		mockedSquashTestStep.verify(() -> io.github.bhecquet.entities.TestStep.create(eq(1), anyMap()), times(2)); // only 2 steps recorded because step without description should not be recorded
	}

	/**
	 * Check that updateTestCase deletes old steps even when there's only one
	 */
	@Test(groups={"ut"})
	public void testUpdateTestCaseDeletesSingleOldStep() {

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {testIdAttr, updateTestManagerAttr});
		when(testMethod.getDescription()).thenReturn("Test description");

		when(testCase.getId()).thenReturn(1);
		when(squashTestStep1.getId()).thenReturn(100);
		when(testCase.getTestSteps()).thenReturn(Arrays.asList(squashTestStep1));

		// no new steps
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().clear();

		squash.updateTestCase(testResult);

		mockedSquashTestStep.verify(() -> io.github.bhecquet.entities.TestStep.delete("100"));
	}

	/**
	 * Check that updateTestCase works when there are no old steps to delete
	 */
	@Test(groups={"ut"})
	public void testUpdateTestCaseNoOldSteps() {

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {testIdAttr, updateTestManagerAttr});
		when(testMethod.getDescription()).thenReturn("Test description");

		when(testCase.getId()).thenReturn(1);
		when(testCase.getTestSteps()).thenReturn(new ArrayList<>());

		TestStep step1 = mock(TestStep.class);
		when(step1.getDescription()).thenReturn("Step action");
		when(step1.getExpectedResult()).thenReturn("Expected");
		when(step1.getSnapshots()).thenReturn(new ArrayList<>());

		SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().add(step1);

		mockedSquashTestStep.when(() -> io.github.bhecquet.entities.TestStep.create(eq(1), anyMap())).thenReturn(newSquashTestStep);
		when(newSquashTestStep.getId()).thenReturn(200);

		squash.updateTestCase(testResult);

        // delete should NOT be called when there are no old steps
        mockedSquashTestStep.verify(() -> io.github.bhecquet.entities.TestStep.delete(anyString()), never());
		// new step created
		mockedSquashTestStep.verify(() -> io.github.bhecquet.entities.TestStep.create(eq(1), anyMap()));
	}

	/**
	 * Check that updateTestCase uploads attachments for snapshots with NONE check type
	 */
	@Test(groups={"ut"})
	public void testUpdateTestCaseWithSnapshot() {
		checkAddSnapshot(SnapshotCheckType.NONE, 1);
	}

	private void checkAddSnapshot(SnapshotCheckType checkType, int timesCalled) {
		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {testIdAttr, updateTestManagerAttr});
		when(testMethod.getDescription()).thenReturn("Test description");

		when(testCase.getId()).thenReturn(1);
		when(testCase.getTestSteps()).thenReturn(new ArrayList<>());

		// setup step with snapshot
		TestStep step1 = mock(TestStep.class);
		when(step1.getDescription()).thenReturn("Step action");
		when(step1.getExpectedResult()).thenReturn("Expected");

		Snapshot snapshot = mock(Snapshot.class);
		ScreenShot screenShot = mock(ScreenShot.class);
		when(snapshot.getCheckSnapshot()).thenReturn(checkType);
		when(snapshot.getScreenshot()).thenReturn(screenShot);
		when(screenShot.getImagePath()).thenReturn("path/to/image.png");
		when(step1.getSnapshots()).thenReturn(List.of(snapshot));

		SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().add(step1);

		mockedSquashTestStep.when(() -> io.github.bhecquet.entities.TestStep.create(eq(1), anyMap())).thenReturn(newSquashTestStep);
		when(newSquashTestStep.getId()).thenReturn(200);

		squash.updateTestCase(testResult);

		// verify attachment uploaded
		verify(newSquashTestStep, times(timesCalled)).uploadAttachment(any(File.class), eq(200));
	}

	/**
	 * Check that updateTestCase does NOT upload attachments for snapshots with full check type
	 */
	@Test(groups={"ut"})
	public void testUpdateTestCaseWithSnapshotFullCheckType() {
		checkAddSnapshot(SnapshotCheckType.FULL, 1);
	}

	/**
	 * Check that updateTestCase does NOT upload attachments for snapshots with reference only type
	 */
	@Test(groups={"ut"})
	public void testUpdateTestCaseWithSnapshotReferenceOnlyType() {
		checkAddSnapshot(SnapshotCheckType.REFERENCE_ONLY, 0);
	}

	/**
	 * Check that no exception is thrown when error occurs during updateTestCase
	 */
	@Test(groups={"ut"})
	public void testUpdateTestCaseNoExceptionOnError() {

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {testIdAttr, updateTestManagerAttr});

		mockedTestCase.when(() -> TestCase.get(1)).thenThrow(new SquashTmException("error"));

		// should not throw
		squash.updateTestCase(testResult);
	}
}
