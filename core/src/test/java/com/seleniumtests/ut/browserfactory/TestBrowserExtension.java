package com.seleniumtests.ut.browserfactory;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.browserfactory.BrowserExtension;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.it.driver.support.server.WebServer;

public class TestBrowserExtension extends GenericTest {


	private String localAddress;
	protected WebServer server;
	
	@BeforeClass(groups={"it", "ut"})
	public void exposeExtension(final ITestContext testNGCtx) throws Exception {
		Map<String, String> mapping = new HashMap<>();
		mapping.put("/tu/test.html", "/test.html");

		localAddress = Inet4Address.getLocalHost().getHostAddress();
        server = new WebServer(localAddress, mapping);
        server.expose();
	}
	
	/**
	 * Test standard case where extension and options are defined
	 */
	@Test(groups="ut")
	public void testCreateExtensionFromOptions() {
		
		Map<String, TestVariable> options = new HashMap<>();
		options.put("extension0.path", new TestVariable("extension0.path", "/home/test/ext.crx"));
		options.put("extension0.options", new TestVariable("extension0.options", "key1=value1;key2=value2"));
		
		List<BrowserExtension> extensions = BrowserExtension.getExtensions(options);
		Assert.assertEquals(extensions.size(), 1);
		Assert.assertEquals(extensions.get(0).getExtensionPath(), new File("/home/test/ext.crx"));
		Assert.assertEquals(extensions.get(0).getOptions().size(), 2);
		Assert.assertEquals(extensions.get(0).getOptions().get("key1"), "value1");
		Assert.assertEquals(extensions.get(0).getOptions().get("key2"), "value2");
	}
	
	/**
	 * Test when options do not define extension parameters
	 */
	@Test(groups="ut")
	public void testCreateExtensionWithoutOptions() {
		
		Map<String, TestVariable> options = new HashMap<>();
		options.put("extension0.path", new TestVariable("extension0.path", "/home/test/ext.crx"));
		
		List<BrowserExtension> extensions = BrowserExtension.getExtensions(options);
		Assert.assertEquals(extensions.size(), 1);
		Assert.assertEquals(extensions.get(0).getExtensionPath(), new File("/home/test/ext.crx"));
		Assert.assertEquals(extensions.get(0).getOptions().size(), 0);
	}
	
	@Test(groups="ut")
	public void testExtensionFromUrl() throws IOException {
		BrowserExtension extension = new BrowserExtension(String.format("http://%s:%d/test.html", localAddress, server.getServerHost().getPort()));
		Assert.assertEquals(extension.getExtensionPath().getParentFile(), File.createTempFile("file", ".tmp").getParentFile());
		Assert.assertEquals(FilenameUtils.getExtension(extension.getExtensionPath().getName()), "html");
	}
	

	@AfterClass(groups={"it", "ut"}, alwaysRun=true)
	public void stop() throws Exception {
		if (server != null) {
			server.stop();
		}
	}
}
