package data.xml.objects;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class DataLevelParams {

    @XStreamImplicit(itemFieldName="environment")
	private String environment;
    @XStreamImplicit(itemFieldName="genoType")
	private String genoType;
	
	public Map<String,String> getDataLevelMap() {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("environment", environment);
		parameters.put("genoType", genoType);
		return parameters;
	}
	
	public void setDataLevel(Map<String,String> dataLevel) {
		environment = dataLevel.get("environment");
		genoType = dataLevel.get("genoType");
	}
}
