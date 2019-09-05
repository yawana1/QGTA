/*
 * 
 */
package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ReportWriterPerTrait {
	
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
	private String outputFileName;
	
	public ReportWriterPerTrait(String strInOutputFileName, String[] strArrayInHeaders) {
		
		this.outputFileName = strInOutputFileName;
		
		File phenoFile = new File(this.outputFileName);
		if(!phenoFile.exists()){
		                	try{
		                		phenoFile.createNewFile();
		                		
		                		//int intCellNumber;

		                		  BufferedWriter outputWriter = null;
		                		  outputWriter = new BufferedWriter(new FileWriter(this.outputFileName));
		                		  
		                		  String joinedString = StringUtils.join(strArrayInHeaders, ",");
		                		  outputWriter.write(joinedString+System.getProperty("line.separator"));
		                		
		                		  outputWriter.close();
		                		
//		                		strArrayInHeaders.toString().
		                		
//		                		for (intCellNumber = 0;
//		                			 intCellNumber < strArrayInHeaders.length;
//		                			 intCellNumber++) {
//		                			strArrayInHeaders[intCellNumber] + "   "));
//		                			this.objHSSFCell.setCellStyle(this.objHeaderHSSFCellStyle);
//		                		}

		                		
		                		
		                		
		                	}catch(IOException e){
		                		System.out.println("Could not create the file");
		                		objLogger.error("Could not create the file");
								throw new RuntimeException(
										"Could not create the file");
		                	}
            	
		                	
		                	
//		                try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(phenoFile, true)))) {
//		                    out.println([HEADER]);
//		                }catch (IOException e) {
		                    //exception handling left as an exercise for the reader
//		                }
		}

		
	}
	
	public void writeRowData(Object[] objArrayInRowData) {
		
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(this.outputFileName, true)))) {
			String joinedString = StringUtils.join(objArrayInRowData, ",");
			out.write(joinedString+System.getProperty("line.separator"));
		}catch (IOException e) {
		    System.out.println("Could not write in file.");
		    objLogger.error("Could not write in file");
			throw new RuntimeException(
					"Could not write in file");
		}

		
	}
	
	
	public void close(String strInPathDataFile) {		

	}
}