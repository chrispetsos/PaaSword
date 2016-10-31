package org.seerc.paasword.translator;

public class QueryConstraint {

	String constraintStatements;
	String query;
	
	public QueryConstraint(String statements, String query)
	{
		this.constraintStatements = statements;
		this.query = query;
	}

	public String getConstraintStatements() {
		return constraintStatements;
	}

	public String getQuery() {
		return query;
	}

}
