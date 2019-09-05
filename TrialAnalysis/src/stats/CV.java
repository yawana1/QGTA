package stats;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import report.ReportOutput;
import report.Summary;
import utils.Funcs;
import asreml.output.AsremlOutput;
import asreml.output.AsremlOutputFile;
import asreml.output.Tab;
import asreml.output.Yht;
import asreml.output.Yhts;
import data.collection.ExpFBK;
import data.collection.ExpFBKs;
import data.xml.objects.Constants;
import data.xml.objects.Trait;

/**
 * Calculate CV from ms error and means. CV uses yht estimates. checkCV built
 * from raw data.
 * 
 * @author Scott Smith
 * 
 */
public class CV {

	static Logger log = Logger.getLogger(ReportOutput.class.getName());

	public static void calcCV(Collection<Summary> summaries, AsremlOutput asremlOutput, ExpFBKs fbks, Map<String, String> map){
		try{
			if(null != summaries){
				String genoType = map.get("genoType");
				String environment = map.get("environment");
				for(Summary summary : summaries){
					Trait trait = summary.getTrait();
					Double cv = 0d;
					Double checkCv = null;

					Double estimate = summary.getEstimate();
					Collection<Entry<Object, Double>> residuals = getResidules(asremlOutput.getYhts(), fbks, trait, environment, genoType, summary.getValues().get(environment));
					if(residuals.size() > 0){
						Double msError = calcMsError(residuals);
	
						if(null != estimate){
							cv = calcCV(estimate, msError);
						}
	
						summary.getValues().put("cv", cv < Double.parseDouble(Constants.INSTANCE.getConstant("max_number").toString()) ? cv : Double.parseDouble(Constants.INSTANCE.getConstant("max_number").toString()));
					}
					
					String checkType;
					String[] prepIds = {environment, "checkNum", genoType};
					String[] ids = {environment, "coreCheck", genoType};
					String colName = AsremlOutputFile.createId(ids);
					String colPrep = AsremlOutputFile.createId(prepIds);
					Collection<Tab> tabs = null;
					if(asremlOutput.getTabs().getTabs().containsKey(colPrep)){
						tabs = asremlOutput.getTabs().getTabs().get(colPrep);
						checkType = prepIds[1];
					}else{
						tabs = asremlOutput.getTabs().getTabs().get(colName);
						checkType = ids[1];
					}
					
					//get this overall mean
					LinkedHashMap<String, Integer> filters = summary.getFilters();
					ids = new String[filters.size()];
					AsremlOutputFile.createId(summary.getFilters().keySet().toArray(ids));
								
					if(null != tabs){
						Integer environmentId = summary.getFilters().get(environment);

						Collection<Tab> replicates = new ArrayList<>();
						for(Tab tab : tabs){
							if(tab.getKeys().get(0).equals(environmentId.toString()) && tab.getKeys().get(1).equals("2")){ //in that location and marked as a check or prep rep entry
								replicates.add(tab);
							}
						}
						
						Double mse = calcMSE(fbks, trait, summary.getValues().get(environment).toString(), environment, genoType, checkType);

						//skip if mse doesn't produce a real number
						if(mse != null && !mse.equals(Double.NaN)){
							estimate = 0d;
							for(Tab tab : replicates){
								estimate += tab.getMean();
							}
							
							if(null != estimate){
								estimate /= replicates.size();
								checkCv = calcCV(estimate, mse);
							}
							summary.getValues().put("checkCV", checkCv);
						}
					}
				}
			}
		}catch (Exception e) {
			log.warn("", e);
		}
	}

	/**
	 * Get the list of residuals for a passed in id from the yht records. Match
	 * up fbk records with yht records by skipping fbk records with a null pheno
	 * value as there will not be a yht record for null
	 * 
	 * @param yhts
	 * @param fbks
	 * @param trait
	 * @param environment
	 * @param id
	 * @return
	 */
	public static Collection<Entry<Object, Double>> getResidules(Yhts yhts,
			ExpFBKs fbks, Trait trait, String environment, String genoType, Object id) {
		Collection<Entry<Object, Double>> residuals = new ArrayList<Entry<Object, Double>>();

		Iterator<Integer> yit = yhts.getData().keySet().iterator();
		if (yit.hasNext()) {
			for (ExpFBK fbk : fbks.getFbks()) {
				if (fbk.getData().get(trait.toString()) == null) {
					continue; // skip since yht file will not have a record if
								// the pheno value is null
				}
				try {
					Integer i = yit.next();
					Yht yht = yhts.getData().get(i);
					if (fbk.getData().get(environment).equals(id)) {
						Object genoTypeId = fbk.getValue(genoType);
						residuals
								.add(new AbstractMap.SimpleEntry<Object, Double>(
										genoTypeId, yht.getResidual()));
					}
				} catch (Exception e) {
					log.warn("", e);
				}
			}
		}

		return residuals;
	}

	/**
	 * (EE (estimate - mean(estimates by genoType))^2 / (totalCount - unique
	 * genoTypes)
	 * 
	 * @param residuals
	 * @return
	 */
	public static Double calcMsError(Collection<Entry<Object, Double>> residuals) {
		Double totalResidule = 0d;
		double msError = 0d;
		Set<Object> uniqueIds = new HashSet<Object>();

		for (Entry<Object, Double> residule : residuals) {
			uniqueIds.add(residule.getKey());
			totalResidule += Math.pow(residule.getValue(), 2);
		}

		msError = totalResidule / (residuals.size() - 1);

		return msError;
	}

	public static Double calcCV(double msError, double effect, double mu) {
		return calcCV(msError, effect + mu);
	}

	/**
	 * ( sqrt(error) / |mean|) * 100
	 * 
	 * @param mean
	 * @param error
	 * @return
	 */
	public static Double calcCV(double mean, double error) {
		double cv = 0d;

		if (mean != 0) {
			cv = (Math.sqrt(error) / Math.abs(mean)) * 100;
		}

		return cv;
	}

	/**
	 * E ( y - avg(y))^2 / N - nc
	 * 
	 * @param environment
	 * @param tabs
	 * @return
	 */
	public static Double calcMSE(ExpFBKs fbks, Trait trait,
			String environmentId, String environment, String genoType, String checkColumn) {
		double mse = 0d;

		String from = " FROM EXP_FBK EF";
		String where = String.format(" WHERE %s = %s AND \"%s\"=true",
				Funcs.quoteString(environment), environmentId, checkColumn);
		String select = String
				.format(" SELECT \"genoId\", POWER((cast(\"%s\" AS DOUBLE) - (SELECT avg(cast(E.\"%s\" AS DOUBLE)) AS \"%s\"  FROM EXP_FBK E WHERE E.\"%s\" = EF.\"%s\" AND E.\"%s\" = EF.\"%s\")), 2) AS \"mean\"",
						trait.getName(), trait.getName(), trait.getName(),
						environment, environment, genoType, genoType);
		List<Map<String, Object>> results = fbks.get(select + from + where);

		if (results != null) {
			int totalRecords = results.size();
			int totalReplicatedRecords = 0;
			double runningTotal = 0d;

			// calc raw mean for each entry(
			Collection<String> entries = new HashSet<String>();
			for (Map<String, Object> row : results) {
				// skip any null values and don't count in degrees of freedom
				if (row.get("mean") != null) {
					String id = "" + row.get("genoId");
					runningTotal += Double.parseDouble("" + row.get("mean"));
					entries.add(id);
				}
			}

			totalReplicatedRecords = entries.size();
			double denom = totalRecords - totalReplicatedRecords;
			mse = runningTotal / denom;
		}
		return mse;
	}
}