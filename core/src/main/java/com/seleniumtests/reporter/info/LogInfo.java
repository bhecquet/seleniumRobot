package com.seleniumtests.reporter.info;

/**
 * class for displaying a log in report, e.g, an error
 * @author S047432
 *
 */
public class LogInfo extends StringInfo {

	private String logs;
	
	public LogInfo(String logs) {
		super("Logs");
		this.logs = logs;
	}

	@Override
	public String encode(String format) {
		if ("html".equals(format)) {
			return String.format("<a class=\"errorTooltip\"><i class=\"fas fa-file-alt\" aria-hidden=\"true\" data-toggle=\"popover\" title=\"Exception\" data-content=\"%s\"></i></a>", logs);
		} else {
			return super.encode(format);
		}
	}

}
