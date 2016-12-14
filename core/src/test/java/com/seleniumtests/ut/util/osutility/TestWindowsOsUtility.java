package com.seleniumtests.ut.util.osutility;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;

public class TestWindowsOsUtility extends GenericTest {
	
	@BeforeClass
	public void isWindows() {
		if (!OSUtility.isWindows()) {
			throw new SkipException("Test only available on Windows platform");
		}
	}

	@Test(groups={"ut"})
	public void testGetBuild() {
		Assert.assertNotEquals(OSUtilityFactory.getInstance().getOSBuild(), 5000);
	}
}
