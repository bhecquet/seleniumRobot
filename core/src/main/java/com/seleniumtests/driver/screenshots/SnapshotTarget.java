package com.seleniumtests.driver.screenshots;

import org.openqa.selenium.WebElement;

/**
 * Class focusing on what is the target of the snapshot: screen, web page, element inside a page
 * @author s047432
 *
 */
public class SnapshotTarget {
	
	public static final SnapshotTarget SCREEN = new SnapshotTarget(Target.SCREEN);
	public static final SnapshotTarget PAGE = new SnapshotTarget(Target.PAGE);
	
	public enum Target {
		SCREEN, 
		PAGE, 
		ELEMENT
	}
	
	private Target target;
	private WebElement element;

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
}
	
