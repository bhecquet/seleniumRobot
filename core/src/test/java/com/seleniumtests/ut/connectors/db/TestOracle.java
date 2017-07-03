package com.seleniumtests.ut.connectors.db;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.io.Files;
import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.db.Oracle;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;

public class TestOracle extends GenericTest {

	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testWithoutTnsNames() {
		new Oracle("", "", "");
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testWithWrongTnsNamesFolder() {
		try {
			SeleniumTestsContextManager.getThreadContext().getConfiguration().put("tnsnamePath", "/home/myFolder/oracle");
			new Oracle("", "", "");
		} finally {
			SeleniumTestsContextManager.getThreadContext().getConfiguration().clear();
		}
	}
	
	/**
	 * Folder exists but file does not
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testWithFolderOkNoFile() {
		try {
			File tmp = Files.createTempDir();
			SeleniumTestsContextManager.getThreadContext().getConfiguration().put("tnsnamePath", tmp.getAbsolutePath());
			new Oracle("", "", "");
		} finally {
			SeleniumTestsContextManager.getThreadContext().getConfiguration().clear();
		}
	}
	
	/**
	 * test creation ok
	 * @throws IOException 
	 */
	@Test(groups={"ut"})
	public void testClientCreation() throws IOException {
		try {
			File tmp = Files.createTempDir();
			File tns = Paths.get(tmp.getAbsolutePath(), "tnsnames.ora").toFile();
			FileUtils.write(tns, "XE = (DESCRIPTION =\n" +
									"(ADDRESS = (PROTOCOL = TCP)(HOST = M10.hello.com)(PORT = 1521))\n" +
										"(CONNECT_DATA =\n" +
										"(SERVER = DEDICATED)\n" +
										"(SERVICE_NAME = XE)\n" +
										")\n" +
									")");
			SeleniumTestsContextManager.getThreadContext().getConfiguration().put("tnsnamePath", tmp.getAbsolutePath());
			new Oracle("", "", "");
			Assert.assertEquals(System.getProperty("oracle.net.tns_admin"), tmp.getAbsolutePath());
		} finally {
			SeleniumTestsContextManager.getThreadContext().getConfiguration().clear();
		}
	}
}
