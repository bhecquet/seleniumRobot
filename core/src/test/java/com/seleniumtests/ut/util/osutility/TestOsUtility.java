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
package com.seleniumtests.ut.util.osutility;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.util.osutility.OSUtility;

public class TestOsUtility extends GenericTest {
	
	@Test(groups={"ut"})
	public void testExtractFirefoxVersion() {
		Assert.assertEquals(OSUtility.extractFirefoxVersion("Mozilla Firefox 52.0\n"), "52.0");
	}
	
	@Test(groups={"ut"})
	public void testExtractIceweaselVersion() {
		Assert.assertEquals(OSUtility.extractFirefoxVersion("Mozilla Iceweasel 38.8.0"), "38.8");
	}
	
	@Test(groups={"ut"})
	public void testExtractFirefoxPatchVersion() {
		Assert.assertEquals(OSUtility.extractFirefoxVersion("Mozilla Firefox 52.0.1"), "52.0");
	}
	
	@Test(groups={"ut"})
	public void testExtractFirefoxUnknownVersion() {
		Assert.assertEquals(OSUtility.extractFirefoxVersion("Firefox 52.0.1"), "");
	}
	
	@Test(groups={"ut"})
	public void testExtractChromeVersion() {
		Assert.assertEquals(OSUtility.extractChromeVersion("Google Chrome 57.0.2987.110\n"), "57.0");
	}
	
	@Test(groups={"ut"})
	public void testExtractChromeVersionNotFound() {
		Assert.assertEquals(OSUtility.extractChromeVersion("57.0.2987.110\n"), "");
	}
	
	@Test(groups={"ut"})
	public void testExtractChromiumVersion() {
		Assert.assertEquals(OSUtility.extractChromiumVersion("Chromium 57.0.2924.76 Built on Ubuntu , running on Ubuntu 16.04\n"), "57.0");
	}
	
	@Test(groups={"ut"})
	public void testExtractChromiumVersionNotFound() {
		Assert.assertEquals(OSUtility.extractChromiumVersion("Chromium 56"), "");
	}
	
	@Test(groups={"ut"})
	public void testExtractIEVersion() {
		Assert.assertEquals(OSUtility.extractIEVersion("11.0.9600.18499"), "11");
	}
	
	@Test(groups={"ut"})
	public void testExtractIEVersionNotFound() {
		Assert.assertEquals(OSUtility.extractIEVersion("Internet Explorer 11"), "Internet Explorer 11");
	}
	
	@Test(groups={"ut"})
	public void testExtractEdgeVersion() {
		Assert.assertEquals(OSUtility.extractEdgeVersion("10240.th1.160802-1852"), "10240");
	}
	
	@Test(groups={"ut"})
	public void testExtractEdgeVersionNotFound() {
		Assert.assertEquals(OSUtility.extractEdgeVersion("Edge 10240"), "Edge 10240");
	}
	
	
}
