package data;


/**
 * Serialize and Deserialize Trait xml data into objects using XStream
 * 
 */
public class TraitXML extends XML {
	public final static TraitXML INSTANCE = new TraitXML();

	// set the right alias for the Trait object
	private TraitXML(){
		super();
		xstream.alias(data.xml.objects.Trait.class.getSimpleName(), data.xml.objects.Trait.class);
	}
}