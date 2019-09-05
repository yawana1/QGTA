package asreml.output;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class Pvs extends AsremlOutputFile{

	static Logger logger = Logger.getLogger(Pvs.class.getName());
	private final static String type = "_pvs";
	private String statHeader;
	private Map<String, Collection<Pv>> pvs;
	
	public void init(){
		statHeader = delimit+"Predicted_Value"+delimit+"Standard_Error"+delimit+"Ecode";
		pvs = new HashMap<String, Collection<Pv>>();
	}
	protected String getType(){
		return type;
	}
	public String getStatheader() {
		return statHeader;
	}
	public Map<String, Collection<Pv>> getPvs() {
		return pvs;
	}
	public void setPvs(Map<String, Collection<Pv>> pvs) {
		this.pvs = pvs;
	}
	public Pvs(Path filename, String ext, String delimit) {
		super(filename, ext, delimit);
	}
	
	/***
	 * Create Asreml style 
	 * @param names
	 * @return
	 */
	public static String createColumnName(Collection<String> names){
		String result = "";
		
		for(String name : names){
			result += name + "-";
		}
		
		return result.substring(0, result.length()-1);
		
	}
	
	protected void load(){
		BufferedReader br = null;
		try {
			File file = new File(getFullFileName());
			if(!file.exists()){
				logger.warn("Pvs.load: "+file.getPath()+" does not exists");
				return;
			}
			br = new BufferedReader(new FileReader(file));
			String str;
			while ((str = br.readLine()) != null){
				if(str.contains(statHeader)){
					str = str.replace("\\", ""); //strip out extra \\ added by Asreml for !TXTFORM 3
					String[] ids = str.replace(statHeader, "").split(delimit);
					String col = ids[0].trim();
					for(int i=1; i<ids.length;i++){
						col += "-"+ids[i].trim();
					}
					
					Collection<Pv> pvList = new ArrayList<Pv>();
					while ((str = br.readLine()) != null){
						if(str.trim().startsWith("SED:")) break;
						str = str.replace("\\", ""); //strip out extra \\ added by Asreml for !TXTFORM 3
						Pv pv  = new Pv(str.trim().split(delimit));
						pvList.add(pv);
					}
					pvs.put(col, pvList);
				}
			}
		}catch (FileNotFoundException e) {
			logger.warn("Pvs.load",e);
		} catch (IOException e) {
			logger.fatal("Pvs.load",e);
		}finally{
			if(br != null)
				try {
					br.close();
				} catch (IOException e) {
					logger.fatal("Pvs.load",e);
				}
		}
	}
}