package org.seerc.paasword.validator.engine;

import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;

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
			this.jdsi.getModel().add(abacRule, this.jdsi.getModel().createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasAntecedent"), ((OntModel)this.jdsi.getModel()).createIndividual(abacRule.toString() + "Antecedent", this.jdsi.getModel().createResource("http://www.paasword.eu/security-policy/seerc/pac#RuleAntecedent")));
			// create conclusion
			this.jdsi.getModel().add(abacRule, this.jdsi.getModel().createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasConclusion"), ((OntModel)this.jdsi.getModel()).createIndividual(abacRule.toString() + "Conclusion", this.jdsi.getModel().createResource("http://www.paasword.eu/security-policy/seerc/pac#RuleConclusion")));
		}
	}

}
