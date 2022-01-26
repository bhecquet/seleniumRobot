package com.seleniumtests.core.testanalysis;

import com.seleniumtests.core.Step.RootCause;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.reporter.logger.TestStep;

public class ErrorCause {
	
	private ErrorType type;
	private String description;
	private TestStep testStep;

	public ErrorCause(ErrorType type, String description, TestStep testStep) {
		if (type == null) {
			throw new ScenarioException("Test type cannot be null");
		}
		
		this.type = type;
		this.description = description;
		this.testStep = testStep;
	}

	public ErrorType getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}
	
	public TestStep getTestStep() {
		return testStep;
	}

	@Override
	public String toString() {
		
		StringBuilder message = new StringBuilder(type.toString());
		if (description != null) {
			message.append(": " + description);
		}
		if (testStep != null) {
			message.append(String.format(" on step '%s'", testStep.getName()));
		}
		if ((type == ErrorType.UNKNOWN_PAGE || type == ErrorType.ERROR_MESSAGE) 
				&& testStep != null 
				&& testStep.getRootCause() != null
				&& testStep.getRootCause() != RootCause.NONE) {
			message.append(String.format("\nDeclared root Cause: %s", testStep.getRootCause()));
			if (testStep.getRootCauseDetails() != null && !testStep.getRootCauseDetails().isEmpty()) {
				message.append(" => " + testStep.getRootCauseDetails());
			}
		}
		return message.toString();
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
		
		boolean sameDescription = (cause.description == null && description == null) || (cause.description != null && cause.description.equals(description));
		boolean sameTestStep = (testStep == null && cause.testStep == null) || (cause.testStep != null && testStep != null && cause.testStep.getName().equals(testStep.getName()));
		
		return sameDescription
				&& cause.type == type 
				&& sameTestStep;
			
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
