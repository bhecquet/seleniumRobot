package com.seleniumtests.driver.screenshots;

import java.util.ArrayList;
import java.util.List;

import com.seleniumtests.connectors.selenium.fielddetector.Field;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Type of check we should do on snapshot
 * method <code>check(SnapshotTarget target)</code> must be called so that element excluded are transformed to 
 * valid exclusion rectangles
 *
 */
public class SnapshotCheckType {

	// TODO: check without colors, check only part of the picture (exclusion zones defined directly in test)
	protected static final Logger logger = SeleniumRobotLogger.getLogger(SnapshotCheckType.class);
	
	private Control control;
	private List<Rectangle> excludeElementsRect = new ArrayList<>();
	private List<WebElement> excludeElements = new ArrayList<>();
	private double errorThreshold = 0.0;
	
	public enum Control {
		NONE,
		FULL,			// pixel comparison
		LAYOUT,			// do not compare colors (not implemented), just shapes
		NONE_REFERENCE 	// no comparison done, it's a reference picture when step fails so that we can show to user what should have been the application if step succeed
	}
	
	// snapshot will be compared to baseline if requested by option seleniumRobotServerCompareSnapshots
	public static final SnapshotCheckType TRUE = new SnapshotCheckType(Control.FULL);
	
	public static final SnapshotCheckType FULL = new SnapshotCheckType(Control.FULL);
	
	public static final SnapshotCheckType LAYOUT = new SnapshotCheckType(Control.LAYOUT); // not really used
	
	// snapshot will not be compared
	public static final SnapshotCheckType FALSE = new SnapshotCheckType(Control.NONE);
	public static final SnapshotCheckType NONE = new SnapshotCheckType(Control.NONE);
	
	// snapshot will be used for reference, when test fails, no comparison should be done on these pictures
	public static final SnapshotCheckType REFERENCE_ONLY = new SnapshotCheckType(Control.NONE_REFERENCE);
			
	private SnapshotCheckType(Control controlType) {
		this.control = controlType;
	}
	
	public String getName() {
		return control.name();
	}
	
	/**
	 * Says whether snasphots will be sent to seleniumRobot server or not for comparing with previous test sessions
	 * @return
	 */
	public boolean recordSnapshotOnServerForComparison() {
		return control != Control.NONE && control != Control.NONE_REFERENCE;
	}
	
	/**
	 * return true if snapshot is a reference image that show what is the application status when step succeed
	 * @return
	 */
	public boolean recordSnapshotOnServerForReference() {
		return control == Control.NONE_REFERENCE;
	}
	
	private String rectangleToString(Rectangle rect) {
		return String.format("Rectangle(%d, %d, %d, %d)", rect.x, rect.y, rect.width, rect.height);
	}

	
	/**
	 * Check if the SnapshotCheckType is valid with the provided target (Screen, Page, Element)
	 * @param target
	 */
	public void check(SnapshotTarget target, double aspectRatio) {
		
		// when target is a screen, do not take into account excluded elements
		if (target.isPageTarget()) {
			for (WebElement el: excludeElements) {
				try {
					excludeElementsRect.add(ScreenshotUtil.getElementRectangleWithAR(el, aspectRatio));
				} catch (WebDriverException e) {
					logger.warn(String.format("Element %s not added to exclusion as it cannot be found", el));
				}
			}
		} else if (target.isElementTarget() || target.isViewportTarget()) {
			WebElement targetElement = target.getElement();
			Rectangle targetRectangle;
			try {
				targetRectangle = target.getSnapshotRectangle(); // aspect ratio is already applied here
			} catch (WebDriverException e) {
				throw new ScenarioException(String.format("Cannot check element %s snapshot as it is not available", targetElement));
			}
			
			// check all elements to exclude are included in targetElement
			for (WebElement el: excludeElements) {
				try {
					Rectangle elementRectangle = ScreenshotUtil.getElementRectangleWithAR(el, aspectRatio);

					if (elementRectangle.x < targetRectangle.x
							|| elementRectangle.y < targetRectangle.y
							|| elementRectangle.x + elementRectangle.width > targetRectangle.x + targetRectangle.width
							|| elementRectangle.y + elementRectangle.height > targetRectangle.y + targetRectangle.height) {
						logger.warn(String.format("Element %s is not inside %s and won't be excluded", rectangleToString(elementRectangle), rectangleToString(targetRectangle)));
					
					} else {
						excludeElementsRect.add(elementRectangle);
					}
					
				} catch (WebDriverException e) {
					logger.warn(String.format("Element %s not added to exclusion as it cannot be found", el));
				}
			}
		}
	}
	
	/**
	 * Percentage of pixels that can be different when comparing this snapshot to its reference.
	 * ex: setting 2.3 means that with 2% of different pixels, 
	 * @param errorThreshold
	 * @return
	 */
	public SnapshotCheckType withThreshold(double errorThreshold) {
		SnapshotCheckType newCheck = new SnapshotCheckType(control);
		newCheck.errorThreshold = errorThreshold;
		return newCheck;
	}
	
	/**
	 * Exclude the elements from comparison
	 * @param element	list of WebElements
	 * @return
	 */
	public SnapshotCheckType exclude(List<WebElement> elements) {
		SnapshotCheckType newCheck = this;
		for (WebElement element: elements) {
			newCheck = newCheck.exclude(element);
		}
		return newCheck;
	}
	
	/**
	 * Exclude the element from comparison
	 * @param element
	 * @return
	 */
	public SnapshotCheckType exclude(WebElement element) {
		SnapshotCheckType newCheck = new SnapshotCheckType(control);
		newCheck.excludeElements = new ArrayList<>(excludeElements);
		newCheck.excludeElements.add(element);
		
		return newCheck;
	}

	public List<Rectangle> getExcludeElementsRect() {
		return excludeElementsRect;
	}

	public double getErrorThreshold() {
		return errorThreshold;
	}

	public Control getControl() {
		return control;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj == null) {
			return false;
		}
		
		if (this == obj) {
			return true;
		}
		
		if (obj instanceof SnapshotCheckType) {
			SnapshotCheckType checkType = (SnapshotCheckType) obj;
			return this.getControl() == checkType.getControl();
		} else {
			return false;
		}
	}
		
}
