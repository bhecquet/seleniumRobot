package com.seleniumtests.ut.util.imaging;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.selenium.fielddetector.Field;
import com.seleniumtests.connectors.selenium.fielddetector.Label;
import com.seleniumtests.util.imaging.StepReferenceComparator;

public class TestStepReferenceComparator extends GenericTest {


	/**
	 * List of fields are the same, comparison is successful (no labels)
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareNoLabel() throws Exception {
		
		List<Field> stepFields = new ArrayList<>();
		stepFields.add(new Field(0, 100, 0, 20, "", "field"));
		stepFields.add(new Field(0, 100, 100, 120, "", "field"));
		
		List<Field> referenceFields = new ArrayList<>();
		referenceFields.add(new Field(0, 100, 0, 20, "", "field"));
		referenceFields.add(new Field(5, 105, 100, 120, "", "field"));

		StepReferenceComparator comparator = new StepReferenceComparator(stepFields, new ArrayList<>(), referenceFields, new ArrayList<>());
		int matching = comparator.compare();
		
		Assert.assertEquals(matching, 100);
		
	}
	
	@Test(groups= {"ut"})
	public void testCompareNoField() throws Exception {
		
		List<Label> stepLabels = new ArrayList<>();
		stepLabels.add(new Label(0, 100, 0, 20, "a text"));
		stepLabels.add(new Label(0, 100, 100, 120, "other text"));
		
		List<Label> referenceLabels = new ArrayList<>();
		referenceLabels.add(new Label(0, 100, 0, 20, "a text"));
		referenceLabels.add(new Label(5, 105, 100, 120, "other text"));

		StepReferenceComparator comparator = new StepReferenceComparator(new ArrayList<>(), stepLabels, new ArrayList<>(), referenceLabels);
		
		Assert.assertEquals(comparator.compare(), 100);
	}
	
	@Test(groups= {"ut"})
	public void testComparePartialMatch() throws Exception {
		
		List<Label> stepLabels = new ArrayList<>();
		stepLabels.add(new Label(0, 100, 20, 50, "a text"));
		stepLabels.add(new Label(0, 100, 100, 120, "other text"));
		
		List<Label> referenceLabels = new ArrayList<>();
		referenceLabels.add(new Label(0, 100, 0, 20, "a text"));
		referenceLabels.add(new Label(5, 105, 100, 120, "other text"));
		
		List<Field> stepFields = new ArrayList<>();
		stepFields.add(new Field(0, 100, 0, 20, "", "field"));
		stepFields.add(new Field(0, 100, 200, 220, "", "field"));
		
		List<Field> referenceFields = new ArrayList<>();
		referenceFields.add(new Field(0, 100, 0, 20, "", "field"));
		referenceFields.add(new Field(5, 105, 100, 120, "", "field"));

		StepReferenceComparator comparator = new StepReferenceComparator(stepFields, stepLabels, referenceFields, referenceLabels);
		
		Assert.assertEquals(comparator.compare(), 50);
	}
	
	@Test(groups= {"ut"})
	public void testCompareNoFieldNoLabel() throws Exception {
		
		StepReferenceComparator comparator = new StepReferenceComparator(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
		
		Assert.assertEquals(comparator.compare(), 100);
	}
	
}
