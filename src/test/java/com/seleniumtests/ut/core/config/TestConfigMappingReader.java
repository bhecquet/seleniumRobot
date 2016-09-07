package com.seleniumtests.ut.core.config;

import java.util.Map;

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
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
	}
	
	@Test(groups={"ut"})
	public void readConfigurationMobile() {
		Map<String, String> config = new ConfigMappingReader().readConfig( "", "", "dev");
		Assert.assertEquals(config.get("id_search"), "searchWordMobile", "read mobile config does not work");
	}
	
	@Test(groups={"ut"})
	public void readConfigurationWeb() {
		Map<String, String> config = new ConfigMappingReader().readConfig( "web", "", "dev");
		Assert.assertEquals(config.get("id_search"), "webSearchWord", "read mobile config does not work");
	}
	
	@Test(groups={"ut"})
	public void readConfigurationMobileAndroid() {
		Map<String, String> config = new ConfigMappingReader().readConfig("android", "", "dev");
		Assert.assertEquals(config.get("id_search"), "androidSearchWord", "read android config does not work");
	}
	
	@Test(groups={"ut"})
	public void readConfigurationMobileApple() {
		Map<String, String> config = new ConfigMappingReader().readConfig( "ios", "", "dev");
		Assert.assertEquals(config.get("id_search"), "appleSearchWord", "read apple config does not work");
	}
	
	@Test(groups={"ut"})
	public void readConfigurationMobileAndroid4_3() {
		Map<String, String> config = new ConfigMappingReader().readConfig( "android", "4.3", "dev");
		Assert.assertEquals(config.get("id_search"), "android_4_3_SearchWord", "read android config with version does not work");
	}
	
	@Test(groups={"ut"})
	public void verifyNoLeakBetween4_4and3_3() {
		Map<String, String> config = new ConfigMappingReader().readConfig( "android", "4.4", "dev");
		Assert.assertNull(config.get("special43"));
	}
	
	@Test(groups={"ut"})
	public void readHeritageInApple7() {
		Map<String, String> config = new ConfigMappingReader().readConfig( "ios", "ios_7", "dev");
		Assert.assertEquals(config.get("id_search"), "apple_7_SearchWord", "read apple config with version does not work");
		Assert.assertEquals(config.get("configuration"), "mobile", "read mobile configuration apple 7 does not work");
		Assert.assertEquals(config.get("phoneType"), "ios", "read mobile phoneType apple, heritage 7, does not work");
	}
	
	/**
	 * Test that is version configuration does not exist, config is taken from system
	 */
	@Test(groups={"ut"})
	public void readHeritageInApple8() {
		Map<String, String> config = new ConfigMappingReader().readConfig( "ios", "ios_8", "dev");
		Assert.assertEquals(config.get("id_search"), "appleSearchWord", "read apple config with version does not work");
		Assert.assertEquals(config.get("configuration"), "mobile", "read mobile configuration apple 7 does not work");
		Assert.assertEquals(config.get("phoneType"), "ios", "read mobile phoneType apple, heritage 7, does not work");
	}
	
	@Test(groups={"ut"})
	public void readHeritageInAndroid() {
		Map<String, String> config = new ConfigMappingReader().readConfig("android", "", "dev");
		Assert.assertEquals(config.get("configuration"), "mobile", "read mobile configuration android does not work");
	}
	
	@Test(groups={"ut"})
	public void readHeritageInAndroid4_3WithOtherMethod() {
		Map<String, String> config = new ConfigMappingReader().readConfig( "android", "4.3").get("dev");
		Assert.assertEquals(config.get("phoneType"), "android", "read mobile phoneType android, heritage 4.3, does not work");
		Assert.assertEquals(config.get("configuration"), "mobile", "read mobile configuration android 4.3 does not work");
	}
	
	@Test(groups={"ut"})
	public void readHeritageInAndroid5_0WithOtherMethod() {
		Map<String, String> config = new ConfigMappingReader().readConfig( "android", "5.0").get("dev");
		Assert.assertEquals(config.get("phoneType"), "android", "read mobile phoneType android, heritage 4.3, does not work");
		Assert.assertEquals(config.get("configuration"), "mobile", "read mobile configuration android 4.3 does not work");
	}
	
	@Test(groups={"ut"})
	public void readConfigAndroidWithoutParams() {
		//test mode mobile
		SeleniumTestsContextManager.getThreadContext().setPlatform("android");
		SeleniumTestsContextManager.getThreadContext().setMobilePlatformVersion("");
		Map<String, String> config = new ConfigMappingReader().readConfig( "dev");
		Assert.assertEquals(config.get("id_search"), "androidSearchWord", "read android config does not work");
	}
	
	@Test(groups={"ut"})
	public void readConfigChromeWithoutParams() {
		//test mode web
		SeleniumTestsContextManager.getThreadContext().setPlatform("vista");
		SeleniumTestsContextManager.getThreadContext().setMobilePlatformVersion("");
		Map<String, String> config = new ConfigMappingReader().readConfig( "dev");
		Assert.assertEquals(config.get("id_search"), "webSearchWord", "read chrome config does not work");
	}
	
	@Test(groups={"ut"})
	public void readConfigAndroidWithoutPage() {
		//test mode mobile
		SeleniumTestsContextManager.getThreadContext().setPlatform("android");
		SeleniumTestsContextManager.getThreadContext().setMobilePlatformVersion("");
		Map<String, String> config = new ConfigMappingReader().readConfig().get("dev");
		Assert.assertEquals(config.get("id_search"), "androidSearchWord", "read android config without any param does not work");
	}
	
	@Test(groups={"ut"})
	public void readConfigMobileDifferentPage() {
		Map<String, String> config = new ConfigMappingReader().readConfig("", "", "firstPage");
		Assert.assertEquals(config.get("id_search"), "searchWordFirstPage", "read config first page does not work");
	}
	
}
