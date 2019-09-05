package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Used as a helper class to create JOSql queries on basic collections
 *  <p><a href="http://josql.sourceforge.net">http://josql.sourceforge.net</a></p>
 * @author Scott Smith
 *
 */
public class ObjectQuery {

	private static Logger log = Logger.getLogger(ObjectQuery.class.getName());
	
	private List<String> select;
	private String from;
	private List<String> where;
	private List<String> group;
	private Collection<?> data;
	private List<String> alias;

	public List<String> getAlias() {
		return alias;
	}
	public void setAlias(List<String> alias) {
		this.alias = alias;
	}
	public List<String> getSelect() {
		return select;
	}
	public void setSelect(List<String> select) {
		this.select = select;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public List<String> getWhere() {
		return where;
	}
	public void setWhere(List<String> where) {
		this.where = where;
	}
	public List<String> getGroup() {
		return group;
	}
	public void setGroup(List<String> group) {
		this.group = group;
	}
	public Collection<?> getData() {
		return data;
	}
	public void setData(Collection<?> data) {
		this.data = data;
	}
	public ObjectQuery(List<String> select, String from, List<String> where, Collection<?> data) {
		super();
		this.select = select;
		this.from = from;
		this.where = where;
		this.data = data;
	}
	
	public ObjectQuery(String select, String from, String where, Collection<?> data) {
		super();
		this.select = new ArrayList<String>();
		this.select.add(select);
		
		this.from = from;
		this.where = new ArrayList<String>();
		this.where.add(where);
		this.data = data;
	}
	
	public ObjectQuery(String select, String from, List<String> where, Collection<?> data) {
		super();
		this.select = new ArrayList<String>();
		this.select.add(select);
		
		this.from = from;
		this.where = where;
		this.data = data;
	}
	
	public ObjectQuery(String select, String from, List<String> where, List<String> group, Collection<?> data) {
		super();
		this.select = new ArrayList<String>();
		this.select.add(select);
		
		this.from = from;
		this.where = where;
		this.group = group;
		this.data = data;
	}
	
	public ObjectQuery(List<String> select, String from, List<String> where, List<String> group, Collection<?> data) {
		super();
		this.select = select;
		
		this.from = from;
		this.where = where;
		this.group = group;
		this.data = data;
	}
	
	public <T> List<T> execute(){
		List<T> result = null;
		try {
			alias = Arrays.asList(new String[select.size()]);
			Collections.copy(alias, select);
			result = Funcs.queryData(toString(), null, getData(), alias);
		} catch (Exception e) {
			log.warn("", e);
		}
		return result;
	}
	
	public String toString(){
		String result = "";
		result += "SELECT ";
		if(null == select){
			result +=  " * ";
		}
		else{
			for(String s : select){
				result += s + ",";
			}
		}
		result = result.substring(0, result.length()-1);
		result += "\n FROM " + from;
		
		if(null != where && where.size() > 0 ){
				result += "\n WHERE ";
				for(String s : where){
					result += s + " AND ";
				}
			result = result.substring(0, result.length()-4);
		}
		
		if(null != group && group.size() > 0){
			result += "\n GROUP BY ";
			for(String groupBy : group){
				result += groupBy + ",";
			}
			result = result.substring(0, result.length()-1);
		}
		
		return result;
	}
}