package org.seerc.paasword.validator.engine;

public class PolicyRulesOrderEnhancer implements JenaModelEnhancer {

	// The data source
	protected JenaDataSourceInferred jdsi;

	public PolicyRulesOrderEnhancer(JenaDataSourceInferred jdsi)
	{
		this.jdsi = jdsi;
	}

	@Override
	public void enhanceModel() {
		// TODO Auto-generated method stub
		
	}

}
