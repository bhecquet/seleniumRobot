package com.seleniumtests.ut.util.imaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.fielddetector.Field;
import com.seleniumtests.connectors.selenium.fielddetector.ImageFieldDetector;
import com.seleniumtests.connectors.selenium.fielddetector.ImageFieldDetector.FieldType;
import com.seleniumtests.connectors.selenium.fielddetector.Label;
import com.seleniumtests.util.imaging.StepReferenceComparator;

@PrepareForTest({ImageFieldDetector.class, StepReferenceComparator.class})
public class TestStepReferenceComparator extends MockitoTest {

	
	@Mock
	ImageFieldDetector stepImageFieldDetector;
	
	@Mock
	ImageFieldDetector refImageFieldDetector;
	
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
		
		PowerMockito.whenNew(ImageFieldDetector.class).withAnyArguments().thenReturn(stepImageFieldDetector, refImageFieldDetector);
		when(stepImageFieldDetector.detectFields()).thenReturn(stepFields);
		when(refImageFieldDetector.detectFields()).thenReturn(referenceFields);
		
		StepReferenceComparator comparator = new StepReferenceComparator(File.createTempFile("img", ".png"), File.createTempFile("img", ".png"));
		int matching = comparator.compare();
		
		Assert.assertEquals(matching, 100);
		PowerMockito.verifyNew(ImageFieldDetector.class, times(2)).withArguments(any(File.class), anyDouble(), eq(FieldType.ALL_FORM_FIELDS));
		
	}
	
	@Test(groups= {"ut"})
	public void testCompareNoField() throws Exception {
		
		List<Label> stepLabels = new ArrayList<>();
		stepLabels.add(new Label(0, 100, 0, 20, "a text"));
		stepLabels.add(new Label(0, 100, 100, 120, "other text"));
		
		List<Label> referenceLabels = new ArrayList<>();
		referenceLabels.add(new Label(0, 100, 0, 20, "a text"));
		referenceLabels.add(new Label(5, 105, 100, 120, "other text"));
		
		PowerMockito.whenNew(ImageFieldDetector.class).withAnyArguments().thenReturn(stepImageFieldDetector, refImageFieldDetector);
		when(stepImageFieldDetector.detectLabels()).thenReturn(stepLabels);
		when(refImageFieldDetector.detectLabels()).thenReturn(referenceLabels);
		
		StepReferenceComparator comparator = new StepReferenceComparator(File.createTempFile("img", ".png"), File.createTempFile("img", ".png"));
		
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
		
		PowerMockito.whenNew(ImageFieldDetector.class).withAnyArguments().thenReturn(stepImageFieldDetector, refImageFieldDetector);
		when(stepImageFieldDetector.detectLabels()).thenReturn(stepLabels);
		when(refImageFieldDetector.detectLabels()).thenReturn(referenceLabels);
		when(stepImageFieldDetector.detectFields()).thenReturn(stepFields);
		when(refImageFieldDetector.detectFields()).thenReturn(referenceFields);
		
		StepReferenceComparator comparator = new StepReferenceComparator(File.createTempFile("img", ".png"), File.createTempFile("img", ".png"));
		
		Assert.assertEquals(comparator.compare(), 50);
	}
	
	@Test(groups= {"ut"})
	public void testCompareNoFieldNoLabel() throws Exception {
		
		PowerMockito.whenNew(ImageFieldDetector.class).withAnyArguments().thenReturn(stepImageFieldDetector, refImageFieldDetector);

		StepReferenceComparator comparator = new StepReferenceComparator(File.createTempFile("img", ".png"), File.createTempFile("img", ".png"));
		
		Assert.assertEquals(comparator.compare(), 100);
	}
	
}
