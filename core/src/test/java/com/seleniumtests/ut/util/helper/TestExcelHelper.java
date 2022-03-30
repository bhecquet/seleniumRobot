package com.seleniumtests.ut.util.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.util.helper.ExcelHelper;

public class TestExcelHelper extends GenericTest {

	/**
	 * Test reading when headers are present
	 * We should get a map with each data row
	 * @throws IOException
	 */
	@Test(groups="ut")
    public void testReadSheet() throws IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("ti/excel/test.xlsx");
        List<Map<String, String>> sheetContent = new ExcelHelper().readSheet(is, "Feuil1", true);

        Assert.assertEquals(sheetContent.size(), 2); // 2 lines
        Assert.assertEquals(sheetContent.get(0).size(), 3); // 3 columns
        Assert.assertEquals(sheetContent.get(0).get("titre1"), "2"); // cell with a simple quote before number
        Assert.assertEquals(sheetContent.get(0).get("titre2"), "3");
        Assert.assertEquals(sheetContent.get(0).get("titre 3"), "toto");
        Assert.assertEquals(sheetContent.get(1).get("titre1"), "");
        Assert.assertEquals(sheetContent.get(1).get("titre2"), "4"); // cell in text format
        Assert.assertTrue("6,7".equals(sheetContent.get(1).get("titre 3")) || "6.7".equals(sheetContent.get(1).get("titre 3"))); // formula with floating number
    }
	
	/**
	 * Read a whole file
	 * @throws IOException
	 */
	@Test(groups="ut")
	public void testReadFile() throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("ti/excel/test2.xlsx");
		Map<String, List<Map<String, String>>> content = new ExcelHelper().read(is, true);
		
		Assert.assertEquals(content.size(), 2);
		Assert.assertTrue(content.containsKey("Feuil1"));
		Assert.assertTrue(content.containsKey("Feuil2"));
		
		List<Map<String, String>> sheetContent1 = content.get("Feuil1");
		Assert.assertEquals(sheetContent1.size(), 2); // 2 lines
		Assert.assertEquals(sheetContent1.get(0).size(), 3); // 3 columns
		Assert.assertEquals(sheetContent1.get(0).get("titre1"), "2"); // cell with a simple quote before number
		Assert.assertEquals(sheetContent1.get(0).get("titre2"), "3");
		Assert.assertEquals(sheetContent1.get(0).get("titre 3"), "toto");
		Assert.assertEquals(sheetContent1.get(1).get("titre1"), "");
		Assert.assertEquals(sheetContent1.get(1).get("titre2"), "4"); // cell in text format
		Assert.assertTrue("6,7".equals(sheetContent1.get(1).get("titre 3")) || "6.7".equals(sheetContent1.get(1).get("titre 3"))); // formula with floating number
	}
	
	/**
	 * Test reading a sheet by index when headers are present
	 * We should get a map with each data row
	 * @throws IOException
	 */
	@Test(groups="ut")
	public void testReadSheetByIndex() throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("ti/excel/test.xlsx");
		List<Map<String, String>> sheetContent = new ExcelHelper().readSheet(is, 0, true);
		
		Assert.assertEquals(sheetContent.size(), 2); // 2 lines
		Assert.assertEquals(sheetContent.get(0).size(), 3); // 3 columns
		Assert.assertEquals(sheetContent.get(0).get("titre1"), "2"); // cell with a simple quote before number
		Assert.assertEquals(sheetContent.get(0).get("titre2"), "3");
		Assert.assertEquals(sheetContent.get(0).get("titre 3"), "toto");
		Assert.assertEquals(sheetContent.get(1).get("titre1"), "");
		Assert.assertEquals(sheetContent.get(1).get("titre2"), "4"); // cell in text format
		Assert.assertTrue("6,7".equals(sheetContent.get(1).get("titre 3")) || "6.7".equals(sheetContent.get(1).get("titre 3"))); // formula with floating number
	}
	
	/**
	 * Test reading when we assume no header is present.
	 * We should get all rows
	 * @throws IOException
	 */
	@Test(groups="ut")
	public void testReadSheetNoHeader() throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("ti/excel/test.xlsx");
		List<Map<String, String>> sheetContent = new ExcelHelper().readSheet(is, "Feuil1", false);
		
		Assert.assertEquals(sheetContent.size(), 3); // 3 lines
		Assert.assertEquals(sheetContent.get(0).size(), 3); // 3 columns
		Assert.assertEquals(sheetContent.get(0).get("0"), "titre1");
		Assert.assertEquals(sheetContent.get(1).get("0"), "2"); 
		Assert.assertEquals(sheetContent.get(2).get("1"), "4");

	}
	
	@Test(groups="ut", expectedExceptions = ScenarioException.class)
    public void testReadSheetInvalidFile() throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("ti/excel/test.csv");
        new ExcelHelper().readSheet(is, "Feuil1", true);
	}
	
	@Test(groups="ut", expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = "Sheet Feuil10 does not exist")
	public void testReadSheetInvalidSheet() throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("ti/excel/test.xlsx");
		new ExcelHelper().readSheet(is, "Feuil10", true);
	}
	
	@Test(groups="ut", expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = "No data in sheet Feuil2")
	public void testReadSheetEmptySheet() throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("ti/excel/test.xlsx");
		new ExcelHelper().readSheet(is, "Feuil2", true);
	}
	
	@Test(groups="ut", expectedExceptions = ScenarioException.class)
	public void testReadSheetByIndexInvalidFile() throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("ti/excel/test.csv");
		new ExcelHelper().readSheet(is, 0, true);
	}
	
	@Test(groups="ut", expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = "Sheet numbered 2 does not exist: Sheet index \\(2\\) is out of range \\(0..1\\)")
	public void testReadSheetByIndexInvalidSheet() throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("ti/excel/test.xlsx");
		new ExcelHelper().readSheet(is, 2, true);
	}
	
	@Test(groups="ut", expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = "No data in sheet 1")
	public void testReadSheetByIndexEmptySheet() throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("ti/excel/test.xlsx");
		new ExcelHelper().readSheet(is, 1, true);
	}
}
