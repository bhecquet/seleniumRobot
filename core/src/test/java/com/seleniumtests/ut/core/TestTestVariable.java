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
package com.seleniumtests.ut.core;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.TestVariable;

public class TestTestVariable extends GenericTest {
	
	@Test(groups={"ut"})
	public void testSimpleConstructor() {
		TestVariable var = new TestVariable("key", "value");
		Assert.assertNull(var.getId());
		Assert.assertEquals(var.getInternalName(), var.getName());
		Assert.assertFalse(var.isReservable());
	}
	
	@Test(groups={"ut"})
	public void testFromJSon() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("id", 1);
		jsonObject.put("name", "key");
		jsonObject.put("value", "value");
		jsonObject.put("reservable", false);
		jsonObject.put("environment", 1);
		jsonObject.put("application", 2);
		jsonObject.put("version", 3);
		
		TestVariable var = TestVariable.fromJsonObject(jsonObject);
		Assert.assertEquals(var.getId(), (Integer)1);
		Assert.assertEquals(var.getName(), "key");
		Assert.assertEquals(var.getValue(), "value");
		Assert.assertEquals(var.isReservable(), false);
		Assert.assertEquals(var.getInternalName(), "key");
	}
	
	@Test(groups={"ut"})
	public void testFromJSonCustomVariable() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("id", 1);
		jsonObject.put("name", "custom.test.variable.key");
		jsonObject.put("value", "value");
		jsonObject.put("reservable", false);
		jsonObject.put("environment", 1);
		jsonObject.put("application", 2);
		jsonObject.put("version", 3);
		
		TestVariable var = TestVariable.fromJsonObject(jsonObject);
		Assert.assertEquals(var.getId(), (Integer)1);
		Assert.assertEquals(var.getName(), "key");
		Assert.assertEquals(var.getValue(), "value");
		Assert.assertEquals(var.isReservable(), false);
		Assert.assertEquals(var.getInternalName(), "custom.test.variable.key");
	}
}