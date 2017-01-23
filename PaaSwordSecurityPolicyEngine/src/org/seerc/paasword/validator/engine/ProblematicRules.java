package org.seerc.paasword.validator.engine;

/**
 * Data class that holds problematic rules. It contains a reason and the two rules
 * that are problematic.
 */
// TODO: Is this deprecated?
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
