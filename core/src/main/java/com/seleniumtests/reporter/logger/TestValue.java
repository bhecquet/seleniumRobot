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
package com.seleniumtests.reporter.logger;

import java.util.ArrayList;

import org.json.JSONObject;

/**
 * Class for storing values that can be reused in reports
 * For example store an amount during the test so that it can be compared by an external tool
 * @author s047432
 *
 */
public class TestValue extends TestAction {
	
	private String message;
	private String value;

	public TestValue(String id, String humanReadableMessage, String value) {
		super(id, false, new ArrayList<>());
		this.message = humanReadableMessage;
		this.value = value;
	}

	public String getMessage() {
		return maskPasswordInString(message);
	}

	public String getValue() {
		return maskPasswordInString(value);
	}
	
	public String format() {
		return String.format("%s %s", message, value);
	}

	@Override
	public JSONObject toJson() {
		JSONObject actionJson = new JSONObject();
		
		actionJson.put("type", "value");
		actionJson.put("message", getMessage());
		actionJson.put("id", getName());
		actionJson.put("value", getValue());
		
		return actionJson;
	}
	
	@Override
	public TestValue encodeTo(String format) {
		TestValue valueToEncode =  new TestValue(name, message, value);
		return encode(format, valueToEncode);
	}

	private TestValue encode(String format, TestValue valueToEncode) {
		super.encode(format, valueToEncode);
		valueToEncode.message = encodeString(message, format);
		valueToEncode.value = encodeString(value, format);
		return valueToEncode;
	}
}
