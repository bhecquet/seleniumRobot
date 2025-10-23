/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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

import org.json.JSONObject;

import com.seleniumtests.core.SeleniumTestsContextManager;

public class GenericFile extends TestAction {

	public enum FileOperation {
		COPY, // copy the file to test output directory
		MOVE, // move the file to test output directory
		KEEP // don't do anything on file, keep it where it is
	}

	private FileContent file;
	private String relativeFilePath; // path relative to the root of test output directory
	

	public GenericFile(File file, String description) throws IOException {
		this(file, description, FileOperation.MOVE);
	}
	public GenericFile(FileContent file, String description, String relativeFilePath)  {
		super(description, false, new ArrayList<>());
		this.file = file;
		this.relativeFilePath = relativeFilePath;
	}
	
	/**
	 * Store a file in log folder
	 * @param file			the file to add for reporting
	 * @param description	what the file represents
	 * @param fileOperation	copy / move / keep file
	 */
	public GenericFile(File file, String description, FileOperation fileOperation) throws IOException {
		super(description, false, new ArrayList<>());
		
		if (file == null || !file.exists()) {
			throw new FileNotFoundException("GenericFile needs a file");
		}
		
		// in case file is not in output directory, copy it (we expect original file to be deleted on exit)
		try {
			relativeFilePath = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()).relativize(file.toPath()).toString().replace("\\", "/");
		} catch (IllegalArgumentException e) {
			relativeFilePath = file.getName();
			fileOperation = FileOperation.COPY;
		}
		
		// move the file to the root of test specific output directory
		if (fileOperation == FileOperation.MOVE || fileOperation == FileOperation.COPY) {
			File loggedFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), file.getName()).toFile();
			relativeFilePath = file.getName(); // correct relavtive path, as we moved the file

			try {
				loggedFile.mkdirs();
				if (fileOperation == FileOperation.MOVE) {
					Files.move(file.toPath(), loggedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} else {
					Files.copy(file.toPath(), loggedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
				this.file = new FileContent(loggedFile);
			} catch (Exception e) {
				logger.error(String.format("Failed to move file %s to %s: %s", file.getAbsolutePath(), loggedFile.getAbsolutePath(), e.getMessage()));
				this.file = new FileContent(file);
			}

		} else {
			this.file = new FileContent(file);
		}

	}
	
	/**
	 * return a string that can be used in HTML logs
	 */
	public String buildLog() {		
		return String.format("%s: <a href='%s'>file</a>", name, relativeFilePath);
    }

	

	@Override
	public JSONObject toJson() {
		JSONObject actionJson = new JSONObject();
		
		actionJson.put("type", "file");
		actionJson.put("name", getName());
		actionJson.put("id", file.getId() == null ? JSONObject.NULL: file.getId());
		
		return actionJson;
	}
	
	@Override
	public GenericFile encodeTo(String format) {
		GenericFile genericFileToEncode = new GenericFile(file, name, relativeFilePath);
		return encode(format, genericFileToEncode);
	}

	private GenericFile encode(String format, GenericFile fileToEncode) {
		super.encode(format, fileToEncode);
		return fileToEncode;
	}

	public File getFile() {
		return file.getFile();
	}
	public FileContent getFileContent() {
		return file;
	}

	public String getRelativeFilePath() {
		return relativeFilePath;
	}
}
