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
import java.util.ArrayList;

import org.json.JSONObject;

public class GenericFile extends TestAction {

	private File file;
	
	public GenericFile(File file, String description) {
		super(description, false, new ArrayList<>(), true);
		this.file = file;

	}
	
	public String buildLog() {
		return String.format("%s: <a href='%s'>file</a>", name, file.getPath());
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
		return new GenericFile(file, encodeString(name, format));
	}

	public File getFile() {
		return file;
	}
}
