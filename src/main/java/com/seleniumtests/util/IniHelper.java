package com.seleniumtests.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Set;

import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.seleniumtests.customexception.ConfigurationException;

public class IniHelper {

	/**
	 * @author Sophie
	 * @param fileToRead
	 *            file where are the targeted data
	 * @param hashMapToComplete
	 *            hashMap we want to fill
	 * @return the HashMap completed with data from the file
	 */
	public static HashMap<String, HashMap<String,String>> readIniFile(File fileToRead, HashMap<String, HashMap<String, String>> hashMapToComplete) {
		
		try {
			Ini ini = new Ini();
			Config conf = ini.getConfig();
			conf.setGlobalSection(true);
			conf.setFileEncoding(Charset.forName("UTF-8"));
			ini.setConfig(conf);
			ini.load(fileToRead);
			Set<Ini.Entry<String, Ini.Section>> sections = ini.entrySet();
			HashMap<String, String> inter=new HashMap<String, String>();
			String actualSection = "";
			
			for (Ini.Entry<String, Ini.Section> section : sections) {
				    inter = new HashMap<String,String>(); //recreate inter
				    actualSection = section.getKey();
				    if(hashMapToComplete.containsKey(actualSection)){
				    	inter.putAll(hashMapToComplete.get(actualSection)); //recup datas already read
				    }
					for (Ini.Entry<String, String> sectionOption : section.getValue().entrySet()) {
						inter.put(sectionOption.getKey(), sectionOption.getValue());
					}
					hashMapToComplete.put(actualSection, inter);
			}
		} catch (InvalidFileFormatException e) {
			throw new ConfigurationException("Invalid file: " + fileToRead);
		} catch (IOException e) {
			throw new ConfigurationException(String.format("File does not exist %s: %s", fileToRead , e.getMessage()));
		}
		return hashMapToComplete;
	}
}
