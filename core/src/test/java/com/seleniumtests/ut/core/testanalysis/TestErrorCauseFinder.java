package com.seleniumtests.ut.core.testanalysis;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.connectors.selenium.fielddetector.Field;
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

import kong.unirest.json.JSONObject;

//@PrepareForTest({ErrorCauseFinder.class, SeleniumRobotSnapshotServerConnector.class})
public class TestErrorCauseFinder extends MockitoTest {

	@Mock
	SeleniumRobotSnapshotServerConnector serverConnector;
	
	@Mock
	StepReferenceComparator stepReferenceComparatorStep3;
	
	@Mock
	StepReferenceComparator stepReferenceComparatorStep2;
	
	@Mock
	StepReferenceComparator stepReferenceComparatorStep1;
	
	private TestStep step1;
	private TestStep step2;
	private TestStep stepFailed;
	private TestStep lastStep;
	
	private File imgStep1Ref;
	private File imgStep1;
	private File imgStep2Ref;
	private File imgStep3Ref;
	private File imgLastStep;
	private File referenceImgStep1;
	private File referenceImgStep2;
	private File referenceImgStep3;
	
	private JSONObject failedStepFieldInformation = new JSONObject("{"
			+ "    \"fields\": ["
			+ "            {"
			+ "                \"class_id\": 2,"
			+ "                \"left\": 0,"
			+ "                \"right\": 100,"
			+ "                \"top\": 20,"
			+ "                \"bottom\": 50,"
			+ "                \"class_name\": \"field\","
			+ "                \"text\": null,"
			+ "                \"related_field\": null,"
			+ "                \"with_label\": false,"
			+ "                \"width\": 100,"
			+ "                \"height\": 20"
			+ "            }"
			+ "        ],"
			+ "    \"labels\": [],"
			+ "    \"error\": null,"
			+ "    \"version\": \"afcc45\""
			+ "}");
	private JSONObject referenceStepFieldInformation = new JSONObject("{"
			+ "    \"fields\": ["
			+ "            {"
			+ "                \"class_id\": 2,"
			+ "                \"left\": 1,"
			+ "                \"right\": 111,"
			+ "                \"top\": 22,"
			+ "                \"bottom\": 222,"
			+ "                \"class_name\": \"field\","
			+ "                \"text\": null,"
			+ "                \"related_field\": null,"
			+ "                \"with_label\": false,"
			+ "                \"width\": 110,"
			+ "                \"height\": 200"
			+ "            }"
			+ "        ],"
			+ "    \"labels\": [],"
			+ "    \"error\": null,"
			+ "    \"version\": \"afcc45\""
			+ "}");
	private JSONObject referenceStepFieldInformation2 = new JSONObject("{"
			+ "    \"fields\": ["
			+ "            {"
			+ "                \"class_id\": 2,"
			+ "                \"left\": 1,"
			+ "                \"right\": 111,"
			+ "                \"top\": 33,"
			+ "                \"bottom\": 333,"
			+ "                \"class_name\": \"field\","
			+ "                \"text\": null,"
			+ "                \"related_field\": null,"
			+ "                \"with_label\": false,"
			+ "                \"width\": 110,"
			+ "                \"height\": 200"
			+ "            }"
			+ "        ],"
			+ "    \"labels\": [],"
			+ "    \"error\": null,"
			+ "    \"version\": \"afcc45\""
			+ "}");
	private JSONObject referenceStepFieldInformation3 = new JSONObject("{"
			+ "    \"fields\": ["
			+ "            {"
			+ "                \"class_id\": 2,"
			+ "                \"left\": 1,"
			+ "                \"right\": 111,"
			+ "                \"top\": 44,"
			+ "                \"bottom\": 444,"
			+ "                \"class_name\": \"field\","
			+ "                \"text\": null,"
			+ "                \"related_field\": null,"
			+ "                \"with_label\": false,"
			+ "                \"width\": 110,"
			+ "                \"height\": 200"
			+ "            }"
			+ "        ],"
			+ "    \"labels\": [],"
			+ "    \"error\": null,"
			+ "    \"version\": \"afcc45\""
			+ "}");

	private MockedStatic mockedSnapshotServer;
	
	@BeforeMethod(alwaysRun = true)
	public void init() throws Exception {
		
		// create first step
		step1 = new TestStep("step 1", Reporter.getCurrentTestResult(), new ArrayList<>(), false);
		step1.setStepResultId(0);
		step1.setPosition(0);
		
		File image = GenericTest.createFileFromResource("tu/images/driverTestPage.png");
		
		
		imgStep1Ref = File.createTempFile("img", ".png");
		imgStep1 = File.createTempFile("img", ".png");
		
		ScreenShot screenshot = new ScreenShot(imgStep1Ref, File.createTempFile("html", ".html"));
		step1.addSnapshot(new Snapshot(screenshot, null, SnapshotCheckType.REFERENCE_ONLY), 1, null);
		
		ScreenShot screenshot2 = new ScreenShot(imgStep1, File.createTempFile("html", ".html"));
		step1.addSnapshot(new Snapshot(screenshot2, null, SnapshotCheckType.FALSE), 1, null);

		// create an second step
		step2 = new TestStep("step 2", Reporter.getCurrentTestResult(), new ArrayList<>(), false);
		step2.setStepResultId(1);
		step2.setPosition(1);
	
		imgStep2Ref = File.createTempFile("img", ".png");
		
		ScreenShot screenshotStep3 = new ScreenShot(imgStep2Ref, File.createTempFile("html", ".html"));
		step2.addSnapshot(new Snapshot(screenshot, null, SnapshotCheckType.REFERENCE_ONLY), 1, null);
		
		// create third step, which will fail
		stepFailed = new TestStep("step 3", Reporter.getCurrentTestResult(), new ArrayList<>(), false);
		stepFailed.setStepResultId(2);
		stepFailed.setFailed(true);
		stepFailed.setPosition(2);
		
		imgStep3Ref = File.createTempFile("img", ".png");
		
		ScreenShot screenshot3 = new ScreenShot(imgStep3Ref, File.createTempFile("html", ".html"));
		stepFailed.addSnapshot(new Snapshot(screenshot3, null, SnapshotCheckType.REFERENCE_ONLY), 1, null);
		
		// create 'Test end' step
		lastStep = new TestStep(TestStepManager.LAST_STEP_NAME, Reporter.getCurrentTestResult(), new ArrayList<>(), false);
		lastStep.setPosition(3);
		
		imgLastStep = File.createTempFile("img", ".png");
		
		ScreenShot screenshot4 = new ScreenShot(imgStep1, File.createTempFile("html", ".html"));
		lastStep.addSnapshot(new Snapshot(screenshot4, "main", SnapshotCheckType.FALSE), 1, null);
		lastStep.setStepResultId(10);

		mockedSnapshotServer = mockStatic(SeleniumRobotSnapshotServerConnector.class);
		mockedSnapshotServer.when(() -> SeleniumRobotSnapshotServerConnector.getInstance()).thenReturn(serverConnector);


		referenceImgStep1 = File.createTempFile("img", ".png"); // reference image for step 1
		FileUtils.copyFile(image, referenceImgStep1);
		referenceImgStep2 = File.createTempFile("img", ".png"); // reference image for step 2
		FileUtils.copyFile(image, referenceImgStep2);
		referenceImgStep3 = File.createTempFile("img", ".png"); // reference image for step 2
		FileUtils.copyFile(image, referenceImgStep3);
		when(serverConnector.getReferenceSnapshot(0)).thenReturn(referenceImgStep1);
		when(serverConnector.getReferenceSnapshot(1)).thenReturn(referenceImgStep2);
		when(serverConnector.getReferenceSnapshot(2)).thenReturn(referenceImgStep3);
		
	}

	@AfterMethod(groups = "ut", alwaysRun = true)
	private void closeMocks() {
		mockedSnapshotServer.close();
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
		
		when(serverConnector.detectErrorInPicture(lastStep.getSnapshots().get(0))).thenReturn(new JSONObject("{"
				+ "        \"fields\": ["
				+ "            {"
				+ "                \"class_id\": 2,"
				+ "                \"top\": 20,"
				+ "                \"bottom\": 50,"
				+ "                \"left\": 0,"
				+ "                \"right\": 100,"
				+ "                \"class_name\": \"field\","
				+ "                \"text\": null,"
				+ "                \"related_field\": null,"
				+ "                \"with_label\": false,"
				+ "                \"width\": 100,"
				+ "                \"height\": 20"
				+ "            },{"
				+ "                \"class_id\": 2,"
				+ "                \"top\": 20,"
				+ "                \"bottom\": 50,"
				+ "                \"left\": 0,"
				+ "                \"right\": 100,"
				+ "                \"class_name\": \"field\","
				+ "                \"text\": null,"
				+ "                \"related_field\": null,"
				+ "                \"with_label\": false,"
				+ "                \"width\": 100,"
				+ "                \"height\": 20"
				+ "            }"
				+ "        ],"
				+ "        \"labels\": ["
				+ "            {"
				+ "                \"top\": 20,"
				+ "                \"left\": 0,"
				+ "                \"width\": 100,"
				+ "                \"height\": 30,"
				+ "                \"text\": \"il y a eu un gros problème\","
				+ "                \"right\": 100,"
				+ "                \"bottom\": 50"
				+ "            },{"
				+ "                \"top\": 100,"
				+ "                \"left\": 0,"
				+ "                \"width\": 100,"
				+ "                \"height\": 20,"
				+ "                \"text\": \"some error\","
				+ "                \"right\": 100,"
				+ "                \"bottom\": 120"
				+ "            }"
				+ "        ],"
				+ ""
				+ "    \"error\": null,"
				+ "    \"version\": \"afcc45\""
				+ "}"));
	
		List<ErrorCause> causes = new ArrayList<>();
		String version = new ErrorCauseFinder(testResult).findErrorInLastStepSnapshots(causes);
		
		Assert.assertEquals(causes.size(), 2);
		Assert.assertEquals(causes.get(0).getType(), ErrorType.ERROR_MESSAGE);
		Assert.assertEquals(causes.get(0).getTestStep(), lastStep); // no failed step except "last step", keep association
		Assert.assertEquals(causes.get(0).getDescription(), "il y a eu un gros problème");
		Assert.assertEquals(causes.get(1).getType(), ErrorType.ERROR_MESSAGE);
		Assert.assertEquals(causes.get(1).getDescription(), "some error");
		Assert.assertEquals(causes.get(1).getTestStep(), lastStep);
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInLastStep(testResult));
		Assert.assertEquals(version, "afcc45");
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
		
		when(serverConnector.detectErrorInPicture(lastStep.getSnapshots().get(0))).thenReturn(new JSONObject("{"
				+ "        \"fields\": ["
				+ "            {"
				+ "                \"class_id\": 2,"
				+ "                \"top\": 20,"
				+ "                \"bottom\": 50,"
				+ "                \"left\": 0,"
				+ "                \"right\": 100,"
				+ "                \"class_name\": \"field\","
				+ "                \"text\": null,"
				+ "                \"related_field\": null,"
				+ "                \"with_label\": false,"
				+ "                \"width\": 100,"
				+ "                \"height\": 20"
				+ "            },{"
				+ "                \"class_id\": 2,"
				+ "                \"top\": 20,"
				+ "                \"bottom\": 50,"
				+ "                \"left\": 0,"
				+ "                \"right\": 100,"
				+ "                \"class_name\": \"field\","
				+ "                \"text\": null,"
				+ "                \"related_field\": null,"
				+ "                \"with_label\": false,"
				+ "                \"width\": 100,"
				+ "                \"height\": 20"
				+ "            }"
				+ "        ],"
				+ "        \"labels\": ["
				+ "            {"
				+ "                \"top\": 20,"
				+ "                \"left\": 0,"
				+ "                \"width\": 100,"
				+ "                \"height\": 30,"
				+ "                \"text\": \"il y a eu un gros problème\","
				+ "                \"right\": 100,"
				+ "                \"bottom\": 50"
				+ "            },{"
				+ "                \"top\": 100,"
				+ "                \"left\": 0,"
				+ "                \"width\": 100,"
				+ "                \"height\": 20,"
				+ "                \"text\": \"some error\","
				+ "                \"right\": 100,"
				+ "                \"bottom\": 120"
				+ "            }"
				+ "        ],"
				+ ""
				+ "    \"error\": null,"
				+ "    \"version\": \"afcc45\""
				+ "}"));
		
		List<ErrorCause> causes = new ArrayList<>();
		String version = new ErrorCauseFinder(testResult).findErrorInLastStepSnapshots(causes);
		
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
		
		when(serverConnector.detectErrorInPicture(lastStep.getSnapshots().get(0))).thenThrow(new SeleniumRobotServerException("error"));
		
		List<ErrorCause> causes = new ArrayList<>();
		new ErrorCauseFinder(testResult).findErrorInLastStepSnapshots(causes);
		
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
		
		when(serverConnector.detectErrorInPicture(lastStep.getSnapshots().get(0))).thenReturn(new JSONObject("{"
				+ "        \"fields\": ["
				+ "            {"
				+ "                \"class_id\": 2,"
				+ "                \"top\": 20,"
				+ "                \"bottom\": 50,"
				+ "                \"left\": 0,"
				+ "                \"right\": 100,"
				+ "                \"class_name\": \"error_field\","
				+ "                \"text\": null,"
				+ "                \"related_field\": null,"
				+ "                \"with_label\": false,"
				+ "                \"width\": 100,"
				+ "                \"height\": 20"
				+ "            },{"
				+ "                \"class_id\": 2,"
				+ "                \"top\": 20,"
				+ "                \"bottom\": 50,"
				+ "                \"left\": 0,"
				+ "                \"right\": 100,"
				+ "                \"class_name\": \"field\","
				+ "                \"text\": null,"
				+ "                \"related_field\": null,"
				+ "                \"with_label\": false,"
				+ "                \"width\": 100,"
				+ "                \"height\": 20"
				+ "            }"
				+ "        ],"
				+ "        \"labels\": ["
				+ "            {"
				+ "                \"top\": 20,"
				+ "                \"left\": 0,"
				+ "                \"width\": 100,"
				+ "                \"height\": 30,"
				+ "                \"text\": \"ok\","
				+ "                \"right\": 100,"
				+ "                \"bottom\": 50"
				+ "            },{"
				+ "                \"top\": 100,"
				+ "                \"left\": 0,"
				+ "                \"width\": 100,"
				+ "                \"height\": 20,"
				+ "                \"text\": \"nothing\","
				+ "                \"right\": 100,"
				+ "                \"bottom\": 120"
				+ "            }"
				+ "        ],"
				+ ""
				+ "    \"error\": null,"
				+ "    \"version\": \"afcc45\""
				+ "}"));
		
		List<ErrorCause> causes = new ArrayList<>();
		new ErrorCauseFinder(testResult).findErrorInLastStepSnapshots(causes);
		
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
		
		when(serverConnector.detectErrorInPicture(lastStep.getSnapshots().get(0))).thenReturn(new JSONObject("{"
				+ "        \"fields\": ["
				+ "            {"
				+ "                \"class_id\": 2,"
				+ "                \"top\": 0,"
				+ "                \"bottom\": 20,"
				+ "                \"left\": 0,"
				+ "                \"right\": 100,"
				+ "                \"class_name\": \"error_message\","
				+ "                \"text\": null,"
				+ "                \"related_field\": null,"
				+ "                \"with_label\": false,"
				+ "                \"width\": 100,"
				+ "                \"height\": 20"
				+ "            },{"
				+ "                \"class_id\": 2,"
				+ "                \"top\": 20,"
				+ "                \"bottom\": 50,"
				+ "                \"left\": 0,"
				+ "                \"right\": 100,"
				+ "                \"class_name\": \"field\","
				+ "                \"text\": null,"
				+ "                \"related_field\": null,"
				+ "                \"with_label\": false,"
				+ "                \"width\": 100,"
				+ "                \"height\": 20"
				+ "            }"
				+ "        ],"
				+ "        \"labels\": ["
				+ "            {"
				+ "                \"top\": 0,"
				+ "                \"left\": 0,"
				+ "                \"width\": 100,"
				+ "                \"height\": 20,"
				+ "                \"text\": \"ko\","
				+ "                \"right\": 100,"
				+ "                \"bottom\": 20"
				+ "            },{"
				+ "                \"top\": 100,"
				+ "                \"left\": 0,"
				+ "                \"width\": 100,"
				+ "                \"height\": 20,"
				+ "                \"text\": \"nothing\","
				+ "                \"right\": 100,"
				+ "                \"bottom\": 120"
				+ "            }"
				+ "        ],"
				+ ""
				+ "    \"error\": null,"
				+ "    \"version\": \"afcc45\""
				+ "}"));
		

		List<ErrorCause> causes = new ArrayList<>();
		new ErrorCauseFinder(testResult).findErrorInLastStepSnapshots(causes);

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
		
		when(serverConnector.detectErrorInPicture(lastStep.getSnapshots().get(0))).thenReturn(new JSONObject("{"
				+ "        \"fields\": ["
				+ "            {"
				+ "                \"class_id\": 2,"
				+ "                \"left\": 0,"
				+ "                \"right\": 100,"
				+ "                \"top\": 20,"
				+ "                \"bottom\": 50,"
				+ "                \"class_name\": \"field\","
				+ "                \"text\": null,"
				+ "                \"related_field\": null,"
				+ "                \"with_label\": false,"
				+ "                \"width\": 100,"
				+ "                \"height\": 20"
				+ "            },{"
				+ "                \"class_id\": 2,"
				+ "                \"left\": 0,"
				+ "                \"right\": 100,"
				+ "                \"top\": 100,"
				+ "                \"bottom\": 120,"
				+ "                \"class_name\": \"field\","
				+ "                \"text\": null,"
				+ "                \"related_field\": null,"
				+ "                \"with_label\": false,"
				+ "                \"width\": 100,"
				+ "                \"height\": 20"
				+ "            }"
				+ "        ],"
				+ "        \"labels\": ["
				+ "            {"
				+ "                \"left\": 0,"
				+ "                \"right\": 100,"
				+ "                \"top\": 0,"
				+ "                \"bottom\": 20,"
				+ "                \"width\": 100,"
				+ "                \"height\": 30,"
				+ "                \"text\": \"tout roule\""
				+ "            },{"
				+ "                \"left\": 0,"
				+ "                \"right\": 100,"
				+ "                \"top\": 200,"
				+ "                \"bottom\": 220,"
				+ "                \"width\": 100,"
				+ "                \"height\": 20,"
				+ "                \"text\": \"everything is fine\""
				+ "            }"
				+ "        ],"
				+ ""
				+ "    \"error\": null,"
				+ "    \"version\": \"afcc45\""
				+ "}"));
		
		List<ErrorCause> causes = new ArrayList<>();
		new ErrorCauseFinder(testResult).findErrorInLastStepSnapshots(causes);
		
		Assert.assertEquals(causes.size(), 0);
	}
	
	@Test(groups= {"ut"})
	public void testSearchInLastStepNoLastStep() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1));
		
		List<ErrorCause> causes = new ArrayList<>();
		new ErrorCauseFinder(testResult).findErrorInLastStepSnapshots(causes);
		
		Assert.assertEquals(causes.size(), 0);
	}
	
	@Test(groups= {"ut"})
	public void testSearchInLastStepAlreadyDone() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, lastStep));
		
		when(serverConnector.detectErrorInPicture(lastStep.getSnapshots().get(0))).thenReturn(new JSONObject("{"
				+ "        \"fields\": ["
				+ "            {"
				+ "                \"class_id\": 2,"
				+ "                \"left\": 0,"
				+ "                \"right\": 100,"
				+ "                \"top\": 20,"
				+ "                \"bottom\": 50,"
				+ "                \"class_name\": \"field\","
				+ "                \"text\": null,"
				+ "                \"related_field\": null,"
				+ "                \"with_label\": false,"
				+ "                \"width\": 100,"
				+ "                \"height\": 20"
				+ "            },{"
				+ "                \"class_id\": 2,"
				+ "                \"left\": 0,"
				+ "                \"right\": 100,"
				+ "                \"top\": 100,"
				+ "                \"bottom\": 120,"
				+ "                \"class_name\": \"field\","
				+ "                \"text\": null,"
				+ "                \"related_field\": null,"
				+ "                \"with_label\": false,"
				+ "                \"width\": 100,"
				+ "                \"height\": 20"
				+ "            }"
				+ "        ],"
				+ "        \"labels\": ["
				+ "            {"
				+ "                \"left\": 0,"
				+ "                \"right\": 100,"
				+ "                \"top\": 0,"
				+ "                \"bottom\": 20,"
				+ "                \"width\": 100,"
				+ "                \"height\": 30,"
				+ "                \"text\": \"il y a eu un gros problème\""
				+ "            },{"
				+ "                \"left\": 0,"
				+ "                \"right\": 100,"
				+ "                \"top\": 200,"
				+ "                \"bottom\": 220,"
				+ "                \"width\": 100,"
				+ "                \"height\": 20,"
				+ "                \"text\": \"some error\""
				+ "            }"
				+ "        ],"
				+ ""
				+ "    \"error\": null,"
				+ "    \"version\": \"afcc45\""
				+ "}"));
				

		TestNGResultUtils.setErrorCauseSearchedInLastStep(testResult, true);
		List<ErrorCause> causes = new ArrayList<>();
		new ErrorCauseFinder(testResult).findErrorInLastStepSnapshots(causes);

		Assert.assertEquals(causes.size(), 0);
	}


	/**
	 * Matching between reference picture stored on server and the one for current test is very good (we are on the right page)
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceGoodMatch() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		step1.setPosition(0);
		stepFailed.setPosition(1);
		
		when(serverConnector.detectFieldsInPicture(stepFailed.getSnapshots().get(0))).thenReturn(failedStepFieldInformation); // no matter the JSONObject as we mock StepReferenceComparator
		when(serverConnector.getStepReferenceDetectFieldInformation(2, "aa")).thenReturn(referenceStepFieldInformation);
		
		// comparison successful
		try (MockedConstruction mockedStepReferenceComparator = mockConstruction(StepReferenceComparator.class, (mock, context) -> {
			when(mock.compare()).thenReturn(90); // good matching
		})) {

			List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");

			Assert.assertEquals(causes.size(), 0);
			Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
		}
	}
	
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceSearchAlreadyDone() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		
		TestNGResultUtils.setErrorCauseSearchedInReferencePicture(testResult, true);
		
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		step1.setPosition(0);
		stepFailed.setPosition(1);
		
		when(serverConnector.detectFieldsInPicture(stepFailed.getSnapshots().get(0))).thenReturn(failedStepFieldInformation); // no matter the JSONObject as we mock StepReferenceComparator
		when(serverConnector.getStepReferenceDetectFieldInformation(2, "aa")).thenReturn(referenceStepFieldInformation);
		
		// comparison successful
		try (MockedConstruction mockedStepReferenceComparator = mockConstruction(StepReferenceComparator.class, (mock, context) -> {
			when(mock.compare()).thenReturn(90); // good matching
		})) {

			List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");

			// no comparison done
			Assert.assertEquals(mockedStepReferenceComparator.constructed().size(), 0);

			Assert.assertEquals(causes.size(), 0);
			Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
		}
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
		step1.setPosition(0);
		stepFailed.setPosition(1);
		
		when(serverConnector.detectFieldsInPicture(stepFailed.getSnapshots().get(0))).thenReturn(failedStepFieldInformation); // no matter the JSONObject as we mock StepReferenceComparator
		when(serverConnector.getStepReferenceDetectFieldInformation(2, "aa")).thenReturn(referenceStepFieldInformation);
		
		// comparison successful
//		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(Field.fromDetectionData(failedStepFieldInformation),
//				Label.fromDetectionData(failedStepFieldInformation),
//				Field.fromDetectionData(referenceStepFieldInformation),
//				Label.fromDetectionData(referenceStepFieldInformation)).thenReturn(stepReferenceComparatorStep3);
		when(stepReferenceComparatorStep3.compare()).thenReturn(50);
		when(stepReferenceComparatorStep3.getMissingFields()).thenReturn(Arrays.asList(new Field(0, 100, 0, 20, "", "field")));
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");
		
		Assert.assertEquals(causes.size(), 1);
		Assert.assertEquals(causes.get(0).getType(), ErrorType.APPLICATION_CHANGED);
		Assert.assertEquals(causes.get(0).getDescription(), "1 field(s) missing: \n"
				+ "field[text=]: java.awt.Rectangle[x=0,y=0,width=100,height=20]\n");
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
		
		// check rectangle has been drawn around the missing field
		BufferedImage image = ImageIO.read(referenceImgStep3);
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
		step1.setPosition(0);
		stepFailed.setPosition(1);
		
		when(serverConnector.detectFieldsInPicture(stepFailed.getSnapshots().get(0))).thenReturn(failedStepFieldInformation); // no matter the JSONObject as we mock StepReferenceComparator
		when(serverConnector.getStepReferenceDetectFieldInformation(2, "aa")).thenReturn(referenceStepFieldInformation);
		
		// comparison successful
//		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(Field.fromDetectionData(failedStepFieldInformation),
//				Label.fromDetectionData(failedStepFieldInformation),
//				Field.fromDetectionData(referenceStepFieldInformation),
//				Label.fromDetectionData(referenceStepFieldInformation)).thenReturn(stepReferenceComparatorStep3);
		when(stepReferenceComparatorStep3.compare()).thenReturn(50);
		when(stepReferenceComparatorStep3.getMissingLabels()).thenReturn(Arrays.asList(new Label(0, 100, 20, 50, "some label")));
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");
		
		Assert.assertEquals(causes.size(), 1);
		Assert.assertEquals(causes.get(0).getType(), ErrorType.APPLICATION_CHANGED);
		Assert.assertEquals(causes.get(0).getDescription(), "1 Label(s) missing: \n"
				+ "some label\n");
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
		
		// check line has been drawn below the missing label
		BufferedImage image = ImageIO.read(referenceImgStep3);
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
		step1.setPosition(0);
		stepFailed.setPosition(1);

		when(serverConnector.detectFieldsInPicture(stepFailed.getSnapshots().get(0))).thenReturn(failedStepFieldInformation); // no matter the JSONObject as we mock StepReferenceComparator
		when(serverConnector.getStepReferenceDetectFieldInformation(2, "aa")).thenReturn(referenceStepFieldInformation); // reference for stepFailed
		when(serverConnector.getStepReferenceDetectFieldInformation(0, "aa")).thenReturn(referenceStepFieldInformation2); // reference for step1

		try (MockedConstruction mockedStepReferenceComparator = mockConstruction(StepReferenceComparator.class, (mock, context) -> {
			if (context.arguments().get(2).equals(Field.fromDetectionData(referenceStepFieldInformation))) {
				when(mock.compare()).thenReturn(49); // comparison between references
			} else if (context.arguments().get(2).equals(Field.fromDetectionData(referenceStepFieldInformation2))) {
				when(mock.compare()).thenReturn(81); // comparison with step 1
			}
		})) {

			// comparison successful
			List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");

			Assert.assertEquals(causes.size(), 1);
			Assert.assertEquals(causes.get(0).getType(), ErrorType.SELENIUM_ERROR);
			Assert.assertEquals(causes.get(0).getDescription(), "Wrong page found, we are on the page of step 'step 1'");
			Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
		}
	}
	
	/**
	 * #525: Test that step order is not modified when we compare with previous steps
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceBadMatchPreviousStep2() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, step2, stepFailed, lastStep));
		step1.setPosition(0);
		step2.setPosition(1);
		stepFailed.setPosition(2);
		
		when(serverConnector.detectFieldsInPicture(stepFailed.getSnapshots().get(0))).thenReturn(failedStepFieldInformation); // no matter the JSONObject as we mock StepReferenceComparator
		when(serverConnector.getStepReferenceDetectFieldInformation(2, "aa")).thenReturn(referenceStepFieldInformation); // reference for stepFailed
		when(serverConnector.getStepReferenceDetectFieldInformation(1, "aa")).thenReturn(referenceStepFieldInformation2); // reference for step2
		when(serverConnector.getStepReferenceDetectFieldInformation(0, "aa")).thenReturn(referenceStepFieldInformation3); // reference for step1
		
//		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(Field.fromDetectionData(failedStepFieldInformation),
//				Label.fromDetectionData(failedStepFieldInformation),
//				Field.fromDetectionData(referenceStepFieldInformation),
//				Label.fromDetectionData(referenceStepFieldInformation)).thenReturn(stepReferenceComparatorStep3);
//		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(Field.fromDetectionData(failedStepFieldInformation),
//				Label.fromDetectionData(failedStepFieldInformation),
//				Field.fromDetectionData(referenceStepFieldInformation2),
//				Label.fromDetectionData(referenceStepFieldInformation2)).thenReturn(stepReferenceComparatorStep2);
//		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(Field.fromDetectionData(failedStepFieldInformation),
//				Label.fromDetectionData(failedStepFieldInformation),
//				Field.fromDetectionData(referenceStepFieldInformation3),
//				Label.fromDetectionData(referenceStepFieldInformation3)).thenReturn(stepReferenceComparatorStep3);
		
		
		when(stepReferenceComparatorStep3.compare()).thenReturn(49); // bad comparison with step3 reference
		when(stepReferenceComparatorStep2.compare()).thenReturn(49); // bad comparison with step2 reference
		when(stepReferenceComparatorStep1.compare()).thenReturn(81); // good comparison with step1 reference
		
		new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");
		
		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.get(0), step1);
		Assert.assertEquals(steps.get(1), step2);
		Assert.assertEquals(steps.get(2), stepFailed);
		Assert.assertEquals(steps.get(3), lastStep);
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
		step1.setPosition(0);
		stepFailed.setPosition(1);
		
		when(serverConnector.detectFieldsInPicture(stepFailed.getSnapshots().get(0))).thenReturn(failedStepFieldInformation); // no matter the JSONObject as we mock StepReferenceComparator
		when(serverConnector.getStepReferenceDetectFieldInformation(2, "aa")).thenReturn(referenceStepFieldInformation); // reference for stepFailed
		when(serverConnector.getStepReferenceDetectFieldInformation(0, "aa")).thenReturn(referenceStepFieldInformation2); // reference for step1
		
//		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(Field.fromDetectionData(failedStepFieldInformation),
//				Label.fromDetectionData(failedStepFieldInformation),
//				Field.fromDetectionData(referenceStepFieldInformation),
//				Label.fromDetectionData(referenceStepFieldInformation)).thenReturn(stepReferenceComparatorStep3);
//		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(Field.fromDetectionData(failedStepFieldInformation),
//				Label.fromDetectionData(failedStepFieldInformation),
//				Field.fromDetectionData(referenceStepFieldInformation2),
//				Label.fromDetectionData(referenceStepFieldInformation2)).thenReturn(stepReferenceComparatorStep1);
		
		
		when(stepReferenceComparatorStep3.compare()).thenReturn(49); // bad comparison with step2 reference
		when(stepReferenceComparatorStep1.compare()).thenReturn(80); // good comparison with step1 reference
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");
		
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
		stepFailed.setPosition(0);
		
		when(serverConnector.detectFieldsInPicture(stepFailed.getSnapshots().get(0))).thenReturn(failedStepFieldInformation); // no matter the JSONObject as we mock StepReferenceComparator
		when(serverConnector.getStepReferenceDetectFieldInformation(2, "aa")).thenReturn(referenceStepFieldInformation); // reference for stepFailed
		
//		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(Field.fromDetectionData(failedStepFieldInformation),
//				Label.fromDetectionData(failedStepFieldInformation),
//				Field.fromDetectionData(referenceStepFieldInformation),
//				Label.fromDetectionData(referenceStepFieldInformation)).thenReturn(stepReferenceComparatorStep3);
		when(stepReferenceComparatorStep3.compare()).thenReturn(49); // bad comparison with step3 reference
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");
		
		Assert.assertEquals(causes.size(), 1);
		Assert.assertEquals(causes.get(0).getType(), ErrorType.UNKNOWN_PAGE);
		Assert.assertNull(causes.get(0).getDescription());
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
	
	
	/**
	 * Bad match: an error occurs when getting reference snapshot information
	 * We should not fail
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceBadMatchErrorGettingReference() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		step1.setPosition(0);
		stepFailed.setPosition(1);
		
		when(serverConnector.detectFieldsInPicture(stepFailed.getSnapshots().get(0))).thenReturn(failedStepFieldInformation); // no matter the JSONObject as we mock StepReferenceComparator
		when(serverConnector.getStepReferenceDetectFieldInformation(2, "aa")).thenReturn(referenceStepFieldInformation); // reference for stepFailed
		when(serverConnector.getStepReferenceDetectFieldInformation(0, "aa")).thenThrow(new SeleniumRobotServerException("error detect")); // reference for step1 fails
				
//		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(Field.fromDetectionData(failedStepFieldInformation),
//				Label.fromDetectionData(failedStepFieldInformation),
//				Field.fromDetectionData(referenceStepFieldInformation),
//				Label.fromDetectionData(referenceStepFieldInformation)).thenReturn(stepReferenceComparatorStep3);
		when(stepReferenceComparatorStep3.compare()).thenReturn(49); // bad comparison with step3 reference
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");
		
		Assert.assertEquals(causes.size(), 1);
		Assert.assertEquals(causes.get(0).getType(), ErrorType.UNKNOWN_PAGE);
		Assert.assertNull(causes.get(0).getDescription());
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
	
	/**
	 * Bad match: an exception occurs when getting reference snapshot information
	 * We should not fail
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceBadMatchErrorGettingReference2() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		step1.setPosition(0);
		stepFailed.setPosition(1);
		
		when(serverConnector.detectFieldsInPicture(stepFailed.getSnapshots().get(0))).thenReturn(failedStepFieldInformation); // no matter the JSONObject as we mock StepReferenceComparator
		when(serverConnector.getStepReferenceDetectFieldInformation(2, "aa")).thenReturn(referenceStepFieldInformation); // reference for stepFailed
		when(serverConnector.getStepReferenceDetectFieldInformation(0, "aa")).thenThrow(new ConfigurationException("error detect")); // reference for step1 fails
				
//		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(Field.fromDetectionData(failedStepFieldInformation),
//				Label.fromDetectionData(failedStepFieldInformation),
//				Field.fromDetectionData(referenceStepFieldInformation),
//				Label.fromDetectionData(referenceStepFieldInformation)).thenReturn(stepReferenceComparatorStep3);
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");
		
		Assert.assertEquals(causes.size(), 1);
		Assert.assertEquals(causes.get(0).getType(), ErrorType.UNKNOWN_PAGE);
		Assert.assertNull(causes.get(0).getDescription());
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
	/**
	 * In case error occurs detecting fields, stop process
	 * We should not fail
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceBadMatchErrorGettingReference3() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		step1.setPosition(0);
		stepFailed.setPosition(1);
		
		when(serverConnector.detectFieldsInPicture(stepFailed.getSnapshots().get(0))).thenThrow(new SeleniumRobotServerException("error detect")); // error while getting information
		when(serverConnector.getStepReferenceDetectFieldInformation(2, "aa")).thenReturn(referenceStepFieldInformation); // reference for stepFailed
		when(serverConnector.getStepReferenceDetectFieldInformation(0, "aa")).thenReturn(referenceStepFieldInformation2); // reference for step1 
		
//		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(Field.fromDetectionData(failedStepFieldInformation),
//				Label.fromDetectionData(failedStepFieldInformation),
//				Field.fromDetectionData(referenceStepFieldInformation),
//				Label.fromDetectionData(referenceStepFieldInformation)).thenReturn(stepReferenceComparatorStep3);
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");
		
		Assert.assertEquals(causes.size(), 0);
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
	/**
	 * In case error occurs detecting fields, stop process
	 * We should not fail
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testCompareStepInErrorWithReferenceBadMatchErrorGettingReference4() throws Exception {
		
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().setTestSteps(Arrays.asList(step1, stepFailed, lastStep));
		step1.setPosition(0);
		stepFailed.setPosition(1);
		
		when(serverConnector.detectFieldsInPicture(stepFailed.getSnapshots().get(0))).thenReturn(failedStepFieldInformation); // error while getting information
		when(serverConnector.getStepReferenceDetectFieldInformation(2, "aa")).thenThrow(new SeleniumRobotServerException("error detect")); // reference for stepFailed
		when(serverConnector.getStepReferenceDetectFieldInformation(0, "aa")).thenReturn(referenceStepFieldInformation2); // reference for step1 
		
//		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(Field.fromDetectionData(failedStepFieldInformation),
//				Label.fromDetectionData(failedStepFieldInformation),
//				Field.fromDetectionData(referenceStepFieldInformation),
//				Label.fromDetectionData(referenceStepFieldInformation)).thenReturn(stepReferenceComparatorStep3);
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");
		
		Assert.assertEquals(causes.size(), 0);
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
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");
		
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
		
//		PowerMockito.whenNew(StepReferenceComparator.class).withAnyArguments().thenReturn(stepReferenceComparatorStep3);
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");
		
		// no comparison done
//		PowerMockito.verifyNew(StepReferenceComparator.class, never()).withArguments(anyList(), anyList(), anyList(), anyList());
		
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
		
//		PowerMockito.whenNew(StepReferenceComparator.class).withAnyArguments().thenReturn(stepReferenceComparatorStep3);
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");

		// no comparison done
//		PowerMockito.verifyNew(StepReferenceComparator.class, never()).withArguments(anyList(), anyList(), anyList(), anyList());
		
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
		
//		PowerMockito.whenNew(StepReferenceComparator.class).withAnyArguments().thenReturn(stepReferenceComparatorStep3);
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");
		
		// no comparison done
//		PowerMockito.verifyNew(StepReferenceComparator.class, never()).withArguments(anyList(), anyList(), anyList(), anyList());
		
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
		
//		PowerMockito.whenNew(StepReferenceComparator.class).withAnyArguments().thenReturn(stepReferenceComparatorStep3);
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");
		
		// no comparison done
//		PowerMockito.verifyNew(StepReferenceComparator.class, never()).withArguments(anyList(), anyList(), anyList(), anyList());
		
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
		step1.setPosition(0);
		stepFailed.setPosition(1);

		when(serverConnector.detectFieldsInPicture(stepFailed.getSnapshots().get(0))).thenReturn(failedStepFieldInformation); // no matter the JSONObject as we mock StepReferenceComparator
		when(serverConnector.getStepReferenceDetectFieldInformation(2, "aa")).thenReturn(referenceStepFieldInformation); // reference for stepFailed
		when(serverConnector.getStepReferenceDetectFieldInformation(0, "aa")).thenReturn(referenceStepFieldInformation2); // reference for stepFailed
		
//		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(Field.fromDetectionData(failedStepFieldInformation),
//				Label.fromDetectionData(failedStepFieldInformation),
//				Field.fromDetectionData(referenceStepFieldInformation),
//				Label.fromDetectionData(referenceStepFieldInformation)).thenReturn(stepReferenceComparatorStep3);
//		PowerMockito.whenNew(StepReferenceComparator.class).withArguments(Field.fromDetectionData(failedStepFieldInformation),
//				Label.fromDetectionData(failedStepFieldInformation),
//				Field.fromDetectionData(referenceStepFieldInformation2),
//				Label.fromDetectionData(referenceStepFieldInformation2)).thenReturn(stepReferenceComparatorStep1);
		
		// comparison successful
		when(stepReferenceComparatorStep3.compare()).thenReturn(49); // bad comparison with step3 reference
		when(stepReferenceComparatorStep1.compare()).thenReturn(81); // good comparison with step1 reference
		
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");

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
		
//		PowerMockito.whenNew(StepReferenceComparator.class).withAnyArguments().thenReturn(stepReferenceComparatorStep3);
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");
		
		// no comparison done
//		PowerMockito.verifyNew(StepReferenceComparator.class, never()).withArguments(anyList(), anyList(), anyList(), anyList());
		
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
		when(serverConnector.getReferenceSnapshot(2)).thenReturn(null);
//		PowerMockito.whenNew(StepReferenceComparator.class).withAnyArguments().thenReturn(stepReferenceComparatorStep3);
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");
		
		// no comparison done
//		PowerMockito.verifyNew(StepReferenceComparator.class, never()).withArguments(anyList(), anyList(), anyList(), anyList());
		
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
		when(serverConnector.getReferenceSnapshot(2)).thenThrow(new SeleniumRobotServerException("error"));
//		PowerMockito.whenNew(StepReferenceComparator.class).withAnyArguments().thenReturn(stepReferenceComparatorStep3);
		List<ErrorCause> causes = new ErrorCauseFinder(testResult).compareStepInErrorWithReference("aa");
		
		// no comparison done
//		PowerMockito.verifyNew(StepReferenceComparator.class, never()).withArguments(anyList(), anyList(), anyList(), anyList());
		
		Assert.assertEquals(causes.size(), 0);
		Assert.assertTrue(TestNGResultUtils.isErrorCauseSearchedInReferencePicture(testResult));
	}
}
