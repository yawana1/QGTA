/*
 * Using JRE 1.6.0_02
 * 
 * @package 	asreml.output
 * @class 		AsremlOutput.java
 * @author 		u409397
 * @modified 	Jul 8, 2010
 * 
 */
package asreml.output;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import asreml.AsremlGlobals;
import asreml.AsremlGlobals.OutputFormat;
import asreml.AsremlTrait;
import asreml.input.AsremlModel;

/**
 * The Class AsremlOutput is used to organize outputs from ASREML.
 */
public class AsremlOutput {

	/** The logger. */
	static Logger logger = Logger.getLogger(AsremlOutput.class.getName());
	private AsrData asr;
	private Slns slns;
	private Yhts yhts;
	private Tabs tabs;
	private Pvs pvs;
	private Res res;
	private Path filename; //full path
	private AsremlModel model;
	
	public AsrData getAsr() {
		return asr;
	}

	public void setAsr(AsrData asr) {
		this.asr = asr;
	}

	public Slns getSlns() {
		return slns;
	}

	public void setSlns(Slns slns) {
		this.slns = slns;
	}

	public Yhts getYhts() {
		return yhts;
	}

	public void setYhts(Yhts yhts) {
		this.yhts = yhts;
	}

	public Tabs getTabs() {
		return tabs;
	}

	public void setTabs(Tabs tabs) {
		this.tabs = tabs;
	}

	public Pvs getPvs() {
		return pvs;
	}

	public void setPvs(Pvs pvs) {
		this.pvs = pvs;
	}
	
	public Res getRes() {
		return res;
	}

	public void setRes(Res res) {
		this.res = res;
	}

	public Path getFilename() {
		return filename;
	}

	public void setFilename(Path filename) {
		this.filename = filename;
	}

	public AsremlModel getModel() {
		return model;
	}

	public void setModel(AsremlModel model) {
		this.model = model;
	}
	
	public AsremlOutput(){
		this.tabs = new Tabs();
	}
	
	/**
	 * Read back in all Asreml output files that are required
	 * Currently
	 * <p>.asr - Convergence and Variance numbers
	 * <p>.sln - Estimates and errors of effects
	 * <p>.yht - Predicted values and residuals
	 * 
	 * @param model
	 * @param trait
	 * @param errorVariance
	 */
	public AsremlOutput(AsremlModel model, AsremlTrait trait, Double errorVariance){
		this.filename = Paths.get(model.getDirectory().toString(),trait.getName(),model.getFilename());
		this.model = model;
		
		List<AsremlTrait> traits = new ArrayList<>();
		traits.add(trait);
		int fixedEffectCount = (model.getFixedEffects()==null ? 0 : model.getFixedEffects().size());
		this.asr = new AsrData(this.filename, fixedEffectCount, (model.getRandomEffects()==null ? 0 : model.getRandomEffects().size()), 1, traits, errorVariance);
		//find Asreml file output format and file extensions
		AsremlGlobals.OutputFormat outputFormat = OutputFormat.get(model.getQualifiers().get("!TXTFORM").getValue());
		this.slns = new Slns(this.filename, outputFormat.getExt(), outputFormat.getDelimit());
		this.yhts = new Yhts(this.filename, outputFormat.getExt(), outputFormat.getDelimit());
		this.pvs = new Pvs(this.filename, outputFormat.getExt(), outputFormat.getDelimit());
		this.res = new Res(this.filename);
		//no long use Asreml tab
		this.tabs = new Tabs();

	}
	
	/**
	 * Gets the file name without extension.
	 * 
	 * @param fileName
	 *            the file name
	 * 
	 * @return the file name without extension
	 */
	public static String getFileNameWithoutExtension(String fileName) {
		File tmpFile = new File(fileName);
		tmpFile.getName();
		int whereDot = tmpFile.getName().lastIndexOf('.');
		if (0 < whereDot && whereDot <= tmpFile.getName().length() - 2 ) {
			return tmpFile.getName().substring(0, whereDot);
		}
		return "";
	}
	
	public boolean isConverged(){
		boolean converged = false;
		if(asr != null){
			converged = asr.isConverged();
		}
		else if(model.getTitle().equals("ONLY TAB")){
			converged = true;
		}
		return converged;
	}
}