package com.seleniumtests.reporter.info;

public abstract class Info {


	public String getInfo() {
		return info;
	}
	
	protected String info;
	
	protected Info(String info) {
		this.info = info;
	}
	
	public abstract String encode(String format);
}
