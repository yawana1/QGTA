package asreml;

/**
 * Defines a Trait for use in writing an Asreml .as file and reading in results from Asreml
 * 
 * @author Scott Smith
 *
 */
public class AsremlTrait {

	private String name;
	private Integer multiNomialScore;
	private boolean multivariant;
	
	public AsremlTrait(String name, Integer score){
		this.name = name;
		this.multiNomialScore = score;
		multivariant = false;
	}
	
	public AsremlTrait(String name, Integer score, boolean multivariant){
		this.name = name;
		this.multiNomialScore = score;
		this.multivariant = multivariant;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Integer getMultiNomialScore() {
		return multiNomialScore;
	}

	public void setMultiNomialScore(Integer multiNomialScore) {
		this.multiNomialScore = multiNomialScore;
	}

	public boolean isMultivariant() {
		return multivariant;
	}

	public void setMultivariant(boolean multivariant) {
		this.multivariant = multivariant;
	}

	public String toString(){
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AsremlTrait other = (AsremlTrait) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}