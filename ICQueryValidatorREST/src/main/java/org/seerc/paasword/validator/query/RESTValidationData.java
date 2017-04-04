package org.seerc.paasword.validator.query;

/**
 * Data class that holds data to be validated that are transmitted over REST.
 * 
 * @author Chris Petsos
 *
 */
public class RESTValidationData {

	private String constraints;
	public String getConstraints() {
		return constraints;
	}
	public void setConstraints(String constraints) {
		this.constraints = constraints;
	}
	private String[] ontologies;
	public String[] getOntologies() {
		return ontologies;
	}
	public void setOntologies(String[] ontologies) {
		this.ontologies = ontologies;
	}
}
