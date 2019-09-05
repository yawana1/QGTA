package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import asreml.AsremlGlobals.GrmType;
import cov.RME;
import data.XML;

/**
 * Class to setup the command and run the covariance engine.  Currently done by calling an exteranl unix script file.
 * <p>The Covariance engine takes a file with a list of genoId's and creates
 * a Grm from the geno's pedigree information and snip data.</p>
 * 
 * @author Scott Smith
 *
 */
public class Grm{

	private static Logger log = Logger.getLogger(Grm.class.getName());

	private String idColumn; //Asreml column that the grm values are mapped to
	private List<String> customIds;
	private String name;
	private String rename;
	private String fileNameHybridMap;
	private Map<String, List<Object>> data;
	private String workDirectory;
	private String workDirectoryPerColumn;
	private Properties config;

	/**
	 * Load asreml.config file.
	 * 
	 * @param data - List of id's
	 * @param idColumn - column name for id's
	 * @param rename - //optional name if the file returned by the covariance engine should be renamed.
	 * @param name - name of the file returned by the covariance engine
	 * @param workDirectory - directory to write all Covariance engine files to
	 * @param fileNameGenoType - File name of the file to write the id's found in the idColumn to.
	 * @param fileNameHybridMap - File name of the mapping file returned by the Covariance engine
	 */
	public Grm(Map<String, List<Object>> data, List<String> customIds, String idColumn, String rename, String workDirectory, String fileNameHybridMap){
		this.data = data; //Get list of id's from map
		this.customIds = customIds;
		this.idColumn = idColumn; //column name for id's
		this.rename = rename; //optional name if the file returned by the covariance engine should be renamed.
		this.workDirectory = workDirectory; //directory to write all Covariance engine files to
		this.workDirectoryPerColumn = this.workDirectory + "/"+idColumn; //directory to copy each grm before combining them in the work directory
		this.fileNameHybridMap = fileNameHybridMap; //File name of the mapping file returned by the Covariance engine
		
		//load config file of asreml constants
		config = new Properties();
		try{
			config.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("asreml.config"));
		}
		catch (Exception e) {
			log.error("Cannot load asreml.config file", e);
		}
	}
	
	/**
	 * Files created by the covariance engine use the form
	 * {H for genomic and A for nongenomic} _ {inbred or hybrid} {.grm for standard and .giv for the inverse of .grm} 
	 * 
	 * Ex. H_inbred.giv
	 * 
	 * @param grmType - {@link GrmType}
	 * @param useGenomic - flag to use similarity data or not
	 * @return
	 */
	private String createCovarianceReturnedFileName(RME rme){
		String name = null;  //name of the file returned by the covariance engine
		GrmType grmType = rme.getMatrixOptions().getAnalysisMode();
		
		String extension = null;
		if(GrmType.hybrid.equals(grmType) || GrmType.inbred.equals(grmType)){
			extension = config.getProperty("standard_file_extension");
		}
		else{
			extension = config.getProperty("inverse_file_extension");
		}
		if(extension == null){
			log.error("grm_file_extension not in asreml.config ");
		}
		
		//genomic or not
		String prepend = null;
		String dataType = rme.getMatrixOptions().getDataType();
		if(dataType.equals("pedigree")){
			prepend = config.getProperty("prepend_nongenomic");			
		}
		else if(dataType.equals("similarity")){
			prepend = config.getProperty("prepend_genomics_only");			
		}
		else if(dataType.equals("realized")){
			prepend = config.getProperty("prepend_realized");
		}
		if(prepend == null){
			log.error("prepend_nongenomic or prepend_genomic not in asreml.config ");
		}
		
		String delimiter = config.getProperty("delimiter_covariance_file_name");
		if(delimiter == null){
			log.error("delimiter_covariance_file_name");
		}
		
		//add dominance letter flag
		String dominanceFlag = "";
		if(GrmType.hybrid.equals(rme.getMatrixOptions().getAnalysisMode())){
			dominanceFlag = config.getProperty("prepend_dominance");
		}
		
		name = dominanceFlag + prepend + delimiter + grmType + extension;
		//name = prepend + delimiter + grmType + hybridExt + extension;

		return name;
	}
	
	/***
	 * 
	 * 
	 * @param scriptFile - Location of script file to run the pedigree and covariance engine
	 * @param useTraited - use the Traited version of the pedigree engine.  Currently only used for multiyear
	 * @param grmType - {@link GrmType}
	 * @param useGenomic - Set the covariance engine to use simialiarity data in the and return the H matrix
	 * @param bend - Force the returned matrix to be positive definite. 
	 * @return Number of levels for the collapsed hybrid map
	 */
	public Integer getGrmData(String scriptFile, RME rme) {
		Integer level = null;
		try {
			if(config == null){
				throw new Exception("asreml.config not set");
			}

			name = createCovarianceReturnedFileName(rme);
			
			//write out id file
			String indexColumn = idColumn;
			Path idFile = Paths.get(workDirectoryPerColumn, rme.getGenoIdFile());
			
			//test if any new lines have been added since last run.  If no then skip.
			boolean skipRunningRME = false;
			String baseIdFileString = null;
			if(Files.isReadable(idFile)){
				baseIdFileString = new String(Files.readAllBytes(idFile));
			}
			
			try{
				StringBuffer buffer = new StringBuffer();
				for(Object id : data.get(indexColumn)){
					buffer.append(id);
					buffer.append(System.lineSeparator());
				}
				
				if(customIds != null){
					for(Object id : customIds){
						buffer.append(id);
						buffer.append(System.lineSeparator());
					}
				}
				
				if(!Files.exists(idFile)){
					Funcs.createWithPermissions(idFile.getParent(), idFile.getParent().getParent(),true);
				}
				if(buffer.toString().equals(baseIdFileString)){
					//check if output file exists for previous run
					String resultFile = rename+config.getProperty("standard_file_extension");
					Path destPath = Paths.get(workDirectory, resultFile);
					
					//check map file exists
					Path mapPath = Paths.get(workDirectoryPerColumn,rme.getMatrixOptions().getAnalysisMode().toString()+fileNameHybridMap);
					
					//check if rme config xml has changed
					String rmeConfigFileName = "rme.xml";
					Path rmeConfigFile = Paths.get(workDirectoryPerColumn, rmeConfigFileName);
					
					if(Files.exists(destPath) && Files.exists(mapPath) && XML.INSTANCE.serialize(rme).equals(new String(Files.readAllBytes(rmeConfigFile)))){
						skipRunningRME = true;
					}
				}
				else{					
					Files.write(Funcs.createWithPermissions(idFile, false), buffer.toString().getBytes(), StandardOpenOption.CREATE); //write id's to file, example geno.txt file
				}
			}
			catch(Exception e){
				log.error("",e);
				throw e;
			}

			Integer result = null; //result code from RME script
			if(!skipRunningRME){
				//write out xml config file for RME.
				String rmeConfigFileName = "rme.xml";
				Path rmeConfigFile = Paths.get(workDirectoryPerColumn, rmeConfigFileName);
				XML.INSTANCE.serialize(rme, rmeConfigFile.toString());
				
				//create sh command
				String cmd = scriptFile + " ";
				cmd += workDirectoryPerColumn;
				cmd += ";" ;

				try {
					result = SystemEx.run(cmd, System.out, System.err);
				} catch (Exception e) {
					log.error("gblup runscript error", e);
				}
		
				renameFile(result, rme.getMatrixOptions().getAnalysisMode());
			}
			
			//Reprocess pre existing files or if RME result had no errors
			if (skipRunningRME || (result != null && result != -1)) {
				// Add column base_id_index
				level = postProcessFBKs(indexColumn, rme.getMatrixOptions().getAnalysisMode());
			}
		} catch (Exception e) {
			log.error("",e);
		} 
		return level;
	}
	
	/**
	 * Rename the result files created by the Covariance engine to the rename specified.
	 * 
	 * @param result - Unix command finished result
	 * @param grmType 
	 * @throws Exception 
	 */
	private void renameFile(Integer result, GrmType grmType) throws Exception{
		if (result != null && result != -1) {							
			if (rename != null){
				String fileExtensionInverse = config.getProperty("inverse_file_extension");
				String fileExtensionStandard = config.getProperty("standard_file_extension");
				//rename grm file since parent grm files are created with the same name
				//do both grm and giv if exists
								
				//move grm
				Path file = Paths.get(workDirectoryPerColumn, name);
				renameFile(file, rename+fileExtensionStandard);
				
				//move giv
				file = Paths.get(workDirectoryPerColumn, name.replace(fileExtensionStandard, fileExtensionInverse));
				renameFile(file, rename + fileExtensionInverse);
			}
		}
	}
	
	/**
	 * Rename file.
	 * @param file - Original file
	 * @param rename - New name
	 * @throws IOException
	 */
	private void renameFile(Path file, String rename) throws IOException{
		if(Files.exists(file)){
			//rename covariance output file name
			Path renamedFile = file.resolveSibling(rename);
			Files.move(file, renamedFile, StandardCopyOption.REPLACE_EXISTING);
			
			//copy to parent directory
			Path destPath = Paths.get(workDirectory, rename);
			Files.copy(renamedFile, destPath, StandardCopyOption.REPLACE_EXISTING);
		}else{
			log.error("file does NOT exist - " + file);
		}
	}
	
	/**
	 * Read result file from the covariance engine.  Currently written in the hybridMap.txt file where
	 * each line is "id grmIndex".  Grm file is collapsed meaning more then one "id" can map to the same grmIndex.
	 * Reindexed grm values are stored in the colMap as "columnName"_index.
	 * 
	 * @param indexColumn - Name of the Asreml Column
	 * @param grmType - {@link GrmType}
	 * @return - Total number of uncolapsed id's sent back from the covariance engine
	 */
	public int postProcessFBKs(String indexColumn, GrmType grmType) {
		//File hybrid_map = new File(workDirectory + "/" + grmType + fileNameHybridMap);
		File hybrid_map = new File(workDirectoryPerColumn + "/" + grmType + fileNameHybridMap);
		Map<Integer, List<Integer>> hybridMap = new HashMap<Integer, List<Integer>>();
		List<Object> ls = new ArrayList<Object>();
		if (hybrid_map.exists()) {
			try {
				FileInputStream fstream = new FileInputStream(hybrid_map);
				BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
				String strLine;
				//parse file into index and list of ids for that index
				while ((strLine = br.readLine()) != null) {
					String[] str = strLine.split(" ");
					int index = (Integer.parseInt(str[1]));
					if (hybridMap.get(index) == null) {
						hybridMap.put(index, new ArrayList<Integer>());
					}
					hybridMap.get(index).add(Integer.parseInt(str[0]));
				}
				fstream.close();

				List<Object> baseIdList = data.get(indexColumn);

				for (int i = 0; i < baseIdList.size(); i++) {
					Object baseIdKey = baseIdList.get(i);
					// ls.add(hybridMap.get(Long.parseLong(baseId.toString())));
					Integer baseIdIndex = getBaseIdIndex(hybridMap,
							Integer.parseInt(baseIdKey.toString()));
					if (baseIdIndex != null) {
						ls.add(baseIdIndex);
					}
				}

				data.put(indexColumn+"_index", ls);

			} catch (Exception e) {
				log.error("", e);
			}
		}
		return hybridMap.size();
	}
	
	/***
	 * Find the values index from covariance engine since the covariance collapses for base genetics.
	 * @param hybridMap
	 * @param baseIdKey
	 * @return
	 */
	public Integer getBaseIdIndex(Map<Integer, List<Integer>> hybridMap, Integer baseIdKey) {
		for (Integer key :hybridMap.keySet()) {
			List<Integer> list = hybridMap.get(key);
			int index = list.indexOf(baseIdKey);
			
			if(index != -1){
				return key;
			}
		}
		return 0;
	}
}