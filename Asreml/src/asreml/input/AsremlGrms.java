package asreml.input;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold the list of Grm sections to write in the .as file.
 * 
 * @author Scott Smith
 *
 */
public class AsremlGrms {
	private List<AsremlGrm> grms;
	
	public AsremlGrms() {
		grms = new ArrayList<AsremlGrm>();
	}
	
	public List<AsremlGrm> getGrms() {
		return grms;
	}
	public void setGrms(List<AsremlGrm> grms) {
		this.grms = grms;
	}
	
	public boolean isEmpty(){
		boolean result = true;
		if(null != grms && !grms.isEmpty()){
			result = false;
		}
		return result;
	}
	
	public String toString(){
		String str = "";
		for(AsremlGrm grm: grms){
			str += grm;
		}
		return str;
	}
}