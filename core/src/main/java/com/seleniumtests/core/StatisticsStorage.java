package com.seleniumtests.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

public class StatisticsStorage {
	
	private StatisticsStorage() {
		// do nothing
	}
	
	public static class DriverUsage {
		
		
		
		String gridHub;
		String gridNode;
		Long startTime;
		Long durationV;
		String sessionId;
		String browserName;
		Long startupDuration;
		String testName;
		
		public DriverUsage(String gridHub, String gridNode, Long startTime, Long duration, String sessionId, String browser, Long startupDuration, String testName) {
			this.gridHub = gridHub;
			this.gridNode = gridNode;
			this.startTime = startTime;
			this.durationV = duration;
			this.sessionId = sessionId;
			this.browserName = browser;
			this.startupDuration = startupDuration;
			this.testName = testName;
		}
		
		public String asJson() {
			return new JSONObject(this).toString();
		}
		
		public String getGridHub() {
			return gridHub;
		}

		public String getGridNode() {
			return gridNode;
		}

		public long getStartTime() {
			return startTime;
		}

		public long getDuration() {
			return durationV;
		}

		public String getSessionId() {
			return sessionId;
		}

		public String getBrowserName() {
			return browserName;
		}

		public Long getStartupDuration() {
			return startupDuration;
		}

		public String getTestName() {
			return testName;
		}

	}

	private static List<DriverUsage> driverUsage = Collections.synchronizedList(new ArrayList<>());
	
	public static List<DriverUsage> getDriverUsage() {
		return driverUsage;
	}

	public static void addDriverUsage(DriverUsage usage) {
		driverUsage.add(usage);
	}
	
	public static void reset() {
		driverUsage.clear();
	}
}
