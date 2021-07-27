package com.seleniumtests.reporter.info;

import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.util.StringUtility;

public class StringInfo extends Info {
	
	public StringInfo(String info) {
		super(info);
	}
	
	public String encode(String format) {
		try {
			return StringUtility.encodeString(info, format);
		} catch (CustomSeleniumTestsException e) {
			return info;
		}
	}
}
