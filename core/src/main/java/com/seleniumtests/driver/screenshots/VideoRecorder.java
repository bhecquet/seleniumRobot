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

import static org.monte.media.FormatKeys.EncodingKey;
import static org.monte.media.FormatKeys.FrameRateKey;
import static org.monte.media.FormatKeys.KeyFrameIntervalKey;
import static org.monte.media.FormatKeys.MIME_AVI;
import static org.monte.media.FormatKeys.MediaTypeKey;
import static org.monte.media.FormatKeys.MimeTypeKey;
import static org.monte.media.VideoFormatKeys.CompressorNameKey;
import static org.monte.media.VideoFormatKeys.DepthKey;
import static org.monte.media.VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
import static org.monte.media.VideoFormatKeys.QualityKey;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.monte.media.Format;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;
import org.monte.screenrecorder.ScreenRecorder.State;

import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;


public class VideoRecorder {
	
	private ScreenRecorder screenRecorder;
	private String fileName;
	private File folderPath;
	private static final Logger logger = SeleniumRobotLogger.getLogger(VideoRecorder.class);
	
	/**
	 * Constructor for grid mode only.
	 */
	public VideoRecorder(File folderPath, String fileName) {
		this(folderPath, fileName, true);
	}
	
	/**
	 * 
	 * @param folderPath
	 * @param fileName
	 * @param localRecording		if true, we will capture something locally. Else grid mode is used
	 */
	public VideoRecorder(File folderPath, String fileName, boolean localRecording) {
	
		
		//Create a instance of ScreenRecorder with the required configurations
		if (localRecording) {
			//Create a instance of GraphicsConfiguration to get the Graphics configuration
			//of the Screen. This is needed for ScreenRecorder class.
			GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
			
			try {
				screenRecorder = new ScreenRecorder(gc, 
								null,
								new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI),
								new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, (int)24, FrameRateKey, Rational.valueOf(25), QualityKey, 1.0f, KeyFrameIntervalKey, (int) (15 * 60)),
								null,
								null,
								folderPath);
			} catch (Exception e) {
				logger.error("Error while creating video recording", e);
			}
		}
		
		this.fileName = fileName;
		this.folderPath = folderPath;
	}
	
	/**
	 * Start capture. Cancel previous one if it exists
	 */
	public void start() {
		if (screenRecorder == null) {
			throw new ScenarioException("recorder is null!. do not use the default constructor");
		}
		
		try {
			if (screenRecorder.getState() == State.RECORDING) {
				screenRecorder.stop();
			}
			screenRecorder.start();
		} catch (IOException e) {
			logger.error("error while starting capture", e);
		}
	}
	
	public File stop() throws IOException {
		if (screenRecorder == null) {
			throw new ScenarioException("recorder is null!. do not use the default constructor");
		}
		
		if (screenRecorder.getState() == State.RECORDING) {
		
			screenRecorder.stop();
			List<File> createdFiles = screenRecorder.getCreatedMovieFiles();
			if (!createdFiles.isEmpty()) {
				File lastFile = createdFiles.get(createdFiles.size() - 1);
				File videoFile = Paths.get(folderPath.getAbsolutePath(), fileName).toFile();
				FileUtils.copyFile(lastFile, videoFile);
				
				// remove temp files
				for (File f: createdFiles) {
					if (!f.delete()) {
						logger.info("could not delete video temp file");
					}
				}
				
				return videoFile;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public String getFileName() {
		return fileName;
	}

	public File getFolderPath() {
		return folderPath;
	}

}
