package com.seleniumtests.reporter.logger;

import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.util.StringUtility;

public class StringInfo {
	
	public String getInfo() {
		return info;
	}

	protected String info;

	public StringInfo(String info) {
		this.info = info;
	}
	
	public String encode(String format) {
		try {
			return StringUtility.encodeString(info, format);
		} catch (CustomSeleniumTestsException e) {
			return info;
		}
	}
}
