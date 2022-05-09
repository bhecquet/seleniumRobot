package com.seleniumtests.ut.core.testanalysis;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.connectors.selenium.fielddetector.Field;
import com.seleniumtests.connectors.selenium.fielddetector.ImageFieldDetector;
import com.seleniumtests.connectors.selenium.fielddetector.ImageFieldDetector.FieldType;
import com.seleniumtests.connectors.selenium.fielddetector.Label;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.core.testanalysis.ErrorCause;
import com.seleniumtests.core.testanalysis.ErrorCauseFinder;
import com.seleniumtests.core.testanalysis.ErrorType;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.imaging.StepReferenceComparator;

@PrepareForTest({ErrorCauseFinder.class, ImageFieldDetector.class, SeleniumRobotSnapshotServerConnector.class})
public class TestErrorCauseFinder extends MockitoTest {

	@Mock
	ImageFieldDetector imageFieldDetector;
	
	@Mock
	SeleniumRobotSnapshotServerConnector serverConnector;
	
	@Mock
	StepReferenceComparator stepReferenceComparatorStep2;
	
	@Mock
	StepReferenceComparator stepReferenceComparatorStep1;
	
	private TestStep step1;
	private TestStep stepFailed;
	private TestStep lastStep;
	
	private File imgStep1Ref;
	private File imgStep1;
	private File imgStep2Ref;
	private File imgLastStep;
	private File referenceImgStep1;
	private File referenceImgStep2;
	
	@BeforeMethod(alwaysRun = true)
	public void init() throws Exception {
		step1 = new TestStep("step 1", Reporter.getCurrentTestResult(), new ArrayList<>(), false);
		step1.setStepResultId(0);
		step1.setPosition(0);
		
		File image = GenericTest.createFileFromResource("tu/images/driverTestPage.png");
		
		
		imgStep1Ref = File.createTempFile("img", ".png");
		imgStep1 = File.createTempFile("img", ".png");
		File tmpHtml = File.createTempFile("html", ".html");
		
		ScreenShot screenshot = new ScreenShot();
		screenshot.setImagePath("screenshot/" + imgStep1Ref.getName());
		screenshot.setHtmlSourcePath("htmls/" + tmpHtml.getName());
		FileUtils.copyFile(imgStep1Ref, new File(screenshot.getFullImagePath()));
		FileUtils.copyFile(tmpHtml, new File(screenshot.getFullHtmlPath()));
		step1.addSnapshot(new Snapshot(screenshot, null, SnapshotCheckType.REFERENCE_ONLY), 1, null);
		
		ScreenShot screenshot2 = new ScreenShot();
		screenshot2.setImagePath("screenshot/" + imgStep1.getName());
		screenshot2.setHtmlSourcePath("htmls/" + tmpHtml.getName());
		FileUtils.copyFile(imgStep1, new File(screenshot.getFullImagePath()));
		FileUtils.copyFile(tmpHtml, new File(screenshot.getFullHtmlPath()));
		step1.addSnapshot(new Snapshot(screenshot2, null, SnapshotCheckType.FALSE), 1, null);
		
		stepFailed = new TestStep("step 2", Reporter.getCurrentTestResult(), new ArrayList<>(), false);
		stepFailed.setStepResultId(1);
		stepFailed.setFailed(true);
		stepFailed.setPosition(1);
		
		imgStep2Ref = File.createTempFile("img", ".png");
		
		ScreenShot screenshot3 = new ScreenShot();
		screenshot3.setImagePath("screenshot/" + imgStep2Ref.getName());
		screenshot3.setHtmlSourcePath("htmls/" + tmpHtml.getName());
		FileUtils.copyFile(imgStep2Ref, new File(screenshot3.getFullImagePath()));
		FileUtils.copyFile(tmpHtml, new File(screenshot3.getFullHtmlPath()));
		stepFailed.addSnapshot(new Snapshot(screenshot3, null, SnapshotCheckType.REFERENCE_ONLY), 1, null);
		
		lastStep = new TestStep(TestStepManager.LAST_STEP_NAME, Reporter.getCurrentTestResult(), new ArrayList<>(), false);
		lastStep.setPosition(2);
		
		imgLastStep = File.createTempFile("img", ".png");
		File tmpHtml2 = File.createTempFile("html", ".html");
		
		ScreenShot screenshot4 = new ScreenShot();
		screenshot4.setImagePath("screenshot/" + imgStep1.getName());
		screenshot4.setHtmlSourcePath("htmls/" + tmpHtml.getName());
		FileUtils.copyFile(imgLastStep, new File(screenshot4.getFullImagePath()));
		FileUtils.copyFile(tmpHtml2, new File(screenshot4.getFullHtmlPath()));
		lastStep.addSnapshot(new Snapshot(screenshot4, "main", SnapshotCheckType.FALSE), 1, null);
		lastStep.setStepResultId(10);

		PowerMockito.mockStatic(SeleniumRobotSnapshotServerConnector.class);
		PowerMockito.when(SeleniumRobotSnapshotServerConnector.getInstance()).thenReturn(serverConnector);
		
		referenceImgStep1 = File.createTempFile("img", ".png"); // reference image for step 1
		FileUtils.copyFile(image, referenceImgStep1);
		referenceImgStep2 = File.createTempFile("img", ".png"); // reference image for step 2
		FileUtils.copyFile(image, referenceImgStep2);
		when(serverConnector.getReferenceSnapshot(0)).thenReturn(referenceImgStep1);
		when(serverConnector.getReferenceSnapshot(1)).thenReturn(referenceImgStep2);
		
	}
	
	/**
	 * Check error causes are found with various error messages
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testSearchInLastStepErrorMessage() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, lastStep));
		
		PowerMockito.whenNew(ImageFieldDetector.class).withArguments(new File(lastStep.getSnapshots().get(0).getScreenshot().getFullImagePath()), (double)1, FieldType.ERROR_MESSAGES_AND_FIELDS).thenReturn(imageFieldDetector);
		
		List<Label> labels = new ArrayList<>();
		labels.add(new Label(0, 100, 20, 50, "il y a eu un gros problème"));
		labels.add(new Label(0, 100, 100, 120, "some error"));
		List<Field> fields = new ArrayList<>();
		fields.add(new Field(0, 100, 0, 20, "", "field"));
		fields.add(new Field(0, 100, 200, 220, "", "field"));
		
		when(imageFieldDetector.detectFields()).thenReturn(fields);
		when(imageFieldDetector.detectLabels()).thenReturn(labels);
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).findErrorInLastStepSnapshots();
		
		Assert.assertEquals(causes.size(), 2);
		Assert.assertEquals(causes.get(0).getType(), ErrorType.ERROR_MESSAGE);
		Assert.assertEquals(causes.get(0).getTestStep(), lastStep); // no failed step except "last step", keep association
		Assert.assertEquals(causes.get(0).getDescription(), "il y a eu un gros problème");
		Assert.assertEquals(causes.get(1).getType(), ErrorType.ERROR_MESSAGE);
		Assert.assertEquals(causes.get(1).getDescription(), "some error");
		Assert.assertEquals(causes.get(1).getTestStep(), lastStep);
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInLastStep(testResult));
	}
	
	/**
	 * When a failed step is present, attach ErrorCause to this one instead of "Last Step"
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testSearchInLastStepErrorMessageWithFailedStep() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		
		PowerMockito.whenNew(ImageFieldDetector.class).withArguments(new File(lastStep.getSnapshots().get(0).getScreenshot().getFullImagePath()), (double)1, FieldType.ERROR_MESSAGES_AND_FIELDS).thenReturn(imageFieldDetector);
		
		List<Label> labels = new ArrayList<>();
		labels.add(new Label(0, 100, 20, 50, "il y a eu un gros problème"));
		labels.add(new Label(0, 100, 100, 120, "some error"));
		List<Field> fields = new ArrayList<>();
		fields.add(new Field(0, 100, 0, 20, "", "field"));
		fields.add(new Field(0, 100, 200, 220, "", "field"));
		
		when(imageFieldDetector.detectFields()).thenReturn(fields);
		when(imageFieldDetector.detectLabels()).thenReturn(labels);
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).findErrorInLastStepSnapshots();
		
		Assert.assertEquals(causes.size(), 2);
		Assert.assertEquals(causes.get(0).getType(), ErrorType.ERROR_MESSAGE);
		Assert.assertEquals(causes.get(0).getDescription(), "il y a eu un gros problème");
		Assert.assertEquals(causes.get(0).getTestStep(), stepFailed); // error cause is associated to the last failed step
		Assert.assertEquals(causes.get(1).getType(), ErrorType.ERROR_MESSAGE);
		Assert.assertEquals(causes.get(1).getDescription(), "some error");
		Assert.assertEquals(causes.get(1).getTestStep(), stepFailed); // error cause is associated to the last failed step
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInLastStep(testResult));
	}
	
	/**
	 * Check error is only logged when something goes wrong while analyzing picture
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testSearchInLastStepErrorMessageWithException() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, lastStep));
		
		PowerMockito.whenNew(ImageFieldDetector.class).withArguments(new File(lastStep.getSnapshots().get(0).getScreenshot().getFullImagePath()), (double)1, FieldType.ERROR_MESSAGES_AND_FIELDS).thenReturn(imageFieldDetector);
		
		List<Label> labels = new ArrayList<>();
		labels.add(new Label(0, 100, 20, 50, "il y a eu un gros problème"));
		labels.add(new Label(0, 100, 100, 120, "some error"));
		List<Field> fields = new ArrayList<>();
		fields.add(new Field(0, 100, 0, 20, "", "field"));
		fields.add(new Field(0, 100, 200, 220, "", "field"));
		
		when(imageFieldDetector.detectFields()).thenThrow(new ConfigurationException("error"));
		when(imageFieldDetector.detectLabels()).thenReturn(labels);
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).findErrorInLastStepSnapshots();
		
		Assert.assertEquals(causes.size(), 0);
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInLastStep(testResult));
	}
	
	/**
	 * In case an error_field is found, check we show it as an error
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testSearchInLastStepDetectedErrorField() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, lastStep));
		
		PowerMockito.whenNew(ImageFieldDetector.class).withArguments(new File(lastStep.getSnapshots().get(0).getScreenshot().getFullImagePath()), (double)1, FieldType.ERROR_MESSAGES_AND_FIELDS).thenReturn(imageFieldDetector);
		
		List<Label> labels = new ArrayList<>();
		labels.add(new Label(0, 100, 20, 50, "ok"));
		labels.add(new Label(0, 100, 100, 120, "nothing"));
		List<Field> fields = new ArrayList<>();
		fields.add(new Field(0, 100, 0, 20, "", ErrorCauseFinder.CLASS_ERROR_FIELD));
		fields.add(new Field(0, 100, 200, 220, "", "field"));
		
		when(imageFieldDetector.detectFields()).thenReturn(fields);
		when(imageFieldDetector.detectLabels()).thenReturn(labels);
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).findErrorInLastStepSnapshots();
		
		Assert.assertEquals(causes.size(), 1);
		Assert.assertEquals(causes.get(0).getType(), ErrorType.ERROR_IN_FIELD);
		Assert.assertEquals(causes.get(0).getDescription(), "At least one field in error");
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInLastStep(testResult));
	}
	
	/**
	 * An error_message field is found. We try to relate it to a label 
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testSearchInLastStepDetectedErrorMessage() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, lastStep));
		
		PowerMockito.whenNew(ImageFieldDetector.class).withArguments(new File(lastStep.getSnapshots().get(0).getScreenshot().getFullImagePath()), (double)1, FieldType.ERROR_MESSAGES_AND_FIELDS).thenReturn(imageFieldDetector);
		
		List<Label> labels = new ArrayList<>();
		labels.add(new Label(0, 100, 0, 20, "ko"));
		labels.add(new Label(0, 100, 100, 120, "nothing"));
		List<Field> fields = new ArrayList<>();
		fields.add(new Field(0, 100, 0, 20, "", ErrorCauseFinder.CLASS_ERROR_MESSAGE));
		fields.add(new Field(0, 100, 200, 220, "", "field"));
		
		when(imageFieldDetector.detectFields()).thenReturn(fields);
		when(imageFieldDetector.detectLabels()).thenReturn(labels);
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).findErrorInLastStepSnapshots();
		
		Assert.assertEquals(causes.size(), 1);
		Assert.assertEquals(causes.get(0).getType(), ErrorType.ERROR_MESSAGE);
		Assert.assertEquals(causes.get(0).getDescription(), "ko");
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInLastStep(testResult));
	}
	
	/**
	 * no error message in the page, no cause should be found
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testSearchInLastStepNoErrors() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, lastStep));
		
		PowerMockito.whenNew(ImageFieldDetector.class).withArguments(new File(lastStep.getSnapshots().get(0).getScreenshot().getFullImagePath()), (double)1, FieldType.ERROR_MESSAGES_AND_FIELDS).thenReturn(imageFieldDetector);
		
		List<Label> labels = new ArrayList<>();
		labels.add(new Label(0, 100, 20, 50, "tout roule"));
		labels.add(new Label(0, 100, 100, 120, "everything is fine"));
		List<Field> fields = new ArrayList<>();
		fields.add(new Field(0, 100, 0, 20, "", "field"));
		fields.add(new Field(0, 100, 200, 220, "", "field"));
		
		when(imageFieldDetector.detectFields()).thenReturn(fields);
		when(imageFieldDetector.detectLabels()).thenReturn(labels);

		List<ErrorCause> causes = new ErrorCauseFinder(testResult).findErrorInLastStepSnapshots();
		
		Assert.assertEquals(causes.size(), 0);
	}
	
	@Test(groups= {"ut"})
	public void testSearchInLastStepNoLastStep() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1));
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).findErrorInLastStepSnapshots();
		
		Assert.assertEquals(causes.size(), 0);
	}
	
	@Test(groups= {"ut"})
	public void testSearchInLastStepAlreadyDone() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, lastStep));
		
		PowerMockito.whenNew(ImageFieldDetector.class).withArguments(new File(lastStep.getSnapshots().get(0).getScreenshot().getFullImagePath()), (double)1, FieldType.ERROR_MESSAGES_AND_FIELDS).thenReturn(imageFieldDetector);
		
		List<Label> labels = new ArrayList<>();
		labels.add(new Label(0, 100, 20, 50, "il y a eu un gros problème"));
		labels.add(new Label(0, 100, 100, 120, "some error"));
		List<Field> fields = new ArrayList<>();
		fields.add(new Field(0, 100, 0, 20, "", "field"));
		fields.add(new Field(0, 100, 200, 220, "", "field"));
		
		when(imageFieldDetector.detectFields()).thenReturn(fields);
		when(imageFieldDetector.detectLabels()).thenReturn(labels);
		
		TestNGResultUtils.setErrorCauseSearchedInLastStep(testResult, true);
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).findErrorInLastStepSnapshots();
		
		Assert.assertEquals(causes.size(), 0);
	}

	// bad match => pas de step avant celle qui plante
	
	/**
	 * Matching between reference picture stored on server and the one for current test is very good (we are on the right page)
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceGoodMatch() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		
		// comparison successful
		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(new File(stepFailed.getSnapshots().get(0).getScreenshot().getFullImagePath()), referenceImgStep2).thenReturn(stepReferenceComparatorStep2);
		when(stepReferenceComparatorStep2.compare()).thenReturn(90);
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference();
		
		Assert.assertEquals(causes.size(), 0);
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
	
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceSearchAlreadyDone() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		
		TestNGResultUtils.setErrorCauseSearchedInReferencePicture(testResult, true);
		
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		
		// comparison successful
		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(new File(stepFailed.getSnapshots().get(0).getScreenshot().getFullImagePath()), referenceImgStep2).thenReturn(stepReferenceComparatorStep2);
		when(stepReferenceComparatorStep2.compare()).thenReturn(90);
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference();
		
		// no comparison done
		PowerMockito.verifyNew(StepReferenceComparator.class, never()).withArguments(any(File.class), any(File.class));
				
		
		Assert.assertEquals(causes.size(), 0);
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
	
	/**
	 * Matching between reference picture stored on server and the one for current test is good enough (we are on the right page but something changed)
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceMediumMatch() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		
		// comparison successful
		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(new File(stepFailed.getSnapshots().get(0).getScreenshot().getFullImagePath()), referenceImgStep2).thenReturn(stepReferenceComparatorStep2);
		when(stepReferenceComparatorStep2.compare()).thenReturn(50);
		when(stepReferenceComparatorStep2.getMissingFields()).thenReturn(Arrays.asList(new Field(0, 100, 0, 20, "", "field")));
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference();
		
		Assert.assertEquals(causes.size(), 1);
		Assert.assertEquals(causes.get(0).getType(), ErrorType.APPLICATION_CHANGED);
		Assert.assertEquals(causes.get(0).getDescription(), "1 field(s) missing: \n"
				+ "field[text=]: java.awt.Rectangle[x=0,y=0,width=100,height=20]\n");
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
		
		// check rectangle has been drawn around the missing field
		BufferedImage image = ImageIO.read(referenceImgStep2);
		Assert.assertEquals(new Color(image.getRGB(0, 20)), Color.RED);
		Assert.assertEquals(new Color(image.getRGB(0, 0)), Color.RED);
		Assert.assertEquals(new Color(image.getRGB(100, 20)), Color.RED);
		Assert.assertEquals(new Color(image.getRGB(100, 0)), Color.RED);
	}
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceMediumMatch2() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		
		// comparison successful
		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(new File(stepFailed.getSnapshots().get(0).getScreenshot().getFullImagePath()), referenceImgStep2).thenReturn(stepReferenceComparatorStep2);
		when(stepReferenceComparatorStep2.compare()).thenReturn(50);
		when(stepReferenceComparatorStep2.getMissingLabels()).thenReturn(Arrays.asList(new Label(0, 100, 20, 50, "some label")));
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference();
		
		Assert.assertEquals(causes.size(), 1);
		Assert.assertEquals(causes.get(0).getType(), ErrorType.APPLICATION_CHANGED);
		Assert.assertEquals(causes.get(0).getDescription(), "1 Label(s) missing: \n"
				+ "some label\n");
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
		
		// check line has been drawn below the missing label
		BufferedImage image = ImageIO.read(referenceImgStep2);
		Assert.assertEquals(new Color(image.getRGB(0, 50)), Color.RED);
		Assert.assertNotEquals(new Color(image.getRGB(0, 20)), Color.RED);
		Assert.assertEquals(new Color(image.getRGB(100, 50)), Color.RED);
		Assert.assertNotEquals(new Color(image.getRGB(100, 20)), Color.RED);
	}

	/**
	 * Matching between reference picture stored on server and the one for current test is bad (we are not on the right page)
	 * Check we try to compare to reference of step1
	 * Comparison with step 1 is good (we are on previous step)
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceBadMatchPreviousStep() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		
		// comparison successful
		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(new File(stepFailed.getSnapshots().get(0).getScreenshot().getFullImagePath()), referenceImgStep2).thenReturn(stepReferenceComparatorStep2);
		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(new File(stepFailed.getSnapshots().get(0).getScreenshot().getFullImagePath()), referenceImgStep1).thenReturn(stepReferenceComparatorStep1);
		when(stepReferenceComparatorStep2.compare()).thenReturn(49); // bad comparison with step2 reference
		when(stepReferenceComparatorStep1.compare()).thenReturn(81); // good comparison with step1 reference
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference();
		
		Assert.assertEquals(causes.size(), 1);
		Assert.assertEquals(causes.get(0).getType(), ErrorType.SELENIUM_ERROR);
		Assert.assertEquals(causes.get(0).getDescription(), "Wrong page found, we are on the page of step 'step 1'");
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
	
	/**
	 * Matching between reference picture stored on server and the one for current test is bad (we are not on the right page)
	 * Check we try to compare to reference of step1
	 * Comparison with step 1 is bad (we are not on previous step)
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceBadMatchNotOnPreviousStep() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		
		// comparison successful
		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(new File(stepFailed.getSnapshots().get(0).getScreenshot().getFullImagePath()), referenceImgStep2).thenReturn(stepReferenceComparatorStep2);
		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(new File(stepFailed.getSnapshots().get(0).getScreenshot().getFullImagePath()), referenceImgStep1).thenReturn(stepReferenceComparatorStep1);
		when(stepReferenceComparatorStep2.compare()).thenReturn(49); // bad comparison with step2 reference
		when(stepReferenceComparatorStep1.compare()).thenReturn(80); // good comparison with step1 reference
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference();
		
		Assert.assertEquals(causes.size(), 1);
		Assert.assertEquals(causes.get(0).getType(), ErrorType.UNKNOWN_PAGE);
		Assert.assertNull(causes.get(0).getDescription());
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
	
	/**
	 * Matching between reference picture stored on server and the one for current test is bad (we are not on the right page)
	 * No previous step can be found
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceBadMatchNoPreviousStep() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(stepFailed, lastStep));
		
		// comparison successful
		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(new File(stepFailed.getSnapshots().get(0).getScreenshot().getFullImagePath()), referenceImgStep2).thenReturn(stepReferenceComparatorStep2);
		when(stepReferenceComparatorStep2.compare()).thenReturn(49); // bad comparison with step2 reference
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference();
		
		Assert.assertEquals(causes.size(), 1);
		Assert.assertEquals(causes.get(0).getType(), ErrorType.UNKNOWN_PAGE);
		Assert.assertNull(causes.get(0).getDescription());
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
	
	
	/**
	 * Bad match: an error occurs when getting reference snapshot
	 * We should not fail
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceBadMatchErrorGettingReference() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		
		// comparison successful
		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(new File(stepFailed.getSnapshots().get(0).getScreenshot().getFullImagePath()), referenceImgStep2).thenReturn(stepReferenceComparatorStep2);
		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(new File(stepFailed.getSnapshots().get(0).getScreenshot().getFullImagePath()), referenceImgStep1).thenReturn(stepReferenceComparatorStep1);
		when(stepReferenceComparatorStep2.compare()).thenReturn(49); // bad comparison with step2 reference
		
		// error occurs when getting reference on step 1
		when(serverConnector.getReferenceSnapshot(0)).thenReturn(null);
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference();
		
		Assert.assertEquals(causes.size(), 1);
		Assert.assertEquals(causes.get(0).getType(), ErrorType.UNKNOWN_PAGE);
		Assert.assertNull(causes.get(0).getDescription());
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
	
	/**
	 * Bad match: an exception occurs when getting reference snapshot
	 * We should not fail
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceBadMatchErrorGettingReference2() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		
		// comparison successful
		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(new File(stepFailed.getSnapshots().get(0).getScreenshot().getFullImagePath()), referenceImgStep2).thenReturn(stepReferenceComparatorStep2);
		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(new File(stepFailed.getSnapshots().get(0).getScreenshot().getFullImagePath()), referenceImgStep1).thenReturn(stepReferenceComparatorStep1);
		when(stepReferenceComparatorStep2.compare()).thenReturn(49); // bad comparison with step2 reference
		
		// exception occurs when getting reference on step 1
		when(serverConnector.getReferenceSnapshot(0)).thenThrow(new ConfigurationException(""));
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference();
		
		Assert.assertEquals(causes.size(), 1);
		Assert.assertEquals(causes.get(0).getType(), ErrorType.UNKNOWN_PAGE);
		Assert.assertNull(causes.get(0).getDescription());
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
	
	/**
	 * Check the case where no TestStep is available
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceNoStep() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(new ArrayList<>());
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference();
		
		Assert.assertEquals(causes.size(), 0);
		Assert.assertFalse(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
	
	/**
	 * Failed test step does not contain a "reference snapshot" so there is no way to compare it to the one stored on server
	 * No error should be raised
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceNoReferenceSnapshot() throws Exception {
		
		stepFailed.getSnapshots().removeAll(stepFailed.getSnapshots());
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		
		PowerMockito.whenNew(StepReferenceComparator.class).withAnyArguments().thenReturn(stepReferenceComparatorStep2);
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference();
		
		// no comparison done
		PowerMockito.verifyNew(StepReferenceComparator.class, never()).withArguments(any(File.class), any(File.class));;
		
		Assert.assertEquals(causes.size(), 0);
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
	
	/**
	 * No failed step in test, no error should be returned
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceNoFailedStep() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, lastStep));
		
		PowerMockito.whenNew(StepReferenceComparator.class).withAnyArguments().thenReturn(stepReferenceComparatorStep2);
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference();

		// no comparison done
		PowerMockito.verifyNew(StepReferenceComparator.class, never()).withArguments(any(File.class), any(File.class));;
		
		Assert.assertEquals(causes.size(), 0);
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
	
	/**
	 * When step has not been stored on server, they have no ID. Check that without ID, no comparison performed, and no error is raised
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceNoIDForStep() throws Exception {
		
		step1.setStepResultId(null);
		stepFailed.setStepResultId(null);
		lastStep.setStepResultId(null);
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		
		PowerMockito.whenNew(StepReferenceComparator.class).withAnyArguments().thenReturn(stepReferenceComparatorStep2);
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference();
		
		// no comparison done
		PowerMockito.verifyNew(StepReferenceComparator.class, never()).withArguments(any(File.class), any(File.class));;
		
		Assert.assertEquals(causes.size(), 0);
		Assert.assertFalse(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
	
	/**
	 * Same thing as above but only the failed step has no ID
	 * In this case, we consider comparison has been performed because other steps have IDs
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceNoIDForStep2() throws Exception {
		
		stepFailed.setStepResultId(null);
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		
		PowerMockito.whenNew(StepReferenceComparator.class).withAnyArguments().thenReturn(stepReferenceComparatorStep2);
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference();
		
		// no comparison done
		PowerMockito.verifyNew(StepReferenceComparator.class, never()).withArguments(any(File.class), any(File.class));;
		
		Assert.assertEquals(causes.size(), 0);
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
	
	/**
	 * Same thing as above but only step1 has no ID with bad match so we cannot look for previous step reference
	 * In this case, we get an unknown_page cause
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceNoIDForStep3() throws Exception {
		
		step1.setStepResultId(null);
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		
		// comparison successful
		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(new File(stepFailed.getSnapshots().get(0).getScreenshot().getFullImagePath()), referenceImgStep2).thenReturn(stepReferenceComparatorStep2);
		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(new File(stepFailed.getSnapshots().get(0).getScreenshot().getFullImagePath()), referenceImgStep1).thenReturn(stepReferenceComparatorStep1);
		when(stepReferenceComparatorStep2.compare()).thenReturn(49); // bad comparison with step2 reference
		when(stepReferenceComparatorStep1.compare()).thenReturn(81); // good comparison with step1 reference
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference();

		Assert.assertEquals(causes.size(), 1);
		Assert.assertEquals(causes.get(0).getType(), ErrorType.UNKNOWN_PAGE);
		Assert.assertNull(causes.get(0).getDescription());
	}
	
	/**
	 * When step fails due to assertion error, we do not compare snapshot with reference as the failure is expected
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceAssertionError() throws Exception {
		
		stepFailed.setActionException(new AssertionError("result is not the expected one"));
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		
		PowerMockito.whenNew(StepReferenceComparator.class).withAnyArguments().thenReturn(stepReferenceComparatorStep2);
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference();
		
		// no comparison done
		PowerMockito.verifyNew(StepReferenceComparator.class, never()).withArguments(any(File.class), any(File.class));;
		
		Assert.assertEquals(causes.size(), 0);
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
	
	/**
	 * There is no reference for this step on server
	 * Search is considered as done
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceNoReferenceOnServer() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		
		// no reference exists for stepFailed on server
		when(serverConnector.getReferenceSnapshot(1)).thenReturn(null);
		PowerMockito.whenNew(StepReferenceComparator.class).withAnyArguments().thenReturn(stepReferenceComparatorStep2);
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference();
		
		// no comparison done
		PowerMockito.verifyNew(StepReferenceComparator.class, never()).withArguments(any(File.class), any(File.class));
		
		Assert.assertEquals(causes.size(), 0);
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
	
	/**
	 * We get an error when trying to get reference snapshot from server
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceErrorGettingReferenceOnServer() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		
		// no reference exists for stepFailed on server
		when(serverConnector.getReferenceSnapshot(1)).thenThrow(new SeleniumRobotServerException("error"));
		PowerMockito.whenNew(StepReferenceComparator.class).withAnyArguments().thenReturn(stepReferenceComparatorStep2);
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference();
		
		// no comparison done
		PowerMockito.verifyNew(StepReferenceComparator.class, never()).withArguments(any(File.class), any(File.class));
		
		Assert.assertEquals(causes.size(), 0);
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
	
	/**
	 * If any error occurs in comparison, continue
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceErrorInComparison() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		
		// comparison successful
		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(new File(stepFailed.getSnapshots().get(0).getScreenshot().getFullImagePath()), referenceImgStep2).thenReturn(stepReferenceComparatorStep2);
		when(stepReferenceComparatorStep2.compare()).thenThrow(new ConfigurationException("error on init"));
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference();
		
		Assert.assertEquals(causes.size(), 0);
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
	
}
