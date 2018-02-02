package com.seleniumtests.reporter.logger;

import com.seleniumtests.driver.screenshots.ScreenShot;

public class Snapshot extends TestAction {
	
	private ScreenShot screenshot;

	public static final String SNAPSHOT_PATTERN = "Application Snapshot";
	public static final String OUTPUT_PATTERN = "Output: ";

	public Snapshot(final ScreenShot screenshot) {
		super(screenshot.getImagePath(), false);
		this.screenshot = screenshot;
	}
	
    /**
     * Log Screenshot method
     * Return: screenshot message with links
     *
     * @param  screenShot
     * 
     * @return String
     */
    public String buildScreenshotLog() {

        StringBuilder sbMessage = new StringBuilder("");
        sbMessage.append(OUTPUT_PATTERN + screenshot.getTitle() + ": ");
        
        if (screenshot.getLocation() != null) {
            sbMessage.append("<a href='" + screenshot.getLocation() + "' target=url>Application URL</a>");
        }

        if (screenshot.getHtmlSourcePath() != null) {
            sbMessage.append(" | <a href='" + screenshot.getHtmlSourcePath()
                    + "' target=html>Application HTML Source</a>");
        }

        if (screenshot.getImagePath() != null) {
            sbMessage.append(" | <a href='" + screenshot.getImagePath()
                    + "' class='lightbox'>" + SNAPSHOT_PATTERN + "</a>");
        }

        return sbMessage.toString();
    }

	public ScreenShot getScreenshot() {
		return screenshot;
	}

}
