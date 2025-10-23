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

public class TestMessage extends TestAction {
	
	private final MessageType messageType;


	public enum MessageType {
		ERROR,
		WARNING,	// warning message
		INFO,		// success message
		LOG			// neutral message
	}
	
	public TestMessage(String name, MessageType type) {
		super(name, type == MessageType.ERROR, new ArrayList<>());
		messageType = type;
	}

	public MessageType getMessageType() {
		return messageType;
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject actionJson = new JSONObject();
		
		actionJson.put("type", "message");
		actionJson.put("name", getName());
		actionJson.put("messageType", messageType.toString());
		
		return actionJson;
	}
	
	@Override
	public TestMessage encodeTo(String format) {
		TestMessage messageToEncode = new TestMessage(name, messageType);
		return encode(format, messageToEncode);
	}

	private TestMessage encode(String format, TestMessage messageToEncode) {
		super.encode(format, messageToEncode);
		return messageToEncode;
	}
}
