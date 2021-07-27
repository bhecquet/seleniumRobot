package com.seleniumtests.reporter.info;

public class ImageLinkInfo extends HyperlinkInfo {

	public ImageLinkInfo(String link) {
		super("Image", link);
	}
	

	@Override
	public String encode(String format) {
		
		if ("html".equals(format)) {
			return String.format("<a href=\"%s\"><i class=\"fa fa-file-image-o\" aria-hidden=\"true\"></i></a>", link);
		} else {
			return super.encode(format);
		}
	}

}
