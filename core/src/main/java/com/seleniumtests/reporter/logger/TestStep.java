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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.WebDriverException;
import org.testng.ITestResult;

import com.seleniumtests.core.Step.RootCause;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.reporter.reporters.CommonReporter;
import com.seleniumtests.util.ExceptionUtility;

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
	private static final String BEFORE_STEP_PREFIX = "before-";
	private List<TestAction> stepActions;
	private Long duration;
	private Date startDate;
	private long videoTimeStamp;
	private List<HarCapture> harCaptures;
	private List<GenericFile> files;
	private List<Snapshot> snapshots;
	private ITestResult testResult;
	private RootCause errorCause;
	private String errorCauseDetails;
	private Integer stepResultId;  // the stepResult if it has been recorded on seleniumRobot-server
	private boolean disableBugtracker;
	
	public enum StepStatus {
		SUCCESS,
		FAILED,
		WARNING
	}
	
	/**
	 * 
	 * @param name			action name
	 * @param testResult	associated TestNG result
	 * @param pwdToReplace	list of string to replace when returning actions so that passwords are masked
	 */
	public TestStep(String name, ITestResult testResult, List<String> pwdToReplace, boolean maskPassword) {
		this(name, testResult, pwdToReplace, maskPassword, RootCause.NONE, null, false);
	}
	public TestStep(String name, ITestResult testResult, List<String> pwdToReplace, boolean maskPassword, RootCause errorCause, String errorCauseDetails, boolean disableBugtracker) {
		super(name, false, pwdToReplace);
		this.maskPassword = maskPassword;
		stepActions = new ArrayList<>();
		files = new ArrayList<>();
		harCaptures = new ArrayList<>();
		snapshots = new ArrayList<>();
		duration = 0L;
		startDate = new Date();
		this.testResult = testResult;
		
		if (errorCause != RootCause.NONE) {
			this.errorCause = errorCause;
			this.errorCauseDetails = errorCauseDetails;
		}
		this.disableBugtracker = disableBugtracker;
	}
	
	public Long getDuration() {
		long consolidatedDuration = duration;
		consolidatedDuration -= getDurationToExclude();

		return Math.max(0, consolidatedDuration);
	}
	
	public boolean isTestEndStep() {
		return TestStepManager.LAST_STEP_NAME.equals(name);
	}
	
	public void setStartDate() {
		startDate = new Date();
	}
	
	public void setStartDate(Date date) {
		startDate = date;
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
	 * Return 
	 * StepStatus.FAILED if and only if the step is marked as failed
	 * StepStatus.WARNIG if any of the sub actions (or steps) are failed, but not the step itself
	 * StepStatus.SUCESS in any other cases
	 */
	public StepStatus getStepStatus() {
		if (Boolean.TRUE.equals(getFailed())) {
			return StepStatus.FAILED;
		} 
		for (TestAction action: stepActions) {
			if ((action instanceof TestStep && ((TestStep) action).getStepStatus() != StepStatus.SUCCESS)
					|| Boolean.TRUE.equals(action.getFailed())) {
				return StepStatus.WARNING;
			}
			
		}
		return StepStatus.SUCCESS;
	}
	
	public String getExceptionMessage(String format) {
		if (actionException == null) {
			return "";
		}
		StringBuilder stackString = new StringBuilder();
		ExceptionUtility.generateTheStackTrace(actionException, actionException.getMessage(), stackString, format);
		return stackString.toString();
	}

	public void addAction(TestAction action) {
		action.setPosition(stepActions.size());
		stepActions.add(action);
		action.setParent(this);
		
		// add replacement of the parent to this action
		action.pwdToReplace.addAll(pwdToReplace);
		
		// inherit password masking from step
		action.maskPassword = maskPassword;
	}
	public void addMessage(TestMessage message) {
		message.setPosition(stepActions.size());
		stepActions.add(message);
		message.setParent(this);

		// add replacement of the parent to this message
		message.pwdToReplace.addAll(pwdToReplace);

		// inherit password masking from step
		message.maskPassword = maskPassword;
	}
	public void addValue(TestValue value) {
		value.setPosition(stepActions.size());
		stepActions.add(value);
		value.setParent(this);
	}
	public void addStep(TestStep step) {
		step.setPosition(stepActions.size());
		stepActions.add(step);
		step.setParent(this);
		
		// add replacement of the parent step to this step
		step.pwdToReplace.addAll(pwdToReplace);

		// inherit password masking from step
		step.maskPassword = maskPassword;
	}
	public void addNetworkCapture(HarCapture har) {
		har.setPosition(stepActions.size());
		harCaptures.add(har);
		har.setParent(this);
	}
	public void addFile(GenericFile file) {
		file.setPosition(stepActions.size());
		files.add(file);
		file.setParent(this);
	}
	
	/**
	 * Add snapshot to this step
	 * @param snapshot		the snapshot
	 * @param stepIdx		the index of this step in the test
	 * @param userGivenName name of the snapshot, user wants to display
	 */
	public void addSnapshot(Snapshot snapshot, int stepIdx, String userGivenName) {
		snapshot.setPosition(snapshots.size());
		
		// rename file so that user can easily consult it
		snapshot.rename(this, position, snapshot.position + getSnapshotIndex(), userGivenName);
		
		snapshots.add(snapshot);
		snapshot.setParent(this);
	}
	
	private int getSnapshotIndex() {
		return getSnapshotIndex(1) - position;
	}
	private int getSnapshotIndex(int multiplier) {
		int idx = (position + 1) * multiplier;
		if (getParent() instanceof TestStep) {
			idx += ((TestStep)getParent()).getSnapshotIndex(multiplier * 10);
		} 
		return idx;
	}
	
	@Override
	public String toString() {
		return toString(0);
	}
	
	@Override
	public String toString(int spaces) {
		String currentIndent = StringUtils.repeat(" ", spaces);
		StringBuilder testStepRepr = new StringBuilder(String.format("Step %s\n", getName()));
		for (TestAction testAction: getStepActions()) {
			testStepRepr.append(currentIndent + "  - " + testAction.toString(spaces + 2) + "\n");
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
		return getAllAttachments(false);
	}
	public List<File> getAllAttachments(boolean onlyPictureOfSnapshots) {
		List<File> usedFiles = new ArrayList<>();
		for (Snapshot snapshot: snapshots) {
			if (snapshot == null || snapshot.getScreenshot() == null ) {
				continue;
			}
			
			if (snapshot.getScreenshot().getFullHtmlPath() != null && !onlyPictureOfSnapshots) {
				try {
					usedFiles.add(new File(snapshot.getScreenshot().getFullHtmlPath()).getCanonicalFile());
				} catch (IOException e) {
					// error while getting HtmlPath
				}
			}
			if (snapshot.getScreenshot().getFullImagePath() != null) {
				try {
					usedFiles.add(new File(snapshot.getScreenshot().getFullImagePath()).getCanonicalFile());
				} catch (IOException e) {
					// error getting image path
				}
			}
		}
		
		// add attachments declared in sub-steps
		for (TestAction subStep: stepActions.stream().filter(a -> a instanceof TestStep).collect(Collectors.toList())) {
			usedFiles.addAll(((TestStep)subStep).getAllAttachments(onlyPictureOfSnapshots));
		}
		
		usedFiles.addAll(getFiles().stream().map(GenericFile::getFile).collect(Collectors.toList()));
		usedFiles.addAll(getHarCaptures().stream().map(HarCapture::getFile).collect(Collectors.toList()));
		
		return usedFiles;
	}
	
	/**
	 * move attachments from "before-" folder to outputDirectory folder
	 * @param outputDirectory
	 * @throws IOException 
	 */
	public void moveAttachments(String outputDirectory) throws IOException {

		for (Snapshot snapshot: snapshots) {
			if (snapshot == null 
					|| snapshot.getScreenshot() == null 
					|| snapshot.getScreenshot().getFullHtmlPath() == null 
					|| !snapshot.getScreenshot().getFullHtmlPath().contains(BEFORE_STEP_PREFIX) ) {
				continue;
			}
			try {
				snapshot.relocate(outputDirectory);
			} catch (IOException e) {
				logger.error("Cannot relocate snapshot: " + e.getMessage());
			}
			
		}
	
		// move attachments in sub-steps
		for (TestAction subStep: stepActions.stream().filter(a -> a instanceof TestStep).collect(Collectors.toList())) {
			((TestStep)subStep).moveAttachments(outputDirectory);
		}
	
		for (HarCapture harCapture: harCaptures) {
			if (harCapture.getFile().toString().contains(BEFORE_STEP_PREFIX)) {
				harCapture.relocate(outputDirectory);
			}
		}
		
		for (GenericFile genericFile: files) {
			if (genericFile.getFile().toString().contains(BEFORE_STEP_PREFIX)) {
				genericFile.relocate(outputDirectory);
			}
		}
	}

	public List<HarCapture> getHarCaptures() {
		return harCaptures;
	}

	public void setHarCaptures(List<HarCapture> harCaptures) {
		this.harCaptures = harCaptures;
	}
	

	/**
	 * @deprecated use 'getRootCause' instead
	 * @return
	 */
	@Deprecated
	public RootCause getErrorCause() {
		return getRootCause();
	}

	public RootCause getRootCause() {
		if (getStepStatus() == StepStatus.SUCCESS) {
			return null;
		}
		if (errorCause != null) {
			return errorCause;
		} 
		for (TestAction action: stepActions) {
			if ((action instanceof TestStep && ((TestStep) action).getRootCause() != null)) {
				return ((TestStep) action).getRootCause();
			}
		}
		
		return errorCause;
	}
	

	/**
	 * @deprecated use getRootCauseDetails instead
	 * @return
	 */
	@Deprecated
	public String getErrorCauseDetails() {
		return getRootCauseDetails();
	}
	
	/**
	 * Returns the error cause detail of the step or any sub step
	 * @return
	 */
	public String getRootCauseDetails() {
		if (getStepStatus() == StepStatus.SUCCESS) {
			return null;
		}
		if (errorCause != null) {
			return errorCauseDetails;
		} 
		for (TestAction action: stepActions) {
			if ((action instanceof TestStep && ((TestStep) action).getRootCause() != null)) {
				return ((TestStep) action).getRootCauseDetails();
			}
		}
		return errorCauseDetails;
	}
	public boolean isDisableBugtracker() {
		return disableBugtracker;
	}
	public List<GenericFile> getFiles() {
		return files;
	}

	/**
	 * Creates an new TestStep, encoding text of sub-steps and elements.
	 */
	@Override
	public TestStep encode(String format) {

		List<String> encodedPasswords = encodePasswords(pwdToReplace, format);
		TestStep step = new TestStep(encodeString(name, format), testResult, encodedPasswords, maskPassword);
		
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
		step.videoTimeStamp = videoTimeStamp;
		step.harCaptures = new ArrayList<>();
		for (HarCapture har: harCaptures) {
			step.harCaptures.add(har);
		}
		step.actionException = actionException;
		if (actionException != null) {
			step.actionExceptionMessage = encodeString(ExceptionUtility.getExceptionMessage(actionException), format);
		}
		

		step.errorCause = errorCause;
		step.errorCauseDetails = encodeString(errorCauseDetails, format);
		step.disableBugtracker = disableBugtracker;
		
		return step;
	}

	@Override
	public TestStep deepCopy() {
		return encode(null);
	}
	public long getVideoTimeStamp() {
		return videoTimeStamp;
	}
	public void setVideoTimeStamp(long videoTimeStamp) {
		this.videoTimeStamp = videoTimeStamp;
	}
	public Integer getStepResultId() {
		return stepResultId;
	}
	public void setStepResultId(Integer stepResultId) {
		this.stepResultId = stepResultId;
	}
	
}
