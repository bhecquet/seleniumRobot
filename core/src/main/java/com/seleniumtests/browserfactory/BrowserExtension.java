package com.seleniumtests.browserfactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;

import com.seleniumtests.core.TestVariable;
import com.seleniumtests.customexception.ConfigurationException;

import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestException;

public class BrowserExtension {
	
	private static final String EXTENSION_PATH_REGEX = "extension\\d+.path"; 
	private static final String EXTENSION_OPTIONS = ".options"; 
	private File extensionPath;
	private Map<String, String> options;
	
	/**
	 * Returns list of browser extensions from command line parameters
	 * @param variables
	 * @return
	 */
	public static List<BrowserExtension> getExtensions(Map<String, TestVariable> variables) {
		
		List<BrowserExtension> browserExtensions = new ArrayList<>();
		
		for (Entry<String, TestVariable> entry: variables.entrySet()) {
			if (entry.getKey().matches(EXTENSION_PATH_REGEX)) {
				String extKey = entry.getKey().split("\\.")[0];
				BrowserExtension extension = new BrowserExtension(entry.getValue().getValue());
				
				if (variables.containsKey(extKey + EXTENSION_OPTIONS)) {
					extension.setOptions(variables.get(extKey + EXTENSION_OPTIONS).getValue());
				}
				
				browserExtensions.add(extension);
			}
		}
		
		return browserExtensions;
		
	}
	
	public File getExtensionPath() {
		return extensionPath;
	}

	public Map<String, String> getOptions() {
		return options;
	}

	/**
	 * Create extension
	 * @param path		File path or HTTP url. In this case, file will be downloaded locally
	 */
	public BrowserExtension(String path) {
		
		if (path.startsWith("http")) {
			try {
				extensionPath = File.createTempFile("extension", "." + FilenameUtils.getExtension(new URL(path).getPath()));
				extensionPath.deleteOnExit();
				Unirest.get(path).asFile(extensionPath.getAbsolutePath());
			} catch (IOException | UnirestException e) {
				throw new ConfigurationException(String.format("Cannot download extenion at %s: %s", path, e.getMessage()));
			}
		} else {
			extensionPath = new File(path);
		}
		options = new HashMap<>();
	}
	
	/**
	 * Sets extension options: Browser extension options must have the format: <key1>=<value1>;<key2>=<value2>
	 * @param userOptions
	 */
	public void setOptions(String userOptions) {
		options = new HashMap<>();
		for (String option: userOptions.split(";")) {
			try {
				String[] optionValue = option.split("=");
				options.put(optionValue[0], optionValue[1]);
			} catch (IndexOutOfBoundsException e) {
				throw new ConfigurationException("Browser extension options must have the format: <key1>=<value1>;<key2>=<value2>");
			}
		}
	}

}
