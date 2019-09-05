package data.xml.objects;

import java.util.List;

/**
 * Mapping bewteen Stat Model names and config file that defines that model.
 * 
 * @author Scott Smith
 * @see StatModels
 */
public class StatModels {
	
		public final static StatModels INSTANCE = new StatModels();
		private List<StatModel> models;
		
		private StatModels(){}

		public StatModel get(String name){
			StatModel model = null;
			int index = models.indexOf(new StatModel(name));
			if(index != -1){
				model = models.get(index);
			}
			return model;
		}

		public List<StatModel> getModels() {
			return models;
		}

		public void setModels(List<StatModel> models) {
			this.models = models;
		}
}