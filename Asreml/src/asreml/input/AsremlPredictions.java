package asreml.input;

import java.util.ArrayList;
import java.util.List;


public class AsremlPredictions {

	private List<AsremlPrediction> predictions = new ArrayList<AsremlPrediction>();

		public List<AsremlPrediction> getPredictions() {
		return predictions;
	}

	public void setPredictions(List<AsremlPrediction> predictions) {
		this.predictions = predictions;
	}
	
	public void add(AsremlPrediction prediction){
		predictions.add(prediction);
	}
	
	public AsremlPrediction getPrediction(int index){
		return predictions.get(index);
	}
	
	@Override
	public String toString() {
		String str ="";
		for (AsremlPrediction prediction : predictions) {
			str +="predict ";
			for(String factor : prediction.getFactors()){
				str += factor+" ";
			}
			if (prediction.getTag() != null) {
				str += prediction.getTag(); 
			}
			str += "\n";
		}
		
		return str;
	}
	
	public int size(){
		return predictions.size();
	}
}
