package com.seleniumtests.util.imaging;

import java.util.ArrayList;
import java.util.List;

import com.seleniumtests.connectors.selenium.fielddetector.Field;
import com.seleniumtests.connectors.selenium.fielddetector.Label;

/**
 * A class that helps comparing fields and labeld extracted from 2 pictures
 *
 */
public class StepReferenceComparator {

	private List<Field> stepSnapshotFields;
	private List<Label> stepSnapshotLabels;
	private List<Field> referenceSnapshotFields;
	private List<Label> referenceSnapshotLabels;
	private List<Label> missingLabels = new ArrayList<>();
	private List<Field> missingFields = new ArrayList<>();
	
	public StepReferenceComparator(List<Field> stepSnapshotFields, List<Label> stepSnapshotLabels, List<Field> referenceSnapshotFields, List<Label> referenceSnapshotLabels) {
		this.stepSnapshotFields = stepSnapshotFields;
		this.stepSnapshotLabels = stepSnapshotLabels;
		this.referenceSnapshotFields = referenceSnapshotFields;
		this.referenceSnapshotLabels = referenceSnapshotLabels;
	}
	
	/**
	 * Compare each field of stepSnapshot with each field of the referenceSnapshot and compute a ratio
	 * A match is done on presence / position / text of fields between referenceSnapshot and stepSnapshot
	 * @return the ratio (100 means best matching, 0 no matching)
	 */
	public int compare() {
		
		int totalLabels = 0;
		int matchedLabels = 0;
		
		missingLabels = new ArrayList<>(referenceSnapshotLabels);
		missingFields = new ArrayList<>(referenceSnapshotFields);

		for (Label referenceSnapshotLabel: referenceSnapshotLabels) {

			totalLabels++;
			for (Label stepSnapshotLabel: stepSnapshotLabels) {
				if (stepSnapshotLabel.match(referenceSnapshotLabel)) {
					missingLabels.remove(referenceSnapshotLabel);
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
					missingFields.remove(referenceSnapshotField);
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

	public List<Label> getMissingLabels() {
		return missingLabels;
	}

	public List<Field> getMissingFields() {
		return missingFields;
	}
}
