/*
 * 
 * 
 * @package 	asreml
 * @class 		AsremlGlobals.java
 * @author 		u409397
 * @modified 	Jul 8, 2010
 * 
 */
package asreml;



/**
 * AsremlGlobals used to store global constants and enums for
 * ASREML.
 */
public interface AsremlGlobals {

	/**
	 * The Enum of flags used in the ASREML command file.
	 */
	enum Flag{

		/**pedigree */
		p("!P"),
		
		/** The g. */
		g("!G"),

		/** The gp. */
		gp("!GP"),

		a("!A"),
		/** The i. */
		i("!I"),

		/** The aising. */
		aising("!AISING"),

		/** The maxit. */
		maxit("!MAXIT"),

		/** The extra. */
		extra("!EXTRA"),
		
		outlier("!OUTLIER"),

		/** The ll. */
		ll("!LL")
		;

		private final String value;

		public String value(){return value;}

		/**
		 * Instantiates a new flag.
		 * 
		 * @param value
		 *            the value
		 */
		Flag(String value){
			this.value = value;
		}
	}

	/**
	 * The Enum SourceMatrix.
	 */
	enum SourceMatrix {
		
		/** The GIV. */
		GIV, 
		
		/** The IDV. */
		IDV,
		AINV
	}

	/**
	 * The Enum AsConst.
	 */
	enum AsConst {
		bounded("B"),
		
		positiveDefinite("P"),
		
		/** The bend cmd. */
		bendCmd("./bend"),
		
		converged("LogL Converged"),

		/** The tmp directory. */
		tmpDirectory("/tmp"),

		/** The sh cmd. */
		shCmd("/bin/sh"),

		/** The prefix. */
		prefix("asreml"),

		/** The model suffix. */
		modelSuffix(".as"),

		/** The script suffix. */
		scriptSuffix(".sh"),

		/** The output suffix. */
		outputSuffix(".output"),

		/** The asd suffix. */
		asdSuffix(".asd"),

		/** The grm suffix. */
		grmSuffix(".grm"),
		
		mu("mu"),
		
		Trait("Trait");

		/** The value. */
		private final String value;

		/**
		 * Value.
		 * 
		 * @return the string
		 */
		public String value(){return value;}

		/**
		 * Instantiates a new as const.
		 * 
		 * @param value
		 *            the value
		 */
		AsConst(String value){this.value = value;}
	}

	/** The asreml script. */
	public String asremlScript = "";

	/** The tmp directory. */
	public String tmpDirectory = "/tmp";
	
	/**
	 * String passed to the Covariance engine to define the type
	 * @author Scott Smith
	 *
	 */
	public static enum GrmType{
		 hybrid
		,inbred
		,similarity
	}
	
	/**
	 * Set's the delimiter that Asreml will use in it's output files 
	 * 
	 * @author Scott Smith
	 *
	 */
	public static enum OutputFormat{
		 COMMA(2,",",".csv")
		,AMP(3,"&",".tex")
		;
		private String delimit, ext;
		public String getExt(){return ext;}
		public String getDelimit(){return delimit;}
		int txtform = 0;
		OutputFormat(int txtform, String delimit, String ext){
			this.txtform = txtform;
			this.delimit = delimit;
			this.ext = ext;
		}
		
		public static OutputFormat get(String txtform){
			OutputFormat result = null;
			for(OutputFormat outFormat: OutputFormat.values()){
				if(txtform.equals(""+outFormat.txtform)){
					result = outFormat;
					break;
				}
			}
			return result;
		}
	}
}