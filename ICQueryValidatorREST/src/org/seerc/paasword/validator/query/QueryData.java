package org.seerc.paasword.validator.query;

/**
 * Data class that holds data to be queried that are transmitted over REST.
 * 
 * @author Chris Petsos
 *
 */
public class QueryData {

	private String query;
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	private String[] ontologies;
	public String[] getOntologies() {
		return ontologies;
	}
	public void setOntologies(String[] ontologies) {
		this.ontologies = ontologies;
	}
}
