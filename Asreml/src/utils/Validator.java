package utils;

import java.text.DecimalFormat;
import java.util.List;

import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.commons.validator.routines.IntegerValidator;
import org.apache.log4j.Logger;

public class Validator {

	private static final Logger logger = Logger.getLogger(Validator.class.getName());
	private static final IntegerValidator intValidator = new IntegerValidator();
	private static final DoubleValidator dblvalidator = new DoubleValidator();
	
	public static Integer getInt(String str){
		if(str==null) return null;
		else return intValidator.validate(str);
	}
	
	public static Double getDbl(String str){
		if(str==null){
			return null;
		}
		else if(str.equals("NaN")){
			return Double.NaN;
		}
		else if(str.equals("Infinity")){
			return Double.POSITIVE_INFINITY;
		}
		else{
			return dblvalidator.validate(str);
		}
	}
	
	public static boolean getBool(String str){
		if(str==null) return false;
		else return Boolean.parseBoolean(str);
	}
	
	public static boolean isParsableToInt(String i){
		return intValidator.validate(i)==null ? false : true;
	}
		
	public static Double formatDec(Double val){
		if(val == null) return val;
		DecimalFormat df = new DecimalFormat("#0.00"); 
		try{
			return Double.parseDouble(df.format(val.doubleValue()));
		}catch(Exception e){
			logger.info(e);
			return val;
		}
	}	

	public static Double checkDbl(Object obj){
		DoubleValidator dv = new DoubleValidator();
		if(obj==null || !dv.isValid(obj.toString())) return null;
		else{
			Double dbl = Double.parseDouble(obj.toString());
			if(dbl.isInfinite()) return null;
			if(dbl.isNaN()) return null;
			return Validator.formatDec(dbl);
		}
	}
	

	
	public static boolean equals(List<String> l1, List<String> l2){
		if((l1==null || l1.isEmpty()) && (l2==null || l2.isEmpty())) return true;
		else if(l1==null || l1.isEmpty()) return false;
		else if(l2==null || l2.isEmpty()) return false;
		else if(l1.size() != l2.size()) return false;
		else{
			for(String s : l1){
				if(!l2.contains(s)) return false;
			}
		}
		return true;
	}
}