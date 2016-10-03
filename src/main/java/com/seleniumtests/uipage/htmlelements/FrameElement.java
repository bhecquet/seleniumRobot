package com.seleniumtests.uipage.htmlelements;

import org.openqa.selenium.By;

public class FrameElement extends HtmlElement {

	public FrameElement(final String label, final By by) {
        super(label, by);
    }

    public FrameElement(final String label, final By by, final int index) {
    	super(label, by, index);
    }
    
    public FrameElement(final String label, final By by, final FrameElement frame) {
    	super(label, by, frame);
    }
    
    public FrameElement(final String label, final By by, final FrameElement frame, final int index) {
    	super(label, by, frame, index);
    }
}
