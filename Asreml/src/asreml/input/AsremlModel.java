/*
 * Using JRE 1.6.0_02
 * 
 * @package 	asreml.input
 * @class 		AsremlModel.java
 * @author 		u409397
 * @modified 	Jul 8, 2010
 * 
 */
package asreml.input;

import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import asreml.AsremlGlobals;
import asreml.AsremlGlobals.AsConst;
import asreml.AsremlTrait;
import asreml.output.AsremlOutput;
import utils.Funcs;

/**
 * The Class AsremlModel is used to define an ASREML command file.  The .as file
 */
public class AsremlModel implements Cloneable {

	static Logger log = Logger.getLogger(AsremlModel.class.getName());
	
	private String title;
	private String filename;
	private Path directory;
	private boolean test;
	private AsremlGrms grms;
	private AsremlTrait trait;
	protected List<AsremlTrait> traits;
	private AsremlColumns columns;
	private Double errorVariance;
	private AsremlQualifiers qualifiers;
	private AsremlAsd asd;
	private AsremlFixedEffects fixedEffects;
	private AsremlRandomEffects randomEffects;
	private AsremlTabs tabs;
	private AsremlPredictions predictions;
	private AsremlErrorVariances errorVariances;
	private int residual;
	private boolean mu = true; //default to true
	private Map<AsremlTrait, AsremlOutput> outputs;
	private String comments;

	/**
	 * Reuse asd and grm data already processed for another ASReml job
	 * @param asremlModel
	 */
	public AsremlModel(AsremlModel asremlModel){
		setColumns(asremlModel.getColumns());
		setAsd(asremlModel.getAsd());
		setDirectory(asremlModel.getDirectory());
		setFilename(asremlModel.getFilename());
		setGrms(asremlModel.getGrms());
		setTraits(asremlModel.getTraits());
	}
	
	/**
	 * Create a deep copy of this ASRemlModel
	 */
	public Object clone(){
		Object result = null;
		try {
			result = super.clone();
		} catch (CloneNotSupportedException e) {
			log.error(e);
		}
		return result;
	}
	
	public AsremlGrms getGrms() {
		return grms;
	}

	public void setGrms(AsremlGrms grms) {
		this.grms = grms;
	}
	
	public Double getErrorVariance() {
		return errorVariance;
	}

	public void setErrorVariance(Double errorVariance) {
		this.errorVariance = errorVariance;
	}

	public List<AsremlTrait> getTraits() {
		return traits;
	}

	public void setTraits(List<AsremlTrait> traits) {
		this.traits = traits;
	}

	public AsremlColumns getColumns() {
		return columns;
	}

	public void setColumns(AsremlColumns columns) {
		this.columns = columns;
	}

	public AsremlFixedEffects getFixedEffects() {
		return fixedEffects;
	}

	public void setFixedEffects(AsremlFixedEffects fixedEffects) {
		this.fixedEffects = fixedEffects;
	}

	public AsremlRandomEffects getRandomEffects() {
		return randomEffects;
	}

	public void setRandomEffects(AsremlRandomEffects randomEffects) {
		this.randomEffects = randomEffects;
	}

	public AsremlQualifiers getQualifiers() {
		return qualifiers;
	}

	public void setQualifiers(AsremlQualifiers qualifiers) {
		this.qualifiers = qualifiers;
	}

	public AsremlTabs getTabs() {
		return tabs;
	}

	public void setTabs(AsremlTabs tabs) {
		this.tabs = tabs;
	}

	public AsremlPredictions getPredictions() {
		return predictions;
	}

	public void setPredictions(AsremlPredictions predictions) {
		this.predictions = predictions;
	}

	public int getResidual() {
		return residual;
	}

	public void setResidual(int residual) {
		this.residual = residual;
	}

	public AsremlAsd getAsd() {
		return asd;
	}

	public void setAsd(AsremlAsd asd) {
		this.asd = asd;
	}

	public boolean getMu() {
		return mu;
	}

	public void setMu(boolean mu) {
		this.mu = mu;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Path getDirectory() {
		return directory;
	}

	public void setDirectory(Path directory) {
		this.directory = directory;
	}

	public boolean isTest() {
		return test;
	}

	public void setTest(boolean test) {
		this.test = test;
	}

	public AsremlTrait getTrait() {
		return trait;
	}

	public void setTrait(AsremlTrait trait) {
		this.trait = trait;
	}

	public AsremlErrorVariances getErrorVariances() {
		return errorVariances;
	}

	public void setErrorVariances(AsremlErrorVariances errorVariances) {
		this.errorVariances = errorVariances;
	}

	public Map<AsremlTrait, AsremlOutput> getOutputs() {
		return outputs;
	}

	public void setOutputs(Map<AsremlTrait, AsremlOutput> outputs) {
		this.outputs = outputs;
	}

	/**
	 * Instantiates a new asreml model.
	 */
	public AsremlModel(){
		outputs = new ConcurrentHashMap<>();
	}
	
	public AsremlModel(AsremlColumns columns, AsremlAsd asd, Path directory, List<AsremlTrait> traits){
		this.columns = columns;
		this.asd = asd;
		this.directory = directory;
		this.traits = traits;
		this.filename = AsremlGlobals.AsConst.prefix.value();
		outputs = new ConcurrentHashMap<>();
	}

	/***
	 * create Asreml .as file for this model
	 */
	public String toString(){
		StringBuffer str = new StringBuffer();
		str.append("!NOGRAPHICS");
		str.append(System.lineSeparator());
		str.append("title");
		str.append(System.lineSeparator());
		
		//add trait columns
		for(AsremlTrait t : asd.getTraits()){
			str.append(" " + t.getName());
			str.append(System.lineSeparator());
		}
		
		//factors
		str.append(columns.toString());
		
		//add grm files
		
		if(grms != null){
			str.append(grms.toString());
		}
		
		//add asd and qualifies
		str.append(asd.getFilename());
		str.append(" "); str.append(qualifiers.toString());
		
		//tab currently not using Asreml for tabulate functions
//		if(tabs != null){
//			tabs.setResponse(trait.getName());
//			str.append(tabs.toString());
//		}

		//set model trait
		str.append(System.lineSeparator());
		str.append(trait.getName());
		
		//set multiNomial
		if(trait.getMultiNomialScore() != null){
			str.append(" !MULT ");
			str.append(trait.getMultiNomialScore());
		}
		
		str.append(" ~");

		//set Trait for multinomial set as mu for the rest
		if(trait.getMultiNomialScore() != null){
			str.append(" ");
			str.append(AsremlGlobals.AsConst.Trait);
		}
		else{
			if(mu){
				str.append(" ");
				str.append(AsremlGlobals.AsConst.mu);
			}	
		}

		//fixed effects
		if(fixedEffects != null){
			str.append(fixedEffects.toString());
		}
		
		//random effects
		if(randomEffects != null){
			str.append(randomEffects.toString());
			str.append(System.lineSeparator());
			str.append("0 0 ");
			
			if(randomEffects.getRandomEffects() == null){
				str.append("0");
			}
			else{
				 str.append(randomEffects.getRandomEffects().size());
				 str.append(System.lineSeparator());
			}
			str.append(randomEffects.getStructuresAsString());
		}
		
		//prediction
		if(predictions != null){
			str.append(System.lineSeparator());
			str.append(predictions.toString());
		}
		
		str.append(comments == null ? "" : comments);
		return str.toString();
	}
	
	/**
	 * Write ASREML command to .as file.
	 * 
	 * @return the string file to which the ASREML command was saved
	 * @throws Exception 
	 */
	public Map<AsremlTrait, Path> writeScriptFile() throws Exception{
		Map<AsremlTrait, Path> files = new HashMap<>(); 
		for(AsremlTrait t: traits){
			try {
				trait = t;
				//skip all asreml functions if using only tabulate
				if(("ONLY TAB").equals(title)){
					AsremlOutput output = new AsremlOutput();
					output.setModel(this);
					outputs.put(trait, output);
				}
				else{
					Path dir = Paths.get(directory.toString(),t.toString());
					if(!Files.exists(dir)){
						Files.createDirectories(dir);
					}
					try{
						Files.setPosixFilePermissions(dir.getParent(), Files.getPosixFilePermissions(dir.getParent().getParent()));
						Files.setPosixFilePermissions(dir, Files.getPosixFilePermissions(dir.getParent().getParent()));
					}
					catch(AccessDeniedException e){
						//eat acess exceptions for permissions
					}
					
					Path file = dir.resolve(AsConst.prefix.value() + AsConst.modelSuffix.value());
					String fileContents = toString();
					Files.write(Funcs.createWithPermissions(file, file.getParent().getParent(),false), fileContents.getBytes());
					
					files.put(t,file);
				}
			} catch (Exception e) {
				log.error("AsremlModel.writeScriptFile()", e);
				throw e;
			}
		}
		return files;
	}
	
	/**
	 * After the asreml model xml template file is loaded check that all parts are correctly specified in the Trial.xml
	 * @throws Exception 
	 */
	public void validateModelFile() throws Exception{
		//check that there are columns for fixed effects
		AsremlFixedEffects fixedEffects = this.getFixedEffects();
		List<String> columnsNames = columns.getColumnNames();
		StringBuffer missingAsremlColumns = new StringBuffer();
		
		List<AsremlEffect> effects = new ArrayList<>();

		//check fixed effects
		if(fixedEffects != null){
			effects.addAll(fixedEffects.getFixedEffects());
		}

		//check random effects
		if(randomEffects != null){
			effects.addAll(randomEffects.getRandomEffects());
		}
		
		Collection<String> usedNames = new ArrayList<>(); 
		usedNames = checkEffects(effects);
		
		//check tabulate
		if(tabs != null){
			List<AsremlTab> tabList = tabs.getTabs();
			if(tabList != null){
				for(AsremlTab tab : tabs.getTabs()){
					List<String> factors = tab.getFactors();
					usedNames.addAll(factors);
				}
			}
		}
		
		for(String name : usedNames){
			if(!checkName(name, columnsNames)){
				missingAsremlColumns.append(name);
				missingAsremlColumns.append(" ");
			}
		}
		
		if(missingAsremlColumns.length() > 0){
			String error = "Aserml Model Template file " + title + " looking for columns not in set in the Trial: " + missingAsremlColumns;
			log.warn(error);
			//throw new Exception();
		}
	}
	
	/**
	 * Extract the individual column names from the fixed and random effects.
	 * @param effects - List of the random and fixed effects of a model
	 * @return - List of the names of all the columns in the effects
	 */
	private Collection<String> checkEffects(List<AsremlEffect> effects){
		Collection<String> columnNames = new ArrayList<>();
		if(effects != null){
			for(AsremlEffect effect : effects){
				columnNames.addAll(Arrays.asList(effect.name));
			}
		}
		return columnNames;
	}
	
	/**
	 * Check if name is in the list of columns allowed.
	 * Format the name to remove interactions and at(name,1) cases.
	 * @param name
	 * @param columnsNames
	 * @return
	 */
	private boolean checkName(String name, List<String> columnsNames){
		boolean result = false;
		
		//passe any variable that use at(varName, #) format
		String regex = "at\\((\\w+),\\d+\\)";
		if(name.matches(regex)){
			name = name.replaceAll(regex, "$1");
		}
		
		//break any interactions into each column.  Split on the .
		String[] names = name.split("\\.");
		
		for(String n : names){
			if(columnsNames.contains(n)){
				result = true;
			}
		}
		
		return result;
	}
}