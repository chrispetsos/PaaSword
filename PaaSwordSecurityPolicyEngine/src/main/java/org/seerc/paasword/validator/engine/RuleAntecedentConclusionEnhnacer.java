package org.seerc.paasword.validator.engine;

import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class RuleAntecedentConclusionEnhnacer implements JenaModelEnhancer {

	// The data source
	protected JenaDataSourceInferred jdsi;

	public RuleAntecedentConclusionEnhnacer(JenaDataSourceInferred jdsi)
	{
		this.jdsi = jdsi;
	}
		
	@Override
	public void enhanceModel()
	{
		// get all ABAC rules
		List<Individual> abacRuleIndividuals = ((OntModel)jdsi.getModel()).listIndividuals(((OntModel)jdsi.getModel()).createClass("http://www.paasword.eu/security-policy/seerc/pac#ABACRule")).toList();
		
		// for each ABAC rule
		for(Individual abacRule:abacRuleIndividuals)
		{
			// create antecedent
			((OntModel)this.jdsi.getModel()).createClass(abacRule.toString() + "Antecedent");
			Individual ruleAntecedent = ((OntModel)this.jdsi.getModel()).createIndividual(abacRule.toString() + "Antecedent", this.jdsi.getModel().createResource("http://www.paasword.eu/security-policy/seerc/pac#RuleAntecedent"));
			this.jdsi.getModel().add(abacRule, this.jdsi.getModel().createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasAntecedent"), ruleAntecedent);
			this.copyParameter(abacRule, ruleAntecedent, "http://www.paasword.eu/security-policy/seerc/pac#hasControlledObject");
			this.copyParameter(abacRule, ruleAntecedent, "http://www.paasword.eu/security-policy/seerc/pac#hasAction");
			this.copyParameter(abacRule, ruleAntecedent, "http://www.paasword.eu/security-policy/seerc/pac#hasActor");
			this.copyParameter(abacRule, ruleAntecedent, "http://www.paasword.eu/security-policy/seerc/pac#hasContextExpression");
			
			// create conclusion
			((OntModel)this.jdsi.getModel()).createClass(abacRule.toString() + "Conclusion");
			Individual ruleConclusion = ((OntModel)this.jdsi.getModel()).createIndividual(abacRule.toString() + "Conclusion", this.jdsi.getModel().createResource("http://www.paasword.eu/security-policy/seerc/pac#RuleConclusion"));
			this.jdsi.getModel().add(abacRule, this.jdsi.getModel().createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasConclusion"), ruleConclusion);
			this.copyParameter(abacRule, ruleConclusion, "http://www.paasword.eu/security-policy/seerc/pac#hasAuthorisation");
		}
	}

	private void copyParameter(Individual source, Individual target, String parameter)
	{
		Property property = ((OntModel)this.jdsi.getModel()).createProperty(parameter);
		RDFNode sourcePropertyValue = source.getPropertyValue(property);
		if(sourcePropertyValue != null)
		{
			((OntModel)this.jdsi.getModel()).add(target, property, sourcePropertyValue);
		}
	}

}
