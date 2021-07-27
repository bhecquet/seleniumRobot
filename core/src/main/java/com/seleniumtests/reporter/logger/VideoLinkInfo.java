package com.seleniumtests.reporter.logger;

import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.util.StringUtility;

public class VideoLinkInfo extends HyperlinkInfo {

	public VideoLinkInfo(String link) {
		super("Video", link);
	}
	

	@Override
	public String encode(String format) {
		
		if ("html".equals(format)) {
			return String.format("<a href=\"%s\"><i class=\"fa fa-video-camera\" aria-hidden=\"true\"></i></a>", link);
		} else {
			try {
				return StringUtility.encodeString(String.format("link %s;info %s", link, info), format);
			} catch (CustomSeleniumTestsException e) {
				return info;
			}
		}
	}

}
