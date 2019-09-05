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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.log4j.Logger;

/**
 * The Class Res.
 */
public class Res extends AsremlOutputFile {

	static Logger logger = Logger.getLogger(Res.class.getName());
	private final static String type = "";
	private final static String ext  = ".res";
	private final static String graphRegExp = "\\s*\\*+(?>\\*+\\s*)*"; //leading white space with a least one * and any number of * and spaces.
	private StringBuffer data;
	
	public void init(){
		data = new StringBuffer();
	}
	public String getType() {
		return type;
	}
	
	public Res(Path filename){
		super(filename, ext, "");
	}

	public StringBuffer getData() {
		return data;
	}
	public void setData(StringBuffer data) {
		this.data = data;
	}

	/**
	 * Read the Asreml output Yht file and create a map of {@link Yht} objects.
	 */
	protected void load(){
		BufferedReader br = null;
		try {
			File file = new File(getFullFileName());
			if(!file.exists()){
				logger.warn(file.getPath()+" does not exists");
				return;
			}
			br = new BufferedReader(new FileReader(file));
			String str;
			while ((str = br.readLine()) != null){
				if(str.matches(graphRegExp)){
					data.append(str);
					data.append(System.lineSeparator());
				}
			}
		}catch (FileNotFoundException e) {
			logger.warn("",e);
		} catch (IOException e) {
			logger.fatal("",e);
		}finally{
			try {
				if(br != null){
					br.close();
				}
			} catch (IOException e) {
				logger.fatal("",e);
			}
		}
	}
}