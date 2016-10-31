package org.seerc.paasword.validator.query;

import java.util.List;

import org.seerc.paasword.translator.QueryConstraint;

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
