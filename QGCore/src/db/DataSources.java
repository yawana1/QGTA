package db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.xml.objects.Crop;


public class DataSources {

	public final static DataSources INSTANCE = new DataSources();
	private List<TADataSource> dataSources;
	private Map<Crop,TADataSource> dataSourceByKey;
	private boolean mapLoaded = false;
	
	private DataSources(){
		dataSources = new ArrayList<TADataSource>();
		dataSourceByKey = new HashMap<Crop, TADataSource>();
	}

	public TADataSource get(Crop key){
		if(!mapLoaded){
			loadMap();
			mapLoaded = true;
		}
		return dataSourceByKey.get(key);
	}

	private void loadMap() {
		for(TADataSource ds: dataSources){
			dataSourceByKey.put(ds.getCrop(), ds);
		}
	}

	public List<TADataSource> getDataSources() {
		return dataSources;
	}

	public void setDataSources(List<TADataSource> dataSources) {
		this.dataSources = dataSources;
	}
}