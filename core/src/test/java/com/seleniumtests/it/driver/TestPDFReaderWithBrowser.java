package com.seleniumtests.it.driver;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import org.testng.annotations.Test;

public class TestPDFReaderWithBrowser  extends GenericMultiBrowserTest {


    public TestPDFReaderWithBrowser() throws Exception {
        super(BrowserType.CHROME, "DriverPDFPage");
    }

    @Test
    public void testPDFReader() throws Exception {
        testPdfPage.clickPDF();
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

}
