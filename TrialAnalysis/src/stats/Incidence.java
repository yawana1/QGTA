package stats;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import asreml.AsremlTrait;
import data.collection.ExpFBKs;
import data.xml.objects.Trait;
import data.xml.objects.Trial;
import report.Summary;
import utils.Funcs;
import utils.Globals.SummaryType;


public class Incidence{
	static Logger log = Logger.getLogger(Incidence.class.getName());
	//private static List<Map<String, Object>> incidence;
	

	public static void calculateIncidence(Collection<Summary> summaries,
			AsremlTrait trait, ExpFBKs fbks, Trial trial ) {
		String select= "SELECT \"genoId\", COUNT(*) AS NUM"; 
		String where_nonzero=" WHERE CAST (:trait as FLOAT) <> 0 AND :trait IS NOT NULL GROUP BY \"genoId\"" ;
		String where_zero=" WHERE CAST(:trait as FLOAT) = 0 AND :trait IS NOT NULL GROUP BY \"genoId\"";
			
		//String select_new= "SELECT \"genoId\", 100.0*E.NZ_COUNT/(E.NZCOUNT+F.ZERO_COUNT) AS INCIDENCE FROM (SELECT \"genoId\", COUNT(*) AS NZ_COUNT FROM \"fbks\" WHERE :trait<>0 GROUP BY \"genoId\") E ,(SELECT \"genoId\", COUNT(\"genoId\") AS ZEROES FROM \"fbks\"  WHERE :trait=0  GROUP BY \"genoId\")F  GROUP BY \"genoId\"";
		
		String name = Funcs.quoteString(trait.getName());
		
		select = select.replace(":trait", name);
		
		where_nonzero = where_nonzero.replace(":trait", name);
		where_zero = where_zero.replace(":trait", name);
		
		//select_new = select_new.replace(":trait",name);
		
		List<Map<String,Object>> data_nonzero = fbks.get(select,where_nonzero,""); 
		List<Map<String,Object>> data_zero = fbks.get(select,where_zero,""); 
		
		//Re-map the fbk outputs to two Maps keyed by genoID
		Map<String,Object> nonzeroes = new HashMap<>() ;
		Map<String,Object> zeroes = new HashMap<>() ;
		
		for (Map<String,Object>  nz_data : data_nonzero){
			String geno= nz_data.get("genoId").toString();
			float num=Float.parseFloat(nz_data.get("NUM").toString());
			nonzeroes.put(geno, num);
			}
			
		for (Map<String,Object>  zero_data : data_zero){
			String geno= zero_data.get("genoId").toString();
			float num=Float.parseFloat(zero_data.get("NUM").toString());
			zeroes.put(geno, num);
			}
		
		Map<String,Double> incidence = new HashMap<>();
		
		for (String check_key : zeroes.keySet()){
			float z=(float) zeroes.get(check_key);
			float nonz;
			Object nz = nonzeroes.get(check_key);
			
			if (nz==null) {
				nonz=0;
				}
			else{
				
				nonz= (float) nz; 
				}
			
			double inc= nonz/(nonz+z);
			incidence.put(check_key, inc);
			}
		
		for( String check_key : nonzeroes.keySet()){
			Double check=incidence.get(check_key);
			if (check==null){
				float nonz=(float) nonzeroes.get(check_key);
				float zer;
				Object z= zeroes.get(check_key);
				
				if (z==null){
					zer=0;
					}
				else{
					zer= (float) z;
					}
	
				double inc= nonz/(nonz+zer);
				incidence.put(check_key,inc);
				}
			}
		
		
		String genoType = trial.getDataLevel().get("genoType");
		List<Object> genoIds = trial.getFbks().getColMap(genoType);
		Trait trait_incidence = null;
		trait_incidence = new Trait(name.replace("\"","")+"_incidence",name.replace("\"","")+"_incidence"); 
		
		
		for(Iterator<Object> git = genoIds.iterator(); git.hasNext();){
			Object genoObj = git.next();
			String genoId = genoObj.toString();
							
			if(incidence.get(genoId)!=null){
				LinkedHashMap<String, Integer> filter = new LinkedHashMap<>();
				filter.put(genoType, fbks.findIndex(genoType, genoId, false));
				Summary summaryIndex = GeneralStats.createEntrySummary(trait_incidence, SummaryType.entrySummary, genoType, Integer.parseInt(genoId), trial, filter);
				summaryIndex.getValues().put("estimate", incidence.get(genoId));
				summaries.add(summaryIndex);
				
			}
	
			
		}
		
		//log.info("Incidence "+name+" "+Incidence);
		
			//Get Incidence values into Entry summary - as a separate column- Commented out
			/*
			for (Summary summary : summaries){
				summary.getValues().put("incidence", Incidence.get(summary.getValues().get("genoId")));
				
			}
			*/
	}
}

