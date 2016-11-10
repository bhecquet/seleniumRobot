package com.seleniumtests.browserfactory.mobile;

public class MobileDevice {

	private String name;
	private String id;
	private String platform;
	private String version;
	
	public MobileDevice(String name, String id, String platform, String version) {
		this.name = name;
		this.id = id;
		this.platform = platform;
		this.version = version;
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
}
