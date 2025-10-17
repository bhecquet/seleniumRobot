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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.Element;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.seleniumtests.core.TestStepManager;
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
	// actionException & actionExceptionMessage are not set for TestAction, only for TestStep
	protected Throwable actionException;
	protected String actionExceptionMessage;
	protected long durationToExclude = 0L; 	// the duration to exclude from the action duration
	protected List<String> pwdToReplace;
	protected boolean maskPassword = true;
	protected boolean encoded = false;		// true if we have encoded messages
	protected OffsetDateTime timestamp;


	protected String action;			// "click", "sendKeys", ...
	private final Element element;		// name of the element on which action occurs
	protected Class<?> origin;		// page / scenario on which action is performed
	
	/**
	 * 
	 * @param name			action name
	 * @param failed		true if this action is failed
	 * @param pwdToReplace	list of string to replace when returning actions so that passwords are masked. Only password longer that 5 characters are replaced to avoid replacing non password strings
	 */
	public TestAction(String name, Boolean failed, List<String> pwdToReplace) {
		this(name, failed, pwdToReplace, null, null, null);
	}

	/**
	 * Creates a test action specifying an element on which action occurs
	 *
	 * @param name			action name, this is the presented name
	 * @param failed		true if this action is failed
	 * @param pwdToReplace	list of string to replace when returning actions so that passwords are masked. Only password longer that 5 characters are replaced to avoid replacing non password strings
	 * @param action		the performed action. Difference with name is that, for element actions, 'name' will be "click on HtmlElement...", 'action' will be 'click'
	 * @param element		the element on which action is performed
	 */
	public TestAction(String name, Boolean failed, List<String> pwdToReplace, String action, Element element) {
		this(name, failed, pwdToReplace, action, element, element.getOriginClass());
	}

	/**
	 * Creates a test action specifying an element on which action occurs
	 *
	 * @param name			action name, this is the presented name
	 * @param failed		true if this action is failed
	 * @param pwdToReplace	list of string to replace when returning actions so that passwords are masked. Only password longer that 5 characters are replaced to avoid replacing non password strings
	 * @param action		the performed action. Difference with name is that, for element actions, 'name' will be "click on HtmlElement...", 'action' will be 'click'
	 * @param origin			the page class on which action is performed
	 */
	public TestAction(String name, Boolean failed, List<String> pwdToReplace, String action, Class<? extends PageObject> origin) {
		this(name, failed, pwdToReplace, action, null, origin);
	}

	/**
	 *
	 * @param name			action name, this is the presented name
	 * @param failed		true if this action is failed
	 * @param pwdToReplace	list of string to replace when returning actions so that passwords are masked. Only password longer that 5 characters are replaced to avoid replacing non password strings
	 * @param action		the performed action. Difference with name is that, for element actions, 'name' will be "click on HtmlElement...", 'action' will be 'click'
	 * @param element		the element on which action is performed
	 */
	public TestAction(String name, Boolean failed, List<String> pwdToReplace, String action, Element element, Class<? extends PageObject> origin) {
		this.name = name; // it's the action performed, with dataset
		this.failed = failed;
		this.pwdToReplace = pwdToReplace.stream()
					.filter(Objects::nonNull)
					.filter(s -> s.length() > TestStepManager.MIN_PASSWORD_LENGTH)
					.collect(Collectors.toList()); // need to be updated

		this.action = action; // it's the action performed, without dataset
		this.element = element;
		this.origin = origin;

		timestamp = OffsetDateTime.now();
	}

	/**
	 * Get the name of the action, replacing passwords
	 * @return		the name of the action
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

	public void setName(String name) {
		this.name = name;
	}

	public OffsetDateTime getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(OffsetDateTime timestamp) {
		this.timestamp = timestamp;
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

		actionJson.put("timestamp", timestamp.toInstant().toEpochMilli());
		actionJson.put("type", "action");
		actionJson.put("name", getName());

		// TestAction do not receive the exception when it failed
		if (parent != null) {
			actionJson.put("exception", parent.actionException == null ? null : parent.actionException.getClass().getName());
			actionJson.put("exceptionMessage", parent.actionException == null ? null : ExceptionUtility.getExceptionMessage(parent.actionException).trim());
		} else {
			actionJson.put("exception", actionException == null ? null : actionException.getClass().getName());
			actionJson.put("exceptionMessage", actionException == null ? null : ExceptionUtility.getExceptionMessage(actionException).trim());
		}
		actionJson.put("failed", failed);
		actionJson.put("position", position);
		actionJson.put("action", action);
		actionJson.put("durationToExclure", durationToExclude);
		if (element != null) {
			actionJson.put("element", element.getName());
		}
		if (origin != null) {
			actionJson.put("origin", origin.getName());
		}

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
				.map(p -> encodeString(p, format))
				.toList();
	}

	public TestAction encode(String format) {
		List<String> encodedPasswords = encodePasswords(pwdToReplace, format);
		TestAction encodedAction = new TestAction(encodeString(name, format), failed, encodedPasswords);
		encodedAction.actionException = actionException;
		encodedAction.maskPassword = maskPassword;
		encodedAction.timestamp = timestamp;
		encodedAction.position = position;
		
		if (format == null) {
			encodedAction.encoded = encoded;
		} else {
			encodedAction.encoded = true;
		}
		encodedAction.durationToExclude = durationToExclude;
		if (actionException != null) {
			encodedAction.actionExceptionMessage = encodeString(ExceptionUtility.getExceptionMessage(actionException), format);
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

	public String getAction() {
		return action;
	}

	public Element getElement() {
		return element;
	}

	public Class<?> getOrigin() {
		return origin;
	}

	public void setAction(String action) {
		this.action = action;
	}

}