package db;

import java.util.HashMap;
import java.util.Map;

import data.xml.objects.Crop;
import data.xml.objects.DBType;

public class TADataSource{

	private Crop crop;
	private DataSource variety;
	private DataSource varietyTest;
	private DataSource varietyRep;
	private DataSource vat;
	private DataSource markerDB;
	private Map<DBType,DataSource> types;
	
	/***
	 * XStream method called after deserialization
	 * @return
	 */
	public Object readResolve(){
		types = new HashMap<DBType, DataSource>();
		types.put(DBType.VARIETY, variety);
		types.put(DBType.VAT, vat);
		types.put(DBType.VARIETY_REP, varietyRep);
		types.put(DBType.VARIETY_TEST, varietyTest);
		types.put(DBType.MARKERDB, varietyTest);
		return this;
	}
	
	public DataSource get(DBType type){
		return types.get(type);
	}
	public Crop getCrop() {
		return crop;
	}
	public void setCrop(Crop crop) {
		this.crop = crop;
	}
	public DataSource getVariety() {
		return variety;
	}
	public void setVariety(DataSource variety) {
		this.variety = variety;
	}
	public DataSource getVarietyRep() {
		return varietyRep;
	}
	public void setVarietyRep(DataSource varietyRep) {
		this.varietyRep = varietyRep;
	}
	public DataSource getVat() {
		return vat;
	}
	public void setVat(DataSource vat) {
		this.vat = vat;
	}
	public DataSource getVarietyTest() {
		return varietyTest;
	}
	public void setVarietyTest(DataSource varietyTest) {
		this.varietyTest = varietyTest;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((crop == null) ? 0 : crop.hashCode());
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
		TADataSource other = (TADataSource) obj;
		if (crop == null) {
			if (other.crop != null)
				return false;
		} else if (!crop.equals(other.crop))
			return false;
		return true;
	}

	public DataSource getMarkerDB() {
		return markerDB;
	}

	public void setMarkerDB(DataSource markerDB) {
		this.markerDB = markerDB;
	}
}