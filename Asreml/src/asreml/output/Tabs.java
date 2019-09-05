package asreml.output;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Class to read in the Asreml Tab file which is a file that contains general statistics min, max, mean, etc from the data, asd file.
 * The results are stored into a Map with the indexes as the key and the {@link Tab} as the value.
 * 
 * @author Scott Smith
 * @see AsremlOutputFile
 */
public class Tabs extends AsremlOutputFile{

	static Logger log = Logger.getLogger(Tabs.class.getName());
	private final static String type = "_tab";
	private String statHeader;
	private String traitHeader;
	private Map<String, Collection<Tab>> tabs;
	
	public void init(){
		statHeader = delimit+"Mean"+delimit+"StandDevn"+delimit+"Minimum"+delimit+"Maximum"+delimit+"Count";
		traitHeader = "Simple tabulation of ";
		tabs = new HashMap<String, Collection<Tab>>();
	}
	
	public String getType() {
		return type;
	}
	public String getStatheader() {
		return statHeader;
	}
	
	public Map<String, Collection<Tab>> getTabs() {
		return tabs;
	}

	public void setTabs(Map<String, Collection<Tab>> tabs) {
		this.tabs = tabs;
	}

	public Tabs(Path filename, String ext, String delimit){
		super(filename, ext, delimit);
	}
	
	public Tabs(){
		super();
		tabs = new HashMap<String, Collection<Tab>>();
	}
	
	/**
	 * @deprecated
	 */
	protected void load(){
		BufferedReader br = null;
		try {
			File file = new File(getFullFileName());
			if(!file.exists()){
				log.warn("Tabs.load: "+file.getPath()+" does not exists");
				return;
			}
			br = new BufferedReader(new FileReader(file));
			String str;
			String trait = "";
			while ((str = br.readLine()) != null){
				if(str.contains(statHeader)){
					str = trait+delimit+str.replace("\\", ""); //strip out extra \\ added by Asreml for !TXTFORM 3
					String[] ids = str.replace(statHeader, "").split(delimit);
					String col = createId(ids);

					Collection<Tab> tabList = new ArrayList<Tab>();
					while ((str = br.readLine()) != null){
						if(str.equals("")) break;
						str = trait+delimit+str.replace("\\", ""); //strip out extra \\ added by Asreml for !TXTFORM 3
						Tab tab = new Tab(str.trim().split(delimit));
						if(tab.isValid()){
							tabList.add(tab);
						}
					}
					tabs.put(col, tabList);					
				}
				else if(str.contains(traitHeader)){
					trait = str.replace(traitHeader, "").trim();
				}
			}
		} catch (Exception e) {
			log.error("Tabs.load",e);
		}finally{
			try {
				if(br != null){
					br.close();
				}
			} catch (IOException e) {
				log.error("Tabs.load",e);
			}
		}
	}
}