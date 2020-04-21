package com.seleniumtests.ut.driver.screenshots;

import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.mockito.Mock;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.driver.screenshots.SnapshotTarget;

public class TestSnapshotCheckType extends MockitoTest {
	
	@Mock
	private WebElement elementToExclude1;
	
	@Mock
	private WebElement elementToExclude2;
	
	@Mock
	private WebElement element;
	
	@Mock
	private SnapshotTarget snapshotTarget;
	
	
	@Test(groups= {"ut"})
	public void testRecordSnapshotOnServer() {
		Assert.assertTrue(SnapshotCheckType.FULL.recordSnapshotOnServer());
	}
	
	@Test(groups= {"ut"})
	public void testDoNotRecordSnapshotOnServer() {
		Assert.assertFalse(SnapshotCheckType.FALSE.recordSnapshotOnServer());
	}
	
	@Test(groups= {"ut"})
	public void testRecordSnapshotOnServerLayoutOnly() {
		Assert.assertTrue(SnapshotCheckType.LAYOUT.recordSnapshotOnServer());
	}

	@Test(groups= {"ut"})
	public void testExcludeElementFromPage() {
		when(elementToExclude1.getRect()).thenReturn(new Rectangle(1,  2,  3,  4));
		when(snapshotTarget.isPageTarget()).thenReturn(true);
		
		SnapshotCheckType checkType = SnapshotCheckType.FULL.exclude(elementToExclude1);
		checkType.check(snapshotTarget);
		
		Assert.assertEquals(checkType.getExcludeElementsRect().size(), 1);
		Assert.assertEquals(checkType.getExcludeElementsRect().get(0), new Rectangle(1,  2,  3,  4));
	}
	
	@Test(groups= {"ut"})
	public void testExcludeElementsFromPage() {
		when(elementToExclude1.getRect()).thenReturn(new Rectangle(1,  2,  3,  4));
		when(elementToExclude2.getRect()).thenReturn(new Rectangle(5,  6,  7,  8));
		when(snapshotTarget.isPageTarget()).thenReturn(true);
		
		SnapshotCheckType checkType = SnapshotCheckType.FULL.exclude(Arrays.asList(elementToExclude1, elementToExclude2));
		checkType.check(snapshotTarget);
		
		Assert.assertEquals(checkType.getExcludeElementsRect().size(), 2);
		Assert.assertEquals(checkType.getExcludeElementsRect().get(0), new Rectangle(1,  2,  3,  4));
		Assert.assertEquals(checkType.getExcludeElementsRect().get(1), new Rectangle(5,  6,  7,  8));
	}
	
	/**
	 * Error getting element to exclude, it's not kept but we continue
	 */
	@Test(groups= {"ut"})
	public void testExcludeElementFromPageWithError() {
		when(elementToExclude1.getRect()).thenThrow(new WebDriverException("error"));
		when(snapshotTarget.isPageTarget()).thenReturn(true);
		
		SnapshotCheckType checkType = SnapshotCheckType.FULL.exclude(elementToExclude1);
		checkType.check(snapshotTarget);
		
		Assert.assertEquals(checkType.getExcludeElementsRect().size(), 0);
	}
	
	/**
	 * Check element excluded from captured element is kept
	 */
	@Test(groups= {"ut"})
	public void testExcludeElementInsideElement() {
		when(elementToExclude1.getRect()).thenReturn(new Rectangle(1,  2,  3,  4));
		when(element.getRect()).thenReturn(new Rectangle(0,  0,  100,  100));
		when(snapshotTarget.getElement()).thenReturn(element);
		when(snapshotTarget.isPageTarget()).thenReturn(false);
		when(snapshotTarget.isElementTarget()).thenReturn(true);
		
		SnapshotCheckType checkType = SnapshotCheckType.FULL.exclude(elementToExclude1);
		checkType.check(snapshotTarget);
		
		Assert.assertEquals(checkType.getExcludeElementsRect().size(), 1);
		Assert.assertEquals(checkType.getExcludeElementsRect().get(0), new Rectangle(1,  2,  3,  4));
	}

	/**
	 * Check element excluded from captured element but outside of it is not kept
	 * element to exclude 'x' is greeter than enclosing element top right position
	 */
	@Test(groups= {"ut"})
	public void testExcludeElementOutsideElement1() {
		when(elementToExclude1.getRect()).thenReturn(new Rectangle(101,  20,  3,  4));
		when(element.getRect()).thenReturn(new Rectangle(0,  0,  100,  100));
		when(snapshotTarget.getElement()).thenReturn(element);
		when(snapshotTarget.isPageTarget()).thenReturn(false);
		when(snapshotTarget.isElementTarget()).thenReturn(true);
		
		SnapshotCheckType checkType = SnapshotCheckType.FULL.exclude(elementToExclude1);
		checkType.check(snapshotTarget);
		
		Assert.assertEquals(checkType.getExcludeElementsRect().size(), 0);
	}
	/**
	 * Check element excluded from captured element but outside of it is not kept
	 * element to exclude 'x' is lower than enclosing element left position
	 */
	@Test(groups= {"ut"})
	public void testExcludeElementOutsideElement2() {
		when(elementToExclude1.getRect()).thenReturn(new Rectangle(0,  0,  3,  4));
		when(element.getRect()).thenReturn(new Rectangle(1,  0,  100,  100));
		when(snapshotTarget.getElement()).thenReturn(element);
		when(snapshotTarget.isPageTarget()).thenReturn(false);
		when(snapshotTarget.isElementTarget()).thenReturn(true);
		
		SnapshotCheckType checkType = SnapshotCheckType.FULL.exclude(elementToExclude1);
		checkType.check(snapshotTarget);
		
		Assert.assertEquals(checkType.getExcludeElementsRect().size(), 0);
	}
	/**
	 * Check element excluded from captured element but outside of it is not kept
	 * element to exclude 'y' is lower than enclosing top position
	 */
	@Test(groups= {"ut"})
	public void testExcludeElementOutsideElement3() {
		when(elementToExclude1.getRect()).thenReturn(new Rectangle(0,  0,  3,  4));
		when(element.getRect()).thenReturn(new Rectangle(0,  1,  100,  100));
		when(snapshotTarget.getElement()).thenReturn(element);
		when(snapshotTarget.isPageTarget()).thenReturn(false);
		when(snapshotTarget.isElementTarget()).thenReturn(true);
		
		SnapshotCheckType checkType = SnapshotCheckType.FULL.exclude(elementToExclude1);
		checkType.check(snapshotTarget);
		
		Assert.assertEquals(checkType.getExcludeElementsRect().size(), 0);
	}
	/**
	 * Check element excluded from captured element but outside of it is not kept
	 * element to exclude 'y' is higher than enclosing top position
	 */
	@Test(groups= {"ut"})
	public void testExcludeElementOutsideElement4() {
		when(elementToExclude1.getRect()).thenReturn(new Rectangle(0,  101,  3,  4));
		when(element.getRect()).thenReturn(new Rectangle(0,  0,  100,  100));
		when(snapshotTarget.getElement()).thenReturn(element);
		when(snapshotTarget.isPageTarget()).thenReturn(false);
		when(snapshotTarget.isElementTarget()).thenReturn(true);
		
		SnapshotCheckType checkType = SnapshotCheckType.FULL.exclude(elementToExclude1);
		checkType.check(snapshotTarget);
		
		Assert.assertEquals(checkType.getExcludeElementsRect().size(), 0);
	}
	/**
	 * Check element excluded from captured element but outside of it is not kept
	 * element to exclude is wider than enclosing element
	 */
	@Test(groups= {"ut"})
	public void testExcludeElementOutsideElement5() {
		when(elementToExclude1.getRect()).thenReturn(new Rectangle(0,  0,  101,  4));
		when(element.getRect()).thenReturn(new Rectangle(0,  0,  100,  100));
		when(snapshotTarget.getElement()).thenReturn(element);
		when(snapshotTarget.isPageTarget()).thenReturn(false);
		when(snapshotTarget.isElementTarget()).thenReturn(true);
		
		SnapshotCheckType checkType = SnapshotCheckType.FULL.exclude(elementToExclude1);
		checkType.check(snapshotTarget);
		
		Assert.assertEquals(checkType.getExcludeElementsRect().size(), 0);
	}
	/**
	 * Check element excluded from captured element but outside of it is not kept
	 * element to exclude is higher than enclosing element
	 */
	@Test(groups= {"ut"})
	public void testExcludeElementOutsideElement6() {
		when(elementToExclude1.getRect()).thenReturn(new Rectangle(0,  0,  3,  101));
		when(element.getRect()).thenReturn(new Rectangle(0,  0,  100,  100));
		when(snapshotTarget.getElement()).thenReturn(element);
		when(snapshotTarget.isPageTarget()).thenReturn(false);
		when(snapshotTarget.isElementTarget()).thenReturn(true);
		
		SnapshotCheckType checkType = SnapshotCheckType.FULL.exclude(elementToExclude1);
		checkType.check(snapshotTarget);
		
		Assert.assertEquals(checkType.getExcludeElementsRect().size(), 0);
	}
	
	/**
	 * When capture is done on screen, no element can be excluded
	 */
	@Test(groups= {"ut"})
	public void testExcludeElementFromScreen() {
		when(elementToExclude1.getRect()).thenReturn(new Rectangle(1,  2,  3,  4));
		when(snapshotTarget.isPageTarget()).thenReturn(false);
		when(snapshotTarget.isElementTarget()).thenReturn(false);
		
		SnapshotCheckType checkType = SnapshotCheckType.FULL.exclude(elementToExclude1);
		checkType.check(snapshotTarget);
		
		Assert.assertEquals(checkType.getExcludeElementsRect().size(), 0);
	}
}
