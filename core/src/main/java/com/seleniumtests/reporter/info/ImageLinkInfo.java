package com.seleniumtests.reporter.info;

import com.seleniumtests.browserfactory.ICapabilitiesFactory;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import org.apache.log4j.Logger;

public class ImageLinkInfo extends HyperlinkInfo {

	public ImageLinkInfo(String link) {
		super("Image", link);
	}
	

	@Override
	public String encode(String format) {
		
		if ("html".equals(format)) {
			return String.format("<a href=\"%s\"><i class=\"fas fa-file-image\" aria-hidden=\"true\"></i></a>", link);
		} else {
			if (format != null) {
				return super.encode(format);
			} else {
				logger.error("format cannot be null");
			}
		}

		return format;
	}

}
