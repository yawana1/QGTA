package asreml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import asreml.AsremlGlobals.AsConst;
import asreml.input.AsremlModel;
import asreml.input.AsremlShellScript;
import asreml.output.AsremlOutput;

/***
 * Class to write, run, and process Asreml files.
 * <p>Asreml is third party executable that uses a .as file to define a liner mixed model
 * and uses a .asd flat file as the data to run the model on.</p>
 * 
 * @author Scott Smith
 *
 */
public class Asreml {

	public static final String ASREML_LICENSE_SERVER_DOWN = "The license server manager (lmgrd) has not been started yet,";
	public static final String ASREML_LICENSE_LIMIT = "Licensing error:-4,132";
	static Logger log = Logger.getLogger(Asreml.class.getName());
	private boolean test = false;
	private List<AsremlModel> models = new ArrayList<>();
    private Collection<AsremlShellScript> scriptFiles; //.sh unix script file to run asreml command.  One per trait.
    private String asCmd; //unix asreml command
    private int threadPoolSize = 8; //default  VM size / 2 x cluster hiQ size
    
	public boolean isTest() {
		return test;
	}
	public void setTest(boolean test) {
		this.test = test;
	}
	public List<AsremlModel> getModels() {
		return models;
	}
	public void setModels(List<AsremlModel> models) {
		this.models = models;
	}
	public Collection<AsremlShellScript> getScriptFiles() {
		return scriptFiles;
	}
	public void setScriptFiles(Collection<AsremlShellScript> scriptFiles) {
		this.scriptFiles = scriptFiles;
	}
	
	public Asreml(boolean test, String asCmd){
		this.test = test;
		this.asCmd = asCmd;
	}
	
	public Asreml(boolean test, String asCmd, int threadPoolSize){
		this.test = test;
		this.asCmd = asCmd;
		this.threadPoolSize = threadPoolSize;
	}
	
	public void addModel(AsremlModel model){
    	models.add(model);
    }
	
	/***
	 * Create a directory and write an .as file and a .sh file to run Asreml
	 * for each model and every trait in each model.
	 * 
	 */
	public void writeScript(){
		scriptFiles = new ArrayList<>();
		try{
			for(AsremlModel model : models){
				Map<AsremlTrait,Path> modelFilenames = model.writeScriptFile();
				
				//add in every traits .as file for this model
				for(Entry<AsremlTrait, Path> entry: modelFilenames.entrySet()){
					try{
						AsremlTrait trait = entry.getKey();
						Path modelFile = entry.getValue();	
							
						//file to redirect asreml output into
						Path output = createAsremlOutputPath(modelFile.getParent());
						
						//create unix command to pushd the .as file directory and the run asreml
						//then popd the directory
						String cmd = " pushd " + modelFile.getParent() + " ; "; 
						cmd += asCmd+" "+modelFile+" &> " + output;
						cmd += " ; chmod --reference=asreml.as *.*  ; popd ;";
	
						scriptFiles.add(new AsremlShellScript(model, trait, cmd));
					}
					catch (Exception e){
						log.error(""+entry, e);
						throw e;
					}
				}
			}
		}
		catch (Exception e) {
			log.error("No asreml script written", e);
		}
    }
	
	/***
	 * Create file name to redirect Asreml unix stdin, stdout to.
	 * @param dir
	 * @return
	 */
	private Path createAsremlOutputPath(Path dir){
		return dir.resolve(AsConst.prefix.value() + AsConst.outputSuffix.value());
	}
	
	public void runScript() throws RunAsremlException{
		runScript(true, false);
	}
	
	public void runScriptLoadAsremlOutputs() throws RunAsremlException{
		runScript(true, true);
	}
	
	public void loadAsremlOutupts() throws RunAsremlException{
		runScript(false, true);
	}
	
	/***
	 * Create thread pool to run the Asreml jobs
	 * Run the asreml jobs as a unix cmd
	 * Block until all jobs are done.
	 * @throws RunAsremlException - Halts entire program
	 */
	public void runScript(boolean executeScript, boolean readOutput) throws RunAsremlException{
		ExecutorService pool = null;
		try{
			int poolSize = threadPoolSize;
			int totalRunCount = scriptFiles.size();
			pool = Executors.newFixedThreadPool(poolSize);
			CompletionService<Integer> results = new ExecutorCompletionService<>(pool);
			
			//run each Asreml sh script in a separate thread and block till all are done
			for(final AsremlShellScript scriptFile : scriptFiles){
				try{
					Callable<Integer> thread = new RunAsreml(scriptFile, executeScript, readOutput);
	    			
	    			//start the thread
	    			results.submit(thread);
	    		}
	    		catch(Exception e){
	    			log.error(scriptFile, e);
	    		}
	    	}
			//wait for all threads to finish
			int[] commandResults = new int[totalRunCount];
			for(int i = 0; i < totalRunCount; i++){
				Future<Integer> result = results.take();
				int commandResult = result.get();
				commandResults[i] = commandResult;
			}
			
			for(int commandResult : commandResults){
				//unix system error when running Asreml then stop
				if(commandResult == -1){
					throw new RunAsremlException();
				}
			}
    	}
		catch(RunAsremlException e){
			throw e;
		}
    	catch (Exception e) {
			log.error("Thread error", e);
		}
    	finally{
    		if(pool != null){
        		pool.shutdown();	
    		}
    	}
		
    }
	
	/**
	 * Callable class to handle a call to Asreml in it's own Thread.
	 * 
	 * @author Scott Smith
	 *
	 */
	public class RunAsreml implements Callable<Integer>{

		private AsremlShellScript scriptFile;
		private boolean executeScript;
		private boolean readOutput;
		
		public RunAsreml(AsremlShellScript scriptFile, boolean executeScript, boolean readOutput) {
			super();
			this.scriptFile = scriptFile;
			this.executeScript = executeScript;
			this.readOutput = readOutput;
		}

		@Override
		public Integer call() throws Exception {
			int commandReturn = -1;
			if(executeScript){
				String cmd = "" + scriptFile;
				try {
					ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", cmd).inheritIO();
					Process process = processBuilder.start();
					commandReturn = process.waitFor();
					//commandReturn = SystemEx.run(cmd, System.out, System.err);
				} catch (Exception e) {
					log.error(cmd, e);
				}
				
				//if unix script failed stop
				if(commandReturn == -1){
					log.fatal("The Asreml program did not complete" + cmd);
				}
			}
			if(readOutput){
				commandReturn = 0;
			
				AsremlModel asremlModel = scriptFile.getModel();
				Path path = asremlModel.getDirectory().resolve(scriptFile.getTrait().getName());
				checkOutputFile(path);
				
		    	//read in Asreml output files
				try{
					AsremlModel model = scriptFile.getModel();
					AsremlTrait trait = scriptFile.getTrait();
					model.getOutputs().put(trait, new AsremlOutput(model, trait, model.getErrorVariance()));
				}
				catch(Exception e){
					log.warn("Creating AsremlOuput " + scriptFile, e);
				}
			}
			return commandReturn;
		}
	}
	
	public class RunAsremlException extends Exception{

		private static final long serialVersionUID = 1L;

		public RunAsremlException() {
			super();
		}
	}
	
	public static String getFileNameWithoutExtension(String fileName) {
		File tmpFile = new File(fileName);
		tmpFile.getName();
		int whereDot = tmpFile.getName().lastIndexOf('.');
		if (0 < whereDot && whereDot <= tmpFile.getName().length() - 2 ) {
			return tmpFile.getName().substring(0, whereDot);
		}
		return "";
	}
	
	/***
	 * Check Asreml command output for license server off.  If so shutdown all analysis.
	 * @param path
	 * @return
	 */
	public int checkOutputFile(Path path){
		int result = 0;
		
		String file;
		try {
			file = new String(Files.readAllBytes(createAsremlOutputPath(path)));
			
			if(file.contains(ASREML_LICENSE_SERVER_DOWN)){
				result = -1;
			}
			
		} catch (IOException e) {
			result = -1;
		}
		
		return result;
	}
}