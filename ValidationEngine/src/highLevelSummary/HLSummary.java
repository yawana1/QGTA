/*
 * 
 */
package highLevelSummary;

import java.util.Arrays;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public abstract class HLSummary {

	protected String crop;
	protected String classType;
	protected String[] traits;
	private HSSFWorkbook workbook;
	private HSSFCellStyle headerCellStyle;
	private HSSFFont headerFont;
	private HSSFCellStyle literalCellStyle;
	private HSSFCellStyle numericCellStyle;
	private boolean addHeader = true; 
	
	public HLSummary(){
		workbook = new HSSFWorkbook();
		
		//column styles
		headerCellStyle = workbook.createCellStyle(); 
		headerCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		headerFont = workbook.createFont();
		headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		headerCellStyle.setFont(headerFont);
		
		literalCellStyle = workbook.createCellStyle();
		literalCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		
		numericCellStyle = workbook.createCellStyle();
		numericCellStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
	}
	
	public String getCrop() {
		return crop;
	}

	public void setCrop(String crop) {
		this.crop = crop;
	}

	public String getClassType() {
		return classType;
	}

	public void setClassType(String classType) {
		this.classType = classType;
	}

	public String[] getTraits() {
		return traits;
	}

	public void setTraits(String[] traits) {
		this.traits = traits;
	}

	public String getFileNameAddition() {
		return crop+"."+classType;
	}

	public HSSFWorkbook getWorkbook() {
		return workbook;
	}

	public void setWorkbook(HSSFWorkbook workbook) {
		this.workbook = workbook;
	}
	
	public HSSFCellStyle getHeaderCellStyle() {
		return headerCellStyle;
	}

	public void setHeaderCellStyle(HSSFCellStyle headerCellStyle) {
		this.headerCellStyle = headerCellStyle;
	}

	public HSSFCellStyle getLiteralCellStyle() {
		return literalCellStyle;
	}

	public void setLiteralCellStyle(HSSFCellStyle literalCellStyle) {
		this.literalCellStyle = literalCellStyle;
	}

	public HSSFCellStyle getNumericCellStyle() {
		return numericCellStyle;
	}

	public void setNumericCellStyle(HSSFCellStyle numericCellStyle) {
		this.numericCellStyle = numericCellStyle;
	}

	public boolean isAddHeader() {
		return addHeader;
	}

	public void setAddHeader(boolean addHeader) {
		this.addHeader = addHeader;
	}

	public boolean createSummary(String trait){
		boolean result = false;
		
		if(Arrays.binarySearch(traits, trait) >= 0){
			result = true;
		}
		
		return result;
	}
	
	public boolean addToSummary(String trait, HSSFRow row){
		boolean result = false;
		
		String crop = row.getCell(0).toString();
		String classType = row.getCell(1).toString();
			
		crop = crop.trim();
		classType = classType.trim();
			
		if(		this.crop.trim().equals(crop)
			&& 	this.classType.trim().equals(classType)
			&& Arrays.binarySearch(traits, trait) >= 0)
		{
			result = true;
		}
		
		return result;
	}
	
	public HSSFSheet getSheet(String trait){
		HSSFSheet sheet = workbook.getSheet(trait);
		
		if(sheet == null){
			sheet = workbook.createSheet(trait);
		}
		return sheet;
	}
	
	public int addHeaders(String sheetName, HSSFRow headerRow){
		HSSFRow newRow = getSheet(sheetName).createRow(0);
		
		for (int cellsNumber = 0; cellsNumber < headerRow.getPhysicalNumberOfCells(); cellsNumber++) {
			HSSFCell objHSSFCell = newRow.createCell(cellsNumber); 
			objHSSFCell.setCellValue("   " + headerRow.getCell(cellsNumber).getStringCellValue() + "   ");
			objHSSFCell.setCellStyle(headerCellStyle);
		}
		return newRow.getPhysicalNumberOfCells();
	}
}