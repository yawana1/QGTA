/*
 * 
 */
package highLevelSummary;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

public class ReportReader {

	private TreeMap<String, List<HSSFRow>> hshData;

	public ReportReader(String strInFileName) {
		HSSFWorkbook objHSSFWorkbook;
		HSSFSheet objHSSFSheet;
		ArrayList<HSSFRow> lstHSSFRows;
		HSSFRow objHSSFRowHeaders;
		int intSheetsNumber;
		String strSheetName;
		int intMatchesNumber;
		int intRowsNumber;
		HSSFRow objHSSFRowData;
		String strCropName;
		String strTypeName;
		double douLocationsDeviation;
		double douEntryCorrelationEstimate;
		double douExlCorrelationRawMean;
		double douLocCorrelationEstimate;	
		double douLocCorrelationCv;	
		double douLocCorrelationCheckCv;	
		double douLocations;
		String strFirstModelConverged;
		String strSecondModelConverged;		
		boolean bolModelsCondition;
		
		this.hshData = new TreeMap<String, List<HSSFRow>>();
		try(InputStream objInputStream = new FileInputStream(strInFileName)) {
			objHSSFWorkbook = new HSSFWorkbook(objInputStream);
			for (intSheetsNumber = 0;
				 intSheetsNumber < (objHSSFWorkbook.getNumberOfSheets() - 1);
				 intSheetsNumber++) {
				objHSSFSheet = objHSSFWorkbook.getSheetAt(intSheetsNumber);
				strSheetName = objHSSFSheet.getSheetName();
				lstHSSFRows = new ArrayList<HSSFRow>();
				objHSSFRowHeaders = objHSSFSheet.getRow(0);
				lstHSSFRows.add(objHSSFRowHeaders);
				intMatchesNumber = 0;
				for (intRowsNumber = 1;
					 intRowsNumber < objHSSFSheet.getPhysicalNumberOfRows();
					 intRowsNumber++) {
					objHSSFRowData = objHSSFSheet.getRow(intRowsNumber);
					strCropName = objHSSFRowData.getCell(0).getStringCellValue().trim().toLowerCase();
					strTypeName = objHSSFRowData.getCell(1).getStringCellValue().trim().toLowerCase();
					if (((strCropName.equals("corn")) && (strTypeName.equals("silage"))) || 
						((strCropName.equals("corn")) && (strTypeName.equals("yield trial"))) ||
						(strCropName.equals("soybean"))) {
						
						if (objHSSFRowData.getCell(8).getCellType() == Cell.CELL_TYPE_NUMERIC) {
							douLocationsDeviation = objHSSFRowData.getCell(8).getNumericCellValue();
						}
						else {
							douLocationsDeviation = -1.0;
						}

						if (objHSSFRowData.getCell(17).getCellType() == Cell.CELL_TYPE_NUMERIC) {
							douEntryCorrelationEstimate = objHSSFRowData.getCell(17).getNumericCellValue();
						}
						else { 
							douEntryCorrelationEstimate = 0;
						}
						
						if (objHSSFRowData.getCell(20).getCellType() == Cell.CELL_TYPE_NUMERIC) {
							douExlCorrelationRawMean = objHSSFRowData.getCell(20).getNumericCellValue();
						}
						else {
							douExlCorrelationRawMean = 0;
						}
						
						if (objHSSFRowData.getCell(23).getCellType() == Cell.CELL_TYPE_NUMERIC) {
							douLocCorrelationEstimate = objHSSFRowData.getCell(23).getNumericCellValue();	
						}
						else {
							douLocCorrelationEstimate = 0;
						}
						
						if (objHSSFRowData.getCell(24).getCellType() == Cell.CELL_TYPE_NUMERIC) {						
							douLocCorrelationCv = objHSSFRowData.getCell(24).getNumericCellValue();
						}
						else {
							douLocCorrelationCv = 0;
						}
						
						if (objHSSFRowData.getCell(25).getCellType() == Cell.CELL_TYPE_NUMERIC) {												
							douLocCorrelationCheckCv = objHSSFRowData.getCell(25).getNumericCellValue();
						}
						else {
							douLocCorrelationCheckCv = 0;
						}
						
						if (objHSSFRowData.getCell(7).getCellType() == Cell.CELL_TYPE_NUMERIC) {
							douLocations = objHSSFRowData.getCell(7).getNumericCellValue();
						}
						else {
							douLocations = -1.0;
						}
						
						if ((douLocations > 1d) &&  
							 ((douEntryCorrelationEstimate < 0.97) || 
							  (douExlCorrelationRawMean < 0.97)  ||
							  (douLocCorrelationEstimate < 0.97) ||
							  (douLocCorrelationCv < 0.97) ||
							  (douLocCorrelationCheckCv < 0.97))) {
							lstHSSFRows.add(objHSSFRowData);
							intMatchesNumber++;
						}
						else {
							strFirstModelConverged = objHSSFRowData.getCell(13).getStringCellValue().toLowerCase().trim();
							strSecondModelConverged = objHSSFRowData.getCell(16).getStringCellValue().toLowerCase().trim();
							bolModelsCondition = (strFirstModelConverged.equals("no")) || 
									             (strSecondModelConverged.equals("no"));
							if ((douLocations > 1d) && 
							    (bolModelsCondition)) {
								lstHSSFRows.add(objHSSFRowData);
								intMatchesNumber++;
							}
						}
					}
				}
				if (0 < intMatchesNumber) {
					this.hshData.put(strSheetName, 
						         	 lstHSSFRows);
				}
			}
		}
		catch (IOException e) {			
			throw new RuntimeException();
		}
	}
	
	public TreeMap<String, List<HSSFRow>> getData() {
		return this.hshData;
	}
}
