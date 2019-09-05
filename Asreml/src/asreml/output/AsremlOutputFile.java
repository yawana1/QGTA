package asreml.output;

import java.nio.file.Path;
import java.util.List;

/**
 * Base class for any output file created by Asreml.  Defines the delimiter and file extension used by all files.
 * 
 * @author Scott Smith
 * @see Yhts
 * @see Slns
 */
public abstract class AsremlOutputFile {
	protected String ext = ".csv";  //default is cvs files
	protected String delimit = ",";
	protected Path filename;

	public AsremlOutputFile(Path filename, String ext, String delimit){
		this.filename = filename;
		this.ext = ext;
		this.delimit = delimit;
		init();
		load();
	}
	
	public AsremlOutputFile(){}
	
	protected abstract String getType();
	
	protected String getFullFileName(){
		return filename+getType()+ext;
	}
	
	public abstract void init();
	protected abstract void load();
	
	/**
	 * Creates an id with composite key from the list of ids
	 * <p>Example genoId-13</p>
	 * 
	 * @param ids
	 * @return
	 */
	public static String createId(String[] ids){
		String col = ids[0].trim();
		for(int i=1; i<ids.length;i++){
			col += "-"+ids[i].trim();
		}
		return col;
	}
	
	/**
	 * Creates an id with composite key from the list of ids
	 * 
	 * @param ids 
	 * @return - String representing the ids
	 */
	public static String createId(List<String> ids){
		String[] idsArray = new String[ids.size()];
		idsArray = ids.toArray(idsArray);
		return createId(idsArray);
	}
}
