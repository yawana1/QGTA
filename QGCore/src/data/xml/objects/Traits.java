package data.xml.objects;

import java.util.List;

public class Traits {

	public final static Traits INSTANCE = new Traits();
	private List<Trait> trait;
	private Integer multiNomialScore;
	
	private Traits(){
	}

	public List<Trait> getTrait() {
		return trait;
	}

	public void setTrait(List<Trait> trait) {
		this.trait = trait;
	}
	
	public Integer getMultiNomialScore() {
		return multiNomialScore;
	}

	public void setMultiNomialScore(Integer multiNomialScore) {
		this.multiNomialScore = multiNomialScore;
	}

	public Trait get(String name){
		Trait trait = null;
		int index = this.trait.indexOf(new Trait(name));
		if(index != -1){
			trait = this.trait.get(index);
		}

		return trait;
	}
}
