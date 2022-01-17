package com.seleniumtests.it.core.testanalysis;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.connectors.selenium.fielddetector.FieldDetectorConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.it.reporter.ReporterTest;
import com.seleniumtests.util.video.VideoCaptureMode;

public class TestErrorCauseFInder extends ReporterTest {
	
	private static final String DETECT_FIELD_REPLY = "{" // add it to a formatting with picture name
			+ "	\"error\": null,"
			+ "	\"%s\": {"
			+ "		\"fields\": [{"
			+ "			\"bottom\": 220,"
			+ "			\"class_id\": 4,"
			+ "			\"class_name\": \"field_with_label\","
			+ "			\"height\": 36,"
			+ "			\"left\": 491,"
			+ "			\"related_field\": {"
			+ "				\"bottom\": 228,"
			+ "				\"class_id\": 0,"
			+ "				\"class_name\": \"field\","
			+ "				\"height\": 26,"
			+ "				\"left\": 495,"
			+ "				\"related_field\": null,"
			+ "				\"right\": 642,"
			+ "				\"text\": null,"
			+ "				\"top\": 202,"
			+ "				\"width\": 147,"
			+ "				\"with_label\": false"
			+ "			},"
			+ "			\"right\": 705,"
			+ "			\"text\": \"Date de naissance\","
			+ "			\"top\": 184,"
			+ "			\"width\": 214,"
			+ "			\"with_label\": true"
			+ "		},"
			+ "		{"
			+ "			\"bottom\": 204,"
			+ "			\"class_id\": 0,"
			+ "			\"class_name\": \"field\","
			+ "			\"height\": 21,"
			+ "			\"left\": 611,"
			+ "			\"related_field\": null,"
			+ "			\"right\": 711,"
			+ "			\"text\": null,"
			+ "			\"top\": 183,"
			+ "			\"width\": 100,"
			+ "			\"with_label\": false"
			+ "		}],"
			+ "		\"labels\": [{"
			+ "			\"bottom\": 20,"
			+ "			\"height\": 15,"
			+ "			\"left\": 159,"
			+ "			\"right\": 460,"
			+ "			\"text\": \"Dossier Client S3]HOMOLOGATIO\","
			+ "			\"top\": 5,"
			+ "			\"width\": 301"
			+ "		},"
			+ "		{"
			+ "			\"bottom\": 262,"
			+ "			\"height\": 8,"
			+ "			\"left\": 939,"
			+ "			\"right\": 999,"
			+ "			\"text\": \"Rechercher\","
			+ "			\"top\": 254,"
			+ "			\"width\": 60"
			+ "		}]"
			+ "	}"
			+ "}";
	
	private static final String DETECT_ERROR_REPLY = "{" 
			+ "	\"error\": null,"
			+ "	\"%s\": {"
			+ "		\"fields\": ["
			+ "		{"
			+ "			\"bottom\": 204,"
			+ "			\"class_id\": 0,"
			+ "			\"class_name\": \"error_field\","
			+ "			\"height\": 21,"
			+ "			\"left\": 611,"
			+ "			\"related_field\": null,"
			+ "			\"right\": 711,"
			+ "			\"text\": null,"
			+ "			\"top\": 183,"
			+ "			\"width\": 100,"
			+ "			\"with_label\": false"
			+ "		}],"
			+ "		\"labels\": [{"
			+ "			\"bottom\": 20,"
			+ "			\"height\": 15,"
			+ "			\"left\": 159,"
			+ "			\"right\": 460,"
			+ "			\"text\": \"Dossier Client S3]HOMOLOGATIO\","
			+ "			\"top\": 5,"
			+ "			\"width\": 301"
			+ "		},"
			+ "		{"
			+ "			\"bottom\": 262,"
			+ "			\"height\": 8,"
			+ "			\"left\": 939,"
			+ "			\"right\": 999,"
			+ "			\"text\": \"Rechercher\","
			+ "			\"top\": 254,"
			+ "			\"width\": 60"
			+ "		}]"
			+ "	}"
			+ "}";
	
	private static final String DETECT_NO_ERROR_REPLY = "{" 
			+ "	\"error\": null,"
			+ "	\"%s\": {"
			+ "		\"fields\": [],"
			+ "		\"labels\": [{"
			+ "			\"bottom\": 20,"
			+ "			\"height\": 15,"
			+ "			\"left\": 159,"
			+ "			\"right\": 460,"
			+ "			\"text\": \"Dossier Client S3]HOMOLOGATIO\","
			+ "			\"top\": 5,"
			+ "			\"width\": 301"
			+ "		},"
			+ "		{"
			+ "			\"bottom\": 262,"
			+ "			\"height\": 8,"
			+ "			\"left\": 939,"
			+ "			\"right\": 999,"
			+ "			\"text\": \"Rechercher\","
			+ "			\"top\": 254,"
			+ "			\"width\": 60"
			+ "		}]"
			+ "	}"
			+ "}";
	
	
	/**
	 * Test  when there is an error in last step, check we display the analysis in report
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"}, enabled=true)
	public void testErrorInLastStep(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.IMAGE_FIELD_DETECTOR_SERVER_URL, SERVER_URL);
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, SERVER_URL);
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.FIND_ERROR_CAUSE, "true");
			System.setProperty(SeleniumTestsContext.RANDOM_IN_ATTACHMENT_NAME, "false");
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, VideoCaptureMode.ON_ERROR.toString());
			
			configureMockedSnapshotServerConnection();
			createServerMock("GET", FieldDetectorConnector.STATUS_URL, 200, "OK");		
			createServerMock("POST", FieldDetectorConnector.DETECT_ERROR_URL, 200, String.format(DETECT_ERROR_REPLY, "testImageDetection_5-1_Test_end-.png"));		
			createServerMock("POST", FieldDetectorConnector.DETECT_URL, 200, String.format(DETECT_FIELD_REPLY, "testImageDetection_4-1__clickErrorButton_-.jpg"));		
			
			ReporterTest.executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.NONE,  new String[] {"testImageDetection"});
			
			// check the error cause is displayed at the top of the report
			String output = readTestMethodResultFile("testImageDetection");
			Assert.assertTrue(output.contains("<th>Possible error causes</th><td><ul><li>Field in error: At least one field in error on step '_clickErrorButton '</li></ul></td>"));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.IMAGE_FIELD_DETECTOR_SERVER_URL);
			System.clearProperty(SeleniumTestsContext.FIND_ERROR_CAUSE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.RANDOM_IN_ATTACHMENT_NAME);
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
		}
		
	}
	
	// test quand on ne demande pas à trouver les causes d'erreurs
	// teste quand il n'y a pas de cause
	// teste avec une erreur dans une des étapes (reference)
	// Si le test est KO à cause d'une assertion, on ne fait pas la recherche des causes, car a priori, on suppose que c'est juste un problème de valeur.
	// Prendre en compte RootCause et RootCauseDetails du testStep: on l'ajoute à la description de "ErrorCause"
	// Quand il y a plusieurs erreurs, quel est le type de l'erreur remontée ?

}
