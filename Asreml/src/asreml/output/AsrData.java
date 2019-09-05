/*
 * Using JRE 1.6.0_02
 * 
 * @package 	asreml.output.asr
 * @class 		AsrData.java
 * @author 		u409397
 * @modified 	Jul 8, 2010
 * 
 */
package asreml.output;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import asreml.AsremlGlobals.AsConst;
import asreml.AsremlTrait;

// TODO: Auto-generated Javadoc
/**
 * The Class AsrData.
 */
public class AsrData {

	static Logger logger = Logger.getLogger(AsrData.class.getName());
	private final static String ext = ".asr";
	private boolean converged = false;
	private int outliers = 0;
	private Path filename;
	private Map<String, Map<AsremlTrait, AsrVariance>> variance = new HashMap<String, Map<AsremlTrait, AsrVariance>>(); //term-trait-AsrVariance
	private HashMap<String, AsrAnalysisOfVariance> analysisOfVariance = new HashMap<String, AsrAnalysisOfVariance>();
	private int fixedEffects = 0;
	private int randomEffects = 0;
	private int mu = 0;	
	private List<AsremlTrait> traits;
	private Double errorVariance;
	private final static String VARIANCE_NAME_V3 = "Variance";
	private final static String VARIANCE_NAME_V4 = "Residual";
	
	/**
	 * Checks if is converged.
	 * 
	 * @return true, if is converged
	 */
	public boolean isConverged() {
		return converged;
	}
	
	/**
	 * Sets the converged.
	 * 
	 * @param converged
	 *            the new converged
	 */
	public void setConverged(boolean converged) {
		this.converged = converged;
	}
	
	public int getOutliers() {
		return outliers;
	}
	
	public void setOutliers(int outliers) {
		this.outliers = outliers;
	}
	
	public List<AsremlTrait> getTraits(){
		return traits;
	}
	
	public void setTraits(List<AsremlTrait> traits){
		this.traits = traits;
	}
	/**
	 * Gets the filename.
	 * 
	 * @return the filename
	 */
	public Path getFilename() {
		return filename;
	}
	
	/**
	 * Gets the variance.
	 * 
	 * @return the variance
	 */
	public Map<String, Map<AsremlTrait, AsrVariance>> getVariance() {
		return variance;
	}
	
	/**
	 * Sets the variance.
	 * 
	 * @param variance
	 *            the variance
	 */
	public void setVariance(Map<String, Map<AsremlTrait, AsrVariance>> variance) {
		this.variance = variance;
	}
	
	/**
	 * Gets the analysis of variance.
	 * 
	 * @return the analysis of variance
	 */
	public HashMap<String, AsrAnalysisOfVariance> getAnalysisOfVariance() {
		return analysisOfVariance;
	}
	
	/**
	 * Sets the analysis of variance.
	 * 
	 * @param analysisOfVariance
	 *            the analysis of variance
	 */
	public void setAnalysisOfVariance(
			HashMap<String, AsrAnalysisOfVariance> analysisOfVariance) {
		this.analysisOfVariance = analysisOfVariance;
	}
	
	/**
	 * Sets the filename.
	 * 
	 * @param filename
	 *            the new filename
	 */
	public void setFilename(Path filename) {
		this.filename = filename;
	}
	
	/**
	 * Gets the ext.
	 * 
	 * @return the ext
	 */
	public String getExt() {
		return ext;
	}
	
	/**
	 * Instantiates a new asr data.
	 * 
	 * @param filename
	 *            the filename
	 * @param fixedEffects
	 *            the fixed effects
	 * @param randomEffects
	 *            the random effects
	 * @param mu
	 *            the mu
	 * @param errorVariance2 
	 */
	public AsrData(Path filename, int fixedEffects, int randomEffects, int mu, List<AsremlTrait> traits, Double errorVariance){
		this.filename = filename;
		this.fixedEffects = fixedEffects;
		this.randomEffects = randomEffects;
		this.mu = mu;
		this.traits = traits;
		this.errorVariance = errorVariance;
		load();
	}

	/**
	 * Load.
	 */
	public void load(){
		File file = new File(filename+ext);
		if(!file.exists()){
			logger.warn("AsrData.load: "+file.getPath()+" does not exists");
			return;
		}
		setConvergence(file);
		setVariance(file);
		setAnalysisOfVariance(file);
			
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		String nl = "\n";
		String str = (converged ? AsConst.converged.value() : "Error") + nl+nl;
		str += "Source Model terms Gamma Component Comp/SE % C"+nl;
		for(String source : variance.keySet()){
			str += variance.get(source).toString()+nl;
		}
		str += nl;
		str += "Analysis of Variance NumDF F_inc"+nl;
		for(String effect : analysisOfVariance.keySet()){
			str += analysisOfVariance.get(effect).toString()+nl;
		}
		return str;
	}

	/**
	 * Sets the convergence.
	 * 
	 * @param file
	 *            the new convergence
	 */
	public void setConvergence(File file){
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(file));
			String str;
			while ((str = br.readLine()) != null){
				str = str.trim().replaceAll("\\s+", " ");
				if(str.contains("possible outliers:")){
					outliers = Integer.parseInt(str.split("\\s+")[0]);
				}
				else if(str.startsWith("Finished:") && !str.endsWith("Local??")){
					converged = str.contains(AsConst.converged.value()) ? true : false;
					break;
				}
			}
		}catch(IOException e){
			logger.error("AsrData.setConvergence", e);
		}finally{
			try {
				if(br != null) br.close();
			} catch (IOException e) {
				logger.error("AsrData.setConvergence", e);
			}
		}
	}

	/**
	 * Sets the variance.
	 * 
	 * @param file
	 *            the new variance
	 */
	public void setVariance(File file){
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(file));
			String str;
			while ((str = br.readLine()) != null){
				str = str.trim().replaceAll("\\s+", " ");
				String header_v4 = "Model_Term Gamma Sigma Sigma/SE % C";
				String header_v3 = "Source Model terms Gamma Component Comp/SE % C";
				if(str.equals(header_v3) || str.equals(header_v4)){
					int version = 4;
					if(str.equals(header_v3)){
						version = 3;
					}
					
					for(int i=0; i<(randomEffects+1)*((traits.size()*(traits.size()+1))/2); i++){
						str = br.readLine();
						if(str==null || str.equals("") || str.startsWith(" Warning:")) break;
						ArrayList<String> array = new ArrayList<String>();
						int endIndex = 20;
						if(version == 3){
							//File only allocates 20 chars for Source name and name may be longer then that
							int offset = 7;
							String name = str.substring(1, endIndex).trim();
							String model = str.substring(endIndex++, endIndex = endIndex+offset).trim();
							String term = str.substring(endIndex, endIndex = endIndex+offset).trim();
							
							//reshuffle since no defined column widths
							//if first char a letter move it to the model
							if(term.matches("^[a-zA-Z][0-9\\s].*")){
								model += term.substring(0, 1);
								term = term.substring(1).trim();
							}
							
							array.add(name);
							array.add(model);
							array.add(term);
						}
						else if(version == 4){
							//File only allocates 20 chars for Source name and name may be longer then that
							endIndex = 22;
							int offset = 7;
							String name = str.substring(1, endIndex).trim();
							String model = str.substring(endIndex++, endIndex = endIndex+offset).trim();
							String term = str.substring(endIndex, endIndex = endIndex+offset).trim();
							
							//reshuffle since no defined column widths
							//if first char a letter move it to the model
							if(term.matches("^[a-zA-Z][0-9\\s].*")){
								model += term.substring(0, 1);
								term = term.substring(1).trim();
							}
							
							array.add(name);
							array.add(model);
							array.add(term);
						}
						array.addAll(Arrays.asList(str.substring(endIndex, str.length()).trim().split("\\s+")));
						String[] arr = new String[array.size()];
						array.toArray(arr);
						if(arr.length<8) continue;
						try{
							AsrVariance asr = new AsrVariance(arr);
							if(errorVariance != null){
								asr.setComponent(errorVariance);	
							}
							//only save the diagonals or single terms
							if(asr.getTerms().size() != 0){
								Map<AsremlTrait, AsrVariance> traitVariances = new HashMap<AsremlTrait, AsrVariance>();
								if(asr.getTerms().size() == 1){ //univariate always use the first and only trait
									traitVariances.put(traits.get(0), asr);
								}
								else if(asr.getTerms().get(0) == asr.getTerms().get(1)){ //use only trait variance diagonals where term = 1 1 , 2 2, 3 3 etc.
									traitVariances.put(traits.get(asr.getTerms().get(0)-1), asr);
								}
								if(!variance.containsKey(asr.getSource())){
									variance.put(asr.getSource(), new HashMap<AsremlTrait, AsrVariance>());
								}
								variance.get(asr.getSource()).putAll(traitVariances);
							}
						}catch(Exception e){
							logger.warn("Error reading AsrVariance - "+str+"\n",e);
						}
					}
					break;
				}
			}
		}catch(IOException e){
			logger.error("AsrData.setVariance", e);
		}finally{
			try {
				if(br != null) br.close();
			} catch (IOException e) {
				logger.error("AsrData.setVariance", e);
			}
		}
	}
	
	/**
	 * Sets the analysis of variance.
	 * 
	 * @param file
	 *            the new analysis of variance
	 */
	public void setAnalysisOfVariance(File file){
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(file));
			String str;
			while ((str = br.readLine()) != null){
				str = str.trim().replaceAll("\\s+", " ");
				if(str.startsWith("Source of Variation NumDF F-inc") 
						|| str.startsWith("Source of Variation NumDF DenDF F-inc P-inc") 
						|| str.startsWith("Source of Variation NumDF DenDF_con F-inc F-con M P-con")){
					for(int i=0; i<(fixedEffects+mu); i++){
						str = br.readLine();
						if(str==null || str.equals("")) break;
						try{
							AsrAnalysisOfVariance asr = new AsrAnalysisOfVariance(str);
							if(asr.isValid())
								analysisOfVariance.put(asr.getEffect(), asr);
						}catch(Exception e){
							logger.warn("Error reading AsrAnalysisOfVariance - "+str+"\n",e);
						}
					}
					break;
				}
			}
		}catch(IOException e){
			logger.error("AsrData.setAnalysisOfVariance", e);
		}finally{
			try {
				if(br != null) br.close();
			} catch (IOException e) {
				logger.error("AsrData.setAnalysisOfVariance", e);
			}
		}
	}
	
	/***
	 * Use for univariate runs where there is only one term.  Returns Effect, Variance Component Map without the trait
	 * @param trait
	 * @return
	 */
	public Map<String, Double> getVarianceComponets(AsremlTrait trait){
		//parse ASR file to get variances
		Map<String, Double> result = new HashMap<String, Double>();
		for(String effectName: getVariance().keySet()){
				result.put(effectName, getVariance().get(effectName).get(trait).getComponent());
		}
		return result;
	}
	
	/***
	 * Get for column 'like' the name.
	 * ASR truncates long named columns so must search using only the first 20 chars
	 * possible error is the first chars match up and only differ passed the ASR file cutoff
	 * @param column
	 * @return
	 */
	public Double getVarianceComponets(String column, AsremlTrait trait){
		Double result = null;
		
		if(column.matches("at\\(.+\\).+")){
			column = column.replace("at(", "").replace(",","_").replace(")", "");
		}
		
		for(String effectName: getVariance().keySet()){
			if(column.startsWith(effectName)){
				result = getVariance().get(effectName).get(trait).getComponent();
				break;
			}
			if(result == null && column.equals(VARIANCE_NAME_V3)){
				if(getVariance().containsKey(VARIANCE_NAME_V4)){
					result = getVariance().get(VARIANCE_NAME_V4).get(trait).getComponent();
					break;
				}
			}
		}
		return result;
	}
	
	public Map<String,AsrVariance> getVariance(AsremlTrait trait) {
		//parse ASR file to get variances
		Map<String, AsrVariance> result = new HashMap<String, AsrVariance>();
		for(String effectName: getVariance().keySet()){
				result.put(effectName, getVariance().get(effectName).get(trait));
		}
		return result;
	}
}