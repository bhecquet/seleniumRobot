package com.seleniumtests.ut.reporter.logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.reporter.logger.HarCapture;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.har.Har;
import com.seleniumtests.util.har.Page;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;

public class TestHarCapture extends GenericTest {

	@AfterMethod(alwaysRun = true)
	public void reset() {
		Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "main-networkCapture.har").toFile().delete();
	}

	@Test(groups={"ut"})
	public void testBuildHarLog() throws IOException {
		TestStepManager.setCurrentRootTestStep(new TestStep("step", "step", this.getClass(), null, new ArrayList<>(), true));
		Har har = new Har();
		har.getLog().addPage(new Page("", "title", "a title"));
		HarCapture capture = new HarCapture(har, "main");
		Assert.assertEquals(capture.buildHarLog(), "Network capture 'main' browser: <a href='main-networkCapture.har'>HAR file</a>");
	}


	@Test(groups={"ut"})
	public void testToJson() throws IOException {
		Har har = new Har();
		har.getLog().addPage(new Page("", "title", "a title"));
		HarCapture capture = new HarCapture(har, "main");

		JSONObject json = capture.toJson();
		Assert.assertEquals(json.getString("type"), "networkCapture");
		Assert.assertEquals(json.getString("name"), "main");
		File harFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "main-networkCapture.har").toFile();
		Assert.assertTrue(harFile.exists());
		Assert.assertTrue(json.isNull("id"));
	}

	@Test(groups={"ut"})
	public void testToJsonWithId() throws IOException {
		Har har = new Har();
		har.getLog().addPage(new Page("", "title", "a title"));
		HarCapture capture = new HarCapture(har, "main");
		capture.getFileContent().setId(2);

		JSONObject json = capture.toJson();
		Assert.assertEquals(json.getString("type"), "networkCapture");
		Assert.assertEquals(json.getString("name"), "main");
		File harFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "main-networkCapture.har").toFile();
		Assert.assertTrue(harFile.exists());
		Assert.assertEquals(json.getInt("id"), 2);
	}

	@Test(groups={"ut"})
	public void testEncodeTo() throws IOException {
		Har har = new Har();
		har.getLog().addPage(new Page("", "title", "a title"));
		HarCapture capture = new HarCapture(har, "main");
		HarCapture encodedHarCapture = capture.encodeTo("xml");
		Assert.assertEquals(encodedHarCapture.getFile(), capture.getFile());
		Assert.assertEquals(encodedHarCapture.getPosition(), capture.getPosition());
		Assert.assertEquals(encodedHarCapture.getTimestamp(), capture.getTimestamp());
		Assert.assertEquals(encodedHarCapture.getName(), "main");
	}

	@Test(groups={"ut"}, expectedExceptions = CustomSeleniumTestsException.class, expectedExceptionsMessageRegExp = ".*only escaping of 'xml', 'html', 'csv', 'json' is allowed.*")
	public void testEncodeToWrongFormat() throws IOException {
		Har har = new Har();
		har.getLog().addPage(new Page("", "title", "a title"));
		HarCapture capture = new HarCapture(har, "main");
		capture.encodeTo("bla");
	}
}
