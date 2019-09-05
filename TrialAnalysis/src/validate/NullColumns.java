package validate;

import java.util.ArrayList;
import java.util.Collection;

import asreml.input.AsremlColumn;
import asreml.input.AsremlColumns;
import data.collection.ExpFBKs;
import data.xml.objects.Trial;
import error.ErrorMessage;

/**
 * Check each trial's asreml columns for any null data.  If one is null throw and error and stop analysis.
 * 
 * @author Scott Smith
 *
 */
public class NullColumns implements Validate {

	public int validate(Trial trial) throws Exception {
		ExpFBKs fbks = trial.getFbks();
		AsremlColumns columns = trial.getColumns();
		
		for(AsremlColumn asremlColumn : columns.getColumns()){
			//Coefficients are calculated and not stored in normal data
			if(asremlColumn.isCoeff()){
				continue;
			}
			String columnName = asremlColumn.getName();
			Collection<String> logColumns = new ArrayList<>();
			if(!columnName.equals("locationName")){
				logColumns.add("locationName");
			}
			boolean hasNull = fbks.hasNull(columnName, logColumns);
			
			if(hasNull){
				throw new Exception(ErrorMessage.INSTANCE.getMessage("has_nulls") + " - " + columnName);
			}
		}
		return 0;
	}

}
