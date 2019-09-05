package asreml.input;


/**
 * The Class FixedEffect is used to define fixed effect in the ASREML command file
 */
public class AsremlFixedEffect extends AsremlEffect {
	
	public AsremlFixedEffect(String name){
		this.name = name;
	}
	
	public String toString() {
		return this.name;
	}
	
	public boolean equals(AsremlFixedEffect effect){
		return name.equals(effect.getName());
	}
}
