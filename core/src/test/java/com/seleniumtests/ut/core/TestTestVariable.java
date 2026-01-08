/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.ut.core;

import java.time.LocalDateTime;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;

import kong.unirest.core.json.JSONObject;

public class TestTestVariable extends GenericTest {
	
	@Test(groups={"ut"})
	public void testSimpleConstructor() {
		TestVariable variable = new TestVariable("key", "value");
		Assert.assertNull(variable.getId());
		Assert.assertEquals(variable.getInternalName(), variable.getName());
		Assert.assertFalse(variable.isReservable());
	}
	
	/**
	 * Test variable without date
	 */
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
		
		TestVariable variable = TestVariable.fromJsonObject(jsonObject, 2, "core");
		Assert.assertEquals(variable.getId(), (Integer)1);
		Assert.assertEquals(variable.getName(), "key");
		Assert.assertEquals(variable.getValue(), "value");
        Assert.assertFalse(variable.isReservable());
		Assert.assertEquals(variable.getInternalName(), "key");
		Assert.assertEquals(variable.getApplication(), 2);
		Assert.assertEquals(variable.getApplicationName(), "core");
		Assert.assertNull(variable.getCreationDate());
	}

	@Test(groups={"ut"})
	public void testFromJSonApplicationNull() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("id", 1);
		jsonObject.put("name", "key");
		jsonObject.put("value", "value");
		jsonObject.put("reservable", false);
		jsonObject.put("environment", 1);
		jsonObject.put("version", 3);

		TestVariable variable = TestVariable.fromJsonObject(jsonObject, 2, "core");
		Assert.assertNull(variable.getApplication());
		Assert.assertNull(variable.getApplicationName());
	}

	@Test(groups={"ut"})
	public void testFromJSonIdNull() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", "key");
		jsonObject.put("value", "value");
		jsonObject.put("reservable", false);
		jsonObject.put("environment", 1);
		jsonObject.put("application", 2);
		jsonObject.put("version", 3);

		TestVariable variable = TestVariable.fromJsonObject(jsonObject, 2, "core");
		Assert.assertNull(variable.getId());
	}

	/**
	 * When variable is from a linked application, it's name contains the application
	 */
	@Test(groups={"ut"})
	public void testFromJSonWithLinkedVariable() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("id", 1);
		jsonObject.put("name", "app1.key");
		jsonObject.put("value", "value");
		jsonObject.put("reservable", false);
		jsonObject.put("environment", 1);
		jsonObject.put("application", 3);
		jsonObject.put("version", 3);

		TestVariable variable = TestVariable.fromJsonObject(jsonObject, 2, "core");
		Assert.assertEquals(variable.getName(), "app1.key");
		Assert.assertEquals(variable.getInternalName(), "app1.key");
		Assert.assertEquals(variable.getApplication(), 3);
		Assert.assertEquals(variable.getApplicationName(), "app1");
	}

	@Test(groups={"ut"})
	public void testFromJSonWithLinkedVariable2() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("id", 1);
		jsonObject.put("name", "app1.key.subkey");
		jsonObject.put("value", "value");
		jsonObject.put("reservable", false);
		jsonObject.put("environment", 1);
		jsonObject.put("application", 3);
		jsonObject.put("version", 3);

		TestVariable variable = TestVariable.fromJsonObject(jsonObject, 2, "core");
		Assert.assertEquals(variable.getName(), "app1.key.subkey");
		Assert.assertEquals(variable.getInternalName(), "app1.key.subkey");
		Assert.assertEquals(variable.getApplication(), 3);
		Assert.assertEquals(variable.getApplicationName(), "app1");
	}
	
	/**
	 * Test variable with date
	 */
	@Test(groups={"ut"})
	public void testFromJSonWithDate() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("id", 1);
		jsonObject.put("name", "key");
		jsonObject.put("value", "value");
		jsonObject.put("reservable", false);
		jsonObject.put("environment", 1);
		jsonObject.put("application", 2);
		jsonObject.put("version", 3);
		jsonObject.put("creationDate", "2018-07-12T08:42:56.156727Z");
		
		TestVariable variable = TestVariable.fromJsonObject(jsonObject, 2, "core");
		Assert.assertEquals(variable.getId(), (Integer)1);
		Assert.assertEquals(variable.getName(), "key");
		Assert.assertEquals(variable.getValue(), "value");
        Assert.assertFalse(variable.isReservable());
		Assert.assertEquals(variable.getInternalName(), "key");
		Assert.assertEquals(variable.getCreationDate(), LocalDateTime.of(2018, 7, 12, 8, 42, 56, 156727000));
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
		
		TestVariable variable = TestVariable.fromJsonObject(jsonObject, 2, "core");
		Assert.assertEquals(variable.getId(), (Integer)1);
		Assert.assertEquals(variable.getName(), "key");
		Assert.assertEquals(variable.getValue(), "value");
        Assert.assertFalse(variable.isReservable());
		Assert.assertEquals(variable.getInternalName(), "custom.test.variable.key");
		Assert.assertNull(variable.getCreationDate());
	}
	
	@Test(groups={"ut"})
	public void testInterpolateValue() {

		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("path", new TestVariable("path", "/foo/bar"));
		
		Assert.assertEquals(new TestVariable("url", "http://mysite${path}").getValue(), "http://mysite/foo/bar");
	}
}