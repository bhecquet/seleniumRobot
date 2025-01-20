package com.seleniumtests.it.util;

import jakarta.xml.bind.DatatypeConverter;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.Proxy.ProxyType;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;
import com.seleniumtests.util.osutility.OSUtilityWindows;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

/**
 * Tests in this class will only configure proxy, you have to check manually in windows settings that value is correctly set
 * @author S047432
 *
 */
public class TestOsUtilityWindows extends GenericTest {

	@BeforeMethod(groups="no")
	public void init() {
		if (!OSUtility.isWindows()) {
			throw new SkipException("Test only available on windows");
		}
	}
	
	
	@Test(groups="no")
	public void testAutoProxy() {
		Proxy proxy = new Proxy();
		proxy.setProxyType(ProxyType.AUTODETECT);
		
		((OSUtilityWindows)OSUtilityFactory.getInstance()).setSystemProxy(proxy);
		logger.info(DatatypeConverter.printHexBinary(Advapi32Util.registryGetBinaryValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\Connections", "DefaultConnectionSettings")));
	}
	
	@Test(groups="no")
	public void testManualProxy() {
		Proxy proxy = new Proxy();
		proxy.setProxyType(ProxyType.MANUAL);
		proxy.setHttpProxy("http://proxy.company.com:8080");
		proxy.setNoProxy("*.no.proxy");
		
		((OSUtilityWindows)OSUtilityFactory.getInstance()).setSystemProxy(proxy);
		logger.info(Advapi32Util.registryGetBinaryValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\Connections", "DefaultConnectionSettings"));
	}
	
	@Test(groups="no")
	public void testProxyAutoConfigUrl() {
		Proxy proxy = new Proxy();
		proxy.setProxyType(ProxyType.PAC);
		proxy.setProxyAutoconfigUrl("http://wpad.company.com:8080/wpad.pac");
		
		((OSUtilityWindows)OSUtilityFactory.getInstance()).setSystemProxy(proxy);
		
		logger.info(Advapi32Util.registryGetBinaryValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\Connections", "DefaultConnectionSettings"));
	}
}
