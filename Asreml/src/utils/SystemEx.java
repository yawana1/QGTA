/*
 * Using JRE 1.6.0_02
 * 
 * @package 	utils
 * @class 		SystemEx.java
 * @author 		u409397
 * @modified 	Jul 8, 2010
 * 
 */
package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * <p>
 * Title: Java classes made in GA-LAB
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2001
 * </p>
 * <p>
 * Company: GA-LAB
 * </p>
 * Originally from
 * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4
 * 
 * @author modified by ng36271
 * @version 1.0
 */

public class SystemEx {
    
	private static Logger log = Logger.getLogger(SystemEx.class);
	
    /**
	 * Instantiates a new system ex.
	 */
    public SystemEx() {
    }
    
    /**
	 * Run.
	 * 
	 * @param cmd
	 *            the command to run
	 * 
	 * @return the int
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
    public static int run(String cmd) throws IOException {
        return run(cmd, System.out, System.err);
    }
    
    /**
	 * Run runs a given command in terminal (linux) or command prompt (windows).
	 * 
	 * @param cmd
	 *            the command ot run
	 * @param out
	 *            the output stream
	 * @param err
	 *            the errorr
	 * 
	 * @return the int
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
    public static int run(String cmd, OutputStream out, OutputStream err) throws IOException {
    	BufferedReader br = null;
    	try {
            String osName = System.getProperty("os.name");
            String[] shell = new String[3];
            
            if (osName.startsWith("Windows 9")){
            	shell[0] = "command.exe";
            	shell[1] = "/C";	
            }
            else if (osName.startsWith("Windows")){
            	shell[0] = "cmd.exe";
            	shell[1] = "/C";
            }
            else{
            	shell[0] = "sh";
            	shell[1] = "-c";
            }
            
            shell[2] = cmd;
            
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(shell); 
            
            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), err);
            errorGobbler.start();
            
            br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
 //           PrintStream bw = new PrintStream(out);
            
            String line;
            while ((line = br.readLine()) != null) {
                if (out != null){
                	//bw.println(line);
                	log.info(line);
                }
            }
            
            return proc.waitFor();
        } catch (IOException ex) {
            throw ex;
        } catch (Throwable t) {
            return -1;
        }finally{
        	if(br != null){
        		br.close();
        	}
        }
    }
}
