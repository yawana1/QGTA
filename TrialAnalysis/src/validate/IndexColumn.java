package validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import transformation.Grm;
import transformation.Transformation;
import asreml.input.AsremlColumn;
import asreml.input.AsremlColumns;
import data.xml.objects.Trial;

/**
 * Check each trial's asreml columns that requires a GRM to make sure it's set as indexed.
 * 
 * @author Scott Smith
 *
 */
public class IndexColumn implements Validate {

	/**
	 * If an AsremlColumn in the Trial.xml is set to IndexedColumn = true
	 * check that there is a matching GRM transformation.
	 */
	public int validate(Trial trial) throws Exception {
		AsremlColumns columns = trial.getColumns();
		Collection<String> failedColumnNames = new ArrayList<>();
		
		//if no indexedColumn exists success or if indexedColumn exists then there needs to be a GRM Transformation
		for(AsremlColumn asremlColumn : columns.getColumns()){
			boolean success = false;
			if(asremlColumn.isIndexColumn()){
				List<Transformation> transformations = trial.getTransformations();
				if(transformations != null){
					//check the trial's transformations if ones a GRM check it's id column to see if one matches the AsremlColumn
					for(Transformation transformation : transformations){
						if(transformation instanceof Grm){
							Grm grm = (Grm) transformation;
							
							if(grm.getIdColumns().contains(asremlColumn.getName())){
								success = true;								
							}
						}
					}
				}
			}
			else{
				success = true;
			}
			
			if(!success){
				failedColumnNames.add(asremlColumn.getName());
			}
		}
		
		if(failedColumnNames.size() > 0){
			String error = "AsremlColumn with indexedColumn set to true with no corrisponding GRM : ";
			for(String failedColumnName : failedColumnNames){
				error += " " + failedColumnName;
			}
			throw new Exception(error);
		}
		return 0;
	}
}
