/*
 * 
 */
package io;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import data.Model;
import data.Trait;
import data.Trial;

public class WorkDirectorySimpleFileVisitor extends SimpleFileVisitor<Path> {
	
	static Logger objLogger = Logger.getLogger(WorkDirectorySimpleFileVisitor.class.getName());
	private List<Trial> lstTrials;
	private String strSeasonName;
	private String strTrialName;

	
	// when a specific work path is defined, that file can be in archive. So we need to know it here for the method preVisitDirectory
	public WorkDirectorySimpleFileVisitor(int intInTrialsCapacity,
			                              String strInSeasonName, String strInTrialName) throws Exception {
		this.lstTrials = new ArrayList<Trial>(intInTrialsCapacity);
		this.strSeasonName = strInSeasonName;
		this.strTrialName = strInTrialName;
	}
	
	@Override public FileVisitResult preVisitDirectory(Path objInDirectoryPath,
            										   BasicFileAttributes objInBasicFileAttributes)  throws IOException {
		
		//if are going through many trials, it will never look into the archive folders
		if (!(objInDirectoryPath.getFileName().toString().startsWith("ARCHIVE_"))) {
			System.out.println(objInDirectoryPath.getFileName().toString());
			return FileVisitResult.CONTINUE;
		}
		else {
			return FileVisitResult.SKIP_SUBTREE;
		}
	}

	@Override public FileVisitResult visitFile(Path objInFilePath,
		                                       BasicFileAttributes objInBasicFileAttributes) throws IOException {
		String strFilePath;
		
		strFilePath = objInFilePath.toString();
		
		
		// it will enter here only if the user is looking for many trials at the time
		// in this case there is no trial name but season name is mandatory
		if((this.strTrialName.equals("")&&(!(this.strSeasonName.equals(""))))){
			//if (!(strFilePath.contains("/" + this.strSeasonName + "/"))){
			//	System.out.println("skip");
			//	return FileVisitResult.SKIP_SUBTREE;
			//}else{
			//	System.out.print("\n#");
			
			if ((strFilePath.contains("/" + this.strSeasonName + "/")) &&
					(strFilePath.endsWith("asreml.asr"))) {
				System.out.print("tem ");
				this.getDirectoryEnvironment(objInFilePath);
			}
			//}
		}else{
			System.out.println(strFilePath);
			// if we get here we are already in the right folder
			// so we only need to check for the asr extension
			//if ((strFilePath.contains("/" + this.strSeasonName + "/")) &&
			//		(strFilePath.contains("/"+ this.strTrialName + "/")) &&
			//		(strFilePath.endsWith("asreml.asr"))) {
			if (strFilePath.endsWith("asreml.asr")) {
				System.out.print("tem ");
				
				this.getDirectoryEnvironment(objInFilePath);
			}
		}
		return FileVisitResult.CONTINUE;
	}
	
	private void getDirectoryEnvironment(Path objInFilePath) {
		String strFilePath; 
		String[] strArrayLineParts;
		Path objTraitPath;
		String strTrialName;
		Path objModelPath;
		String strModelName;
		Path objTrialPath;
		String strTraitName;
		Trial objTrial;
		int intTrialIndex;
		Model objModel;
		int intModelIndex;
		Trait objTrait;
		
		strFilePath = objInFilePath.toString();
		if (3 <= (strFilePath.toString().split("\\/").length)) {
			////////////////////////////////////////
			// Trial/Model/Trait                    
			// Example: XA3752WW/prep_2yd/moisture  
			////////////////////////////////////////
			// Trait 
			//////////
			objTraitPath = objInFilePath.getParent();
			strArrayLineParts = objTraitPath.toString().split("\\/");
			strTraitName = strArrayLineParts[strArrayLineParts.length - 1];

			//////////
			// Model  
			//////////
			objModelPath = objTraitPath.getParent();
			strArrayLineParts = objModelPath.toString().split("\\/");
			strModelName = strArrayLineParts[strArrayLineParts.length - 1];

			//////////
			// Trial  
			//////////
			// Changed by U755482 because of the new structure of the folders
			if(objModelPath.toString().contains("ARCHIVE_")){
				/// back to work // back to ARCHIVE /// back to archive // back to trial
				objTrialPath = objModelPath.getParent().getParent().getParent().getParent();
			}else{
				// back to work // back to trial
				objTrialPath = objModelPath.getParent().getParent();
			}
			//objTrialPath = objModelPath.getParent();
			strArrayLineParts = objTrialPath.toString().split("\\/");
			strTrialName = strArrayLineParts[strArrayLineParts.length - 1];

			/////////////////
			// Trial Object    
			/////////////////
			//objTrial = new Trial(strTrialName,
			//		             objTrialPath); Modified by U755482
			
			objTrial = new Trial(strTrialName,
		             objModelPath.getParent());
			if (!(this.lstTrials.contains(objTrial))) {
				this.lstTrials.add(objTrial);
				objLogger.info("Processing a new trial: " + objTrial.getTrialName() + 
						       "...");
			}	 
			intTrialIndex = this.lstTrials.indexOf(objTrial);
			objTrial = this.lstTrials.get(intTrialIndex);

			/////////////////
			// Model Object    
			/////////////////
			objModel = new Model(strModelName,
			                     objModelPath);
			if (!(objTrial.getModels().contains(objModel))) {
				this.lstTrials.get(intTrialIndex).addModel(objModel);
				objLogger.info("Processing a new model: " + objModel.getName() + 
						       " in the trial: " + objTrial.getTrialName() +
						       "...");
			}	 
			intModelIndex = this.lstTrials.get(intTrialIndex).getModels().indexOf(objModel);
			objModel = this.lstTrials.get(intTrialIndex).getModels().get(intModelIndex);
				
			/////////////////
			// Trait Object    
			/////////////////
			objTrait = new Trait(strTraitName,
                                 objTraitPath);				
			if (!(objModel.getTraits().contains(objTrait))) {
				this.lstTrials.get(intTrialIndex).getModels().get(intModelIndex).addTrait(objTrait);
				objLogger.info("Processing a new trait: " + objTrait.getName() + 
						       " in the model: " + objModel.getName() +
						       " in the trial: " + objTrial.getTrialName() +
						       "...");
			}
			else {
				objLogger.error("The trait, " + strTraitName + 
						        ", is repeated in " + objInFilePath);
				throw new RuntimeException("The trait, " + strTraitName + 
				                           ", is repeated in " + objInFilePath);
			}
		}
		else {
			objLogger.error(objInFilePath.toString() + 
					        " seems not having the three concepts: trial, model, and trait");
			throw new RuntimeException(objInFilePath.toString() + 
					                   " seems not having the three concepts: trial, model, and trait");
		}		
	} 

	public List<Trial> getTrials() {
		return this.lstTrials;
	}
}