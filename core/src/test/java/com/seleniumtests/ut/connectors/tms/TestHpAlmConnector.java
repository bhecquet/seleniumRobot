package com.seleniumtests.ut.connectors.tms;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.tms.hpalm.HpAlmConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.customexception.ConfigurationException;

public class TestHpAlmConnector extends GenericTest {
	
	@AfterMethod(groups={"ut"})
	public void reset() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().clear();
	}

	@Test(groups={"ut"})
	public void checkConfiguration() {
		String config = "{'type': 'hp', 'run': '3'}";
		HpAlmConnector hp = new HpAlmConnector(new JSONObject(config));
		Assert.assertEquals(hp.getCurrentRunId(), "3");
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void checkErrorWhenNoRunId() {
		String config = "{'type': 'hp'}";
		new HpAlmConnector(new JSONObject(config));
	}
	
	@Test(groups={"ut"})
	public void testInitWithAllParameters() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_SERVER_URL, new TestVariable(HpAlmConnector.HP_ALM_SERVER_URL, "http://myServer"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_DOMAIN, new TestVariable(HpAlmConnector.HP_ALM_DOMAIN, "domain"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_PROJECT, new TestVariable(HpAlmConnector.HP_ALM_PROJECT, "project"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_USER, new TestVariable(HpAlmConnector.HP_ALM_USER, "user"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_PASSWORD, new TestVariable(HpAlmConnector.HP_ALM_PASSWORD, "password"));
		
		String config = "{'type': 'hp', 'run': '3'}";
		HpAlmConnector hp = new HpAlmConnector(new JSONObject(config));
		hp.init();
		Assert.assertTrue(hp.getInitialized());
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testInitWithoutUrlParameter() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_DOMAIN, new TestVariable(HpAlmConnector.HP_ALM_DOMAIN, "domain"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_PROJECT, new TestVariable(HpAlmConnector.HP_ALM_PROJECT, "project"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_USER, new TestVariable(HpAlmConnector.HP_ALM_USER, "user"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_PASSWORD, new TestVariable(HpAlmConnector.HP_ALM_PASSWORD, "password"));
		
		String config = "{'type': 'hp', 'run': '3'}";
		HpAlmConnector hp = new HpAlmConnector(new JSONObject(config));
		hp.init();
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testInitWithoutDomainParameter() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_SERVER_URL, new TestVariable(HpAlmConnector.HP_ALM_SERVER_URL, "http://myServer"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_PROJECT, new TestVariable(HpAlmConnector.HP_ALM_PROJECT, "project"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_USER, new TestVariable(HpAlmConnector.HP_ALM_USER, "user"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_PASSWORD, new TestVariable(HpAlmConnector.HP_ALM_PASSWORD, "password"));
		
		String config = "{'type': 'hp', 'run': '3'}";
		HpAlmConnector hp = new HpAlmConnector(new JSONObject(config));
		hp.init();
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testInitWithoutProjectParameter() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_SERVER_URL, new TestVariable(HpAlmConnector.HP_ALM_SERVER_URL, "http://myServer"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_DOMAIN, new TestVariable(HpAlmConnector.HP_ALM_DOMAIN, "domain"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_USER, new TestVariable(HpAlmConnector.HP_ALM_USER, "user"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_PASSWORD, new TestVariable(HpAlmConnector.HP_ALM_PASSWORD, "password"));
		
		String config = "{'type': 'hp', 'run': '3'}";
		HpAlmConnector hp = new HpAlmConnector(new JSONObject(config));
		hp.init();
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testInitWithoutUserParameter() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_SERVER_URL, new TestVariable(HpAlmConnector.HP_ALM_SERVER_URL, "http://myServer"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_DOMAIN, new TestVariable(HpAlmConnector.HP_ALM_DOMAIN, "domain"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_PROJECT, new TestVariable(HpAlmConnector.HP_ALM_PROJECT, "project"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_PASSWORD, new TestVariable(HpAlmConnector.HP_ALM_PASSWORD, "password"));
		
		String config = "{'type': 'hp', 'run': '3'}";
		HpAlmConnector hp = new HpAlmConnector(new JSONObject(config));
		hp.init();
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testInitWithoutPasswordParameter() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_SERVER_URL, new TestVariable(HpAlmConnector.HP_ALM_SERVER_URL, "http://myServer"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_DOMAIN, new TestVariable(HpAlmConnector.HP_ALM_DOMAIN, "domain"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_PROJECT, new TestVariable(HpAlmConnector.HP_ALM_PROJECT, "project"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(HpAlmConnector.HP_ALM_USER, new TestVariable(HpAlmConnector.HP_ALM_USER, "user"));
		
		String config = "{'type': 'hp', 'run': '3'}";
		HpAlmConnector hp = new HpAlmConnector(new JSONObject(config));
		hp.init();
	}
}
