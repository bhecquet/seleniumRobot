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
package com.seleniumtests.browserfactory.mobile;

import java.util.List;

public class MobileDevice {

	private String name;
	private String id;
	private String platform;
	private String version;
	private List<String> browsers;
	
	public MobileDevice(String name, String id, String platform, String version, List<String> browsers) {
		this.name = name;
		this.id = id;
		this.platform = platform;
		this.version = version;
		this.browsers = browsers;
	}
	
	public List<String> getBrowsers() {
		return browsers;
	}

	public String getName() {
		return name;
	}
	
	public String getId() {
		return id;
	}
	
	public String getPlatform() {
		return platform;
	}
	
	public String getVersion() {
		return version;
	}
	
	@Override
	public String toString() {
		return String.format("name: %s, id: %s, platform: %s, version: %s, browsers: %s", name, id, platform, version, browsers.toString());
	}
}
