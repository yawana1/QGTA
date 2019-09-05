package data.xml.objects;

public class SqlColumn {

	private String name;
	private String varietyName;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVarietyName() {
		return varietyName;
	}
	public void setVarietyName(String varietyName) {
		this.varietyName = varietyName;
	}

	public String toSql(){
		String result = null;
		if(varietyName!=null && name!=null){
			result = varietyName + " AS " + name + ",";
		}
		return result;
	}
}
