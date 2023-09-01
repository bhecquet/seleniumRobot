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
package com.seleniumtests.driver.screenshots;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import com.seleniumtests.reporter.logger.FileContent;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class ScreenShot {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(ScreenShot.class);

    private String location;
    private FileContent image;
    private FileContent html;
    private String title;
    private String suiteName;
    private long duration;
    private boolean isException;
    private String outputDirectory;

    public ScreenShot() {
        this(null);
    }
    
    public ScreenShot(String imagePath) {
    	if (SeleniumTestsContextManager.getGlobalContext().getTestNGContext() != null) {
            suiteName = SeleniumTestsContextManager.getGlobalContext().getTestNGContext().getSuite().getName();
            outputDirectory = SeleniumTestsContextManager.getThreadContext().getOutputDirectory();
        }
    	
    	if (imagePath != null) {
            this.image = new FileContent(Paths.get(outputDirectory, imagePath).toFile());
        }
    
    	this.duration = 0;
    }

    public boolean isException() {
        return isException;
    }

    public void setException(final boolean isException) {
        this.isException = isException;
    }

    /**
     * @deprecated not used anymore
     * @return
     */
    @Deprecated
    public String getSuiteName() {
        return suiteName;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(final String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setSuiteName(final String suiteName) {
        this.suiteName = suiteName;
    }

    /**
     * URL of the page for which this screenshot has been taken
     * @return
     */
    public String getLocation() {
        return location;
    }
    
    public String getImageName() {
    	if (image != null) {
    		return image.getFile().getName();
    	} else {
    		return "";
    	}
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setHtmlSourcePath(String htmlSourcePath) {
        this.html = new FileContent(Paths.get(outputDirectory, htmlSourcePath).toFile());
    }

    public void setImagePath(String imagePath) {
        this.image = new FileContent(Paths.get(outputDirectory, imagePath).toFile());
    }

    public String getHtmlSourcePath() {
        return Paths.get(outputDirectory).relativize(Paths.get(html.getFile().getAbsolutePath())).toString();
    }
    
    public String getHtmlSource() {
    	if (html != null) {
    		try {
				return FileUtils.readFileToString(html.getFile(), StandardCharsets.UTF_8);
			} catch (IOException e) {
				logger.error("cannot read source file", e);
				return "";
			}
    	} else {
    		return "";
    	}
    }

    /**
     * Get the image path relative to outputDirectory (directory where info for a specific test are recorded)
     * @return
     */
    public String getImagePath() {
        return Paths.get(outputDirectory).relativize(Paths.get(image.getFile().getAbsolutePath())).toString();
    }

    /**
     * Get the title of the page associated to this screenshot
     * @return
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getFullImagePath() {
        if (image != null) {
            return image.getFile().getAbsolutePath().replace("\\", "/");
        } else {
            return null;
        }
    }

    public String getFullHtmlPath() {
        if (html != null) {
        	return html.getFile().getAbsolutePath().replace("\\", "/");
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "!!!EXCEPTION:" + this.isException + "|APPLICATION URL:" + this.location + "|PAGE TITLE:" + this.title
                + "|PAGE HTML SOURCE:" + this.getFullHtmlPath() + "|PAGE IMAGE:" + this.getFullImagePath();
    }

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	/**
	 * Move physically the image to 'destOutputDirectory'
	 * @param destOutputDirectory		the output directory to copy to. It's a root directory where test results / image / html / video are stored
	 * @throws IOException
	 */
	public void relocate(String destOutputDirectory) throws IOException {
		relocate(destOutputDirectory, getImagePath(), getHtmlSourcePath());
	}
	
	/**
	 * Move physically the image to 'destOutputDirectory'
	 * @param destOutputDirectory		the output directory to copy to. It's a root directory where test results / image / html / video are stored
	 * @param newImagePath				the new path of the image if it needs to be moved inside the output directory
     * @param newHtmlPath               the new path of the html if it needs to be moved inside the output directory
	 * @throws IOException
	 */
	public void relocate(String destOutputDirectory, String newImagePath, String newHtmlPath) throws IOException {

		new File(destOutputDirectory).mkdirs();
		
		if (html != null) {
		    html.relocate(destOutputDirectory, newHtmlPath);
		}
		
		if (image != null) {
			image.relocate(destOutputDirectory, newImagePath);
		}
		
		outputDirectory = destOutputDirectory;
		
	}
	
	public FileContent getImage() {
	    return image;
    }
    
    public FileContent getHtml() {
	    return html;
    }
}
