/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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

package com.seleniumtests.reporter;

import java.io.InputStream;

import com.seleniumtests.customexception.ConfigurationException;

public class ReportInfo {
	String prefix;
	String extension;
	String templatePath;
	
	/**
	 * From String with format <prefix>::<extension>::<template> (e.g: PERF::xml::reporter/templates/report.perf.vm), generate a ReportInfo
	 * @return
	 */
	public ReportInfo(String reportInfoStr) {
		try {
			String[] elements = reportInfoStr.trim().split("::");
			prefix = elements[0];
			extension = "." + elements[1];
			templatePath = elements[2];
		} catch (IndexOutOfBoundsException e) {
			throw new ConfigurationException(String.format("Template %s does not follow the format <prefix>::<extension>::<template>", reportInfoStr));
		}
		
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(templatePath);
		if (stream == null) {
			throw new ConfigurationException(String.format("Template %s does not exist in resources", templatePath));
		}
	}
	
	public String getPrefix() {
		return prefix;
	}
	public String getExtension() {
		return extension;
	}
	public String getTemplatePath() {
		return templatePath;
	}
	
	
}
