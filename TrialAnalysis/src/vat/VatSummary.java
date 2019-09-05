package vat;

import java.util.List;

import utils.Globals.SummaryType;

/**
 * Defines which stored procedure in Vat to call to upload a Summary, based on the {@link SummaryType}.
 * Defined in the Vat.xml file in the data/properties directory. 
 * 
 * @author Scott Smith
 *
 */
public class VatSummary {

	private String procName;  //stored procdure on in the VAT database's name
	private List<String> procColumns; //column order stored proc is expecting
	private SummaryType summaryType;
	private String procStringColumns; //list of column that should be set as non decimals
	
	public String getProcStringColumns() {
		return procStringColumns;
	}

	public void setProcStringColumns(String procStringColumns) {
		this.procStringColumns = procStringColumns;
	}
	
	public String getProcName() {
		return procName;
	}
	public void setProcName(String procName) {
		this.procName = procName;
	}
	public List<String> getProcColumns() {
		return procColumns;
	}
	public void setProcColumns(List<String> procColumns) {
		this.procColumns = procColumns;
	}
	public SummaryType getSummaryType() {
		return summaryType;
	}
	public void setSummaryType(SummaryType summaryType) {
		this.summaryType = summaryType;
	}
	
	public VatSummary(String procName, List<String> procColumns,
			SummaryType summaryType, String procStringColumns) {
		super();
		this.procName = procName;
		this.procColumns = procColumns;
		this.summaryType = summaryType;
		this.procStringColumns = procStringColumns;
	}
	
	
}
