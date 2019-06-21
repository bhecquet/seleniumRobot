package com.seleniumtests.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

public class StatisticsStorage {
	
	public static class DriverUsage {
		
		public static final String GRID_HUB = "gridHub";
		public static final String GRID_NODE = "gridNode";
		public static final String START_TIME = "startTime";
		public static final String DURATION = "duration";
		public static final String SESSION_ID = "sessionId";
		public static final String BROWSER = "browser";
		public static final String STARTUP_DURATION = "startupDuration";
		public static final String TEST_NAME = "testName";
		
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
