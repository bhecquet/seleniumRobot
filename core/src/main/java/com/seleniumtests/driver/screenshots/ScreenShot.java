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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.reporter.logger.FileContent;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.HashCodeGenerator;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Represents a screenshot (image + HTML source code when applicable)
 * Files are stored in the output directory of the test
 */
public class ScreenShot {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(ScreenShot.class);

    private String location;
    private FileContent image;
    private FileContent html;
    private String title;
    private long duration;
    private String outputDirectory;
    
    /**
     * File will be copied in <output_directory>/screenshots/<file_name>
     * @param imageBuffer
     */
    public ScreenShot(BufferedImage imageBuffer, String pageSource) {
    
        initializeOutputDirectory();
        
        String filename = HashCodeGenerator.getRandomHashCode("web");
        if (imageBuffer != null) {
            Path filePath = Paths.get(outputDirectory, ScreenshotUtil.SCREENSHOT_DIR, filename + ".png");
            FileUtility.writeImage(filePath.toString(), imageBuffer);
            this.image = new FileContent(filePath.toFile());
        }

        if (pageSource != null) {
            try {
                File htmlFile = Paths.get(outputDirectory, ScreenshotUtil.HTML_DIR, filename + ".html").toFile();
                FileUtils.writeStringToFile(htmlFile, pageSource, StandardCharsets.UTF_8);
                html = new FileContent(htmlFile);
            } catch (IOException e) {
                logger.warn("Ex", e);
            }
        }
    }
    
    /**
     * File will be copied in <output_directory>/screenshots/<file_name>
     * @param imageFile
     */
    public ScreenShot(File imageFile) {
        this(imageFile, null, ScreenshotUtil.SCREENSHOT_DIR);
    }
    public ScreenShot(File imageFile, File htmlFile) {
        this(imageFile, htmlFile, ScreenshotUtil.SCREENSHOT_DIR);
    }
    
    /**
     * File will be copied in <output_directory>/<relative_path>/<file_name>
     * @param imageFile
     */
    public ScreenShot(File imageFile, File htmlFile, String relativePath) {
        
        
    
        initializeOutputDirectory();
    
        // copy the input image file to <output_directory>/screenshots/<file_name>
        if (imageFile != null && imageFile.exists()) {
            Path filePath = Paths.get(outputDirectory, relativePath, imageFile.getName());
            filePath.getParent().toFile().mkdirs();
            try {
                Files.move(imageFile.toPath(), filePath, StandardCopyOption.REPLACE_EXISTING);
                this.image = new FileContent(filePath.toFile());
            } catch (Exception e) {
                throw new ScenarioException(String.format("Failed to move image file %s to %s: %s", imageFile.getAbsolutePath(), filePath.toFile().getAbsolutePath(), e.getMessage()));
            }
        }
    
        // copy the input HTML file to <output_directory>/htmls/<file_name>
        if (htmlFile != null && htmlFile.exists()) {
            Path htmlFilePath = Paths.get(outputDirectory, ScreenshotUtil.HTML_DIR, htmlFile.getName());
            htmlFilePath.getParent().toFile().mkdirs();
            try {
                Files.move(htmlFile.toPath(), htmlFilePath, StandardCopyOption.REPLACE_EXISTING);
                this.html = new FileContent(htmlFilePath.toFile());
            } catch (Exception e) {
                throw new ScenarioException(String.format("Failed to move html file %s to %s: %s", htmlFile.getAbsolutePath(), htmlFilePath.toFile().getAbsolutePath(), e.getMessage()));
            }
        }
    
        this.duration = 0;
    }
    
    private void initializeOutputDirectory() {
        if (SeleniumTestsContextManager.getGlobalContext().getTestNGContext() != null) {
            outputDirectory = SeleniumTestsContextManager.getThreadContext().getOutputDirectory();
        } else {
            throw new ScenarioException("Cannot create a screenshot outside of a test");
        }
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }
    
    /**
     * For test
     * @param outputDirectory
     */
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
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
    
    /**
     * Returns the relative path of HTML file (relative to outputDirectory)
     * @return
     */
    public String getHtmlSourcePath() {
        if (html != null) {
            return Paths.get(outputDirectory).relativize(Paths.get(html.getFile().getAbsolutePath())).toString().replace("\\", "/");
        } else {
            return null;
        }
        
    }

    /**
     * Get the image path relative to outputDirectory (directory where info for a specific test are recorded)
     * @return
     */
    public String getImagePath() {
        if (image != null) {
            return Paths.get(outputDirectory).relativize(Paths.get(image.getFile().getAbsolutePath())).toString().replace("\\", "/");
        } else {
            return null;
        }
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

    @Override
    public String toString() {
        return "|APPLICATION URL:" + this.location + "|PAGE TITLE:" + this.title
                + "|PAGE HTML SOURCE:" + this.getHtmlSourcePath() + "|PAGE IMAGE:" + this.getImagePath();
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
