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
package com.seleniumtests.ut.connectors.db;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

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
	 * @throws IOException 
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testWithFolderOkNoFile() throws IOException {
		File tmp = Files.createTempDirectory("tmp").toFile();
		new Oracle("", "", "", tmp.getAbsolutePath());
	}
	
	/**
	 * test creation ok
	 * @throws IOException 
	 */
	@Test(groups={"ut"})
	public void testClientCreation() throws IOException {
		File tmp = Files.createTempDirectory("tmp").toFile();
		File tns = Paths.get(tmp.getAbsolutePath(), "tnsnames.ora").toFile();
		FileUtils.write(tns, "XE = (DESCRIPTION =\n" +
								"(ADDRESS = (PROTOCOL = TCP)(HOST = M10.hello.com)(PORT = 1521))\n" +
									"(CONNECT_DATA =\n" +
									"(SERVER = DEDICATED)\n" +
									"(SERVICE_NAME = XE)\n" +
									")\n" +
								")", StandardCharsets.UTF_8);
		new Oracle("", "", "", tmp.getAbsolutePath());
		Assert.assertEquals(System.getProperty("oracle.net.tns_admin"), tmp.getAbsolutePath());
	}
}
