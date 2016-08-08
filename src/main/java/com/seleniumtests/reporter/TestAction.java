package com.seleniumtests.reporter;

/**
 * This is an action made inside the test: click on an element, ...
 * @author behe
 *
 */
public class TestAction {
	private String name;
	private Boolean failed;
	
	public TestAction(String name, Boolean failed) {
		this.name = name;
		this.failed = failed;
	}

	public String getName() {
		return name;
	}

	public Boolean getFailed() {
		return failed;
	}

	public void setFailed(Boolean failed) {
		this.failed = failed;
	}
}