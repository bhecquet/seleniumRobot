package com.seleniumtests.driver.screenshots;

import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;

/**
 * Class focusing on what is the target of the snapshot: screen, web page, element inside a page
 * @author s047432
 *
 */
public class SnapshotTarget {
	
	public static final SnapshotTarget SCREEN = new SnapshotTarget(Target.SCREEN);
	public static final SnapshotTarget MAIN_SCREEN = new SnapshotTarget(Target.MAIN_SCREEN);
	public static final SnapshotTarget PAGE = new SnapshotTarget(Target.PAGE);
	public static final SnapshotTarget VIEWPORT = new SnapshotTarget(Target.VIEWPORT);
	
	public enum Target {
		SCREEN,   		// all the physical screens
		MAIN_SCREEN, 	// only the main physical screen
		PAGE, 			// the web page
		ELEMENT,
		VIEWPORT
	}
	
	private Target target;
	private WebElement element;
	private Rectangle snapshotRectangle = new Rectangle(0, 0, 0, 0);

	public SnapshotTarget(Target target) {
		this.target = target;
	}
	
	public SnapshotTarget(WebElement element) {
		this.target = Target.ELEMENT;
		this.element = element;
	}
	
	public boolean isScreenTarget() {
		return target == Target.SCREEN;
	}
	
	public boolean isMainScreenTarget() {
		return target == Target.MAIN_SCREEN;
	}
	
	public boolean isViewportTarget() {
		return target == Target.VIEWPORT;
	}
	
	public boolean isPageTarget() {
		return target == Target.PAGE;
	}
	
	public boolean isElementTarget() {
		return target == Target.ELEMENT;
	}

	public Target getTarget() {
		return target;
	}

	public WebElement getElement() {
		return element;
	}

	public Rectangle getSnapshotRectangle() {
		return snapshotRectangle;
	}

	public void setSnapshotRectangle(Rectangle snapshotRectangle) {
		this.snapshotRectangle = snapshotRectangle;
	}
}
	
