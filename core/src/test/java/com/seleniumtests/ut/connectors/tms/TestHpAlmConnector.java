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

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.tms.hpalm.HpAlmConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;

public class TestHpAlmConnector extends GenericTest {
	
	@AfterMethod(groups={"ut"})
	public void reset() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().clear();
	}

	@Test(groups={"ut"})
	public void checkConfiguration() {
		String config = "{'type': 'hp', 'tmsRun': '3'}";
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
		JSONObject connect = new JSONObject();
		connect.put(HpAlmConnector.HP_ALM_SERVER_URL, "http://myServer");
		connect.put(HpAlmConnector.HP_ALM_DOMAIN, "domain");
		connect.put(HpAlmConnector.HP_ALM_PROJECT, "project");
		connect.put(HpAlmConnector.HP_ALM_USER, "user");
		connect.put(HpAlmConnector.HP_ALM_PASSWORD, "password");
		
		String config = "{'type': 'hp', 'tmsRun': '3'}";
		HpAlmConnector hp = new HpAlmConnector(new JSONObject(config));
		hp.init(connect);
		Assert.assertTrue(hp.getInitialized());
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testInitWithoutUrlParameter() {
		JSONObject connect = new JSONObject();
		connect.put(HpAlmConnector.HP_ALM_DOMAIN, "domain");
		connect.put(HpAlmConnector.HP_ALM_PROJECT, "project");
		connect.put(HpAlmConnector.HP_ALM_USER, "user");
		connect.put(HpAlmConnector.HP_ALM_PASSWORD, "password");
		
		String config = "{'type': 'hp', 'tmsRun': '3'}";
		HpAlmConnector hp = new HpAlmConnector(new JSONObject(config));
		hp.init(connect);
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testInitWithoutDomainParameter() {
		JSONObject connect = new JSONObject();
		connect.put(HpAlmConnector.HP_ALM_SERVER_URL, "http://myServer");
		connect.put(HpAlmConnector.HP_ALM_PROJECT, "project");
		connect.put(HpAlmConnector.HP_ALM_USER, "user");
		connect.put(HpAlmConnector.HP_ALM_PASSWORD, "password");
		
		String config = "{'type': 'hp', 'tmsRun': '3'}";
		HpAlmConnector hp = new HpAlmConnector(new JSONObject(config));
		hp.init(connect);
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testInitWithoutProjectParameter() {
		JSONObject connect = new JSONObject();
		connect.put(HpAlmConnector.HP_ALM_SERVER_URL, "http://myServer");
		connect.put(HpAlmConnector.HP_ALM_DOMAIN, "domain");
		connect.put(HpAlmConnector.HP_ALM_USER, "user");
		connect.put(HpAlmConnector.HP_ALM_PASSWORD, "password");
		
		String config = "{'type': 'hp', 'tmsRun': '3'}";
		HpAlmConnector hp = new HpAlmConnector(new JSONObject(config));
		hp.init(connect);
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testInitWithoutUserParameter() {
		JSONObject connect = new JSONObject();
		connect.put(HpAlmConnector.HP_ALM_SERVER_URL, "http://myServer");
		connect.put(HpAlmConnector.HP_ALM_DOMAIN, "domain");
		connect.put(HpAlmConnector.HP_ALM_PROJECT, "project");
		connect.put(HpAlmConnector.HP_ALM_PASSWORD, "password");
		
		String config = "{'type': 'hp', 'tmsRun': '3'}";
		HpAlmConnector hp = new HpAlmConnector(new JSONObject(config));
		hp.init(connect);
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testInitWithoutPasswordParameter() {
		JSONObject connect = new JSONObject();
		connect.put(HpAlmConnector.HP_ALM_SERVER_URL, "http://myServer");
		connect.put(HpAlmConnector.HP_ALM_DOMAIN, "domain");
		connect.put(HpAlmConnector.HP_ALM_PROJECT, "project");
		connect.put(HpAlmConnector.HP_ALM_USER, "user");
		
		String config = "{'type': 'hp', 'tmsRun': '3'}";
		HpAlmConnector hp = new HpAlmConnector(new JSONObject(config));
		hp.init(connect);
	}
}
