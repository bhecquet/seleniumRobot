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
package com.seleniumtests.reporter;

import java.util.ArrayList;
import java.util.List;

/**
 * Group of test actions
 * Represent each method call inside a PageObject during the test
 * @author behe
 *
 */
public class TestStep extends TestAction {
	private List<TestAction> stepActions;
	private Long duration;
	
	public TestStep(String name) {
		super(name, false);
		stepActions = new ArrayList<>();
		duration = null;
	}
	
	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public List<TestAction> getStepActions() {
		return stepActions;
	}

	/**
	 * Return true if this step or one of its actions / sub-step is failed
	 */
	@Override
	public Boolean getFailed() {
		if (super.getFailed()) {
			return true;
		} 
		for (TestAction action: stepActions) {
			if (action.getFailed()) {
				return true;
			}
		}
		return false;
	}

	public void addAction(TestAction action) {
		stepActions.add(action);
	}
	public void addStep(TestStep step) {
		stepActions.add(step);
	}
	
	
}
