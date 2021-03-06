/*
 * 
 */
package io;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ReportWriter {
	
	static Logger objLogger = Logger.getLogger(ReportWriter.class.getName());
	private HSSFWorkbook objHSSFWorkbook;
	private HSSFCellStyle objHeaderHSSFCellStyle;
	private HSSFFont objHeaderHSSFFont;
	private HSSFCellStyle objLiteralHSSFCellStyle; 
	private HSSFCellStyle objNumericHSSFCellStyle; 
	private HSSFSheet objHSSFSheet;
	private int intRowNumber;
	private HSSFRow objHSSFRow;
	private int intColumnsNumber;
	private HSSFCell objHSSFCell;	
	
	public ReportWriter() {
		this.objHSSFWorkbook = new HSSFWorkbook();
		this.objHeaderHSSFCellStyle = this.objHSSFWorkbook.createCellStyle();
		this.objHeaderHSSFCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		this.objHeaderHSSFFont = this.objHSSFWorkbook.createFont();
		this.objHeaderHSSFFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		this.objHeaderHSSFCellStyle.setFont(this.objHeaderHSSFFont);
		this.objLiteralHSSFCellStyle = this.objHSSFWorkbook.createCellStyle();
		this.objLiteralHSSFCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		this.objNumericHSSFCellStyle = this.objHSSFWorkbook.createCellStyle();
		this.objNumericHSSFCellStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
	}
	
	public void createSheet(String strInTraitName,
			                String[] strArrayInHeaders) {
		int intCellNumber;

		this.objHSSFSheet = this.objHSSFWorkbook.createSheet(strInTraitName);
		this.intRowNumber = 0;
		this.objHSSFRow = this.objHSSFSheet.createRow(this.intRowNumber);
		for (intCellNumber = 0;
			 intCellNumber < strArrayInHeaders.length;
			 intCellNumber++) {
			this.objHSSFCell = this.objHSSFRow.createCell((Integer)intCellNumber);
			this.objHSSFCell.setCellValue((String)("   " + strArrayInHeaders[intCellNumber] + "   "));
			this.objHSSFCell.setCellStyle(this.objHeaderHSSFCellStyle);
		}
		this.intColumnsNumber = strArrayInHeaders.length; 
	}
	
	public void writeRowData(Object[] objArrayInRowData) {
		int intCellNumber;
		
		this.intRowNumber++;
		this.objHSSFRow = this.objHSSFSheet.createRow(this.intRowNumber);
		for (intCellNumber = 0;
			 intCellNumber < objArrayInRowData.length;
			 intCellNumber++) {
			this.objHSSFCell = this.objHSSFRow.createCell((Integer)intCellNumber);
			if (objArrayInRowData[intCellNumber] instanceof Integer) {
				this.objHSSFCell.setCellValue((Integer)objArrayInRowData[intCellNumber]);
				this.objHSSFCell.setCellStyle(this.objNumericHSSFCellStyle);
			}
			else {
				if (objArrayInRowData[intCellNumber] instanceof Double) {
					this.objHSSFCell.setCellValue((Double)objArrayInRowData[intCellNumber]);
					this.objHSSFCell.setCellStyle(this.objNumericHSSFCellStyle);
				}
				else {
					if (objArrayInRowData[intCellNumber] instanceof String) {
						this.objHSSFCell.setCellValue((String)objArrayInRowData[intCellNumber]);
						this.objHSSFCell.setCellStyle(this.objLiteralHSSFCellStyle);
					}	
				}
			}
		}		
	}
	
	public void closeSheet() {
		int intColumnIndex;
		
		for (intColumnIndex = 0;
			 intColumnIndex	< this.intColumnsNumber;
			 intColumnIndex++) {
			this.objHSSFSheet.autoSizeColumn(intColumnIndex);
		}
	}
	
	public void close(String strInPathDataFile) {		
		try(FileOutputStream objFileOutputStream = new FileOutputStream(new File(strInPathDataFile));) { 
			this.objHSSFWorkbook.write(objFileOutputStream);
		} 
		catch (Exception e) {
			objLogger.error("ReportWriter.close", 
			                e);
			throw new RuntimeException();
		}
	}
}