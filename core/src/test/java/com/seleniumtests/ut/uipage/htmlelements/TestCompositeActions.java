/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.ut.uipage.htmlelements;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.SessionId;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.util.helper.WaitHelper;

import io.appium.java_client.AppiumDriver;

@PrepareForTest({WebUIDriver.class, WaitHelper.class})
public class TestCompositeActions extends MockitoTest {

	@Mock
	private RemoteWebDriver driver;
	
	@Mock
	private RemoteWebElement element;
	
	@Mock
	private Mouse mouse;
	
	@Mock
	private Coordinates coordinates;
	
	private CustomEventFiringWebDriver eventDriver;
	

	@BeforeMethod(groups={"ut"})
	private void init() {
		
		eventDriver = spy(new CustomEventFiringWebDriver(driver));
		
		PowerMockito.mockStatic(WebUIDriver.class);
		when(WebUIDriver.getWebDriver()).thenReturn(eventDriver);
		when(driver.getMouse()).thenReturn(mouse);

	}
	
	/**
	 * Checks that CompositeAction.updateHandles() aspect is called when 
	 * a click is done in a composite action
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testUpdateHandles() throws Exception {
		new Actions(eventDriver).click().perform();

		// check handled are updated on click
		verify(eventDriver).updateWindowsHandles();
	}
	
	@Test(groups={"ut"})
	public void testHandlesNotUpdated() throws Exception {
		new Actions(eventDriver).clickAndHold().perform();
		
		// check handled are not updated when no click is done
		verify(eventDriver, never()).updateWindowsHandles();
	}
	
	/**
	 * Checks that CompositeAction.updateHandles() aspect is called when 
	 * a click is done in a composite action with a driver supporting new actions (the real driver)
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testUpdateHandlesNewActions() throws Exception {
		CommandExecutor ce = Mockito.mock(CommandExecutor.class);
		Response response = new Response(new SessionId("1"));
		response.setValue(new HashMap<String, Object>());
		Response findResponse = new Response(new SessionId("1"));
		findResponse.setValue(element);

		// newSession, getSession, getSession, findElement
		when(ce.execute(anyObject())).thenReturn(response, response, response, findResponse);
		driver = new RemoteWebDriver(ce, new DesiredCapabilities());

		eventDriver = spy(new CustomEventFiringWebDriver(driver));
		when(WebUIDriver.getWebDriver()).thenReturn(eventDriver);
		
		new Actions(eventDriver.getWebDriver()).click().perform();
		
		// check handled are updated on click
		verify(eventDriver).updateWindowsHandles();
	}
	
	/**
	 * No update when only down or up action is done
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testUpdateHandlesNotUpdatedNewActions() throws Exception {
		new Actions(eventDriver.getWebDriver()).clickAndHold().perform();
		
		// check handled are updated on click
		verify(eventDriver, never()).updateWindowsHandles();
	}

	
	/**
	 * Test replay of CompositeAction
	 */
	@Test(groups={"ut"})
	public void testReplayOnSearch() {
		when(element.getCoordinates()).thenThrow(WebDriverException.class).thenThrow(WebDriverException.class).thenReturn(coordinates);
		new Actions(eventDriver).click(element).perform();
		
		// coordinates search is done 3 times, because of errors
		verify(element, atLeast(3)).getCoordinates();

	}
	
	/**
	 * Test replay when error occurs in any part of the action (except search)
	 */
	@Test(groups={"ut"})
	public void testReplayOnPerform() {
		when(element.getCoordinates()).thenReturn(coordinates);
		doThrow(new WebDriverException("error clicking")).doNothing().when(mouse).click(coordinates);
		
		new Actions(eventDriver).click(element).perform();

		verify(mouse, times(2)).click(coordinates);
	}
}
