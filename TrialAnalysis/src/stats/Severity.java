package stats;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import asreml.AsremlTrait;
import report.Summary;
import utils.Funcs;
import utils.Globals.SummaryType;
import data.collection.ExpFBKs;
import data.xml.objects.Trait;
import data.xml.objects.Trial;

public class Severity {
	static Logger log = Logger.getLogger(Incidence.class.getName());
	public static void calculateSeverity(Collection<Summary> summaries,
			AsremlTrait trait, ExpFBKs fbks, Trial trial) {
		
		String name = Funcs.quoteString(trait.getName());
		String selAvg="SELECT \"genoId\", AVG(CAST (:trait AS FLOAT)) as SEVERITY";
		String whereAvg="WHERE CAST(:trait AS FLOAT) <> 0 AND :trait IS NOT NULL GROUP BY \"genoId\"";
		
		selAvg = selAvg.replace(":trait", name);
		whereAvg = whereAvg.replace(":trait", name);
	
		List<Map<String,Object>> severities = fbks.get(selAvg,whereAvg,"");
		
		Map<String,Object> severity= new HashMap<>();
		
		for (Map<String,Object>  dat : severities){	 	
			String geno = dat.get("genoId").toString();
			double sev = Double.parseDouble(dat.get("SEVERITY").toString());
			severity.put(geno,sev);
		}
		
		
		String genoType = trial.getDataLevel().get("genoType");
		List<Object> genoIds = trial.getFbks().getColMap(genoType);
		Trait traitSeverity = null;
		traitSeverity = new Trait(name.replace("\"","")+"_severity",name.replace("\"","")+"_severity"); 
		
		
		for(Iterator<Object> git = genoIds.iterator(); git.hasNext();){
			Object genoObj = git.next();
			String genoId = genoObj.toString();
							
			if(severity.get(genoId)!=null){
				LinkedHashMap<String, Integer> filter = new LinkedHashMap<>();
				filter.put(genoType, fbks.findIndex(genoType, genoId, false));
				Summary summaryIndex = GeneralStats.createEntrySummary(traitSeverity, SummaryType.entrySummary, genoType, Integer.parseInt(genoId), trial, filter);
				summaryIndex.getValues().put("estimate", severity.get(genoId));
				summaries.add(summaryIndex);
			}
		}
		//Get Severity values into the Entry summary as a new column. right now we add new rows
		/* 
		for (Summary summary : summaries){
			summary.getValues().put("severity", Severity.get(summary.getValues().get("genoId")));
			
		}
	 */
		
		
		
	
		}

}
