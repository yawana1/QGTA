/*
 * 
 */
package highLevelSummary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ReportWriter {
	
	public ReportWriter(String strInFileName, TreeMap<String, List<HSSFRow>> mapInData) {
		Collection<HLSummary> hlSummaries;
		
		HSSFCellStyle objHeaderHSSFCellStyle = null;
		HSSFCellStyle objLiteralHSSFCellStyle = null;
		HSSFCellStyle objNumericHSSFCellStyle = null;
		boolean bolHeaders;
		List<HSSFRow> lstHSSFRows;
		int intArrayListIndex;
		HSSFRow objOutputHSSFRow;
		int intCellsNumber;
		HSSFCell objHSSFCell;
		int intColumnsNumber;
		int intColumnIndex;
		
		hlSummaries = new ArrayList<>();
		hlSummaries.add(new CornGrain());
		hlSummaries.add(new CornSilage());
		hlSummaries.add(new Soybean());

		intColumnsNumber = 0;
		for(Entry<String, List<HSSFRow>> entry : mapInData.entrySet()) {
			bolHeaders = true;
			String sheetName = entry.getKey();
			lstHSSFRows = entry.getValue();
			HSSFSheet newSheet = null;
			
			for (intArrayListIndex = 0; intArrayListIndex < lstHSSFRows.size(); intArrayListIndex++) {
				HSSFRow orginalRow = lstHSSFRows.get(intArrayListIndex);
				
				//write headers to each summary
				if (bolHeaders) {
					for(HLSummary hlSummary : hlSummaries){
						if(hlSummary.createSummary(sheetName)){
							intColumnsNumber = hlSummary.addHeaders(sheetName, orginalRow);
						}
					}
					bolHeaders = false;
				}
				else{
					
					//get which high level Summary and which sheet this row of data will be placed on
					for(HLSummary hlSummary : hlSummaries){					
						if(hlSummary.addToSummary(sheetName, orginalRow)){
							newSheet = hlSummary.getSheet(sheetName);
							objHeaderHSSFCellStyle = hlSummary.getHeaderCellStyle();
							objLiteralHSSFCellStyle = hlSummary.getLiteralCellStyle();
							objNumericHSSFCellStyle = hlSummary.getNumericCellStyle();
							break;
						}
					}
	
					if(newSheet != null){					
						//write data rows
						objOutputHSSFRow = newSheet.createRow(intArrayListIndex);
						for (intCellsNumber = 0; intCellsNumber < lstHSSFRows.get(intArrayListIndex).getPhysicalNumberOfCells(); intCellsNumber++) {
							HSSFRow row = lstHSSFRows.get(intArrayListIndex);
							objHSSFCell = objOutputHSSFRow.createCell(intCellsNumber);
							if (row.getCell(intCellsNumber).getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
								objHSSFCell.setCellValue(lstHSSFRows.get(intArrayListIndex).getCell(intCellsNumber).getNumericCellValue());
								objHSSFCell.setCellStyle(objNumericHSSFCellStyle);
							}
							else {
								if (row.getCell(intCellsNumber).getCellType() == HSSFCell.CELL_TYPE_STRING) {
									objHSSFCell.setCellValue(lstHSSFRows.get(intArrayListIndex).getCell(intCellsNumber).getStringCellValue());
									objHSSFCell.setCellStyle(objLiteralHSSFCellStyle);
								}
							}
						}
					}
				}
			}
		}

		//output to files
		try {
			for(HLSummary hlSummary : hlSummaries){
				
				//format sheets.
				HSSFWorkbook workbook = hlSummary.getWorkbook();
				List<Integer> removeIndexes = new ArrayList<>();
				for(int i = 0; i < workbook.getNumberOfSheets(); i++){
					
					HSSFSheet sheet =  workbook.getSheetAt(i);
					//remove sheets with only headers
					if(sheet.getPhysicalNumberOfRows() <= 1){
						removeIndexes.add(i);
					}
					else{
						for (intColumnIndex = 0; intColumnIndex	< intColumnsNumber; intColumnIndex++) {
							sheet.autoSizeColumn(intColumnIndex);
						}
					}
				}
				
				//need to remove last sheet first.
				Collections.sort(removeIndexes, Collections.reverseOrder());
				for(int i : removeIndexes){
					workbook.removeSheetAt(i);
				}
				
				//skip blank sheets
				if(workbook.getNumberOfSheets() != 0){
				
					OutputStream outputStream = null;
					try{
						String reportFileName = strInFileName.substring(0, strInFileName.lastIndexOf(".")+1) + hlSummary.getFileNameAddition() + ".xls";
						outputStream = new FileOutputStream(new File(reportFileName));
						workbook.write(outputStream);
					}
					catch(Exception e){
						throw e;
					}
					finally{
						if(outputStream != null){
							outputStream.close();
						}
					}
				}
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}