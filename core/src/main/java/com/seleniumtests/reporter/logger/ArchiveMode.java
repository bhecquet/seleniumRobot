package com.seleniumtests.reporter.logger;

public enum ArchiveMode {
	TRUE ("true"),
    FALSE ("false"),
    ON_SUCCESS ("onSuccess"),
    ON_ERROR ("onError");
	
	String[] mode;
	
	ArchiveMode(final String... archiveMode) {
        this.mode = archiveMode;
    }
	
	public static ArchiveMode fromString(String mode) {
		try {
			return ArchiveMode.valueOf(mode);
		} catch (IllegalArgumentException ex) {
			for (ArchiveMode archiveMode : ArchiveMode.values()) {
		        for (String matcher : archiveMode.mode) {
		          if (mode.equalsIgnoreCase(matcher)) {
		            return archiveMode;
		          }
		        }
		      }
		      throw new IllegalArgumentException("Unrecognized archive mode: " + mode);
		}
	}	
}
