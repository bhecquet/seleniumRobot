package com.seleniumtests.core.testanalysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.seleniumtests.connectors.selenium.fielddetector.Field;
import com.seleniumtests.connectors.selenium.fielddetector.ImageFieldDetector;
import com.seleniumtests.connectors.selenium.fielddetector.Label;
import com.seleniumtests.connectors.selenium.fielddetector.ImageFieldDetector.FieldType;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestStep;
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
	 */
	
	private static final String[] ERROR_WORDS = new String[] {"error", "erreur", "problem", "problème"};

	private static final Logger logger = SeleniumRobotLogger.getLogger(ErrorCauseFinder.class);
	
	public enum ErrorType {
		ERROR_MESSAGE,
		ERROR_IN_FIELD
	}
	
	public class ErrorCause {
		
		private ErrorType type;
		private String description;
	
		public ErrorCause(ErrorType type, String description) {
			this.type = type;
			this.description = description;
		}

		public ErrorType getType() {
			return type;
		}

		public String getDescription() {
			return description;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}

			if (obj instanceof ErrorCause && this == obj) {
				return true;
			}
			
			ErrorCause cause = (ErrorCause)obj;
			if (cause.description != null && cause.description.equals(description) && cause.type == type) {
				return true;
			} else {
				return false;
			}
		}
		
		@Override
		public int hashCode() {
			if (description != null) {
				return description.hashCode();
			} else {
				return 0;
			}
			
		}
		
		
	}
	
	public void findErrorCause() {
		findErrorInLastStepSnapshots();
	}
	
	private List<ErrorCause> findErrorInLastStepSnapshots() {
		
		List<ErrorCause> causes = new ArrayList<>();
		
		TestStep lastTestStep = TestStepManager.getInstance().getLastTestStep();
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
				
				/*
				 *  les "error_field" vont nous dire qu'il y a X champs en erreur, on ne pourra pas nécessairement faire plus
				 *  Cela dit juste que dans le formulaire, il y a des erreurs de saisie (champs manquants ou valeurs incorrectes)
				 *  les "error_message" doivent être mis en relation avec les labels qui sont trouvés pour disposer du texte 
				 */
			}
		}
		
		return causes;
	}

}
