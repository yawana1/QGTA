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

import org.apache.log4j.Logger;
//import java.util.List;

public class ExperimentSummaryReader {
	
	static Logger objLogger = Logger.getLogger(ExperimentSummaryReader.class.getName());
	public static final String TAB = "\t";
	private int intTraitsCapacity;
	private Map<String, Integer> mapLocationsNumbers;
	//private List<Map<String, String>> lstMapExperimentSummaryData;
	
	public ExperimentSummaryReader(int intInTraitsCapacity,
                                   Path objInExperimentSummaryFilePath) throws NoSuchFieldException {
		BufferedReader objBufferedReader;
		int intLineNumber;
		String strLine;
		String[] strArrayHeaders;
		ArrayList<String> lstHeaders;
		int intTraitNameColumnIndex;
		int intLocationsNumberColumnIndex;
		String[] strArrayLineParts;
		String strTraitName;
		int intLocationsNumber;
		//Map<String, String> mapDataLine;
		
		this.intTraitsCapacity = intInTraitsCapacity;

		this.mapLocationsNumbers = new HashMap<String, Integer>(this.intTraitsCapacity);
		//this.lstMapExperimentSummaryData = new ArrayList<Map<String, String>>(this.intTraitsCapacity);
		
		objBufferedReader = null;
		intLineNumber = 0;
		strArrayHeaders = null;
		intTraitNameColumnIndex = -1;
		intLocationsNumberColumnIndex = -1;
		try {
			objBufferedReader = new BufferedReader(new FileReader(objInExperimentSummaryFilePath.toFile()));
			while ((strLine = objBufferedReader.readLine()) != null) {
				intLineNumber++;
				////////////////
				// The headers  
				////////////////
				if (intLineNumber == 1) {
					strArrayHeaders = strLine.trim().split(TAB);
					lstHeaders = new ArrayList<String>(Arrays.asList(strArrayHeaders));
					try {
						if (!(lstHeaders.contains("traitVatString"))) {
							throw new NoSuchFieldException("No traitVatString column in " + 
                                                           objInExperimentSummaryFilePath.toString());
						}
						else {
							intTraitNameColumnIndex = lstHeaders.indexOf("traitVatString");
						}
						
						//////////////////////
						// Locations Numbers  
						//////////////////////
						if (!(lstHeaders.contains("numberLocations"))) {
							throw new NoSuchFieldException("No numberLocations column in " + 
                                                           objInExperimentSummaryFilePath.toString());
						}
						else {
							intLocationsNumberColumnIndex = lstHeaders.indexOf("numberLocations");
						}
					}
					catch (NoSuchFieldException e) {
						e.printStackTrace();
						objLogger.error("ExperimentSummaryReader.ExperimentSummaryReader", 
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
							
							//////////////////////
							// Locations Numbers    
							//////////////////////
							try {
								if (strArrayLineParts[intLocationsNumberColumnIndex].trim().equals("")) {
									intLocationsNumber = 0;
								}
								else {
									intLocationsNumber = Integer.parseInt(strArrayLineParts[intLocationsNumberColumnIndex].trim());
								}
								this.mapLocationsNumbers.put(strTraitName, 
										                     intLocationsNumber);
							}
							catch (NumberFormatException e) {
								e.printStackTrace();
								objLogger.error("ExperimentSummaryReader.ExperimentSummaryReader", 
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
							//	   intArrayIndex < strArrayLineParts.length;	
							//     intArrayIndex++) {
							//	mapDataLine.put(strArrayHeaders[intArrayIndex], 
							//		            strArrayLineParts[intArrayIndex]);
							//}
							//this.lstMapExperimentSummaryData.add(mapDataLine);
						}
						else {
							throw new NoSuchFieldException("Inconsistency in the headers and fields numbers in " + 
                                                           objInExperimentSummaryFilePath.toString());
						}
					}
					catch (NoSuchFieldException e) {
						e.printStackTrace();
						objLogger.error("ExperimentSummaryReader.ExperimentSummaryReader", 
			                            e);
						throw new RuntimeException();
					}	
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			objLogger.error("ExperimentSummaryReader.ExperimentSummaryReader", 
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
				objLogger.error("ExperimentSummaryReader.ExperimentSummaryReader", 
                		        e);	
				throw new RuntimeException();
			}
		}
	}
	
	public Map<String, Integer> getLocationsNumbers() {
		return this.mapLocationsNumbers;
	}

	//public List<Map<String, String>> getData() {
	//	return this.lstMapExperimentSummaryData;
	//}
}
