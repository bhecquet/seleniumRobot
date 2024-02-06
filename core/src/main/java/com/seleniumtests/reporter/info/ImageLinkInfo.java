package com.seleniumtests.reporter.info;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.reporter.logger.FileContent;
import org.json.JSONObject;
import org.testng.ITestResult;

import java.io.File;
import java.nio.file.Paths;

public class ImageLinkInfo extends HyperlinkInfo {

	FileContent imageFileContent;

	public ImageLinkInfo(FileContent imageFileContent) {

		super("Image", imageFileContent.getName());
		this.imageFileContent = imageFileContent;
	}

	/**
	 * Returns the string associated to this info
	 * For HTML, path is relative to the root of test results
	 * @param format
	 * @return
	 */
	@Override
	public String encode(String format) {
		
		if ("html".equals(format)) {
			link = Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).relativize(imageFileContent.getFile().toPath()).toString();
			return String.format("<a href=\"%s\"><i class=\"fas fa-file-image\" aria-hidden=\"true\"></i></a>", link.replace("\\", "/"));
		} else {
			if (format != null) {
				return super.encode(format);
			} else {
				logger.error("format cannot be null");
			}
		}

		return description;
	}

	@Override
	public JSONObject toJson() {
		return new JSONObject().put("type", "imagelink")
				.put("info", description)
				.put("link", imageFileContent.getName())
				.put("id", imageFileContent.getId() == null ? JSONObject.NULL: imageFileContent.getId());
	}

}
