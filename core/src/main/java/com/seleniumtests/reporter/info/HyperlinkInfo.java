package com.seleniumtests.reporter.info;

import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.util.StringUtility;

public class HyperlinkInfo extends Info {

	protected String link;

	public HyperlinkInfo(String info, String link) {
		super(info);
		this.link = link;
	}
	
	@Override
	public String encode(String format) {
		
		if ("html".equals(format)) {
			return String.format("<a href=\"%s\">%s</a>", link, StringUtility.encodeString(description, format));
		} else {
			try {
				return StringUtility.encodeString(String.format("link %s;info %s", link, description), format);
			} catch (CustomSeleniumTestsException e) {
				return description;
			}
		}
	}

}
