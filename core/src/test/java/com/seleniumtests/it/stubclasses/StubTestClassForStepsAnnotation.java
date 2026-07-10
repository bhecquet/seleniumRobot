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
package com.seleniumtests.it.stubclasses;

import org.testng.annotations.Test;

import com.seleniumtests.it.core.aspects.CalcPage;

public class StubTestClassForStepsAnnotation extends StubParentClass {

	@Test(groups="stub")
	public void testCauseWithErrorAndDetails() {
		new CalcPage()
		.addWithErrorCauseErrorAndDetails(1);
	}
	
	@Test(groups="stub")
	public void testCauseWithErrorNoDetails() {
		new CalcPage()
		.addWithErrorCauseError(1);
	}
	
	@Test(groups="stub")
	public void testCauseNoError() {
		new CalcPage()
		.addWithErrorCauseNoError(1);
	}
	
	@Test(groups="stub")
	public void testNoCauseAndError() {
		new CalcPage()
		.addNoCauseErrorNoDetails(1);
	}
	
}
