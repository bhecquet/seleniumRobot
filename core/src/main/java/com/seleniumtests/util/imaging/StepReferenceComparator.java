package com.seleniumtests.util.imaging;

import java.io.File;
import java.util.List;

import com.seleniumtests.connectors.selenium.fielddetector.Field;
import com.seleniumtests.connectors.selenium.fielddetector.ImageFieldDetector;
import com.seleniumtests.connectors.selenium.fielddetector.Label;
import com.seleniumtests.connectors.selenium.fielddetector.ImageFieldDetector.FieldType;

/**
 * A class that helps comparing 2 pictures that may be reference for a test step
 * @author S047432
 *
 */
public class StepReferenceComparator {

	private File stepSnapshot;
	private File referenceSnapshot;
	
	public StepReferenceComparator(File stepSnapshot, File referenceSnapshot) {
		this.stepSnapshot = stepSnapshot;
		this.referenceSnapshot = referenceSnapshot;
	}
	
	/**
	 * Compare each field of stepSnapshot with each field of the referenceSnapshot and compute a ratio
	 * A match is done on presence / position / text of fields between referenceSnapshot and stepSnapshot
	 * @return the ratio (100 means best matching, 0 no matching)
	 */
	public int compare() {

		ImageFieldDetector stepSnapshotImageFieldDetector = new ImageFieldDetector(stepSnapshot, 1, FieldType.ALL_FORM_FIELDS);
		List<Field> stepSnapshotFields = stepSnapshotImageFieldDetector.detectFields();
		List<Label> stepSnapshotLabels = stepSnapshotImageFieldDetector.detectLabels();
		
		ImageFieldDetector referenceSnapshotImageFieldDetector = new ImageFieldDetector(referenceSnapshot, 1, FieldType.ALL_FORM_FIELDS);
		List<Field> referenceSnapshotFields = referenceSnapshotImageFieldDetector.detectFields();
		List<Label> referenceSnapshotLabels = referenceSnapshotImageFieldDetector.detectLabels();
		
		int totalLabels = 0;
		int matchedLabels = 0;

		for (Label referenceSnapshotLabel: referenceSnapshotLabels) {

			totalLabels++;
			for (Label stepSnapshotLabel: stepSnapshotLabels) {
				if (stepSnapshotLabel.match(referenceSnapshotLabel)) {
					matchedLabels++;
					break;
				}
			}
		}
		
		int totalFields = 0;
		int matchedFields = 0;
		
		for (Field referenceSnapshotField: referenceSnapshotFields) {

			totalFields++;
			for (Field stepSnapshotField: stepSnapshotFields) {
				if (stepSnapshotField.match(referenceSnapshotField)) {
					matchedFields++;
					break;
				}
			}
		}
		
		if (totalFields + totalLabels == 0) {
			return 100;
		}
		
		return 100 * (matchedFields + matchedLabels) / (totalFields + totalLabels);
	}
}
