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
package com.seleniumtests.ut.util.squashta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.squashta.SquashTaTestDef;
import com.seleniumtests.util.squashta.TaScriptGenerator;

public class TestTaScriptGenerator extends GenericTest {

	@BeforeClass(groups={"ut"})
	public void init(ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
	}
	
	
	/**
	 * Test parsing of feature files to see if all scenario / scenario outlines, except the excluded ones are get
	 */
	@Test(groups={"squash"})
	public void testParseFeatures() {
		
		TaScriptGenerator scriptGenerator = new TaScriptGenerator("core", SeleniumTestsContextManager.getRootPath(), SeleniumTestsContextManager.getRootPath());
		List<String> scenarios = scriptGenerator.parseFeatures();
		Assert.assertTrue(scenarios.contains("core_ <text>"));
		Assert.assertTrue(scenarios.contains("core_3"));
		Assert.assertTrue(scenarios.contains("core_4"));
		
		// this one is excluded
		Assert.assertFalse(scenarios.contains("core_5"));
		Assert.assertTrue(scenarios.contains("core_6"));
	}
	
	@Test(groups={"squash"})
	public void testParseTestNg() {
		TaScriptGenerator scriptGenerator = new TaScriptGenerator("core", SeleniumTestsContextManager.getRootPath(), SeleniumTestsContextManager.getRootPath());
		List<SquashTaTestDef> testList = scriptGenerator.parseTestNgXml();
		Assert.assertTrue(testList.contains(new SquashTaTestDef(null, "SquashTa_cucumber_test", true, "Conge")));
		Assert.assertTrue(testList.contains(new SquashTaTestDef(null, "SquashTa_cucumber_tags", true, "@new")));
		Assert.assertTrue(testList.contains(new SquashTaTestDef(null, "SquashTa_cucumber_generic", true, "")));
		Assert.assertTrue(testList.contains(new SquashTaTestDef(null, "SquashTa_testPlan", false, "")));
		Assert.assertFalse(testList.contains(new SquashTaTestDef(null, "SquashTa_excluded", true, "")));
		Assert.assertTrue(testList.contains(new SquashTaTestDef(null, "UnitTestsWithContext", false, "")));
	}
	
	/**
	 * Test if files which are no more valid are deleted
	 * @throws IOException
	 */
	@Test(groups={"squash"})
	public void testCleanGeneratedFiles() throws IOException {
		File tmpFolder = Paths.get(SeleniumTestsContextManager.getDataPath(), "tmp").toFile();
		tmpFolder.mkdirs();
		FileUtils.write(Paths.get(tmpFolder.getPath(), "a_file.ta").toFile(), "");
		FileUtils.write(Paths.get(tmpFolder.getPath(), "g__a_file.ta").toFile(), "");
		FileUtils.write(Paths.get(tmpFolder.getPath(), "g__an_other_file.ta").toFile(), "");
		TaScriptGenerator scriptGenerator = new TaScriptGenerator("core", SeleniumTestsContextManager.getRootPath(), SeleniumTestsContextManager.getRootPath());
		
		try {
			scriptGenerator.cleanGeneratedFile(tmpFolder.toString(), Arrays.asList("g__a_file.ta", "g__a_file2.ta"));
			Assert.assertFalse(Paths.get(tmpFolder.getPath(), "g__an_other_file.ta").toFile().exists(), "g__an_other_file.ta should have been deleted");
			Assert.assertTrue(Paths.get(tmpFolder.getPath(), "g__a_file.ta").toFile().exists(), "g__an_other_file.ta should not have been deleted");
			Assert.assertTrue(Paths.get(tmpFolder.getPath(), "a_file.ta").toFile().exists(), "g__an_other_file.ta should not have been deleted");
		} finally {
			FileUtils.cleanDirectory(tmpFolder);
			tmpFolder.delete();
		}
	}
	
	/**
	 * Test the right scripts are generated
	 * @throws IOException 
	 */
	@Test(groups={"squash"})
	public void testGenerateTaScripts() throws IOException {
		File generationFolder = Paths.get(SeleniumTestsContextManager.getRootPath(), "data", "core", "squash-ta").toFile();
		TaScriptGenerator scriptGenerator = new TaScriptGenerator("core", SeleniumTestsContextManager.getRootPath(), generationFolder.getPath());
		
		try {
			scriptGenerator.generateTaScripts();
			Assert.assertTrue(Paths.get(generationFolder.getPath(), "src", "squashTA", "tests", "g__squashta.xml_SquashTa_cucumber_tags.ta").toFile().isFile());
			Assert.assertTrue(Paths.get(generationFolder.getPath(), "src", "squashTA", "tests", "g__squashta.xml_SquashTa_cucumber_test.ta").toFile().isFile());
			Assert.assertTrue(Paths.get(generationFolder.getPath(), "src", "squashTA", "tests", "g__squashta.xml_SquashTa_testPlan.ta").toFile().isFile());
			Assert.assertTrue(Paths.get(generationFolder.getPath(), "src", "squashTA", "tests", "g__tu.xml_UnitTestsWithContext.ta").toFile().isFile());
			Assert.assertTrue(Paths.get(generationFolder.getPath(), "src", "squashTA", "tests", "g__squashta.xml_SquashTa_cucumber_generic_core__text.ta").toFile().isFile());
			Assert.assertTrue(Paths.get(generationFolder.getPath(), "src", "squashTA", "tests", "core_generic.ta").toFile().isFile());
		} finally {
			for (File f: Paths.get(generationFolder.getPath(), "src", "squashTA", "tests").toFile().listFiles()) {
				if (!f.getName().equals("core_generic.ta") && f.isFile()) {
					f.delete();
				}
			}

		}
	}
	
	/**
	 * Test the right scripts are generated
	 * @throws IOException 
	 */
	@Test(groups={"squash"})
	public void testGenerateTaScriptContent() throws IOException {
		File generationFolder = Paths.get(SeleniumTestsContextManager.getRootPath(), "data", "core", "squash-ta").toFile();
		TaScriptGenerator scriptGenerator = new TaScriptGenerator("core", SeleniumTestsContextManager.getRootPath(), generationFolder.getPath());
		
		try {
			// compare non generic cucumber
			scriptGenerator.generateTaScripts();
			String content = FileUtils.readFileToString(Paths.get(generationFolder.getPath(), "src", "squashTA", "tests", "g__squashta.xml_SquashTa_cucumber_tags.ta").toFile());
			Assert.assertTrue(content.contains("application:core"));
			Assert.assertTrue(content.contains("testngFile:%STF_HOME%"));
			Assert.assertTrue(content.contains("TC_CUF_cucumberTest:"));
			Assert.assertTrue(content.contains("testngName:SquashTa_cucumber_tags"));
			
			// compare generic cucumber
			content = FileUtils.readFileToString(Paths.get(generationFolder.getPath(), "src", "squashTA", "tests", "g__squashta.xml_SquashTa_cucumber_generic_core__text.ta").toFile());
			Assert.assertTrue(content.contains("application:core"));
			Assert.assertTrue(content.contains("testngFile:%STF_HOME%/data/core/testng/squashta.xml"));
			Assert.assertTrue(content.contains("TC_CUF_cucumberTest:-DcucumberTests\"=core_&nbsp;<text>\""));
			Assert.assertTrue(content.contains("testngName:SquashTa_cucumber_generic"));
		} finally {
			for (File f: Paths.get(generationFolder.getPath(), "src", "squashTA", "tests").toFile().listFiles()) {
				if (!f.getName().equals("core_generic.ta") && f.isFile()) {
					f.delete();
				}
			}
			
		}
	}
}
