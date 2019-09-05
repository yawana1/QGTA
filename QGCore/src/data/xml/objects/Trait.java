package data.xml.objects;

/**
 * Defines a Pheno Type.  Currently defined in the data/properties/trait.xml file.
 * 
 * @author Scott Smith
 *
 */
public class Trait {

	private String name; //Trait name used by Trial Analysis
	private String varietyName; //Name of column in Variety
	private String varietyAdjName;
	private String vatName; //Name of Trait expected by VAT
	private Integer direction; //Either smaller or bigger values are better
	private Double outlier; //Threshold to define if a trait value is an outlier or not.
	private Integer score; //if the trait is scored for mulit nominal analysis
	private boolean multivariant;  //defines if this trait is used as part of a multivariant analysis
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVarName() {
		return varietyName;
	}
	public void setVarName(String varName) {
		this.varietyName = varName;
	}
	public String getVatName() {
		return vatName;
	}
	public void setVatName(String vatName) {
		this.vatName = vatName;
	}
	public Integer getDirection() {
		return direction;
	}
	public void setDirection(Integer direction) {
		this.direction = direction;
	}
	public Double getOutlier() {
		return outlier;
	}
	public void setOutlier(Double outlier) {
		this.outlier = outlier;
	}
	public Integer getScore() {
		return score;
	}
	public void setScore(Integer score) {
		this.score = score;
	}
	public boolean isMultivariant() {
		return multivariant;
	}
	public void setMultivariant(boolean multivariant) {
		this.multivariant = multivariant;
	}
	public String getVarietyAdjName() {
		return varietyAdjName;
	}
	public void setVarietyAdjName(String varietyAdjName) {
		this.varietyAdjName = varietyAdjName;
	}
	
	public Trait(String name, String varietyName, String varietyAdjName, String vatName,
			Integer direction, Double outlier, boolean multivariant) {
		super();
		this.name = name;
		this.varietyName = varietyName;
		this.varietyAdjName = varietyAdjName;
		this.vatName = vatName;
		this.direction = direction;
		this.outlier = outlier;
		this.multivariant = multivariant;
	}
	
	public Trait(String name){
		this.name = name;
		
	}
	
	public Trait(String name,String vatName){
		this.name = name;
		this.vatName = vatName;
	}
	
	public Trait(){}
	
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

	/**
	 * Equals.
	 * 
	 * @param trait
	 *            the trait
	 * 
	 * @return true, if successful
	 */
	public boolean equals(Object trait){
		return name.equals(trait.toString());
	}
	
	/***
	 * Load trait with full cache Trait values
	 * @return
	 */
	public Object readResolve(){
		Trait t = this;
		if(null != Traits.INSTANCE.getTrait()){
		 t = Traits.INSTANCE.get(name);
		}
		return t;
	}
}