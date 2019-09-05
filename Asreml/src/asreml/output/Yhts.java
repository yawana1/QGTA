package asreml.output;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

/**
 * Class to read in the Asreml Yht file which is a file that contains y^ values, residuals, etc for the model that was run.
 * The results are stored into a Map with the record index as the key and the {@link Yht} as the value.
 * 
 * @author Scott Smith
 * @see AsremlOutputFile
 */
public class Yhts extends AsremlOutputFile {

	static Logger logger = Logger.getLogger(Yhts.class.getName());
	private final static String type = "_yht";
	private Map<Integer, Yht> data;
	
	public void init(){
		data = new TreeMap<Integer, Yht>();
	}
	public String getType() {
		return type;
	}
	public Map<Integer, Yht> getData() {
		return data;
	}
	public void setData(Map<Integer, Yht> data) {
		this.data = data;
	}
	
	public Yhts(Path filename, String ext, String delimit){
		super(filename, ext, delimit);
	}
	
	/**
	 * Read the Asreml output Yht file and create a map of {@link Yht} objects.
	 */
	protected void load(){
		BufferedReader br = null;
		try {
			File file = new File(getFullFileName());
			if(!file.exists()){
				logger.warn("Yhts.load: "+file.getPath()+" does not exists");
				return;
			}
			br = new BufferedReader(new FileReader(file));
			String str;
			br.readLine();  //skip header
			while ((str = br.readLine()) != null){
				str = str.replace("\\", ""); //strip out extra \\ added by Asreml for !TXTFORM 3
				Yht yht = new Yht(str.trim().split(delimit));
				data.put(yht.getRecord(), yht);
			}
		}catch (FileNotFoundException e) {
			logger.warn("YhtData.load",e);
		} catch (IOException e) {
			logger.fatal("YhtData.load",e);
		}finally{
			try {
				if(br != null){
					br.close();
				}
			} catch (IOException e) {
				logger.fatal("YhtData.load",e);
			}
		}
	}
	
	public String toString(){
		String nl = "\n";
		String str = "Record Yhat Residual Hat"+nl;
		for(Iterator<Integer> it = data.keySet().iterator(); it.hasNext();){
			Integer key = it.next();
			str += data.get(key).toString()+nl;
		}
		return str;
	}
}
