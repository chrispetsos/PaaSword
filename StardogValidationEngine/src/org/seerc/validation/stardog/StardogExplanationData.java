package org.seerc.validation.stardog;

public class StardogExplanationData {

	private String statement;
	public String getStatement() {
		return statement;
	}
	public void setStatement(String statement) {
		this.statement = statement;
	}
	private String[] ontologies;
	public String[] getOntologies() {
		return ontologies;
	}
	public void setOntologies(String[] ontologies) {
		this.ontologies = ontologies;
	}
}
