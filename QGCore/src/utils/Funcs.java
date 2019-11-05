package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystemException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.commons.validator.routines.IntegerValidator;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.josql.Query;
import org.josql.QueryExecutionException;
import org.josql.QueryParseException;
import org.josql.QueryResults;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import utils.Globals.UnitTypes;
import error.ErrorMessage;

/**
 * Class for small static helper funcs.
 * 
 * @author Scott Smith
 *
 */

public class Funcs {

	private static final Logger log = Logger.getLogger(Funcs.class.getName());
	
	public static Integer getInt(String str){
		if(str==null) return null;
		else return IntegerValidator.getInstance().validate(str);
	}
	
	public static Double getDbl(String str){
		if(str==null) return null;
		else return DoubleValidator.getInstance().validate(str);
	}
	
	public static boolean getBool(String str){
		if(str==null) return false;
		else return Boolean.parseBoolean(str);
	}
	
	public static boolean getBool(Object obj){
		return Boolean.parseBoolean(""+obj);
	}
	
	public static List<String> getList(String str){
		if(str==null || str.isEmpty()) return null;
		String[] arr = str.split(",\\s*");
		return Arrays.asList(arr);
	}
	
	public static List<String> arrayToList(String[] arr){
		List<String> lst = new ArrayList<String>();
		for(String str : arr)
			lst.add(str);
		return lst;
	}
	
	public static String listToSql(Collection<?> myList, boolean forceStr) {
		if(myList.isEmpty()){
			return "";
		}
		boolean allInts = true;
		StringBuffer myStr = new StringBuffer("'");
		StringBuffer myIntStr = new StringBuffer("");
		for (Object o : myList){
			String myThing = o.toString();
			if (!isParsableToInt(myThing)){
				allInts = false;
			} else{}
			myStr.append(myThing);
			myStr.append("','");
			myIntStr.append(myThing);
			myIntStr.append(",");
		}
		if (allInts && !forceStr){
			myIntStr.delete(myIntStr.length() - 1, myIntStr.length());
			return myIntStr.toString();
		} else {
			myStr.delete(myStr.length() - 2, myStr.length());
			return myStr.toString();
		}
	}
	
	public static String listToQuotedStr(List<?> lst, String delimiter){
		StringBuffer str = new StringBuffer();
		for(int i=0; i<lst.size(); i++){
			str.append("" + quoteString(lst.get(i)));
			str.append(delimiter);
		}
		
		return str.substring(0, str.length()-1);
	}
	
	public static boolean isParsableToInt(String i){
		return IntegerValidator.getInstance().validate(i)==null ? false : true;
	}
	
	public static String fileToString(String fileName) {
		String result = null;
		try{
			Path path = Paths.get(fileName);
			result = new String(Files.readAllBytes(path));
		}
		catch (FileNotFoundException e) {
			log.warn(ErrorMessage.INSTANCE.getMessage("not_found_file") + fileName, e);
		}
		catch (Exception e) {
			log.warn(ErrorMessage.INSTANCE.getMessage("file_load") + fileName, e);
		}
		return result;
	}
	
	public static void deleteDir(File path){
		if(path.exists()){
			File[] files = path.listFiles();
			for(File file : files){
				try{
					if(file.isDirectory()) deleteDir(file);
					else file.delete();
				}catch(Exception e){
					log.warn(e.getMessage());
				}
			}
			path.delete();
		}
	}
	
	public static Double formatDec(Double val){
		if(val == null) return val;
		DecimalFormat df = new DecimalFormat("#0.0000"); 
		try{
			return Double.parseDouble(df.format(val.doubleValue()));
		}catch(Exception e){
			log.info(e);
			return val;
		}
	}
	
	public static String StringArrayListToSql(List<String> myList, boolean forceStr) {
		StringBuffer myStr = new StringBuffer("");
		if (forceStr){
			myStr.append("'");
		}else{}
		for (String myString : myList){
			if (forceStr){
				myStr.append(myString.toString());
				myStr.append("','");
			} else {
				myStr.append(myString.toString());
				myStr.append(",");
			}
		}
		if (forceStr){
			myStr.delete(myStr.length() - 2, myStr.length());
			return myStr.toString();
		} else {
			myStr.delete(myStr.length() - 1, myStr.length());
			return myStr.toString();
		}
	}
	
	public static Double sqr(double d) {		
		return d*d;
	}
	
	public static String setSql(String sql, Properties props){
		while(sql.matches(".+\\[.+\\].+")){
			String frame = sql.substring(sql.indexOf("["), sql.indexOf("]")+1);
			String key = sql.substring(sql.indexOf("[")+1, sql.indexOf("]"));
			String value = props.getProperty(key);
			String tmp = sql.substring(0, sql.indexOf(frame)) + value + sql.substring(sql.indexOf(frame)+frame.length());
			sql = tmp;
		}
		return sql;
	}
	
	public static Double calculateCV(Double mean, Double stdDev){
		return mean==null || mean==0 || stdDev==null || stdDev==0? null : Math.sqrt(stdDev)/Math.abs(mean);
	}
	
//	public static String delimit(Object obj){
//		return App.INSTANCE.get("output_delimiter") +(obj==null ? "" : obj);
//	}
	
	public static Double checkDbl(Object obj){
		DoubleValidator dv = new DoubleValidator();
		if(obj==null || !dv.isValid(obj.toString())) return null;
		else{
			Double dbl = Double.parseDouble(obj.toString());
			if(dbl.isInfinite()) return null;
			if(dbl.isNaN()) return null;
			return Funcs.formatDec(dbl);
		}
	}
	
	public static Object checkVal(Object obj){
		DoubleValidator dv = new DoubleValidator();
		if(obj == null) return null;
		else if(obj instanceof Double){
			Double dbl = (Double) obj;
			if(dbl.isInfinite()) return null;
			if(dbl.isInfinite()) return null;
			if(dbl.isNaN()) return null;
			return formatDec(dbl);
		}
		else if(dv.isValid(obj.toString())){
			Double dbl = Double.parseDouble(obj.toString());
			if(dbl.isInfinite()) return null;
			if(dbl.isNaN()) return null;
			return Funcs.formatDec(dbl);
		}else{
			return obj;
		}
	}
	
	/***
	 * Call the getXXX method on the object for a field.
	 * @param o - Object where the value to "get" is.
	 * @param varName - name of the field.
	 * @return - result of the getXXX method.
	 */
	public static Object getValue(Object o, String varName){
		Object result = null;
		try
		{
			String firstLetter = null;
			if(Character.isUpperCase(varName.charAt(1))){
				firstLetter = varName.substring(0, 1);
			}
			else{
				firstLetter = varName.substring(0, 1).toUpperCase();
			}
			Method method = o.getClass().getMethod("get"+ firstLetter+ varName.substring(1));
			result = method.invoke(o, new Object());
		}
		catch (Exception e)
		{
			log.warn(e);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	/***
	 * Use JoSql to query collection of T objects using the sql statement
	 */
	public static <T> List<T> queryData(String sql, List<SimpleEntry<String, Object>> params, Collection<?> data, List<String> alias) throws QueryParseException, QueryExecutionException{
		List<T> result = new ArrayList<T>();
		if(null != data){
			Query query = new Query();
			if(null != params){
				for(Entry<String, Object> entry:params){
					query.setVariable(entry.getKey(), entry.getValue());
				}
			}
			query.parse(sql);
			QueryResults qResult =  query.execute(data);
			result = qResult.getResults();
			
			if(alias != null){
				for(Object o : query.getAliases().entrySet()){
					@SuppressWarnings("rawtypes")
					Map.Entry entry = (Entry) o;
					if(!entry.getKey().equals(""+entry.getValue())){
						int index = Funcs.getInt(entry.getValue().toString()) -1;
						alias.set(index, entry.getKey().toString());
					}
				}
			}

		}
		return result;
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
	
	public static Double averageList(List<Double> lst){
		Double val = 0d;
		for(Iterator<Double> dit = lst.iterator(); dit.hasNext();){
			val += dit.next();
		}
		return (val/lst.size());
	}
	
	public static Integer round(Double dbl){
		return dbl==null ? null : Math.round(dbl.floatValue());
	}
	
	public static Double plotUnitConvertor(UnitTypes unit, Integer index){
		switch(unit){
		case INCHES :
			return plotUnitInches(index);
		case FEET :
			return plotUnitFeet(index);	
		default :
			return 0d;
		}
	}
	
	public static Double plotUnitInches(Integer index){
		switch(index){
		case 1 : return 400d;	// inches
		case 2 : return 3.33;	// feeet
		case 3 : return 10d;	// meters
		case 4 : return 1000d;	// centimeters
		default : return 10d;
		}
	}
	
	public static Double plotUnitFeet(Integer index){
		switch(index){
		case 1 : return 120d;	// inches
		case 2 : return 10d;	// feet
		case 3 : return 32.8d;	// meters
		case 4 : return 3280.8d;// centimeters
		default : return 10d;
		}
	}
	
	public static String[] toStringArray(List<Object> array){
		String[] result = new String[array.size()];
		for(int i=0; i<array.size(); i++){
			result[i] = ""+array.get(i);
		}
		return result;
	}
	
	public static String[] toStringArray(Object[] array){
		String[] result = new String[array.length];
		for(int i=0; i<array.length; i++){
			result[i] = ""+array[i];
		}
		return result;
	}
	
	public static <T> String quoteString(T string){
		return "\""+string+"\"";
	}
	
	public static <T> String quote(Collection<T> data, String delimiter){
		String result = new String();
		
		if(null != data && data.size() > 0 ){
			StringBuffer buffer = new StringBuffer();
			for(T item : data){
				buffer.append(quoteString(item));
				buffer.append(delimiter);
			}
			
			int index = buffer.lastIndexOf(delimiter);
			result = buffer.substring(0, index);
		}
		
		return result;
	}
	
	public static long getUsedMemory(){
		int size = 1024*1024;
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / size;
	}
	
	/**
	 * Gets the file name without extension.
	 * 
	 * @param fileName
	 *            the file name
	 * 
	 * @return the file name without extension
	 */
	public static String getFileNameWithoutExtension(String fileName) {
		File tmpFile = new File(fileName);
		tmpFile.getName();
		int whereDot = tmpFile.getName().lastIndexOf('.');
		if (0 < whereDot && whereDot <= tmpFile.getName().length() - 2 ) {
			return tmpFile.getName().substring(0, whereDot);
		}
		return "";
	}
	
	/**
	 * Gets the file name without extension.
	 * 
	 * @param fileName
	 *            the file name
	 * 
	 * @return the file name without extension
	 */
	public static String getFileExtension(String fileName) {
		File tmpFile = new File(fileName);
		tmpFile.getName();
		int whereDot = tmpFile.getName().lastIndexOf('.');
		if (0 < whereDot && whereDot <= tmpFile.getName().length() - 2 ) {
			return tmpFile.getName().substring(whereDot, tmpFile.getName().length());
		}
		return "";
	}
	
	public static List<Path> findFiles(String pattern, Path path) throws IOException{
		List<Path> result = new ArrayList<>();
		PathMatcher match = FileSystems.getDefault().getPathMatcher("glob:"+pattern);
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
			for(Path p : directoryStream){
				if(match.matches(p.getFileName())){
					result.add(p);
				}
			}
		}
		return result;
	}
	
	/**
	 * Recursively delete all files and subdirectories in this directory.
	 * @param path
	 * @throws IOException
	 */
	public static void deleteDir(Path path) throws IOException{
		Path directory = path;
		if(Files.exists(directory)){
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
			   @Override
			   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				   Files.delete(file);
				   return FileVisitResult.CONTINUE;
			   }

			   @Override
			   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				   Files.delete(dir);
				   return FileVisitResult.CONTINUE;
			   }

		   });
		}
	}
	
	/**
	 * Generate MD5 hash from file
	 * @param file
	 * @return
	 * @throws NoSuchAlgorithmException 
	 * @throws IOException 
	 */
	public static String checkSum(String file){
		String result = null;
		
		Path path = Paths.get(file);
		if(Files.isReadable(path)){
			try {
				byte[] b = Files.readAllBytes(path);
				byte[] hash = MessageDigest.getInstance("MD5").digest(b);
				result = new String(hash);
			} catch (IOException | NoSuchAlgorithmException e) {
				log.error("Error on cheksum compute " + path);
			}
		}
		else{
			log.warn("File not readable : " + path);
		}

		return result;
	}
	
	/**
	 * Compute hash of the 2 files and compare.
	 * @param file1
	 * @param file2
	 * @return
	 */
	public static boolean checkSumCompare(String file1, String file2){
		String hash1 = checkSum(file1);
		String hash2 = checkSum(file2);
		
		if(hash1 == null || hash2 == null){
			return true;
		}
		else{
			return hash1.equals(hash2);
		}
	}
	
	/**
	 * Calculate a flag that if the date passed in should be run or not based on the schedule
	 * 
	 * @param scheduleFormatString
	 * @param date - Date used as the basis to judge from Now()
	 * @return
	 */
	public static boolean shouldRun(String scheduleFormatString, DateTime date){
		//create CronDefintion object used to parse cron string and generate execution times.
		CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
		CronParser cronParser = new CronParser(cronDefinition);
		Cron cron = cronParser.parse(scheduleFormatString);
		ExecutionTime executionTime = ExecutionTime.forCron(cron);
		
		
		DateTime lastExecution = executionTime.lastExecution(DateTime.now());
		DateTime nextExecution = executionTime.nextExecution(lastExecution);
		
		long scheduledInterval = nextExecution.getMillis() - lastExecution.getMillis();  //time between last scheduled execution and scheduled next execution
		long timeFromLastExecution = DateTime.now().getMillis() - date.getMillis();  //time between now and the last run
		
		//if time from the last run is greater then the scheduled interval between runs.
		boolean m = (timeFromLastExecution >= scheduledInterval);
		
		return m;
	}
	
	/**
	 * 
	 * @param scheduleFormatString
	 * @return
	 */
	public static boolean shouldRun(String scheduleFormatString, long scheduledInterval){
		//create CronDefintion object used to parse cron string and generate execution times.
		CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
		CronParser cronParser = new CronParser(cronDefinition);
		Cron cron = cronParser.parse(scheduleFormatString);
		ExecutionTime executionTime = ExecutionTime.forCron(cron);
		
		
		DateTime lastExecution = executionTime.lastExecution(DateTime.now());
		long timeFromLastExecution = DateTime.now().getMillis() - lastExecution.getMillis();  //time between now and the last scheduled run
		
		//if time from the last run is greater then the scheduled interval between runs.
		boolean m = (timeFromLastExecution >= scheduledInterval);
		
		return m;
	}
	
	/*
	 * Create file or directory with permissions set to parent folder permissions
	 */
	public static Path createWithPermissions(Path path, Path pathPermissions, boolean dir) throws IOException{
		if(dir || Files.isDirectory(path)){
			Files.createDirectories(path);
		}
		else if(!Files.exists(path)){
			Files.createFile(path);
		}
		try{
			Files.setPosixFilePermissions(path, Files.getPosixFilePermissions(pathPermissions));
		}
		catch(AccessDeniedException e){
			//eat permission errors
		}
		catch(FileSystemException f){
			
		}
		catch(UnsupportedOperationException e) {
			//windows machines
		}
		
		return path;
	}
	
	public static Path createWithPermissions(Path path, boolean dir) throws IOException{
		return createWithPermissions(path, path.getParent(), dir);
	}
}