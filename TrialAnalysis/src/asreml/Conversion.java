package asreml;

import java.util.ArrayList;
import java.util.List;

import data.xml.objects.Trait;

/**
 * Convert TrailAnalysis objects to Asreml project objects.  Allows for complete separation of Asreml package.
 * 
 * @author Scott Smith
 *
 */
public class Conversion {

	/**
	 * Convert
	 * 
	 * @param traits - TrialAnalysis Traits
	 * @return - Asreml Traits
	 */
	public static List<asreml.AsremlTrait> trait(List<Trait> traits){
		List<asreml.AsremlTrait> asTraits = new ArrayList<asreml.AsremlTrait>();
		for(Trait t:traits){
			asTraits.add(trait(t));
		}
		return asTraits;
	}

	/**
	 * Convert
	 * 
	 * @param traits - TrialAnalysis Traits
	 * @return - Asreml Traits
	 */
	public static asreml.AsremlTrait trait(Trait trait){
		asreml.AsremlTrait asTrait = new asreml.AsremlTrait(trait.getName(), trait.getScore(), trait.isMultivariant());
		return asTrait;
	}	
}
