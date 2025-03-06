package com.seleniumtests.it.driver;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.WebTestPageServer;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverPDFPage;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

public class TestPDFReaderWithBrowser  extends GenericDriverTest {

    private String localAddress;
    private WebTestPageServer server;

    @BeforeMethod(groups = "it", alwaysRun = true)
    public void init() throws Exception {
        server = new WebTestPageServer();
        server.exposeTestPage();
        localAddress = server.getLocalAddress();
    }

    @AfterMethod(groups = "it", alwaysRun = true)
    public void reset() {
        try {
            server.stopServer();
        } catch (Exception e) {
        }
    }

    @Test(groups = "it")
    public void testPDFReader() {
//        testPdfPage.clickPDF();
// ((CustomEventFiringWebDriver) driver).executeScript("fetch('http://10.166.162.235:64690/Test_PDF.pdf').then(res => res).then(console.log)");

        /*DevTools devTools = ((HasDevTools) ajc$this.driver.getOriginalDriver()).getDevTools();
        devTools.createSession(ajc$this.driver.getWindowHandle());
        try {
            Page.PrintToPDFResponse response = devTools.send(Page.printToPDF(Optional.of(false), Optional.of(false), Optional.of(false), Optional.of(1), Optional.of(8.5), Optional.of(11), Optional.of(0), Optional.of(0), Optional.of(0), Optional.of(0), Optional.of(""), Optional.of(""), Optional.of(""), Optional.of(false), Optional.of(Page.PrintToPDFTransferMode.RETURNASBASE64), Optional.of(false), Optional.of(false)));
            String out = response.getData();
            byte[] binaryData = Base64.getDecoder().decode(out);
            Files.write(Paths.get("D:\\tmp\\out.pdf"), binaryData);
        } finally {
            devTools.disconnectSession();
        }*/
    }

    @Test(groups = "it")
    public void testDownloadPDFChrome() {
        downloadPdf("chrome");
    }

    @Test(groups = "it")
    public void testDownloadPDFEdge() {
        downloadPdf("edge");
    }

    @Test(groups = "it")
    public void testDownloadPDFFirefox() {
        downloadPdf("firefox");
    }

    private void downloadPdf(String browser) {
        SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
        SeleniumTestsContextManager.getThreadContext().setBrowser(browser);

        DriverPDFPage testPdfPage = new DriverPDFPage(String.format("http://%s:%d/testpdf.html", localAddress, server.getServerHost().getPort()));
        testPdfPage.clickPDFToDownload();
        File file = TestTasks.getDownloadedFile("nom-du-fichier.pdf");
        Assert.assertTrue(file.exists());
    }


}
