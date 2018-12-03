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
