package com.seleniumtests.it.core.testanalysis;

import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.connectors.selenium.fielddetector.FieldDetectorConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.it.reporter.ReporterTest;
import com.seleniumtests.util.video.VideoCaptureMode;

public class TestErrorCauseFInder extends ConnectorsTest {
	
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


}
