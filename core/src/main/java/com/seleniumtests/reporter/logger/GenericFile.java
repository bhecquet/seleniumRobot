package com.seleniumtests.reporter.logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.json.JSONObject;

import com.seleniumtests.core.SeleniumTestsContextManager;

import net.lightbody.bmp.core.har.Har;

public class GenericFile extends TestAction {

	private File file;
	
	public GenericFile(File file, String description) {
		super(description, false, new ArrayList<>());
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
