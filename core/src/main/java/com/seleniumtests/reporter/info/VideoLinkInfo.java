package com.seleniumtests.reporter.info;

public class VideoLinkInfo extends HyperlinkInfo {

	public VideoLinkInfo(String link) {
		super("Video", link);
	}
	

	@Override
	public String encode(String format) {

		if (format == null) {
			logger.error("format cannot be null");
			return description;
		} else if ("html".equals(format)) {
			return String.format("<a href=\"%s\"><i class=\"fas fa-video\" aria-hidden=\"true\"></i></a>", link);
		} else {
			return super.encode(format);
		}
	}

}
