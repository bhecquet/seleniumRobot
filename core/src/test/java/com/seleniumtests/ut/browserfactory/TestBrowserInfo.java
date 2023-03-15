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
package com.seleniumtests.ut.browserfactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.SystemUtils;
import org.mockito.Mock;
import org.openqa.selenium.Platform;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.util.osutility.OSUtility;

@PrepareForTest({BrowserInfo.class, OSUtility.class})
public class TestBrowserInfo extends MockitoTest {
	
	@Mock
	private Stream<Path> streamPaths;

	@Test(groups={"ut"})
	public void getDriverFiles() throws IOException {
		Assert.assertFalse(new BrowserInfo(BrowserType.CHROME, "58.0", null).getDriverFiles().isEmpty());
	}
	
	/**
	 * Test that if a wrong version format is provided (parsable as float), returned version is 0.0
	 * Else, version is not touched
	 */
	@Test(groups= {"ut"})
	public void testWrongVersion() {
		BrowserInfo bi = new BrowserInfo(BrowserType.CHROME, "58.alpha", null);
		Assert.assertEquals(bi.getVersion(), "0.0");
	}
	
	@Test(groups= {"ut"})
	public void testToString() {
		BrowserInfo bi = new BrowserInfo(BrowserType.CHROME, "58", "/usr/bin/chrome", false);
		Assert.assertEquals(bi.toString(), "CHROME v58 [/usr/bin/chrome]");
	}
	
	/**
	 * Test that if a non existent path is provided, error is raised
	 */
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testWrongPath() {
		new BrowserInfo(BrowserType.CHROME, "58", "/home/tu/browser/chrome");
	}

	@Test(groups={"ut"})
	public void testEdgeVersion() {
		if (SystemUtils.IS_OS_WINDOWS) {
			BrowserInfo bInfo = new BrowserInfo(BrowserType.EDGE, "107.0", null);
			Assert.assertEquals(bInfo.getDriverFileName(), "edgedriver_107.0_edge-107-108");
		}
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testDriverDoesNotExist() {
		BrowserInfo bInfo = new BrowserInfo(BrowserType.EDGE, "10", null);
		bInfo.getDriverFileName();
	}
	
	@Test(groups={"ut"})
	public void testIE9Version() {
		if (SystemUtils.IS_OS_WINDOWS) {
			BrowserInfo bInfo = new BrowserInfo(BrowserType.INTERNET_EXPLORER, "9", null);
			Assert.assertEquals(bInfo.getDriverFileName(), "IEDriverServer_x64");
		}
	}
	
	@Test(groups={"ut"})
	public void testIE10Version() {
		if (SystemUtils.IS_OS_WINDOWS) {
			BrowserInfo bInfo = new BrowserInfo(BrowserType.INTERNET_EXPLORER, "10", null);
			Assert.assertEquals(bInfo.getDriverFileName(), "IEDriverServer_Win32");
		}
	}
	
	/**
	 * Check we take the highest driver version matching this chrome version
	 * @throws Exception 
	 */
	@Test(groups={"ut"})
	public void testChromeVersionHighestDriverVersion() throws Exception {

		List<String> driverList = Arrays.asList("chromedriver_2.28_chrome-55-57_android_7.0.exe", "chromedriver_2.29_chrome-56-58_android_7.0.exe", "chromedriver_2.31_chrome-58-60.exe");
		BrowserInfo bInfo = PowerMockito.spy(new BrowserInfo(BrowserType.CHROME, "58.0", null));
		PowerMockito.when(bInfo, "getDriverFiles").thenReturn(driverList);
		PowerMockito.doNothing().when(bInfo, "checkResourceExists");
		
		Assert.assertEquals(bInfo.getDriverFileName(), "chromedriver_2.31_chrome-58-60");
	}
	
	/**
	 * Check we take the highest driver version matching this chrome version
	 * @throws Exception 
	 */
	@Test(groups={"ut"})
	public void testGetFilesFromList() throws Exception {
		
		List<String> driverList = Arrays.asList("chromedriver_2.28_chrome-55-57_android_7.0.exe", "chromedriver_2.29_chrome-56-58_android_7.0.exe", "chromedriver_2.31_chrome-58-60.exe");
		BrowserInfo bInfo = PowerMockito.spy(new BrowserInfo(BrowserType.CHROME, "58.0", null));
		BrowserInfo.setDriverList(driverList);
		PowerMockito.doNothing().when(bInfo, "checkResourceExists");
		
		Assert.assertEquals(bInfo.getDriverFileName(), "chromedriver_2.31_chrome-58-60");
	}
	
	/**
	 * Check we can discover version inside a range of versions
	 * @throws Exception 
	 */
	@Test(groups={"ut"})
	public void testChromeVersionMiddleRange() throws Exception {
		List<String> driverList = Arrays.asList("chromedriver_2.28_chrome-55-57_android_7.0", "chromedriver_2.29_chrome-56-58_android_7.0", "chromedriver_2.31_chrome-58-60");
		BrowserInfo bInfo = PowerMockito.spy(new BrowserInfo(BrowserType.CHROME, "57.1", null));
		PowerMockito.when(bInfo, "getDriverFiles").thenReturn(driverList);
		PowerMockito.doNothing().when(bInfo, "checkResourceExists");
		
		Assert.assertEquals(bInfo.getDriverFileName(), "chromedriver_2.29_chrome-56-58_android_7.0");
	}
	
	/**
	 * Check that an error is raised when no driver matches a lower browser version
	 * @throws Exception 
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testChromeVersionNotFound() throws Exception {
		List<String> driverList = Arrays.asList("chromedriver_2.28_chrome-55-57_android_7.0", "chromedriver_2.29_chrome-56-58_android_7.0", "chromedriver_2.31_chrome-58-60");
		BrowserInfo bInfo = PowerMockito.spy(new BrowserInfo(BrowserType.CHROME, "1.0", null));
		PowerMockito.when(bInfo, "getDriverFiles").thenReturn(driverList);
		PowerMockito.doNothing().when(bInfo, "checkResourceExists");
		
		bInfo.getDriverFileName();
	}
	
	/**
	 * Check that no error is raised if browser version is higher than available version
	 * an error message should be displayed
	 * @throws Exception 
	 */
	@Test(groups={"ut"})
	public void testChromeVersionHigherThanDriverVersion() throws Exception {
		List<String> driverList = Arrays.asList("chromedriver_2.28_chrome-55-57_android_7.0", "chromedriver_2.29_chrome-56-58_android_7.0", "chromedriver_2.31_chrome-58-60");
		BrowserInfo bInfo = PowerMockito.spy(new BrowserInfo(BrowserType.CHROME, "70.0", null));
		PowerMockito.when(bInfo, "getDriverFiles").thenReturn(driverList);
		PowerMockito.doNothing().when(bInfo, "checkResourceExists");
		
		Assert.assertEquals(bInfo.getDriverFileName(), "chromedriver_2.31_chrome-58-60");
	}
	
	/**
	 * Error should be raised when no driver is found
	 * @throws Exception 
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testChromeVersionNoDriver() throws Exception {
		List<String> driverList = Arrays.asList("chromedriver_2.20_android-6.0");
		BrowserInfo bInfo = PowerMockito.spy(new BrowserInfo(BrowserType.CHROME, "70.0", null));
		PowerMockito.when(bInfo, "getDriverFiles").thenReturn(driverList);
		PowerMockito.doNothing().when(bInfo, "checkResourceExists");
		
		bInfo.getDriverFileName();
	}
	
	/**
	 * Error should be raised when file pattern is not correct
	 * @throws Exception 
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testChromeVersionBadPattern() throws Exception {
		List<String> driverList = Arrays.asList("chromedriver_2.20_chrome-55.0-57.0");
		BrowserInfo bInfo = PowerMockito.spy(new BrowserInfo(BrowserType.CHROME, "55.0", null));
		PowerMockito.when(bInfo, "getDriverFiles").thenReturn(driverList);
		PowerMockito.doNothing().when(bInfo, "checkResourceExists");
		
		bInfo.getDriverFileName();
	}

	
	/**
	 * Check we take the exact driver version matching this android browser version
	 * @throws Exception 
	 */
	@Test(groups={"ut"})
	public void testAndroidVersionExactMatch() throws Exception {

		List<String> driverList = Arrays.asList("chromedriver_2.20_chrome-55-57_android-6.0.exe", "chromedriver_2.29_chrome-56-58_android-7.0", "chromedriver_2.31_chrome-58-60");
		BrowserInfo bInfo = PowerMockito.spy(new BrowserInfo(BrowserType.BROWSER, "6.0", null));
		PowerMockito.when(bInfo, "getDriverFiles").thenReturn(driverList);
		PowerMockito.doNothing().when(bInfo, "checkResourceExists");
		
		Assert.assertEquals(bInfo.getDriverFileName(), "chromedriver_2.20_chrome-55-57_android-6.0");
	}
	
	/**
	 * Check that if file name does not respect the pattern, file is rejected
	 * @throws Exception 
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testAndroidVersionWrongPattern() throws Exception {
		
		List<String> driverList = Arrays.asList("chromedriver_2.20_chrome-55-57_android-6");
		BrowserInfo bInfo = PowerMockito.spy(new BrowserInfo(BrowserType.BROWSER, "6.0", null));
		PowerMockito.when(bInfo, "getDriverFiles").thenReturn(driverList);
		PowerMockito.doNothing().when(bInfo, "checkResourceExists");
		
		bInfo.getDriverFileName();
	}
	
	/**
	 * Error raised if no version matches
	 * @throws Exception 
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testAndroidVersionNoMatch() throws Exception {
		
		List<String> driverList = Arrays.asList("chromedriver_2.20_chrome-55-57_android-6.0", "chromedriver_2.29_chrome-56-58_android-7.0", "chromedriver_2.31_chrome-58-60");
		BrowserInfo bInfo = PowerMockito.spy(new BrowserInfo(BrowserType.BROWSER, "5.0", null));
		PowerMockito.when(bInfo, "getDriverFiles").thenReturn(driverList);
		PowerMockito.doNothing().when(bInfo, "checkResourceExists");
		
		bInfo.getDriverFileName();
	}

	/**
	 * Check that we get the driver list if driver artifact is installed for the right OS
	 * @throws Exception 
	 */
	@Test(groups={"ut"})
	public void testGetDriverFilesWithInstalledArtifact() throws Exception {
		PowerMockito.mockStatic(OSUtility.class);
		when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.WINDOWS);
		
		String[] driversFiles = new String[] {"windows/chromedriver_2.20_chrome-55-57_android-6.0.exe", "windows/chromedriver_2.29_chrome-56-58_android-7.0.exe", "windows/chromedriver_2.31_chrome-58-60.exe"};
		
		BrowserInfo bInfo = PowerMockito.spy(new BrowserInfo(BrowserType.CHROME, "55.0", null));
		PowerMockito.when(bInfo, "getDriverListFromJarResources", "driver-list-windows.txt").thenReturn(driversFiles);
		
		Assert.assertEquals(bInfo.getDriverFiles().size(), 3);
	}
	
	/**
	 * If artifact is not installed, getDriverListFromJarResources raises NullPointerException
	 * @throws Exception
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testGetDriverFilesWithNotInstalledArtifact() throws Exception {
		PowerMockito.mockStatic(OSUtility.class);
		when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.WINDOWS);
		
		BrowserInfo bInfo = PowerMockito.spy(new BrowserInfo(BrowserType.CHROME, "55.0", null));
		PowerMockito.when(bInfo, "getDriverListFromJarResources", "driver-list-windows.txt").thenThrow(NullPointerException.class);
		
		Assert.assertEquals(bInfo.getDriverFiles().size(), 3);
	}
	
	/**
	 * Check we filter drivers by OS
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testGetDriverFilesWithInstalledArtifactForOtherOS() throws Exception {
		PowerMockito.mockStatic(OSUtility.class);
		when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.LINUX);
		
		String[] driversFiles = new String[] {"windows/chromedriver_2.20_chrome-55-57_android-6.0.exe", "windows/chromedriver_2.29_chrome-56-58_android-7.0.exe", "windows/chromedriver_2.31_chrome-58-60.exe"};
		
		BrowserInfo bInfo = PowerMockito.spy(new BrowserInfo(BrowserType.CHROME, "55.0", null));
		PowerMockito.when(bInfo, "getDriverListFromJarResources", "driver-list-linux.txt").thenReturn(driversFiles);
		
		Assert.assertEquals(bInfo.getDriverFiles().size(), 0);
	}
	
	/**
	 * Error raised if no driver file exists
	 * @throws Exception 
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testAndroidVersionNoDriver() throws Exception {
		
		List<String> driverList = Arrays.asList("chromedriver_2.31_chrome-58-60");
		BrowserInfo bInfo = PowerMockito.spy(new BrowserInfo(BrowserType.BROWSER, "5.0", null));
		PowerMockito.when(bInfo, "getDriverFiles").thenReturn(driverList);
		PowerMockito.doNothing().when(bInfo, "checkResourceExists");
		
		bInfo.getDriverFileName();
	}
	
	@Test(groups= {"ut"})
	public void testHighestDriverVersion() {
		BrowserInfo bi1 = new BrowserInfo(BrowserType.CHROME, "48.0", null);
		BrowserInfo bi2 = new BrowserInfo(BrowserType.CHROME, "58.0", null);
		
		BrowserInfo highestBi = BrowserInfo.getHighestDriverVersion(Arrays.asList(bi1, bi2));
		Assert.assertEquals(highestBi, bi2);
	}
	
	@Test(groups= {"ut"})
	public void testHighestDriverVersionNullBrowserInfo() {
		BrowserInfo bi1 = new BrowserInfo(BrowserType.CHROME, "48.0", null);
		BrowserInfo bi2 = new BrowserInfo(BrowserType.CHROME, "58.0", null);
		
		BrowserInfo highestBi = BrowserInfo.getHighestDriverVersion(Arrays.asList(bi1, null, bi2));
		Assert.assertEquals(highestBi, bi2);
	}
	
	@Test(groups= {"ut"})
	public void testGetInfoFromVersion() {
		BrowserInfo bi1 = new BrowserInfo(BrowserType.CHROME, "48.0", null);
		BrowserInfo bi2 = new BrowserInfo(BrowserType.CHROME, "58.0", null);
		
		BrowserInfo biFromVersion = BrowserInfo.getInfoFromVersion("58.0", Arrays.asList(bi1, bi2));
		Assert.assertEquals(biFromVersion, bi2);
	}
	
	@Test(groups= {"ut"})
	public void testGetInfoFromVersionNullInfo() {
		BrowserInfo bi1 = new BrowserInfo(BrowserType.CHROME, "48.0", null);
		BrowserInfo bi2 = new BrowserInfo(BrowserType.CHROME, "58.0", null);
		
		BrowserInfo biFromVersion = BrowserInfo.getInfoFromVersion("58.0", Arrays.asList(bi1, null, bi2));
		Assert.assertEquals(biFromVersion, bi2);
	}
	
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testGetInfoFromNonExistentVersion() {
		BrowserInfo bi1 = new BrowserInfo(BrowserType.CHROME, "48.0", null);
		BrowserInfo bi2 = new BrowserInfo(BrowserType.CHROME, "58.0", null);
		
		BrowserInfo.getInfoFromVersion("57.0", Arrays.asList(bi1, bi2));
	}
	
	@Test(groups= {"ut"})
	public void testGetInfoFromPath() {
		BrowserInfo bi1 = new BrowserInfo(BrowserType.CHROME, "48.0", "/home/tu/chrome", false);
		BrowserInfo bi2 = new BrowserInfo(BrowserType.CHROME, "58.0", "/home/tu/chrome2", false);
		
		BrowserInfo biFromVersion = BrowserInfo.getInfoFromBinary("/home/tu/chrome2", Arrays.asList(bi1, bi2));
		Assert.assertEquals(biFromVersion, bi2);
	}
	
	@Test(groups= {"ut"})
	public void testGetInfoFromPathNullInfo() {
		BrowserInfo bi1 = new BrowserInfo(BrowserType.CHROME, "48.0", "/home/tu/chrome", false);
		BrowserInfo bi2 = new BrowserInfo(BrowserType.CHROME, "58.0", "/home/tu/chrome2", false);
		
		BrowserInfo biFromVersion = BrowserInfo.getInfoFromBinary("/home/tu/chrome2", Arrays.asList(bi1, null, bi2));
		Assert.assertEquals(biFromVersion, bi2);
	}
	
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testGetInfoFromNonExistentPath() {
		BrowserInfo bi1 = new BrowserInfo(BrowserType.CHROME, "48.0", "/home/tu/chrome", false);
		BrowserInfo bi2 = new BrowserInfo(BrowserType.CHROME, "58.0", "/home/tu/chrome2", false);
		
		BrowserInfo.getInfoFromBinary("/home/tu/chrome3", Arrays.asList(bi1, bi2));
	}
	
	@Test(groups={"ut"})
	public void testGetDefaultEdgeWindowsProfile() throws Exception {
		PowerMockito.mockStatic(OSUtility.class);
		when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.WINDOWS);
		
		BrowserInfo bi = new BrowserInfo(BrowserType.EDGE, "90.0", null);
		Assert.assertTrue(bi.getDefaultProfilePath().replace("\\",  "/").matches("C:/Users/.*?/AppData/Local/Microsoft/Edge/User Data"));
	}

	@Test(groups={"ut"})
	public void testGetDefaultEdgeLinuxProfile() throws Exception {
		PowerMockito.mockStatic(OSUtility.class);
		when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.LINUX);
		
		BrowserInfo bi = new BrowserInfo(BrowserType.EDGE, "90.0", null);
		Assert.assertTrue(bi.getDefaultProfilePath().matches("/home/.*?/.config/edge/default"));
	}

	@Test(groups={"ut"})
	public void testGetDefaultEdgeMacProfile() throws Exception {
		PowerMockito.mockStatic(OSUtility.class);
		when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.MAC);
		
		BrowserInfo bi = new BrowserInfo(BrowserType.EDGE, "90.0", null);
		Assert.assertTrue(bi.getDefaultProfilePath().matches("/Users/.*?/Library/Application Support/Microsoft/Edge"));
	}
	
	@Test(groups={"ut"})
	public void testGetDefaultChromeWindowsProfile() throws Exception {
		PowerMockito.mockStatic(OSUtility.class);
		when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.WINDOWS);
		
		BrowserInfo bi = new BrowserInfo(BrowserType.CHROME, "58.0", null);
		Assert.assertTrue(bi.getDefaultProfilePath().replace("\\",  "/").matches("C:/Users/.*?/AppData/Local/Google/Chrome/User Data"));
	}
	
	
	@Test(groups={"ut"})
	public void testGetDefaultChromeLinuxProfile() throws Exception {
		PowerMockito.mockStatic(OSUtility.class);
		when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.LINUX);
		
		BrowserInfo bi = new BrowserInfo(BrowserType.CHROME, "58.0", null);
		Assert.assertTrue(bi.getDefaultProfilePath().matches("/home/.*?/.config/google-chrome/default"));
	}
	
	@Test(groups={"ut"})
	public void testGetDefaultChromeMacProfile() throws Exception {
		PowerMockito.mockStatic(OSUtility.class);
		when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.MAC);
		
		BrowserInfo bi = new BrowserInfo(BrowserType.CHROME, "58.0", null);
		Assert.assertTrue(bi.getDefaultProfilePath().matches("/Users/.*?/Library/Application Support/Google/Chrome"));
	}
	
	@Test(groups={"ut"})
	public void testGetDefaultFirefoxWindowsProfile() throws Exception {
		PowerMockito.mockStatic(OSUtility.class);
		when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.WINDOWS);
		
		BrowserInfo bi = new BrowserInfo(BrowserType.FIREFOX, "58.0", null);
		Assert.assertTrue(bi.getDefaultProfilePath().replace("\\",  "/").matches("C:/Users/.*?/AppData/Roaming/Mozilla/Firefox/Profiles/.*\\.default"));
	}
	
	@Test(groups={"ut"})
	public void testGetDefaultFirefoxLinuxProfile() throws Exception {
		PowerMockito.mockStatic(Files.class);
		PowerMockito.mockStatic(OSUtility.class);
		when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.LINUX);
		when(Files.list(any(Path.class))).thenReturn(streamPaths);
		when(streamPaths.filter(any(Predicate.class))).thenReturn(streamPaths);
		when(streamPaths.collect(any())).thenReturn(Arrays.asList(Paths.get("/home/user/.mozilla/firefox")));
		
		BrowserInfo bi = new BrowserInfo(BrowserType.FIREFOX, "58.0", null);
		
		Assert.assertEquals(bi.getDefaultProfilePath().replace("\\",  "/"), "/home/user/.mozilla/firefox");
	}
	
	@Test(groups={"ut"})
	public void testGetDefaultFirefoxMacProfile() throws Exception {

		PowerMockito.mockStatic(Files.class);
		PowerMockito.mockStatic(OSUtility.class);
		when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.MAC);
		when(Files.list(any(Path.class))).thenReturn(streamPaths);
		when(streamPaths.filter(any(Predicate.class))).thenReturn(streamPaths);
		when(streamPaths.collect(any())).thenReturn(Arrays.asList(Paths.get("/Users/user/Library/Application Support/Firefox/Profiles/")));
		
		BrowserInfo bi = new BrowserInfo(BrowserType.FIREFOX, "58.0", null);
		
		Assert.assertEquals(bi.getDefaultProfilePath().replace("\\",  "/"), "/Users/user/Library/Application Support/Firefox/Profiles");
	}

}
