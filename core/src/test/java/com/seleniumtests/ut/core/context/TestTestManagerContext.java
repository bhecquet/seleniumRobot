package com.seleniumtests.ut.core.context;

import static org.mockito.ArgumentMatchers.argThat;

import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.tms.TestManager;
import com.seleniumtests.connectors.tms.squash.SquashTMConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.contexts.TestManagerContext;

@PrepareForTest({ TestManager.class, SeleniumTestsContext.class })
public class TestTestManagerContext extends ConnectorsTest {


	@Mock
	private TestManager testManager;
	
	@Mock
	private SquashTMConnector squashTmConnector;
	
	@Test(groups="ut context")
	public void testTmsType(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().testManager().setTmsType("squash");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().testManager().getTmsType(), "squash");
	}
	@Test(groups="ut context")
	public void testTmsTypeNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().testManager().setTmsType(null);
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().testManager().getTmsType());
	}
	@Test(groups="ut context")
	public void testTmsUrl(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().testManager().setTmsUrl("http://foo.bar");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().testManager().getTmsUrl(), "http://foo.bar");
	}
	@Test(groups="ut context")
	public void testTmsUrlNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().testManager().setTmsUrl(null);
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().testManager().getTmsUrl());
	}
	@Test(groups="ut context")
	public void testTmsUser(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().testManager().setTmsUser("user");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().testManager().getTmsUser(), "user");
	}
	@Test(groups="ut context")
	public void testTmsUserNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().testManager().setTmsUser(null);
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().testManager().getTmsUser());
	}
	@Test(groups="ut context")
	public void testTmsPassword(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().testManager().setTmsPassword("pwd");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().testManager().getTmsPassword(), "pwd");
	}
	@Test(groups="ut context")
	public void testTmsPasswordNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().testManager().setTmsPassword(null);
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().testManager().getTmsPassword());
	}
	@Test(groups="ut context")
	public void testTmsProject(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().testManager().setTmsProject("project");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().testManager().getTmsProject(), "project");
	}
	@Test(groups="ut context")
	public void testTmsProjectNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().testManager().setTmsProject(null);
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().testManager().getTmsProject());
	}
	@Test(groups="ut context")
	public void testTmsTestId(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().testManager().setTestId(1);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getConfiguration().get(TestManager.TMS_TEST_ID).getValue(), "1");
	}
	@Test(groups="ut", expectedExceptions = UnsupportedOperationException.class)
	public void testTmsSquashCampaignNullTms(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().testManager().setCampaignName("campaign");
	}
	@Test(groups="ut")
	public void testTmsSquashCampaign(final ITestContext testNGCtx, final XmlTest xmlTest) throws NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		try {
			System.setProperty(TestManagerContext.TMS_TYPE, "squash");
			System.setProperty(TestManagerContext.TMS_URL, "http://localhost:1234");
			System.setProperty(TestManagerContext.TMS_USER, "user");
			System.setProperty(TestManagerContext.TMS_PASSWORD, "password");
			System.setProperty(TestManagerContext.TMS_PROJECT, "project");

			PowerMockito.mockStatic(TestManager.class);


			PowerMockito.when(TestManager.getInstance(argThat(config -> config.getString("tmsType").equals("squash")))).thenReturn(squashTmConnector);

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());

			initThreadContext(testNGCtx, "myTest", testResult);
			SeleniumTestsContextManager.getThreadContext().testManager().setCampaignName("campaign");
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().testManager().getCampaignName(), "campaign");
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getConfiguration().get(SquashTMConnector.SQUASH_CAMPAIGN).getValue(), "campaign");

		} finally {
			System.clearProperty(TestManagerContext.TMS_TYPE);
			System.clearProperty(TestManagerContext.TMS_URL);
			System.clearProperty(TestManagerContext.TMS_USER);
			System.clearProperty(TestManagerContext.TMS_PASSWORD);
			System.clearProperty(TestManagerContext.TMS_PROJECT);
		}		
	}
	/**
	 * Test via user parameter
	 */
	@Test(groups="ut")
	public void testTmsSquashCampaign2(final ITestContext testNGCtx, final XmlTest xmlTest) throws NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		try {
			System.setProperty(TestManagerContext.TMS_TYPE, "squash");
			System.setProperty(TestManagerContext.TMS_URL, "http://localhost:1234");
			System.setProperty(TestManagerContext.TMS_USER, "user");
			System.setProperty(TestManagerContext.TMS_PASSWORD, "password");
			System.setProperty(TestManagerContext.TMS_PROJECT, "project");
			System.setProperty(SquashTMConnector.SQUASH_CAMPAIGN, "campaign2");
			
			PowerMockito.mockStatic(TestManager.class);
			
			
			PowerMockito.when(TestManager.getInstance(argThat(config -> config.getString("tmsType").equals("squash")))).thenReturn(squashTmConnector);
			
			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			
			initThreadContext(testNGCtx, "myTest", testResult);
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().testManager().getCampaignName(), "campaign2");
			
		} finally {
			System.clearProperty(TestManagerContext.TMS_TYPE);
			System.clearProperty(TestManagerContext.TMS_URL);
			System.clearProperty(TestManagerContext.TMS_USER);
			System.clearProperty(TestManagerContext.TMS_PASSWORD);
			System.clearProperty(TestManagerContext.TMS_PROJECT);
			System.clearProperty(SquashTMConnector.SQUASH_CAMPAIGN);
		}		
	}
	@Test(groups="ut", expectedExceptions = UnsupportedOperationException.class)
	public void testTmsSquashCampaignFolderNullTms(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().testManager().setCampaignFolderPath("campaign");
	}
	/**
	 * Test via method call
	 */
	@Test(groups="ut")
	public void testTmsSquashCampaignFolderPath(final ITestContext testNGCtx, final XmlTest xmlTest) throws NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		try {
			System.setProperty(TestManagerContext.TMS_TYPE, "squash");
			System.setProperty(TestManagerContext.TMS_URL, "http://localhost:1234");
			System.setProperty(TestManagerContext.TMS_USER, "user");
			System.setProperty(TestManagerContext.TMS_PASSWORD, "password");
			System.setProperty(TestManagerContext.TMS_PROJECT, "project");
			
			PowerMockito.mockStatic(TestManager.class);
			
			
			PowerMockito.when(TestManager.getInstance(argThat(config -> config.getString("tmsType").equals("squash")))).thenReturn(squashTmConnector);
			
			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			
			initThreadContext(testNGCtx, "myTest", testResult);
			SeleniumTestsContextManager.getThreadContext().testManager().setCampaignFolderPath("folder1/folder2");
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().testManager().getCampaignFolderPath(), "folder1/folder2");
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getConfiguration().get(SquashTMConnector.SQUASH_CAMPAIGN_FOLDER).getValue(), "folder1/folder2");
			
		} finally {
			System.clearProperty(TestManagerContext.TMS_TYPE);
			System.clearProperty(TestManagerContext.TMS_URL);
			System.clearProperty(TestManagerContext.TMS_USER);
			System.clearProperty(TestManagerContext.TMS_PASSWORD);
			System.clearProperty(TestManagerContext.TMS_PROJECT);
		}		
	}
	
	/**
	 * Test via user parameter
	 */
	@Test(groups="ut")
	public void testTmsSquashCampaignFolderPath2(final ITestContext testNGCtx, final XmlTest xmlTest) throws NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		try {
			System.setProperty(TestManagerContext.TMS_TYPE, "squash");
			System.setProperty(TestManagerContext.TMS_URL, "http://localhost:1234");
			System.setProperty(TestManagerContext.TMS_USER, "user");
			System.setProperty(TestManagerContext.TMS_PASSWORD, "password");
			System.setProperty(TestManagerContext.TMS_PROJECT, "project");
			System.setProperty(SquashTMConnector.SQUASH_CAMPAIGN_FOLDER, "folder1");
			
			PowerMockito.mockStatic(TestManager.class);
			
			
			PowerMockito.when(TestManager.getInstance(argThat(config -> config.getString("tmsType").equals("squash")))).thenReturn(squashTmConnector);
			
			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			
			initThreadContext(testNGCtx, "myTest", testResult);
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().testManager().getCampaignFolderPath(), "folder1");
			
		} finally {
			System.clearProperty(TestManagerContext.TMS_TYPE);
			System.clearProperty(TestManagerContext.TMS_URL);
			System.clearProperty(TestManagerContext.TMS_USER);
			System.clearProperty(TestManagerContext.TMS_PASSWORD);
			System.clearProperty(TestManagerContext.TMS_PROJECT);
			System.clearProperty(SquashTMConnector.SQUASH_CAMPAIGN_FOLDER);
		}		
	}
	@Test(groups="ut")
	public void testTmsSquashCampaignFolderPathNull(final ITestContext testNGCtx, final XmlTest xmlTest) throws NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		try {
			System.setProperty(TestManagerContext.TMS_TYPE, "squash");
			System.setProperty(TestManagerContext.TMS_URL, "http://localhost:1234");
			System.setProperty(TestManagerContext.TMS_USER, "user");
			System.setProperty(TestManagerContext.TMS_PASSWORD, "password");
			System.setProperty(TestManagerContext.TMS_PROJECT, "project");
			
			PowerMockito.mockStatic(TestManager.class);
			
			
			PowerMockito.when(TestManager.getInstance(argThat(config -> config.getString("tmsType").equals("squash")))).thenReturn(squashTmConnector);
			
			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			
			initThreadContext(testNGCtx, "myTest", testResult);

			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().testManager().getCampaignFolderPath(), "");
			
		} finally {
			System.clearProperty(TestManagerContext.TMS_TYPE);
			System.clearProperty(TestManagerContext.TMS_URL);
			System.clearProperty(TestManagerContext.TMS_USER);
			System.clearProperty(TestManagerContext.TMS_PASSWORD);
			System.clearProperty(TestManagerContext.TMS_PROJECT);
		}		
	}
	@Test(groups="ut", expectedExceptions = UnsupportedOperationException.class)
	public void testTmsSquashIterationNullTms(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().testManager().setIterationName("iteration");
	}
	@Test(groups="ut")
	public void testTmsSquashIteration(final ITestContext testNGCtx, final XmlTest xmlTest) throws NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		try {
			System.setProperty(TestManagerContext.TMS_TYPE, "squash");
			System.setProperty(TestManagerContext.TMS_URL, "http://localhost:1234");
			System.setProperty(TestManagerContext.TMS_USER, "user");
			System.setProperty(TestManagerContext.TMS_PASSWORD, "password");
			System.setProperty(TestManagerContext.TMS_PROJECT, "project");
			
			PowerMockito.mockStatic(TestManager.class);
			
			
			PowerMockito.when(TestManager.getInstance(argThat(config -> config.getString("tmsType").equals("squash")))).thenReturn(squashTmConnector);
			
			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			
			initThreadContext(testNGCtx, "myTest", testResult);
			SeleniumTestsContextManager.getThreadContext().testManager().setIterationName("iteration");
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getConfiguration().get(SquashTMConnector.SQUASH_ITERATION).getValue(), "iteration");
			
		} finally {
			System.clearProperty(TestManagerContext.TMS_TYPE);
			System.clearProperty(TestManagerContext.TMS_URL);
			System.clearProperty(TestManagerContext.TMS_USER);
			System.clearProperty(TestManagerContext.TMS_PASSWORD);
			System.clearProperty(TestManagerContext.TMS_PROJECT);
		}		
	}
	@Test(groups="ut")
	public void testTmsSquashIteration2(final ITestContext testNGCtx, final XmlTest xmlTest) throws NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		try {
			System.setProperty(TestManagerContext.TMS_TYPE, "squash");
			System.setProperty(TestManagerContext.TMS_URL, "http://localhost:1234");
			System.setProperty(TestManagerContext.TMS_USER, "user");
			System.setProperty(TestManagerContext.TMS_PASSWORD, "password");
			System.setProperty(TestManagerContext.TMS_PROJECT, "project");
			System.setProperty(SquashTMConnector.SQUASH_ITERATION, "iteration2");
			
			PowerMockito.mockStatic(TestManager.class);
			
			
			PowerMockito.when(TestManager.getInstance(argThat(config -> config.getString("tmsType").equals("squash")))).thenReturn(squashTmConnector);
			
			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			
			initThreadContext(testNGCtx, "myTest", testResult);
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getConfiguration().get(SquashTMConnector.SQUASH_ITERATION).getValue(), "iteration2");
			
		} finally {
			System.clearProperty(TestManagerContext.TMS_TYPE);
			System.clearProperty(TestManagerContext.TMS_URL);
			System.clearProperty(TestManagerContext.TMS_USER);
			System.clearProperty(TestManagerContext.TMS_PASSWORD);
			System.clearProperty(TestManagerContext.TMS_PROJECT);
			System.clearProperty(SquashTMConnector.SQUASH_ITERATION);
		}		
	}
	

	@Test(groups = "ut")
	public void testInitTestManager(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		try {
			System.setProperty(TestManagerContext.TMS_TYPE, "squash");
			System.setProperty(TestManagerContext.TMS_URL, "http://localhost:1234");
			System.setProperty(TestManagerContext.TMS_USER, "user");
			System.setProperty(TestManagerContext.TMS_PASSWORD, "password");
			System.setProperty(TestManagerContext.TMS_PROJECT, "project");
			System.setProperty("tmsDomain", "domain"); // check that any parameter starting with "tms" will be used for configuration

			PowerMockito.mockStatic(TestManager.class);


			PowerMockito.when(TestManager.getInstance(argThat(config -> config.getString("tmsType").equals("squash") && config.getString("tmsDomain").equals("domain")))).thenReturn(testManager);

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());

			initThreadContext(testNGCtx, "myTest", testResult);

			// check test manager has been created
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestManagerInstance(), testManager);

		} finally {
			System.clearProperty(TestManagerContext.TMS_TYPE);
			System.clearProperty(TestManagerContext.TMS_URL);
			System.clearProperty(TestManagerContext.TMS_USER);
			System.clearProperty(TestManagerContext.TMS_PASSWORD);
			System.clearProperty(TestManagerContext.TMS_PROJECT);
			System.clearProperty("tmsDomain");
		}
	}
}
