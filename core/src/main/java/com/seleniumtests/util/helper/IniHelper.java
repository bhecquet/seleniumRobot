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
package com.seleniumtests.util.helper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.seleniumtests.customexception.ConfigurationException;

public class IniHelper {

	private IniHelper() {
	}
	/**
	 * @author Sophie
	 * @param fileToRead
	 *            file where are the targeted data
	 * @param hashMapToComplete
	 *            hashMap we want to fill
	 * @return the HashMap completed with data from the file
	 */
	public static Map<String, HashMap<String, String>> readIniFile(File fileToRead,
			Map<String, HashMap<String, String>> hashMapToComplete) {

		try {
			Ini ini = new Ini();
			Config conf = ini.getConfig();
			conf.setGlobalSection(true);
			conf.setFileEncoding(Charset.forName("UTF-8"));
			ini.setConfig(conf);
			ini.load(fileToRead);
			Set<Ini.Entry<String, Ini.Section>> sections = ini.entrySet();

			for (Ini.Entry<String, Ini.Section> section : sections) {
				HashMap<String, String> inter = new HashMap<>(); 
				String actualSection = section.getKey();
				if (hashMapToComplete.containsKey(actualSection)) {
					inter.putAll(hashMapToComplete.get(actualSection)); // recup datas already read
				}
				for (Ini.Entry<String, String> sectionOption : section.getValue().entrySet()) {
					inter.put(sectionOption.getKey(), sectionOption.getValue());
				}
				hashMapToComplete.put(actualSection, inter);
			}
		} catch (InvalidFileFormatException e) {
			throw new ConfigurationException("Invalid file: " + fileToRead);
		} catch (IOException e) {
			throw new ConfigurationException(String.format("File does not exist %s: %s", fileToRead, e.getMessage()));
		}
		return hashMapToComplete;
	}
}
