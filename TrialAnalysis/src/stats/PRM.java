package stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import report.ReportOutputs;
import report.Summary;
import utils.Funcs;
import utils.Globals.SummaryType;
import data.collection.ExpFBK;
import data.collection.ExpFBKs;
import data.xml.objects.Trait;
import data.xml.objects.Traits;
import data.xml.objects.Trial;

/**
 * Calculate Predicted Relative Maturity
 * Uses a regression from RM and Moisture and report as a Rank.
 */
public class PRM {

	private static Logger log = Logger.getLogger(PRM.class.getName());
	
	public static void calcPRM(Trial trial, ReportOutputs reportOutputs){
		Trait traitMst = !trial.getTraits().contains(Traits.INSTANCE.get("moisture")) ? Traits.INSTANCE.get("sil_mst_total") : Traits.INSTANCE.get("moisture");
		try{
			ExpFBKs fbks = trial.getFbks();
			Map<String, List<Double>> checks_R = new HashMap<String, List<Double>>();
			Map<String, List<Double>> checks_M = new HashMap<String, List<Double>>();
			Map<String, Double> M = new HashMap<String, Double>();
			Map<String, Double> R = new HashMap<String, Double>();
			
			Double avgR = 0d; Double avgM = 0d;
			Integer count = 0;

			for (ExpFBK fbk : fbks.getFbks()) {
				Object geno_id = fbk.getValue("genoId");
				if(fbks.getCoreChecks().contains(geno_id)){
					Double rm = fbk.getDbl("rm");
					Double mst = fbk.getDbl(traitMst.toString());
					if(rm==null || mst==null) continue;
					avgR += rm;
					avgM += mst;
					count ++;
					if(!checks_R.containsKey(geno_id)) checks_R.put(geno_id.toString(), new ArrayList<Double>());
					if(!checks_M.containsKey(geno_id)) checks_M.put(geno_id.toString(), new ArrayList<Double>());
					checks_R.get(geno_id).add(rm);
					checks_M.get(geno_id).add(mst);
//					}
				}
			}
			avgR = count==0 ? 0 : (avgR/count);
			avgM = count==0 ? 0 : (avgM/count);
			
			// average lines
			for(Iterator<String> gid = checks_R.keySet().iterator(); gid.hasNext();){
				String geno_id = gid.next();
				Double r = Funcs.averageList(checks_R.get(geno_id));
				Double m = Funcs.averageList(checks_M.get(geno_id));
				R.put(geno_id, r);
				M.put(geno_id, m);
			}
			
			// compute sum(R-avgR) and sum(M-avgM)
			Double beta_num = 0d;
			Double beta_denom = 0d;
			for(Iterator<String> git = R.keySet().iterator(); git.hasNext();){
				String geno_id = git.next();
				Double rm = R.get(geno_id);
				Double mst = M.get(geno_id);
				beta_num += (mst-avgM)*(rm-avgR);
				beta_denom += Funcs.sqr(mst-avgM);
			}
			
			// calculate beta 0 and 1
			Double beta_1 = beta_num/beta_denom;
			Double beta_0 = avgR - (beta_1*avgM);
			Trait rmTrait = Traits.INSTANCE.get("rm");
			String genoType = trial.getDataLevel().get("genoType");
			Collection<Summary> entryMst = reportOutputs.get(traitMst, false).get(SummaryType.entrySummary);
			Collection<Summary> collection = new ArrayList<Summary>();
			for(Summary summary : entryMst){
				Double est = summary.getEstimate();
				if(est == null){
					est = 0d;
				}
				est = beta_0 + (beta_1*est);
				Integer rank = Funcs.round(est);
				Integer genoId = Integer.parseInt(summary.getValues().get(genoType).toString());
				Summary summaryIndex = GeneralStats.createEntrySummary(rmTrait, SummaryType.entrySummary, genoType, genoId, trial);
				summaryIndex.getValues().put("rank", rank);
				collection.add(summaryIndex);
			}
			if(collection.size()>0) reportOutputs.get(rmTrait, true).add(SummaryType.entrySummary, collection);
		}catch(Exception e){
			log.warn(e.getMessage(), e);
		}
	}
}
