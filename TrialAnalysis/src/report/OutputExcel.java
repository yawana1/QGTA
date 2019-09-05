package report;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import utils.Funcs;
import utils.Globals.SummaryType;
import vat.Vat;
import vat.VatSummary;
import data.XML;
import data.xml.objects.Trial;


public class OutputExcel implements Output{
	
	static Logger log = Logger.getLogger(OutputExcel.class.getName());

	protected Workbook expWB; 
	
	public OutputExcel(){}

	public void runOutput(Vat vat, Trial trial, ReportOutput reportOutput) {
		String fileName = trial.getReportDirectory(false) +"/"+
							Trial.cleanName(trial.getTrialName())+
							trial.getFileExtention("output_spreadsheet_file");
		writeXls(vat,reportOutput,trial, fileName);
	}
	
	@SuppressWarnings("static-access")
	public void writeXls(Vat vat, ReportOutput reportOutput,Trial trial, String filename){
		expWB = new HSSFWorkbook();
		Vat vatXLS = new Vat();
		XML.INSTANCE.deserialize(trial.getXlsColumnFile(), vatXLS);
		
		List<String> coreChecks = trial.getFbks().getCoreChecks();
		List<String> bmrChecks = trial.getFbks().getBmrChecks();
		List<String> geneticChecks = trial.getFbks().getGeneticChecks();
		List<String> performanceChecks = trial.getFbks().getPerformanceChecks();
		
		Map<String, List<Object>> fbkMap = trial.getFbks().getColMap();
		Map<SummaryType,VatSummary> summaryMap = vat.getVatSummaries(); 
		for(Map.Entry<SummaryType, VatSummary> summaryTypes: summaryMap.entrySet()){
			try{
				boolean multiFilter = false;
				SummaryType type = summaryTypes.getKey();
				VatSummary vatSum = vatXLS.get(type);//note: cannot get the vatSummary out of this map b/c need excel output, not vat output
				
				if(reportOutput.get(type)!=null && vatSum!=null){
					/*
					first find the summary filter (this needs to be changed to get it off the type, but for now we can get it off the first summary
					For example, entrySummary has a filter of genoId and locSummary has a filter of locId.
					More complicated summaries like exlSummary, has 2 filters (locId and genoId) so filter is delimited as locId-genoId
					*/
					String filter = getFilterString(reportOutput,type);
					
					//this is temporary until Scott finishes his code for outlier
					if(type.name().equals("outlierSummary") && filter==null){
						filter = "rowId";
					}
					
					if(filter!=null && filter.contains("-")){
						multiFilter = true;
					}
					
					//create one sheet per SummaryType
					Sheet sheet = expWB.createSheet(type.name());
		
					//freeze only the top row
					sheet.createFreezePane(0,1,0,1);
					
					//write out column header
//					if(vatSum!=null){
						List<String> vatSummaryCols = vatSum.getProcColumns();
						Row header = sheet.createRow((short)0);
						for(int x=0; x<vatSummaryCols.size(); x++){
							String columnName = vatSummaryCols.get(x).toString();
							Cell headerCell = header.createCell(x);
							headerCell.setCellValue(columnName);
							
							CellStyle cellStyle = expWB.createCellStyle();
							cellStyle.setFillBackgroundColor(HSSFColor.LIGHT_YELLOW.index);
							cellStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
							cellStyle.setFillPattern(cellStyle.SOLID_FOREGROUND);
							headerCell.setCellStyle(cellStyle);
						}
						
						//first put the unique keys in the map (genoId for entrySummary)
						Map<String,String> filterMap = new HashMap<String,String>();
						if(filter!=null){
							for(@SuppressWarnings("unused") Map.Entry<String, List<Object>> colMap: fbkMap.entrySet()){
								if(multiFilter){
									filterMap = loadMapForMultiFilterSummary(reportOutput,type,filter, sheet);
								}else{
									filterMap = loadMapForSingleFilterSummary(reportOutput,type,filter, sheet);
								}
							}
						}
						
						//loop through the summaries again, this time putting them in the right now
						for(Summary summary : reportOutput.get(type)){
							if(!summary.show()) continue;
							boolean hasCoreChecks = false;
							boolean hasBmrChecks = false;
							boolean hasPerfChecks = false;
							boolean hasGenChecks = false;
							
							Map<String,Object> sumMap = summary.getValues();
							Object filterValue = sumMap.get(filter); 
							
							
							/*
							 * The genoId is important for entrySummary so we can look up the checks on the fbk, and color-code the rows.
							 * It is not on the locSummary, in which case the booleans will stay false and the rows won't get color-coded
							 */
							Object geno = sumMap.get(trial.getDataLevel().get("genoType"));
							String genoId = "";
							if(geno != null){
								genoId = geno.toString();
							}
							if(coreChecks.contains(genoId)){
								hasCoreChecks = true;
							}
							if(bmrChecks.contains(genoId)){
								hasBmrChecks = true;
							}
							if(performanceChecks.contains(genoId)){
								hasPerfChecks = true;
							}
							if(geneticChecks.contains(genoId)){
								hasGenChecks = true;
							}
							
							String rowID = null;
							if(multiFilter){
								/*
								 * When there are multiple filters (for example exlSummary has locId and genoId), we cannot just pull
								 * the value off the map since there are multiple values.  
								 */
								rowID = getRowIDForMultiFilter(sumMap,filterMap,filter,sheet);
							}else{
								rowID = filterMap.get(filterValue.toString());
							}
							
							if(rowID!=null){
								Row row = sheet.getRow(Integer.valueOf(rowID));
								for(int x=0; x<vatSummaryCols.size(); x++){
									String columnName = vatSummaryCols.get(x).toString();
									//For columns with format TraitName.Measurement, split them and look up the specific value in the map with that trait name
									if(columnName.contains(".")){
										String traitName = null;
										String traitMeasurement = null;
										String[] colSplit = columnName.split("\\.");
										
										if(colSplit!=null && colSplit.length>0){
											try{
												traitName = colSplit[0];
												traitMeasurement = colSplit[1];
											}catch(Exception Ex){
												System.out.println(Ex.getMessage());
											}
											
										}
										
										if(traitName!=null && summary.getTrait().equals(traitName)){
											Cell c = row.createCell(x);
											//if the measurement is found, put it in the cell
											if(sumMap.get(traitMeasurement)!= null){
												setValue(c, Funcs.checkVal(sumMap.get(traitMeasurement)));
											}
											//all cells have to be formatted even if no value found
											formatCell(c, hasCoreChecks,hasBmrChecks,hasPerfChecks,hasGenChecks);
										}else{
											//name off of summary.getTrait() does not equal traitName, so create an empty formatted cell of the correct color
											Cell c = row.getCell(x);
											if(c==null){
												c = row.createCell(x);
											}
											formatCell(c, hasCoreChecks,hasBmrChecks,hasPerfChecks,hasGenChecks); 
										}
									}else{
										//column name does not have a "."
										if(sumMap.get(columnName)!= null){
											Cell c = row.createCell(x);
											if(!vatSum.getProcStringColumns().contains(columnName)){
												setValue(c, Funcs.checkVal(sumMap.get(columnName)));
											}else{
												c.setCellValue("" + sumMap.get(columnName));
											}
											formatCell(c, hasCoreChecks,hasBmrChecks,hasPerfChecks,hasGenChecks);
										}
									}//end if columns contains "."
								}//end loop of columns
							}//end summary loop
						}//end reportOutput.get(type)!=null
//					}else{
//						logger.warn("VatSummary does not have columns for "+type.name());
//					}//end if vatSum is null 
				}//check for summary type in report output
			}
			catch (Exception e) {
				log.warn(e);
			}
		}//end loop of summary types
		writeFile(filename);
	}
	
	private void setValue(Cell c, Object value){
		if(value instanceof String){
			c.setCellValue((String)value);
		}
		else if(value instanceof Double){
			c.setCellValue((Double)value);
		}
		else if(value instanceof Boolean){
			c.setCellValue((Boolean)value);
		}
		else if(value instanceof Date){
			c.setCellValue((Date)value);
		}
	}
	
	private String getFilterString(ReportOutput reportOutput,SummaryType type){
		String filter = null;

		try{
			Collection<Summary> sumCol = reportOutput.get(type);
			if(sumCol!=null){
				Iterator<Summary> it = sumCol.iterator();
				if(it.hasNext()){
					Summary sum = it.next();
					if(sum!=null && sum.getFilters()!=null){ //not all summaries have filters right now (i.e. Sunflower has no filters)
						LinkedHashMap<String, Integer> filters = sum.getFilters();
						for(String key: filters.keySet()){
							if(filter==null){
								filter = key;
							}else{
								filter = filter+"-"+key;
							}
						}
					}
				}
			}
		}catch(Exception e){
			log.error("ReportWriter getFilterString error: ", e);
		}
		
		return filter;
	}

	@SuppressWarnings("static-access")
	private void formatCell(Cell c, boolean hasCoreChecks, boolean hasBmrChecks, boolean hasPerfChecks, boolean hasGenChecks){
		
		if(hasCoreChecks){
			CellStyle cellStyle = expWB.createCellStyle();
			cellStyle.setFillBackgroundColor(HSSFColor.BRIGHT_GREEN.index);
			cellStyle.setFillForegroundColor(HSSFColor.BRIGHT_GREEN.index);
			cellStyle.setFillPattern(cellStyle.SOLID_FOREGROUND);
			c.setCellStyle(cellStyle);
		}
		if(hasBmrChecks){
			CellStyle cellStyle = expWB.createCellStyle();
			cellStyle.setFillBackgroundColor(HSSFColor.CORAL.index);
			cellStyle.setFillForegroundColor(HSSFColor.CORAL.index);
			cellStyle.setFillPattern(cellStyle.SOLID_FOREGROUND);
			c.setCellStyle(cellStyle);
		}
		if(hasPerfChecks){
			CellStyle cellStyle = expWB.createCellStyle();
			cellStyle.setFillBackgroundColor(HSSFColor.CORNFLOWER_BLUE.index);
			cellStyle.setFillForegroundColor(HSSFColor.CORNFLOWER_BLUE.index);
			cellStyle.setFillPattern(cellStyle.SOLID_FOREGROUND);
			c.setCellStyle(cellStyle);
		}
		if(hasGenChecks){
			CellStyle cellStyle = expWB.createCellStyle();
			cellStyle.setFillBackgroundColor(HSSFColor.GREY_50_PERCENT.index);
			cellStyle.setFillForegroundColor(HSSFColor.GREY_50_PERCENT.index);
			cellStyle.setFillPattern(cellStyle.SOLID_FOREGROUND);
			c.setCellStyle(cellStyle);
		}
		
	}
	
	private void writeFile(String filename){
		if(expWB.getNumberOfSheets() > 0){
			// write the out to the experiment file
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(filename);
				expWB.write(out);
			} catch (FileNotFoundException e) {
				log.error("ReportWriter.writeToExcel " + e.getMessage());
			} catch (IOException e) {
				log.error("ReportWriter.writeToExcel " + e.getMessage());
			}finally{
				if(out!=null){
					try {
						out.close();
					} catch (IOException e) {
						log.error("ReportWriter.writeToExcel " + e.getMessage());
					}
				}
			}
		}else{
			System.out.println("No excel sheets were written out so file was NOT created: "+ filename);
		}
	}

	private Map<String,String> loadMapForMultiFilterSummary(ReportOutput reportOutput,SummaryType type,String filter, Sheet sheet){
		int rowNum = 1;
		Map<String,String> filterMap = new HashMap<String,String>();
		try{
			//build the map assuming 2 filters when we have a multiFilter
			String filter1 = getFilterByIndex(0,filter,"-");
			String filter2 = getFilterByIndex(1,filter,"-");

			String f1UniqueID = null;
			String f2UniqueID = null;
			for(Summary summary : reportOutput.get(type)){
				Map<String,Object> sumMap = summary.getValues();
				f1UniqueID = "" + sumMap.get(filter1);
				f2UniqueID = "" + sumMap.get(filter2);
				String uniqueIDCombo = f1UniqueID+"-"+f2UniqueID;
				if(!filterMap.containsKey(uniqueIDCombo)){ //only put something in the map if it isn't already in there
					filterMap.put(uniqueIDCombo, String.valueOf(rowNum));
					rowNum++;
				}
			}
			
			createRows(rowNum, sheet);
		}catch(Exception Ex){
			System.out.println("loadMapForMultiFilterSummary error: " + Ex.getMessage());
		}
		return filterMap;
	}
	
	private Map<String,String> loadMapForSingleFilterSummary(ReportOutput reportOutput,SummaryType type,String filter, Sheet sheet){
		int rowNum = 1;
		Map<String,String> filterMap = new HashMap<String,String>();
		try{	
			String f1UniqueID = null;
			for(Summary summary : reportOutput.get(type)){
				Map<String,Object> sumMap = summary.getValues();
				f1UniqueID = sumMap.get(filter).toString();
				if(!filterMap.containsKey(f1UniqueID)){
					filterMap.put(f1UniqueID, String.valueOf(rowNum));
					rowNum++;
				}
			}
			createRows(rowNum, sheet);
		}catch(Exception Ex){
			System.out.println("loadMapForMultiFilterSummary error: " + Ex.getMessage());
		}
		return filterMap;
		
	}
	
	private void createRows(int numberOfRows, Sheet sheet){
		//create one row for each unique key/geno
		for(int x=1; x<numberOfRows+1; x++){
			sheet.createRow(x);
		}
	}

	private String getRowIDForMultiFilter(Map<String,Object> sumMap,Map<String,String> filterMap,String filter,Sheet sheet){
		String rowID = null;
		try{
			String filter1 = getFilterByIndex(0,filter,"-");
			String filter2 = getFilterByIndex(1,filter,"-");
			
			String filterValue1 = sumMap.get(filter1).toString();
			String filterValue2 = sumMap.get(filter2).toString();
			String filterComboValue = filterValue1+"-"+filterValue2;
			rowID = filterMap.get(filterComboValue);
			if(rowID!=null){
				sheet.getRow(Integer.valueOf(rowID));
			}else{
				System.out.println("bad row");
			}
		}catch(Exception Ex){
			System.out.println("getRowIDForMultiFilter error: " + Ex.getMessage());
		}
		return rowID;
	}
	
	private String getFilterByIndex(int index, String filter,String delimiter){
		String filterValue = null;
		if(filter!=null){
			String[] filterList = filter.split(delimiter);
			if(filterList.length>index){
				filterValue = filterList[index];
			}
		}
		return filterValue;
	}
}
