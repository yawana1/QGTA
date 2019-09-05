/*
 * 
 */
package highLevelSummary;

import data.xml.objects.Crop;

public class CornSilage extends HLSummary{

	public CornSilage(){
		crop = Crop.corn.toString();
		classType = "Silage";
		String[] traits = {"ear_ht","ear_ht_cm","grn_snap","moisture","pctrl","pctsl","plant_ht","plant_ht_cm","test_wt","yield","yield_tph"}; //needs to be alpha order
		this.traits = traits;
	}
}