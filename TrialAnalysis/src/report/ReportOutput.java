package report;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import data.xml.objects.Trial;
import utils.Funcs;
import utils.Globals.SummaryType;
import vat.Vat;
import vat.VatSummary;

/**
 * Write the report files from the giving summary data.  Using the Vat.xml file as the format the data 
 * should be written in.
 * 
 * @author Scott Smith
 *
 */
public class ReportOutput {
	
	private static Logger log = Logger.getLogger(ReportOutput.class.getName());
	
	private Map<SummaryType, Collection<Summary>> summaries;
	
	public Map<String,String> vatStoredProcs;
	
	public ReportOutput() {
		summaries = new HashMap<SummaryType, Collection<Summary>>();
		vatStoredProcs = new HashMap<String, String>();
	}

	public Collection<Summary> get(SummaryType type){
		return summaries.get(type);
	}
	
	public void add(SummaryType type, Collection<Summary> collection){
		if(!summaries.containsKey(type)){
			summaries.put(type, collection);
		}
		else{
			summaries.get(type).addAll(collection);
		}
	}
	
	public Object get(SummaryType summaryType, String key, String id, String valueColumn){
		Object result = null;
		Collection<Summary> summaries = get(summaryType);
		
		if(summaries != null){
			for(Summary summary : summaries){
				if(summary.getValues().get(key).equals(id)){
					result = summary.getValues().get(valueColumn);
					break;
				}
			}
		}
		
		return result;
	}
	
	public Collection<SummaryType> getSummaryTypes(){
		return summaries.keySet();
	}
	
	private void addVatStoredProc(String file, String storedProc){
		vatStoredProcs.put(file, storedProc);
	}
	
	public void export(Trial trial, Vat vat){
		try{
			for(SummaryType type :getSummaryTypes()){
				VatSummary vatSum = null;
				String storedProcName = null;
				vatSum = vat.get(type);
				if(vatSum!=null){
					storedProcName = vatSum.getProcName();
				}
				String file = getTextFilePath(trial,type, false);
				String fileVatPath = getTextFilePath(trial,type, true);
				if(vatSum!=null){
					Collection<Summary> data = get(type);
					exportSummaryFile(file, data, vatSum);
					if( data != null && data.size() > 0 ){
						addVatStoredProc(fileVatPath,storedProcName);
					}
				}else{
					log.warn(this.getClass().getName()+".export :: No VAT summary");
				}
			}
		}
		catch(Exception e){
			log.error("", e);
		}
	}

	public static String getTextFilePath(Trial trial,SummaryType sumType, boolean win){
		String extension;
		String result;
		if (sumType.toString().equals("heatmapSummary")) {
			extension = "output_csv_file";
			result = String.format("%s/%s.%s.csv"
					,trial.getReportDirectory(win)
					,trial.getTrialName()
					,trial.getSeasonName());
		}
		else {
			extension = "output_flat_file";
			result = trial.getReportDirectory(win)+"/"+sumType+trial.getFileExtention(extension);
		}
		return result;
	}	
	
	private static void exportSummaryFile(String fullFilePath, Collection<Summary> data, VatSummary vatSum){
		OutputStream writer = null;
		try {
			//create a new file for each Summary
			Path path = Paths.get(fullFilePath); 
			writer = Files.newOutputStream(Funcs.createWithPermissions(path,false));
			String sepStr;
			if (vatSum.getSummaryType().toString().equals("heatmapSummary")) {
				sepStr = ",";
			}
			else {
				sepStr = "\t";
			}

			//write out column header
			List<String> vatSummaryCols = vatSum.getProcColumns();
			for(int x=0; x<vatSummaryCols.size(); x++){
				String columnName = vatSummaryCols.get(x).toString();
				writer.write(columnName.getBytes());
				if (x < vatSummaryCols.size() - 1) {
					writer.write(sepStr.getBytes());
				}
			}
			writer.write("\r\n".getBytes());
			
			//write out the rows of values
			NumberFormat nf = NumberFormat.getInstance();
			nf.setGroupingUsed(false);
			for(Summary summary: data){
				if(summary.show()){
					Map<String,Object> sumMap = summary.getValues();
					for(int x=0; x<vatSummaryCols.size(); x++){
						String columnName = vatSummaryCols.get(x).toString();
						if(sumMap.get(columnName)!= null){
							try{
								if(!vatSum.getProcStringColumns().contains(columnName)){
									Object value = Funcs.checkVal(sumMap.get(columnName));
									if(null != value){
										String s = nf.format(value);
										writer.write(s.getBytes());
									}
								}else{
									writer.write(sumMap.get(columnName).toString().getBytes());
								}
							}catch(Exception Ex){
								log.warn("ReportOutput - Problem with Funcs.checkDbl of this column: "+ columnName + " = " + sumMap.get(columnName) + " " + Ex.getMessage());
							}
						}
						if (x < vatSummaryCols.size() - 1) {
							writer.write(sepStr.getBytes());
						}
					}
					writer.write("\r\n".getBytes());
				}
			}
		} catch (Exception e) {
			log.error("File creatation failed for: " + fullFilePath);
		}
		finally{
			if(null != writer){
				try {
					writer.flush();
				} catch (IOException e) {
					System.out.println("error in ReportOutput "+ e.getMessage());
					log.error(e);
				}
				
				try {
					writer.close();
				} catch (IOException e) {
					System.out.println("error in ReportOutput "+ e.getMessage());
					log.error(e);
				}
			}
		}
	}
}