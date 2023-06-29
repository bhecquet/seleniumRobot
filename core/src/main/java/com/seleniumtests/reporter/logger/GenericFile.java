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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.json.JSONObject;

import com.seleniumtests.core.SeleniumTestsContextManager;

public class GenericFile extends TestAction {

	private File file;
	private String relativeFilePath; // path relative to the root of test output directory
	

	public GenericFile(File file, String description) throws IOException {
		this(file, description, true);
	}
	
	/**
	 * Store a file in log folder
	 * @param file
	 * @param description
	 * @param move			if true, move the file to log folder
	 * @throws IOException
	 */
	public GenericFile(File file, String description, boolean move) throws IOException {
		super(description, false, new ArrayList<>());
		
		if (file == null || !file.exists()) {
			throw new FileNotFoundException("GenericFile needs a file");
		}
		
		// in case file is not in output directory, move it
		try {
			relativeFilePath = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()).relativize(file.toPath()).toString().replace("\\", "/");
		} catch (IllegalArgumentException e) {
			relativeFilePath = file.getName();
			move = true;
		}
		
		// move the file to the root of test specific output directory
		if (move) {
			File loggedFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), file.getName()).toFile();
			relativeFilePath = file.getName(); // correct relavtive path, as we moved the file
			
			try {
				Files.move(file.toPath(), loggedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				this.file = loggedFile;
			} catch (Exception e) {
				logger.error(String.format("Failed to move file %s to %s: %s", file.getAbsolutePath(), loggedFile.getAbsolutePath(), e.getMessage()));
				this.file = file;
			}
		
		} else {
			this.file = file;
		}

	}
	
	/**
	 * Build a string that can be used in HTML logs
	 * @return
	 */
	public String buildLog() {		
		return String.format("%s: <a href='%s'>file</a>", name, relativeFilePath);
    }
	

	/**
	 * Change path of the file
	 * @param outputDirectory
	 * @throws IOException
	 */
	public void relocate(String outputDirectory) throws IOException {
		if (outputDirectory == null) {
			return;
		}
		new File(outputDirectory).mkdirs();
		Path newPath = Paths.get(outputDirectory, file.getName());
		
		try {
			Files.move(file.toPath(), newPath, StandardCopyOption.REPLACE_EXISTING);
			file = newPath.toFile();
		} catch (Exception e) {
			logger.error(String.format("Failed to relocate file %s to %s: %s", file.getAbsolutePath(), newPath.toString(), e.getMessage()));
		}
	}
	

	@Override
	public JSONObject toJson() {
		JSONObject actionJson = new JSONObject();
		
		actionJson.put("type", "file");
		actionJson.put("name", name);
		actionJson.put("file", String.format(FILE_PATTERN, file.getAbsolutePath().replace("\\", "/")));
		
		return actionJson;
	}
	
	@Override
	public GenericFile encode(String format) {
		try {
			GenericFile genericFile = new GenericFile(file, encodeString(name, format), false);
			genericFile.relativeFilePath = this.relativeFilePath; // restore the path, as this method is called at the end of test suite, where thread context output directory point to the 
																	// root output directory. RelativePath would be lost
																	// we do not move file, so relativePath remains the same
			return genericFile;
		} catch (IOException e) {
			logger.error("Cannot encode file: " + e.getMessage());
			return this;
		}
	}

	public File getFile() {
		return file;
	}

	public String getRelativeFilePath() {
		return relativeFilePath;
	}
}
