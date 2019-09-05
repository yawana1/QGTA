package error;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Loads errror messages from properties file.  Used for logging error messages.
 * 
 * @author Scott Smith
 *
 */
public class ErrorMessage {

	public static final ErrorMessage INSTANCE = new ErrorMessage();
	private static Logger log = Logger.getLogger(ErrorMessage.class.getName());
	private static final String errorFile = "error.properties";
	private static Properties config;
	private static Boolean initFlag = false;
	
	private ErrorMessage() {}
	
	private void initErrorMessage(){
		config = new Properties();
		try(InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(errorFile);){
			if(inputStream == null){
				String error = "Cannot load file " + errorFile;
				log.error(error);
				throw new Exception(error);
			}
			else{
				config.load(inputStream);
			}
		}
		catch (Exception e) {
			log.error("Cannot load error string file", e);
		}
	}
	
	public String getMessage(String name){
		synchronized (initFlag) {
			if (! initFlag) {
				initFlag = true;
				initErrorMessage();
			}
		}
		
		String message = "";
		
		if(config == null){
			message = "Error Properties file not loaded";
		}
		else{
			if( (message = config.getProperty(name)) == null){
				message = "No error messsage for " + name;
			}
		}
		
		return message;
	}
}
