/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
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
	
	@Test(groups={"ut"}, enabled=false)
	public void testDriverVersion() {
		String version = PackageUtility.getDriverVersion();
		System.out.println(version);
	}

	@Test(groups = "ut")
	public void testSeleniumVersion() {
		String version = PackageUtility.getSeleniumVersion();
		Assert.assertTrue(version.startsWith("4."));
        Assert.assertNotEquals(version, "4.37.0"); // we do not get the default version
	}
}
