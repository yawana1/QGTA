package data.xml.objects;

import java.util.HashMap;
import java.util.Map;

public class Constants {
	
	private Map<String,Object> constant;
	public final static Constants INSTANCE = new Constants();
	
	private Constants(){
		constant = new HashMap<String, Object>();
	}
	
	public Map<String, Object> getConstant() {
		return constant;
	}
	public void setConstant(Map<String, Object> constant) {
		this.constant = constant;
	}
	public Object getConstant(String key) {
		return constant.get(key);
	}
}