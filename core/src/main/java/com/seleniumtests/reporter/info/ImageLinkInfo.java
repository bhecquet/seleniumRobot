package com.seleniumtests.reporter.info;

public class ImageLinkInfo extends HyperlinkInfo {

	public ImageLinkInfo(String link) {
		super("Image", link);
	}
	

	@Override
	public String encode(String format) {
		
		if ("html".equals(format)) {
			return String.format("<a href=\"%s\"><i class=\"fas fa-file-image\" aria-hidden=\"true\"></i></a>", link);
		} else {
			return super.encode(format);
		}
	}

}