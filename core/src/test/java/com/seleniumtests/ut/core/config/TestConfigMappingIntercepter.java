/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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
package com.seleniumtests.ut.core.config;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.GenericTest;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.uipage.Locator;
import com.seleniumtests.uipage.aspects.InterceptBy;

/**
 * Check that InterceptBy aspect is working and has been weaved
 * @author behe
 *
 */
public class TestConfigMappingIntercepter extends GenericTest {
	@BeforeMethod(groups={"ut"})
	public void initContext(final ITestContext testNGCtx, final XmlTest xmlTest) {
		InterceptBy.setPage("TestConfigMobileIntercepter");
		initThreadContext(testNGCtx);
	}
	
	@Test(groups={"ut"})
	public void interceptBy() {
		Assert.assertEquals(By.id("map:id").toString(), "By.id: login", "intercept by with map doesn't work");
	}

	@Test(groups={"ut"})
	public void interceptByLocator() {
		Assert.assertEquals(Locator.locateById("map:id").toString(), "By.id: login", "intercept by Locator doesn't work");
	}
	
	@Test(groups={"ut"})
	public void noChangeBy() {
		Assert.assertEquals(By.id("id").toString(), "By.id: id", "no change when no key word doesn't work");
	}
	
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class)
	public void exceptionWhenNoPresence() {
		By.id("map:name");
	}

}
