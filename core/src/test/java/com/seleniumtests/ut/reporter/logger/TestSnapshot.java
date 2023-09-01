package com.seleniumtests.ut.reporter.logger;

import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.logger.Snapshot;

public class TestSnapshot extends GenericTest {


	@Test(groups={"ut"})
	public void testBuildScreenshotString() throws FileNotFoundException {
		ScreenShot screenshot = new ScreenShot();
		screenshot.setTitle("title");
		screenshot.setLocation("http://location");
		screenshot.setHtmlSourcePath("file.html");
		screenshot.setImagePath("file.png");
		Snapshot snapshotLogger = new Snapshot(screenshot, "main", SnapshotCheckType.TRUE);
		String screenshotStr = snapshotLogger.buildScreenshotLog().replace("\n", "").replaceAll(">\\s+<", "><");
		Matcher matcher = Pattern.compile(".*<img id\\=\"(.+)\" src\\=.*").matcher(screenshotStr);
		String imageId = "";
		if (matcher.matches()) {
			imageId = matcher.group(1);
		} else {
			throw new IndexOutOfBoundsException("no match");
		}
		
		Assert.assertEquals(screenshotStr, String.format("<div class=\"text-center\">"
				+ 	"<a href=\"#\" onclick=\"$('#imagepreview').attr('src', $('#%s').attr('src'));$('#imagemodal').modal('show');\">"
				+ 		"<img id=\"%s\" src=\"file.png\" style=\"width: 300px\">"
				+ 	"</a>"
				+ "</div>"
				+ "<div class=\"text-center\">main: title</div>"
				+ "<div class=\"text-center font-weight-lighter\"><a href='http://location' target=url>URL</a> | <a href='file.html' target=html>HTML Source</a></div>", imageId, imageId));
	}
	
	@Test(groups={"ut"})
	public void testBuildScreenshotStringWithoutInfo() throws FileNotFoundException {
		ScreenShot screenshot = new ScreenShot();
		Snapshot snapshotLogger = new Snapshot(screenshot, "main", SnapshotCheckType.FALSE);
		String screenshotStr = snapshotLogger.buildScreenshotLog().replace("\n", "");
		Assert.assertEquals(screenshotStr, "<div class=\"text-center\">main</div>"
				+ "<div class=\"text-center font-weight-lighter\"></div>");
	}
	
	@Test(groups={"ut"})
	public void testBuildScreenshotStringWithoutImage() throws FileNotFoundException {
		ScreenShot screenshot = new ScreenShot();
		screenshot.setTitle("title");
		screenshot.setLocation("http://location");
		screenshot.setHtmlSourcePath("file.html");
		Snapshot snapshotLogger = new Snapshot(screenshot, "main", SnapshotCheckType.TRUE);
		String screenshotStr = snapshotLogger.buildScreenshotLog().replace("\n", "").replaceAll(">\\s+<", "><");
		Assert.assertEquals(screenshotStr, "<div class=\"text-center\">main: title</div><div class=\"text-center font-weight-lighter\"><a href='http://location' target=url>URL</a> | <a href='file.html' target=html>HTML Source</a></div>");
	}
	
	@Test(groups={"ut"})
	public void testBuildScreenshotStringWithoutSource() throws FileNotFoundException {
		ScreenShot screenshot = new ScreenShot();
		screenshot.setTitle("title");
		screenshot.setLocation("http://location");
		screenshot.setImagePath("file.png");
		Snapshot snapshotLogger = new Snapshot(screenshot, "main", SnapshotCheckType.FALSE);
		String screenshotStr = snapshotLogger.buildScreenshotLog().replace("\n", "").replaceAll(">\\s+<", "><");
		
		Matcher matcher = Pattern.compile(".*<img id\\=\"(.+)\" src\\=.*").matcher(screenshotStr);
		String imageId = "";
		if (matcher.matches()) {
			imageId = matcher.group(1);
		} else {
			throw new IndexOutOfBoundsException("no match");
		}
		
		Assert.assertEquals(screenshotStr, String.format("<div class=\"text-center\">"
				+ 	"<a href=\"#\" onclick=\"$('#imagepreview').attr('src', $('#%s').attr('src'));$('#imagemodal').modal('show');\">"
				+ 		"<img id=\"%s\" src=\"file.png\" style=\"width: 300px\">"
				+ 	"</a>"
				+ "</div>"
				+ "<div class=\"text-center\">main: title</div><div class=\"text-center font-weight-lighter\"><a href='http://location' target=url>URL</a></div>", imageId, imageId));
	}
	
	@Test(groups={"ut"})
	public void testBuildScreenshotStringWithoutLocation() throws FileNotFoundException {
		ScreenShot screenshot = new ScreenShot();
		screenshot.setTitle("title");
		screenshot.setHtmlSourcePath("file.html");
		screenshot.setImagePath("file.png");
		Snapshot snapshotLogger = new Snapshot(screenshot, "main", SnapshotCheckType.TRUE);
		String screenshotStr = snapshotLogger.buildScreenshotLog().replace("\n", "").replaceAll(">\\s+<", "><");
		
		Matcher matcher = Pattern.compile(".*<img id\\=\"(.+)\" src\\=.*").matcher(screenshotStr);
		String imageId = "";
		if (matcher.matches()) {
			imageId = matcher.group(1);
		} else {
			throw new IndexOutOfBoundsException("no match");
		}
		Assert.assertEquals(screenshotStr, String.format("<div class=\"text-center\">"
				+ 	"<a href=\"#\" onclick=\"$('#imagepreview').attr('src', $('#%s').attr('src'));$('#imagemodal').modal('show');\">"
				+ 		"<img id=\"%s\" src=\"file.png\" style=\"width: 300px\">"
				+ 	"</a>"
				+ "</div>"
				+ "<div class=\"text-center\">main: title</div>"
				+ "<div class=\"text-center font-weight-lighter\"> | <a href='file.html' target=html>HTML Source</a></div>", imageId, imageId));
	}
	
	@Test(groups={"ut"}, expectedExceptions = FileNotFoundException.class)
	public void testNullScreenshot() throws FileNotFoundException {
		new Snapshot(null, "main", SnapshotCheckType.TRUE);
	}
	
	@Test(groups={"ut"})
	public void testToJsonWithId() throws FileNotFoundException {
		
		ScreenShot screenshot = new ScreenShot();
		screenshot.setTitle("title");
		screenshot.setLocation("http://location");
		screenshot.setHtmlSourcePath("file.html");
		screenshot.setImagePath("file.png");
		Snapshot snapshotLogger = new Snapshot(screenshot, "main", SnapshotCheckType.TRUE);
		snapshotLogger.getScreenshot().getHtml().setId(2);
		snapshotLogger.getScreenshot().getImage().setId(3);
		
		JSONObject json = snapshotLogger.toJson();
		
		Assert.assertEquals(json.getString("type"), "snapshot");
		Assert.assertEquals(json.getString("url"), "http://location");
		Assert.assertEquals(json.getString("title"), "title");
		Assert.assertEquals(json.getString("name"), "file.png");
		Assert.assertEquals(json.getInt("idHtml"), 2);
		Assert.assertEquals(json.getInt("idImage"), 3);
		Assert.assertEquals(json.getString("snapshotCheckType"), "FULL");
	}
	@Test(groups={"ut"})
	public void testToJson() throws FileNotFoundException {
		
		ScreenShot screenshot = new ScreenShot();
		screenshot.setTitle("title");
		screenshot.setLocation("http://location");
		screenshot.setHtmlSourcePath("file.html");
		screenshot.setImagePath("file.png");
		Snapshot snapshotLogger = new Snapshot(screenshot, "main", SnapshotCheckType.TRUE);
		
		JSONObject json = snapshotLogger.toJson();
		
		Assert.assertEquals(json.getString("type"), "snapshot");
		Assert.assertEquals(json.getString("url"), "http://location");
		Assert.assertEquals(json.getString("title"), "title");
		Assert.assertEquals(json.getString("name"), "file.png");
		Assert.assertTrue(json.isNull("idHtml"));
		Assert.assertTrue(json.isNull("idImage"));
		Assert.assertEquals(json.getString("snapshotCheckType"), "FULL");
	}
}
