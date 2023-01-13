package com.seleniumtests.ut.core.context;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;

public class TestBugTrackerContext extends GenericTest {


	@Test(groups="ut context")
	public void testBugtrackerType(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().bugtracker().setBugtrackerType("squash");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().bugtracker().getBugtrackerType(), "squash");
	}
	@Test(groups="ut context")
	public void testBugtrackerTypeNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().bugtracker().setBugtrackerType(null);
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().bugtracker().getBugtrackerType());
	}
	@Test(groups="ut context")
	public void testBugtrackerUrl(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().bugtracker().setBugtrackerUrl("http://foo.bar");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().bugtracker().getBugtrackerUrl(), "http://foo.bar");
	}
	@Test(groups="ut context")
	public void testBugtrackerUrlFromVariable(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("bugtrackerUrl", new TestVariable("bugtrackerUrl", "http://foo.bar2"));
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().bugtracker().getBugtrackerUrl(), "http://foo.bar2");
	}
	@Test(groups="ut context")
	public void testBugtrackerUrlNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().bugtracker().setBugtrackerUrl(null);
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().bugtracker().getBugtrackerUrl());
	}
	@Test(groups="ut context")
	public void testBugtrackerUser(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().bugtracker().setBugtrackerUser("user");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().bugtracker().getBugtrackerUser(), "user");
	}
	@Test(groups="ut context")
	public void testBugtrackerUserNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().bugtracker().setBugtrackerUser(null);
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().bugtracker().getBugtrackerUser());
	}
	@Test(groups="ut context")
	public void testBugtrackerUserFromVariable(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("bugtrackerUser", new TestVariable("bugtrackerUser", "user"));
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().bugtracker().getBugtrackerUser(), "user");
	}
	@Test(groups="ut context")
	public void testBugtrackerPassword(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().bugtracker().setBugtrackerPassword("pwd");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().bugtracker().getBugtrackerPassword(), "pwd");
	}
	@Test(groups="ut context")
	public void testBugtrackerPasswordNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().bugtracker().setBugtrackerPassword(null);
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().bugtracker().getBugtrackerPassword());
	}
	@Test(groups="ut context")
	public void testBugtrackerPasswordFromVariable(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("bugtrackerPassword", new TestVariable("bugtrackerPassword", "pwd"));
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().bugtracker().getBugtrackerPassword(), "pwd");
	}
	@Test(groups="ut context")
	public void testBugtrackerProject(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().bugtracker().setBugtrackerProject("project");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().bugtracker().getBugtrackerProject(), "project");
	}
	@Test(groups="ut context")
	public void testBugtrackerProjectNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().bugtracker().setBugtrackerProject(null);
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().bugtracker().getBugtrackerProject());
	}
	@Test(groups="ut context")
	public void testBugtrackerProjectFromVariable(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("bugtrackerProject", new TestVariable("bugtrackerProject", "project"));
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().bugtracker().getBugtrackerProject(), "project");
	}
	
}
