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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class ScreenShot {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(ScreenShot.class);

    private String location;
    private String htmlSourcePath;
    private String imagePath;
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
    	
    	this.imagePath = imagePath;
    	this.duration = 0;
    }

    public boolean isException() {
        return isException;
    }

    public void setException(final boolean isException) {
        this.isException = isException;
    }

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

    public void setLocation(String location) {
        this.location = location;
    }

    public void setHtmlSourcePath(String htmlSourcePath) {
        this.htmlSourcePath = htmlSourcePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getHtmlSourcePath() {
        return htmlSourcePath;
    }
    
    public String getHtmlSource() {
    	if (htmlSourcePath != null) {
    		try {
				return FileUtils.readFileToString(new File(outputDirectory + "/" + htmlSourcePath));
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
        return imagePath;
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
        if (imagePath != null) {
            return outputDirectory + "/" + imagePath;
        } else {
            return null;
        }
    }

    public String getFullHtmlPath() {
        if (htmlSourcePath != null) {
            return outputDirectory + "/" + htmlSourcePath;
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
	 * @param destOutputDirectory
	 * @throws IOException
	 */
	public void relocate(String destOutputDirectory) throws IOException {

		new File(destOutputDirectory).mkdirs();
		
		if (htmlSourcePath != null) {
			File htmlSrc = Paths.get(outputDirectory, htmlSourcePath).toFile().getCanonicalFile();
			File newHtmlSrc = new File(htmlSrc.getAbsolutePath().replace("\\",  "/").replace(outputDirectory, destOutputDirectory));
			try {
				FileUtils.moveFile(htmlSrc, newHtmlSrc);
			} catch (FileExistsException e) {
				// nothing to do, file is there
			}
			htmlSourcePath = Paths.get(destOutputDirectory).relativize(Paths.get(newHtmlSrc.getAbsolutePath())).toString().replace("\\",  "/");
		}
		
		if (imagePath != null) { 
			File imgSrc = Paths.get(outputDirectory, imagePath).toFile().getCanonicalFile();
			File newImgSrc = new File(imgSrc.getAbsolutePath().replace("\\",  "/").replace(outputDirectory, destOutputDirectory));
			try {
				FileUtils.moveFile(imgSrc, newImgSrc);
			} catch (FileExistsException e) {
				// nothing to do, file is there
			}
			imagePath = Paths.get(destOutputDirectory).relativize(Paths.get(newImgSrc.getAbsolutePath())).toString().replace("\\",  "/");
		}
		
		outputDirectory = destOutputDirectory;
		
	}
}
