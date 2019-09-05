package transformation;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import asreml.AsremlGlobals;
import asreml.AsremlGlobals.GrmType;
import cov.MatrixOptions;
import cov.RME;
import data.collection.ExpFBKs;
import data.xml.objects.Constants;
import error.ErrorMessage;
import utils.Funcs;

/**
 * Transformation to create a GRM/GIV file to be used by Asreml.  This is done by creating the unix command and calling
 * the covariance engine.
 * Currently both GRM and the inverted GIV are being created by the covariance engine but only the GIV is used.
 * 
 * @author Scott Smith
 *
 */
public class Grm extends Transformation{

	static Logger log = Logger.getLogger(Grm.class.getName());

	private String insilicoFile;
	private List<String> idColumns;
	private String rename; //rename the covariance engine file to this
	private GrmType grmType = GrmType.hybrid; //default to hybrid
	private RME rme;
	
	public AsremlGlobals.GrmType getGrmType() {
		return grmType;
	}

	public void setGrmType(AsremlGlobals.GrmType grmType) {
		this.grmType = grmType;
	}

	public Collection<String> getIdColumns() {
		return idColumns;
	}

	public void setIdColumns(List<String> idColumns) {
		this.idColumns = idColumns;
	}

	public Grm(){
		idColumns = new ArrayList<String>();
	}

	/**naTes
	 * Call off to run the {@link utils.Grm} to run the Covariance engine and then reset the levels of columns as the collapsing of base genetics by the
	 * covariance engine can change it.
	 */
	public void run(){
		String scriptFile = Constants.INSTANCE.getConstant("gblup_script_file") != null ? Constants.INSTANCE.getConstant("gblup_script_file").toString() : null;
		String genoTxtFile = Constants.INSTANCE.getConstant("geno_txt") != null ? Constants.INSTANCE.getConstant("geno_txt").toString() : null;
		String hybridMapFile = Constants.INSTANCE.getConstant("hybrid_map") != null ? Constants.INSTANCE.getConstant("hybrid_map").toString() : null;
		String pedigreeFile = Constants.INSTANCE.getConstant("pedigree_txt") != null ? Constants.INSTANCE.getConstant("pedigree_txt").toString() : null;
		String similarityFile = Constants.INSTANCE.getConstant("similarity_txt") != null ? Constants.INSTANCE.getConstant("similarity_txt").toString() : null;
		String matrixFormat = Constants.INSTANCE.getConstant("matrix_format") != null ? Constants.INSTANCE.getConstant("matrix_format").toString() : null;
		Map<String, List<Object>> data = trial.getFbks().getColMap();
		
		if(grmType == null){
			grmType = GrmType.hybrid; //default to hybrid
		}
		
		if(scriptFile == null){
			log.error(ErrorMessage.INSTANCE.getMessage("no_covariance_script"));
		}
		else if(genoTxtFile == null){
			log.error(ErrorMessage.INSTANCE.getMessage("no_geno_txt"));
		}
		else if(hybridMapFile == null){
			log.error(ErrorMessage.INSTANCE.getMessage("no_hybrid_map"));
		}
		else{
			for(String idColumn : idColumns){				
				rme.setCrop(trial.getCrop().toString());
				rme.setRunPedigree(rme.getRunPedigree()==null ? true:rme.getRunPedigree());
				rme.setPedigreeFileName(rme.getPedigreeFileName()==null? pedigreeFile:rme.getPedigreeFileName());
				rme.setRunSimilarity(rme.getRunSimilarity()==null?true:rme.getRunSimilarity());
				rme.setSimilarityFileName(rme.getSimilarityFileName()==null? similarityFile:rme.getSimilarityFileName());
				rme.setRunMatrixOperations(rme.getRunMatrixOperations()==null?true:rme.getRunMatrixOperations());
				rme.setRunBend(rme.getRunBend()==null ? true : rme.getRunBend());
				rme.setGenoIdFile(rme.getGenoIdFile()==null? genoTxtFile:rme.getGenoIdFile());
				MatrixOptions matrixOptions = rme.getMatrixOptions();
				matrixOptions.setMatrixFormat(matrixOptions.getMatrixFormat()==null?matrixFormat:matrixOptions.getMatrixFormat());
				matrixOptions.setMatrixInvert(matrixOptions.getMatrixInvert()==null?true:matrixOptions.getMatrixInvert());
				matrixOptions.setReduction(matrixOptions.getReduction()==null?true:matrixOptions.getReduction());
				rme.setMatrixOptions(matrixOptions);
				
				List<String> customData = null;
				if(insilicoFile != null){
					Path path = Paths.get(trial.getWorkingDirectory(), insilicoFile);
					if(Files.exists(path)){
						try {
							customData = Files.readAllLines(path, Charset.defaultCharset());
						}
						catch (IOException e) {
							log.error(ErrorMessage.INSTANCE.getMessage("file_load") + insilicoFile, e);
						}
					}
					customData = new ArrayList<String>();
				}
				
				utils.Grm grm = new utils.Grm(data, customData, idColumn, rename, trial.getTrialWorkDirectory(), hybridMapFile);
				int level = grm.getGrmData(scriptFile, rme);
				trial.getColumns().get(idColumn).setLevel(level);
				
				//create .grm file with line names and relationship value only for grm files, not .giv
				if(GrmType.hybrid.equals(grmType)){
					try{
						Path grmFile = Paths.get(trial.getTrialWorkDirectory(),rename+".grm");
						//createGrmGenoNameFile(grmFile, idColumn, "lineName", trial.getFbks());
					}
					catch(Exception e){
						log.error("", e);
					}
				}
			}
		}
	}
	
	/**
	 * Read in grm file and create new file with genoId's in the grm replaced with Geno Names
	 * 
	 * @param grm - File to create a new geno name file from
	 * @throws Exception 
	 */
	private void createGrmGenoNameFile(Path grmFile, String idColumn, String nameColumn, ExpFBKs fbks) throws Exception{
		try{
			Map<Object,String> cacheNames = new HashMap<>();
			StringBuffer output = new StringBuffer();
			
			//read in data
			List<String> grmData = Files.readAllLines(grmFile, Charset.defaultCharset());
			
			//read through every line and place genoId with name
			for(String line : grmData){
				//should be "genoIdIndex genoIdIndex relationship" 
				String[] lineValues = line.split("\\s+");
				
				//last value not an index
				for(int i = 0; i < lineValues.length -1 ; i++){
					String currentIndex = lineValues[i];
					String genoName = null;
					if((genoName = cacheNames.get(currentIndex)) == null){
					
						//lookup genoName for index if not already cached
						Object actualValue = fbks.getColMap().get(idColumn).get(Integer.parseInt(currentIndex));
						String where = "where "+ Funcs.quoteString(idColumn) + " = " + actualValue;
						String select = "select "+Funcs.quoteString(nameColumn); 
						List<Map<String, Object>> result = fbks.get(select, where, null);
						if(result != null && result.size() > 0){
							genoName = "";
							Object temp = null;
							if((temp=result.get(0).get(nameColumn)) != null ){
								genoName = temp.toString();	
							}
							cacheNames.put(lineValues[i], genoName);
						}
						else{
							log.error("No genoName for - " + line);
							break;
						}
					}
					output.append(genoName);
					output.append(" ");
				}
				output.append(lineValues[lineValues.length-1]);
				output.append(System.lineSeparator());
			}
			
			//write output file with added _Name.grm
			Path outputFile = grmFile.getParent().resolve(grmFile.getFileName().toString().replace(".", "_Name."));
			Funcs.createWithPermissions(outputFile,false);
			Files.write(outputFile, output.toString().getBytes());
		}
		catch(Exception e){
			log.error("Creating GRM geno name file - " + grmFile);
			throw e;
		}
	}
}