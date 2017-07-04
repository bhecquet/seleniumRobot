package com.seleniumtests.ut.connectors.tms;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.tms.TestManager;
import com.seleniumtests.connectors.tms.hpalm.HpAlmConnector;
import com.seleniumtests.customexception.ConfigurationException;


public class TestTestManager extends GenericTest {

	@Test(groups={"ut"})
	public void testTmsSelectionHpAlm() {
		String config = "{'type': 'hp', 'run': '3'}";
		TestManager manager = TestManager.getInstance(new JSONObject(config));
		Assert.assertTrue(manager instanceof HpAlmConnector);
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testTmsSelectionWrongType() {
		String config = "{'type': 'spira', 'run': '3'}";
		TestManager.getInstance(new JSONObject(config));
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testTmsSelectionNoType() {
		String config = "{'run': '3'}";
		TestManager.getInstance(new JSONObject(config));
	}
}
