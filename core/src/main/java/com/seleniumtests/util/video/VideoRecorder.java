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
package com.seleniumtests.util.video;

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

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

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
	private JLabel label;
	private JFrame window;
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
				startStepDisplay(gc);
				screenRecorder = new ScreenRecorder(gc, 
								null,
								new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI),
								new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 24, FrameRateKey, Rational.valueOf(25), QualityKey, 1.0f, KeyFrameIntervalKey, 15 * 60),
								new Format(FrameRateKey, Rational.valueOf(25), EncodingKey, ScreenRecorder.ENCODING_BLACK_CURSOR),
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
	 * Display the JFrame
	 */
	private void startStepDisplay(GraphicsConfiguration gc) {
		Rectangle screenBounds = gc.getBounds();
		
		window = new JFrame("SeleniumRobot"); 
		window.setAlwaysOnTop (true);
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.setUndecorated(true); 
		try {
			window.setOpacity(0.7f);
		} catch (UnsupportedOperationException e) {
			// nothing to do
		}
		window.setBounds(screenBounds.width / 3, 0, screenBounds.width * 2 / 3 - 200, 25);
		window.setLayout(new BorderLayout());
		label = new JLabel("Starting");
		label.setFont (label.getFont ().deriveFont (12.0f));
		window.add(label, BorderLayout.CENTER);

		window.setVisible(true);

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
			
			window.dispose();
			screenRecorder.stop();
			List<File> createdFiles = screenRecorder.getCreatedMovieFiles();
			if (!createdFiles.isEmpty()) {
				File lastFile = createdFiles.get(createdFiles.size() - 1);
				File videoFile = Paths.get(folderPath.getAbsolutePath(), fileName).toFile();
				FileUtils.copyFile(lastFile, videoFile);
				
				// remove temp files
				for (File f: createdFiles) {
					try {
						Files.delete(Paths.get(f.getAbsolutePath()));
					} catch (IOException e) {
						logger.info("could not delete video temp file: " + e.getMessage());
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
	
	public void displayRunningStep(String stepName) {	
		if (label != null) {
			label.setText(stepName);
		}
	}

	public String getFileName() {
		return fileName;
	}

	public File getFolderPath() {
		return folderPath;
	}

	public JLabel getLabel() {
		return label;
	}

	public JFrame getWindow() {
		return window;
	}

}
