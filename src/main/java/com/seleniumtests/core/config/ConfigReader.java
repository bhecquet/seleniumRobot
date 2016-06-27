/*
 * Copyright 2016 www.infotel.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seleniumtests.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestLogging;
import com.seleniumtests.customexception.ConfigurationException;

public class ConfigReader {
	
	private static final String GLOBAL_SECTION_NAME = "General";
	private static final Logger logger = TestLogging.getLogger(ConfigReader.class);

	public HashMap<String, String> readConfig(InputStream iniFileStream) {
		return readConfig(iniFileStream, SeleniumTestsContextManager.getThreadContext().getTestEnv());
	}
	
	public HashMap<String, String> readConfig(InputStream iniFileStream, String environment) {
		
		HashMap<String, String> testConfig = new HashMap<String, String>();
	
		try {
			Ini ini = new Ini();
			Config conf = ini.getConfig();
			conf.setGlobalSection(true);
			conf.setGlobalSectionName(GLOBAL_SECTION_NAME);
			conf.setFileEncoding(Charset.forName("UTF-8"));
			ini.setConfig(conf);
			ini.load(iniFileStream);
			
			Set<Ini.Entry<String, Ini.Section>> sections = ini.entrySet();
			
			// first get global options
			for (Ini.Entry<String, Ini.Section> section: sections) {
				
				if (section.getKey().equals(GLOBAL_SECTION_NAME)) {
					for (Ini.Entry<String, String> sectionOption: section.getValue().entrySet()) {
						testConfig.put(sectionOption.getKey(), sectionOption.getValue());
					}
				}
			}
			
			// then overwrite global options with local ones
			boolean envFound = false;
			for (Ini.Entry<String, Ini.Section> section: sections) {
				
				if (section.getKey().equals(environment)) {
					envFound = true;
					for (Ini.Entry<String, String> sectionOption: section.getValue().entrySet()) {
						testConfig.put(sectionOption.getKey(), sectionOption.getValue());
					}
				}
			}
			if (!envFound) {
				logger.warn(String.format("No section in config.ini file matches the environment '%s'", environment));
			}
			
			return testConfig;
		} catch (InvalidFileFormatException e) {
			throw new ConfigurationException("Invalid file: " + iniFileStream);
		} catch (IOException e) {
			throw new ConfigurationException(String.format("File does not exist %s: %s", iniFileStream, e.getMessage()));
		}
	}
}
