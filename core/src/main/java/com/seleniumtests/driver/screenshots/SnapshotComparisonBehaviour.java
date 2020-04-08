package com.seleniumtests.driver.screenshots;

public enum SnapshotComparisonBehaviour {

	DISPLAY_ONLY("displayOnly"), // test result is not changed, an indicator shows the status of snapshot
									// comparison in HTML result (summary and detailed file)
	ADD_TEST_RESULT("addTestResult"), // test result is not changed. An additionnal test result is created for each
										// test, but giving only the snapshotComparison result
	CHANGE_TEST_RESULT("changeTestResult"); // test result is changed according to snapshot comparison. If test is
											// failed, it remains failed whatever the comparison is. If the test is
											// sucessful and snapshot comparison if failed, test is failed

	String comparisonBehaviour;

	SnapshotComparisonBehaviour(String comparisonBehaviour) {
		this.comparisonBehaviour = comparisonBehaviour;
	}

	public static SnapshotComparisonBehaviour fromString(String mode) {
		try {
			return SnapshotComparisonBehaviour.valueOf(mode);
		} catch (IllegalArgumentException ex) {
			for (SnapshotComparisonBehaviour compMode : SnapshotComparisonBehaviour.values()) {
				if (compMode.comparisonBehaviour.equalsIgnoreCase(mode)) {
					return compMode;
				}
			}
			throw new IllegalArgumentException("Unrecognized Snapshot comparison behaviour: " + mode);
		}
	}
}
