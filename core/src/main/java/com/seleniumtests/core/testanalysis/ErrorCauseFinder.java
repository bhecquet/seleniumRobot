package com.seleniumtests.core.testanalysis;

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
	
	// TODO: link to RootCause and RootCause details from the Step annotation which can indicate what is the root cause of this error (declarative for a step)
	
	/*
	 *  TESTS:
	 *  - si le ImageFieldDetector ne s'initialise pas, il ne faut pas planter
	 *  - on n'a pas le stepResultId => on doit renvoyer null
	 */
	
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
		
		return causes;
	}
	
	/**
	 * Compare the failed step with it's reference that can be found on seleniumRobot server
	 * If reference cannot be found, skip this step
	 * @return
	 */
	public List<ErrorCause> compareStepInErrorWithReference() {
		List<ErrorCause> causes = new ArrayList<>();
		
		// do not seearch again
		if (TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult)) {
			return causes; 
		}
		
		// don't analyze if result has not been recorded on seleniumRobot server
		TestStepManager testStepManager = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager();
		if (testStepManager.getLastTestStep().getStepResultId() == null) {
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
					int matching = compareReferenceToStepSnapshot(stepSnapshotFile, referenceSnapshot);
					
					// bad matching: the reference does not match at all the current step, we will check with other reference steps
					if (matching < 50) {
						searchMatchingInPreviousStep(testStepManager, testStep, stepSnapshotFile, causes);
						
					// middle matching: we may be on the right web page but the page has changed (some fields appeared or disappeared)
					// or the text changed slightly. This could mean application changed
					} else if (matching < 90) {
						causes.add(new ErrorCause(ErrorType.APPLICATION_CHANGED, null));
					}
					
					// else, very good matching: we are on the same web page, error does not come from there
					
					
					break;
					
				} catch (IndexOutOfBoundsException e) {
					// skip this step
				}
				

				
				
				// à partir de la référence de ce step, on va comparer le nombre de champs avec l'image prise au début de l'étape du test qui nous intéresse
				// - nombre et position des champs
				// - ressemblance et position du texte / des labels
				// Si la correspondance n'est pas bonne (la plupart des champs de la référence ne sont pas présents), on va chercher par rapport à l'une des étapes précedente
				// 		On va considérer qu'il y a eu une erreur dans les clics, qui font qu'on n'est pas sur la bonne page au démarrage de l'étape
				// Si la correspondance est moyenne (X % des champs présents et texte similaire), on considère qu'il y a eu évolution de l'application
				// Si la correspondance est bonne, il n'y a rien de plus à faire de ce côté
				
			}
		}
		

		TestNGResultUtils.setErrorCauseSearchedInReferencePicture(testResult, true);
		
		return causes;
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
		List<TestStep> testStepsSubList = testStepManager.getTestSteps().subList(0, testStep.getPosition());
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
						errorCauses.add(new ErrorCause(ErrorType.SELENIUM_ERROR, String.format("Wrong page found, we are on the page of step '%s'", testStep2.getName())));
						return;
					}
				} catch (ConfigurationException | SeleniumRobotServerException e) {
					logger.error(e.getMessage());
				}
			}
		}
		
		errorCauses.add(new ErrorCause(ErrorType.UNKNOWN_PAGE, null));
	}
	
	/**
	 * Compare the reference snapshot for this step to the version of the current test
	 * @param stepSnapshot
	 * @param referenceSnapshot
	 * @return an integer representing the matching (0: no matching, 100 very good matching) 
	 */
	private int compareReferenceToStepSnapshot(File stepSnapshot, File referenceSnapshot) {
		return new StepReferenceComparator(stepSnapshot, referenceSnapshot).compare();
	}
	
	/**
	 * Search in snapshots of the last step if there are any displayed errors (error messages or fields in error)
	 * @return
	 */
	public List<ErrorCause> findErrorInLastStepSnapshots() {
		
		List<ErrorCause> causes = new ArrayList<>();
		
		// do not seearch again
		if (TestNGResultUtils.isErrorCauseSearchedInLastStep(testResult)) {
			return causes; 
		}
		
		TestStep lastTestStep = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getLastTestStep();
		
		if (lastTestStep != null) {
			for (Snapshot snapshot: lastTestStep.getSnapshots()) {
				try {
					ImageFieldDetector imageFieldDetector = new ImageFieldDetector(new File(snapshot.getScreenshot().getFullImagePath()), 1, FieldType.ERROR_MESSAGES_AND_FIELDS);
					List<Field> fields = imageFieldDetector.detectFields();
					List<Label> labels = imageFieldDetector.detectLabels();
					
					// are some text considered as error messages (mainly in red on page)
					for (Field field: fields) {
						if ("error_message".equals(field.getClassName())) {
							// find the related label
							for (Label label: labels) {
								if (label.isInside(field)) {
									causes.add(new ErrorCause(ErrorType.ERROR_MESSAGE, label.getText()));
								}
							}
						} else if ("error_field".equals(field.getClassName())) {
							causes.add(new ErrorCause(ErrorType.ERROR_IN_FIELD, "At least one field in error"));
						}
					}

					// do some label contain "error" or "problem"
					for (Label label: labels) {
						for (String errorWord: ERROR_WORDS) {
							ErrorCause possibleErrorCause = new ErrorCause(ErrorType.ERROR_MESSAGE, label.getText());
							if (label.getText().contains(errorWord) && !causes.contains(possibleErrorCause)) {
								causes.add(possibleErrorCause);
							}
						}
					}
					
				} catch (Exception e) {
					logger.error("Error searching for errors in last snapshots: " + e.getMessage());
					break;
				}

			}
			TestNGResultUtils.setErrorCauseSearchedInLastStep(testResult, true);
		}
		
		return causes;
	}

}
