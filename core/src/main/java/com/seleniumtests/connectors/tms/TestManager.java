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
package com.seleniumtests.connectors.tms;

import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.ITestResult;

import com.seleniumtests.connectors.tms.hpalm.HpAlmConnector;
import com.seleniumtests.connectors.tms.squash.SquashTMConnector;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public abstract class TestManager {
	

	protected static final Logger logger = SeleniumRobotLogger.getLogger(TestManager.class);
	
	public static final String TMS_TYPE = "tmsType"; 
	public static final String TMS_SERVER_URL = "tmsUrl";
	public static final String TMS_PASSWORD = "tmsPassword";
	public static final String TMS_USER = "tmsUser";
	public static final String TMS_PROJECT = "tmsProject";
	public static final String TMS_DOMAIN = "tmsDomain";
	
    // variable that may be defined dynamically
    public static final String TMS_TEST_ID = "tms.testId";					// name of the variable that identifies the id of the test in TMS
	
	protected boolean initialized;
	
	protected TestManager() {
		initialized = false;
	}
	
	public abstract void recordResult();
	
	public abstract void recordResult(ITestResult testResult);

	public abstract void recordResultFiles();
	
	public abstract void recordResultFiles(ITestResult testResult);
	
	public abstract void login();

	public abstract void init(JSONObject connectParams);

	public abstract void logout();
	
	public static TestManager getInstance(JSONObject configString) {

		String type;
		try {
			type = configString.getString(TMS_TYPE);
		} catch (JSONException e) {
			throw new ConfigurationException("Test manager type must be provided. ex: {'tmsType': 'hp', 'run': '3'}");
		}
		
		if ("hp".equals(type)) {
			return new HpAlmConnector(configString);
		} else if ("squash".equals(type)) {
			return new SquashTMConnector();
		} else {
			throw new ConfigurationException(String.format("TestManager type [%s] is unknown, valid values are: ['hp', 'squash']", type));
		}
	}

	public boolean getInitialized() {
		return initialized;
	}

}
