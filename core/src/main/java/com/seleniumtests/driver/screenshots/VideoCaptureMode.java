package com.seleniumtests.driver.screenshots;

public enum VideoCaptureMode {
	TRUE ("true"),
    FALSE ("false"),
    ON_SUCCESS ("onSuccess"),
    ON_ERROR ("onError");
	
	String[] captureMode;
	
	VideoCaptureMode(final String... captureMode) {
        this.captureMode = captureMode;
    }
	
	public static VideoCaptureMode fromString(String mode) {
		try {
			return VideoCaptureMode.valueOf(mode);
		} catch (IllegalArgumentException ex) {
			for (VideoCaptureMode capMode : VideoCaptureMode.values()) {
		        for (String matcher : capMode.captureMode) {
		          if (mode.equalsIgnoreCase(matcher)) {
		            return capMode;
		          }
		        }
		      }
		      throw new IllegalArgumentException("Unrecognized video capture mode: " + mode);
		}
	}	
}
