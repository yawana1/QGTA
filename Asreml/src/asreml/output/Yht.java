/*
 * 
 * @package 	asreml.output.yht
 * @class 		Yht.java
 * @author 		u409397
 * @modified 	Jul 8, 2010
 * 
 */
package asreml.output;

/**
 * The Class Yht.
 */
public class Yht {

	private int record;
	private double yhat;
	private double residual;
	private double hat;
	private Double rinv;
	private Double outlier;
	
	public Double getRinv() {
		return rinv;
	}

	public void setRinv(Double rinv) {
		this.rinv = rinv;
	}

	public Double getOutlier() {
		return outlier;
	}

	public void setOutlier(Double outlier) {
		this.outlier = outlier;
	}

	public Integer getRecord() {
		return record;
	}

	public void setRecord(Integer record) {
		this.record = record;
	}

	public Double getYhat() {
		return yhat;
	}

	public void setYhat(Double yhat) {
		this.yhat = yhat;
	}

	public Double getResidual() {
		return residual;
	}

	public void setResidual(Double residual) {
		this.residual = residual;
	}

	public Double getHat() {
		return hat;
	}

	public void setHat(Double hat) {
		this.hat = hat;
	}

	public Yht(String[] data){
		this.record = Integer.parseInt(data[0].trim());
		this.yhat = Double.parseDouble(data[1].trim());
		this.residual = Double.parseDouble(data[2].trim());
		this.hat = Double.parseDouble(data[3].trim());
		this.rinv = data.length>4 ? Double.parseDouble(data[4].trim()) : null;
		this.outlier = data.length>5 ? Double.parseDouble(data[5].trim()) : null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return record+" "+yhat+" "+residual+" "+hat+" " + outlier;
	}
}
