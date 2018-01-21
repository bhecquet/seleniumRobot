package com.seleniumtests.it.driver.support;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Factory;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestDriver;
import com.seleniumtests.util.osutility.OSUtilityFactory;

public class MultiBrowserTestFactory {
	@Factory
	public Object[] createInstances() throws Exception {
		List<Object> result = new ArrayList<>();
		
		for (BrowserType browser: OSUtilityFactory.getInstance().getInstalledBrowsers()) {
			if (browser == BrowserType.NONE || browser == BrowserType.PHANTOMJS) {
				continue;
			}
			result.add(new TestDriver(browser.getBrowserType()));
		}

		return result.toArray();
	}

}
