/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.reporter.logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.reporters.CommonReporter;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.StringUtility;

public class Snapshot extends TestAction {
	
	private ScreenShot screenshot;
	private SnapshotCheckType checkSnapshot; // whether this snapshot will be sent to Snapshot server to check if it conforms to baseline

	public static final String SNAPSHOT_PATTERN = "Application Snapshot";
	public static final String OUTPUT_PATTERN = "Output '%s' browser: ";

	public Snapshot(final ScreenShot screenshot, String driverName, SnapshotCheckType checkSnapshot) {
		super(driverName, false, new ArrayList<>());
		this.screenshot = screenshot;
		this.checkSnapshot = checkSnapshot;
		durationToExclude = screenshot.getDuration();
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
        sbMessage.append(String.format(OUTPUT_PATTERN, name) + StringEscapeUtils.escapeHtml4(screenshot.getTitle()) + ": ");
        
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
     * @param userGivenName	name specified by user, rename to this name
     */
    public void rename(final TestStep testStep, final int stepIdx, final int snapshotIdx, final String userGivenName) {
    	String newBaseName;
    	if (userGivenName == null) {
	    	newBaseName = String.format("%s_%d-%d_%s-", 
	    			StringUtility.replaceOddCharsFromFileName(CommonReporter.getTestName(testStep.getTestResult())),
	    			stepIdx, 
	    			snapshotIdx,
	    			StringUtility.replaceOddCharsFromFileName(testStep.getName()));
    	} else {
    		newBaseName = StringUtility.replaceOddCharsFromFileName(userGivenName);
    	}
    	
    	
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
    		
    		// if file cannot be moved, go back to old name
    		try {
    			oldFile = new File(oldFullPath);
    			if (SeleniumTestsContextManager.getGlobalContext().getOptimizeReports()) {
    				screenshot.setHtmlSourcePath(folderName + newName + ".zip");
    				oldFile = FileUtility.createZipArchiveFromFiles(Arrays.asList(oldFile));
    			} else {
    				screenshot.setHtmlSourcePath(folderName + newName);
    			}
    			
				FileUtils.copyFile(oldFile, new File(screenshot.getFullHtmlPath()));
				if (!new File(oldFullPath).delete()) {
					logger.warn(String.format("Could not delete %s", oldFullPath));
				}
				
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
    			Files.move(Paths.get(oldFullPath), Paths.get(screenshot.getFullImagePath()), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				screenshot.setImagePath(oldPath);
			}
    	}
    }
    
	public ScreenShot getScreenshot() {
		return screenshot;
	}
	
	public void relocate(String outputDirectory) throws IOException {
		if (screenshot != null) {
			screenshot.relocate(outputDirectory);
		}
	}
	
	public Snapshot encode() {
		return new Snapshot(screenshot, name, checkSnapshot);
	}
	
	public SnapshotCheckType getCheckSnapshot() {
		return checkSnapshot;
	}

}
