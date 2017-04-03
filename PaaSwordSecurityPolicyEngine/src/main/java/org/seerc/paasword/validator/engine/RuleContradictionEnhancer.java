package org.seerc.paasword.validator.engine;

import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class RuleContradictionEnhancer implements JenaModelEnhancer {

	// The data source
	protected JenaDataSourceInferred jdsi;

	public RuleContradictionEnhancer(JenaDataSourceInferred jdsi)
	{
		this.jdsi = jdsi;
	}
		
	@Override
	public void enhanceModel()
	{
		// get all ABAC rules
		List<Individual> abacRuleIndividuals = ((OntModel)jdsi.getModel()).listIndividuals(((OntModel)jdsi.getModel()).createClass("http://www.paasword.eu/security-policy/seerc/pac#ABACRule")).toList();
		
		// for each ABAC rule
		for(int i=0;i<abacRuleIndividuals.size()-1;i++)
		{
			Individual abacRule = abacRuleIndividuals.get(i);
			for(int j=i+1;j<abacRuleIndividuals.size();j++)
			{
				Individual secondRule = abacRuleIndividuals.get(j);
				if(this.rulesContradict(abacRule, secondRule))
				{	// add the contradicts property
					((OntModel)jdsi.getModel()).createStatement(abacRule, this.jdsi.getModel().createProperty("http://www.paasword.eu/security-policy/seerc/pac#contradicts"), secondRule);
				}
			}
		}
	}

	private boolean rulesContradict(Individual rule1, Individual rule2)
	{
		// First check if they have same authorisation
		if(this.rulesHaveEqualParameter(rule1, rule2, "http://www.paasword.eu/security-policy/seerc/pac#hasAuthorisation"))
		{	// if they do, we do not have contradiction
			return false;
		}
		
		// different authorisation, potential contradiction
		
		// Now compare each rule parameter, if they have one different, they do not contradict
		if(
			!this.rulesHaveEqualParameter(rule1, rule2, "http://www.paasword.eu/security-policy/seerc/pac#hasControlledObject") ||
			!this.rulesHaveEqualParameter(rule1, rule2, "http://www.paasword.eu/security-policy/seerc/pac#hasAction") ||
			!this.rulesHaveEqualParameter(rule1, rule2, "http://www.paasword.eu/security-policy/seerc/pac#hasActor") ||
			!this.rulesHaveEqualParameter(rule1, rule2, "http://www.paasword.eu/security-policy/seerc/pac#hasContextExpression")
		)
		{
			return false;
		}
		
		return true;
	}

	private boolean rulesHaveEqualParameter(Individual rule1, Individual rule2, String parameter) {
		RDFNode rule1Parameter = rule1.getPropertyValue(this.jdsi.getModel().createProperty(parameter));
		RDFNode rule2Parameter = rule2.getPropertyValue(this.jdsi.getModel().createProperty(parameter));

		if(rule1Parameter == null && rule2Parameter == null)
		{
			return true;
		}
		
		if(rule1Parameter != null && rule1Parameter.equals(rule2Parameter))
		{
			return true;
		}
		
		return false;
	}

}
