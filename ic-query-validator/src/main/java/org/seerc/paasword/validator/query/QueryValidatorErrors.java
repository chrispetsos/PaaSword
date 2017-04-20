package org.seerc.paasword.validator.query;

import java.util.List;

import org.seerc.paasword.translator.QueryConstraint;

/**
 * Data class that holds the validation errors. Holds the QueryConstraint and a 
 * list of problematic resources related to the violation.
 * 
 * @author Chris Petsos
 *
 */

// TODO: Remove the plural from here!
public class QueryValidatorErrors {

	QueryConstraint queryConstraint;
	List<String> problematicResources;
	
	public QueryValidatorErrors(QueryConstraint queryConstraint, List<String> problematicResources)
	{
		this.queryConstraint = queryConstraint;
		this.problematicResources = problematicResources;
	}

	public QueryConstraint getQueryConstraint() {
		return queryConstraint;
	}

	public List<String> getProblematicResources() {
		return problematicResources;
	}

}
