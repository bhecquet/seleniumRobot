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
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import com.seleniumtests.core.SeleniumTestsContextManager;

public class GenericFile extends TestAction {

	private File file;
	

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
	private GenericFile(File file, String description, boolean move) throws IOException {
		super(description, false, new ArrayList<>());
		
		if (file == null || !file.exists()) {
			throw new FileNotFoundException("GenericFile needs a file");
		}
		
		if (move) {
			File loggedFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), file.getName()).toFile();
			if (!file.equals(loggedFile)) {
				if (loggedFile.exists()) {
					loggedFile.delete();
				}
				FileUtils.moveFile(file, loggedFile);
			}
			
			this.file = loggedFile;
		} else {
			this.file = file;
		}

	}
	
	public String buildLog() {
		return String.format("%s: <a href='%s'>file</a>", name, file.getName());
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
		Files.move(Paths.get(file.toString()), newPath);
		file = newPath.toFile();
	}
	

	@Override
	public JSONObject toJson() {
		JSONObject actionJson = new JSONObject();
		
		actionJson.put("type", "file");
		actionJson.put("name", name);
		actionJson.put("file", this.file.getAbsolutePath());
		
		return actionJson;
	}
	
	@Override
	public GenericFile encode(String format) {
		try {
			return new GenericFile(file, encodeString(name, format), false);
		} catch (IOException e) {
			logger.error("Cannot encode file: " + e.getMessage());
			return this;
		}
	}

	public File getFile() {
		return file;
	}
}
