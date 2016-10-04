package org.seerc.paasword.validator.engine;

import java.util.List;

public class ValidationReport {

	private List<ProblematicRules> ruleContradictions;
	private List<String> policyContradictions;
	private List<ProblematicRules> ruleSubsumptions;

	public void setContradictingRules(List<ProblematicRules> ruleContradictions)
	{
		this.ruleContradictions = ruleContradictions;
	}

	public void setContradictingPolicies(List<String> policyContradictions)
	{
		this.policyContradictions = policyContradictions;
	}

	public void setSubsumptiveRules(List<ProblematicRules> ruleSubsumptions)
	{
		this.ruleSubsumptions = ruleSubsumptions;
	}

}
