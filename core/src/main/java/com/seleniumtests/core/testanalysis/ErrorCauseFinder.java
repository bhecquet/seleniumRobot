package com.seleniumtests.core.testanalysis;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.testng.ITestResult;

import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.connectors.selenium.fielddetector.Field;
import com.seleniumtests.connectors.selenium.fielddetector.ImageFieldDetector;
import com.seleniumtests.connectors.selenium.fielddetector.ImageFieldDetector.FieldType;
import com.seleniumtests.connectors.selenium.fielddetector.Label;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.imaging.ImageProcessor;
import com.seleniumtests.util.imaging.StepReferenceComparator;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Class that may help the tester to know what if the cause of test failure
 * - we were not on the right page when performing the last step
 * - we were on the right page, but an error occured
 * 		- error message is displayed
 * 		- a field is in error
 * - we were on the right page, but this page has slightly changed and a new field may have appeared
 * 
 * Some other errors may be found using network data (probably using selenium 4 features)
 * - some resources took too long to display
 * - error loading resources (HTTP code different from 2xx or 3xx)
 * 
 * @author S047432
 *
 */
public class ErrorCauseFinder {
	
	public static final String CLASS_ERROR_FIELD = "error_field";

	public static final String CLASS_ERROR_MESSAGE = "error_message";

	// TODO: link to RootCause and RootCause details from the Step annotation which can indicate what is the root cause of this error (declarative for a step)

	private static final String[] ERROR_WORDS = new String[] {"error", "erreur", "problem", "problème"};

	private static final Logger logger = SeleniumRobotLogger.getLogger(ErrorCauseFinder.class);
	
	private ITestResult testResult;
	
	
	
	
	public ErrorCauseFinder(ITestResult testResult) {
		this.testResult = testResult;
	}
	
	/**
	 * Try to find what caused the test to fail
	 * @return
	 */
	public List<ErrorCause> findErrorCause() {
		
		
		
		List<ErrorCause> causes = new ArrayList<>();
		
		causes.addAll(findErrorInLastStepSnapshots());
		causes.addAll(compareStepInErrorWithReference());
		
		logger.info(String.format("Found %d causes of error", causes.size()));
		
		return causes;
	}
	
	/**
	 * Compare the failed step with it's reference that can be found on seleniumRobot server
	 * If reference cannot be found, skip this step
	 * @return
	 */
	public List<ErrorCause> compareStepInErrorWithReference() {
		logger.info("Searching causes: comparing with references");
		List<ErrorCause> causes = new ArrayList<>();
		
		// do not seearch again
		if (TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult)) {
			return causes; 
		}
		
		// don't analyze if result has not been recorded on seleniumRobot server
		TestStepManager testStepManager = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager();
		if (testStepManager.getLastTestStep() == null || testStepManager.getLastTestStep().getStepResultId() == null) {
			return causes;
		}
		
		for (TestStep testStep: testStepManager.getTestSteps()) {

			Integer stepResultId = testStep.getStepResultId(); // stepResultId is set when step recording is done on server
			if (Boolean.TRUE.equals(testStep.getFailed()) && !(testStep.getActionException() instanceof AssertionError) && stepResultId != null) {
				
				try {
					Snapshot stepSnapshot = testStep.getSnapshots()
						.stream()
						.filter(s -> s.getCheckSnapshot().recordSnapshotOnServerForReference())
						.collect(Collectors.toList()).get(0);
					File stepSnapshotFile = new File(stepSnapshot.getScreenshot().getFullImagePath());
					
					File referenceSnapshot = SeleniumRobotSnapshotServerConnector.getInstance().getReferenceSnapshot(stepResultId);
					if (referenceSnapshot == null) {
						continue;
					}
					
					// perform a match between the picture of this step and the reference stored on server
					// We look at presence, position and text of each field
					List<Label> missingLabels = new ArrayList<>();
					List<Field> missingFields = new ArrayList<>();
					int matching = compareReferenceToStepSnapshot(stepSnapshotFile, referenceSnapshot, missingLabels, missingFields);
					
					// bad matching: the reference does not match at all the current step, we will check with other reference steps
					if (matching < 50) {
						searchMatchingInPreviousStep(testStepManager, testStep, stepSnapshotFile, causes);
						
					// middle matching: we may be on the right web page but the page has changed (some fields appeared or disappeared)
					// or the text changed slightly. This could mean application changed
					} else if (matching < 90) {
						
						// draw missing labels and fields
						for (Label missingLabel: missingLabels) {
							Rectangle rect = missingLabel.getRectangle();
							Line2D.Double line = new Line2D.Double(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y + rect.height);
							ImageProcessor.drawLines(referenceSnapshot, Color.RED, line);
						}
						ImageProcessor.drawRectangles(referenceSnapshot, Color.RED, missingFields.stream().map(Field::getRectangle).collect(Collectors.toList()).toArray(new Rectangle[] {}));
						
						causes.add(new ErrorCause(ErrorType.APPLICATION_CHANGED, formatApplicationChangedDescription(missingLabels, missingFields), testStep));
					}
					
					// else, very good matching: we are on the same web page, error does not come from there
					
					
					break;
					
				} catch (IndexOutOfBoundsException e) {
					// skip this step
				} catch (Exception e) {
					logger.error(e);
				}				
			}
		}
		

		TestNGResultUtils.setErrorCauseSearchedInReferencePicture(testResult, true);
		
		return causes;
	}
	
	private String formatApplicationChangedDescription(List<Label> missingLabels, List<Field> missingFields) {
		StringBuilder description = new StringBuilder();
		if (!missingLabels.isEmpty()) {
			description.append(String.format("%d Label(s) missing: \n", missingLabels.size()));
			for (Label label: missingLabels) {
				description.append(label.getText() + "\n");
			}
		}
		if (!missingFields.isEmpty()) {
			description.append(String.format("%d field(s) missing: \n", missingFields.size()));
			for (Field field: missingFields) {
				description.append(field.toString() + "\n");
			}
		}
		
		return description.toString();
	}
	
	/**
	 * Search a test step before 'testStep' which is matching the current failed step
	 * e.g: we failed on step 3 so we search in 'step 2', then 'step 1' to see if one of them matches our 'step 3' visually
	 * @param testStepManager		the TestStepManager
	 * @param testStep				the TestStep on which we fail
	 * @param stepSnapshotFile		the snapshot taken at the beginning of the step (which will be compared to references
	 * @param errorCauses			the list of error causes
	 */
	private void searchMatchingInPreviousStep(TestStepManager testStepManager, TestStep testStep, File stepSnapshotFile, List<ErrorCause> errorCauses) {
		

		// read the list in reverse order to find the best matching reference
		// #525: copy list so that original step list is not modified
		List<TestStep> testStepsSubList = new ArrayList<>(testStepManager.getTestSteps().subList(0, testStep.getPosition()));
		Collections.reverse(testStepsSubList);
		for (TestStep testStep2: testStepsSubList) {
			if (testStep2.getStepResultId() != null) {
				try {
					File referenceSnapshot = SeleniumRobotSnapshotServerConnector.getInstance().getReferenceSnapshot(testStep2.getStepResultId());
				
					if (referenceSnapshot == null) {
						continue;
					}
					int matching = compareReferenceToStepSnapshot(stepSnapshotFile, referenceSnapshot);
					
					if (matching > 80) {
						errorCauses.add(new ErrorCause(ErrorType.SELENIUM_ERROR, String.format("Wrong page found, we are on the page of step '%s'", testStep2.getName()), testStep));
						return;
					}
				} catch (ConfigurationException | SeleniumRobotServerException e) {
					logger.error(e.getMessage());
				}
			}
		}
		
		errorCauses.add(new ErrorCause(ErrorType.UNKNOWN_PAGE, null, testStep));
	}
	
	/**
	 * Compare the reference snapshot for this step to the version of the current test
	 * @param stepSnapshot
	 * @param referenceSnapshot
	 * @return an integer representing the matching (0: no matching, 100 very good matching) 
	 */
	private int compareReferenceToStepSnapshot(File stepSnapshot, File referenceSnapshot) {
		return compareReferenceToStepSnapshot(stepSnapshot, referenceSnapshot, new ArrayList<>(), new ArrayList<>());
	}
	private int compareReferenceToStepSnapshot(File stepSnapshot, File referenceSnapshot, List<Label> missingLabels, List<Field> missingFields) {
		StepReferenceComparator stepReferenceComparator = new StepReferenceComparator(stepSnapshot, referenceSnapshot);
		int matching = stepReferenceComparator.compare();
		
		missingLabels.addAll(stepReferenceComparator.getMissingLabels());
		missingFields.addAll(stepReferenceComparator.getMissingFields());
		
		return matching;
	}
	
	/**
	 * Search in snapshots of the last step if there are any displayed errors (error messages or fields in error)
	 * @return
	 */
	public List<ErrorCause> findErrorInLastStepSnapshots() {
		
		logger.info("Searching causes: find errors in last snapshot");
		
		List<ErrorCause> causes = new ArrayList<>();
		
		// do not seearch again
		if (TestNGResultUtils.isErrorCauseSearchedInLastStep(testResult)) {
			return causes; 
		}
		

		TestStep lastTestStep = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getLastTestStep();
		TestStep lastFailedStep = lastTestStep;
		for (TestStep testStep: TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getTestSteps()) {
			if (Boolean.TRUE.equals(testStep.getFailed()) && testStep != lastTestStep) {
				lastFailedStep = testStep;
			}
		}
		
		if (lastTestStep != null) {
			for (Snapshot snapshot: lastTestStep.getSnapshots()) {
				try {
					ImageFieldDetector imageFieldDetector = new ImageFieldDetector(new File(snapshot.getScreenshot().getFullImagePath()), 1, FieldType.ERROR_MESSAGES_AND_FIELDS);
					List<Field> fields = imageFieldDetector.detectFields();
					List<Label> labels = imageFieldDetector.detectLabels();
					
					// are some text considered as error messages (mainly in red on page)
					parseFields(causes, fields, labels, lastFailedStep);

					// do some label contain "error" or "problem"
					parseLabels(causes, labels, lastFailedStep);
					
				} catch (Exception e) {
					logger.error("Error searching for errors in last snapshots: " + e.getMessage());
					break;
				}

			}
			TestNGResultUtils.setErrorCauseSearchedInLastStep(testResult, true);
		}
		
		return causes;
	}

	/**
	 * @param causes
	 * @param labels
	 */
	private void parseLabels(List<ErrorCause> causes, List<Label> labels, TestStep testStep) {
		for (Label label: labels) {
			for (String errorWord: ERROR_WORDS) {
				ErrorCause possibleErrorCause = new ErrorCause(ErrorType.ERROR_MESSAGE, label.getText(), testStep);
				if (label.getText().contains(errorWord) && !causes.contains(possibleErrorCause)) {
					causes.add(possibleErrorCause);
				}
			}
		}
	}

	/**
	 * @param causes
	 * @param fields
	 * @param labels
	 */
	private void parseFields(List<ErrorCause> causes, List<Field> fields, List<Label> labels, TestStep testStep) {
		for (Field field: fields) {
			if (CLASS_ERROR_MESSAGE.equals(field.getClassName())) {
				// find the related label
				for (Label label: labels) {
					if (label.isInside(field)) {
						causes.add(new ErrorCause(ErrorType.ERROR_MESSAGE, label.getText(), testStep));
						break;
					}
				}
			} else if (CLASS_ERROR_FIELD.equals(field.getClassName())) {
				causes.add(new ErrorCause(ErrorType.ERROR_IN_FIELD, "At least one field in error", testStep));
			}
		}
	}

}
