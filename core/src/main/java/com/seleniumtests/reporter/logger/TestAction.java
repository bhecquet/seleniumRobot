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
package com.seleniumtests.reporter.logger;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.seleniumtests.util.ExceptionUtility;
import com.seleniumtests.util.StringUtility;
import com.seleniumtests.util.logging.SeleniumRobotLogger;


/**
 * This is an action made inside the test: click on an element, ...
 * @author behe
 *
 */
public class TestAction {
	

	protected static final Logger logger = SeleniumRobotLogger.getLogger(TestAction.class);
	
	protected String name;
	protected TestAction parent = null;
	protected int position = 0;
	protected Boolean failed;
	protected Throwable actionException;
	protected String actionExceptionMessage;
	protected long durationToExclude = 0L; 	// the duration to exclude from the action duration
	protected List<String> pwdToReplace;
	protected boolean maskPassword = true;
	protected boolean encoded = false;		// true if we have encoded messages
	
	/**
	 * 
	 * @param name			action name
	 * @param failed		true if this action is failed
	 * @param pwdToReplace	list of string to replace when returning actions so that passwords are masked. Only password longer that 5 characters are replaced to avoid replacing non password strings
	 */
	public TestAction(String name, Boolean failed, List<String> pwdToReplace) {
		this.name = name;
		this.failed = failed;
		this.pwdToReplace = pwdToReplace.stream()
					.filter(s -> s.length() > 5)
					.collect(Collectors.toList());
	}

	/**
	 * Get the name of the action, replacing passwords
	 * @return
	 */
	public String getName() {
		String newName = name;
		if (maskPassword) {
			for (String pwd: pwdToReplace) {
				newName = newName.replace(pwd, "******");
			}
		}
		return newName;
	}

	public Boolean getFailed() {
		return failed;
	}

	public List<String> getPwdToReplace() {
		return pwdToReplace;
	}

	public void setFailed(Boolean failed) {
		this.failed = failed;
	}
	
	public Throwable getActionException() {
		return actionException;
	}
	
	public long getDurationToExclude() {
		return durationToExclude;
	}

	public void setDurationToExclude(long durationToExclude) {
		this.durationToExclude += durationToExclude;
	}

	public String getActionExceptionMessage() {
		return actionExceptionMessage;
	}

	public void setActionException(Throwable actionException) {
		this.actionException = actionException;
	}
	
	@Override
	public String toString() {
		return toString(0);
	}
	
	public String toString(int spaces) {
		return getName();
	}
	
	public JSONObject toJson() {
		JSONObject actionJson = new JSONObject();
		
		actionJson.put("type", "action");
		actionJson.put("name", encodeString(name, "json"));
		actionJson.put("exception", actionException == null ? null: actionException.toString());
		actionJson.put("failed", failed);
		
		return actionJson;
	}
	
	protected String encodeString(String message, String format) {
		if (encoded || format == null) {
			return message;
		}
		
		return StringUtility.encodeString(message, format);
	}
	
	protected List<String> encodePasswords(List<String> passwords, String format) {
		return passwords
				.stream()
				.map(p -> encodeString(p, format)).collect(Collectors.toList());
	}

	public TestAction encode(String format) {
		List<String> encodedPasswords = encodePasswords(pwdToReplace, format);
		TestAction encodedAction = new TestAction(encodeString(name, format), failed, encodedPasswords);
		encodedAction.actionException = actionException;
		encodedAction.maskPassword = maskPassword;
		
		if (format == null) {
			encodedAction.encoded = encoded;
		} else {
			encodedAction.encoded = true;
		}
		encodedAction.durationToExclude = durationToExclude;
		if (actionException != null) {
			encodedAction.actionExceptionMessage = encodeString(ExceptionUtility.getExceptionMessage(actionException), format);;
		}
		return encodedAction;
	}
	
	public TestAction deepCopy() {
		return encode(null);
	}

	public TestAction getParent() {
		return parent;
	}

	public void setParent(TestAction parent) {
		this.parent = parent;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

}