package asreml.output;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

/**
 * Class to read in the Asreml Sln file which is a file that contains estimates for fixed and random effect with their standard errors
 * in an array with four columns for the model that was run.
 * The results are stored into a Map with the record index as the key and the {@link Yht} as the value.
 * 
 * @author Scott Smith
 * @see AsremlOutputFile
 */
public class Slns extends AsremlOutputFile{

	static Logger log = Logger.getLogger(Slns.class.getName());
	private final static String type = "_sln";
	private static final String HEADER_ROW_V3 = "ModTerm";
	private static final String HEADER_ROW_V4 = "Model_Term";
	private Map<String, Map<int[], Sln>> data; // columnName/ids/solution values
	
	public void init(){
		data = new TreeMap<>();
	}
	protected String getType() {
		return type;
	}
	public Map<String, Map<int[], Sln>> getData() {
		return data;
	}
	public void setData(Map<String, Map<int[], Sln>> data) {
		this.data = data;
	}

	/***
	 * Create Asreml style 
	 * @param names
	 * @return
	 */
	public static String createColumnName(Collection<String> names){
		String result = "";
		
		for(String name : names){
			result += name + ".";
		}
		
		return result.substring(0, result.length()-1);
		
	}
	
	public Sln get(String colName, int[] ids){
		Sln sln = null;
		Map<int[],Sln> slns = data.get(colName);
		if(slns == null){
			log.error("No solutions for effect " + colName);
		}
		else{
			for(int[] key : slns.keySet()){
				if(Arrays.equals(key, ids)){
					sln = slns.get(key);
				}
			}
		}
		return sln;
	}
	
	public Slns(Path filename, String ext, String delimit){
		super(filename, ext, delimit);
	}
	
	/**
	 * Load .sln file data.
	 */
	public void load(){
		BufferedReader br = null;
		try {
			File file = new File(getFullFileName());
			if(!file.exists()){
				log.warn("Slns.load: "+file.getPath()+" does not exists");
				return;
			}
			br = new BufferedReader(new FileReader(file));
			String str;
			while ((str = br.readLine()) != null){
				try{
					str = str.replace("\\", ""); //strip out extra \\ added by Asreml for !TXTFORM 3
					String[] row = str.trim().split(delimit);
					//skip header row
					if(headerTest(row)){
						continue;
					}
					
					if(Arrays.binarySearch(row, "Infinity") >= 0){
						continue;
					}
					
					Sln sln = new Sln(row);
					
					//skip any id's will negative numbers.  Asreml bug
					if(sln.isValid()){
						Map<int[], Sln> colData = data.get(sln.getColumn());
						if(colData == null){
							colData = new HashMap<>();
							data.put(sln.getColumn(), colData);
						}
						colData.put(sln.getIds(), sln);
					}
					else{
						log.warn("Not valid sln row - "+str);
					}
				}catch(Exception e){
					log.warn("Error reading Sln - "+str+"\n",e);
				}
			}
		}catch (FileNotFoundException e) {
			log.warn("Slns.load",e);
		} catch (IOException e) {
			log.fatal("Slns.load",e);
		}finally{
			try {
				if(br != null){
					br.close();
				}
			} catch (IOException e) {
				log.fatal("Slns.load",e);
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public Sln getMu(){
		Map<int[], Sln> mus = data.get("mu");
		for(Object ids : mus.keySet()){
			if(ids != null && mus.get(ids) != null){
				return mus.get(ids);
			}
		}
		return null;
	}
	
	private boolean headerTest(String[] row) {
		boolean result = false;
		if(HEADER_ROW_V3.equals(row[0].trim()) || HEADER_ROW_V4.equals(row[0].trim())){
			result = true;
		}
		
		return result;
	}
	
	public String toString(){
		String nl = "\n";
		String str = "Variety Effect Replicate Error"+nl;
		for(Iterator<String> it = data.keySet().iterator(); it.hasNext();){
			String key = it.next();
			for(Iterator<int[]> itS = data.get(key).keySet().iterator(); itS.hasNext();){
				str += data.get(key).get(itS.next()).toString()+nl;
			}
		}
		return str;
	}
}