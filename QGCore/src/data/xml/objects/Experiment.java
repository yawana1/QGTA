package data.xml.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Experiment {

	private String name;
	private Season season;
	private String region;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Season getSeason() {
		return season;
	}
	public void setSeason(Season season) {
		this.season = season;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	
	public Experiment(String name, String seasonName, String seasonId) {
		super();
		this.name = name;
		this.season = new Season(seasonName, seasonId);
	}
	
	public Experiment(String name, String seasonName, String seasonId, String region) {
		super();
		this.name = name;
		this.season = new Season(seasonName, seasonId);
		this.region = region;
	}
	
	public Experiment(String name, Season season) {
		super();
		this.name = name;
		this.season = season; 
	}
	
	/**
	 * Create an list of Experiment objects from a jobDef which is a Map of a List of trials and the seasons they're in.
	 * @param experimentsBySeason
	 * @return
	 */
	public static List<Experiment> getExperiments(Map<Season,List<String>> experimentsBySeason) {
		List<Experiment> experiments = new ArrayList<Experiment>();
		if(experimentsBySeason != null && experimentsBySeason.size() > 0){
			for(Season season : experimentsBySeason.keySet()){
				for(String name : experimentsBySeason.get(season)){
					experiments.add(new Experiment(name, season.getSeasonName(), season.getSeasonId()));
				}
			}
		}
		
		return experiments;
	}
}
