package data.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import utils.Funcs;

/***
 * Creates an {@link ExpFBK} from raw data contained in a list of data rows.
 * Also creates indexes for each column and save them into the colMap.  An index is created by assigning every unique value a column
 * has an integer starting at 1 and counting up.
 * Also store the genoType Id of any record flaged as any type of check.
 * 
 * @author Scott Smith
 *
 */
public class DataProcesser{

	static Logger log = Logger.getLogger(DataProcesser.class.getName());
	private ExpFBKs fbks;
	private Map<String, String> dataLevel;
	private List<String> booleanColumns;
	
	public DataProcesser(ExpFBKs fbks, Map<String, String> dataLevel){
		this.fbks = fbks;
		this.dataLevel = dataLevel;
		this.booleanColumns = fbks.getBooleanColumns();
	}
	
	/***
	 * Takes a list of data rows and creates an expfbks object where the list is a collections of rows of columnName/value map.
	 */
	public void processData(){
		if(fbks.isDataLoaded()){
			
			//use only Set, since duplicates will just override.  Needs LinkedHashMap to maintain order of objects
			Map<String,LinkedHashSet<Object>> indexes = new HashMap<String, LinkedHashSet<Object>>();
			
			Map<String, Object> columns = null;
			if(fbks.getFbks() != null && fbks.getFbks().size() > 0){
				columns = fbks.getFbks().get(0).getData();
			}
			List<String> colNames = getColNames(columns);
			Map<String,List<Object>> colMap = new HashMap<String, List<Object>>();
	
			initColMap(colMap, colNames, indexes);
	
			//check Sets
			Set<String> core = new HashSet<String>();
			Set<String> performance = new HashSet<String>();
			Set<String> bmr = new HashSet<String>();
			Set<String> genetic = new HashSet<String>();
			Set<String> susceptable = new HashSet<String>();			

			List<ExpFBK> data = fbks.getFbks();
			for(ExpFBK fbk : data){
				try{
					Map<String, Object> row = fbk.getData();
					
					for(String column : row.keySet()){
						Object val = row.get(column);
						indexes.get(column).add(val); //hashing of key will eliminate duplicates automatically
					}
					
					//store check ids
					String genoType = "" + row.get(dataLevel.get("genoType"));
					boolean core_check = Boolean.parseBoolean(""+row.get("coreCheck"));
					boolean performance_check = Boolean.parseBoolean(""+row.get("performanceCheck"));
					boolean genetic_check = Boolean.parseBoolean(""+row.get("geneticCheck"));
					boolean bmr_check = Boolean.parseBoolean(""+row.get("bmrCheck"));
					boolean susceptable_check = Boolean.parseBoolean(""+row.get("susceptableCheck"));
					
					if(core_check){
						core.add(genoType);
					}
					if(performance_check ){
						performance.add(genoType);
					}
					if(genetic_check){
						genetic.add(genoType);
					}
					if(bmr_check){
						bmr.add(genoType);
					}
					if(susceptable_check){
						susceptable.add(genoType);
					}
				}catch(Exception e){
					log.error("", e);
				}
			}
			
			//move checks to fbk
			fbks.coreChecks = new ArrayList<String>(core);
			fbks.performanceChecks = new ArrayList<String>(performance);
			fbks.bmrChecks = new ArrayList<String>(bmr);
			fbks.geneticChecks = new ArrayList<String>(genetic);
			fbks.susceptableChecks = new ArrayList<String>(susceptable);
			
			//move values from indexes to the fbks colMap.
			for(int i = 0; i < colNames.size(); i++){
				String columnName = colNames.get(i);
				colMap.get(columnName).addAll(indexes.get(columnName));  //add list of unique values into the fbks				
				
				//create map of value to index used to speed up ASD file creation later and index searches.
				Map<Object, Integer> m = new HashMap<Object, Integer>();
				int j=1;
				for(Object o :indexes.get(columnName)){
					if(booleanColumns != null && booleanColumns.contains(columnName)){
						j = fbks.getIndex(Funcs.getBool(o));
					}
					m.put(o, j);
					j++;
				}
				fbks.getMapFindIndex().put(columnName, m);
			}
			
			fbks.setColMap(colMap);
		}
	}
	
	/***
	 * Method to get col names from the returned data.
	 * Currently assuming a List of Column,Values Maps in the data
	 * @param data
	 * @return
	 */
	private List<String> getColNames(Map<String,Object> row) {
		List<String> colNames = new ArrayList<String>();
		if(row != null){
			colNames = new ArrayList<String>(row.keySet());
		}
		return colNames;
	}

	/**
	 * Create data Structures for all the columns in the data set
	 * @param colMap
	 * @param colNames
	 * @param indexes
	 */
	private void initColMap(Map<String,List<Object>> colMap, List<String> colNames, Map<String,LinkedHashSet<Object>> indexes){
		for(int i = 0; i < colNames.size(); i++){
			colMap.put(colNames.get(i), new ArrayList<Object>());
			indexes.put(colNames.get(i), new LinkedHashSet<Object>()); //
		}	
	}
}