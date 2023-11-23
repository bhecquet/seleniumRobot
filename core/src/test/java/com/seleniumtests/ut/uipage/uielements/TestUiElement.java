package com.seleniumtests.ut.uipage.uielements;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.openqa.selenium.Point;
import org.openqa.selenium.remote.ScreenshotException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.connectors.selenium.fielddetector.Field;
import com.seleniumtests.connectors.selenium.fielddetector.Label;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.SnapshotTarget;
import com.seleniumtests.uipage.uielements.ByUI;
import com.seleniumtests.uipage.uielements.ElementType;
import com.seleniumtests.uipage.uielements.UiElement;

import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

public class TestUiElement extends MockitoTest {
	
	public TestUiElement() throws IOException {
	}
	
	@Mock
	private ScreenshotUtil screenshotUtil;

	@Mock
	private CustomEventFiringWebDriver driver;
	
	@Mock
	private SeleniumRobotSnapshotServerConnector serverConnector;
	
	private Field field1;
	private Field fieldWithLabel;
	private Field field2;
	private Field field3;
	private Field field4;
	private Field field5;
	
	private Label label1Right;
	private Label label1Left;
	private Label label1Above;
	private Label label1Below;
	private Label label2;
	private Label labelInside;
	
	private ScreenShot screenCapture;
	private ScreenShot browserCapture;

	private MockedStatic mockedWebUIDriver;
	private MockedStatic mockedCustomFiringWebDriver;
	private MockedStatic mockedSnapshotServerConnector;


	@BeforeMethod(groups= {"ut"})
	public void init() throws IOException {
		UiElement.resetPageInformation();
		
		/*
		 * Create fields and labels
		 * 
		 * 								field4
		 * 
		 * 								label1Below
		 * 
		 *	field5		label1Right		field1/labelInside		label1Left		field3
		 * 
		 * 								label1Above
		 * 
		 * 								field2
		 * 
		 */
		field1 = new Field(200, 300, 100, 120, null, "field"); // field to the left of "label1Left" and to the right of "label1Right"
		fieldWithLabel = new Field(100, 300, 100, 120, null, "field_with_label", field1);
		field2 = new Field(200, 300, 300, 320, null, "field"); // field above "label1Above"
		field3 = new Field(600, 700, 100, 120, null, "field"); // field to the right of "label1Left"
		field4 = new Field(200, 300, 0, 20, null, "field"); // field above "label1Below"
		field5 = new Field(10, 60, 100, 120, null, "field"); // field to the left of "label1Right"
		
		label1Right = new Label(100, 150, 100, 120, "label_with_field_on_right");
		label1Left = new Label(350, 400, 100, 120, "label_with_field_on_left");
		label1Above = new Label(200, 400, 200, 220, "label_with_field_above"); // label longer than field
		label1Below = new Label(200, 252, 70, 90, "label_with_field_below"); // label shorter than field
		label2 = new Label(100, 150, 0, 20, "label_without_field");
		labelInside = new Label(200, 250, 100, 120, "label_inside");

		mockedWebUIDriver = mockStatic(WebUIDriver.class);
		mockedCustomFiringWebDriver = mockStatic(CustomEventFiringWebDriver.class);
		mockedSnapshotServerConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);

		mockedWebUIDriver.when(() -> WebUIDriver.getWebDriver(anyBoolean())).thenReturn(driver);
		when(driver.getBrowserInfo()).thenReturn(new BrowserInfo(BrowserType.CHROME, "83.0"));
		
		File screenCaptureFile = createImageFromResource("tu/imageFieldDetection/screenCapture.png");
		File browserCaptureFile = createImageFromResource("tu/imageFieldDetection/browserCapture.png");
		File newScreenCaptureFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), screenCaptureFile.getName()).toFile();
		FileUtils.moveFile(screenCaptureFile, newScreenCaptureFile);
		File newBrowserCaptureFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), browserCaptureFile.getName()).toFile();
		FileUtils.moveFile(browserCaptureFile, newBrowserCaptureFile);
		
		screenCapture = new ScreenShot(newScreenCaptureFile, null, "");
		browserCapture = new ScreenShot(newBrowserCaptureFile, null, "");

	}

	@AfterMethod(groups= {"ut"}, alwaysRun = true)
	private void closeMocks() {
		mockedCustomFiringWebDriver.close();
		mockedWebUIDriver.close();
		mockedSnapshotServerConnector.close();
	}
	
	private void initDetector(UiElement element, List<Field> fields, List<Label> labels) {
		// mock part of viewport detection
		doReturn(screenshotUtil).when(element).getScreenshotUtil();
		when(screenshotUtil.capture(SnapshotTarget.PAGE, ScreenShot.class, true)).thenReturn(browserCapture);
		when(screenshotUtil.capture(SnapshotTarget.MAIN_SCREEN, ScreenShot.class, true)).thenReturn(screenCapture);

		mockedSnapshotServerConnector.when(() -> SeleniumRobotSnapshotServerConnector.getInstance()).thenReturn(serverConnector);
		
		// build detect response from fields and labels
		JSONObject detectResult = new JSONObject();
		detectResult.put("version", "aaa");
		detectResult.put("error", (String)null);
		detectResult.put("fileName", "foo.png");
		detectResult.put("fields", new JSONArray(fields.stream().map(Field::toJson).collect(Collectors.toList())));
		detectResult.put("labels", new JSONArray(labels.stream().map(Label::toJson).collect(Collectors.toList())));
		when(serverConnector.detectFieldsInPicture(browserCapture)).thenReturn(detectResult);

	}
	
	@Test(groups= {"ut"})
	public void testFindElement() {
		
		UiElement element = spy(new UiElement(ByUI.type(ElementType.TEXT_FIELD).toTheRightOfLabel("label_with_field_on_right")));
		
		initDetector(element, Arrays.asList(field1, fieldWithLabel, field2), Arrays.asList(label1Right, label2));
		
		element.findElement();
		
		// check the right field is found
		Assert.assertEquals(element.getDetectedObjectRectangle(), new Rectangle(200, 215, 100, 20));
		
		// check offsetPerPage, fieldsPerPage and labelsPerPage has been updated
		Assert.assertTrue(UiElement.getFieldsPerPage().containsKey(null)); // in the case of unit tests, "origin" of the element cannot be computed and is replaced by None
		Assert.assertEquals(UiElement.getFieldsPerPage().get(null).size(), 3);
		
		Assert.assertTrue(UiElement.getLabelsPerPage().containsKey(null));
		Assert.assertEquals(UiElement.getLabelsPerPage().get(null).size(), 2);
		
		Assert.assertTrue(UiElement.getOffsetPerPage().containsKey(null));
		Assert.assertEquals(UiElement.getOffsetPerPage().get(null), new Point(0, 115));
		
		// check field and label position has been changed to be relative to screen, not to browser window
		Assert.assertEquals(UiElement.getFieldsPerPage().get(null).get(0).getRectangle(), new Rectangle(200, 215, 100, 20));
		Assert.assertEquals(UiElement.getFieldsPerPage().get(null).get(1).getRectangle(), new Rectangle(100, 215, 200, 20));
		Assert.assertEquals(UiElement.getFieldsPerPage().get(null).get(2).getRectangle(), new Rectangle(200, 415, 100, 20));
		
		Assert.assertEquals(UiElement.getLabelsPerPage().get(null).get(0).getRectangle(), new Rectangle(100, 215, 50, 20));
		Assert.assertEquals(UiElement.getLabelsPerPage().get(null).get(1).getRectangle(), new Rectangle(100, 115, 50, 20));
	}
	
	/**
	 * Check that if doing multiple searches on the same page, image field detector is called only once
	 */
	@Test(groups= {"ut"})
	public void testFindElementSearchedOnlyOnce() {
		
		UiElement element = spy(new UiElement(ByUI.type(ElementType.TEXT_FIELD).toTheRightOfLabel("label_with_field_on_right")));
		
		initDetector(element, Arrays.asList(field1, fieldWithLabel, field2), Arrays.asList(label1Right, label2));
		
		element.findElement();
		element.findElement();
		
		verify(serverConnector).detectFieldsInPicture(browserCapture);
	}
	
	/**
	 * If element is created with the "resetSearch" flag set to true, then each "findElement" will call imageFieldDetector to refresh page
	 * This is useful if page changes (a field appears) when some action is done since the last search
	 */
	@Test(groups= {"ut"})
	public void testFindElementSearchTwiceIfReset() {
		
		UiElement element = spy(new UiElement(ByUI.type(ElementType.TEXT_FIELD).toTheRightOfLabel("label_with_field_on_right"), true));
		

		initDetector(element, Arrays.asList(field1, fieldWithLabel, field2), Arrays.asList(label1Right, label2));
		
		element.findElement();
		element.findElement();
		
		verify(serverConnector, times(2)).detectFieldsInPicture(browserCapture);
	}
	
	/**
	 * Search element when label is on the left of field
	 */
	@Test(groups= {"ut"})
	public void testFindElementLabelOnLeft() {
		
		UiElement element = spy(new UiElement(ByUI.type(ElementType.TEXT_FIELD).toTheRightOfLabel("label_with_field_on_right")));
		

		initDetector(element, Arrays.asList(field1, fieldWithLabel, field2, field3, field4, field5), Arrays.asList(label1Right, label1Left, label1Above, label1Below, labelInside, label2));
		
		element.findElement();
		
		// check the right field is found
		Assert.assertEquals(element.getDetectedObjectRectangle(), new Rectangle(200, 215, 100, 20));
	}
	
	/**
	 * Search element when label is on the right of field
	 */
	@Test(groups= {"ut"})
	public void testFindElementLabelOnRight() {
		
		UiElement element = spy(new UiElement(ByUI.type(ElementType.TEXT_FIELD).toTheLeftOfLabel("label_with_field_on_left")));
		
		initDetector(element, Arrays.asList(field1, fieldWithLabel, field2, field3, field4, field5), Arrays.asList(label1Right, label1Left, label1Above, label1Below, labelInside, label2));
		
		element.findElement();
		
		// check the right field is found
		Assert.assertEquals(element.getDetectedObjectRectangle(), new Rectangle(200, 215, 100, 20));
	}
	
	/**
	 * Search element when label is above the field
	 */
	@Test(groups= {"ut"})
	public void testFindElementLabelAbove() {
		
		UiElement element = spy(new UiElement(ByUI.type(ElementType.TEXT_FIELD).belowLabel("label_with_field_below")));
		
		initDetector(element, Arrays.asList(field1, fieldWithLabel, field2, field3, field4, field5), Arrays.asList(label1Right, label1Left, label1Above, label1Below, labelInside, label2));
		
		element.findElement();
		
		// check the right field is found
		Assert.assertEquals(element.getDetectedObjectRectangle(), new Rectangle(200, 215, 100, 20));
	}
	
	/**
	 * Search element when label is below the field
	 */
	@Test(groups= {"ut"})
	public void testFindElementLabelBelow() {
		
		UiElement element = spy(new UiElement(ByUI.type(ElementType.TEXT_FIELD).aboveLabel("label_with_field_above")));
		
		initDetector(element, Arrays.asList(field1, fieldWithLabel, field2, field3, field4, field5), Arrays.asList(label1Right, label1Left, label1Above, label1Below, labelInside, label2));
		
		element.findElement();
		
		// check the right field is found
		Assert.assertEquals(element.getDetectedObjectRectangle(), new Rectangle(200, 215, 100, 20));
	}
	
	/**
	 * Search element when label is below the field
	 */
	@Test(groups= {"ut"})
	public void testFindElementLabelInside() {
		
		UiElement element = spy(new UiElement(ByUI.type(ElementType.TEXT_FIELD).textMatching("label_inside")));
		
		initDetector(element, Arrays.asList(field1, fieldWithLabel, field2, field3, field4, field5), Arrays.asList(label1Right, label1Left, label1Above, label1Below, labelInside, label2));
		
		element.findElement();
		
		// check the right field is found
		Assert.assertEquals(element.getDetectedObjectRectangle(), new Rectangle(200, 215, 100, 20));
	}
	
	/**
	 * Check error is raised when the wrong type is specified, even if a field could be found
	 */
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "No field could be found matching search criteria \\[ByUI\\(type='BUTTON', text='label_inside'\\)\\]")
	public void testFindElementWrongType() {
		
		UiElement element = spy(new UiElement(ByUI.type(ElementType.BUTTON).textMatching("label_inside")));
		
		initDetector(element, Arrays.asList(field1, fieldWithLabel, field2, field3, field4, field5), Arrays.asList(label1Right, label1Left, label1Above, label1Below, labelInside, label2));
		
		element.findElement();
		
		// check the right field is found
		Assert.assertEquals(element.getDetectedObjectRectangle(), new Rectangle(200, 215, 100, 20));
	}
	
	/**
	 * Element cannot be found
	 */
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "No field could be found matching search criteria \\[ByUI\\(type='TEXT_FIELD', leftOf='label_with_field_above'\\)\\]")
	public void testFindElementNotFound() {
		
		UiElement element = spy(new UiElement(ByUI.type(ElementType.TEXT_FIELD).toTheLeftOfLabel("label_with_field_above")));
		
		initDetector(element, Arrays.asList(field1, fieldWithLabel, field2, field3, field4, field5), Arrays.asList(label1Right, label1Left, label1Above, label1Below, labelInside, label2));
		
		element.findElement();
	}
	
	/**
	 * Label cannot be found
	 */
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "No label could be found matching search criteria \\[ByUI\\(type='TEXT_FIELD', leftOf='label_does_not_exist'\\)\\]")
	public void testFindElementNoLabelFound() {
		
		UiElement element = spy(new UiElement(ByUI.type(ElementType.TEXT_FIELD).toTheLeftOfLabel("label_does_not_exist")));
		
		initDetector(element, Arrays.asList(field1, fieldWithLabel, field2, field3, field4, field5), Arrays.asList(label1Right, label1Left, label1Above, label1Below, labelInside, label2));
		
		element.findElement();
	}
	
	@Test(groups= {"ut"}, expectedExceptions = ScreenshotException.class)
	public void testFindElementNoScreenshotCapture() {
		
		UiElement element = spy(new UiElement(ByUI.type(ElementType.TEXT_FIELD).toTheRightOfLabel("label_with_field_on_right")));
		
		// mock part of viewport detection
		doReturn(screenshotUtil).when(element).getScreenshotUtil();
		when(screenshotUtil.capture(SnapshotTarget.PAGE, ScreenShot.class, true)).thenReturn(null);
		when(screenshotUtil.capture(SnapshotTarget.MAIN_SCREEN, ScreenShot.class, true)).thenReturn(screenCapture);

		// build detect response from fields and labels
		JSONObject detectResult = new JSONObject();
		detectResult.put("version", "aaa");
		detectResult.put("error", (String) null);
		detectResult.put("fileName", "foo.png");
		detectResult.put("fields", new JSONArray(Arrays.asList(field1, fieldWithLabel, field2).stream().map(Field::toJson).collect(Collectors.toList())));
		detectResult.put("labels", new JSONArray(Arrays.asList(label1Right, label2).stream().map(Label::toJson).collect(Collectors.toList())));
		when(serverConnector.detectFieldsInPicture(browserCapture)).thenReturn(detectResult);

		element.findElement();

		
	}
	
	/**
	 * Check we cannot search if element type is not defined
	 */
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "Element type is mandatory to search a field")
	public void testFindElementNoElementType() {
		UiElement element = new UiElement(ByUI.toRightOf("foo"));
		element.findElement();
	}
	
	/**
	 * Check we cannot search no label is given
	 */
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "At least one of 'above', 'below', 'rightOf', 'leftOf', 'text' must be defined")
	public void testFindElementNoLabel() {
		UiElement element = new UiElement(ByUI.type(ElementType.TEXT_FIELD));
		element.findElement();
	}
	
	@Test(groups= {"ut"})
	public void testClickAt() {
		
		UiElement element = spy(new UiElement(ByUI.type(ElementType.TEXT_FIELD).toTheRightOfLabel("label_with_field_on_right")));
		
		initDetector(element, Arrays.asList(field1, fieldWithLabel, field2, field3, field4, field5), Arrays.asList(label1Right, label1Left, label1Above, label1Below, labelInside, label2));
		
		doReturn(driver).when(element).getDriver();
		when(driver.getScrollPosition()).thenReturn(new Point(0, 100));
		
		element.clickAt(0, 0);
		
		// check we try to scroll at top/left position of the found element
		verify(driver).scrollTo(200, 100);
		
		// check click is done at the right position taking into account
		// - the element positions (field1 center is at (250, 110) in web page
		// - scroll position in page (0, 100) as defined in this test
		// - viewport position in screen (0, 115)
		mockedCustomFiringWebDriver.verify(() -> CustomEventFiringWebDriver.leftClicOnDesktopAt(eq(true), eq(250), eq(125), eq(DriverMode.LOCAL), eq(null)));
	}
	
	@Test(groups= {"ut"})
	public void testDoubleClickAt() {
		
		UiElement element = spy(new UiElement(ByUI.type(ElementType.TEXT_FIELD).toTheRightOfLabel("label_with_field_on_right")));
		
		initDetector(element, Arrays.asList(field1, fieldWithLabel, field2, field3, field4, field5), Arrays.asList(label1Right, label1Left, label1Above, label1Below, labelInside, label2));
		
		doReturn(driver).when(element).getDriver();
		when(driver.getScrollPosition()).thenReturn(new Point(0, 100));
		
		element.doubleClickAt(10, 15);
		
		// check we try to scroll at top/left position of the found element
		verify(driver).scrollTo(200, 100);
		
		// check click is done at the right position taking into account
		// - the element positions (field1 center is at (250, 110) in web page
		// - scroll position in page (0, 100) as defined in this test
		// - viewport position in screen (0, 115)
		// - click offset (10, 15)
		mockedCustomFiringWebDriver.verify(() -> CustomEventFiringWebDriver.doubleClickOnDesktopAt(eq(true), eq(260), eq(140), eq(DriverMode.LOCAL), eq(null)));
	}
	
	@Test(groups= {"ut"})
	public void testRightClickAt() {
		
		UiElement element = spy(new UiElement(ByUI.type(ElementType.TEXT_FIELD).toTheRightOfLabel("label_with_field_on_right")));
		
		initDetector(element, Arrays.asList(field1, fieldWithLabel, field2, field3, field4, field5), Arrays.asList(label1Right, label1Left, label1Above, label1Below, labelInside, label2));
		
		doReturn(driver).when(element).getDriver();
		when(driver.getScrollPosition()).thenReturn(new Point(0, 0));
		
		element.rightClickAt(10, 15);
		
		// check we try to scroll at top/left position of the found element
		verify(driver).scrollTo(200, 100);
		
		// check click is done at the right position taking into account
		// - the element positions (field1 center is at (250, 110) in web page
		// - scroll position in page (0, 100) as defined in this test
		// - viewport position in screen (0, 115)
		// - click offset (10, 15)
		mockedCustomFiringWebDriver.verify(() -> CustomEventFiringWebDriver.rightClicOnDesktopAt(eq(true), eq(260), eq(240), eq(DriverMode.LOCAL), eq(null)));
	}

	@Test(groups= {"ut"})
	public void testSendKeys() {
		
		UiElement element = spy(new UiElement(ByUI.type(ElementType.TEXT_FIELD).toTheRightOfLabel("label_with_field_on_right")));
		
		initDetector(element, Arrays.asList(field1, fieldWithLabel, field2, field3, field4, field5), Arrays.asList(label1Right, label1Left, label1Above, label1Below, labelInside, label2));
		
		doReturn(driver).when(element).getDriver();
		when(driver.getScrollPosition()).thenReturn(new Point(0, 100));
		
		element.sendKeys(10, 10, "foo");
		
		// check we try to scroll at top/left position of the found element
		verify(driver).scrollTo(200, 100);
		
		// check click is done at the right position taking into account
		// - the element positions (field1 center is at (250, 110) in web page
		// - scroll position in page (0, 100) as defined in this test
		// - viewport position in screen (0, 115)
		mockedCustomFiringWebDriver.verify(() -> CustomEventFiringWebDriver.leftClicOnDesktopAt(eq(true), eq(260), eq(135), eq(DriverMode.LOCAL), eq(null)));
		mockedCustomFiringWebDriver.verify(() -> CustomEventFiringWebDriver.writeToDesktop(eq("foo"), eq(DriverMode.LOCAL), eq(null)));
	}
	
	@Test(groups= {"ut"})
	public void testIsElementPresent() {
		
		UiElement element = spy(new UiElement(ByUI.type(ElementType.TEXT_FIELD).toTheRightOfLabel("label_with_field_on_right")));
		
		initDetector(element, Arrays.asList(field1, fieldWithLabel, field2, field3, field4, field5), Arrays.asList(label1Right, label1Left, label1Above, label1Below, labelInside, label2));
		
		Assert.assertTrue(element.isElementPresent());
	}
	
	@Test(groups= {"ut"})
	public void testIsElementNotPresent() {
		
		UiElement element = spy(new UiElement(ByUI.type(ElementType.TEXT_FIELD).toTheRightOfLabel("label_with_field_not_present")));
		
		initDetector(element, Arrays.asList(field1, fieldWithLabel, field2, field3, field4, field5), Arrays.asList(label1Right, label1Left, label1Above, label1Below, labelInside, label2));
		
		Assert.assertFalse(element.isElementPresent());
	}

}
