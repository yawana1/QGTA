package vat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import utils.Globals;
import utils.Globals.SummaryType;
import data.xml.objects.Crop;
import data.xml.objects.DBType;
import db.Sessions;
import db.hibernate.HibernateStoredProc;

/**
 * Handles the uploading of the summary reports
 * 
 * @author Scott Smith
 *
 */
public class Vat {

	static Logger log = Logger.getLogger(Vat.class.getName());
	
	private LinkedHashMap<SummaryType, VatSummary> vatSummaries; //order it's loaded will be the order written out
	private List<VatSummary> vatSummary;
	

	public Map<SummaryType, VatSummary> getVatSummaries() {
		if(null == vatSummaries){
			vatSummaries = new LinkedHashMap<Globals.SummaryType, VatSummary>();
			for(VatSummary summary : vatSummary){
				vatSummaries.put(summary.getSummaryType(), summary);
			}
		}
		return vatSummaries;
	}
	
	public void setVatSummaries(LinkedHashMap<SummaryType, VatSummary> vatSummaries) {
		this.vatSummaries = vatSummaries;
	}
	
	public VatSummary get(SummaryType type){
		getVatSummaries();
		return vatSummaries.get(type);
	}
	
	/**
	 * Calls stored procs on VAT to load sent file.
	 * Currently VAT needs Tab separated files.
	 
	 * @param procName
	 * @param params
	 */
	public static void uploadFileToStoredProc(String procName, List<Object> params){
		org.hibernate.Session session = null;
		try{
			//proc name stored in config file.
			if(null != procName && !procName.isEmpty()){
				session = Sessions.INSTANCE.get(Crop.corn, DBType.VAT).openSession();
				HibernateStoredProc.callStoredProc(session, procName, params);
			}
		}
		catch(Exception e){
			log.error("Problem uploading to stored proc - "+ " sp: "+ procName, e);
		}
		finally{
			if(null != session && session.isOpen()){
				session.close();
			}
		}
	}
}