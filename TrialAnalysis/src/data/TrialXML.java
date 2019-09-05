package data;

import asreml.AsremlGlobals.GrmType;
import asreml.input.AsremlColumns;
import asreml.input.AsremlModelBlup;
import cov.MatrixOptions;
import db.modules.RetrieveDataDBTrial;
import db.modules.SQLBuilder;
import report.OutputDeleteWorkingDir;
import report.OutputExcel;
import report.OutputHeatmap;
import report.OutputOutlier;
import report.OutputVat;
import report.Summary;
import transformation.Grm;
import transformation.SpatialCorrection;
import validate.HybridPerBlock;
import validate.Insilico;
import validate.NullColumns;
import validate.StdDevOutliers;

/**
 * Serialize and Deserialize xml data into objects using XStream
 * 
 * @author Scott Smith
 *
 * @see <a href="http://xstream.codehaus.org/index.html">http://xstream.codehaus.org/index.html</a>
 */
public class TrialXML extends XML {
	public final static TrialXML INSTANCE = new TrialXML();

	/**
	 * Also can use a StaxDriver instead of Dom
	 */
	private TrialXML(){
		super();
		xstream.alias(SpatialCorrection.class.getSimpleName(), SpatialCorrection.class);
		xstream.alias(Grm.class.getSimpleName(), Grm.class);
		xstream.alias(OutputExcel.class.getSimpleName(), OutputExcel.class);
		xstream.alias(OutputVat.class.getSimpleName(), OutputVat.class);
		xstream.alias(OutputOutlier.class.getSimpleName(), OutputOutlier.class);
		xstream.alias(OutputHeatmap.class.getSimpleName(), OutputHeatmap.class);
		xstream.alias(OutputDeleteWorkingDir.class.getSimpleName(), OutputDeleteWorkingDir.class);
		xstream.alias(RetrieveDataDBTrial.class.getSimpleName(), RetrieveDataDBTrial.class);
		xstream.alias(SQLBuilder.class.getSimpleName(), SQLBuilder.class);
		xstream.alias(AsremlModelBlup.class.getSimpleName(), AsremlModelBlup.class);
		xstream.alias(GrmType.class.getSimpleName(), GrmType.class);
		xstream.alias(NullColumns.class.getSimpleName(), NullColumns.class);
		xstream.alias(HybridPerBlock.class.getSimpleName(), HybridPerBlock.class);
		xstream.alias(Insilico.class.getSimpleName(), Insilico.class);
		xstream.alias(StdDevOutliers.class.getSimpleName(), StdDevOutliers.class);
		xstream.useAttributeFor(MatrixOptions.class, "iterative");
		xstream.aliasAttribute(MatrixOptions.class, "iterative", "Iterative");
		xstream.aliasAttribute(MatrixOptions.class, "startWeight", "StartWeight");
		xstream.aliasAttribute(MatrixOptions.class, "removeNegative", "RemoveNegative");
		xstream.aliasAttribute(MatrixOptions.class, "thresholdWeight", "ThresholdWeight");
		xstream.aliasAttribute(MatrixOptions.class, "thresholdCondition", "ThresholdCondition");
		xstream.useAttributeFor(MatrixOptions.class, "DevMode");
		xstream.aliasField("traitName",data.xml.objects.Trait.class, "name");
		
		// Changes for use with modified XML suitable for the Xonomy XML editor
		xstream.aliasField("asremlColumns", AsremlColumns.class, "columns");
		xstream.aliasField("summaryType", Summary.class, "type");
		xstream.addImplicitCollection(Grm.class, "idColumns", "idColumn", String.class);
	}
}