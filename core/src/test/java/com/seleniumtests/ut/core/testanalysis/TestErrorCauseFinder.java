package com.seleniumtests.ut.core.testanalysis;

import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
		referenceImgStep2 = File.createTempFile("img", ".png"); // reference image for step 2
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
		Assert.assertEquals(causes.get(0).getDescription(), "il y a eu un gros problème");
		Assert.assertEquals(causes.get(1).getType(), ErrorType.ERROR_MESSAGE);
		Assert.assertEquals(causes.get(1).getDescription(), "some error");
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
	
	// pas de step
	// pas de snapshot
	// pas de step en erreur
	// pas d'ID au step
	// assertionError
	// pas de step reference
	// getReferenceSnapshot renvoie null / exception
	// matching < 90
	// recherche déjà faite
	// echec lors de la comparaison (exception)
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
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference();
		
		Assert.assertEquals(causes.size(), 1);
		Assert.assertEquals(causes.get(0).getType(), ErrorType.APPLICATION_CHANGED);
		Assert.assertNull(causes.get(0).getDescription());
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
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
	

}
