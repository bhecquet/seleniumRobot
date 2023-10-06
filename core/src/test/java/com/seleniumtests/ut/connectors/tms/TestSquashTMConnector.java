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

import org.json.JSONObject;
import org.mockito.Mock;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.CustomAttribute;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.tms.squash.SquashTMApi;
import com.seleniumtests.connectors.tms.squash.SquashTMConnector;
import com.seleniumtests.connectors.tms.squash.entities.Campaign;
import com.seleniumtests.connectors.tms.squash.entities.Iteration;
import com.seleniumtests.connectors.tms.squash.entities.IterationTestPlanItem;
import com.seleniumtests.connectors.tms.squash.entities.TestPlanItemExecution.ExecutionStatus;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;

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
	private IterationTestPlanItem iterationTestPlanItem;
	
	@AfterMethod(groups={"ut"})
	public void reset() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().clear();
	}

	
	@Test(groups={"ut"})
	public void testInitWithAllParameters() {
		JSONObject connect = new JSONObject();
		connect.put(SquashTMConnector.TMS_SERVER_URL, "http://myServer");
		connect.put(SquashTMConnector.TMS_PROJECT, "project");
		connect.put(SquashTMConnector.TMS_USER, "user");
		connect.put(SquashTMConnector.TMS_PASSWORD, "password");
		
		SquashTMConnector squash = new SquashTMConnector();
		squash.init(connect);
		Assert.assertTrue(squash.getInitialized());
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testInitWithoutUrlParameter() {
		JSONObject connect = new JSONObject();
		connect.put(SquashTMConnector.TMS_PROJECT, "project");
		connect.put(SquashTMConnector.TMS_USER, "user");
		connect.put(SquashTMConnector.TMS_PASSWORD, "password");
		
		SquashTMConnector squash = new SquashTMConnector();
		squash.init(connect);
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testInitWithoutProjectParameter() {
		JSONObject connect = new JSONObject();
		connect.put(SquashTMConnector.TMS_SERVER_URL, "http://myServer");
		connect.put(SquashTMConnector.TMS_USER, "user");
		connect.put(SquashTMConnector.TMS_PASSWORD, "password");
		
		SquashTMConnector squash = new SquashTMConnector();
		squash.init(connect);
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testInitWithoutUserParameter() {
		JSONObject connect = new JSONObject();
		connect.put(SquashTMConnector.TMS_SERVER_URL, "http://myServer");
		connect.put(SquashTMConnector.TMS_PROJECT, "project");
		connect.put(SquashTMConnector.TMS_PASSWORD, "password");
		
		SquashTMConnector squash = new SquashTMConnector();
		squash.init(connect);
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testInitWithoutPasswordParameter() {
		JSONObject connect = new JSONObject();
		connect.put(SquashTMConnector.TMS_SERVER_URL, "http://myServer");
		connect.put(SquashTMConnector.TMS_PROJECT, "project");
		connect.put(SquashTMConnector.TMS_USER, "user");
		
		SquashTMConnector squash = new SquashTMConnector();
		squash.init(connect);
	}

	@Test(groups={"ut"})
	public void testRecordResultTestInSuccess(ITestContext testContext) {

		JSONObject connect = new JSONObject();
		connect.put(SquashTMConnector.TMS_SERVER_URL, "http://myServer");
		connect.put(SquashTMConnector.TMS_PROJECT, "project");
		connect.put(SquashTMConnector.TMS_USER, "user");
		connect.put(SquashTMConnector.TMS_PASSWORD, "password");

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();
		
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
		// customize test result so that it has attributes
		when(testResult.getMethod()).thenReturn(testMethod);
		when(testResult.isSuccess()).thenReturn(true);
		when(testResult.getName()).thenReturn("MyTest");
		when(testResult.getTestContext()).thenReturn(testContext);
		when(testResult.getParameters()).thenReturn(new Object[] {});
		when(testResult.getAttribute("testContext")).thenReturn(SeleniumTestsContextManager.getThreadContext());
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {testIdAttr});
		when(api.createCampaign(anyString(), anyString())).thenReturn(campaign);
		when(api.createIteration(any(Campaign.class), anyString())).thenReturn(iteration);
		when(api.addTestCaseInIteration(iteration, 1)).thenReturn(iterationTestPlanItem);
		
		squash.recordResult(testResult);
		
		// check we call all necessary API methods to record the result
		verify(api).createCampaign("Selenium " + testContext.getName(), "");
		verify(api).createIteration(campaign, SeleniumTestsContextManager.getThreadContext().getApplicationVersion());
		verify(api).addTestCaseInIteration(iteration, 1);
		verify(api).setExecutionResult(iterationTestPlanItem, ExecutionStatus.SUCCESS);
	}

	/**
	 * Check result is not recorded when testId is not available
	 * @param testContext
	 */
	@Test(groups={"ut"})
	public void testRecordResultTestInSuccessNoId(ITestContext testContext) {

		JSONObject connect = new JSONObject();
		connect.put(SquashTMConnector.TMS_SERVER_URL, "http://myServer");
		connect.put(SquashTMConnector.TMS_PROJECT, "project");
		connect.put(SquashTMConnector.TMS_USER, "user");
		connect.put(SquashTMConnector.TMS_PASSWORD, "password");

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		// customize test result so that it has attributes
		when(testResult.getMethod()).thenReturn(testMethod);
		when(testMethod.getMethodName()).thenReturn("myTestMethod");
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {});
		when(testResult.isSuccess()).thenReturn(true);
		when(testResult.getName()).thenReturn("MyTest");
		when(testResult.getTestContext()).thenReturn(testContext);
		when(testResult.getParameters()).thenReturn(new Object[] {});
		when(testResult.getAttribute("testContext")).thenReturn(SeleniumTestsContextManager.getThreadContext());
		when(api.createCampaign(anyString(), anyString())).thenReturn(campaign);
		when(api.createIteration(any(Campaign.class), anyString())).thenReturn(iteration);
		when(api.addTestCaseInIteration(iteration, 1)).thenReturn(iterationTestPlanItem);

		squash.recordResult(testResult);

		// check we call all necessary API methods to record the result
		verify(api, never()).createCampaign("Selenium " + testContext.getName(), "");
		verify(api, never()).createIteration(campaign, SeleniumTestsContextManager.getThreadContext().getApplicationVersion());
		verify(api, never()).addTestCaseInIteration(iteration, 1);
		verify(api, never()).setExecutionResult(iterationTestPlanItem, ExecutionStatus.SUCCESS);
	}

	/**
	 * Check cache is used when 2 tests records on the same campaign
	 * @param testContext
	 */
	@Test(groups={"ut"})
	public void testRecordResultTestInSuccessOnSameCampaign(ITestContext testContext) {

		JSONObject connect = new JSONObject();
		connect.put(SquashTMConnector.TMS_SERVER_URL, "http://myServer");
		connect.put(SquashTMConnector.TMS_PROJECT, "project");
		connect.put(SquashTMConnector.TMS_USER, "user");
		connect.put(SquashTMConnector.TMS_PASSWORD, "password");

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

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
		// customize test result so that it has attributes
		when(testResult.getMethod()).thenReturn(testMethod);
		when(testResult.isSuccess()).thenReturn(true);
		when(testResult.getName()).thenReturn("MyTest");
		when(testResult.getTestContext()).thenReturn(testContext);
		when(testResult.getParameters()).thenReturn(new Object[] {});
		when(testResult.getAttribute("testContext")).thenReturn(SeleniumTestsContextManager.getThreadContext());
		when(testResult2.getMethod()).thenReturn(testMethod);
		when(testResult2.isSuccess()).thenReturn(true);
		when(testResult2.getName()).thenReturn("MyTest");
		when(testResult2.getTestContext()).thenReturn(testContext);
		when(testResult2.getParameters()).thenReturn(new Object[] {});
		when(testResult2.getAttribute("testContext")).thenReturn(SeleniumTestsContextManager.getThreadContext());
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {testIdAttr});
		when(api.createCampaign(anyString(), anyString())).thenReturn(campaign);
		when(api.createIteration(any(Campaign.class), anyString())).thenReturn(iteration);
		when(api.addTestCaseInIteration(iteration, 1)).thenReturn(iterationTestPlanItem);

		squash.recordResult(testResult);
		squash.recordResult(testResult2);

		// check we call all necessary API methods to record the result
		verify(api).createCampaign("Selenium " + testContext.getName(), "");
		verify(api).createIteration(campaign, SeleniumTestsContextManager.getThreadContext().getApplicationVersion());
		verify(api, times(2)).addTestCaseInIteration(iteration, 1);
		verify(api, times(2)).setExecutionResult(iterationTestPlanItem, ExecutionStatus.SUCCESS);
	}

	/**
	 * Check that if the testID is not valid, we raise an error
	 * @param testContext
	 */
	@Test(groups={"ut"})
	public void testRecordResultTestWrongTestId(ITestContext testContext) {
		
		JSONObject connect = new JSONObject();
		connect.put(SquashTMConnector.TMS_SERVER_URL, "http://myServer");
		connect.put(SquashTMConnector.TMS_PROJECT, "project");
		connect.put(SquashTMConnector.TMS_USER, "user");
		connect.put(SquashTMConnector.TMS_PASSWORD, "password");
		
		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();
		
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
		// customize test result so that it has attributes
		when(testResult.getMethod()).thenReturn(testMethod);
		when(testResult.isSuccess()).thenReturn(true);
		when(testResult.getName()).thenReturn("MyTest");
		when(testResult.getTestContext()).thenReturn(testContext);
		when(testResult.getParameters()).thenReturn(new Object[] {});
		when(testResult.getAttribute("testContext")).thenReturn(SeleniumTestsContextManager.getThreadContext());
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {testIdAttr});
		when(api.createCampaign(anyString(), anyString())).thenReturn(campaign);
		when(api.createIteration(any(Campaign.class), anyString())).thenReturn(iteration);
		when(api.addTestCaseInIteration(iteration, 1)).thenReturn(iterationTestPlanItem);
		
		squash.recordResult(testResult);
		
		// check we call all necessary API methods to record the result
		verify(api).createCampaign("Selenium " + testContext.getName(), "");
		verify(api).createIteration(campaign, SeleniumTestsContextManager.getThreadContext().getApplicationVersion());
		verify(api).addTestCaseInIteration(iteration, 1);
		verify(api).setExecutionResult(iterationTestPlanItem, ExecutionStatus.SUCCESS);
	}
	
	@Test(groups={"ut"})
	public void testRecordResultTestInError(ITestContext testContext) {
		
		JSONObject connect = new JSONObject();
		connect.put(SquashTMConnector.TMS_SERVER_URL, "http://myServer");
		connect.put(SquashTMConnector.TMS_PROJECT, "project");
		connect.put(SquashTMConnector.TMS_USER, "user");
		connect.put(SquashTMConnector.TMS_PASSWORD, "password");
		
		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();
		
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
		// customize test result so that it has attributes
		when(testResult.getMethod()).thenReturn(testMethod);
		when(testResult.isSuccess()).thenReturn(false);
		when(testResult.getStatus()).thenReturn(2);
		when(testResult.getName()).thenReturn("MyTest");
		when(testResult.getTestContext()).thenReturn(testContext);
		when(testResult.getParameters()).thenReturn(new Object[] {});
		when(testResult.getAttribute("testContext")).thenReturn(SeleniumTestsContextManager.getThreadContext());
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {testIdAttr});
		when(api.createCampaign(anyString(), anyString())).thenReturn(campaign);
		when(api.createIteration(any(Campaign.class), anyString())).thenReturn(iteration);
		when(api.addTestCaseInIteration(iteration, 1)).thenReturn(iterationTestPlanItem);
		
		squash.recordResult(testResult);
		
		// check we call all necessary API methods to record the result
		verify(api).createCampaign("Selenium " + testContext.getName(), "");
		verify(api).createIteration(campaign, SeleniumTestsContextManager.getThreadContext().getApplicationVersion());
		verify(api).addTestCaseInIteration(iteration, 1);
		verify(api).setExecutionResult(iterationTestPlanItem, ExecutionStatus.FAILURE, null);
	}
	
	/**
	 * Same as above, adding a comment to result, from raised exception
	 * @param testContext
	 */
	@Test(groups={"ut"})
	public void testRecordResultTestInErrorWithComment(ITestContext testContext) {
		
		JSONObject connect = new JSONObject();
		connect.put(SquashTMConnector.TMS_SERVER_URL, "http://myServer");
		connect.put(SquashTMConnector.TMS_PROJECT, "project");
		connect.put(SquashTMConnector.TMS_USER, "user");
		connect.put(SquashTMConnector.TMS_PASSWORD, "password");
		
		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();
		
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
		// customize test result so that it has attributes
		when(testResult.getMethod()).thenReturn(testMethod);
		when(testResult.isSuccess()).thenReturn(false);
		when(testResult.getStatus()).thenReturn(2);
		when(testResult.getName()).thenReturn("MyTest");
		when(testResult.getTestContext()).thenReturn(testContext);
		when(testResult.getParameters()).thenReturn(new Object[] {});
		when(testResult.getAttribute("testContext")).thenReturn(SeleniumTestsContextManager.getThreadContext());
		when(testResult.getThrowable()).thenReturn(new WebDriverException("error from driver"));
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {testIdAttr});
		when(api.createCampaign(anyString(), anyString())).thenReturn(campaign);
		when(api.createIteration(any(Campaign.class), anyString())).thenReturn(iteration);
		when(api.addTestCaseInIteration(iteration, 1)).thenReturn(iterationTestPlanItem);
		
		squash.recordResult(testResult);
		
		// check we call all necessary API methods to record the result
		verify(api).createCampaign("Selenium " + testContext.getName(), "");
		verify(api).createIteration(campaign, SeleniumTestsContextManager.getThreadContext().getApplicationVersion());
		verify(api).addTestCaseInIteration(iteration, 1);
		verify(api).setExecutionResult(eq(iterationTestPlanItem), eq(ExecutionStatus.FAILURE), contains("error from driver"));
	}
	
	@Test(groups={"ut"})
	public void testRecordResultTestSkipped(ITestContext testContext) {
		
		JSONObject connect = new JSONObject();
		connect.put(SquashTMConnector.TMS_SERVER_URL, "http://myServer");
		connect.put(SquashTMConnector.TMS_PROJECT, "project");
		connect.put(SquashTMConnector.TMS_USER, "user");
		connect.put(SquashTMConnector.TMS_PASSWORD, "password");
		
		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();
		
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
		// customize test result so that it has attributes
		when(testResult.getMethod()).thenReturn(testMethod);
		when(testResult.isSuccess()).thenReturn(false);
		when(testResult.getStatus()).thenReturn(3);
		when(testResult.getName()).thenReturn("MyTest");
		when(testResult.getTestContext()).thenReturn(testContext);
		when(testResult.getParameters()).thenReturn(new Object[] {});
		when(testResult.getAttribute("testContext")).thenReturn(SeleniumTestsContextManager.getThreadContext());
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {testIdAttr});
		when(api.createCampaign(anyString(), anyString())).thenReturn(campaign);
		when(api.createIteration(any(Campaign.class), anyString())).thenReturn(iteration);
		when(api.addTestCaseInIteration(iteration, 1)).thenReturn(iterationTestPlanItem);
		
		squash.recordResult(testResult);
		
		// check we call all necessary API methods to record the result
		verify(api).createCampaign("Selenium " + testContext.getName(), "");
		verify(api).createIteration(campaign, SeleniumTestsContextManager.getThreadContext().getApplicationVersion());
		verify(api).addTestCaseInIteration(iteration, 1);
		verify(api).setExecutionResult(iterationTestPlanItem, ExecutionStatus.BLOCKED);
	}
	
	/**
	 * Test we do not record result when no testId is provided
	 * @param testContext
	 */
	@Test(groups={"ut"})
	public void testDoNotRecordResult(ITestContext testContext) {
		
		JSONObject connect = new JSONObject();
		connect.put(SquashTMConnector.TMS_SERVER_URL, "http://myServer");
		connect.put(SquashTMConnector.TMS_PROJECT, "project");
		connect.put(SquashTMConnector.TMS_USER, "user");
		connect.put(SquashTMConnector.TMS_PASSWORD, "password");
		
		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

		// customize test result so that it has attributes
		when(testResult.getMethod()).thenReturn(testMethod);
		when(testResult.isSuccess()).thenReturn(true);
		when(testResult.getName()).thenReturn("MyTest");
		when(testResult.getTestContext()).thenReturn(testContext);
		when(testResult.getParameters()).thenReturn(new Object[] {});
		when(testResult.getAttribute("testContext")).thenReturn(SeleniumTestsContextManager.getThreadContext());
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {});
		when(api.createCampaign(anyString(), anyString())).thenReturn(campaign);
		when(api.createIteration(any(Campaign.class), anyString())).thenReturn(iteration);
		when(api.addTestCaseInIteration(iteration, 1)).thenReturn(iterationTestPlanItem);
		
		squash.recordResult(testResult);
		
		// check we do not call API as testId is not provided
		verify(api, never()).createCampaign("Selenium " + testContext.getName(), "");
		verify(api, never()).createIteration(campaign, SeleniumTestsContextManager.getThreadContext().getApplicationVersion());
		verify(api, never()).addTestCaseInIteration(iteration, 1);
		verify(api, never()).setExecutionResult(iterationTestPlanItem, ExecutionStatus.SUCCESS);
	}
	
	/**
	 * Check that if any error occurs during result recording, it does not raise any exception, only message will be displayed
	 * @param testContext
	 */
	@Test(groups={"ut"})
	public void testNoExceptionWhenErrorInRecording(ITestContext testContext) {
		
		JSONObject connect = new JSONObject();
		connect.put(SquashTMConnector.TMS_SERVER_URL, "http://myServer");
		connect.put(SquashTMConnector.TMS_PROJECT, "project");
		connect.put(SquashTMConnector.TMS_USER, "user");
		connect.put(SquashTMConnector.TMS_PASSWORD, "password");
		
		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();

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
		
		// customize test result so that it has attributes
		when(testResult.getMethod()).thenReturn(testMethod);
		when(testResult.isSuccess()).thenReturn(true);
		when(testResult.getName()).thenReturn("MyTest");
		when(testResult.getTestContext()).thenReturn(testContext);
		when(testResult.getParameters()).thenReturn(new Object[] {});
		when(testResult.getAttribute("testContext")).thenReturn(SeleniumTestsContextManager.getThreadContext());
		when(testMethod.getAttributes()).thenReturn(new CustomAttribute[] {testIdAttr});
		when(api.createCampaign(anyString(), anyString())).thenThrow(new ScenarioException("Something went wrong"));
		when(api.createIteration(any(Campaign.class), anyString())).thenReturn(iteration);
		when(api.addTestCaseInIteration(iteration, 1)).thenReturn(iterationTestPlanItem);
		
		squash.recordResult(testResult);
		
		// check we do not call API as testId is not provided
		verify(api).createCampaign("Selenium " + testContext.getName(), "");
		verify(api, never()).createIteration(campaign, SeleniumTestsContextManager.getThreadContext().getApplicationVersion());
		verify(api, never()).addTestCaseInIteration(iteration, 1);
		verify(api, never()).setExecutionResult(iterationTestPlanItem, ExecutionStatus.SUCCESS);
	}
	

	/**
	 * With a custom campaign name, check it's created if it does not exist
	 * @param testContext
	 */
	@Test(groups={"ut"})
	public void testRecordResultTestInSuccessCustomCampaign(ITestContext testContext) {

		JSONObject connect = new JSONObject();
		connect.put(SquashTMConnector.TMS_SERVER_URL, "http://myServer");
		connect.put(SquashTMConnector.TMS_PROJECT, "project");
		connect.put(SquashTMConnector.TMS_USER, "user");
		connect.put(SquashTMConnector.TMS_PASSWORD, "password");

		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();
		

		// customize test result so that it has attributes
		when(testResult.getMethod()).thenReturn(testMethod);
		when(testResult.isSuccess()).thenReturn(true);
		when(testResult.getName()).thenReturn("MyTest");
		when(testResult.getTestContext()).thenReturn(testContext);
		when(testResult.getParameters()).thenReturn(new Object[] {});
		when(testResult.getAttribute("testContext")).thenReturn(SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().testManager().setTestId(23);
		SeleniumTestsContextManager.getThreadContext().setTestManagerInstance(squash);
		SeleniumTestsContextManager.getThreadContext().testManager().setCampaignName("My Campaign");
		when(api.createCampaign(anyString(), anyString())).thenReturn(campaign);
		when(api.createIteration(any(Campaign.class), anyString())).thenReturn(iteration);
		when(api.addTestCaseInIteration(iteration, 23)).thenReturn(iterationTestPlanItem);
		
		squash.recordResult(testResult);
		
		// check we call all necessary API methods to record the result
		verify(api).createCampaign("My Campaign", "");
		verify(api).createIteration(campaign, SeleniumTestsContextManager.getThreadContext().getApplicationVersion());
		verify(api).addTestCaseInIteration(iteration, 23);
		verify(api).setExecutionResult(iterationTestPlanItem, ExecutionStatus.SUCCESS);
	}
	
	/**
	 * With a custom iteration name, check it's created if it does not exist
	 * @param testContext
	 */
	@Test(groups={"ut"})
	public void testRecordResultTestInSuccessCustomIteration(ITestContext testContext) {
		
		JSONObject connect = new JSONObject();
		connect.put(SquashTMConnector.TMS_SERVER_URL, "http://myServer");
		connect.put(SquashTMConnector.TMS_PROJECT, "project");
		connect.put(SquashTMConnector.TMS_USER, "user");
		connect.put(SquashTMConnector.TMS_PASSWORD, "password");
		
		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();
		
		
		// customize test result so that it has attributes
		when(testResult.getMethod()).thenReturn(testMethod);
		when(testResult.isSuccess()).thenReturn(true);
		when(testResult.getName()).thenReturn("MyTest");
		when(testResult.getTestContext()).thenReturn(testContext);
		when(testResult.getParameters()).thenReturn(new Object[] {});
		when(testResult.getAttribute("testContext")).thenReturn(SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().testManager().setTestId(23);
		SeleniumTestsContextManager.getThreadContext().setTestManagerInstance(squash);
		SeleniumTestsContextManager.getThreadContext().testManager().setIterationName("My Iteration");
		when(api.createCampaign(anyString(), anyString())).thenReturn(campaign);
		when(api.createIteration(any(Campaign.class), anyString())).thenReturn(iteration);
		when(api.addTestCaseInIteration(iteration, 23)).thenReturn(iterationTestPlanItem);
		
		squash.recordResult(testResult);
		
		// check we call all necessary API methods to record the result
		verify(api).createCampaign("Selenium " + testContext.getName(), "");
		verify(api).createIteration(campaign, "My Iteration");
		verify(api).addTestCaseInIteration(iteration, 23);
		verify(api).setExecutionResult(iterationTestPlanItem, ExecutionStatus.SUCCESS);
	}
	/**
	 * With a custom iteration name, check it's created if it does not exist
	 * @param testContext
	 */
	@Test(groups={"ut"})
	public void testRecordResultTestInSuccessCustomCampaignFolder(ITestContext testContext) {
		
		JSONObject connect = new JSONObject();
		connect.put(SquashTMConnector.TMS_SERVER_URL, "http://myServer");
		connect.put(SquashTMConnector.TMS_PROJECT, "project");
		connect.put(SquashTMConnector.TMS_USER, "user");
		connect.put(SquashTMConnector.TMS_PASSWORD, "password");
		
		SquashTMConnector squash = spy(new SquashTMConnector());
		squash.init(connect);
		doReturn(api).when(squash).getApi();
		
		
		// customize test result so that it has attributes
		when(testResult.getMethod()).thenReturn(testMethod);
		when(testResult.isSuccess()).thenReturn(true);
		when(testResult.getName()).thenReturn("MyTest");
		when(testResult.getTestContext()).thenReturn(testContext);
		when(testResult.getParameters()).thenReturn(new Object[] {});
		when(testResult.getAttribute("testContext")).thenReturn(SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().testManager().setTestId(23);
		SeleniumTestsContextManager.getThreadContext().setTestManagerInstance(squash);
		SeleniumTestsContextManager.getThreadContext().testManager().setCampaignFolderPath("folder1/folder2");
		when(api.createCampaign(anyString(), anyString())).thenReturn(campaign);
		when(api.createIteration(any(Campaign.class), anyString())).thenReturn(iteration);
		when(api.addTestCaseInIteration(iteration, 23)).thenReturn(iterationTestPlanItem);
		
		squash.recordResult(testResult);
		
		// check we call all necessary API methods to record the result
		verify(api).createCampaign("Selenium " + testContext.getName(), "folder1/folder2");
		verify(api).addTestCaseInIteration(iteration, 23);
		verify(api).setExecutionResult(iterationTestPlanItem, ExecutionStatus.SUCCESS);
	}
}
