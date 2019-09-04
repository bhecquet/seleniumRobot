package com.seleniumtests.it.driver;

import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverScrollingTestPage;

public class TestAutoScrolling extends GenericMultiBrowserTest {
	
	public TestAutoScrolling() throws Exception {
		super(null, "DriverScrollingTestPage");  
	}
	
	public TestAutoScrolling(BrowserType browserType) throws Exception {
		super(browserType, "DriverScrollingTestPage");  
	}
	
	@AfterMethod(groups={"it", "ut"})
	public void reset() {
		if (driver == null) {
			return;
		}
		DriverScrollingTestPage.resetButton.click();
		Assert.assertEquals("", DriverScrollingTestPage.textElement.getValue());
	}
	
	/**
	 * Scroll into div
	 */
	@Test(groups={"it", "ut"})
	public void testScrollToMiddleDiv1() {
		DriverScrollingTestPage.greenBox.click();
		Assert.assertEquals(DriverScrollingTestPage.textElement.getValue(), "greenbox");
	}

	/**
	 * Scroll into div
	 */
	@Test(groups={"it", "ut"})
	public void testScrollToMiddleDiv2() {
		DriverScrollingTestPage.blueBox.click();
		Assert.assertEquals(DriverScrollingTestPage.textElement.getValue(), "bluebox");
	}

	/**
	 * Scroll into top of div. Check that header do not hide the button
	 */
	@Test(groups={"it", "ut"})
	public void testScrollToDivTop() {
		DriverScrollingTestPage.buttonScrollTop.click();
		Assert.assertEquals(DriverScrollingTestPage.textElement.getValue(), "scroll top");
	}
	
	/**
	 * Scroll into bottom of div. Check that footer do not hide the button
	 */
	@Test(groups={"it", "ut"})
	public void testScrollToDivBottom() {
		DriverScrollingTestPage.buttonScrollBottom.click();
		Assert.assertEquals(DriverScrollingTestPage.textElement.getValue(), "scroll bottom");
	}

	/**
	 * Scroll into bottom of div. Check that composite action is done
	 */
	@Test(groups={"it", "ut"})
	public void testScrollToDivBottomWithCompositeAction() {
		new Actions(driver).click(DriverScrollingTestPage.buttonScrollBottom).perform();
		Assert.assertEquals(DriverScrollingTestPage.textElement.getValue(), "scroll bottom");
	}
	
	/**
	 * Scroll to page bottom and check that element is not hidden by footer
	 */
	@Test(groups={"it", "ut"})
	public void testScrollToBottom() {
			DriverScrollingTestPage.setButton.click();
			Assert.assertEquals(DriverScrollingTestPage.textElement.getValue(), "bottom button");
	}
	
	/**
	 * Scroll to page bottom without headers
	 */
	@Test(groups={"it", "ut"})
	public void testScrollToBottomNoHeader() {
		
		DriverScrollingTestPage.hideButton.click();
		DriverScrollingTestPage.setButton.click();
		Assert.assertEquals(DriverScrollingTestPage.textElement.getValue(), "bottom button");
	}

	
	/* force auto scrolling to see if there are problems of hidden elements */
	/**
	 * Scroll into div
	 */
	@Test(groups={"it", "ut"})
	public void testAutoScrollToMiddleDiv1() {
		try {
			DriverScrollingTestPage.greenBox.setScrollToElementBeforeAction(true);
			DriverScrollingTestPage.greenBox.click();
			Assert.assertEquals(DriverScrollingTestPage.textElement.getValue(), "greenbox");
		} finally {
			DriverScrollingTestPage.greenBox.setScrollToElementBeforeAction(false);
		}
	}

	/**
	 * Scroll into div
	 */
	@Test(groups={"it", "ut"})
	public void testAutoScrollToMiddleDiv2() {
		try {
			DriverScrollingTestPage.blueBox.setScrollToElementBeforeAction(true);
			DriverScrollingTestPage.blueBox.click();
			Assert.assertEquals(DriverScrollingTestPage.textElement.getValue(), "bluebox");
		} finally {
			DriverScrollingTestPage.blueBox.setScrollToElementBeforeAction(false);
		}
	}

	/**
	 * Scroll into top of div. Check that header do not hide the button
	 */
	@Test(groups={"it", "ut"})
	public void testAutoScrollToDivTop() {
		try {
			DriverScrollingTestPage.buttonScrollTop.setScrollToElementBeforeAction(true);
			DriverScrollingTestPage.buttonScrollTop.click();
			Assert.assertEquals(DriverScrollingTestPage.textElement.getValue(), "scroll top");
		} finally {
			DriverScrollingTestPage.buttonScrollTop.setScrollToElementBeforeAction(false);
		}
	}

	/**
	 * Scroll into bottom of div. Check that footer do not hide the button
	 */
	@Test(groups={"it", "ut"})
	public void testAutoScrollToDivBottom() {
		try {
			DriverScrollingTestPage.buttonScrollBottom.setScrollToElementBeforeAction(true);
			DriverScrollingTestPage.buttonScrollBottom.click();
			Assert.assertEquals(DriverScrollingTestPage.textElement.getValue(), "scroll bottom");
		} finally {
			DriverScrollingTestPage.buttonScrollBottom.setScrollToElementBeforeAction(false);
		}
	}

	/**
	 * Scroll to page bottom and check that element is not hidden by footer
	 */
	@Test(groups={"it", "ut"})
	public void testAutoScrollToBottom() {
		try {
			DriverScrollingTestPage.setButton.setScrollToElementBeforeAction(true);
			DriverScrollingTestPage.setButton.click();
			Assert.assertEquals(DriverScrollingTestPage.textElement.getValue(), "bottom button");
		} finally {

			DriverScrollingTestPage.setButton.setScrollToElementBeforeAction(false);
		}
	}
}
