/**
 * Not actually used!! Just a quick template for how to call native methods.
 */
package cov;

import org.apache.log4j.Logger;


enum ANALYSIS_TYPES{
	invalid, inbred, hybrid, regualr, forward, similarity
}

public class CovEngWrapper {
	
	private static Logger log = Logger.getLogger(CovEngWrapper.class.getName());
	
	static{
		try{
			System.loadLibrary("coveng");
		}
		catch(Exception e){
			log.error("RME lib not loaded");
		}
	}

	private native void version();
	private static native long getEngine(Params params);
	private static native void calc_A(long handle, String fName);
	private static native void calc_D(long handle, String fName);
	private static native void calc_H(long handle, String fName);
	private static native void stopEngine(long handle);

	public void run(Params params) {
		long handle = 0;
		try {
//			params = new Params("geno_c.txt","pedigree_c.txt","similarity_c.out", ANALYSIS_TYPES.hybrid);
			version();
			handle = getEngine(params);
			calc_A(handle, "A.grm");
			calc_D(handle, "D.grm");
		} 
		catch (Exception e) {
			System.err.print(e.toString());
		} 
		finally {
			if (handle != 0) {
				stopEngine(handle);
			}
		}
	}
}
