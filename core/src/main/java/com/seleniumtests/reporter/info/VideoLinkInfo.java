package com.seleniumtests.reporter.info;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.reporter.logger.FileContent;
import com.seleniumtests.util.FileUtility;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.testng.ITestResult;

import java.io.File;
import java.nio.file.Paths;

public class VideoLinkInfo extends HyperlinkInfo implements FileLinkInfo {

	FileContent videoFileContent;

	public VideoLinkInfo(FileContent videoFileContent) {
		super("Video", videoFileContent.getName());
		this.videoFileContent = videoFileContent;
	}

	/**
	 * Returns the string associated to this info
	 * For HTML, path is relative to the root of test results
	 * @param format
	 * @return
	 */
	@Override
	public String encode(String format) {

		if (format == null) {
			logger.error("format cannot be null");
			return description;
		} else if ("html".equals(format)) {
			link = Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).relativize(videoFileContent.getFile().toPath()).toString();
			return String.format("<a href=\"%s\"><i class=\"fas fa-video\" aria-hidden=\"true\"></i></a>", link.replace("\\", "/"));
		} else {
			return super.encode(format);
		}
	}

	@Override
	public JSONObject toJson() {
		return new JSONObject().put("type", "videolink")
				.put("info", description)
				.put("link", videoFileContent.getName())
				.put("id", videoFileContent.getId());
	}

	@Override
	public FileContent getFileContent() {
		return videoFileContent;
	}
}
