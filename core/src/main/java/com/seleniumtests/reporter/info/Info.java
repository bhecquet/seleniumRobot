package com.seleniumtests.reporter.info;

public abstract class Info {


	public String getInfo() {
		return description;
	}
	
	protected String description;
	
	protected Info(String info) {
		this.description = info;
	}
	
	public abstract String encode(String format);
}
