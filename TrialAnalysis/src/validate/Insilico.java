package validate;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import data.collection.ExpFBKs;
import data.xml.objects.Trial;
import error.ErrorMessage;

/**
 * Load Insilico data from a flat file.
 * 
 * @author Scott Smith
 *
 */
public class Insilico implements Validate {

	private static Logger log = Logger.getLogger(Insilico.class.getName());
	private String fileName;
	
	public int validate(Trial trial) throws Exception {
		Path path = Paths.get(trial.getTrialWorkDirectory(), fileName);
		
		if(!Files.exists(path)){
			log.error(String.format("Insilico file %s not found", path));
			throw new Exception();
		}
		
		//read data from csv file
		List<String[]> data = loadDelimitedFile(path.toString(), ' ');
		
		ExpFBKs fbks = trial.getFbks();
		
		fbks.loadData(data);
		
		return 0;
	}
	
	public static List<String[]> loadDelimitedFile(String file, char delimiter) throws IOException{
		List<String[]> data = new ArrayList<>();
			
		if(file == null){
			log.error("Values File" + ErrorMessage.INSTANCE.getMessage("null_value"));
		}
		else{
			if(Files.isReadable(Paths.get(file))){
				CSVReader csvReader = null;
				try{
					csvReader = new CSVReader(new FileReader(file),delimiter, '\"', 0);
					data = csvReader.readAll();
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
		
		return data;
	}
}