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
package com.seleniumtests.reporter.logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.ITestResult;

import com.seleniumtests.reporter.reporters.CommonReporter;

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
	private Date startDate;
	private List<Snapshot> snapshots;
	private ITestResult testResult;
	
	/**
	 * 
	 * @param name			action name
	 * @param testResult	associated TestNG result
	 * @param pwdToReplace	list of string to replace when returning actions so that passwords are masked
	 */
	public TestStep(String name, ITestResult testResult, List<String> pwdToReplace) {
		super(name, false, pwdToReplace);
		stepActions = new ArrayList<>();
		snapshots = new ArrayList<>();
		duration = 0L;
		startDate = new Date();
		this.testResult = testResult;
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
	
	public void updateDuration() {
		duration = new Date().getTime() - startDate.getTime();
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
		
		// add replacement of the parent to this action
		action.pwdToReplace.addAll(pwdToReplace);
	}
	public void addMessage(TestMessage message) {
		stepActions.add(message);

		// add replacement of the parent to this message
		message.pwdToReplace.addAll(pwdToReplace);
	}
	public void addValue(TestValue value) {
		stepActions.add(value);
	}
	public void addStep(TestStep step) {
		stepActions.add(step);
		
		// add replacement of the parent step to this step
		step.pwdToReplace.addAll(pwdToReplace);
	}
	
	/**
	 * Add snapshot to this step
	 * @param snapshot		the snapshot
	 * @param stepIdx		the index of this step in the test
	 */
	public void addSnapshot(Snapshot snapshot, int stepIdx) {
		// rename file so that user can easily consult it
		snapshot.rename(this, stepIdx, snapshots.size() + 1);
		
		snapshots.add(snapshot);
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
		
		stepJSon.put("name", getName());
		stepJSon.put("type", "step");
		
		stepJSon.put("actions", new JSONArray());
		for (TestAction testAction: getStepActions()) {
			stepJSon.getJSONArray("actions").put(testAction.toJson());
		}
		
		return stepJSon;
	}

	public Date getStartDate() {
		return startDate;
	}

	public List<Snapshot> getSnapshots() {
		return snapshots;
	}

	public ITestResult getTestResult() {
		return testResult;
	}
	
	/**
	 * Returns the list of files referenced by this test step and sub-steps
	 * @return
	 */
	public List<File> getAllAttachments() {
		List<File> usedFiles = new ArrayList<>();
		for (Snapshot snapshot: snapshots) {
			if (snapshot == null || snapshot.getScreenshot() == null ) {
				continue;
			}
			
			if (snapshot.getScreenshot().getFullHtmlPath() != null) {
				usedFiles.add(new File(snapshot.getScreenshot().getFullHtmlPath()));
			}
			if (snapshot.getScreenshot().getFullImagePath() != null) {
				usedFiles.add(new File(snapshot.getScreenshot().getFullImagePath()));
			}
		}
		
		for (TestAction subStep: stepActions.stream().filter(a -> a instanceof TestStep).collect(Collectors.toList())) {
			usedFiles.addAll(((TestStep)subStep).getAllAttachments());
		}
		
		return usedFiles;
	}
	
}
