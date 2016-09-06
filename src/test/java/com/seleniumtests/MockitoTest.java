package com.seleniumtests;

import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;

public class MockitoTest extends PowerMockTestCase {

	@BeforeMethod(alwaysRun=true)  
	public void injectDoubles() {
		MockitoAnnotations.initMocks(this); //This could be pulled up into a shared base class
}
}
