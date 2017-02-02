package org.seerc.paasword.validator.engine;

public class SubclassSubsumptionsEngine extends EntitySubsumptionBaseEngine {

	public SubclassSubsumptionsEngine(JenaDataSourceInferred jdsi) {
		super(jdsi);
	}

	@Override
	protected boolean entitySubsumes(String entity1Uri, String entity2Uri) {
		return false;
	}

	@Override
	protected void addSubsumption(String entity1Uri, String entity2Uri) {
		
	}

}
