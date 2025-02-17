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
import java.nio.file.Paths;
import java.util.ArrayList;

import com.seleniumtests.util.har.Har;
import org.json.JSONObject;

import com.seleniumtests.core.SeleniumTestsContextManager;

public class HarCapture extends TestAction {

	private Har har;
	private FileContent harFile;
	private static final String HAR_FILE_NAME = "networkCapture.har";
	
	public HarCapture(Har har, String name, FileContent harFile) {

		super(name, false, new ArrayList<>());
		this.har = har;
		this.harFile = harFile;
	}
	
	public HarCapture(Har har, String name) throws IOException {
		super(name, false, new ArrayList<>());
		this.har = har;
		File f = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), name + "-" + HAR_FILE_NAME).toFile();
		f.getParentFile().mkdirs();
		harFile = new FileContent(f);
		
		har.writeTo(harFile.getFile());

		logger.info("HAR capture file copied to " + harFile.getFile().getAbsolutePath());
	}
	
	public String buildHarLog() {
		return String.format("Network capture '%s' browser: <a href='%s-%s'>HAR file</a>", name, name, HAR_FILE_NAME);
    }

	@Override
	public JSONObject toJson() {
		JSONObject actionJson = new JSONObject();
		
		actionJson.put("type", "networkCapture");
		actionJson.put("name", name);
		actionJson.put("id", harFile.getId() == null ? JSONObject.NULL: harFile.getId());
		
		return actionJson;
	}

	public Har getHarFile() {
		return har;
	}
	
	public File getFile() {
		return harFile.getFile();
	}
	
	public FileContent getFileContent() {
		return harFile;
	}
	
	public HarCapture encode() {
		return new HarCapture(har, name, harFile);
	}

}
