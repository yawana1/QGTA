package stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import report.Summary;
import utils.Funcs;
import utils.ObjectQuery;
import utils.Parallel;
import asreml.AsremlTrait;
import asreml.input.AsremlColumn;
import asreml.input.AsremlModel;
import asreml.input.AsremlTab;
import asreml.input.AsremlTabs;
import asreml.output.AsremlOutput;
import asreml.output.AsremlOutputFile;
import asreml.output.Tab;
import asreml.output.Tabs;
import data.collection.ExpFBKs;
import error.ErrorMessage;

/**
 * Used to create Tabulated values, min, max, raw mean, std dev, count.
 * Currently uses HSQL in memory data to produce values.
 */
public class Tabulate{

	static Logger log = Logger.getLogger(Tabulate.class.getName());
	private static final String SELECT = "SELECT :ids avg(cast(:trait as double)) as \"rawMean\", ISNULL(stddev_samp(cast(:trait as double)), 0) as \"rawStdDev\", min(cast(:trait as double)) as \"rawMin\", max(cast(:trait as double)) as \"rawMax\", count(:trait) as \"rawCount\", median(cast(:trait as double)) as \"median\"";
	private static final String SUMS = ", (avg(cast(:trait as double)) * count(:trait)) as \"sums\"";
	
	public static void getTabulateStatistic(Collection<Summary> summaries, LinkedHashMap<String, Integer> filters, Tabs tabs, String summaryColumn, String columnToFind){
		//create tab id name ie locId-genoId
		List<String> ids = new ArrayList<String>();
		ids.addAll(filters.keySet());
		String col = AsremlOutputFile.createId(ids.toArray(new String[ids.size()]));
		
		Collection<Tab> data = tabs.getTabs().get(col);
		
		for(Summary summary : summaries){
			
			//pull out filters from summary and add any additional if desired
			for(Entry<String, Integer> filter : filters.entrySet()){
				if(summary.getFilters().containsKey(filter.getKey())){
					filter.setValue(summary.getFilters().get(filter.getKey()));
				}
			}
			
			Double result = getTabulateStatistic(filters, data, columnToFind);
			summary.getValues().put(summaryColumn, result);
		}
	}
	
	/**
	 * 
	 * @param data
	 * @param columnToFind 
	 */
	public static Double getTabulateStatistic(LinkedHashMap<String, Integer> filters, Collection<Tab> data, String columnToFind){
		Double result = null;
		try{
			//select
			String select = columnToFind;
			
			//where
			List<String> entryFilter = new ArrayList<String>();
			int j = 0;
			for(String filter : filters.keySet()){
				entryFilter.add(" get(keys, " + j++ + ") = '" + filters.get(filter).toString() + "' ");
			}
			
			ObjectQuery query = new ObjectQuery( Arrays.asList(select), Tab.class.getName(), entryFilter, data);
			List<List<Double>> results = query.execute();
			
			if(results.size() > 0){
				List<Double> selectValues = results.get(0);
				for(int i =0; i<query.getAlias().size(); i++){
					result = selectValues.get(i);
				}
			}
		}
		catch (Exception e) {
			log.warn("", e);
		}
		return result;
	}
	
	/***
	 * Create Tabs using tab command in asremlModel, but use expfkbs to calcuate statistics
	 * @param <V>
	 * @param models - AsremlModels
	 * @param fbks
	 * @param threadPoolSize 
	 * @throws Exception 
	 */
	public static <V> void createTabs(final List<AsremlModel> models, final ExpFBKs fbks, int threadPoolSize) throws Exception{
		try{
			Collection<Callable<V>> tasks = new ArrayList<>();
			
			for(final AsremlModel model: models){
				AsremlTabs asremlTabs = model.getTabs();
				if(asremlTabs == null){
					break;
				}
				final List<AsremlTab> tabs = asremlTabs.getTabs();
				if(tabs == null){
					break;
				}
				
				List<AsremlTrait> traits = model.getTraits();

				final Map<AsremlTrait, AsremlOutput> outputs = model.getOutputs();
				
				for(final AsremlTrait trait: traits){
					Callable<V> task = new Callable<V>() {
						@Override
						public V call() throws Exception {		
							AsremlOutput asremlOutput = outputs.get(trait);
							for(AsremlTab tab : tabs){
								List<String> factors = tab.getFactors();
								String ids = AsremlOutputFile.createId(tab.getFactors());
								
								//run query
								//run stat's group by x number of factors
								String idSql = Funcs.listToQuotedStr(factors, ",");
								String groupBy = " GROUP BY " + idSql;
								String sql = SELECT.replace(":trait", Funcs.quoteString(trait.getName())); //replace trait in sql with actual trait name ie yield
								sql = sql.replace(":ids", idSql + ","); //add comma to end of the list
								
								//only this trait where it's not null
								String where = " WHERE " + Funcs.quoteString(trait.getName()) + " IS NOT NULL";
								
								List<Object[]> data = fbks.getList(sql, where, groupBy);
								
								//create Tabs
								Collection<Tab> createdTabs = new ArrayList<>() ;
								for(Object[] row : data){
									//change actual value id's to indexes
									for(int i=0; i < factors.size(); i++){
										String columnName = factors.get(i);
										AsremlColumn column = model.getColumns().get(columnName);
										row[i] = fbks.findIndex(columnName, row[i], column.isIndexColumn());
									}
									
									Tab t = new Tab(Funcs.toStringArray(row));
									createdTabs.add(t);
								}
								
								if(asremlOutput == null){
									log.info("Null Trait = " + trait);
									
									for(AsremlTrait t : model.getOutputs().keySet()){
										log.info("trait = " + t);
										log.info("AsremlOutput = " + model.getOutputs().get(t));
									}
									
									throw new Exception("Blank asremlOutput for " + trait);
								}
		
								//store new tabs in the output
								Tabs outputTabs = asremlOutput.getTabs();
								Map<String, Collection<Tab>> outputTabMap = outputTabs.getTabs();
								outputTabMap.put(ids, createdTabs);
							}
							return null;
						}
					};
					tasks.add(task);
				}

				Parallel parallel = new Parallel(threadPoolSize);
				parallel.call(tasks);
			}
		}
		catch(Exception e){
			log.fatal(e);
			throw e;
		}
	}
	
	
	public static void createTab(AsremlModel model, AsremlTrait trait, ExpFBKs fbks) throws Exception{
		Map<AsremlTrait, AsremlOutput> outputs = model.getOutputs();
		AsremlOutput asremlOutput = outputs.get(trait);
		AsremlTabs asremlTabs = model.getTabs();
		List<AsremlTab> tabs = asremlTabs.getTabs();
		for(AsremlTab tab : tabs){
			List<String> factors = tab.getFactors();
			String ids = AsremlOutputFile.createId(tab.getFactors());
			
			//run query
			//run stat's group by x number of factors
			String idSql = Funcs.listToQuotedStr(factors, ",");
			String groupBy = " GROUP BY " + idSql;
			String sql = SELECT.replace(":trait", Funcs.quoteString(trait.getName())); //replace trait in sql with actual trait name ie yield
			sql = sql.replace(":ids", idSql + ","); //add comma to end of the list
			
			//only this trait where it's not null
			String where = " WHERE " + Funcs.quoteString(trait.getName()) + " IS NOT NULL";
			
			List<Object[]> data = fbks.getList(sql, where, groupBy);
			
			//create Tabs
			Collection<Tab> createdTabs = new ArrayList<>() ;
			for(Object[] row : data){
				//change actual value id's to indexes
				for(int i=0; i < factors.size(); i++){
					String columnName = factors.get(i);
					AsremlColumn column = model.getColumns().get(columnName);
					row[i] = fbks.findIndex(columnName, row[i], column.isIndexColumn());
				}
				
				Tab t = new Tab(Funcs.toStringArray(row));
				createdTabs.add(t);
			}
			
			if(asremlOutput == null){
				log.info("Null Trait = " + trait);
				
				for(AsremlTrait t : model.getOutputs().keySet()){
					log.info("trait = " + t);
					log.info("AsremlOutput = " + model.getOutputs().get(t));
				}
				
				throw new Exception("Blank asremlOutput for " + trait);
			}

			//store new tabs in the output
			Tabs outputTabs = asremlOutput.getTabs();
			Map<String, Collection<Tab>> outputTabMap = outputTabs.getTabs();
			outputTabMap.put(ids, createdTabs);
		}
	}
	
	/***
	 * Query ExpFbks to get min, max, mean, stdev, count for the giving summaries filters
	 * @param summaries
	 * @param fbks
	 */
	public static void getTabulateStatistics(Collection<Summary> summaries, ExpFBKs fbks){
		try{
			//long start = System.currentTimeMillis();
			Collection<Summary> removeList = new ArrayList<Summary>(); //remove summaries that have zero count
			
			for(Summary summary : summaries){
				boolean remove = getTabulateStatistics(summary, fbks);
				if(remove){
					removeList.add(summary);
				}
			}
			
			//remove any summary with a zero rawCount
			for(Summary summary : removeList){
				summaries.remove(summary);
			}
		}
		catch(Exception e){
			log.warn("",e);
		}
	}
	
	/***
	 * Query ExpFbks to get min, max, mean, stdev, count for the giving summaries filters
	 * @param summaries
	 * @param fbks
	 */
	public static boolean getTabulateStatistics(Summary summary, ExpFBKs fbks){
		boolean remove = false;
		try{
			String select = SELECT + SUMS;
			
			//select
			select = select.replace(":trait", Funcs.quoteString(summary.getTrait().getName()));
			select = select.replace(":ids", "");

			//add group by factors
			String where = " where ";
			for(String columnName : summary.getFilters().keySet()){
				Object value =  summary.getValues().get(columnName);
				where += " " + Funcs.quoteString(columnName) + "=" + "'" + value + "'" + " AND ";
			}
			where += Funcs.quoteString(summary.getTrait()) + " IS NOT NULL";
				
			//run query
			List<Map<String,Object>> data = fbks.get(select,where,"");
				
			if(null == data){
				log.warn(ErrorMessage.INSTANCE.getMessage("sql_select_no_data") + select + where);
			}
			else{
				//add results to summary
				for(Map<String,Object> row : data){
					//add any summary with a zero rawCount to the remove list
					Object count = row.get("rawCount");
					if(count == null || Integer.parseInt(count.toString()) == 0){
						remove = true;
					}
						
					//add queried data to the summary
					summary.getValues().putAll(row);
				}
			}
		}
		catch(Exception e){
			log.warn("",e);
		}
		return remove;
	}
	
	
	/***
	 * Add Tabulate stats to the summary objects.  All tab outputs prefixed with the word "raw".
	 * @param summaries
	 * @param data
	 */
	public static void getTabulateStatistics(Collection<Summary> summaries, Collection<Tab> data){
		try{
			if(null != summaries){
				if(null == data){
					summaries.clear(); //remove any summary with a rawCount of null
				}
				else{
					//query select clause
					String[] select = {"mean AS rawMean", "min","max","count AS rawCount","mean*count AS sums", "stDev As rawStdDev"};
					
					for(Iterator<Summary> it = summaries.iterator(); it.hasNext();){
						Summary summary = it.next();
						
						//create query filter
						List<String> entryFilter = new ArrayList<String>();
						int j = 1;
						for(String filter : summary.getFilters().keySet()){
							entryFilter.add(" get(keys, " + j++ + ") = '" + summary.getFilters().get(filter).toString() + "' ");
						}
						
						//query Tab collection
						ObjectQuery query = new ObjectQuery( Arrays.asList(select), Tab.class.getName(), entryFilter, data);
						List<List<Integer>> results = query.execute();
						
						//add data to summary
						if(results.size() > 0){
							List<Integer> selectValues = results.get(0);
							for(int i =0; i<query.getAlias().size(); i++){
								summary.getValues().put(query.getAlias().get(i), selectValues.get(i));
							}
						}
						
						//remove any summary with a rawCount of null
						if(summary.getValues().get("rawCount") == null){
							it.remove();
						}
					}
				}
			}
		}
		catch (Exception e) {
			log.warn("", e);
		}
	}
	
	/***
	 * Wrapper method that gets the collection of tab objects from the tabs using the trait-filter-filter name.
	 * @param summaries
	 * @param trait
	 * @param tabs
	 * @param filters
	 */
	public static void getTabulateStatistics(Collection<Summary> summaries, AsremlTrait trait, Tabs tabs, LinkedHashMap<String, Integer> filters){
		try{
 			if(null != summaries && null != tabs && null != trait && null != filters){
				
				//create tab id name ie yield-locId-genoId
				List<String> ids = new ArrayList<String>();
				ids.add(trait.getName());
				ids.addAll(filters.keySet());
				String col = AsremlOutputFile.createId(ids.toArray(new String[ids.size()]));
				
				Collection<Tab> data = tabs.getTabs().get(col);
				getTabulateStatistics(summaries, data);
			}
		}
		catch (Exception e) {
			log.warn("", e);
		}
	}
}