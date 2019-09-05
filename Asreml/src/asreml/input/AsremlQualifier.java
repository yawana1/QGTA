package asreml.input;


/**
 * The Class Qualifier is used to define qualifier in the ASREML command file
 */
public class AsremlQualifier {

	private String name;	
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public AsremlQualifier(String name){
		this.name = name;
	}

	@Override
	public String toString() {
		return name + ((value!=null && !value.equals("0")) ? " "+value+ " ": " ");
	}

	public AsremlQualifier(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public boolean equals(Object o){
		boolean result = false;
		if(name != null && o !=null){
			if(o instanceof AsremlQualifier){
				result = name.equals(((AsremlQualifier)o).getName());
			}
			else{
				result = super.equals(o);
			}
		}
		return result;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}