package com.seleniumtests.ut.core.config;

import java.util.HashMap;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.config.ConfigMappingReader;

public class TestConfigMappingReader extends GenericTest {

	@BeforeMethod(enabled=true, alwaysRun = true)
	public void initContext(final ITestContext testNGCtx, final XmlTest xmlTest) {
		SeleniumTestsContextManager.initThreadContext(testNGCtx, xmlTest);
	}
	
	@Test(groups={"ut"})
	public void readConfigurationMobile() {
		HashMap<String, String> config = new ConfigMappingReader().readConfig( "", "", "dev");
		Assert.assertEquals(config.get("id_search"), "searchWordMobile", "read mobile config does not work");
	}
	
	@Test(groups={"ut"})
	public void readConfigurationMobileAndroid() {
		HashMap<String, String> config = new ConfigMappingReader().readConfig("android", "", "dev");
		Assert.assertEquals(config.get("id_search"), "androidSearchWord", "read android config does not work");
	}
	
	@Test(groups={"ut"})
	public void readConfigurationMobileApple() {
		HashMap<String, String> config = new ConfigMappingReader().readConfig( "ios", "", "dev");
		Assert.assertEquals(config.get("id_search"), "appleSearchWord", "read apple config does not work");
	}
	
	@Test(groups={"ut"})
	public void readConfigurationMobileAndroid4_3() {
		HashMap<String, String> config = new ConfigMappingReader().readConfig( "android", "4.3", "dev");
		Assert.assertEquals(config.get("id_search"), "android_4_3_SearchWord", "read android config with version does not work");
	}
	
	@Test(groups={"ut"})
	public void verifyNoLeakBetween4_4and3_3() {
		HashMap<String, String> config = new ConfigMappingReader().readConfig( "android", "4.4", "dev");
		Assert.assertNull(config.get("special43"));
	}
	
	@Test(groups={"ut"})
	public void readHeritageInApple7() {
		HashMap<String, String> config = new ConfigMappingReader().readConfig( "ios", "ios_7", "dev");
		Assert.assertEquals(config.get("id_search"), "apple_7_SearchWord", "read apple config with version does not work");
		Assert.assertEquals(config.get("configuration"), "mobile", "read mobile configuration apple 7 does not work");
		Assert.assertEquals(config.get("phoneType"), "ios", "read mobile phoneType apple, heritage 7, does not work");
	}
	
	@Test(groups={"ut"})
	public void readHeritageInAndroid() {
		HashMap<String, String> config = new ConfigMappingReader().readConfig("android", "", "dev");
		Assert.assertEquals(config.get("configuration"), "mobile", "read mobile configuration android does not work");
	}
	
	@Test(groups={"ut"})
	public void readHeritageInAndroid4_3WithOtherMethod() {
		HashMap<String, String> config = new ConfigMappingReader().readConfig( "android", "4.3").get("dev");
		Assert.assertEquals(config.get("phoneType"), "android", "read mobile phoneType android, heritage 4.3, does not work");
		Assert.assertEquals(config.get("configuration"), "mobile", "read mobile configuration android 4.3 does not work");
	}
	
	@Test(groups={"ut"})
	public void readConfigAndroidWithoutParams() {
		//test mode mobile
		SeleniumTestsContextManager.getThreadContext().setPlatform("android");
		SeleniumTestsContextManager.getThreadContext().setMobilePlatformVersion("");
		HashMap<String, String> config = new ConfigMappingReader().readConfig( "dev");
		Assert.assertEquals(config.get("id_search"), "androidSearchWord", "read android config does not work");
	}
	
	@Test(groups={"ut"})
	public void readConfigChromeWithoutParams() {
		//test mode web
		SeleniumTestsContextManager.getThreadContext().setPlatform("web");
		SeleniumTestsContextManager.getThreadContext().setMobilePlatformVersion("");
		HashMap<String, String> config = new ConfigMappingReader().readConfig( "dev");
		Assert.assertEquals(config.get("id_search"), "webSearchWord", "read chrome config does not work");
	}
	
	@Test(groups={"ut"})
	public void readConfigAndroidWithoutPage() {
		//test mode mobile
		SeleniumTestsContextManager.getThreadContext().setPlatform("android");
		SeleniumTestsContextManager.getThreadContext().setMobilePlatformVersion("");
		HashMap<String, String> config = new ConfigMappingReader().readConfig().get("dev");
		Assert.assertEquals(config.get("id_search"), "androidSearchWord", "read android config without any param does not work");
	}
	
	@Test(groups={"ut"})
	public void readConfigMobileDifferentPage() {
		HashMap<String, String> config = new ConfigMappingReader().readConfig("", "", "firstPage");
		Assert.assertEquals(config.get("id_search"), "searchWordFirstPage", "read config first page does not work");
	}
	
}
