package com.seleniumtests.reporter.logger;

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

		har.writeTo(Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), HAR_FILE_NAME).toFile());
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
