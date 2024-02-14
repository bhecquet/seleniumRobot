package com.seleniumtests.ut.uipage.htmlelements;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.uipage.htmlelements.ElementInfo;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.util.imaging.ImageProcessor;

public class TestElementInfo extends MockitoTest {

	private static String JSON_INFO = "{" + 
			"	\"path\": \"D:\\\\Dev\\\\seleniumRobot\\\\seleniumRobot-core\\\\core\\\\cache\\\\core\\\\com.seleniumtests.it.driver.support.pages.DriverTestPage\\\\button_after_scroll.json\"," + 
			"	\"lastUpdate\": {" + 
			"		\"date\": {" + 
			"			\"year\": 2019," + 
			"			\"month\": 2," + 
			"			\"day\": 13" + 
			"		}," + 
			"		\"time\": {" + 
			"			\"hour\": 11," + 
			"			\"minute\": 40," + 
			"			\"second\": 27," + 
			"			\"nano\": 211000000" + 
			"		}" + 
			"	}," + 
			"	\"name\": \"button after scroll\"," + 
			"	\"id\": \"com.seleniumtests.it.driver.support.pages.DriverTestPage/button_after_scroll\"," + 
			"	\"locator\": \"By.id: buttonScroll\"," + 
			"	\"tagName\": \"button\"," + 
			"	\"text\": \"set\"," + 
		"		\"coordX\": 8," + 
		"		\"coordY\": 1829," + 
		"		\"height\": 22," + 
		"		\"width\": 39," + 
			"	\"b64Image\": \"iVBORw0KGgoAAAANSUhEUgAAAE8AAAA+CAIAAADlKNG1AAACAUlEQVR42u2YzUoCURiGz/2oYLfiz8q6hzajuNUWNhsRIRQvQClahUhJm3ZWEEVI1KIYXagZmo4GGtg3moo/cwr0NMd4Hwad883xe3lmhpEzbGACY7xtQ2GwhS1sYQtb2MIWtrCFLWxhC1vYwha2sIWtTLaW8/yi5c8vjo5P1rj9YBuPxwOBwK5IqD+lLEZrpVq13vy7a7uvqrFYrFKpfIqE+lMKZS3a/umdrChKrVbr9XotkVB/SqEsi23pNqNz3xIPpVCW9bb9fv9dPJQii21TPBLZNlblcIdt7V3zZshiS4+Qt1XJbDNn+Io3g1Jksa1zIJEJ25m50rBwGXbOz1hEIttXMwohJ3OGCqNB2udLz5SMXaNkHJpOW44stt1u98mMfNDBmDc1V5kUJoOUlzmC+ScOlCKL7SMHEhnjTc0Mp0Wjag+e8drIYtvpdB5+Q9LDmCc5+lxyzB445f2aUmSxLZqRcNv9ue/9nN9uDOiLTYo5v3u4m3Az5k4UOchiq+v6vSkHruk9a1OyC8VxLavYhmPXgUkjSpHF9k48sti22+1b8VCK9ba0CtM0jf79b0RC/SnF+hWfqqrRaLRcLrdFQv0pRbV8NU9EIhE660Lf1FB/SrH+TY216PpHqdpY77axL0v/2bWFLWxhC1vYwha2sIUtbGELW9jCFrawHQy+AKNXbZ/fKJeiAAAAAElFTkSuQmCC\"," + 
			"	\"attributes\": {" + 
			"		\"onclick\": \"javascript:addText(\\u0027text5\\u0027, \\u0027a text\\u0027);\"," + 
			"		\"id\": \"buttonScroll\"" + 
			"	}," + 
			"	\"tagConfidence\": 0.0," + 
			"	\"textConfidence\": 0.0," + 
			"	\"rectangleConfidence\": 0.0," + 
			"	\"b64ImageConfidence\": 0.0," + 
			"	\"totalSearch\": 8," + 
			"	\"tagStability\": 4," + 
			"	\"textStability\": 4," + 
			"	\"rectangleStability\": 4," + 
			"	\"b64ImageStability\": 0," + 
			"	\"attributesStability\": {" + 
			"		\"onclick\": 5," + 
			"		\"id\": 5" + 
			"	}" + 
			"}";
	
	private static String JSON_INFO_FOO = "{" + 
			"	\"path\": \"D:\\\\foo.json\"," + 
			"	\"lastUpdate\": {" + 
			"		\"date\": {" + 
			"			\"year\": 2019," + 
			"			\"month\": 2," + 
			"			\"day\": 13" + 
			"		}," + 
			"		\"time\": {" + 
			"			\"hour\": 11," + 
			"			\"minute\": 40," + 
			"			\"second\": 27," + 
			"			\"nano\": 211000000" + 
			"		}" + 
			"	}," + 
			"	\"name\": \"mylabel\"," + 
			"	\"id\": \"foo.bar.Page/mylabel\"," + 
			"	\"locator\": \"By.id: foo\"," + 
			"	\"tagName\": \"h1\"," + 
			"	\"text\": \"sometext\"," + 
		"		\"coordX\": 10," + 
		"		\"coordY\": 20," + 
		"		\"height\": 30," + 
		"		\"width\": 40," + 
			"	\"b64Image\": \"ABCD\"," + 
			"	\"attributes\": {" + 
			"		\"class\": \"someClass\"," + 
			"		\"id\": \"foo\"" + 
			"	}," + 
			"	\"tagConfidence\": 0.0," + 
			"	\"textConfidence\": 0.0," + 
			"	\"rectangleConfidence\": 0.0," + 
			"	\"b64ImageConfidence\": 0.0," + 
			"	\"totalSearch\": 1," + 
			"	\"tagStability\": 0," + 
			"	\"textStability\": 0," + 
			"	\"rectangleStability\": 0," + 
			"	\"b64ImageStability\": 0," + 
			"	\"attributesStability\": {" + 
			"		\"class\": 0," + 
			"		\"id\": 0" + 
			"	}" + 
			"}";
	
	@Mock
	private HtmlElement htmlElement;
	
	@Mock
	private WebElement element;
	
	@Mock
	private CustomEventFiringWebDriver driver;
	
	@Mock
	private BufferedImage image;

	private MockedStatic mockedImageProcessor;
	
	@BeforeMethod(groups={"ut"})
	private void init() throws Exception {
		mockedImageProcessor = mockStatic(ImageProcessor.class);
		
		// delete all previously created information
		ElementInfo.purgeAll();
		SeleniumTestsContextManager.getThreadContext().setAdvancedElementSearch("full");
		
		when(htmlElement.getDriver()).thenReturn(driver);
		when(htmlElement.getLabel()).thenReturn("mylabel");
		when(htmlElement.getOrigin()).thenReturn("foo.bar.Page");
		when(htmlElement.getBy()).thenReturn(By.id("foo"));
		when(htmlElement.getRealElement()).thenReturn(element);
		
		when(element.getText()).thenReturn("sometext");
		when(element.getRect()).thenReturn(new Rectangle(10,  20, 30, 40));
		when(element.getTagName()).thenReturn("h1");
		
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("class", "someClass");
		attributes.put("id", "foo");
		when(driver.executeScript(ElementInfo.JAVASCRIPT_GET_ATTRIBUTES, element)).thenReturn(attributes);
		
		when(driver.getScrollPosition()).thenReturn(new Point(10, 10));
		mockedImageProcessor.when(() -> ImageProcessor.cropImage(any(BufferedImage.class), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(image);
		mockedImageProcessor.when(() -> ImageProcessor.toBase64(image)).thenReturn("ABCD");
		
		// remove elementInfo file that could have been created
		File elementInfoPath = ElementInfo.buildElementInfoPath(htmlElement);
		elementInfoPath.delete();
	}

	@AfterMethod(groups = {"ut"})
	public void closeMocks() {
		mockedImageProcessor.close();
	}
	
	/**
	 * Create a new elementInfo
	 */
	@Test(groups={"ut"})
	public void testCreateNewInfo() {
		ElementInfo elInfo = ElementInfo.getInstance(htmlElement);
		
		// check element info is created with basic information
		Assert.assertNotNull(elInfo);
		Assert.assertEquals(elInfo.getId(), "foo.bar.Page/mylabel");
		Assert.assertEquals(elInfo.getName(), "mylabel");
		Assert.assertEquals(elInfo.getLocator(), "By.id: foo");
		Assert.assertNull(elInfo.getTagName());
		Assert.assertNull(elInfo.getText());
		Assert.assertEquals((Integer)0, elInfo.getCoordX());
		Assert.assertEquals((Integer)0, elInfo.getCoordY());
		Assert.assertEquals((Integer)0, elInfo.getWidth());
		Assert.assertEquals((Integer)0, elInfo.getHeight());
		Assert.assertEquals(elInfo.getTotalSearch(), 0);
	}
	
	/**
	 * Do not create new ElementInfo, do not even search as we forbid this
	 */
	@Test(groups={"ut"})
	public void testCreateNewInfoWithFalseMode() {
		SeleniumTestsContextManager.getThreadContext().setAdvancedElementSearch("false");
		ElementInfo elInfo = ElementInfo.getInstance(htmlElement);
		
		// check element info is created with basic information
		Assert.assertNull(elInfo);
	}
	
	/**
	 * Create a new elementInfo in Full mode and Web content
	 * We should get all element information for web content
	 */
	@Test(groups={"ut"})
	public void testUpdateInfo() {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		
		ElementInfo elInfo = spy(ElementInfo.getInstance(htmlElement));
		doReturn(image).when(elInfo).getScreenshot();

		elInfo.updateInfo(htmlElement);
		
		// check element info is created with basic information
		Assert.assertNotNull(elInfo);
		Assert.assertEquals(elInfo.getText(), "sometext");
		Assert.assertEquals(elInfo.getCoordX(), (Integer)10);
		Assert.assertEquals(elInfo.getCoordY(),  (Integer)20);
		Assert.assertEquals(elInfo.getHeight(),  (Integer)30);
		Assert.assertEquals(elInfo.getWidth(),  (Integer)40);
		Assert.assertEquals(elInfo.getTotalSearch(), 1);
		Assert.assertEquals(elInfo.getTagName(), "h1");
		Assert.assertEquals(elInfo.getAttributes().get("class"), "someClass");
		Assert.assertEquals(elInfo.getB64Image(), "ABCD");
		
		// indicators are initialized
		Assert.assertEquals(elInfo.getTextStability(), 0);
		Assert.assertEquals(elInfo.getTagStability(), 0);
		Assert.assertEquals(elInfo.getRectangleStability(), 0);
	}
	
	/**
	 * create element information in dom mode. In this mode, element capture is not done
	 */
	@Test(groups={"ut"})
	public void testUpdateInfoInDomMode() {
		SeleniumTestsContextManager.getThreadContext().setAdvancedElementSearch("dom");
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		
		ElementInfo elInfo = spy(ElementInfo.getInstance(htmlElement));
		doReturn(image).when(elInfo).getScreenshot();

		elInfo.updateInfo(htmlElement);
		
		// in dom mode, no picture generated
		Assert.assertNotNull(elInfo);
		Assert.assertEquals(elInfo.getText(), "sometext");
		Assert.assertEquals(elInfo.getB64Image(), "");

	}
	
	/**
	 * create element information simulating application testing. In this mode, some information are not available (those specific to web browsing): tagName and attributes
	 */
	@Test(groups={"ut"})
	public void testUpdateInfoForAppTesting() {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APP);
		
		ElementInfo elInfo = spy(ElementInfo.getInstance(htmlElement));
		doReturn(image).when(elInfo).getScreenshot();

		elInfo.updateInfo(htmlElement);
		
		// check element info is created with basic information
		Assert.assertNotNull(elInfo);
		Assert.assertEquals(elInfo.getText(), "sometext");
		Assert.assertEquals(elInfo.getCoordX(), (Integer)10);
		Assert.assertEquals(elInfo.getCoordY(),  (Integer)20);
		Assert.assertEquals(elInfo.getHeight(),  (Integer)30);
		Assert.assertEquals(elInfo.getWidth(),  (Integer)40);
		Assert.assertEquals(elInfo.getTotalSearch(), 1);
		Assert.assertEquals(elInfo.getTagName(), "");			// tagname not initialized for apps
		Assert.assertEquals(elInfo.getAttributes().size(), 0);	// attributes not initalized for apps
		Assert.assertEquals(elInfo.getB64Image(), "ABCD");
	
	}
	
	@Test(groups={"ut"}, expectedExceptions=CustomSeleniumTestsException.class)
	public void testUpdateInfoWithoutRealElement() {
		ElementInfo elInfo = ElementInfo.getInstance(htmlElement);
		when(htmlElement.getRealElement()).thenReturn(null);
		
		elInfo.updateInfo(htmlElement);
	}
	
	/**
	 * Test case where the ElementInfo already exists on disk
	 * We should get it and update information in there
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testUpdateExistingInfo() throws IOException {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		
		// write data to file we will then read
		File elementInfoPath = ElementInfo.buildElementInfoPath(htmlElement);
		FileUtils.write(elementInfoPath, JSON_INFO_FOO, StandardCharsets.UTF_8);
		
		ElementInfo elInfo = spy(ElementInfo.getInstance(htmlElement));
		
		// we should get the existing information
		Assert.assertEquals(elInfo.getTagName(), "h1");
		
		doReturn(image).when(elInfo).getScreenshot();
		
		// change information of the underlying element
		when(element.getTagName()).thenReturn("h2");

		elInfo.updateInfo(htmlElement);
		
		Assert.assertEquals(elInfo.getTagName(), "h2");
	}
	
	/**
	 * Empty label blocks process
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testCreateElementInfoWithEmptyLabel() {
		when(htmlElement.getLabel()).thenReturn("");
		ElementInfo.getInstance(htmlElement);
	}
	
	/**
	 * When the element locator has changed compared to what is stored on disk, delete the file and create an new ElementInfo
	 * @throws IOException 
	 */
	@Test(groups={"ut"})
	public void testCreateElementInfoWithChangedLocator() throws IOException {
		
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		
		// write data to file we will then read
		File elementInfoPath = ElementInfo.buildElementInfoPath(htmlElement);
		FileUtils.write(elementInfoPath, JSON_INFO_FOO, StandardCharsets.UTF_8);
		
		// change HtmlElement locator
		when(htmlElement.getBy()).thenReturn(By.name("bar"));
		
		ElementInfo elInfo = ElementInfo.getInstance(htmlElement);
		
		// we should get a new element info not fully initialized
		Assert.assertNull(elInfo.getTagName());
		Assert.assertEquals(elInfo.getLocator(), "By.name: bar");
		
	}
	
	/**
	 * Test case where the ElementInfo already exists on disk
	 * We should get it 
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testCreateElementInfoWithExistingInfo() throws IOException {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		
		// write data to file we will then read
		File elementInfoPath = ElementInfo.buildElementInfoPath(htmlElement);
		FileUtils.write(elementInfoPath, JSON_INFO_FOO, StandardCharsets.UTF_8);
		
		ElementInfo elInfo = spy(ElementInfo.getInstance(htmlElement));
		
		// we should get the existing information
		Assert.assertEquals(elInfo.getTagName(), "h1");
	}
	
	/**
	 * If label contains some characters not supported by filesystem, replace them
	 */
	@Test(groups={"ut"})
	public void testCreateElementInfoWithLabelHavingOddCharacters() {
		when(htmlElement.getLabel()).thenReturn("some%'/\\label");
		ElementInfo elInfo = ElementInfo.getInstance(htmlElement);
		
		// check element info is created with basic information
		Assert.assertNotNull(elInfo);
		Assert.assertEquals(elInfo.getId(), "foo.bar.Page/some_label");
	}
	
	/**
	 * When the strictly same element is found, increment stability information
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testUpdateStabilityInfoWithSameElement() throws IOException {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		
		// write data to file we will then read
		File elementInfoPath = ElementInfo.buildElementInfoPath(htmlElement);
		FileUtils.write(elementInfoPath, JSON_INFO_FOO, StandardCharsets.UTF_8);
		
		ElementInfo elInfo = spy(ElementInfo.getInstance(htmlElement));
		Assert.assertEquals(elInfo.getTotalSearch(), 1);
		Assert.assertEquals(elInfo.getTextStability(), 0);
		Assert.assertEquals(elInfo.getTagStability(), 0);
		Assert.assertEquals(elInfo.getRectangleStability(), 0);
		Assert.assertEquals(elInfo.getAttributesStability().get("id"), (Integer)0);
		
		doReturn(image).when(elInfo).getScreenshot();

		elInfo.updateInfo(htmlElement);
		
		// check stability indicators have been updated
		Assert.assertEquals(elInfo.getTotalSearch(), 2);
		Assert.assertEquals(elInfo.getTextStability(), 1);
		Assert.assertEquals(elInfo.getTagStability(), 1);
		Assert.assertEquals(elInfo.getRectangleStability(), 1);
		Assert.assertEquals(elInfo.getAttributesStability().get("id"), (Integer)1);
	}
	
	/**
	 * When an element changed, its stability indicator is reset to 0
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testUpdateStabilityInfoWithDifferentElement() throws IOException {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		
		// write data to file we will then read
		File elementInfoPath = ElementInfo.buildElementInfoPath(htmlElement);
		FileUtils.write(elementInfoPath, JSON_INFO_FOO, StandardCharsets.UTF_8);
		
		ElementInfo elInfo = spy(ElementInfo.getInstance(htmlElement));
		Assert.assertEquals(elInfo.getTotalSearch(), 1);
		
		doReturn(image).when(elInfo).getScreenshot();
		
		// change text value
		when(element.getText()).thenReturn("newText");

		elInfo.updateInfo(htmlElement);
		
		// check stability indicators have been updated. Text indicator is reset to 0 as value has changed
		Assert.assertEquals(elInfo.getTotalSearch(), 2);
		Assert.assertEquals(elInfo.getTextStability(), 0);
		Assert.assertEquals(elInfo.getTagStability(), 1);
		Assert.assertEquals(elInfo.getRectangleStability(), 1);
		Assert.assertEquals(elInfo.getAttributesStability().get("id"), (Integer)1);
	}
	
	@Test(groups={"ut"})
	public void testUpdateStabilityInfoWithDifferentAttributes() throws IOException {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		
		// write data to file we will then read
		File elementInfoPath = ElementInfo.buildElementInfoPath(htmlElement);
		FileUtils.write(elementInfoPath, JSON_INFO_FOO, StandardCharsets.UTF_8);
		
		ElementInfo elInfo = spy(ElementInfo.getInstance(htmlElement));
		Assert.assertEquals(elInfo.getTotalSearch(), 1);
		
		doReturn(image).when(elInfo).getScreenshot();
		
		// change attribute values (one added and one removed)
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("data", "someData");
		attributes.put("id", "foo");
		when(driver.executeScript(ElementInfo.JAVASCRIPT_GET_ATTRIBUTES, element)).thenReturn(attributes);

		elInfo.updateInfo(htmlElement);
		
		// check stability indicators have been updated. Text indicator is reset to 0 as value has changed
		Assert.assertEquals(elInfo.getTotalSearch(), 2);
		Assert.assertEquals(elInfo.getTextStability(), 1);
		Assert.assertEquals(elInfo.getTagStability(), 1);
		Assert.assertEquals(elInfo.getRectangleStability(), 1);
		Assert.assertEquals(elInfo.getAttributesStability().get("id"), (Integer)1);
		Assert.assertEquals(elInfo.getAttributesStability().get("class"), (Integer)0); // reset as not found anymore
		Assert.assertEquals(elInfo.getAttributesStability().get("data"), (Integer)0); // created
	}
	
	@Test(groups={"ut"})
	public void testExportToJson() throws IOException {
		
		// get an elementInfo from file, as it is complete (no mocks)
		File einfoFile = File.createTempFile("elementInfo", ".json");
		FileUtils.write(einfoFile, JSON_INFO, StandardCharsets.UTF_8);
		einfoFile.deleteOnExit();
		ElementInfo elementInfo = ElementInfo.readFromJsonFile(einfoFile);
		
		File out = elementInfo.exportToJsonFile(false, htmlElement);
		out.deleteOnExit();
		Assert.assertTrue(out.exists());
		String content = FileUtils.readFileToString(out, StandardCharsets.UTF_8);
		Assert.assertTrue(content.contains("\"id\":\"com.seleniumtests.it.driver.support.pages.DriverTestPage/button_after_scroll\""));
		Assert.assertTrue(content.contains("mylabel.json")); // path updated
	}
	
	/**
	 * Check we are able to read an element info file and extract information
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testReadFromJson() throws IOException {
		File einfoFile = File.createTempFile("elementInfo", ".json");
		FileUtils.write(einfoFile, JSON_INFO, StandardCharsets.UTF_8);
		einfoFile.deleteOnExit();
		ElementInfo elementInfo = ElementInfo.readFromJsonFile(einfoFile);
		Assert.assertEquals(elementInfo.getName(), "button after scroll");
		Assert.assertEquals(elementInfo.getId(), "com.seleniumtests.it.driver.support.pages.DriverTestPage/button_after_scroll");
		Assert.assertEquals(elementInfo.getText(), "set");
		Assert.assertEquals(elementInfo.getTagName(), "button");
		Assert.assertEquals(elementInfo.getLocator(), "By.id: buttonScroll");
		Assert.assertEquals(elementInfo.getCoordX(), (Integer)8);
		Assert.assertNotNull(elementInfo.getB64Image());
		Assert.assertEquals(elementInfo.getAttributes().size(), 2);
		Assert.assertEquals(elementInfo.getAttributes().get("id"), "buttonScroll");
		Assert.assertEquals(elementInfo.getTotalSearch(), 8);
		Assert.assertEquals(elementInfo.getTextStability(), 4);
		Assert.assertEquals((int)elementInfo.getAttributesStability().get("id"), 5);
	}

	/**
	 * Check that if file is empty, null is returned
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testReadFromEmptyJson() throws IOException {
		File einfoFile = File.createTempFile("elementInfo", ".json");
		einfoFile.deleteOnExit();
		Assert.assertNull(ElementInfo.readFromJsonFile(einfoFile));
	}
	
	/**
	 * Check we return null if the file information cannot be found
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testReadFromNonExistentJson() throws IOException {
		ElementInfo elementInfo = ElementInfo.readFromJsonFile(new File("foo.json"));
		Assert.assertNull(elementInfo);
	}
}
