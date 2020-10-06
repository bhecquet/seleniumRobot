package com.seleniumtests.connectors.tms.reportportal;

import com.epam.reportportal.testng.BaseTestNGListener;
import com.epam.reportportal.testng.ITestNGService;

public class ReportPortalTestListener extends BaseTestNGListener {
	
	static {
		System.setProperty("rp.endpoint", "http://zld7205v:8082");
		System.setProperty("rp.uuid", "4e402de0-b6da-438b-b0b2-5d7c2c96482a");
		System.setProperty("rp.launch", "s047432_TEST_EXAMPLE");
		System.setProperty("rp.project", "s047432_personal");
	}

	public ReportPortalTestListener(ITestNGService testNgService) {
		super(testNgService);
	}
	
	public ReportPortalTestListener() {
		super(new ReportPortalService());
	}

}
