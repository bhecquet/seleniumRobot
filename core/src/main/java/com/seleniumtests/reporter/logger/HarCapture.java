/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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

import org.json.JSONObject;

import com.seleniumtests.core.SeleniumTestsContextManager;

import net.lightbody.bmp.core.har.Har;

public class HarCapture extends TestAction {

	private Har harFile;
	private static final String HAR_FILE_NAME = "networkCapture.har";
	
	public HarCapture(Har har) throws IOException {
		super(har.getLog().getPages().get(0).getTitle(), false, new ArrayList<>());
		harFile = har;
		File harFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), HAR_FILE_NAME).toFile();
		
		har.writeTo(harFile);

		logger.info("HAR capture file copied to " + harFile.getAbsolutePath());
	}
	
	public String buildHarLog() {
		return String.format("Network capture: <a href='%s'>HAR file</a>", HAR_FILE_NAME);
    }
	

	@Override
	public JSONObject toJson() {
		JSONObject actionJson = new JSONObject();
		
		actionJson.put("type", "networkCapture");
		actionJson.put("name", name);
		
		return actionJson;
	}

	public Har getHarFile() {
		return harFile;
	}
}
