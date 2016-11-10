package com.seleniumtests.it.reporter;

import java.io.IOException;

import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.uipage.PageObject;

public class StubTestPage extends PageObject {

	public StubTestPage() throws IOException {
		super();
	}
	
	public StubTestPage doSomething() {
		TestLogging.log("tell me why");
		TestLogging.info("an info message");
		return this;
	}
	
	public StubTestPage doSomethingElse() {
		TestLogging.log("Hello");
		return this;
	}

}
