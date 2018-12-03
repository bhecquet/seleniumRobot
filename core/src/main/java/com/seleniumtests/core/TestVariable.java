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
package com.seleniumtests.core;

import org.json.JSONObject;

public class TestVariable {


	public static final String TEST_VARIABLE_PREFIX = "custom.test.variable.";
	public static final int TIME_TO_LIVE_INFINITE = -1; // value for saying that varible will never be deleted
	private Integer id; 
	private String name;
	private String internalName;	// name of the variable as stored in variable server. For variables added by test, a prefix is prepended for storage
									// this prefix is not visible to user.
	private String value;
	private boolean reservable;
	private int timeToLive;
	
	
	/**
	 * 
	 * @param id			internal id of variable. if -1, this variable does not come from variable server
	 * @param name
	 * @param value
	 * @param reservable
	 */
	public TestVariable(Integer id, String name, String value, boolean reservable, String internalName) {
		this(id, name, value, reservable, internalName, TIME_TO_LIVE_INFINITE);
	}
	
	public TestVariable(Integer id, String name, String value, boolean reservable, String internalName, int timeToLive) {
		this.id = id;
		this.name = name;
		this.internalName = internalName;
		this.value = value;
		this.reservable = reservable;
		this.timeToLive = timeToLive;
	}
	
	/**
	 * Variable construction for those coming from env.ini file. They are not reservable and no id is associated
	 * @param name
	 * @param value
	 */
	public TestVariable(String name, String value) {
		this(null, name, value, false, name, TIME_TO_LIVE_INFINITE);
	}
	
	public static TestVariable fromJsonObject(JSONObject variableJson) {
		String name = variableJson.getString("name").replace(TEST_VARIABLE_PREFIX, "");
		return new TestVariable(variableJson.getInt("id"), 
							name, 
							variableJson.getString("value"),
							variableJson.optBoolean("reservable", false),
							variableJson.getString("name"),
							variableJson.optInt("timeToLive", TIME_TO_LIVE_INFINITE));
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

	public String getValue() {
		return value;
	}

	public String getInternalName() {
		return internalName;
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

        return (this.getId() == other.getId() 
        		&& this.getName() == other.getName() 
        		&& this.isReservable() == other.isReservable()
        		&& this.getTimeToLive() == other.getTimeToLive());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
