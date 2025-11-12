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
package com.seleniumtests.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.apache.logging.log4j.Logger;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.StringUtility;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import kong.unirest.core.json.JSONObject;


public class TestVariable {

	private static final Logger logger = SeleniumRobotLogger.getLogger(TestVariable.class);
	public static final String TEST_VARIABLE_PREFIX = "custom.test.variable.";
	public static final int TIME_TO_LIVE_INFINITE = -1; // value for saying that varible will never be deleted
	private final Integer id;
	private String name;
	private String internalName;	// name of the variable as stored in variable server. For variables added by test, a prefix is prepended for storage
									// this prefix is not visible to user.
	private String value;
	private final LocalDateTime creationDate;
	private boolean reservable;
	private int timeToLive;
	
	
	/**
	 * 
	 * @param id			internal id of variable. if -1, this variable does not come from variable server
	 * @param name			name of the variable
	 * @param value			value of the variable
	 * @param reservable	whether this variable is reservable or not
	 */
	public TestVariable(Integer id, String name, String value, boolean reservable, String internalName) {
		this(id, name, value, reservable, internalName, TIME_TO_LIVE_INFINITE, null);
	}
	
	public TestVariable(Integer id, String name, String value, boolean reservable, String internalName, int timeToLive, LocalDateTime creationDate) {
		this.id = id;
		this.name = name;
		this.internalName = internalName;
		this.value = value;
		this.reservable = reservable;
		this.timeToLive = timeToLive;
		this.creationDate = creationDate;
	}
	
	/**
	 * Variable construction for those coming from env.ini file. They are not reservable and no id is associated
	 * @param name		name of the variable
	 * @param value		value of the variable
	 */
	public TestVariable(String name, String value) {
		this(null, name, value, false, name, TIME_TO_LIVE_INFINITE, null);
	}
	
	public static TestVariable fromJsonObject(JSONObject variableJson) {
		String name = variableJson.getString("name").replace(TEST_VARIABLE_PREFIX, "");
		LocalDateTime creationDate;
		try {
			creationDate = LocalDateTime.parse(variableJson.getString("creationDate"), DateTimeFormatter.ISO_DATE_TIME);
		} catch (Exception e) {
			creationDate = null;
		}
		
		return new TestVariable(variableJson.getInt("id"), 
							name, 
							variableJson.getString("value"),
							variableJson.optBoolean("reservable", false),
							variableJson.getString("name"),
							variableJson.optInt("timeToLive", TIME_TO_LIVE_INFINITE),
							creationDate);
	}
	
	public void setValue(String newValue) {
		value = newValue;
	}

	public boolean isReservable() {
		return reservable;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getTimeToLive() {
		return timeToLive;
	}
	
	public String getValueNoInterpolation() {
		return value;
	}

	public String getValue() {
		try {
			return StringUtility.interpolateString(value, SeleniumTestsContextManager.getThreadContext());
		} catch (ConfigurationException e) {
			if (StringUtility.PLACEHOLDER_PATTERN.matcher(value).find()) {
				logger.warn("Cannot interpolate variable value, context is not initalized");
			}
			return value;
		}
	}

	public String getInternalName() {
		return internalName;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setInternalName(String internalName) {
		this.internalName = internalName;
	}

	public void setReservable(boolean reservable) {
		this.reservable = reservable;
	}

	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}

	@Override
	public String toString() {
		if (getId() != null) {
			return String.format("%d - %s", getId(), getName());
		} else {
			return getName();
		}
	}
	 
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
        	return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        TestVariable other;
        try {
        	other = (TestVariable) obj;
        } catch (Exception e) {
        	return false;
        }

        return (Objects.equals(this.getId(), other.getId())
        		&& Objects.equals(this.getName(), other.getName())
        		&& this.isReservable() == other.isReservable()
        		&& this.getTimeToLive() == other.getTimeToLive());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
