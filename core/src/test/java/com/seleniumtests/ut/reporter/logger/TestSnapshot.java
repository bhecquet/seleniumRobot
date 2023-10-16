package com.seleniumtests.ut.reporter.logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	public void testBuildScreenshotString() throws IOException {
		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot = new ScreenShot(tmpImgFile, tmpHtmlFile, "");
		screenshot.setTitle("title");
		screenshot.setLocation("http://location");
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
				+ 		"<img id=\"%s\" src=\"%s\" style=\"width: 300px\">"
				+ 	"</a>"
				+ "</div>"
				+ "<div class=\"text-center\">main: title</div>"
				+ "<div class=\"text-center font-weight-lighter\"><a href='http://location' target=url>URL</a> | <a href='htmls/%s' target=html>HTML Source</a></div>", imageId, imageId, tmpImgFile.getName(), tmpHtmlFile.getName()));
	}
	
	@Test(groups={"ut"})
	public void testBuildScreenshotStringWithoutInfo() throws FileNotFoundException {
		ScreenShot screenshot = new ScreenShot(null);
		Snapshot snapshotLogger = new Snapshot(screenshot, "main", SnapshotCheckType.FALSE);
		String screenshotStr = snapshotLogger.buildScreenshotLog().replace("\n", "");
		Assert.assertEquals(screenshotStr, "<div class=\"text-center\">main</div>"
				+ "<div class=\"text-center font-weight-lighter\"></div>");
	}
	
	@Test(groups={"ut"})
	public void testBuildScreenshotStringWithoutImage() throws IOException {
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot = new ScreenShot(null, tmpHtmlFile, "");
		screenshot.setTitle("title");
		screenshot.setLocation("http://location");
		Snapshot snapshotLogger = new Snapshot(screenshot, "main", SnapshotCheckType.TRUE);
		String screenshotStr = snapshotLogger.buildScreenshotLog().replace("\n", "").replaceAll(">\\s+<", "><");
		Assert.assertEquals(screenshotStr, String.format("<div class=\"text-center\">main: title</div><div class=\"text-center font-weight-lighter\"><a href='http://location' target=url>URL</a> | <a href='htmls/%s' target=html>HTML Source</a></div>", tmpHtmlFile.getName()));
	}
	
	@Test(groups={"ut"})
	public void testBuildScreenshotStringWithoutSource() throws IOException {
		
		File tmpImgFile = File.createTempFile("img", ".png");
		ScreenShot screenshot = new ScreenShot(tmpImgFile, null, "");
		screenshot.setTitle("title");
		screenshot.setLocation("http://location");
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
				+ 		"<img id=\"%s\" src=\"%s\" style=\"width: 300px\">"
				+ 	"</a>"
				+ "</div>"
				+ "<div class=\"text-center\">main: title</div><div class=\"text-center font-weight-lighter\"><a href='http://location' target=url>URL</a></div>", imageId, imageId, tmpImgFile.getName()));
	}
	
	@Test(groups={"ut"})
	public void testBuildScreenshotStringWithoutLocation() throws IOException {
		
		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot = new ScreenShot(tmpImgFile, tmpHtmlFile, "");
		screenshot.setTitle("title");
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
				+ 		"<img id=\"%s\" src=\"%s\" style=\"width: 300px\">"
				+ 	"</a>"
				+ "</div>"
				+ "<div class=\"text-center\">main: title</div>"
				+ "<div class=\"text-center font-weight-lighter\"> | <a href='htmls/%s' target=html>HTML Source</a></div>", imageId, imageId, tmpImgFile.getName(), tmpHtmlFile.getName()));
	}
	
	@Test(groups={"ut"}, expectedExceptions = FileNotFoundException.class)
	public void testNullScreenshot() throws FileNotFoundException {
		new Snapshot(null, "main", SnapshotCheckType.TRUE);
	}
	
	@Test(groups={"ut"})
	public void testToJsonWithId() throws IOException {
		
		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot = new ScreenShot(tmpImgFile, tmpHtmlFile, "");
		screenshot.setTitle("title");
		screenshot.setLocation("http://location");
		Snapshot snapshotLogger = new Snapshot(screenshot, "main", SnapshotCheckType.TRUE);
		snapshotLogger.getScreenshot().getHtml().setId(2);
		snapshotLogger.getScreenshot().getImage().setId(3);
		
		JSONObject json = snapshotLogger.toJson();
		
		Assert.assertEquals(json.getString("type"), "snapshot");
		Assert.assertEquals(json.getString("url"), "http://location");
		Assert.assertEquals(json.getString("title"), "title");
		Assert.assertEquals(json.getString("name"), "main");
		Assert.assertEquals(json.getInt("idHtml"), 2);
		Assert.assertEquals(json.getInt("idImage"), 3);
		Assert.assertEquals(json.getString("snapshotCheckType"), "FULL");
	}
	@Test(groups={"ut"})
	public void testToJson() throws IOException {
		
		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot = new ScreenShot(tmpImgFile, tmpHtmlFile, "");
		screenshot.setTitle("title");
		screenshot.setLocation("http://location");
		Snapshot snapshotLogger = new Snapshot(screenshot, "main", SnapshotCheckType.TRUE);
		
		JSONObject json = snapshotLogger.toJson();
		
		Assert.assertEquals(json.getString("type"), "snapshot");
		Assert.assertEquals(json.getString("url"), "http://location");
		Assert.assertEquals(json.getString("title"), "title");
		Assert.assertEquals(json.getString("name"), "main");
		Assert.assertTrue(json.isNull("idHtml"));
		Assert.assertTrue(json.isNull("idImage"));
		Assert.assertEquals(json.getString("snapshotCheckType"), "FULL");
	}
}
