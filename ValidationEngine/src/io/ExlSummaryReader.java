/*
 * 
 */
package io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
//import java.util.List;

public class ExlSummaryReader {

	static Logger objLogger = Logger.getLogger(ExlSummaryReader.class.getName());
	public static final String TAB = "\t";
	private int intTraitsCapacity;
	private Map<String, Map<String, Double>> mapRawMeans;
	private Map<String, Map<String, Double>> mapCoreChecks;
	private Map<String, Map<String, Double>> mapCavs;	
	//private List<Map<String, String>> lstMapExlSummaryData;

	public ExlSummaryReader(int intInTraitsCapacity,
                            Path objInExlSummaryFilePath) throws NoSuchFieldException {
		BufferedReader objBufferedReader;
		int intLinesNumber;
		int intLineNumber;
		String strLine;
		String[] strArrayHeaders;
		ArrayList<String> lstHeaders;
		int intTraitNameColumnIndex;
		int intGenoIdColumnIndex;
		int intLocIdColumnIndex;
		int intRawMeanColumnIndex;
		int intCoreCheckColumnIndex;
		int intCavColumnIndex;	
		String[] strArrayLineParts;
		String strTraitName;
		String strComposedKey;
		Map<String, Double> mapRawMeans;
		Double douRawMean;
		Map<String, Double> mapCoreChecks;
		Double douCoreCheck;		
		Map<String, Double> mapCavs;
		Double douCav;			
		//Map<String, String> mapDataLine;
		Map<String, Double> objTreeMapDoubles;
		
		this.intTraitsCapacity = intInTraitsCapacity;
		
		objBufferedReader = null;
		intLinesNumber = 0;
		try {
			objBufferedReader = new BufferedReader(new FileReader(objInExlSummaryFilePath.toFile()));
			while ((strLine = objBufferedReader.readLine()) != null) {
				intLinesNumber++;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			objLogger.error("EntrySummaryReader.EntrySummaryReader", 
					        e);
			throw new RuntimeException();
		}
		
		this.mapRawMeans = new HashMap<String, Map<String, Double>>(this.intTraitsCapacity);
		this.mapCoreChecks = new HashMap<String, Map<String, Double>>(this.intTraitsCapacity);
		this.mapCavs = new HashMap<String, Map<String, Double>>(this.intTraitsCapacity);	
		//this.lstMapExlSummaryData = new ArrayList<Map<String, String>>(this.intTraitsCapacity);
		
		objBufferedReader = null;
		intLineNumber = 0;
		strArrayHeaders = null;
		intTraitNameColumnIndex = -1;
		intGenoIdColumnIndex = -1;
		intLocIdColumnIndex = -1;
		intRawMeanColumnIndex = -1;
		intCoreCheckColumnIndex = -1;
		intCavColumnIndex = -1;	
		try {
			objBufferedReader = new BufferedReader(new FileReader(objInExlSummaryFilePath.toFile()));
			while ((strLine = objBufferedReader.readLine()) != null) {
				intLineNumber++;
				////////////////
				// The headers  
				////////////////
				if (intLineNumber == 1) {
					strArrayHeaders = strLine.trim().split(TAB);
					lstHeaders = new ArrayList<String>(Arrays.asList(strArrayHeaders));
					try {
						///////////////////
						// traitVatString 
						///////////////////
						if (!(lstHeaders.contains("traitVatString"))) {
							throw new NoSuchFieldException("No traitVatString column in " + 
	                                                       objInExlSummaryFilePath.toString());
						}
						else {
							intTraitNameColumnIndex = lstHeaders.indexOf("traitVatString");
						}
						
						/////////////////////////
						// genoId (or entryNum)  
						/////////////////////////
						if (!(lstHeaders.contains("genoId"))) {
							if (!(lstHeaders.contains("entryNum"))) {
								throw new NoSuchFieldException("No genoId or entryNum column in " + 
						                                   	   objInExlSummaryFilePath.toString());
							}
							else {
								intGenoIdColumnIndex = lstHeaders.indexOf("entryNum");
							}
						}
						else {
							intGenoIdColumnIndex = lstHeaders.indexOf("genoId");
						}
						
						//////////
						// locId  
						//////////
						if (!(lstHeaders.contains("locId"))) {
							throw new NoSuchFieldException("No locId column in " + 
						                                   objInExlSummaryFilePath.toString());
						}
						else {
							intLocIdColumnIndex = lstHeaders.indexOf("locId");
						}
						
						////////////
						// rawMean    
						////////////
						if (!(lstHeaders.contains("rawMean"))) {
							throw new NoSuchFieldException("No rawMean column in " + 
                                                           objInExlSummaryFilePath.toString());
						}
						else {
							intRawMeanColumnIndex = lstHeaders.indexOf("rawMean");
						}

						//////////////
						// coreCheck    
						//////////////
						if (!(lstHeaders.contains("coreCheck"))) {
							throw new NoSuchFieldException("No coreCheck column in " + 
                                                           objInExlSummaryFilePath.toString());
						}
						else {
							intCoreCheckColumnIndex = lstHeaders.indexOf("coreCheck");
						}
					
						////////
						// CAV      
						////////
						if (!(lstHeaders.contains("CAV"))) {
							throw new NoSuchFieldException("No CAV column in " + 
                                                           objInExlSummaryFilePath.toString());
						}
						else {
							intCavColumnIndex = lstHeaders.indexOf("CAV");
						}
					}
					catch (NoSuchFieldException e) {
						e.printStackTrace();
						objLogger.error("ExlSummaryReader.ExlSummaryReader", 
						                e);
						throw new RuntimeException();
					}
				}
				///////////////////
				// The data lines  
				///////////////////
				else {
					strArrayLineParts = strLine.split(TAB,
	                                                  strArrayHeaders.length);
					try {
						if (strArrayHeaders.length == strArrayLineParts.length) {
							strTraitName = strArrayLineParts[intTraitNameColumnIndex];

							if(!strArrayLineParts[intGenoIdColumnIndex].equals("")){
							try {
								/////////////////////////////////////////////////////
								// Checking if the genoId/entryNum value is integer  
								/////////////////////////////////////////////////////
								Integer.parseInt(strArrayLineParts[intGenoIdColumnIndex].trim());
							
								///////////////////////////////////////////
								// Checking if the locId value is integer  
								///////////////////////////////////////////
								Integer.parseInt(strArrayLineParts[intLocIdColumnIndex].trim());
							
								//////////////////////////////////////////////////////////////////////
								// A two-value key from a concatenation of locId and genoId/entryNum   
								//////////////////////////////////////////////////////////////////////
								strComposedKey = strArrayLineParts[intLocIdColumnIndex].trim() + "_" +  
								                 strArrayLineParts[intGenoIdColumnIndex].trim();
							
								////////////
								// rawMean    
								////////////
								if (strArrayLineParts[intRawMeanColumnIndex].trim().equals("")) {
									douRawMean = 0.0;
									objLogger.warn("No data in the rawMean column in " + objInExlSummaryFilePath.toString() + 
												   " in the row #" + intLineNumber);
								}
								else {
									douRawMean = Double.parseDouble(strArrayLineParts[intRawMeanColumnIndex].trim());
								}
								if (this.mapRawMeans.containsKey(strTraitName)) {
									mapRawMeans = this.mapRawMeans.get(strTraitName);
								}
								else {
									mapRawMeans = new HashMap<String, Double>((intLinesNumber / this.intTraitsCapacity) + 10);
								}
								mapRawMeans.put(strComposedKey, 
												douRawMean);
								this.mapRawMeans.put(strTraitName, 
										         	 mapRawMeans);
								
								//////////////
								// coreCheck    
								//////////////
								if (strArrayLineParts[intCoreCheckColumnIndex].trim().equals("")) {
									douCoreCheck = 0.0;
									objLogger.warn("No data in the coreCheck column in " + objInExlSummaryFilePath.toString() + 
												   " in the row #" + intLineNumber);
								}
								else {
									douCoreCheck = Double.parseDouble(strArrayLineParts[intCoreCheckColumnIndex].trim());
								}
								if (this.mapCoreChecks.containsKey(strTraitName)) {
									mapCoreChecks = this.mapCoreChecks.get(strTraitName);
								}
								else {
									mapCoreChecks = new HashMap<String, Double>((intLinesNumber / this.intTraitsCapacity) + 10);
								}
								mapCoreChecks.put(strComposedKey, 
												  douCoreCheck);
								this.mapCoreChecks.put(strTraitName, 
										         	   mapCoreChecks);

								////////
								// CAV      
								////////
								if (strArrayLineParts[intCavColumnIndex].trim().equals("")) {
									douCav = 0.0;
									objLogger.warn("No data in the CAV column in " + objInExlSummaryFilePath.toString() + 
												   " in the row #" + intLineNumber);
								}
								else {
									douCav = Double.parseDouble(strArrayLineParts[intCavColumnIndex].trim());
								}
								if (this.mapCavs.containsKey(strTraitName)) {
									mapCavs = this.mapCavs.get(strTraitName);
								}
								else {
									mapCavs = new HashMap<String, Double>((intLinesNumber / this.intTraitsCapacity) + 10);
								}
								mapCavs.put(strComposedKey, 
											douCav);
								this.mapCavs.put(strTraitName, 
										         mapCavs);
							}
							catch (NumberFormatException e) {
								e.printStackTrace();
								objLogger.error("ExlSummaryReader.ExlSummaryReader", 
				                                e);
								throw new RuntimeException();
							}
							}
							//////////////
							// Data Line 
							////////////////////////////////////////
							// Commented to Shorten Execution Time  
                            ////////////////////////////////////////
							//mapDataLine = new HashMap<String, String>(strArrayLineParts.length);
							//for (intArrayIndex = 0;
							//     intArrayIndex < strArrayLineParts.length;	
							//	   intArrayIndex++) {
							//	mapDataLine.put(strArrayHeaders[intArrayIndex], 
							//			        strArrayLineParts[intArrayIndex]);
							//}
							//this.lstMapExlSummaryData.add(mapDataLine);
						}
						else {
							throw new NoSuchFieldException("Inconsistency in the headers and fields numbers in " + 
                                                           objInExlSummaryFilePath.toString());
						}
					}
					catch (NoSuchFieldException e) {
						e.printStackTrace();
						objLogger.error("ExlSummaryReader.ExlSummaryReader", 
				                        e);
						throw new RuntimeException();
					}
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			objLogger.error("ExlSummaryReader.EntrySummaryReader", 
			                e);
			throw new RuntimeException();
		}
		finally {
			try {
				if (objBufferedReader != null) {
					objBufferedReader.close();
				}
			} 
			catch (IOException e) {
				e.printStackTrace();
				objLogger.error("ExlSummaryReader.EntrySummaryReader", 
		                        e);	
				throw new RuntimeException();
			}
		}
		
		///////////////////////////////////////////////////////////////////////////////////
		// Sorts each rawMean map basing on the composed key of LocId and genoId/entryNum   
		///////////////////////////////////////////////////////////////////////////////////
		for (String strKey : this.mapRawMeans.keySet()) {
			mapRawMeans = this.mapRawMeans.get(strKey);
			objTreeMapDoubles = new TreeMap<String, Double>(mapRawMeans);
			this.mapRawMeans.put(strKey,
					             objTreeMapDoubles);
		}		

		/////////////////////////////////////////////////////////////////////////////////////
		// Sorts each coreCheck map basing on the composed key of LocId and genoId/entryNum   
		/////////////////////////////////////////////////////////////////////////////////////
		for (String strKey : this.mapCoreChecks.keySet()) {
			mapCoreChecks = this.mapCoreChecks.get(strKey);
			objTreeMapDoubles = new TreeMap<String, Double>(mapCoreChecks);
			this.mapCoreChecks.put(strKey,
					               objTreeMapDoubles);
		}		

		///////////////////////////////////////////////////////////////////////////////
		// Sorts each CAV map basing on the composed key of LocId and genoId/entryNum     
		///////////////////////////////////////////////////////////////////////////////
		for (String strKey : this.mapCavs.keySet()) {
			mapCavs = this.mapCavs.get(strKey);
			objTreeMapDoubles = new TreeMap<String, Double>(mapCavs);
			this.mapCavs.put(strKey,
					         objTreeMapDoubles);
		}		
	}	
	
	public Map<String, Map<String, Double>> getRawMeans() {
		return this.mapRawMeans;
	}
	
	public Map<String, Map<String, Double>> getCoreChecks() {
		return this.mapCoreChecks;
	}

	public Map<String, Map<String, Double>> getCavs() {
		return this.mapCavs;
	}

	//public List<Map<String, String>> getData() {
	//	return this.lstMapExlSummaryData;
	//}		
}
