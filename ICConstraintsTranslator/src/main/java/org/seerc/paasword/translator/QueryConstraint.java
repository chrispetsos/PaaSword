package org.seerc.paasword.translator;

/**
 * Data class that holds results of axiom-to-query conversions.
 * It holds the OWL form of the restriction and the converted SPARQL query.
 * 
 * @author Chris Petsos
 *
 */
public class QueryConstraint {

	// This will hold the OWL representation of the restriction.
	String constraintStatements;
	// This will hold the converted SPARQL query.
	String query;
	// This will hold the human-friendly constraint description as added to its rdfs:label.
	String constraintDescription;
	// This will hold the constraint level (error, warning etc.).
	String constraintLevel;
	
	public QueryConstraint(String statements, String query, String constraintDescription, String constraintLevel)
	{
		this.constraintStatements = statements;
		this.query = query;
		this.constraintDescription = constraintDescription;
		this.constraintLevel = constraintLevel;
	}

	public String getConstraintStatements() {
		return constraintStatements;
	}

	public String getQuery() {
		return query;
	}

	public String getConstraintDescription() {
		return constraintDescription;
	}

	public String getConstraintLevel() {
		return constraintLevel;
	}

}
