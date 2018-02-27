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
import com.seleniumtests.customexception.ConfigurationException;

public class TestOracle extends GenericTest {

	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testWithWrongTnsNamesFolder() {
		new Oracle("", "", "", "/home/myFolder/oracle");
	}
	
	/**
	 * Folder exists but file does not
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testWithFolderOkNoFile() {
		File tmp = Files.createTempDir();
		new Oracle("", "", "", tmp.getAbsolutePath());
	}
	
	/**
	 * test creation ok
	 * @throws IOException 
	 */
	@Test(groups={"ut"})
	public void testClientCreation() throws IOException {
		File tmp = Files.createTempDir();
		File tns = Paths.get(tmp.getAbsolutePath(), "tnsnames.ora").toFile();
		FileUtils.write(tns, "XE = (DESCRIPTION =\n" +
								"(ADDRESS = (PROTOCOL = TCP)(HOST = M10.hello.com)(PORT = 1521))\n" +
									"(CONNECT_DATA =\n" +
									"(SERVER = DEDICATED)\n" +
									"(SERVICE_NAME = XE)\n" +
									")\n" +
								")");
		new Oracle("", "", "", tmp.getAbsolutePath());
		Assert.assertEquals(System.getProperty("oracle.net.tns_admin"), tmp.getAbsolutePath());
	}
}
