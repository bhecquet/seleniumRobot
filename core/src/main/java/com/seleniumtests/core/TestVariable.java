package com.seleniumtests.core;

import org.json.JSONObject;

import com.seleniumtests.core.runner.CucumberScenarioWrapper;

public class TestVariable {


	public static final String TEST_VARIABLE_PREFIX = "custom.test.variable.";
	private Integer id; 
	private String name;
	private String internalName;	// name of the variable as stored in variable server. For variables added by test, a prefix is prepended for storage
									// this prefix is not visible to user.
	private String value;
	private boolean reservable;
	
	
	/**
	 * 
	 * @param id			internal id of variable. if -1, this variable does not come from variable server
	 * @param name
	 * @param value
	 * @param reservable
	 */
	public TestVariable(Integer id, String name, String value, boolean reservable, String internalName) {
		this.id = id;
		this.name = name;
		this.internalName = internalName;
		this.value = value;
		this.reservable = reservable;
	}
	
	/**
	 * Variable construction for those coming from env.ini file. They are not reservable and no id is associated
	 * @param name
	 * @param value
	 */
	public TestVariable(String name, String value) {
		this.id = null;
		this.name = name;
		this.internalName = name;
		this.value = value;
		this.reservable = false;
	}
	
	public static TestVariable fromJsonObject(JSONObject variableJson) {
		String name = variableJson.getString("name").replace(TEST_VARIABLE_PREFIX, "");
		return new TestVariable(variableJson.getInt("id"), 
							name, 
							variableJson.getString("value"),
							variableJson.optBoolean("reservable", false),
							variableJson.getString("name"));
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

	public String getValue() {
		return value;
	}

	public String getInternalName() {
		return internalName;
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

        return this.getId() == other.getId() && this.getName() == other.getName();
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
