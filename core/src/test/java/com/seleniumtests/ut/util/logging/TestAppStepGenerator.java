package com.seleniumtests.ut.util.logging;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.logging.AppStepsGenerator;

public class TestAppStepGenerator extends GenericTest {

	@Test(groups={"ut"})
	public void testFileAnalysisStepMethods() throws IOException {
		List<AppStepsGenerator.TestStep> steps = new AppStepsGenerator().analyzeFile(createFileFromResource("tu/JPetStoreHome.java"));
		
		Assert.assertEquals(steps.size(), 4);
		Assert.assertEquals(steps.get(0).getStepMethod(), "goToFish");
		Assert.assertEquals(steps.get(1).getStepMethod(), "goToSonarLanguageList");
		Assert.assertEquals(steps.get(2).getStepMethod(), "goToMockedSonarLanguageList");
		Assert.assertEquals(steps.get(3).getStepMethod(), "goToFishFromHeader");
	}
	
	/**
	 * Test with cucumber annotation and without
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testFileAnalysisStepNames() throws IOException {
		List<AppStepsGenerator.TestStep> steps = new AppStepsGenerator().analyzeFile(createFileFromResource("tu/JPetStoreHome.java"));
		
		Assert.assertEquals(steps.get(0).getStepName(), "When Cliquer sur le lien 'FISH'");
		Assert.assertEquals(steps.get(1).getStepName(), "public LanguageList goToSonarLanguageList()");
		Assert.assertEquals(steps.get(3).getStepName(), "public FishList goToFishFromHeader(String param)");
		
		Assert.assertEquals(steps.get(0).getStepDetails(), "Go to fish page\n" +
				"@return\n" +
				"@throws Exception");
	}
	
	/**
	 * Test with and without comment for method
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testFileAnalysisStepDetails() throws IOException {
		List<AppStepsGenerator.TestStep> steps = new AppStepsGenerator().analyzeFile(createFileFromResource("tu/JPetStoreHome.java"));
		
		Assert.assertEquals(steps.get(0).getStepDetails(), "Go to fish page\n" +
				"@return\n" +
				"@throws Exception");
		Assert.assertEquals(steps.get(3).getStepDetails(), "");
	}
	
	@Test(groups={"ut"})
	public void testGetSourceFiles() {
		File resourceDir = new File(TestAppStepGenerator.class.getClassLoader().getResource("tu/JPetStoreHome.java").getFile()).getParentFile();
		List<File> javaFiles = new AppStepsGenerator().getSourceFiles(resourceDir);
		Collections.sort(javaFiles);
		Assert.assertEquals(javaFiles.size(), 3);
		Assert.assertEquals(javaFiles.get(0).getName(), "Catalog.java");
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testGetSourceFilesWrongPath() {
		new AppStepsGenerator().getSourceFiles(new File("/home/test/src"));
	}
	
	@Test(groups={"ut"})
	public void testTxtFormat() throws IOException {
		File resourceDir = new File(TestAppStepGenerator.class.getClassLoader().getResource("tu/JPetStoreHome.java").getFile()).getParentFile();
		String output = new AppStepsGenerator().generate(resourceDir);
		System.out.println(output);
		Assert.assertTrue(output.contains("\tWhen Cliquer sur le lien 'FISH'"));
		Assert.assertTrue(output.contains("\"Go to fish page"));
		
		// check IntegrationTestsClass has not been taken into account
		Assert.assertFalse(output.contains("addAnimalToCart"));
	}

}
