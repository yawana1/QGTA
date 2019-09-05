package validate;

import asreml.input.AsremlColumn;
import asreml.input.AsremlColumns;
import data.collection.ExpFBKs;
import data.xml.objects.Trial;
import error.ErrorMessage;

/**
 * Check each trial's phenotype data and see if it has null values.
 * 
 * @author Scott Smith
 *
 */
public class TraitsExist implements Validate {

	public int validate(Trial trial) throws Exception {
		ExpFBKs fbks = trial.getFbks();
		AsremlColumns columns = trial.getColumns();
		
		for(AsremlColumn asremlColumn : columns.getColumns()){
			//Coefficients are calculated and not stored in normal data
			if(asremlColumn.isCoeff()){
				continue;
			}
			String columnName = asremlColumn.getName();
			boolean hasNull = fbks.hasNull(columnName);
			
			if(hasNull){
				throw new Exception(ErrorMessage.INSTANCE.getMessage("has_nulls" ) + columnName);
			}
		}
		return 0;
	}
}