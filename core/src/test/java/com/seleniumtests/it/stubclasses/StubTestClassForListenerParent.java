package com.seleniumtests.it.stubclasses;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.xml.XmlTest;

import com.seleniumtests.reporter.logger.TestLogging;

public class StubTestClassForListenerParent extends StubParentClass {

	@BeforeTest
	public void beforeTestInParent(XmlTest xmlTest) {
		TestLogging.info("beforeTest parent call");
	}
	
	@AfterClass
	public void afterClassInParent(XmlTest xmlTest) {
		TestLogging.info("afterClass parent call");
	}
}
