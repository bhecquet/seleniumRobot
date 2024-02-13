package com.seleniumtests.util.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.seleniumtests.customexception.ScenarioException;

public class ExcelHelper {
	
//	static {
//        org.apache.logging.log4j.core.config.Configurator.setLevel("org.apache.poi.util.XMLHelper", Level.ERROR);
//    }
	

    private File excelFile;
	
	public ExcelHelper(File excelFile) {
        this.excelFile = excelFile;
    }
	public ExcelHelper() {
		this.excelFile = null;
	}

	/**
     * Read a sheet in the excel file
     * @param sheetName
     * @return
     * @throws IOException
     */
	public List<Map<String, String>> readSheet(String sheetName, boolean headerPresent) throws IOException {
		FileInputStream fis = new FileInputStream(excelFile);
		return readSheet(fis, sheetName, headerPresent);
	}
	
	/**
     * Read a sheet by index in the excel file
     * @param sheetName
     * @return
     * @throws IOException
     */
	public List<Map<String, String>> readSheet(int sheetIndex, boolean headerPresent) throws IOException {
		FileInputStream fis = new FileInputStream(excelFile);
		return readSheet(fis, sheetIndex, headerPresent);
	}
	
	/**
	 * Read the whole excel file
	 * @param headerPresent
	 * @return
	 * @throws IOException
	 */
	public Map<String, List<Map<String, String>>> read(boolean headerPresent) throws IOException {
        FileInputStream fis = new FileInputStream(excelFile);
        return read(fis, headerPresent);
    }
	

    public List<Map<String, String>> readSheet(Sheet sheet, boolean headerPresent, FormulaEvaluator formulaEvaluator, DataFormatter dataFormatter) {
        List<Map<String, String>> content = new ArrayList<>();

        int firstRow = sheet.getFirstRowNum();
        int lastRow = sheet.getLastRowNum();
        int firstColumn = 100000;
        int lastColumn = 0;

        Row headerRow = sheet.getRow(firstRow);
        List<String> headers = new ArrayList<>();

        // sheet is empty
        if (headerRow == null) {
            return null;
        }

        for (Cell cell: headerRow) {
            firstColumn = Math.min(cell.getColumnIndex(), firstColumn);
            lastColumn = Math.max(cell.getColumnIndex(), lastColumn);

        	if (headerPresent) {
        		headers.add(readCell(formulaEvaluator, dataFormatter, cell));
        	} else {
        		headers.add(Integer.toString(cell.getColumnIndex()));
        	}
        }

        firstRow = headerPresent ? firstRow + 1: firstRow;
        int colIdx = 0;
        
        for (int rowIdx = firstRow; rowIdx <= lastRow; rowIdx++) {
        	try {
                Row row = sheet.getRow(rowIdx);
                if (row == null) { // nothing has been written in cell, it's null
                    continue;
                }

                int empytCells = 0;
                Map<String, String> rowContent = new LinkedHashMap<>();

                for (colIdx = firstColumn; colIdx <= lastColumn; colIdx++) {

                    Cell cell = row.getCell(colIdx);

                    if (cell == null) { // cell is empty, it's never been used
                        rowContent.put(headers.get(colIdx), "");
                        empytCells++;
                    } else {
                        String cellContent = readCell(formulaEvaluator, dataFormatter, cell);
                        if (cellContent != null && cellContent.isEmpty()) {
                            empytCells++;
                        }
                        rowContent.put(headers.get(colIdx), cellContent);
                    }
                }
                
                if (empytCells < rowContent.size()) {
                    content.add(rowContent);
                }
        	} catch (Exception e) {
                throw new ScenarioException(String.format("problem reading Excel sheet %s, line %d, column %d", sheet.getSheetName(), rowIdx + 1, colIdx + 1), e);
            }
        }
        
        return content;
    }

    /**
     * Read a sheet by index in the excel file
     * @param fis
     * @param sheetName
     * @return
     * @throws IOException
     */
    public List<Map<String, String>> readSheet(InputStream fis, int sheetIndex, boolean headerPresent) throws IOException {
    	try (Workbook workbook = WorkbookFactory.create(fis)) {
    		FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            DataFormatter dataFormatter = new DataFormatter();

            Sheet sheet = workbook.getSheetAt(sheetIndex);

            List<Map<String, String>> sheetContent = readSheet(sheet, headerPresent, formulaEvaluator, dataFormatter);
            if (sheetContent == null) {
            	throw new ScenarioException(String.format("No data in sheet %d", sheetIndex));
            }
            
            return sheetContent;
    		
    	} catch (IOException e) {
        	throw new ScenarioException(e.getMessage());
        } catch (IllegalArgumentException e) {
        	throw new ScenarioException(String.format("Sheet numbered %d does not exist: %s", sheetIndex, e.getMessage()));
        }
    }
    
    /**
     * Read a sheet by name in the excel file
     * @param fis
     * @param sheetName
     * @return
     * @throws IOException
     */
    public List<Map<String, String>> readSheet(InputStream fis, String sheetName, boolean headerPresent) throws IOException {

        try (Workbook workbook = WorkbookFactory.create(fis)) {
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            DataFormatter dataFormatter = new DataFormatter();

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
            	throw new ScenarioException(String.format("Sheet %s does not exist", sheetName));
            }

            List<Map<String, String>> sheetContent = readSheet(sheet, headerPresent, formulaEvaluator, dataFormatter);
            if (sheetContent == null) {
            	throw new ScenarioException(String.format("No data in sheet %s", sheetName));
            }
            
            return sheetContent;
        } catch (IOException e) {
        	throw new ScenarioException(e.getMessage());
        }
    }
    
    /**
     * Read an excel file and returns the content
     * @param fis
     * @param headerPresent
     * @return
     * @throws IOException
     */
    public Map<String, List<Map<String, String>>> read(InputStream fis, boolean headerPresent) throws IOException {

    	Map<String, List<Map<String, String>>> content = new LinkedHashMap<>();
    	
        try (Workbook workbook = WorkbookFactory.create(fis)) {
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            DataFormatter dataFormatter = new DataFormatter();
            
            for (int sheetIdx = workbook.getNumberOfSheets() - 1; sheetIdx >= 0; sheetIdx--) {
            	Sheet sheet = workbook.getSheetAt(sheetIdx);
            	
            	List<Map<String, String>> sheetContent = readSheet(sheet, headerPresent, formulaEvaluator, dataFormatter);
            	if (sheetContent == null) {
            		continue;
            	}
            	content.put(sheet.getSheetName(), sheetContent);
            }
            
            return content;
        } catch (IOException e) {
        	throw new ScenarioException(e.getMessage());
        }
    }

    /**
     * read a cell
     * @param formulaEvaluator
     * @param cell
     * @return
     */
    private String readCell(FormulaEvaluator formulaEvaluator, DataFormatter dataFormatter, Cell cell) {
        return dataFormatter.formatCellValue(cell, formulaEvaluator);
    }
}
