package asreml.input;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Class FixedEffects is used to store a collection of {@link AsremlFixedEffect}
 * s.
 */
public class AsremlFixedEffects {

	private List<AsremlFixedEffect> fixedEffects = new ArrayList<AsremlFixedEffect>();	
	
	public void add(AsremlFixedEffect fixedEffect){
		fixedEffects.add(fixedEffect);
	}
	
	public boolean contains(String name){
		return fixedEffects.contains(new AsremlFixedEffect(name));
	}
	
	public List<AsremlFixedEffect> getFixedEffects(){
		return fixedEffects;
	}
	
	public AsremlFixedEffect get(String name){
		AsremlFixedEffect fixedEffect = null;
		int index = fixedEffects.indexOf(new AsremlFixedEffect(name));
		if(index != -1){
			fixedEffect = fixedEffects.get(index);
		}
		return fixedEffect;
	}
	
	@Override
	public String toString() {
		String str = "";
		
		for(Iterator<AsremlFixedEffect> cit = fixedEffects.iterator(); cit.hasNext();){
			str += " "+cit.next();
		}
		return str;
	}
	
	public int size(){
		return fixedEffects.size();
	}
}
