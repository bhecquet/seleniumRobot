package com.seleniumtests.ut.connectors.extools;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.extools.PDFReader;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class TestPDFReader extends GenericTest {

    @Test
    public void testReadPdf() throws IOException {

        File pdfFile = createFileFromResource("tu/Test_PDF.pdf");
        String text = new PDFReader(pdfFile).getText();
        Assert.assertTrue(text.contains("Some table"));
    }
}
