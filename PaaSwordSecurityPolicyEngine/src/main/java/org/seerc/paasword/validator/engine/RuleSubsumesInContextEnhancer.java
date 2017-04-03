package org.seerc.paasword.validator.engine;

import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class RuleSubsumesInContextEnhancer implements JenaModelEnhancer {

	// The data source
	protected JenaDataSourceInferred jdsi;

	public RuleSubsumesInContextEnhancer(JenaDataSourceInferred jdsi)
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
				if(this.ruleSubsumesInContext(abacRule, secondRule))
				{	// add the subsumesInContext property
					((OntModel)jdsi.getModel()).add(abacRule, this.jdsi.getModel().createProperty("http://www.paasword.eu/security-policy/seerc/pac#subsumesInContext"), secondRule);
				}
			}
		}
	}

	private boolean ruleSubsumesInContext(Individual rule1, Individual rule2)
	{
		// First get their prioritiesInContext(s)
		StmtIterator rule1PICs = ((OntModel)jdsi.getModel()).listStatements(rule1, this.jdsi.getModel().createProperty("http://www.paasword.eu/security-policy/seerc/list#hasPriorityInContext"), (RDFNode)null);
		StmtIterator rule2PICs = ((OntModel)jdsi.getModel()).listStatements(rule2, this.jdsi.getModel().createProperty("http://www.paasword.eu/security-policy/seerc/list#hasPriorityInContext"), (RDFNode)null);

		// iterate over them
		while(rule1PICs.hasNext())
		{
			Individual rule1PIC = rule1PICs.next().getObject().as(Individual.class);
			while(rule2PICs.hasNext())
			{
				Individual rule2PIC = rule2PICs.next().getObject().as(Individual.class);
				// if they have the same context
				if(this.entitiesHaveEqualParameter(rule1PIC, rule2PIC, "http://www.paasword.eu/security-policy/seerc/list#InContext"))
				{
					// if rule2's priority is >= rule1's priority
					int rule2Priority = rule2PIC.getPropertyValue(this.jdsi.getModel().createProperty("http://www.paasword.eu/security-policy/seerc/list#hasPriority")).asLiteral().getInt();
					int rule1Priority = rule1PIC.getPropertyValue(this.jdsi.getModel().createProperty("http://www.paasword.eu/security-policy/seerc/list#hasPriority")).asLiteral().getInt();
					if(rule2Priority >= rule1Priority)
					{
						// if antecedent of rule1 subsumes antecedent of rule2 -> antecedent of rule2 subclassOf antecedent of rule1  
						OntClass rule1Antecedent = rule1.getPropertyValue(this.jdsi.getModel().createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasAntecedent")).as(OntClass.class);
						OntClass rule2Antecedent = rule2.getPropertyValue(this.jdsi.getModel().createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasAntecedent")).as(OntClass.class);
						if(rule1Antecedent.hasSubClass(rule2Antecedent))
						{
							// they are subsumed in context
							return true;
						}
					}
				}
			}
		}
		
		// they haven't been found subsumed in context
		return false;
	}

	private boolean entitiesHaveEqualParameter(Individual rule1, Individual rule2, String parameter) {
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
