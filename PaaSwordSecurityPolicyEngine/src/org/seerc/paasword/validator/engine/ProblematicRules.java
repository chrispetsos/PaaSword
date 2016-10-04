package org.seerc.paasword.validator.engine;

public class ProblematicRules {

	String reason;
	String rule1;
	String rule2;
	
	public ProblematicRules(String reason, String rule1, String rule2)
	{
		this.reason = reason;
		this.rule1 = rule1;
		this.rule2 = rule2;
	}
	
	public String toString()
	{
		return reason + ": " + rule1 + ", " + rule2;
	}
}
