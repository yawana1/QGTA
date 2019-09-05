package asreml.input;

/**
 * Class to define writing the grm section of the .as file
 * 
 * @author Scott Smith
 *
 */

public class AsremlGrm {
	private String grmFile;
	private boolean nsd;
	
	public AsremlGrm(String grmFile, boolean nsd) {
		super();
		this.grmFile = grmFile;
		this.nsd = nsd;
	}
	public String getGrmFile() {
		return grmFile;
	}
	public void setGrmFile(String grmFile) {
		this.grmFile = grmFile;
	}
	public boolean isNsd() {
		return nsd;
	}
	public void setNsd(boolean nsd) {
		this.nsd = nsd;
	}
	
	public String toString(){
		String str = nsd ? " !NSD " : "" ;
		return grmFile + str + System.getProperty("line.separator");
	}
}
