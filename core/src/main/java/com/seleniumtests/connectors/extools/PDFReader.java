package com.seleniumtests.connectors.extools;

import com.seleniumtests.customexception.ScenarioException;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class aimed to handle PDF files
 */
public class PDFReader {

    File pdfFile;

    public PDFReader(File file) {
        this.pdfFile = file;
    }

    public PDFReader(InputStream inputStream) throws IOException {
        pdfFile = File.createTempFile("document", ".pdf");
        pdfFile.deleteOnExit();
        FileUtils.copyInputStreamToFile(inputStream, pdfFile);
    }

    /**
     * Return the text contained in PDF
     * @return
     */
    public String getText() {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper textStripper = new PDFTextStripper();
            textStripper.setSortByPosition(true);
            return textStripper.getText(document);
        } catch (IOException e) {
            throw new ScenarioException("Cannot read PDF file " + pdfFile.getAbsolutePath());
        }
    }
}
