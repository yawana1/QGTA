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

public class LocSummaryReader {

	static Logger objLogger = Logger.getLogger(LocSummaryReader.class.getName());
	public static final String TAB = "\t";
	private int intTraitsCapacity;
	private Map<String, Map<String, Double>> mapEstimates;
	private Map<String, Map<String, Double>> mapCvs;
	private Map<String, Map<String, Double>> mapCheckCvs;
	private Map<String, Map<String, Double>> mapRawCoreCheckMeans;	
	//private List<Map<String, String>> lstMapLocSummaryData;

	public LocSummaryReader(int intInTraitsCapacity, 
			                Path objInLocSummaryFilePath) throws NoSuchFieldException {
		BufferedReader objBufferedReader;
		int intLinesNumber;
		int intLineNumber;
		String strLine;
		String[] strArrayHeaders;
		ArrayList<String> lstHeaders;
		int intTraitNameColumnIndex;
		int intLocIdColumnIndex;
		int intEstimateColumnIndex;
		int intCvColumnIndex;
		int intCheckCvColumnIndex;
		int intRawCoreCheckMeanColumnIndex;
		String[] strArrayLineParts;
		String strTraitName;
		Map<String, Double> mapEstimates;
		Double douEstimate;
		Map<String, Double> mapCvs;
		Double douCv;
		Map<String, Double> mapCheckCvs;
		Double douCheckCv;		
		Map<String, Double> mapRawCoreCheckMeans;
		Double douRawCoreCheckMean;				
		//Map<String, String> mapDataLine;
		Map<String, Double> objTreeMapDoubles;
		
		this.intTraitsCapacity = intInTraitsCapacity;
		
		objBufferedReader = null;
		intLinesNumber = 0;
		try {
			objBufferedReader = new BufferedReader(new FileReader(objInLocSummaryFilePath.toFile()));
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
		
		this.mapEstimates = new HashMap<String, Map<String, Double>>(this.intTraitsCapacity);
		this.mapCvs = new HashMap<String, Map<String, Double>>(this.intTraitsCapacity);
		this.mapCheckCvs = new HashMap<String, Map<String, Double>>(this.intTraitsCapacity);
		this.mapRawCoreCheckMeans = new HashMap<String, Map<String, Double>>(this.intTraitsCapacity);	
		//this.lstMapLocSummaryData = new ArrayList<Map<String, String>>(this.intTraitsCapacity);
		
		objBufferedReader = null;
		intLineNumber = 0;
		strArrayHeaders = null;
		intTraitNameColumnIndex = -1;
		intLocIdColumnIndex = -1;
		intEstimateColumnIndex = -1;
		intCvColumnIndex = -1;
		intCheckCvColumnIndex = -1;
		intRawCoreCheckMeanColumnIndex = -1;
		try {
			objBufferedReader = new BufferedReader(new FileReader(objInLocSummaryFilePath.toFile()));
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
                                                           objInLocSummaryFilePath.toString());
						}
						else {
							intTraitNameColumnIndex = lstHeaders.indexOf("traitVatString");
						}
						
						//////////
						// locId  
						//////////
						if (!(lstHeaders.contains("locId"))) {
							throw new NoSuchFieldException("No locId column in " + 
						                                   objInLocSummaryFilePath.toString());
						}
						else {
							intLocIdColumnIndex = lstHeaders.indexOf("locId");
						}

						/////////////
						// estimate    
						/////////////
						if (!(lstHeaders.contains("estimate"))) {
							throw new NoSuchFieldException("No estimate column in " + 
                                                           objInLocSummaryFilePath.toString());
						}
						else {
							intEstimateColumnIndex = lstHeaders.indexOf("estimate");
						}

						///////
						// cv  
						///////
						if (!(lstHeaders.contains("cv"))) {
							throw new NoSuchFieldException("No cv column in " + 
                                                           objInLocSummaryFilePath.toString());
						}
						else {
							intCvColumnIndex = lstHeaders.indexOf("cv");
						}

						////////////
						// checkCV  
						////////////
						if (!(lstHeaders.contains("checkCV"))) {
							throw new NoSuchFieldException("No checkCV column in " + 
                                                           objInLocSummaryFilePath.toString());
						}
						else {
							intCheckCvColumnIndex = lstHeaders.indexOf("checkCV");
						}
						
						/////////////////////
						// rawCoreCheckMean  
						/////////////////////
						if (!(lstHeaders.contains("rawCoreCheckMean"))) {
							throw new NoSuchFieldException("No rawCoreCheckMean column in " + 
                                                           objInLocSummaryFilePath.toString());
						}
						else {
							intRawCoreCheckMeanColumnIndex = lstHeaders.indexOf("rawCoreCheckMean");
						}
					}
					catch (NoSuchFieldException e) {
						e.printStackTrace();
						objLogger.error("LocSummaryReader.LocSummaryReader", 
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
							strTraitName = strArrayLineParts[intTraitNameColumnIndex].trim();

							try {
								/////////////
								// estimate  
								/////////////
								if (strArrayLineParts[intEstimateColumnIndex].trim().equals("")) {
									douEstimate = 0.0;
									objLogger.warn("No data in the estimate column in " + objInLocSummaryFilePath.toString() + 
									               " in the row #" + intLineNumber);
								}
								else {
									douEstimate = Double.parseDouble(strArrayLineParts[intEstimateColumnIndex].trim());
								}
								if (this.mapEstimates.containsKey(strTraitName)) {
									mapEstimates = this.mapEstimates.get(strTraitName);
								}
								else {
									mapEstimates = new HashMap<String, Double>((intLinesNumber / this.intTraitsCapacity) + 10);
								}
								Integer.parseInt(strArrayLineParts[intLocIdColumnIndex].trim());
								mapEstimates.put(strArrayLineParts[intLocIdColumnIndex].trim(),
										         douEstimate);
								this.mapEstimates.put(strTraitName, 
								                      mapEstimates);

								///////
								// cv      
								///////
								if (strArrayLineParts[intCvColumnIndex].trim().equals("")) {
									douCv = 0.0;
									objLogger.warn("No data in the cv column in " + objInLocSummaryFilePath.toString() + 
									               " in the row #" + intLineNumber);
								}
								else {
									douCv = Double.parseDouble(strArrayLineParts[intCvColumnIndex].trim());
								}
								if (this.mapCvs.containsKey(strTraitName)) {
									mapCvs = this.mapCvs.get(strTraitName);
								}
								else {
									mapCvs = new HashMap<String, Double>((intLinesNumber / this.intTraitsCapacity) + 10);
								}
								Integer.parseInt(strArrayLineParts[intLocIdColumnIndex].trim());
								mapCvs.put(strArrayLineParts[intLocIdColumnIndex].trim(),
										   douCv);
								this.mapCvs.put(strTraitName, 
								                mapCvs);
							
								////////////
								// checkCV  
								////////////
								if (strArrayLineParts[intCheckCvColumnIndex].trim().equals("")) {
									douCheckCv = 0.0;
									objLogger.warn("No data in the checkCV column in " + objInLocSummaryFilePath.toString() + 
									               " in the row #" + intLineNumber);
								}
								else {
									douCheckCv = Double.parseDouble(strArrayLineParts[intCheckCvColumnIndex].trim());
								}
								if (this.mapCheckCvs.containsKey(strTraitName)) {
									mapCheckCvs = this.mapCheckCvs.get(strTraitName);
								}
								else {
									mapCheckCvs = new HashMap<String, Double>((intLinesNumber / this.intTraitsCapacity) + 10);
								}
								Integer.parseInt(strArrayLineParts[intLocIdColumnIndex].trim());
								mapCheckCvs.put(strArrayLineParts[intLocIdColumnIndex].trim(),
										        douCheckCv);
								this.mapCheckCvs.put(strTraitName, 
								                     mapCheckCvs);

								/////////////////////
								// rawCoreCheckMean  
								/////////////////////
								if (strArrayLineParts[intRawCoreCheckMeanColumnIndex].trim().equals("")) {
									douRawCoreCheckMean = 0.0;
									objLogger.warn("No data in the rawCoreCheckMean column in " + objInLocSummaryFilePath.toString() + 
									               " in the row #" + intLineNumber);
								}
								else {
									douRawCoreCheckMean = Double.parseDouble(strArrayLineParts[intRawCoreCheckMeanColumnIndex].trim());
								}
								if (this.mapRawCoreCheckMeans.containsKey(strTraitName)) {
									mapRawCoreCheckMeans = this.mapRawCoreCheckMeans.get(strTraitName);
								}
								else {
									mapRawCoreCheckMeans = new HashMap<String, Double>((intLinesNumber / this.intTraitsCapacity) + 10);
								}
								Integer.parseInt(strArrayLineParts[intLocIdColumnIndex].trim());
								mapRawCoreCheckMeans.put(strArrayLineParts[intLocIdColumnIndex].trim(),
										                 douRawCoreCheckMean);
								this.mapRawCoreCheckMeans.put(strTraitName, 
								                              mapRawCoreCheckMeans);
							}
							catch (NumberFormatException e) {
								   e.printStackTrace();
								   objLogger.error("LocSummaryReader.LocSummaryReader", 
				                                   e);
								   throw new RuntimeException();
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
							//		            strArrayLineParts[intArrayIndex]);
							//}
							//this.lstMapLocSummaryData.add(mapDataLine);
						}
						else {
							throw new NoSuchFieldException("Inconsistency in the headers and fields numbers in " + 
                                                           objInLocSummaryFilePath.toString());
						}
					}
					catch (NoSuchFieldException e) {
						e.printStackTrace();
						objLogger.error("LocSummaryReader.LocSummaryReader", 
				                        e);
						throw new RuntimeException();
					}
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			objLogger.error("LocSummaryReader.LocSummaryReader",
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
				objLogger.error("LocSummaryReader.LocSummaryReader", 
        		                e);	
				throw new RuntimeException();
			}
		}
		
		////////////////////////////////////////////////
		// Sorts each estimate map basing on the locId  
		////////////////////////////////////////////////
		for (String strKey : this.mapEstimates.keySet()) {
			mapEstimates = this.mapEstimates.get(strKey);
			objTreeMapDoubles = new TreeMap<String, Double>(mapEstimates);
			this.mapEstimates.put(strKey,
					              objTreeMapDoubles);
		}		

		//////////////////////////////////////////
		// Sorts each cv map basing on the locId    
		//////////////////////////////////////////
		for (String strKey : this.mapCvs.keySet()) {
			mapCvs = this.mapCvs.get(strKey);
			objTreeMapDoubles = new TreeMap<String, Double>(mapCvs);
			this.mapCvs.put(strKey,
					        objTreeMapDoubles);
		}
		
		///////////////////////////////////////////////
		// Sorts each checkCV map basing on the locId    
		///////////////////////////////////////////////
		for (String strKey : this.mapCheckCvs.keySet()) {
			mapCheckCvs = this.mapCheckCvs.get(strKey);
			objTreeMapDoubles = new TreeMap<String, Double>(mapCheckCvs);
			this.mapCheckCvs.put(strKey,
					             objTreeMapDoubles);
		}
		
		////////////////////////////////////////////////////////
		// Sorts each rawCoreCheckMean map basing on the locId    
		////////////////////////////////////////////////////////
		for (String strKey : this.mapRawCoreCheckMeans.keySet()) {
			mapRawCoreCheckMeans = this.mapRawCoreCheckMeans.get(strKey);
			objTreeMapDoubles = new TreeMap<String, Double>(mapRawCoreCheckMeans);
			this.mapRawCoreCheckMeans.put(strKey,
					                      objTreeMapDoubles);
		}
	}
	
	public Map<String, Map<String, Double>> getEstimates() {
		return this.mapEstimates;
	}

	public Map<String, Map<String, Double>> getCvs() {
		return this.mapCvs;
	}

	public Map<String, Map<String, Double>> getCheckCvs() {
		return this.mapCheckCvs;
	}

	public Map<String, Map<String, Double>> getRawCoreCheckMeans() {
		return this.mapRawCoreCheckMeans;
	}
	
	//public List<Map<String, String>> getData() {
	//	return this.lstMapLocSummaryData;
	//}	
}
