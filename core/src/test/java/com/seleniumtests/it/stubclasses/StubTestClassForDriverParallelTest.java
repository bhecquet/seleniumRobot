/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
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
package com.seleniumtests.it.stubclasses;

import java.lang.reflect.Method;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.util.helper.WaitHelper;

public class StubTestClassForDriverParallelTest extends StubParentClass {
	
	private static int threadCount = 0;
	public static final int maxThreads = 4;
	private static Object sync = new Object();

	@BeforeClass(groups="stub")
	public static void initClass() {
		threadCount = 0;
	}
	
	@BeforeMethod(groups="stub")
	public void init(Method method) {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(true);
		SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(false);
		if (System.getProperty(SeleniumTestsContext.TEST_RETRY_COUNT) == null) {
			SeleniumTestsContextManager.getThreadContext().setTestRetryCount(0);
		}
	}

	
	private void execute(String text) throws Exception {

		DriverTestPage drvPage = new DriverTestPage(true);
		synchronized (sync) {
			threadCount += 1;
		}
		while (threadCount < maxThreads) {
			WaitHelper.waitForMilliSeconds(5);
		}
		drvPage._writeSomething(text);
		Assert.assertEquals(drvPage._getTextElementContent(), text);
	}
	
	@Test(groups="stub")
	public void testDriver1() throws Exception {
		execute("text1");
	}
	
	@Test(groups="stub")
	public void testDriver2() throws Exception {
		execute("text2");
	}
	@Test(groups="stub")
	public void testDriver3() throws Exception {
		execute("text3");
	}
	@Test(groups="stub")
	public void testDriver4() throws Exception {
		execute("text4");
	}
//	@Test(groups="stub")
//	public void testDriver5() throws Exception {
//		execute("text5");
//	}
//	@Test(groups="stub")
//	public void testDriver6() throws Exception {
//		execute("text6");
//	}
//	@Test(groups="stub")
//	public void testDriver7() throws Exception {
//		execute("text7");
//	}
//	@Test(groups="stub")
//	public void testDriver8() throws Exception {
//		execute("text8");
//	}


}
