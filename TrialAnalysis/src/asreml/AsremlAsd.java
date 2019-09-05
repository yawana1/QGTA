package asreml;

import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import asreml.AsremlGlobals.AsConst;
import asreml.input.AsremlColumn;
import asreml.input.AsremlColumns;
import data.collection.ExpFBK;
import data.collection.ExpFBKs;
import data.xml.objects.Constants;
import data.xml.objects.Trait;
import data.xml.objects.Trial;
import error.ErrorMessage;
import utils.Funcs;

/***
 * Creates an .asd txt file to be used by Asreml.
 * 
 * @author Scott Smith
 */
public class AsremlAsd {

	static Logger log = Logger.getLogger(AsremlAsd.class.getName());
	private Trial trial;
	private Path filename;
	private boolean test;
	public static final String DELIMITER = ",";
	public static final String MISSING_VALUE = "NA";
	public static final String INDEX_COLUMN_SUFFIX = "_index";

	public Path getFilename() {
		return filename;
	}

	public void setFilename(Path filename) {
		this.filename = filename;
	}

	public boolean isTest() {
		return test;
	}

	public void setTest(boolean test) {
		this.test = test;
	}

	public AsremlAsd(Trial trial, boolean test) {
		this.trial = trial;
		this.test = test;
		filename = Paths.get(trial.getTrialWorkDirectory(), AsConst.prefix.value() + AsConst.asdSuffix.value());
	}
	
	/***
	 * Write the asd file.
	 * @throws Exception 
	 */
	public void createAsd() throws Exception{
		ExpFBKs fbks = trial.getFbks();
		if(fbks == null){
			throw new Exception("ExpFBKS " + ErrorMessage.INSTANCE.getMessage("null_value"));
		}
		else if(!fbks.isDataLoaded()){
			throw new Exception("ExpFbk's " + ErrorMessage.INSTANCE.getMessage("empty"));
		}
		
		//columns that will be in the asd file.
		AsremlColumns columns = trial.getColumns();
		if(columns == null){
			throw new Exception("Trial's Asreml Columns " + ErrorMessage.INSTANCE.getMessage("null_value"));
		}
		
		try{
			//file to write path
			Path asdFile = filename;
			
			//write each expFbk as a line in the file
			int size = fbks.getFbks().size();
			List<String> data = new ArrayList<>(size);
			for(ExpFBK fbk  : fbks.getFbks()){
				StringBuffer row = new StringBuffer();
//				String strNonIndexed = "";
				
				//add Trait values first
				for(Trait t : trial.getTraits()){
					Double value = fbk.getDbl(t.getName());
					if(value==null){
						row.append(MISSING_VALUE);
					}
					else{
						row.append(value);
					}
					row.append(DELIMITER);
				}

				row = row.deleteCharAt(row.length()-1); //remove extra delimiter add to the end
//				strNonIndexed = str;
				
				//add columns defined in the .as file
				for(AsremlColumn column : columns.getColumns()){
					String columnName = column.getName();
					if(columnName == null){
						throw new NullPointerException("AsremlColumn " + ErrorMessage.INSTANCE.getMessage("null_value"));
					}
					
					if(column.isCoeff()){
						for(int i=0; i<fbk.getCoeff().size(); i++){
							row.append(DELIMITER);
							row.append(fbk.getCoeff(i));
//							strNonIndexed += " "+fbk.getCoeff(i);
						}
					}
					else if(fbk.getValue(columnName)!=null){
						//if boolean set it to 2 or 1
						if(column.isBool()){
							row.append(DELIMITER);
							row.append(fbks.getIndex(fbk.getBool(columnName))); //2 is true 1 is false.  Asreml specific
//							strNonIndexed += " "+(fbk.getBool(column.getName())? 2 : 1);
						}
						//add indexed values
						else if(column.isIndexed()){
							Object value = fbk.getValue(columnName);
							if(!fbk.hasColumn(columnName)){
								throw new Exception("No fbk column for asreml column " + columnName);
							}
							
							//for columns that are reordered by a grm
							if(column.isIndexColumn()){
								//get index of value
								String indexColumnName = columnName+INDEX_COLUMN_SUFFIX;
								if(!fbks.getColMap().containsKey(indexColumnName)){
									throw new Exception("ColMap doesn't contain " + indexColumnName);
								}
							
								//get grm reindex index number
								int index = (fbks.getColMap().get(columnName).indexOf(value));
								Object grmIndex = fbks.getColMap().get(indexColumnName).get(index);
								row.append(DELIMITER);
								row.append(grmIndex);
								
//								strNonIndexed += " "+fbk.getValue(column.getName());
							}
							//normal indexed columns
							else{
								if(fbk.getValue(columnName) == null){
									log.error("Fbk missing Asreml column"+ columnName);
									throw new Exception();
								}
								else{
									Map<Object,Integer> valueIndexMap = fbks.getMapFindIndex().get(columnName);
									Integer index =  valueIndexMap.get(value);
									
									//should always have a value for every factor
									if(index == -1){
										log.error(columnName + ErrorMessage.INSTANCE.getMessage("null_value") + " for value - " + value);
										throw new Exception();
									}
									
									row.append(DELIMITER);
									row.append(index);
//									strNonIndexed += " "+fbk.getValue(column.getName());
								}
							}
						}
						//add normal value
						else{
							row.append(DELIMITER);
							row.append(fbk.getValue(columnName));
//							strNonIndexed += " "+fbk.getValue(column.getName());
						}
					}else{
						row.append(DELIMITER);
						row.append(MISSING_VALUE);
//						strNonIndexed += " NA ";
					}
				}
				data.add(row.toString());
			}
			
			Funcs.createWithPermissions(asdFile, false);
			Files.write(asdFile, data, Charset.defaultCharset());
		}
		catch(Exception e){
			log.error("", e);
			throw e;
		}
	}
	
	/***
	 * Convert the TrialAnalysis AsremlAsd object to a Asreml package AsremlAsd object
	 * @return
	 */
	public asreml.input.AsremlAsd getAsd(){
		List<asreml.AsremlTrait> asTraits = Conversion.trait(trial.getTraits());
		asreml.input.AsremlAsd asd = new asreml.input.AsremlAsd(trial.getTrialWorkDirectory(),asTraits, test);
		return asd;
	}
	
	public static void createValuesFile(Trial trial, List<Map<String,Object>> data){
		createValuesFile(trial, data, null);
	}

	/***
	 * Write flat file of the data.
	 * @param trial
	 * @param data - List of column/value rows
	 */
	public static Path createValuesFile(Trial trial, List<Map<String,Object>> data, String fileName){
		//write values.asd file
		
		//default to name from property file
		if(fileName == null){
			fileName = "" + Constants.INSTANCE.getConstant("output_data_file");
		}

		Path file = Paths.get(trial.getTrialWorkDirectory(), fileName);
		if(!Files.isWritable(file.getParent())){
			log.warn(ErrorMessage.INSTANCE.getMessage("not_writable_file ") + file.getParent());
		}
		else{

			try(Writer writer = Files.newBufferedWriter(Funcs.createWithPermissions(file,false), Charset.defaultCharset())){
				if(null != data && data.size() > 0){
					String[] columnNames = trial.getColumnNames();
					StringBuffer rowStr = new StringBuffer();
					
					//write headers
					for(String column : columnNames){
						rowStr.append(Funcs.quoteString(column));
						rowStr.append(DELIMITER);
					}
					//remove extra trailing comma with new line.
					rowStr.replace(rowStr.length()-1, rowStr.length(), System.lineSeparator());
					writer.write(rowStr.toString());
					
					
					//write data rows
					for(Map<String, Object> row : data){
						rowStr = new StringBuffer();
						
						for(String column : columnNames){
							if(row.get(column) != null){
								rowStr.append(Funcs.quoteString(row.get(column)));
							}
							rowStr.append(DELIMITER);
						}
												
						//remove extra trailing comma with new line.
						rowStr.replace(rowStr.length()-1, rowStr.length(), System.lineSeparator());
						writer.write(rowStr.toString());
					}
				}
			}
			catch (Exception e) {
				log.warn("Error creating values.asd", e);
			}
		}
		return file;
	}
}