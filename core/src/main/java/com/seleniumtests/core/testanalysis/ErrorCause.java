package com.seleniumtests.core.testanalysis;

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
		return cause.description != null && cause.description.equals(description) && cause.type == type;
			
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
