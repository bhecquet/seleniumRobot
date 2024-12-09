package com.seleniumtests.uipage.htmlelements;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriverException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.util.StringUtility;
import com.seleniumtests.util.imaging.ImageProcessor;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class ElementInfo {
	
	public enum Mode {
		FALSE,
		DOM,
		FULL,
	}
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(ElementInfo.class);
	private static final Path ELEMENT_INFO_LOCATION = Paths.get(SeleniumTestsContextManager.getCachePath());
	private static final Path ELEMENT_INFO_REFERENCE_LOCATION = Paths.get(SeleniumTestsContextManager.getCachePath() + "_reference");
	private static final int EXPIRE_INFO_DELAY_DAYS = 180;
	public static final String JAVASCRIPT_GET_ATTRIBUTES = "var items = {}; for (index = 0; index < arguments[0].attributes.length; ++index) { items[arguments[0].attributes[index].name] = arguments[0].attributes[index].value }; return items;";

	private String path;
	private LocalDateTime lastUpdate;
	private String name;
	private String id;
	private String locator;
	private String tagName;
	private String text;
	private Integer width = 0;
	private Integer height = 0;
	private Integer coordX = 0;
	private Integer coordY = 0;
	private String b64Image;
	private Map<String, Object> attributes = new HashMap<>();
	
	// TODO: confidence indicator
	private float tagConfidence;
	private float textConfidence;
	private float rectangleConfidence;
	private float b64ImageConfidence;
	private Map<String, Float> attributesConfidence;
	
	// stability indicator
	private int totalSearch = 0;
	private int tagStability = 0;
	private int textStability = 0;
	private int rectangleStability = 0;
	private int b64ImageStability = 0;
	private Map<String, Integer> attributesStability = new HashMap<>();
	
	/**
	 * Returns an element info, either by searching in cache or creating a new one if none has been found
	 * @param htmlElement
	 * @return
	 */
	public static ElementInfo getInstance(HtmlElement htmlElement) {
		
		if (SeleniumTestsContextManager.getThreadContext().getAdvancedElementSearch() == Mode.FALSE) {
			return null;
		}
		
		ElementInfo elementInfo = searchElementInfo(htmlElement);
		
		if (elementInfo == null) {
			return new ElementInfo(htmlElement);
			
		// reset element info if the locator has changed
		} else if (!elementInfo.locator.equals(htmlElement.getBy().toString())) {
			elementInfo.delete();
			return new ElementInfo(htmlElement);
		} else {
			return elementInfo;
		}
	}
	
	public ElementInfo(HtmlElement htmlElement) {
		name = htmlElement.getLabel();
		id = buildId(htmlElement);
		locator = htmlElement.getBy().toString();
	}
	
	public BufferedImage getScreenshot() {
		return new ScreenshotUtil().capturePage(0, 0);
	}
	
	/**
	 * Update information by calling the real element.
	 * @param htmlElement
	 */
	public void updateInfo(HtmlElement htmlElement) {
		
		if (htmlElement.getRealElement() == null) {
			throw new CustomSeleniumTestsException(String.format("Updating element information [%s] is not possible if real element has not yet been searched", name));
		}
		
    	String newText = htmlElement.getRealElement().getText();
    	
    	// depending on drivers, rect may raise an error
    	Rectangle newRectangle;
    	try {
    		newRectangle = htmlElement.getRealElement().getRect();
		} catch (WebDriverException e) {
			Point location = htmlElement.getRealElement().getLocation();
			Dimension size = htmlElement.getRealElement().getSize();
			newRectangle = new Rectangle(location, size);
		}
    	
    	String newB64Image = "";
    	String newTagName = "";
    	Map<String, Object> newAttributes = new HashMap<>();

    	// only capture picture in FULL mode
    	if (SeleniumTestsContextManager.getThreadContext().getAdvancedElementSearch() == Mode.FULL) {
			
	    	try {
		    	BufferedImage  fullImg = getScreenshot();
		    	
		    	// Get the location of htmlElement on the page
		    	Point point = newRectangle.getPoint();
		
		    	// Get width and height of the element
		    	int eleWidth = newRectangle.getWidth();
		    	int eleHeight = newRectangle.getHeight();
		    	Point scrollPosition = ((CustomEventFiringWebDriver)htmlElement.getDriver()).getScrollPosition();
		
		    	// Crop the entire page screenshot to get only element screenshot. Keep 20 px around the picture
		    	BufferedImage eleScreenshot = ImageProcessor.cropImage(fullImg, Math.max(0, point.getX() - scrollPosition.getX() - 20), 
		    									Math.max(0, point.getY() - scrollPosition.getY() - 20), 
		    									Math.min(eleWidth + 40, fullImg.getWidth()), 
		    									Math.min(eleHeight + 40, fullImg.getHeight()));
		    	
		    	// for debug purpose
		    	/*File tmp = File.createTempFile("screenshot", ".png");
		    	tmp.deleteOnExit();
		    	ImageIO.write(eleScreenshot, "png", tmp);*/
		    	newB64Image = ImageProcessor.toBase64(eleScreenshot);
	    	
			} catch (Exception e) {
				logger.error("Error taking element screenshot", e);
			}
    	}
    	
    	if (SeleniumTestsContextManager.isWebTest()) {

    		newTagName = htmlElement.getRealElement().getTagName();
        	newAttributes = (Map<String, Object>) ((JavascriptExecutor)htmlElement.getDriver()).executeScript(JAVASCRIPT_GET_ATTRIBUTES, htmlElement.getRealElement());
    	}
    	
    	// record stability information (is the information stable over time or not)
    	totalSearch += 1;
    	textStability = newText.equals(text) ? textStability+1: 0;
    	tagStability = newTagName.equals(tagName) ? tagStability+1: 0;
    	rectangleStability = newRectangle.equals(new Rectangle(coordX, coordY, height, width)) ? rectangleStability+1: 0;

    	for (Entry<String, Object> entryAttr: newAttributes.entrySet()) {
    		// attribute was unknown
    		if (!attributes.containsKey(entryAttr.getKey()) || !attributesStability.containsKey(entryAttr.getKey())) {
    			attributesStability.put(entryAttr.getKey(), 0);
    		}
    		// attribute is known but changed
    		else if (attributes.containsKey(entryAttr.getKey())) {
    			if (!attributes.get(entryAttr.getKey()).equals(newAttributes.get(entryAttr.getKey()))) {
        			attributesStability.put(entryAttr.getKey(), 0);
    			} else {
    				attributesStability.put(entryAttr.getKey(), attributesStability.get(entryAttr.getKey()) + 1);
    			}
    		}
    	}
    	
    	// reset indicators for attributes that are not found anymore
    	for (Entry<String, Object> entryAttr: attributes.entrySet()) {
    		if (!newAttributes.containsKey(entryAttr.getKey())) {
    			attributesStability.put(entryAttr.getKey(), 0);
    		}
    	}
    	
    	// TODO: image
    	b64ImageStability = 0;
    	
    	text = newText;
    	coordX = newRectangle.x;
    	coordY = newRectangle.y;
    	width = newRectangle.width;
    	height = newRectangle.height;
    	b64Image = newB64Image;
    	tagName = newTagName;
    	attributes = newAttributes;
    	lastUpdate = LocalDateTime.now();
	}
	
	public Rectangle getRectangle() {
		return new Rectangle(coordX, coordY, height, width);
	}
	
	/**
	 * Build element info id from name and origin
	 * @param htmlElement
	 * @return
	 * @throws ScenarioException if name is empty
	 */
	protected static String buildId(HtmlElement htmlElement) {
		String name = htmlElement.getLabel();
		name = StringUtility.replaceOddCharsFromFileName(name);
		
		if (name == null || name.isEmpty()) {
			throw new ScenarioException("not storing element information, no label provided");
		}
		
		return String.format("%s/%s", htmlElement.getOrigin(), name);
	}
	
	/**
	 * Export information to json for storage
	 * @return
	 * @throws IOException 
	 */
	public synchronized File exportToJsonFile(HtmlElement htmlElement) throws IOException {
		return exportToJsonFile(false, htmlElement);
	}
	public synchronized File exportToJsonFile(boolean reference, HtmlElement htmlElement) throws IOException {
		
		Gson gson = new Gson();
	
		File outputFile;
		if (htmlElement == null) {
			if (reference) {
				outputFile = ELEMENT_INFO_REFERENCE_LOCATION.resolve(Paths.get(id + ".json")).toFile();
			} else {
				outputFile = getElementInfoFile(id);
			}
		} else {
			outputFile = buildElementInfoPath(htmlElement);
		}
		path = outputFile.getAbsolutePath();
		FileUtils.writeStringToFile(outputFile, gson.toJson(this), StandardCharsets.UTF_8);
		return outputFile;
	}
	
	public static ElementInfo readFromJsonFile(File elementInfoFile) {
		
		Gson gson = new Gson();
		try {
			ElementInfo info = gson.fromJson(FileUtils.readFileToString(elementInfoFile, StandardCharsets.UTF_8), ElementInfo.class);
			
			if (info == null) {
				return null;
			}
			info.path = elementInfoFile.getAbsolutePath();
			return info;
		} catch (JsonSyntaxException | IOException e) {
			return null;
		}
		
	}
	
	/**
	 * Read an ElementInfo from json string
	 * @param elementInfoJson
	 * @return
	 */
	public static ElementInfo readFromJson(String elementInfoJson) {
		
		Gson gson = new Gson();
		try {
			ElementInfo info = gson.fromJson(elementInfoJson, ElementInfo.class);
			
			if (info == null) {
				return null;
			}
			return info;
		} catch (JsonSyntaxException e) {
			return null;
		}
		
	}
	
	private void delete() {
		if (new File(path).exists()) {

			try {
				Files.delete(Paths.get(path));
			} catch (IOException e) {
				logger.error(String.format("Failed to delete elementInfo file for %s: %s", name, e.getMessage()));
			}
		}
	}
	
	private static File getElementInfoFile(String elementId) {
		return ELEMENT_INFO_LOCATION.resolve(Paths.get(elementId + ".json")).toFile();
	}
	
	public static File buildElementInfoPath(HtmlElement htmlElement) {
		return getElementInfoFile(buildId(htmlElement));
	}
	
	/**
	 * Returns an ElementInfo object if it's found on file system 
	 * @param htmlElement
	 * @return
	 */
	public static ElementInfo searchElementInfo(HtmlElement htmlElement) {
		File elementInfoPath = buildElementInfoPath(htmlElement);
		if (elementInfoPath.isFile()) {
			return readFromJsonFile(elementInfoPath);
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the list of element infos stored on disk
	 * @param reference
	 * @return
	 */
	public static Map<String, ElementInfo> getAllStoredElementInfos(boolean reference) {
		Map<String, ElementInfo> elementInfos = new HashMap<>();
		try (Stream<Path> files = Files.walk(reference ? ELEMENT_INFO_REFERENCE_LOCATION: ELEMENT_INFO_LOCATION)) {
			
			for (Path jsonFile: files
			        .filter(Files::isRegularFile)
			        .collect(Collectors.toList())) {
				
				ElementInfo elementInfo = ElementInfo.readFromJsonFile(jsonFile.toFile());
				if (elementInfo != null) {
					elementInfos.put(elementInfo.getId(), elementInfo);
				}
			}
		} catch (IOException e) {
			logger.error("Cannot get list of element infos: " + e.getMessage());
		}
		
		return elementInfos;
	}
	
	/**
	 * Remove old element information objects from file system
	 * This will only remove objects that have not been used in the last N days
	 */
	public static void purgeElementInfo() {
		purgeElementInfo(EXPIRE_INFO_DELAY_DAYS);
	}
	private static void purgeElementInfo(int delay) {

		try (Stream<Path> files = Files.walk(ELEMENT_INFO_LOCATION)) {
			
	        files.filter(Files::isRegularFile)
		        .filter(p -> p.toFile().lastModified() < LocalDateTime.now().minusDays(delay).toEpochSecond(ZoneOffset.UTC) * 1000)
		        .forEach(t -> {
					try {
						Files.delete(t);
					} catch (IOException e) {
						logger.error("Cannot purge ElementInfo: " + e.getMessage());
					}
				});
			
		} catch (IOException e) {
			logger.error("Cannot purge ElementInfo : " + e.getMessage());
		}

	}
	
	public static void purgeAll() {
		purgeElementInfo(-1);
	}

	public String getPath() {
		return path;
	}

	public LocalDateTime getLastUpdate() {
		return lastUpdate;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public String getLocator() {
		return locator;
	}

	public String getTagName() {
		return tagName;
	}

	public String getText() {
		return text;
	}

	public String getB64Image() {
		return b64Image;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public float getTagConfidence() {
		return tagConfidence;
	}

	public float getTextConfidence() {
		return textConfidence;
	}

	public float getRectangleConfidence() {
		return rectangleConfidence;
	}

	public float getB64ImageConfidence() {
		return b64ImageConfidence;
	}

	public Map<String, Float> getAttributesConfidence() {
		return attributesConfidence;
	}

	public int getTotalSearch() {
		return totalSearch;
	}

	public int getTagStability() {
		return tagStability;
	}

	public int getTextStability() {
		return textStability;
	}

	public int getRectangleStability() {
		return rectangleStability;
	}

	public int getB64ImageStability() {
		return b64ImageStability;
	}

	public Map<String, Integer> getAttributesStability() {
		return attributesStability;
	}
	
	/**
	 * Returns path where element information are stored
	 * @return
	 */
	public static Path getElementInfoLocation() {
		return ELEMENT_INFO_LOCATION;
	}

	public Integer getWidth() {
		return width;
	}

	public Integer getHeight() {
		return height;
	}

	public Integer getCoordX() {
		return coordX;
	}

	public Integer getCoordY() {
		return coordY;
	}
}
