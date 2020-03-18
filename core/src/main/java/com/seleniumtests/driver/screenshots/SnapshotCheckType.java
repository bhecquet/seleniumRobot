package com.seleniumtests.driver.screenshots;

/**
 * Type of check we should do on snapshot
 * @author s047432
 *
 */
public class SnapshotCheckType {
	
	private boolean sendToServer;
	
	// snapshot will be compared to baseline if requested by option seleniumRobotServerCompareSnapshots
	public static final SnapshotCheckType TRUE = new SnapshotCheckType(true);
	
	// snapshot will not be compared
	public static final SnapshotCheckType FALSE = new SnapshotCheckType(false);
			
	private SnapshotCheckType(boolean sendToServer) {
		this.sendToServer = sendToServer;
	}
		
	// TODO: check without colors, check only part of the picture (exclusion zones defined directly in test)
}
