/*
 * 
 */
package io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
//import java.util.List;

public class EntrySummaryReader {

	static Logger objLogger = Logger.getLogger(EntrySummaryReader.class.getName());
	public static final String TAB = "\t";
	private int intTraitsCapacity;
	private String strCreationTime;
	private Map<String, Integer> mapGenotypesNumbers;
	private Map<String, Map<String, Double>> mapEstimates;
	private Map<String, Map<String, Double>> mapCoreChecks;	
	private Map<String, Map<String, Double>> mapRanks;
	private Map<String, Integer> mapRawCounts;
	//private List<Map<String, String>> lstMapEntrySummaryData;
	
	public EntrySummaryReader(int intInTraitsCapacity,
			                  Path objInEntrySummaryFilePath) throws NoSuchFieldException {
		BasicFileAttributes objBasicFileAttributes;
		BufferedReader objBufferedReader;
		int intLinesNumber;
		int intLineNumber;
		String strLine;
		String[] strArrayHeaders;
		ArrayList<String> lstHeaders;
		int intTraitNameColumnIndex;
		int intGenoIdColumnIndex;
		int intEstimateColumnIndex;
		int intCoreCheckColumnIndex;
		int intRankColumnIndex;
		int intRawCountColumnIndex;
		String[] strArrayLineParts;
		String strTraitName;
		Map<String, Double> mapEstimates;
		Double douEstimate;
		Double douCoreCheck;
		Map<String, Double> mapCoreChecks;		
		Double douRank;
		Map<String, Double> mapRanks;				
		int intRawCount;
		//Map<String, String> mapDataLine;
		//int intArrayIndex;
		Map<String, Double> objTreeMapDoubles;

		this.intTraitsCapacity = intInTraitsCapacity;

		try {
			objBasicFileAttributes = Files.readAttributes(objInEntrySummaryFilePath, 
				                                      	  BasicFileAttributes.class);
			this.strCreationTime = objBasicFileAttributes.creationTime().toString();
		}
		catch (IOException e) {
			objLogger.error("EntrySummaryReader.EntrySummaryReader", 
					        e);
			throw new RuntimeException();
		}
		
		objBufferedReader = null;
		intLinesNumber = 0;
		try {
			objBufferedReader = new BufferedReader(new FileReader(objInEntrySummaryFilePath.toFile()));
			while ((strLine = objBufferedReader.readLine()) != null) {
				intLinesNumber++;
			}
		}
		catch (IOException e) {
			objLogger.error("EntrySummaryReader.EntrySummaryReader", 
					        e);
			throw new RuntimeException();
		}
		
		this.mapGenotypesNumbers =  new HashMap<String, Integer>(this.intTraitsCapacity);
		this.mapEstimates = new HashMap<String, Map<String, Double>>(this.intTraitsCapacity);
		this.mapCoreChecks = new HashMap<String, Map<String, Double>>(this.intTraitsCapacity);
		this.mapRanks = new HashMap<String, Map<String, Double>>(this.intTraitsCapacity);
		this.mapRawCounts = new HashMap<String, Integer>(this.intTraitsCapacity);
		//this.lstMapEntrySummaryData = new ArrayList<Map<String, String>>(this.intTraitsCapacity);
		
		objBufferedReader = null;
		intLineNumber = 0;
		strArrayHeaders = null;
		intTraitNameColumnIndex = -1;
		intGenoIdColumnIndex = -1;
		intEstimateColumnIndex = -1;
		intCoreCheckColumnIndex = -1;
		intRankColumnIndex = -1;
		intRawCountColumnIndex = -1;
		try {
			objBufferedReader = new BufferedReader(new FileReader(objInEntrySummaryFilePath.toFile()));
			
			// we will read line by line of the the current file
			while ((strLine = objBufferedReader.readLine()) != null) {
				intLineNumber++;
				
				
				// the first part of the code deals with the header - first line
				// the remaining deals with the actual data
				
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
						
						// traitVatString appears as the name of a column
						// I need to check what it means
						
						if (!(lstHeaders.contains("traitVatString"))) {
							throw new NoSuchFieldException("No traitVatString column in " + 
						                                   objInEntrySummaryFilePath.toString());
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
						                                   	   objInEntrySummaryFilePath.toString());
							}
							else {
								intGenoIdColumnIndex = lstHeaders.indexOf("entryNum");
							}
						}
						else {
							intGenoIdColumnIndex = lstHeaders.indexOf("genoId");
						}
						
						/////////////
						// estimate    
						/////////////
						if (!(lstHeaders.contains("estimate"))) {
							throw new NoSuchFieldException("No estimate column in " + 
	                                                       objInEntrySummaryFilePath.toString());
						}
						else {
							intEstimateColumnIndex = lstHeaders.indexOf("estimate");
						}

						//////////////
						// coreCheck    
						//////////////
						if (!(lstHeaders.contains("coreCheck"))) {
							throw new NoSuchFieldException("No coreCheck column in " + 
	                                                       objInEntrySummaryFilePath.toString());
						}
						else {
							intCoreCheckColumnIndex = lstHeaders.indexOf("coreCheck");
						}

						/////////
						// rank    
						/////////
						if (!(lstHeaders.contains("rank"))) {
							throw new NoSuchFieldException("No rank column in " + 
	                                                       objInEntrySummaryFilePath.toString());
						}
						else {
							intRankColumnIndex = lstHeaders.indexOf("rank");
						}
						
						/////////////
						// rawCount  
						/////////////
						if (!(lstHeaders.contains("rawCount"))) {
							throw new NoSuchFieldException("No rawCount column in " + 
	                                                       objInEntrySummaryFilePath.toString());
						}
						else {
							intRawCountColumnIndex = lstHeaders.indexOf("rawCount");
						}
					}
					catch (NoSuchFieldException e) {
						e.printStackTrace();
						objLogger.error("EntrySummaryReader.EntrySummaryReader", 
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
							
							/////////////////////
							// Genotypes Number  
							/////////////////////
							if (this.mapGenotypesNumbers.containsKey(strTraitName)) {
								this.mapGenotypesNumbers.put(strTraitName, 
										                     (this.mapGenotypesNumbers.get(strTraitName) + 1));
							}
							else {
								this.mapGenotypesNumbers.put(strTraitName, 
						                                     1);
							}

							
							if(!strArrayLineParts[intGenoIdColumnIndex].equals("")){
							
							try {
								/////////////
								// estimate   
								/////////////
								if (strArrayLineParts[intEstimateColumnIndex].trim().equals("")) {
									douEstimate = 0.0;
									objLogger.warn("No data in the estimate column in " + objInEntrySummaryFilePath.toString() + 
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
								//System.out.println(intGenoIdColumnIndex);
								//System.out.println(strArrayLineParts[0]);
								//System.out.println(strArrayLineParts[1]);
								//System.out.println(strArrayLineParts[2]);
								// 
								//System.out.println(strArrayLineParts[3]);
								//System.out.println(strArrayLineParts[4]);
								//System.out.println(strArrayLineParts[5]);
								//System.out.println(strArrayLineParts[6]);
								Integer.parseInt(strArrayLineParts[intGenoIdColumnIndex].trim());
								mapEstimates.put(strArrayLineParts[intGenoIdColumnIndex].trim(), 
											     douEstimate); 
								this.mapEstimates.put(strTraitName, 
								                      mapEstimates);

								//////////////
								// coreCheck  
								//////////////
								if (strArrayLineParts[intCoreCheckColumnIndex].trim().equals("")) {
									douCoreCheck = 0.0;
									objLogger.warn("No data in the coreCheck column in " + objInEntrySummaryFilePath.toString() + 
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
								Integer.parseInt(strArrayLineParts[intGenoIdColumnIndex].trim());
								mapCoreChecks.put(strArrayLineParts[intGenoIdColumnIndex].trim(), 
											      douCoreCheck); 
								this.mapCoreChecks.put(strTraitName, 
								                       mapCoreChecks);

								/////////
								// rank    
								/////////
								if (strArrayLineParts[intRankColumnIndex].trim().equals("")) {
									douRank = 0.0;
									objLogger.warn("No data in the rank column in " + objInEntrySummaryFilePath.toString() + 
									               " in the row #" + intLineNumber);
								}
								else {
									douRank = Double.parseDouble(strArrayLineParts[intRankColumnIndex].trim());
								}
								if (this.mapRanks.containsKey(strTraitName)) {
									mapRanks = this.mapRanks.get(strTraitName);
								}
								else {
									mapRanks = new HashMap<String, Double>((intLinesNumber / this.intTraitsCapacity) + 10);
								}
								Integer.parseInt(strArrayLineParts[intGenoIdColumnIndex].trim());
								mapRanks.put(strArrayLineParts[intGenoIdColumnIndex].trim(), 
											 douRank); 
								this.mapRanks.put(strTraitName, 
								                  mapRanks);
							
								/////////////
								// rawCount  
								/////////////
								if (strArrayLineParts[intRawCountColumnIndex].trim().equals("")) {
									intRawCount = 0;
									objLogger.warn("No data in the rawCount column in " + objInEntrySummaryFilePath.toString() + 
									               " in the row #" + intLineNumber);
								}
								else {
									intRawCount = Integer.parseInt(strArrayLineParts[intRawCountColumnIndex].trim());
								}
								if (this.mapRawCounts.containsKey(strTraitName)) {
									this.mapRawCounts.put(strTraitName, 
												          this.mapRawCounts.get(strTraitName) + intRawCount);
								}
								else {
									this.mapRawCounts.put(strTraitName, 
											              intRawCount);
								}
							}
							catch (NumberFormatException e) {
								e.printStackTrace();
								objLogger.error("EntrySummaryReader.EntrySummaryReader", 
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
							//	   intArrayIndex < strArrayLineParts.length;	
							//     intArrayIndex++) {
							//	mapDataLine.put(strArrayHeaders[intArrayIndex], 
							//		            strArrayLineParts[intArrayIndex]);
							//}
							//this.lstMapEntrySummaryData.add(mapDataLine);
						}
						else {
							throw new NoSuchFieldException("Inconsistency in the headers and fields numbers in " + 
                                                           objInEntrySummaryFilePath.toString());
						}
							
							
					}
					catch (NoSuchFieldException e) {
						e.printStackTrace();
						objLogger.error("EntrySummaryReader.EntrySummaryReader", 
				                        e);
						throw new RuntimeException();
					}
					
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			objLogger.error("EntrySummaryReader.EntrySummaryReader", 
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
				objLogger.error("EntrySummaryReader.EntrySummaryReader", 
				                e);
				throw new RuntimeException();
			}
		}
		
		///////////////////////////////////////////////////////////
		// Sorts each estimates map basing on the genoId/entryNum  
		///////////////////////////////////////////////////////////
		for (String strKey : this.mapEstimates.keySet()) {
			mapEstimates = this.mapEstimates.get(strKey);
			objTreeMapDoubles = new TreeMap<String, Double>(mapEstimates);
			this.mapEstimates.put(strKey,
					              objTreeMapDoubles);
		}		

		/////////////////////////////////////////////////////////////
		// Sorts each corechecks map basing on the genoId/entryNum  
		/////////////////////////////////////////////////////////////
		for (String strKey : this.mapCoreChecks.keySet()) {
			mapCoreChecks = this.mapCoreChecks.get(strKey);
			objTreeMapDoubles = new TreeMap<String, Double>(mapCoreChecks);
			this.mapCoreChecks.put(strKey,
					               objTreeMapDoubles);
		}		
		
		/////////////////////////////////////////////////////////////
		// Sorts each ranks map basing on the genoId/entryNum  
		/////////////////////////////////////////////////////////////
		for (String strKey : this.mapRanks.keySet()) {
			mapRanks = this.mapRanks.get(strKey);
			objTreeMapDoubles = new TreeMap<String, Double>(mapRanks);
			this.mapRanks.put(strKey,
					          objTreeMapDoubles);
		}	
	}
	
	public String getCreationTime() {
		return this.strCreationTime;
	}
	
	public Map<String, Integer> getGenotypesNumbers() {
		return this.mapGenotypesNumbers;
	}

	public Map<String, Map<String, Double>> getEstimates() {
		return this.mapEstimates;
	}

	public Map<String, Map<String, Double>> getCoreChecks() {
		return this.mapCoreChecks;
	}
	
	public Map<String, Map<String, Double>> getRanks() {
		return this.mapRanks;
	}
	
	public Map<String, Integer> getRawCounts() {
		return this.mapRawCounts;
	}
	
	//public List<Map<String, String>> getData() {
	//	return this.lstMapEntrySummaryData;
	//}	
}
