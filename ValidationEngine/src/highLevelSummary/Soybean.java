/*
 * 
 */
package highLevelSummary;

import data.xml.objects.Crop;

public class Soybean extends HLSummary{

	public Soybean(){
		crop = Crop.soybean.toString();
		classType = "Soybean_Trial";
		String[] traits = {"lodging_rate","maturity_day","plant_ht","yield"}; //needs to be alpha order
		this.traits = traits;
	}
}