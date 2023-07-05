package com.seleniumtests.ut.reporter.logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.reporter.logger.HarCapture;
import com.seleniumtests.reporter.logger.TestStep;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarLog;
import net.lightbody.bmp.core.har.HarPage;

public class TestHarCapture extends GenericTest {


	@Test(groups={"ut"})
	public void testBuildHarLog() throws IOException {
		TestStepManager.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>(), true));
		Har har = new Har(new HarLog());
		har.getLog().addPage(new HarPage("title", "a title"));
		HarCapture capture = new HarCapture(har, "main");
		Assert.assertEquals(capture.buildHarLog(), "Network capture 'main' browser: <a href='main-networkCapture.har'>HAR file</a>");
	}
	

	@Test(groups={"ut"})
	public void testRelocateHar() throws IOException {

		try {
			Har har = new Har(new HarLog());
			har.getLog().addPage(new HarPage("title", "a title"));
			HarCapture capture = new HarCapture(har, "main");
			File initialFile = capture.getFile();

			// relocate
			capture.relocate(SeleniumTestsContextManager.getThreadContext().getOutputDirectory() + "_moved");
			File movedFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory() + "_moved", "main-networkCapture.har").toFile();
			
			Assert.assertTrue(movedFile.exists());
			Assert.assertFalse(initialFile.exists());
			Assert.assertEquals(capture.getFile(), movedFile);
		} finally {
			FileUtils.deleteQuietly(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
			FileUtils.deleteQuietly(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory() + "_moved"));
		}
	}
	
	/**
	 * Test no error is raised is outputDirectory is null
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testRelocateHarNull() throws IOException {
		
		try {
			Har har = new Har(new HarLog());
			har.getLog().addPage(new HarPage("title", "a title"));
			HarCapture capture = new HarCapture(har, "main");

			// relocate
			capture.relocate(null);
			
		} finally {
			FileUtils.deleteQuietly(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		}
	}
	
	@Test(groups={"ut"})
	public void testToJson() throws IOException {
		Har har = new Har(new HarLog());
		har.getLog().addPage(new HarPage("title", "a title"));
		HarCapture capture = new HarCapture(har, "main");
		
		JSONObject json = capture.toJson();
		Assert.assertEquals(json.getString("type"), "networkCapture");
		Assert.assertEquals(json.getString("name"), "main");
		File harFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "main-networkCapture.har").toFile();
		Assert.assertEquals(json.getString("file"), "<file:" + harFile.getAbsolutePath().replace("\\", "/") + ">");
	}
}
