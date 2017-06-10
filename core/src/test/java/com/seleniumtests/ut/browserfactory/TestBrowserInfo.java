package com.seleniumtests.ut.browserfactory;

import java.util.Arrays;
import java.util.List;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;

@PrepareForTest(BrowserInfo.class)
public class TestBrowserInfo extends MockitoTest {

	@Test(groups={"ut"})
	public void testEdgeVersion() {
		BrowserInfo bInfo = new BrowserInfo(BrowserType.EDGE, "10240", null);
		Assert.assertEquals(bInfo.getDriverFileName(), "MicrosoftWebDriver_10240");
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testDriverDoesNotExist() {
		BrowserInfo bInfo = new BrowserInfo(BrowserType.EDGE, "10241", null);
		bInfo.getDriverFileName();
	}
	
	@Test(groups={"ut"})
	public void testIE9Version() {
		BrowserInfo bInfo = new BrowserInfo(BrowserType.INTERNET_EXPLORER, "9", null);
		Assert.assertEquals(bInfo.getDriverFileName(), "IEDriverServer_x64");
	}
	
	@Test(groups={"ut"})
	public void testIE10Version() {
		BrowserInfo bInfo = new BrowserInfo(BrowserType.INTERNET_EXPLORER, "10", null);
		Assert.assertEquals(bInfo.getDriverFileName(), "IEDriverServer_Win32");
	}
	
	/**
	 * Check we take the highest driver version matching this chrome version
	 * @throws Exception 
	 */
	@Test(groups={"ut"})
	public void testChromeVersionHighestDriverVersion() throws Exception {

		List<String> driverList = Arrays.asList(new String[] {"chromedriver_2.28_chrome-55-57_android_7.0.exe", "chromedriver_2.29_chrome-56-58_android_7.0.exe", "chromedriver_2.30_chrome-58-60.exe"});
		BrowserInfo bInfo = PowerMockito.spy(new BrowserInfo(BrowserType.CHROME, "58.0", null));
		PowerMockito.when(bInfo, "getDriverFiles").thenReturn(driverList);
		PowerMockito.doNothing().when(bInfo, "checkResourceExists");
		
		Assert.assertEquals(bInfo.getDriverFileName(), "chromedriver_2.30_chrome-58-60");
	}
	
	/**
	 * Check we can discover version inside a range of versions
	 * @throws Exception 
	 */
	@Test(groups={"ut"})
	public void testChromeVersionMiddleRange() throws Exception {
		List<String> driverList = Arrays.asList(new String[] {"chromedriver_2.28_chrome-55-57_android_7.0", "chromedriver_2.29_chrome-56-58_android_7.0", "chromedriver_2.30_chrome-58-60"});
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
		List<String> driverList = Arrays.asList(new String[] {"chromedriver_2.28_chrome-55-57_android_7.0", "chromedriver_2.29_chrome-56-58_android_7.0", "chromedriver_2.30_chrome-58-60"});
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
		List<String> driverList = Arrays.asList(new String[] {"chromedriver_2.28_chrome-55-57_android_7.0", "chromedriver_2.29_chrome-56-58_android_7.0", "chromedriver_2.30_chrome-58-60"});
		BrowserInfo bInfo = PowerMockito.spy(new BrowserInfo(BrowserType.CHROME, "70.0", null));
		PowerMockito.when(bInfo, "getDriverFiles").thenReturn(driverList);
		PowerMockito.doNothing().when(bInfo, "checkResourceExists");
		
		Assert.assertEquals(bInfo.getDriverFileName(), "chromedriver_2.30_chrome-58-60");
	}
	
	/**
	 * Error should be raised when no driver is found
	 * @throws Exception 
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testChromeVersionNoDriver() throws Exception {
		List<String> driverList = Arrays.asList(new String[] {"chromedriver_2.20_android-6.0"});
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
		List<String> driverList = Arrays.asList(new String[] {"chromedriver_2.20_chrome-55.0-57.0"});
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

		List<String> driverList = Arrays.asList(new String[] {"chromedriver_2.20_chrome-55-57_android-6.0.exe", "chromedriver_2.29_chrome-56-58_android-7.0", "chromedriver_2.30_chrome-58-60"});
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
		
		List<String> driverList = Arrays.asList(new String[] {"chromedriver_2.20_chrome-55-57_android-6"});
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
		
		List<String> driverList = Arrays.asList(new String[] {"chromedriver_2.20_chrome-55-57_android-6.0", "chromedriver_2.29_chrome-56-58_android-7.0", "chromedriver_2.30_chrome-58-60"});
		BrowserInfo bInfo = PowerMockito.spy(new BrowserInfo(BrowserType.BROWSER, "5.0", null));
		PowerMockito.when(bInfo, "getDriverFiles").thenReturn(driverList);
		PowerMockito.doNothing().when(bInfo, "checkResourceExists");
		
		bInfo.getDriverFileName();
	}
	
	/**
	 * Error raised if no driver file exists
	 * @throws Exception 
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testAndroidVersionNoDriver() throws Exception {
		
		List<String> driverList = Arrays.asList(new String[] {"chromedriver_2.30_chrome-58-60"});
		BrowserInfo bInfo = PowerMockito.spy(new BrowserInfo(BrowserType.BROWSER, "5.0", null));
		PowerMockito.when(bInfo, "getDriverFiles").thenReturn(driverList);
		PowerMockito.doNothing().when(bInfo, "checkResourceExists");
		
		bInfo.getDriverFileName();
	}
}
