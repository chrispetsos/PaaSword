package org.seerc.paasword.validator.engine;

public class RuleAntecedentConclusionEnhnacer implements JenaModelEnhancer {

	// The data source
	protected JenaDataSourceInferred jdsi;

	public RuleAntecedentConclusionEnhnacer(JenaDataSourceInferred jdsi)
	{
		this.jdsi = jdsi;
	}
		
	@Override
	public void enhanceModel() {
	}

}
