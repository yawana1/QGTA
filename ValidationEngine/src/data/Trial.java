/*
 * 
 */
package data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

////////////////////////////////////////////////////
//The identifier attribute of the trial object is:  
//1) the trial name. Example: F3311AB                                    
////////////////////////////////////////////////////
public class Trial {
	
	public static final String NEWLINE = System.getProperty("line.separator");
	private String strTrialName;
	private Path objPath;
	private List<Model> lstModels;
	
	public Trial(String strInTrialName,
			     Path objInPath) {
		this.strTrialName = strInTrialName;
		this.objPath = objInPath;
		/////////////////////////////////////////////////////////////////////////////
		// It is estimated that the maximum number of models a trial can have is 10  
		/////////////////////////////////////////////////////////////////////////////
		this.lstModels = new ArrayList<Model>(10);
	}

	@Override
	public boolean equals(Object objIn) {
		if ((objIn != null) && 
			(objIn instanceof Trial)) {
			return (this.strTrialName.equals(((Trial)objIn).getTrialName()));
		}
		else {
			return false;
		}
	}
	
	public void addModel(Model objInModel) {
		this.lstModels.add(objInModel);
	}

	public String getTrialName() {
		return this.strTrialName;
	}
	
	public Path getPath() {
		return this.objPath;
	}
	
	public List<Model> getModels() {
		return this.lstModels;
	}
	
	@Override 
	public String toString() {
		String strOutput;
		
		strOutput = "Name Trial: " + this.strTrialName + NEWLINE;
		strOutput = strOutput + "Path: " + this.objPath.toString() + NEWLINE;
		strOutput = strOutput + "Models: " + NEWLINE;
		for (Model objModel : this.lstModels) { 
			strOutput = strOutput + "\t" + "Model Name: " + objModel.getName() + NEWLINE;
			strOutput = strOutput + "\t" + "Path: " + objModel.getPath().toString() + NEWLINE;
			strOutput = strOutput + "\t" + "Traits: " + NEWLINE;
			for (Trait objTrait : objModel.getTraits()) {
				strOutput = strOutput + "\t\t" + "Trait Name: " + objTrait.getName() + NEWLINE;
				strOutput = strOutput + "\t\t" + "Path: " + objTrait.getPath().toString() + NEWLINE;
				strOutput = strOutput + "\t\t" + "Converged: " + objTrait.getConverged() + NEWLINE;
				strOutput = strOutput + "\t\t" + "Bounded VCs: " + objTrait.getBoundedVCs() + NEWLINE;			
			}
		}
		strOutput = strOutput + NEWLINE;
		
		return strOutput;
	}
}
