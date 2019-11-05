package asreml.input;

import java.nio.file.Paths;
import java.util.List;

import asreml.AsremlGlobals.AsConst;
import asreml.AsremlTrait;

/**
 * Asreml .asd file is an Asreml data file.  It is a flat files that contains the data to be used when running the .as file 
 * 
 * @author Scott Smith
 *
 */
public class AsremlAsd {

	private List<AsremlTrait> traits;
	private String filename; // full path of the .asd file
	private String directory;
	private boolean test;

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public boolean isTest() {
		return test;
	}

	public void setTest(boolean test) {
		this.test = test;
	}
	
	public List<AsremlTrait> getTraits() {
		return traits;
	}

	public void setTraits(List<AsremlTrait> traits) {
		this.traits = traits;
	}

	public AsremlAsd(String directory, List<AsremlTrait> traits, boolean test) {
		this.directory = directory;
		this.traits = traits;
		this.test = test;
		filename = Paths.get(directory,AsConst.prefix.value() + AsConst.asdSuffix.value()).toString();
	}
}