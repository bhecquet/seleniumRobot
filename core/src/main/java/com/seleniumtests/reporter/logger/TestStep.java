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
package com.seleniumtests.reporter.logger;

import java.io.File;
import java.io.IOException;
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
 * Each TestStep is then logged in {@link TestLogging TestLogging class} with ScenarioLogger.logTestStep() method
 * 
 * @author behe
 *
 */
public class TestStep extends TestAction {
	private List<TestAction> stepActions;
	private Long duration;
	private Date startDate;
	private List<HarCapture> harCaptures;
	private List<GenericFile> files;
	private List<Snapshot> snapshots;
	private ITestResult testResult;
	
	/**
	 * 
	 * @param name			action name
	 * @param testResult	associated TestNG result
	 * @param pwdToReplace	list of string to replace when returning actions so that passwords are masked
	 */
	public TestStep(String name, ITestResult testResult, List<String> pwdToReplace, boolean maskPassword) {
		super(name, false, pwdToReplace);
		this.maskPassword = maskPassword;
		stepActions = new ArrayList<>();
		files = new ArrayList<>();
		harCaptures = new ArrayList<>();
		snapshots = new ArrayList<>();
		duration = 0L;
		startDate = new Date();
		this.testResult = testResult;
	}
	
	public Long getDuration() {
		long consolidatedDuration = duration;
		consolidatedDuration -= getDurationToExclude();

		return Math.max(0, consolidatedDuration);
	}
	
	@Override
	public long getDurationToExclude() {
		long consolidatedDurationToExclude = durationToExclude; 
		
		for (Snapshot snapshot: snapshots) {
			consolidatedDurationToExclude += snapshot.getDurationToExclude();
		}
		
		for (TestAction action: stepActions) {
			consolidatedDurationToExclude += action.getDurationToExclude();
		}
		
		return consolidatedDurationToExclude;
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
	
	public String getExceptionMessage(String format) {
		if (actionException == null) {
			return "";
		}
		StringBuilder stackString = new StringBuilder();
		CommonReporter.generateTheStackTrace(actionException, actionException.getMessage(), stackString, format);
		return stackString.toString();
	}

	public void addAction(TestAction action) {
		stepActions.add(action);
		
		// add replacement of the parent to this action
		action.pwdToReplace.addAll(pwdToReplace);
		
		// inherit password masking from step
		action.maskPassword = maskPassword;
	}
	public void addMessage(TestMessage message) {
		stepActions.add(message);

		// add replacement of the parent to this message
		message.pwdToReplace.addAll(pwdToReplace);

		// inherit password masking from step
		message.maskPassword = maskPassword;
	}
	public void addValue(TestValue value) {
		stepActions.add(value);
	}
	public void addStep(TestStep step) {
		stepActions.add(step);
		
		// add replacement of the parent step to this step
		step.pwdToReplace.addAll(pwdToReplace);

		// inherit password masking from step
		step.maskPassword = maskPassword;
	}
	public void addNetworkCapture(HarCapture har) {
		harCaptures.add(har);
	}
	public void addFile(GenericFile file) {
		files.add(file);
	}
	
	/**
	 * Add snapshot to this step
	 * @param snapshot		the snapshot
	 * @param stepIdx		the index of this step in the test
	 * @param userGivenName name of the snapshot, user wants to display
	 */
	public void addSnapshot(Snapshot snapshot, int stepIdx, String userGivenName) {
		// rename file so that user can easily consult it
		snapshot.rename(this, stepIdx, snapshots.size() + 1, userGivenName);
		
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
		
		stepJSon.put("name", encodeString(getName(), "json"));
		stepJSon.put("type", "step");
		stepJSon.put("harCaptures", new JSONArray());
		for (HarCapture harCapture: getHarCaptures()) {
			stepJSon.getJSONArray("harCaptures").put(harCapture.toJson());
		}
		
		stepJSon.put("actions", new JSONArray());
		for (TestAction testAction: getStepActions()) {
			stepJSon.getJSONArray("actions").put(testAction.toJson());
		}
		
		stepJSon.put("files", new JSONArray());
		for (GenericFile file: getFiles()) {
			stepJSon.getJSONArray("files").put(file.toJson());
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
				try {
					usedFiles.add(new File(snapshot.getScreenshot().getFullHtmlPath()).getCanonicalFile());
				} catch (IOException e) {
				}
			}
			if (snapshot.getScreenshot().getFullImagePath() != null) {
				try {
					usedFiles.add(new File(snapshot.getScreenshot().getFullImagePath()).getCanonicalFile());
				} catch (IOException e) {
				}
			}
		}
		
		for (TestAction subStep: stepActions.stream().filter(a -> a instanceof TestStep).collect(Collectors.toList())) {
			usedFiles.addAll(((TestStep)subStep).getAllAttachments());
		}
		
		return usedFiles;
	}

	public List<HarCapture> getHarCaptures() {
		return harCaptures;
	}

	public void setHarCaptures(List<HarCapture> harCaptures) {
		this.harCaptures = harCaptures;
	}
	

	public List<GenericFile> getFiles() {
		return files;
	}

	/**
	 * Creates an new TestStep, encoding text of sub-steps and elements.
	 */
	@Override
	public TestStep encode(String format) {
		TestStep step = new TestStep(encodeString(name, format), testResult, new ArrayList<>(pwdToReplace), maskPassword);
		
		step.stepActions = new ArrayList<>();
		for (TestAction testAction: stepActions) {
			step.stepActions.add(testAction.encode(format));
		}
		
		step.failed = failed;
		step.snapshots = new ArrayList<>(snapshots);
		step.files = new ArrayList<>();
		for (GenericFile file: files) {
			step.files.add(file.encode(format));
		}
		
		if (format == null) {
			step.encoded = encoded;
		} else {
			step.encoded = true;
		}
		
		step.duration = duration;
		step.startDate = startDate;
		step.harCaptures = new ArrayList<>();
		for (HarCapture har: harCaptures) {
			step.harCaptures.add(har);
		}
		step.actionException = actionException;
		if (actionException != null) {
			step.actionExceptionMessage = actionException.getClass().toString() + ": " + encodeString(actionException.getMessage(), format);
		}
		return step;
	}

	public TestStep deepCopy() {
		return encode(null);
	}
	
}
