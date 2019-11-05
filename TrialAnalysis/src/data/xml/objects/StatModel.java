package data.xml.objects;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

/**
 * Holds path to config file that defines a model.
 * 
 * @author Scott Smith
 *
 */
public class StatModel {

	private static Logger log = Logger.getLogger(StatModel.class.getName());
	
	private String name;
	private String file;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public StatModel(){
		
	}
	public StatModel(String name){
		this.name = name;
	}
	
	public String toString(){
		return name;
	}
	
	public boolean equals(Object o){
		boolean  result = false;
		if(o != null && o.toString() != null && name != null){
			result = name.equals(o.toString());
		}
		return result;
	}
	
	/***
	 * Load statModle with full cache Model values
	 * @return
	 */
	public Object readResolve(){
		StatModel model = this;
		//TODO fix hack
		if(!model.getFile().contains(":")){
			model.setFile(Paths.get(App.INSTANCE.getAsremlDirectory(), model.getFile()).toString());
		}
		if(null != StatModels.INSTANCE.getModels()){
			model = StatModels.INSTANCE.get(name);
		}
		return model;
	}
	
	public boolean isValid() {
		boolean result = false;
		if(name == null){
			log.warn("Stat Model missing name");
		}
		else if(file == null){
			log.warn("Stat Model missing file name");
		}
		else{
			result = true;
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}
}