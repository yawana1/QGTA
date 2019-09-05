package stats;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import data.xml.objects.Trait;
import data.xml.objects.Traits;
import data.xml.objects.Trial;
import report.ReportOutputs;
import report.Summary;
import utils.Globals.SummaryType;

/**
 * Calculate Indexes require for registration different EU countries.
 */
public class EUIndexes {

	private static Logger log = Logger.getLogger(EUIndexes.class);
	
	/**
	 * Try to find value of colum from the given summary.
	 * @param summary -
	 * @param column - Column on the Summary
	 * @return - Return the value of the column or 0.
	 */
	private static Double getSummaryValue(Summary summary, String column){
		Double result = 0d;
		if(summary != null){
			if(summary.getValues().get(column) != null){
				String value = summary.getValues().get(column).toString();
				result = Double.parseDouble(value);
			}
		}
		return result;
	}
	
	private static void calcEUSilage_NL(ReportOutputs reportOutputs, Trial trial, Collection<Summary> entrySilYld, Collection<Summary> entrySilMst, Collection<Summary> entryStarch, Collection<Summary> entryIdv){
		try{
			if(entrySilYld==null || entrySilMst==null || entryStarch==null|| entryIdv==null){
				return;
			}
			
			String genoType = trial.getDataLevel().get("genoType");
			List<Object> genoIds = trial.getFbks().getColMap(genoType);
			Collection<Summary> collection = new ArrayList<Summary>();
			Trait trait = Traits.INSTANCE.get("Index_S_NL");
			NumberFormat nf = NumberFormat.getInstance();
			nf.setGroupingUsed(false);
			for(Iterator<Object> git = genoIds.iterator(); git.hasNext();){
				try{
					Object genoObj = git.next();
					Integer genoId = Integer.parseInt(genoObj.toString());
					Integer genoIndex = GeneralStats.getGenoIndex(trial, genoType, genoObj);
					LinkedHashMap<String, Integer> filter = new LinkedHashMap<String, Integer>();
					filter.put(genoType, genoIndex);
					
					double rank = 0d;
					
					Summary summarySilYld = Summary.search(entrySilYld, filter);
					Summary summarySilMst = Summary.search(entrySilMst, filter);
					Summary summaryStarch = Summary.search(entryStarch, filter);
					Summary summaryIdv = Summary.search(entryIdv, filter);
						
					Double checkSilYld = getSummaryValue(summarySilYld,"coreCheck");
					Double checkSilMst = getSummaryValue(summarySilMst,"coreCheck");
					Double checkStarch = getSummaryValue(summaryStarch,"coreCheck");
					Double checkIdv = getSummaryValue(summaryIdv,"coreCheck");
						
					rank = 	(1/6d)*(200 - checkSilMst) 
									+ (1/3d) * (checkSilYld)
									+ (1/3d) * (checkIdv) 
									+ (1/6d) * (checkStarch-100+checkSilMst);

					Summary summaryIndex = GeneralStats.createEntrySummary(trait, SummaryType.entrySummary, genoType, genoId, trial);
					summaryIndex.getValues().put("rank", nf.format(rank));
					collection.add(summaryIndex);
				}catch(Exception e){
					log.warn(e.getMessage(), e);
				}
			}
			if(collection.size()>0){
				reportOutputs.get(trait, true).add(SummaryType.entrySummary, collection);
			}
		}
		catch (Exception e) {
			log.warn("", e);
		}
	}
	
	public static void calcEuGrainIndices(Trial trial, ReportOutputs reportOutputs){
		try{
			Collection<Summary> entryYield = reportOutputs.get(Traits.INSTANCE.get("yield_tph"), SummaryType.entrySummary);
			Collection<Summary> entryMst = reportOutputs.get(Traits.INSTANCE.get("moisture"), SummaryType.entrySummary);
			Collection<Summary> entrySl = reportOutputs.get(Traits.INSTANCE.get("sl"), SummaryType.entrySummary);
			Collection<Summary> entryRl = reportOutputs.get(Traits.INSTANCE.get("rl"), SummaryType.entrySummary);
			Collection<Summary> expYield = reportOutputs.get(Traits.INSTANCE.get("yield_tph"), SummaryType.experimentSummary);
			Collection<Summary> expMst = reportOutputs.get(Traits.INSTANCE.get("moisture"), SummaryType.experimentSummary);
			Collection<Summary> expSl = reportOutputs.get(Traits.INSTANCE.get("sl"), SummaryType.experimentSummary);
			
			
			Summary expYieldSummary = null;
			if(expYield != null && !expYield.isEmpty()){
				expYieldSummary = expYield.iterator().next();
			}
			Summary expMstSummary = null;
			if(expMst != null && !expMst.isEmpty()){
				expMstSummary = expMst.iterator().next();
			}
			Summary expSLSummary = null;
			if(expSl != null && !expSl.isEmpty()){
				expSLSummary = expSl.iterator().next();
			}
			
	//		if(entryYield==null || entryMst==null || expYield==null || expMst==null) return;
			calcEuGrain_DE(reportOutputs, trial, entryYield, entryMst, entrySl, expYieldSummary, expMstSummary, expSLSummary);
			calcEuGrain_FR(reportOutputs, trial, entryYield, entryMst, expMstSummary);
			calcEuGrain_HU(reportOutputs, trial, entryYield, entryMst, entrySl, entryRl);
		}
		catch (Exception e) {
			log.warn("",e);
		}
	}
	
	public static void calcEuSilageIndices(Trial trial, ReportOutputs reportOutputs){
		try{
			Collection<Summary> entrySilYld = reportOutputs.get(Traits.INSTANCE.get("sil_yield_dm_tha"), false).get(SummaryType.entrySummary);
			Collection<Summary> entryStarch = reportOutputs.get(Traits.INSTANCE.get("cho"), false).get(SummaryType.entrySummary);
			Collection<Summary> entryElo = reportOutputs.get(Traits.INSTANCE.get("elo_grn_nir"), false).get(SummaryType.entrySummary);
			Collection<Summary> entrySilMst = reportOutputs.get(Traits.INSTANCE.get("sil_moist_nir"), false).get(SummaryType.entrySummary);
			Collection<Summary> entrySl = reportOutputs.get(Traits.INSTANCE.get("sl"), false).get(SummaryType.entrySummary);
			Collection<Summary> entryUfl = reportOutputs.get(Traits.INSTANCE.get("ufl_grn_nir"), false).get(SummaryType.entrySummary);
			Collection<Summary> entryIdv = reportOutputs.get(Traits.INSTANCE.get("ivd_grn_nir"), false).get(SummaryType.entrySummary);
			
			Collection<Summary> _expSilYld = reportOutputs.get(Traits.INSTANCE.get("sil_yield_dm_tha"), false).get(SummaryType.experimentSummary);
			Collection<Summary> _expStarch = reportOutputs.get(Traits.INSTANCE.get("cho"), false).get(SummaryType.experimentSummary);
			Collection<Summary> _expElo = reportOutputs.get(Traits.INSTANCE.get("elo_grn_nir"), false).get(SummaryType.experimentSummary);
			Collection<Summary> _expSilMst = reportOutputs.get(Traits.INSTANCE.get("sil_moist_nir"), false).get(SummaryType.experimentSummary);
			Collection<Summary> _expSl = reportOutputs.get(Traits.INSTANCE.get("sl"), false).get(SummaryType.experimentSummary);
			
			Summary expSilYld = null;
			if(_expSilYld != null && !_expSilYld.isEmpty()){
				expSilYld = _expSilYld.iterator().next();
			}
			Summary expStarch = null;
			if(_expStarch != null && !_expStarch.isEmpty()){
				expStarch = _expStarch.iterator().next();
			}
			Summary expElo = null;
			if(_expElo != null && !_expElo.isEmpty()){
				expElo = _expElo.iterator().next();
			}
			Summary expSilMst = null;
			if(_expSilMst != null && !_expSilMst.isEmpty()){
				expSilMst = _expSilMst.iterator().next();
			}
			Summary expSl = null;
			if(_expSl != null && !_expSl.isEmpty()){
				expSl = _expSl.iterator().next();
			}
			
	//		if(entrySilMst==null || entrySilYld==null || entryStarch==null || entryElo==null || expSilMst==null || expSilYld==null || expStarch==null || expElo==null) return;
			calcEUSilage_DE(reportOutputs, trial, entrySilYld, entrySilMst, entryStarch, entryElo, entrySl, expSilYld, expSilMst, expStarch, expElo, expSl);
			calcEUSilage_FR(reportOutputs, trial, entrySilYld, entrySilMst, entryUfl, expSilMst);
			calcEUSilage_NL(reportOutputs, trial, entrySilYld, entrySilMst, entryStarch, entryIdv);
		}
		catch (Exception e) {
			log.warn("",e);
		}
	}
	
	private static void calcEuGrain_DE(ReportOutputs reportOutputs, Trial trial, Collection<Summary> entryYield, Collection<Summary> entryMst, Collection<Summary> entrySl, Summary expYield, Summary expMst, Summary expSl){
		try{
			String genoType = trial.getDataLevel().get("genoType");
			List<Object> genoIds = trial.getFbks().getColMap(genoType);
			List<Object> genoIndices = trial.getFbks().getColMap(genoType+"_index");
			Collection<Summary> collection = new ArrayList<Summary>();
			Trait trait = Traits.INSTANCE.get("Index_G_DE");
			NumberFormat nf = NumberFormat.getInstance();
			nf.setGroupingUsed(false);
			for(Iterator<Object> git = genoIds.iterator(); git.hasNext();){
				try{
					Object genoObj = git.next();
					Integer genoId = Integer.parseInt(genoObj.toString());
					Integer genoIndex = GeneralStats.getGenoIndex(genoIds,genoIndices, genoObj);
					LinkedHashMap<String, Integer> filter = new LinkedHashMap<String, Integer>();
					filter.put(genoType, genoIndex);
					
					Double rank = 0d;
											
					Summary summaryYield = Summary.search(entryYield, filter);
					Summary summaryMst = Summary.search(entryMst, filter);
					Summary summarySl = Summary.search(entrySl, filter);
		
					Double checkYld = getSummaryValue(summaryYield, "coreCheck");
					Integer locYld = getSummaryValue(summaryYield, "numberLocations").intValue();
					if(locYld==null)
						locYld = getSummaryValue(expYield, "numberLocations").intValue();
					Double estMst = getSummaryValue(summaryMst, "estimate");
					Double moisture = getSummaryValue(expMst, "coreCheckMean");
					Double sl = getSummaryValue(expSl, "coreCheckMean");
					Double estSl = getSummaryValue(summarySl,"estimate");
					Integer locSl = getSummaryValue(summarySl, "numberLocations").intValue();
		
					if(locYld != 0){
						rank= checkYld + (2.5*(moisture-estMst)) + ((sl-estSl)*locSl/locYld);
					}

					Summary summaryIndex = GeneralStats.createEntrySummary(trait, SummaryType.entrySummary, genoType, genoId, trial);
					summaryIndex.getValues().put("rank", nf.format(rank));
					collection.add(summaryIndex);
				}catch(Exception e){
					log.warn("",e);
				}
			}
			if(collection.size()>0){
				reportOutputs.get(trait, true).add(SummaryType.entrySummary, collection);
			}
		}
		catch (Exception e) {
			log.warn("",e);
		}
	}
	
	private static void calcEuGrain_FR(ReportOutputs reportOutputs, Trial trial, Collection<Summary> entryYield, Collection<Summary> entryMst, Summary expMst){
		String genoType = trial.getDataLevel().get("genoType");
		List<Object> genoIds = trial.getFbks().getColMap(genoType);
		List<Object> genoIndices = trial.getFbks().getColMap(genoType+"_index");
		Collection<Summary> collection = new ArrayList<Summary>();
		Trait trait = Traits.INSTANCE.get("Index_G_FR");
		NumberFormat nf = NumberFormat.getInstance();
		nf.setGroupingUsed(false);
		for(Iterator<Object> git = genoIds.iterator(); git.hasNext();){
			try{
				Object genoObj = git.next();
				Integer genoId = Integer.parseInt(genoObj.toString());
				Integer genoIndex = GeneralStats.getGenoIndex(genoIds,genoIndices, genoObj);
				LinkedHashMap<String, Integer> filter = new LinkedHashMap<String, Integer>();
				filter.put(genoType, genoIndex);
				
				double rank = 0d;
				Summary summaryYield = Summary.search(entryYield, filter);
				Summary summaryMst = Summary.search(entryMst, filter);
					
				Double checkYld = getSummaryValue(summaryYield, "coreCheck");
				Double estMst = getSummaryValue(summaryMst, "estimate");
				Double moisture = getSummaryValue(expMst, "coreCheckMean");
					
				rank = (10d*checkYld) + (2.5*(moisture-estMst)) - Math.pow(moisture-estMst, 2d) - 1d;

				Summary summaryIndex = GeneralStats.createEntrySummary(trait, SummaryType.entrySummary, genoType, genoId, trial);
				summaryIndex.getValues().put("rank", nf.format(rank));
				collection.add(summaryIndex);
			}catch(Exception e){
				log.warn("",e);
			}
		}
		
		if(collection.size()>0){
			reportOutputs.get(trait, true).add(SummaryType.entrySummary, collection);
		}
	}
	
	private static void calcEuGrain_HU(ReportOutputs reportOutputs, Trial trial, Collection<Summary> entryYield, Collection<Summary> entryMst, Collection<Summary> entrySl, Collection<Summary> entryRl){
		try{
			String genoType = trial.getDataLevel().get("genoType");
			List<Object> genoIds = trial.getFbks().getColMap(genoType);
			List<Object> genoIndices = trial.getFbks().getColMap(genoType+"_index");
			Collection<Summary> collection = new ArrayList<Summary>();
			Trait trait = Traits.INSTANCE.get("Index_G_HU");
			NumberFormat nf = NumberFormat.getInstance();
			nf.setGroupingUsed(false);
			for(Iterator<Object> git = genoIds.iterator(); git.hasNext();){
				Integer genoId = null;
				try{
					Object genoObj = git.next();
					genoId = Integer.parseInt(genoObj.toString());
					Integer genoIndex = GeneralStats.getGenoIndex(genoIds,genoIndices, genoObj);
					LinkedHashMap<String, Integer> filter = new LinkedHashMap<String, Integer>();
					filter.put(genoType, genoIndex);
					
					double rank = 0d;

					Summary summaryYield = Summary.search(entryYield, filter);
					Summary summaryMst = Summary.search(entryMst, filter);
					Summary summarySl = Summary.search(entrySl, filter);
					Summary summaryRl = Summary.search(entryRl, filter);
						
					Double checkYld = getSummaryValue(summaryYield, "coreCheck");
					Double checkMst = getSummaryValue(summaryMst, "coreCheck");
					Double estSl = getSummaryValue(summarySl, "estimate");
					Double estRl = getSummaryValue(summaryRl, "estimate");
						 
					rank = (3 * checkYld * (200 - checkMst) / 100) / ((estSl + estRl + 100)/100);

					Summary summaryIndex = GeneralStats.createEntrySummary(trait, SummaryType.entrySummary, genoType, genoId, trial);
					summaryIndex.getValues().put("rank", nf.format(rank));
					collection.add(summaryIndex);
				}catch(Exception e){
					log.warn("GenoId = " + genoId, e);
				}
			}
			if(collection.size()>0){
				reportOutputs.get(trait, true).add(SummaryType.entrySummary, collection);
			}
		}
		catch (Exception e) {
			log.warn("", e);
		}
	}
	
	private static void calcEUSilage_DE(ReportOutputs reportOutputs, Trial trial, Collection<Summary> entrySilYld, Collection<Summary> entrySilMst, Collection<Summary> entryStarch, Collection<Summary> entryElo, Collection<Summary> entrySl, 
			Summary expSilYld, Summary expSilMst, Summary expStarch, Summary expElo, Summary expSl){
		try{
			String genoType = trial.getDataLevel().get("genoType");
			List<Object> genoIndices = trial.getFbks().getColMap(genoType+"_index");
			List<Object> genoIds = trial.getFbks().getColMap(genoType);
			Collection<Summary> collection = new ArrayList<Summary>();
			Trait trait = Traits.INSTANCE.get("Index_S_DE");
			NumberFormat nf = NumberFormat.getInstance();
			nf.setGroupingUsed(false);
			for(Iterator<Object> git = genoIds.iterator(); git.hasNext();){
				try{
					Object genoObj = git.next();
					Integer genoId = Integer.parseInt(genoObj.toString());
					Integer genoIndex = GeneralStats.getGenoIndex(genoIds, genoIndices, genoObj);
					LinkedHashMap<String, Integer> filter = new LinkedHashMap<String, Integer>();
					filter.put(genoType, genoIndex);
					
					double rank = 0d;
					Summary summarySilYld = Summary.search(entrySilYld, filter);
					Summary summarySilMst = Summary.search(entrySilMst, filter);
					Summary summaryStarch = Summary.search(entryStarch, filter);
					Summary summaryElo = Summary.search(entryElo, filter);
					Summary summarySl = Summary.search(entrySl, filter);
						
					Double checkSilYld = getSummaryValue(summarySilYld, "coreCheck");
					Integer locSilYld = getSummaryValue(summarySilYld, "numberLocations").intValue();
					if(locSilYld==null)
						locSilYld = getSummaryValue(expSilYld, "numberLocations").intValue();
					Double estStarch = getSummaryValue(summaryStarch, "estimate");
					Double starch = getSummaryValue(expStarch, "coreCheckMean");
					Double estElo = getSummaryValue(summaryElo, "estimate");
					Double elo = getSummaryValue(expElo, "coreCheckMean");
					Double estSilMst = getSummaryValue(summarySilMst, "estimate");
					Double silMst = getSummaryValue(expSilMst, "coreCheckMean");
					Double estSl = getSummaryValue(summarySl, "estimate");
					Double sl = getSummaryValue(expSl, "coreCheckMean");
					Integer locSl = getSummaryValue(summarySl, "numberLocations").intValue();
						
					if(locSilYld != 0){					
						rank = 	checkSilYld + (0.5 * (estStarch - starch)) 
									+ (0.5 * (estElo - elo)) 
									+ (silMst - estSilMst) 
									+ ((sl - estSl) * locSl / locSilYld);
					}
					Summary summaryIndex = GeneralStats.createEntrySummary(trait, SummaryType.entrySummary, genoType, genoId, trial);
					summaryIndex.getValues().put("rank", nf.format(rank));
					collection.add(summaryIndex);
				}catch(Exception e){
					log.warn(e.getMessage());
				}
			}
			if(collection.size()>0) reportOutputs.get(trait, true).add(SummaryType.entrySummary, collection);
		}
		catch (Exception e) {
			log.warn("", e);
		}
	}
	
	private static void calcEUSilage_FR(ReportOutputs reportOutputs, Trial trial, Collection<Summary> entrySilYld, Collection<Summary> entrySilMst, Collection<Summary> entryUfl, Summary expSilMst){
		try{
			String genoType = trial.getDataLevel().get("genoType");
			List<Object> genoIds = trial.getFbks().getColMap(genoType);
			List<Object> genoIndices = trial.getFbks().getColMap(genoType+"_index");
			Collection<Summary> collection = new ArrayList<Summary>();
			Trait trait = Traits.INSTANCE.get("Index_S_FR");
			NumberFormat nf = NumberFormat.getInstance();
			nf.setGroupingUsed(false);
			for(Iterator<Object> git = genoIds.iterator(); git.hasNext();){
				try{
					Object genoObj = git.next();
					Integer genoId = Integer.parseInt(genoObj.toString());
					Integer genoIndex = GeneralStats.getGenoIndex(genoIds, genoIndices, genoObj);
					LinkedHashMap<String, Integer> filter = new LinkedHashMap<String, Integer>();
					filter.put(genoType, genoIndex);
					Summary summarySilYld = Summary.search(entrySilYld, filter);
					Summary summarySilMst = Summary.search(entrySilMst, filter);
					Summary summaryUfl = Summary.search(entryUfl, filter);
					
					double rank = 0d;
					Double estSilYld = getSummaryValue(summarySilYld, "estimate");
					Double estSilMst = getSummaryValue(summarySilMst, "estimate");
					Double silMst = getSummaryValue(expSilMst, "coreCheckMean");
					Double estUfl = getSummaryValue(summaryUfl, "estimate");
					
					rank = ((estSilYld + (0.2 * (silMst - estSilMst)) - (Math.pow((silMst - estSilMst), 2)/10))) 
								* (estUfl - (0.0025 * (silMst - estSilMst)));
					Summary summaryIndex = GeneralStats.createEntrySummary(trait, SummaryType.entrySummary, genoType, genoId, trial);
					summaryIndex.getValues().put("rank", nf.format(rank));
					collection.add(summaryIndex);
				}catch(Exception e){
					log.warn(e.getMessage(), e);
				}
			}
			if(collection.size()>0) reportOutputs.get(trait, true).add(SummaryType.entrySummary, collection);
		}
		catch (Exception e) {
			log.warn("",e);
		}
	}
}