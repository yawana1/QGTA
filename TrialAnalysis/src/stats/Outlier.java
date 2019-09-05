package stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import report.Summary;
import utils.Globals;
import utils.Globals.SummaryType;
import asreml.AsremlGlobals;
import asreml.input.AsremlQualifier;
import asreml.output.AsremlOutput;
import asreml.output.Yht;
import data.collection.ExpFBK;
import data.collection.ExpFBKs;
import data.xml.objects.Trait;

public class Outlier {

	static Logger log = Logger.getLogger(GeneralStats.class.getName());

	public static void outlier(Collection<Summary> summaries, Trait trait, 
			AsremlOutput asremlOutput, ExpFBKs fbks, List<Trait> outlierTraits){
		try{
			String modelName = asremlOutput.getModel().getTitle();
			AsremlQualifier asremlQualifier = asremlOutput.getModel().getQualifiers().get(AsremlGlobals.Flag.outlier.value());
			if(asremlQualifier == null){
				log.error("Model " + modelName + " does not have outlier set in the asreml qualifiers");
			}
			else{
				Iterator<Integer> yhtIndex = asremlOutput.getYhts().getData().keySet().iterator();
				if(yhtIndex.hasNext()){
					for(ExpFBK fbk : fbks.getFbks()){
						if(outlierTraits.contains(trait)){
							if(fbk.getData().get(trait.getName())==null){
								continue;
							}
							
							Integer rowId = fbk.getInt("rowId");
							Yht yht = asremlOutput.getYhts().getData().get(yhtIndex.next());
		
							Summary summary = createOutlierSummary(rowId, summaries, trait);
		
							if(trait.getOutlier()!=null && Math.abs(yht.getOutlier()) >= trait.getOutlier()){
								String outlierTrait = summary.getValues().get("outlierTraits") == null ? "" : summary.getValues().get("outlierTraits") + ",";
								summary.getValues().put("outlierTraits", outlierTrait + trait.getName());
								summary.getValues().put("outlier", "" + Globals.Outlier.YES);
								summaries.add(summary);
							}
						}
					}
				}
			}
		}catch(Exception e){
			log.warn("", e);
		}

	}

	private static Summary createOutlierSummary(Integer rowId, Collection<Summary> summaries, Trait trait) {
		Summary result = null;

		if(null != summaries){
			for(Summary summary : summaries){
				if(rowId.equals(summary.getValues().get("rowId"))){
					result = summary;
				}
			}
		}
		else{
			summaries = new ArrayList<Summary>();
		}

		if(null == result){
			Summary summary = new Summary(SummaryType.outlierSummary, trait);
			summary.getValues().put("rowId", "" + rowId);
			result = summary;
		}
		return result;
	}

	/**
	 * Combine outlier summaries so there is only one per rowId with comma separated trait list
	 * @param summariesOut
	 * @param summariesIn
	 */
	public static void combineOutliers(Collection<Summary> summariesOut, Collection<Summary> summariesIn){
		for(Summary summaryOut : summariesOut){
			if(summariesIn == null){
				summariesIn = new ArrayList<Summary>();
			}
			if(summariesIn.size() == 0){
				summariesIn.add(summaryOut);
			}
			else{
				boolean found = false;
				for(Summary summaryIn : summariesIn){
					if(summaryOut.getValues().get("rowId").equals(summaryIn.getValues().get("rowId")))
					{
						String outlierTrait = summaryIn.getValues().get("outlierTraits") == null ? "" : 
							summaryIn.getValues().get("outlierTraits") + ",";
						summaryIn.getValues().put("outlierTraits", outlierTrait + 
								summaryOut.getValues().get("outlierTraits"));
						found = true;
						break;
					}
				}
				if(!found){
					summariesIn.add(summaryOut);
				}
			}
		}
	}
}