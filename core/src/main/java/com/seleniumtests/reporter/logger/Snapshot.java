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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.reporters.CommonReporter;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.StringUtility;

public class Snapshot extends TestAction {
	
	private ScreenShot screenshot;
	private SnapshotCheckType snapshotCheckType; // whether this snapshot will be sent to Snapshot server to check if it conforms to baseline
	private boolean displayInReport = true;

	public static final String SNAPSHOT_PATTERN = "Application Snapshot";
	public static final String OUTPUT_PATTERN = "Output '%s' browser: ";

	public Snapshot(final ScreenShot screenshot, String snaphotName, SnapshotCheckType snapshotCheckType) throws FileNotFoundException {
		super(snaphotName, false, new ArrayList<>());
		
		if (screenshot == null) {
			throw new FileNotFoundException("Snapshot needs a screenshot");
		}
		
		this.screenshot = screenshot;
		this.snapshotCheckType = snapshotCheckType;
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
        
        
        if (screenshot.getImagePath() != null) {
        	String uuid = UUID.randomUUID().toString();
        	sbMessage.append(String.format("<div class=\"text-center\">\n"
        			+ "      <a href=\"#\" onclick=\"$('#imagepreview').attr('src', $('#%s').attr('src'));$('#imagemodal').modal('show');\">\n"
        			+ "          <img id=\"%s\" src=\"%s\" style=\"width: 300px\">\n"
        			+ "      </a>"
        			+ "</div>\n", uuid, uuid, screenshot.getImagePath()));
        }
        
        String snapshotTitle = name;
        if (screenshot.getTitle() != null) {
        	snapshotTitle = snapshotTitle + ": " + screenshot.getTitle();
        }
        sbMessage.append("<div class=\"text-center\">" + StringEscapeUtils.escapeHtml4(snapshotTitle) + "</div>\n");
        sbMessage.append("<div class=\"text-center font-weight-lighter\">");
        
        if (screenshot.getLocation() != null) {
            sbMessage.append("<a href='" + screenshot.getLocation() + "' target=url>URL</a>\n");
        }

        if (screenshot.getHtmlSourcePath() != null) {
            sbMessage.append(" | <a href='" + screenshot.getHtmlSourcePath() + "' target=html>HTML Source</a>\n");
        }

        sbMessage.append("</div>\n");

        

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
    		renameHtmlSourceFile(newBaseName);
    						
    	}
    	if (screenshot.getImagePath() != null) {
    		renameImageFile(newBaseName);
    	}
    }
    
    @Override
	public JSONObject toJson() {
    	JSONObject snapshotJson = super.toJson();
    	snapshotJson.put("snapshotCheckType", snapshotCheckType.getName());
    	snapshotJson.put("image", String.format(FILE_PATTERN, screenshot.getFullImagePath() != null ? screenshot.getFullImagePath().replace("\\", "/"): null));
    	snapshotJson.put("html", String.format(FILE_PATTERN, screenshot.getFullHtmlPath()!= null ? screenshot.getFullHtmlPath().replace("\\", "/"): null));
    	snapshotJson.put("name", screenshot.getImageName());
    	snapshotJson.put("title", screenshot.getTitle());
    	snapshotJson.put("url", screenshot.getLocation());
    	snapshotJson.put("type", "snapshot");
    	
    	return snapshotJson;
    }

	/**
	 * Rename the image file with "newBaseName"
	 * @param newBaseName
	 */
	private void renameImageFile(String newBaseName) {
		
		// old file name contains a generated UUID
		String oldFullPath = screenshot.getFullImagePath();
		String oldPath = screenshot.getImagePath();
		File oldFile = new File(oldPath);
		String folderName = "";
		if (oldFile.getParent() != null) {
			folderName = oldFile.getParent().replace(File.separator, "/") + "/";
		}
		

		String oldFileName = oldFile.getName();
		// build file name with <base name>-<part of UUID>.html
		// this way, even when test is repeated multiple times, snapshots will have different names 
		// (usefull for comments in bugtrackers where reference to new file should be different from previous ones)
		String newName;
		if (Boolean.TRUE.equals(SeleniumTestsContextManager.getThreadContext().getRandomInAttachments())) {
			newName = String.format("%s-%s", newBaseName.substring(0, Math.min(50, newBaseName.length())), 
																oldFileName.substring(oldFileName.length() - 10));
		} else {
			newName = String.format("%s.%s", newBaseName.substring(0, Math.min(50, newBaseName.length())), 
					FilenameUtils.getExtension(oldFileName));
		}

		screenshot.setImagePath(folderName + newName);
		
		// if file cannot be moved, go back to old name
		try {
			Files.move(Paths.get(oldFullPath), Paths.get(screenshot.getFullImagePath()), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			screenshot.setImagePath(oldPath);
		}
	}

	/**
	 * @param newBaseName
	 */
	private void renameHtmlSourceFile(String newBaseName) {
		String oldFullPath = screenshot.getFullHtmlPath();
		String oldPath = screenshot.getHtmlSourcePath();
		File oldFile = new File(oldPath);
		String folderName = "";
		if (oldFile.getParent() != null) {
			folderName = oldFile.getParent().replace(File.separator, "/") + "/";
		}
		
		String oldFileName = oldFile.getName();
		// build file name with <base name>-<part of UUID>.html
		// this way, even when test is repeated multiple times, snapshots will have different names 
		// (usefull for comments in bugtrackers where reference to new file should be different from previous ones)
		String newName;
		if (Boolean.TRUE.equals(SeleniumTestsContextManager.getThreadContext().getRandomInAttachments())) {
			newName = String.format("%s-%s", newBaseName.substring(0, Math.min(50, newBaseName.length())), 
																oldFileName.substring(oldFileName.length() - 10));
		} else {
			newName = String.format("%s.%s", newBaseName.substring(0, Math.min(50, newBaseName.length())), 
					FilenameUtils.getExtension(oldFileName));
		}

		
		// if file cannot be moved, go back to old name
		try {
			oldFile = new File(oldFullPath);
			if (SeleniumTestsContextManager.getGlobalContext().getOptimizeReports()) {
				screenshot.setHtmlSourcePath(folderName + newName + ".zip");
				oldFile = FileUtility.createZipArchiveFromFiles(Arrays.asList(oldFile));
			} else {
				screenshot.setHtmlSourcePath(folderName + newName);
			}
			
			FileUtility.copyFile(oldFile, new File(screenshot.getFullHtmlPath()));
		} catch (IOException e) {
			screenshot.setHtmlSourcePath(oldPath);
		}

		try {
			Files.delete(Paths.get(new File(oldFullPath).getCanonicalPath()));
		} catch (IOException e) {
			// do not consider it as an error if old file cannot be deleted
			logger.warn(String.format("Could not delete %s", oldFullPath));
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
	
	public void relocate(String outputDirectory, String newImagePath) throws IOException {
		if (screenshot != null) {
			screenshot.relocate(outputDirectory, newImagePath, null);
		}
	}
	
	public Snapshot encode() throws FileNotFoundException {
		return new Snapshot(screenshot, name, snapshotCheckType);
	}
	
	public SnapshotCheckType getCheckSnapshot() {
		return snapshotCheckType;
	}

	public void setCheckSnapshot(SnapshotCheckType checkSnapshot) {
		this.snapshotCheckType = checkSnapshot;
	}

	public boolean isDisplayInReport() {
		return displayInReport;
	}

	public void setDisplayInReport(boolean displayInReport) {
		this.displayInReport = displayInReport;
	}

}
