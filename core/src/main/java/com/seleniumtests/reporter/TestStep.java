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

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Group of test actions
 * Represent each method call inside a PageObject during the test
 * TestStep allows to log any action done during the test. See {@link com.seleniumtests.core.aspects.LogAction#logTestStep }
 * We get a tree like this:
 * root (TestStep)
 * +--- action1 (TestAction)
 * +--+ sub-step1 (TestStep)
 *    +--- sub-action1
 *    +--- message (TestMessage)
 *    +--- sub-action2
 * +--- action2
 * 
 * Each TestStep is then logged in {@link TestLogging TestLogging class} with TestLogging.logTestStep() method
 * 
 * @author behe
 *
 */
public class TestStep extends TestAction {
	private List<TestAction> stepActions;
	private Long duration;
	private String snapshot; // store at most 1 snapshot. It will be the last one if more than one are recorded
	
	public TestStep(String name) {
		super(name, false);
		stepActions = new ArrayList<>();
		duration = 0L;
	}
	
	public Long getDuration() {
		return duration;
	}

	/**
	 * set duration in milliseconds
	 * @param duration
	 */
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
	
	public String getExceptionMessage() {
		if (actionException == null) {
			return "";
		}
		StringBuilder stackString = new StringBuilder();
		CommonReporter.generateTheStackTrace(actionException, actionException.getMessage(), stackString);
		return stackString.toString();
	}

	public void addAction(TestAction action) {
		stepActions.add(action);
	}
	public void addMessage(TestMessage action) {
		stepActions.add(action);
	}
	public void addStep(TestStep step) {
		stepActions.add(step);
	}
	
	@Override
	public String toString() {
		StringBuilder testStepRepr = new StringBuilder(String.format("Step %s\n", getName()));
		for (TestAction testAction: getStepActions()) {
			testStepRepr.append(testAction.toString() + "\n");
		}
		
		return testStepRepr.toString().trim();
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject stepJSon = new JSONObject();
		
		stepJSon.put("name", name);
		stepJSon.put("type", "step");
		
		stepJSon.put("actions", new JSONArray());
		for (TestAction testAction: getStepActions()) {
			stepJSon.getJSONArray("actions").put(testAction.toJson());
		}
		
		return stepJSon;
	}

	public String getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(String snapshot) {
		this.snapshot = snapshot;
	}
	
	
}
