/*
 * 
 */
package data;

import java.nio.file.Path;

////////////////////////////////////////////////////
// The identifier attribute of the trait object is:  
// 1) the trait name. Example: moisture              
////////////////////////////////////////////////////
public class Trait {
	
	public static final String NEWLINE = System.getProperty("line.separator");
	private String strName;
	private Path objPath;
	private boolean bolConverged;
	private boolean bolBoundedVCs;

	public Trait(String strInName,
			     Path objInPath) {
		this.strName = strInName;
		this.objPath = objInPath;
		this.bolConverged = false;
		this.bolBoundedVCs = false;
	}
	
	@Override
	public boolean equals(Object objIn) {
		if ((objIn != null) && 
			(objIn instanceof Trait)) {
			return this.strName.equals(((Trait)objIn).getName());
		}
		else {
			return false;
		}
	}
	
	public String getName() {
		return this.strName;
	}
	
	public Path getPath() {
		return this.objPath;
	}
	
	public void setConverged(boolean bolInConverged) {
		this.bolConverged = bolInConverged;
	}
	
	public boolean getConverged() {
		return this.bolConverged;
	}

	public void setBoundedVCs(boolean bolInBoundedVCs) {
		this.bolBoundedVCs = bolInBoundedVCs;
	}
	
	public boolean getBoundedVCs() {
		return this.bolBoundedVCs;
	}
	
	@Override 
	public String toString() {
		String strOutput;
		
		strOutput = "Trait Name: " + this.strName + NEWLINE;
		strOutput = strOutput + "Path: " + this.objPath.toString() + NEWLINE;
		strOutput = strOutput + "Converged: " + this.bolConverged + NEWLINE;
		strOutput = strOutput + "BoundedVCs: " + this.bolBoundedVCs + NEWLINE;		
		strOutput = strOutput + NEWLINE;
		
		return strOutput;
	}
}
