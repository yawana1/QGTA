package db;

import java.util.HashMap;
import java.util.Map;

import data.xml.objects.Crop;
import data.xml.objects.DBType;

/**
 * Cache of sessions to all datasources.
 * 
 * @author Scott Smith
 *
 */
public class Sessions {

	public final static Sessions INSTANCE = new Sessions();
	private DataSources dataSources;
	private Map<Crop, Map<DBType, Session>> cache;
	private Map<String, DataSourceType> types;
	
    public Map<String, DataSourceType> getTypes() {
		return types;
	}

	public void setTypes(Map<String, DataSourceType> types) {
		this.types = types;
	}
	
	private Sessions(){
		dataSources = DataSources.INSTANCE;
		cache = new HashMap<Crop, Map<DBType,Session>>();
	}
	
	/***
	 * XStream method called after deserialization
	 * @return
	 */
	public Object readResolve(){
		DataSources.INSTANCE.setDataSources(dataSources.getDataSources());
		dataSources = DataSources.INSTANCE;
		return this;
	}
	
	public Session get(Crop crop, DBType dbType ) {
		loadFromCache(crop, dbType);
        return cache.get(crop).get(dbType);
	 }

	private void loadFromCache(Crop crop, DBType dbType) {
		if(!cache.containsKey(crop)){
			cache.put(crop, new HashMap<DBType, Session>());
		}
		Map<DBType,Session> sessionMap = cache.get(crop);
		if(!sessionMap.containsKey(dbType)){
			Session session = new Session(dataSources.get(crop).get(dbType));
			if(session.isConnected()){
				sessionMap.put(dbType, session);
			}
		}
	}
}
