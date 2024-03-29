/*
 * 
 */
package data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/////////////////////////////////////////////////////
// The identifier attribute of the model object is:  
// 1) the model name. Example: anova                                    
/////////////////////////////////////////////////////
public class Model {

	public static final String NEWLINE = System.getProperty("line.separator");
	private String strName;
	private Path objPath;
	private List<Trait> lstTraits;
	
	public Model(String strInName,
		         Path objInPath) {
		this.strName = strInName;
		this.objPath = objInPath;
		/////////////////////////////////////////////////////////////////////////////
		// It is estimated that the maximum number of traits a model can have is 30  
		/////////////////////////////////////////////////////////////////////////////
		this.lstTraits = new ArrayList<Trait>(30);
	}

	@Override
	public boolean equals(Object objIn) {
		if ((objIn != null) && 
			(objIn instanceof Model)) {
			return this.strName.equals(((Model)objIn).getName());
		}
		else {
			return false;
		}
	}
	
	public void addTrait(Trait objInTrait) {
		this.lstTraits.add(objInTrait);
	}
	
	public String getName() {
		return this.strName;
	}
	
	public Path getPath() {
		return this.objPath;
	}
	
	public List<Trait> getTraits() {
		return this.lstTraits;
	}
	
	@Override 
	public String toString() {
		String strOutput;
		
		strOutput = "Model Name: " + this.strName + NEWLINE;
		strOutput = strOutput + "Path: " + this.objPath.toString() + NEWLINE;
		strOutput = strOutput + "Traits: " + NEWLINE;
		for (Trait objTrait : this.lstTraits) {
			strOutput = strOutput + "\t" + "Trait Name: " + objTrait.getName() + NEWLINE;
			strOutput = strOutput + "\t" + "Path: " + objTrait.getPath().toString() + NEWLINE;
			strOutput = strOutput + "\t" + "Converged: " + objTrait.getConverged() + NEWLINE;
			strOutput = strOutput + "\t" + "Bounded VCs: " + objTrait.getBoundedVCs() + NEWLINE;			
		}
		strOutput = strOutput + NEWLINE;
		
		return strOutput;
	}
}
