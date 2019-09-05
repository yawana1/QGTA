/*
 * 
 * @package 	utils
 * @class 		ArgumentParser.java (ArgumentParser)
 * @author 		u409397
 * @modified 	Jul 8, 2010
 * 
 */
package utils;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class ArgumentParser.
 * 
 * @author u409397
 */
public class ArgumentParser{

	private Map<String,String> options = new HashMap<String, String>();
	private int paramIndex = 0;

	/***
	 * 
	 * @param args
	 * @throws Exception
	 */
	public ArgumentParser(String[] args) throws Exception {
		if(args != null){
			for(String arg : args){
				if (arg.startsWith("-") || arg.startsWith("/")) {
					int loc = arg.indexOf("=");
					String key = null;
					if(loc > 0){
						key = arg.substring(1, loc);
					}
					else{
						key = arg.substring(1);
					}
					
					String value = null;
					if(loc > 0){
						value = arg.substring(loc+1);
					}
					else{
						value = "";
					}
					options.put(key.toLowerCase(), value);
				}
			}
		}
	}

	/**
	 * Checks for option.
	 * 
	 * @param opt
	 *            the option
	 * 
	 * @return true, if checks for option
	 */
	public boolean hasOption(String opt) {
		return options.containsKey(opt.toLowerCase());
	}

	/**
	 * Next option.
	 * 
	 * @return the string
	 */
	public String nextOption(){
		String str = null;
		if(hasNext()){
			str = (String) options.keySet().toArray()[paramIndex++];
		}
		return str;
	}

	/**
	 * Gets the option.
	 * 
	 * @param opt
	 *            the opt
	 * 
	 * @return the option
	 */
	public String getOption(String opt) {
		return options.get(opt.toLowerCase());
	}

	/**
	 * Checks for next.
	 * 
	 * @return true, if successful
	 */
	public boolean hasNext(){
		return paramIndex < options.size();
	}
}