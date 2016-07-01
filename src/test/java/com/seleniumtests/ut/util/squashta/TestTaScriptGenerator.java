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

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.squashta.SquashTaTestDef;
import com.seleniumtests.util.squashta.TaScriptGenerator;

public class TestTaScriptGenerator {

	@BeforeClass(groups={"ut"})
	public void init(ITestContext testNGCtx) {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
	}
	
	
	/**
	 * Test parsing of feature files to see if all scenario / scenario outlines, except the excluded ones are get
	 */
	@Test(groups={"squash"})
	public void testParseFeatures() {
		
		TaScriptGenerator scriptGenerator = new TaScriptGenerator(SeleniumTestsContext.getRootPath(), "core");
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
		TaScriptGenerator scriptGenerator = new TaScriptGenerator(SeleniumTestsContext.getRootPath(), "core");
		List<SquashTaTestDef> testList = scriptGenerator.parseTestNgXml();
		Assert.assertTrue(testList.contains(new SquashTaTestDef(null, "SquashTa_cucumber_test", true, "Conge")));
		Assert.assertTrue(testList.contains(new SquashTaTestDef(null, "SquashTa_cucumber_tags", true, "@new")));
		Assert.assertTrue(testList.contains(new SquashTaTestDef(null, "SquashTa_cucumber_generic", true, "")));
		Assert.assertTrue(testList.contains(new SquashTaTestDef(null, "SquashTa_testPlan", false, "")));
		Assert.assertFalse(testList.contains(new SquashTaTestDef(null, "SquashTa_excluded", true, "")));
		Assert.assertTrue(testList.contains(new SquashTaTestDef(null, "Unit tests with context", false, "")));
	}
	
	/**
	 * Test if files which are no more valid are deleted
	 * @throws IOException
	 */
	@Test(groups={"squash"})
	public void testCleanGeneratedFiles() throws IOException {
		File tmpFolder = Paths.get(SeleniumTestsContext.getDataPath(), "tmp").toFile();
		tmpFolder.mkdirs();
		FileUtils.write(Paths.get(tmpFolder.getPath(), "a_file.ta").toFile(), "");
		FileUtils.write(Paths.get(tmpFolder.getPath(), "g__a_file.ta").toFile(), "");
		FileUtils.write(Paths.get(tmpFolder.getPath(), "g__an_other_file.ta").toFile(), "");
		TaScriptGenerator scriptGenerator = new TaScriptGenerator(SeleniumTestsContext.getRootPath(), "core");
		
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
		TaScriptGenerator scriptGenerator = new TaScriptGenerator(SeleniumTestsContext.getRootPath(), "core");
		File generationFolder = Paths.get(SeleniumTestsContext.getRootPath(), "data", "core", "squash-ta", "src", "squashTA", "tests").toFile();
		try {
			scriptGenerator.generateTaScripts();
			Assert.assertTrue(Paths.get(generationFolder.getPath(), "g__squashta.xml_SquashTa_cucumber_tags.ta").toFile().isFile());
			Assert.assertTrue(Paths.get(generationFolder.getPath(), "g__squashta.xml_SquashTa_cucumber_test.ta").toFile().isFile());
			Assert.assertTrue(Paths.get(generationFolder.getPath(), "g__squashta.xml_SquashTa_testPlan.ta").toFile().isFile());
			Assert.assertTrue(Paths.get(generationFolder.getPath(), "g__tu.xml_Unit tests with context.ta").toFile().isFile());
			Assert.assertTrue(Paths.get(generationFolder.getPath(), "g__squashta.xml_SquashTa_cucumber_generic_core__text.ta").toFile().isFile());
			Assert.assertTrue(Paths.get(generationFolder.getPath(), "core_generic.ta").toFile().isFile());
		} finally {
			for (File f: generationFolder.listFiles()) {
				if (!f.getName().equals("core_generic.ta")) {
					f.delete();
				}
			}

		}
	}
}
