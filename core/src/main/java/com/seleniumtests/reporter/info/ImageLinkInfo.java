package com.seleniumtests.reporter.info;

import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.util.StringUtility;

public class ImageLinkInfo extends HyperlinkInfo {

	public ImageLinkInfo(String link) {
		super("Image", link);
	}
	

	@Override
	public String encode(String format) {
		
		if ("html".equals(format)) {
			return String.format("<a href=\"%s\"><i class=\"fa fa-file-image-o\" aria-hidden=\"true\"></i></a>", link);
		} else {
			try {
				return StringUtility.encodeString(String.format("link %s;info %s", link, info), format);
			} catch (CustomSeleniumTestsException e) {
				return info;
			}
		}
	}

}
