package com.seleniumtests.reporter.logger;

import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.util.StringUtility;

public class HyperlinkInfo extends StringInfo {

	protected String link;
	
	public HyperlinkInfo(String info, String link) {
		super(info);
		this.link = link;
	}
	
	@Override
	public String encode(String format) {
		
		if ("html".equals(format)) {
			return String.format("<a href=\"%s\">%s</a>", link, StringUtility.encodeString(info, format));
		} else {
			try {
				return StringUtility.encodeString(String.format("link %s;info %s", link, info), format);
			} catch (CustomSeleniumTestsException e) {
				return info;
			}
		}
	}

}
