package data.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import asreml.AsremlAsd;
import data.xml.objects.Checks;
import data.xml.objects.Trait;
import data.xml.objects.Trial;
import db.DataSource;
import db.SQL;
import db.Session;
import error.ErrorMessage;
import utils.Funcs;

/**
 * 
 * @author Scott Smith
 *
 */
public class ExpFBKs {

//	private static final String CONNECTION_STRING = "jdbc:derby:memory:myDB;create=true;autoCommit=false;derby.system.durability=test";
//	private static final String DB_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	private static final String CONNECTION_STRING = "jdbc:hsqldb:mem:TA;";
	private static final String DB_DRIVER = "org.hsqldb.jdbc.JDBCDriver";
	private static final String SQL_FROM = " FROM EXP_FBK ";
	private static final String SQL_TABLE_NAME = " EXP_FBK ";
	
	private static Logger log = Logger.getLogger(ExpFBKs.class.getName());
	private Session session;
	private boolean dataLoaded = false;
	private List<ExpFBK> expFbks; //cache expFbks from database after first lookup
	
	private List<Trait> traits = new ArrayList<Trait>();
	private List<String> booleanColumns;
	protected List<String> coreChecks = new ArrayList<String>();
	protected List<String> performanceChecks = new ArrayList<String>();
	protected List<String> geneticChecks = new ArrayList<String>();
	protected List<String> bmrChecks = new ArrayList<String>();
	protected List<String> susceptableChecks = new ArrayList<String>();
	protected Map<String, List<Object>> colMap = new HashMap<String, List<Object>>();
	protected Map<String, Map<Object, Integer>> mapFindIndex = new HashMap<String, Map<Object, Integer>>();
	
	// Spatial Corrections parameters
	protected Integer globalPass = 0;
	protected Integer globalRange = 0;
	protected HashMap<Trait, HashMap<Integer, Double>> countNonZero = new HashMap<Trait, HashMap<Integer,Double>>();
	protected Map<Trait, Integer> countRec = new HashMap<Trait, Integer>();

	protected Map<Integer,HashMap<Integer,Double>> twoDCoefficients=new HashMap<Integer,HashMap<Integer,Double>>();
	
	protected HashMap<Trait, HashMap<Integer, HashMap<Integer, Double>>> adjPassRange = new HashMap<Trait, HashMap<Integer, HashMap<Integer, Double>>>();
	protected HashMap<Trait, HashMap<Integer, HashMap<Integer, Double>>> adjPass = new HashMap<Trait, HashMap<Integer, HashMap<Integer, Double>>>();
	protected HashMap<Trait, HashMap<Integer, HashMap<Integer, Double>>> adjRange = new HashMap<Trait, HashMap<Integer, HashMap<Integer, Double>>>();
	protected HashMap<Trait, HashMap<Integer, Double>> blockCenters = new HashMap<Trait, HashMap<Integer, Double>>();
	protected HashMap<Trait, HashMap<Integer, Double>> locCenters = new HashMap<Trait, HashMap<Integer,Double>>();
	protected HashMap<Trait, HashMap<Integer, HashMap<Integer, Double>>> customValues = new HashMap<Trait, HashMap<Integer,HashMap<Integer,Double>>>();
	protected HashMap<Trait, HashMap<Integer, HashMap<Integer, Integer>>> customNum = new HashMap<Trait, HashMap<Integer,HashMap<Integer,Integer>>>();
	protected Map<Trait, Map<Object,Checks>> checkMeans = new ConcurrentHashMap<Trait, Map<Object, Checks>>();

	public boolean isDataLoaded() {
		return dataLoaded;
	}
	public void setDataLoaded(boolean dataLoaded) {
		this.dataLoaded = dataLoaded;
	}
	public Integer getGlobalPass() {
		return globalPass;
	}
	public void setGlobalPass(Integer globalPass) {
		this.globalPass = globalPass;
	}
	public Integer getGlobalRange() {
		return globalRange;
	}
	public void setGlobalRange(Integer globalRange) {
		this.globalRange = globalRange;
	}
	public HashMap<Trait, HashMap<Integer, HashMap<Integer, Integer>>> getCustomNum() {
		return customNum;
	}
	public void setCustomNum(
			HashMap<Trait, HashMap<Integer, HashMap<Integer, Integer>>> customNum) {
		this.customNum = customNum;
	}
	public HashMap<Trait, HashMap<Integer, HashMap<Integer, Double>>> getCustomValues() {
		return customValues;
	}
	public void setCustomValues(
			HashMap<Trait, HashMap<Integer, HashMap<Integer, Double>>> customValues) {
		this.customValues = customValues;
	}
	public Map<Trait, Map<Object, Checks>> getCheckMeans() {
		return checkMeans;
	}
	public void setCheckMeans(Map<Trait, Map<Object, Checks>> checkMeans) {
		this.checkMeans = checkMeans;
	}
	public List<Trait> getTraits() {
		return traits;
	}
	public HashMap<Trait, HashMap<Integer, Double>> getLocCenters() {
		return locCenters;
	}
	public void setLocCenters(HashMap<Trait, HashMap<Integer, Double>> locCenters) {
		this.locCenters = locCenters;
	}
	public Map<Integer, HashMap<Integer, Double>> getTwoDCoefficients() {
		return twoDCoefficients;
	}
	public void setTwoDCoefficients(
			TreeMap<Integer, HashMap<Integer, Double>> twoDCoefficients) {
		this.twoDCoefficients = twoDCoefficients;
	}
	public HashMap<Trait, HashMap<Integer, Double>> getCountNonZero() {
		return countNonZero;
	}
	public void setCountNonZero(
			HashMap<Trait, HashMap<Integer, Double>> countNonZero) {
		this.countNonZero = countNonZero;
	}
	public Map<Trait, Integer> getCountRec() {
		return countRec;
	}
	public void setCountRec(Map<Trait, Integer> countRec) {
		this.countRec = countRec;
	}
	public HashMap<Trait, HashMap<Integer, HashMap<Integer, Double>>> getAdjPassRange() {
		return adjPassRange;
	}
	public void setAdjPassRange(
			HashMap<Trait, HashMap<Integer, HashMap<Integer, Double>>> adjPassRange) {
		this.adjPassRange = adjPassRange;
	}
	public HashMap<Trait, HashMap<Integer, HashMap<Integer, Double>>> getAdjPass() {
		return adjPass;
	}
	public void setAdjPass(
			HashMap<Trait, HashMap<Integer, HashMap<Integer, Double>>> adjPass) {
		this.adjPass = adjPass;
	}
	public HashMap<Trait, HashMap<Integer, HashMap<Integer, Double>>> getAdjRange() {
		return adjRange;
	}
	public void setAdjRange(
			HashMap<Trait, HashMap<Integer, HashMap<Integer, Double>>> adjRange) {
		this.adjRange = adjRange;
	}
	public HashMap<Trait, HashMap<Integer, Double>> getBlockCenters() {
		return blockCenters;
	}
	public void setBlockCenters(
			HashMap<Trait, HashMap<Integer, Double>> blockCenters) {
		this.blockCenters = blockCenters;
	}
	public void setTraits(List<Trait> traits) {
		this.traits = traits;
	}
	public Map<String, Map<Object, Integer>> getMapFindIndex() {
		return mapFindIndex;
	}
	public void setMapFindIndex(Map<String, Map<Object, Integer>> mapFindIndex) {
		this.mapFindIndex = mapFindIndex;
	}
	public Map<String, List<Object>> getColMap() {
		return colMap;
	}
	public List<Object> getColMap(String col){
		return colMap.get(col);
	}
	public void setColMap(Map<String, List<Object>> colMap) {
		this.colMap = colMap;
	}
	public void setColMap(String col, List<Object> map){
		this.colMap.put(col, map);
	}
	public List<String> getCoreChecks() {
		return coreChecks;
	}
	public void setCoreChecks(List<String> coreChecks) {
		this.coreChecks = coreChecks;
	}
	public List<String> getBmrChecks() {
		return bmrChecks;
	}
	public void setBmrChecks(List<String> bmrChecks) {
		this.bmrChecks = bmrChecks;
	}
	public List<String> getPerformanceChecks() {
		return performanceChecks;
	}
	public void setPerformanceChecks(List<String> performanceChecks) {
		this.performanceChecks = performanceChecks;
	}
	public List<String> getGeneticChecks() {
		return geneticChecks;
	}
	public void setGeneticChecks(List<String> geneticChecks) {
		this.geneticChecks = geneticChecks;
	}
	public List<String> getSusceptableChecks() {
		return susceptableChecks;
	}
	public void setSusceptableChecks(List<String> susceptableChecks) {
		this.susceptableChecks = susceptableChecks;
	}
	public List<String> getBooleanColumns() {
		return booleanColumns;
	}
	public void setBooleanColumns(List<String> booleanColumns) {
		this.booleanColumns = booleanColumns;
	}
	/***
	 * Load hsql drive needs hsql.jar on the classpath
	 * @throws Exception
	 */
	public ExpFBKs() throws Exception{
		
		try{
			String dbName = "inMemory";
			DataSource dataSource = new DataSource(dbName, CONNECTION_STRING, DB_DRIVER);
			session = new Session(dataSource);
			
			if( !session.isConnected()){
				log.error(ErrorMessage.INSTANCE.getMessage("cannot_connection") + dbName );
			}
		}
		catch(Exception e){
			log.error(ErrorMessage.INSTANCE.getMessage("init_error") + ExpFBKs.class.getName(), e);
			throw e;
		}
	}
	
	public void shutdown(){
		try{
			session.updateSQL("SHUTDOWN");
			session.closeSession();
		}
		catch(Exception e){
			
		}
	}
	
	/**
	 * Return first instance of data with this filter
	 * @param col - Column to search on
	 * @param id - Value of the column to search on
	 * @return
	 */
	public ExpFBK getFirstFBK(String col, Integer id){
		ExpFBK fbk = null;
		
		Map<String, Object> filters = new HashMap<>();
		filters.put(col, id);
		
		fbk = getFirstFBK(filters);
		
		return fbk;
	}
	
	/**
	 * Return first instance of data with this filter
	 * @param col - Column to search on
	 * @param id - Value of the column to search on
	 * @return
	 */
	public ExpFBK getFirstFBK(Map<String,Object> filters){
		ExpFBK fbk = null;
		
		String sql = "SELECT * FROM EXP_FBK WHERE ";
		
		boolean first = true;
		for(Entry<String, Object> filter : filters.entrySet()){
			if(first){
				first = false;
			}
			else{
				sql += " AND ";
			}
			
			sql += Funcs.quoteString(filter.getKey()) + " = ";
			
			if(filter.getValue() instanceof String){
				sql += "'" + filter.getValue() + "'";
			}
			else{
				sql += filter.getValue();
			}
		}
		
		sql += " LIMIT 1 ";
		List<Map<String,Object>> data =  get(sql);
		
		if(data != null && !data.isEmpty()){
			fbk = new ExpFBK(data.get(0));
		}
		else{
			log.warn(ErrorMessage.INSTANCE.getMessage("sql_select_no_data") + sql);
		}
		
		return fbk;
	}
	
	/***
	 * Get data from the exp_fbk by creating query from the parames 
	 * @param select
	 * @param where
	 * @param groupBy
	 * @return - List of columnName/value Maps.
	 */
	public List<Map<String,Object>> get(String select, String where, String groupBy){
		String sql = createSql(select, where, groupBy);
		List<Map<String,Object>> data = get(sql);
		return data;
	}
	
	/***
	 * Get data from the exp_fbk by creating query from the sql
	 * @param sql
	 * @return - List of columnName/value Maps.
	 */
	public List<Object[]> getList(String sql){
		List<Object[]> data = null;
		try{
			data =  session.procSQLList(sql);
		}
		catch(Exception e){
			log.error(sql, e);
			throw e;
		}
		return data;
	}
	
	public String createSql(String select, String where, String groupBy){
		return createSql(select, where, groupBy, null);
	}
	
	public String createSql(String select, String where, String groupBy, String having){
		String sql = select + SQL_FROM;
		if(where != null){
			sql += where;
		}
		if(groupBy != null){
			sql += groupBy;
		}
		if(having != null){
			sql += having;
		}
		return sql;
	}
	
	public List<Object[]> getList(String select, String where, String groupBy){
		String sql = createSql(select, where, groupBy);
		List<Object[]> data = getList(sql);
		return data;
	}
	
	/***
	 * Get data from the exp_fbk by creating query from the sql
	 * @param sql
	 * @return - List of columnName/value Maps.
	 */
	public List<Map<String,Object>> get(String sql){
		List<Map<String,Object>> data = null;
		try{
			data =  session.procSQLMap(sql);
		}
		catch(Exception e){
			log.error(sql, e);
			throw e;
		}
		return data;
	}
	
	/***
	 * Update data from the exp_fbk by creating query from the sql
	 * clear cache
	 * @param sql
	 * @return - Number of updated rows
	 */
	public int update(String sql){
		int rows = 0;
		
		try{
			rows = session.updateSQL(sql);
			
			if(expFbks != null){
				expFbks.clear();
			}
		}
		catch(Exception e){
			log.error(sql, e);
			throw e;
		}
		return rows;
	}
	
	/**
	 * Return list of fbks that match the filters
	 * @param sql
	 * @return
	 */
	public List<ExpFBK> getFBK(String sql){
		List<ExpFBK> fbks = new ArrayList<ExpFBK>();

		List<Map<String,Object>> data = get(sql);
		
		//Format data into fbks
		if(data != null && !data.isEmpty()){
			for(Map<String,Object> row : data){
				ExpFBK fbk = new ExpFBK(row);
				fbks.add(fbk);
			}
		}
		else{
			log.warn(ErrorMessage.INSTANCE.getMessage("sql_select_no_data") + sql);
		}
		
		return fbks;
	}
	
	public void loadData(TrialData trialData){
		loadData(trialData.getData(), trialData.getTypes(), trialData.getIndexColumns());
	}
	
	/**
	 * Data has no header information.
	 * @param header
	 * @param data
	 */
	public void loadData(String[] header, List<String[]> data){
		try{
			if(expFbks != null){
				expFbks.clear();
			}
			
			if(data != null && data.size() > 0){
				SQL sql = new SQL(SQL_TABLE_NAME, Arrays.asList(header));
				
				try{
					String insertStmt = sql.insert();
					session.runBulkSQL(insertStmt, sql.getColumns(), data);
				}
				catch(Exception e){
					log.error(ErrorMessage.INSTANCE.getMessage("sql_insert"), e);
					throw e;
				}
			}
		}
		catch(Exception e){
			log.error("", e);
			throw e;
		}
	}
	
	/**
	 * Expects first row is header info
	 * @param data
	 */
	public void loadData(List<String[]> data){
		if(data != null && data.size() > 1){
			String[] header = data.get(0);
			
			List<String[]> list = data.subList(1, data.size());
			
			loadData(header, list);
		}
	}
	
	/**
	 * Reload inmem db from cache;
	 * @param trial
	 */
	public void loadData(Trial trial){
		if(expFbks != null && expFbks.size() > 1){
		
			//get header
			ExpFBK fbk = expFbks.get(0);
			Set<String> names = fbk.getColNames();
			Map<String, String> types = new HashMap<>();
			for(String name : names){
				types.put(name, "varchar(255)");
			}
			
			//get columns to index
			List<String> indexColumns = ExpFBKs.getIndexColumns(trial);
			
			List<Map<String, Object>> data = new ArrayList<>();
			for(ExpFBK expFbk : expFbks){
				data.add(expFbk.getData());
			}
			
			
			loadData(data, types, indexColumns);
		}
	}
	
	/**
	 * Bulk insert data into the in memory database
	 * 
	 * @param data
	 * @param types - Map of column names to database column datatypes
	 * @param booleanColumns - Columns that are boolean type
	 * @param indexColums - Columns to index
	 */
	public void loadData(List<Map<String, Object>> data, Map<String, String> types, List<String> indexColumns){
		try{
			if(expFbks != null){
				expFbks.clear();
			}
			
			if(data != null){
				//create fbk table
				Map<String,Object> row = data.get(0);
				SQL sql = new SQL(SQL_TABLE_NAME, row.keySet());
				String sqlCreate = null;
				
				try{
					sqlCreate = sql.createTable(row, types);
					session.updateSQL(sqlCreate);
				}
				catch(Exception e){
					log.error("Sql = " + sqlCreate, e);
				}
								
				try{
					String insertStmt = sql.insert();
					session.runBulkSQL(insertStmt, sql.getColumns(), data);
					String createIndexSql = " CREATE INDEX index_exl ON " + SQL_TABLE_NAME + " (" + Funcs.quote(indexColumns, ",") + ") ;";
					session.updateSQL(createIndexSql);
					
					//for Geneva
					if(row.keySet().contains("fParent")){
						createIndexSql = " CREATE INDEX index_fParent ON " + SQL_TABLE_NAME + " (" + Funcs.quoteString("fParent") + ") ;";
						session.updateSQL(createIndexSql);
					}
					if(row.keySet().contains("mParent")){
						createIndexSql = " CREATE INDEX index_mParent ON " + SQL_TABLE_NAME + " (" + Funcs.quoteString("mParent") + ") ;";
						session.updateSQL(createIndexSql);
					}
					
					dataLoaded = true;
				}
				catch(Exception e){
					log.error(ErrorMessage.INSTANCE.getMessage("sql_insert"), e);
				}
			}
		}
		catch(Exception e){
			log.error("", e);
		}
	}

	public int getIndex(String column, Object val){
		return mapFindIndex.get(column).get(val);
	}
	
	/**
	 * Return List of expfbk's.  If already set use cached objects else retrive from the database and cache.
	 * @return
	 */
	public List<ExpFBK> getFbks() {
		try{
			//pull from cache if avaliable
			if(expFbks == null || expFbks.size() == 0){
				String sql = "SELECT *"+ SQL_FROM;
				expFbks = getFBK(sql);
			}
		}
		catch(Exception e){
			log.warn("Getting list of expFbk's");
		}
		
		return expFbks;
	}
	
	public Integer findIndex(String columnName, Object id, boolean reIndexed){
		int result = -1;
		if(!mapFindIndex.containsKey(columnName)){
			log.error("No index column " + columnName);
		}
		else{
			Map<Object,Integer> map = mapFindIndex.get(columnName);
			int index = map.get(id);
			
			if(reIndexed){
				columnName = columnName + AsremlAsd.INDEX_COLUMN_SUFFIX;
				List<Object> reIndexes = colMap.get(columnName);
				index = Integer.parseInt(""+reIndexes.get(index-1));
			}
			result = index;
		}
		return result;
	}
	
	public Integer getIndex(boolean id){
		int result = -1;
		result = id == true ? 2 : 1; //asreml specified
		return result;
	}
	
	public boolean hasNull(String columnName){
		return hasNull(columnName, null);
	}
	
	public boolean hasNull(String columnName, Collection<String> logColumns){
		boolean result = true;
		
		try{
			//add extra columns for logging if needed
			String delimiter = ",";
			String extraLogging =  Funcs.quote(logColumns, delimiter);
			
			String select = " SELECT "+Funcs.quoteString(columnName);
			if(extraLogging.length() > 0){
				select += delimiter + extraLogging;
			}
			
			String where = " WHERE " + Funcs.quoteString(columnName) +" IS NULL ";
			List<Map<String,Object>> data = get(select, where, "");
			
			if(data == null || data.size() == 0){
				result = false;
			}
			else{
				if(logColumns != null){
					log.info("Null column row data");
					for(Map<String, Object> row : data){
						for(String logColumn : logColumns){
							log.info("Column " + logColumn + " = " + row.get(logColumn));
						}
					}
				}
			}
		}
		catch (Exception e) {
			log.error("Column = " + columnName,e);
			throw e;
		}
		
		return result;
	}
	
	public int delete(String where, String in){
		int rows = 0;
		String sql = " DELETE " + SQL_FROM + " WHERE " + Funcs.quoteString(in) + " IN ( " + where + " ) " ;
		try{
			rows = update(sql);
		}
		catch(Exception e){
			log.error("", e);
			throw e;
		}
		return rows;
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		for(ExpFBK expFBK : getFbks()){
			buffer.append(expFBK.toString());
			buffer.append(System.lineSeparator());
		}
		return buffer.toString();
	}
	
	public static List<String> getIndexColumns(Trial trial){
		List<String> indexColumns = new ArrayList<>();
		indexColumns.add(trial.getDataLevel().get("genoType"));
		indexColumns.add(trial.getDataLevel().get("environment"));
		return indexColumns;
	}
}