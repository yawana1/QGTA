package stats;

public class Measurement {

	private String name;
	private String outputName;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	/***
	 * No outputName set it will use the name field.
	 * @return
	 */
	public String getOutputName() {
		String result = outputName;
		if(result == null){
			result = name;
		}
		return result;
	}
	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}
	
	public Measurement(String name, String outputName) {
		super();
		this.name = name;
		this.outputName = outputName;
	}
}
