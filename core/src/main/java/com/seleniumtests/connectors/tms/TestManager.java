/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.seleniumtests.connectors.tms.hpalm.HpAlmConnector;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public abstract class TestManager {
	

	protected static final Logger logger = SeleniumRobotLogger.getLogger(TestManager.class);
	protected Boolean initialized;
	
	public TestManager() {
		initialized = false;
	}
	
	public abstract void recordResult();
	
	public abstract void recordResultFiles();
	
	public abstract void login();

	public abstract void init(JSONObject connectParams);

	public abstract void logout();
	
	public static TestManager getInstance(JSONObject configString) {
		String type;
		try {
			type = configString.getString("type");
		} catch (JSONException e) {
			throw new ConfigurationException("Test manager type must be provided. ex: {'type': 'hp', 'run': '3'}");
		}
		
		if ("hp".equals(type)) {
			return new HpAlmConnector(configString);
		} else {
			throw new ConfigurationException(String.format("TestManager type [%s] is unknown, valid values are: ['hp']", type));
		}
	}

	public Boolean getInitialized() {
		return initialized;
	}

}
