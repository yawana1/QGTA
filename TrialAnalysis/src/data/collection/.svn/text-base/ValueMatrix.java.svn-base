package data.collection;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import error.ErrorMessage;

/***
 * Construct list of column/value maps from an nonindexed asd file
 * 
 * @author Scott Smith
 *
 */
public class ValueMatrix {

	private static Logger log = Logger.getLogger(ValueMatrix.class.getName());
	private String[] columns;
	
	public ValueMatrix(String[] columns){
		this.columns = columns;
	}
	
	/***
	 * Load a flat file of values into a list to be used to create an ExpFkbs object
	 * @param file - Flat file to load
	 * @return - Rows of column name/value maps.
	 * @throws IOException 
	 */
	//TODO interface for dataCollection
	public TrialData load(String file) throws IOException {
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
		Map<String, String> types = new HashMap<>();
		
		if(file == null){
			log.error("Values File" + ErrorMessage.INSTANCE.getMessage("null_value"));
		}
		else{
			if(Files.isReadable(Paths.get(file))){
				CSVReader csvReader = null;
				try{
					String[] row;
					boolean addTypes = true;
					csvReader = new CSVReader(new FileReader(file),',', '\"', 1);
					while((row = csvReader.readNext()) != null){
						Map<String, Object> dataRow = new HashMap<String, Object>();
						for(int i = 0; i < columns.length; i++){
							dataRow.put(columns[i], processValue(row[i]));

							//add column names for types
							if(addTypes){
								types.put(columns[i], "varchar(255)");
							}
						}
						
						addTypes = false;
						data.add(dataRow);
					}
				}
				catch (Exception e) {
					log.error(ErrorMessage.INSTANCE.getMessage(""), e);	
				}
				finally{
					if(csvReader != null){
						csvReader.close();
					}
				}
			}
			else{
				log.error(ErrorMessage.INSTANCE.getMessage("not_found_file") + file);
			}
		}

		return new TrialData(data, types);
	}
	
	/**
	 * Change NA and blank values to null
	 * 
	 * @param value
	 * @return
	 */
	private String processValue(String value){
		String correctedValue = value.replaceAll("\"", "");
		
		if(correctedValue.equals("NA")){
			correctedValue = null;
		}
		else if(correctedValue.equals("")){
			correctedValue = null;
		}
		
		return correctedValue;
	}
}