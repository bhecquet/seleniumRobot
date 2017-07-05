package com.seleniumtests.ut.util;

import java.io.InputStream;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.util.PackageUtility;

public class TestPackageUtility extends GenericTest {


	@Test(groups={"ut"})
	public void testPomVersionReadingWithParent() throws Exception {
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("tu/pomWithParent.xml");
		String version = PackageUtility.getVersionFromPom(stream);
		Assert.assertEquals(version, "2.7.0-SNAPSHOT");
	}
	
	@Test(groups={"ut"})
	public void testPomVersionReadingWithoutParent() throws Exception {
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("tu/pomWithoutParent.xml");
		String version = PackageUtility.getVersionFromPom(stream);
		Assert.assertEquals(version, "2.7.0-SNAPSHOT");
	}
}
