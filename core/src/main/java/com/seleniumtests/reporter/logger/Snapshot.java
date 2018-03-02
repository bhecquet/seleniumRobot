package com.seleniumtests.reporter.logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.reporters.CommonReporter;
import com.seleniumtests.util.StringUtility;

public class Snapshot extends TestAction {
	
	private ScreenShot screenshot;

	public static final String SNAPSHOT_PATTERN = "Application Snapshot";
	public static final String OUTPUT_PATTERN = "Output: ";

	public Snapshot(final ScreenShot screenshot) {
		super(screenshot.getImagePath(), false, new ArrayList<>());
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

    /**
     * Rename HTML and PNG files so that they do not present an uuid
     * New name is <test_name>_<step_idx>_<snapshot_idx>_<step_name>_<uuid>
     * @param testStep
     * @param stepIdx	   	number of this step
     * @param snapshotIdx	number of this snapshot for this step
     */
    public void rename(TestStep testStep, int stepIdx, int snapshotIdx) {
    	String newBaseName = String.format("%s_%d-%d_%s-", 
    			StringUtility.replaceOddCharsFromFileName(CommonReporter.getTestName(testStep.getTestResult())),
    			stepIdx, 
    			snapshotIdx,
    			StringUtility.replaceOddCharsFromFileName(testStep.getName()));
    	
    	
    	if (screenshot.getHtmlSourcePath() != null) {
    		String oldFullPath = screenshot.getFullHtmlPath();
    		String oldPath = screenshot.getHtmlSourcePath();
    		File oldFile = new File(oldPath);
    		String folderName = "";
    		if (oldFile.getParent() != null) {
    			folderName = oldFile.getParent().replace(File.separator, "/") + "/";
    		}
    		
    		String newName = newBaseName + FilenameUtils.getBaseName(oldFile.getName());
    		newName = newName.substring(0, Math.min(50, newName.length())) + "." +  FilenameUtils.getExtension(oldFile.getName());
    		screenshot.setHtmlSourcePath(folderName + newName);
    		
    		// if file cannot be moved, go back to old name
    		try {
				FileUtils.moveFile(new File(oldFullPath), new File(screenshot.getFullHtmlPath()));
			} catch (IOException e) {
				screenshot.setHtmlSourcePath(oldPath);
			}
    	}
    	if (screenshot.getImagePath() != null) {
    		String oldFullPath = screenshot.getFullImagePath();
    		String oldPath = screenshot.getImagePath();
    		File oldFile = new File(oldPath);
    		String folderName = "";
    		if (oldFile.getParent() != null) {
    			folderName = oldFile.getParent().replace(File.separator, "/") + "/";
    		}
    		
    		String newName = newBaseName + FilenameUtils.getBaseName(oldFile.getName());
    		newName = newName.substring(0, Math.min(50, newName.length())) + "." + FilenameUtils.getExtension(oldFile.getName());
    		screenshot.setImagePath(folderName + newName);
    		
    		// if file cannot be moved, go back to old name
    		try {
				FileUtils.moveFile(new File(oldFullPath), new File(screenshot.getFullImagePath()));
			} catch (IOException e) {
				screenshot.setImagePath(oldPath);
			}
    	}
    }
    
	public ScreenShot getScreenshot() {
		return screenshot;
	}

}
