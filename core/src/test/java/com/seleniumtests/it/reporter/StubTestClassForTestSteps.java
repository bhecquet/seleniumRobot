package com.seleniumtests.it.reporter;

import java.io.IOException;

import org.testng.annotations.Test;

public class StubTestClassForTestSteps extends StubParentClass {

	@Test(groups="stub")
	public void testPage() throws IOException {
		new StubTestPage()
			.doSomething()
			.doSomethingElse();
	}
}
