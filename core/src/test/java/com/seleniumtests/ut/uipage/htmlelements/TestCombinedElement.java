/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.ut.uipage.htmlelements;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.mockito.Mock;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.uipage.htmlelements.CombinedElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;

public class TestCombinedElement extends MockitoTest {

	@Mock
	private HtmlElement element1;

	@Mock
	private HtmlElement element2;

	@Test(groups = { "ut" })
	public void testIsElementPresentAllPresent() {
		when(element1.isElementPresent()).thenReturn(true);
		when(element2.isElementPresent()).thenReturn(true);

		CombinedElement combinedElement = new CombinedElement(element1, element2);

		Assert.assertTrue(combinedElement.isElementPresent());
	}

	@Test(groups = { "ut" })
	public void testIsElementPresentOnePresentOneAbsent() {
		when(element1.isElementPresent()).thenReturn(true);
		when(element2.isElementPresent()).thenReturn(false);

		CombinedElement combinedElement = new CombinedElement(element1, element2);

		Assert.assertFalse(combinedElement.isElementPresent());
	}

	@Test(groups = { "ut" })
	public void testIsElementPresentNonePresent() {
		when(element1.isElementPresent()).thenReturn(false);
		when(element2.isElementPresent()).thenReturn(false);

		CombinedElement combinedElement = new CombinedElement(element1, element2);

		Assert.assertFalse(combinedElement.isElementPresent());
	}

	/**
	 * When first element is already absent, we should not even check the second one (short-circuit)
	 */
	@Test(groups = { "ut" })
	public void testIsElementPresentShortCircuit() {
		when(element1.isElementPresent()).thenReturn(false);

		CombinedElement combinedElement = new CombinedElement(element1, element2);

		Assert.assertFalse(combinedElement.isElementPresent());
		verify(element2, times(0)).isElementPresent();
	}

	@Test(groups = { "ut" })
	public void testIsElementPresentWithTimeoutAllPresent() {
		when(element1.isElementPresent(10)).thenReturn(true);
		when(element2.isElementPresent(10)).thenReturn(true);

		CombinedElement combinedElement = new CombinedElement(element1, element2);

		Assert.assertTrue(combinedElement.isElementPresent(10));
	}

	@Test(groups = { "ut" })
	public void testIsElementPresentWithTimeoutOneAbsent() {
		when(element1.isElementPresent(10)).thenReturn(true);
		when(element2.isElementPresent(10)).thenReturn(false);

		CombinedElement combinedElement = new CombinedElement(element1, element2);

		Assert.assertFalse(combinedElement.isElementPresent(10));
	}

	@Test(groups = { "ut" })
	public void testConstructorWithList() {
		when(element1.isElementPresent()).thenReturn(true);
		when(element2.isElementPresent()).thenReturn(true);

		List<HtmlElement> elementList = Arrays.asList(element1, element2);
		CombinedElement combinedElement = new CombinedElement(elementList);

		Assert.assertTrue(combinedElement.isElementPresent());
		Assert.assertEquals(combinedElement.getElements(), elementList);
	}

	@Test(groups = { "ut" })
	public void testGetElements() {
		CombinedElement combinedElement = new CombinedElement(element1, element2);

		Assert.assertEquals(combinedElement.getElements(), Arrays.asList(element1, element2));
	}

	@Test(groups = { "ut" }, expectedExceptions = NotImplementedException.class)
	public void testClickNotImplemented() {
		new CombinedElement(element1, element2).click();
	}

	@Test(groups = { "ut" }, expectedExceptions = NotImplementedException.class)
	public void testSendKeysNotImplemented() {
		new CombinedElement(element1, element2).sendKeys("foo");
	}

	@Test(groups = { "ut" }, expectedExceptions = NotImplementedException.class)
	public void testGetTextNotImplemented() {
		new CombinedElement(element1, element2).getText();
	}

	@Test(groups = { "ut" }, expectedExceptions = NotImplementedException.class)
	public void testIsDisplayedNotImplemented() {
		new CombinedElement(element1, element2).isDisplayed();
	}

	@Test(groups = { "ut" }, expectedExceptions = NotImplementedException.class)
	public void testIsElementPresentAndDisplayedNotImplemented() {
		new CombinedElement(element1, element2).isElementPresentAndDisplayed();
	}

	@Test(groups = { "ut" }, expectedExceptions = NotImplementedException.class)
	public void testFindElementNotImplemented() {
		new CombinedElement(element1, element2).findElement(By.id("foo"));
	}

	@Test(groups = { "ut" }, expectedExceptions = NotImplementedException.class)
	public void testGetByNotImplemented() {
		new CombinedElement(element1, element2).getBy();
	}

	@Test(groups = { "ut" }, expectedExceptions = NotImplementedException.class)
	public void testToStringNotImplemented() {
		new CombinedElement(element1, element2).toString();
	}

	@Test(groups = { "ut" }, expectedExceptions = NotImplementedException.class)
	public void testGetNameNotImplemented() {
		new CombinedElement(element1, element2).getName();
	}
}
