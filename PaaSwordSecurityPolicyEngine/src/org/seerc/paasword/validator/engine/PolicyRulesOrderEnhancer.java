package org.seerc.paasword.validator.engine;

import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class PolicyRulesOrderEnhancer implements JenaModelEnhancer {

	// The data source
	protected JenaDataSourceInferred jdsi;

	public PolicyRulesOrderEnhancer(JenaDataSourceInferred jdsi)
	{
		this.jdsi = jdsi;
	}

/*	
	list:PriorityInContext a owl:Class .

	list:hasPriority a owl:DatatypeProperty .
	list:inContext a owl:ObjectProperty .

	list:hasPriorityInContext a owl:ObjectProperty .
	
*/	
	@Override
	public void enhanceModel() {
		// Get all Policies
		ExtendedIterator<Individual> abacPolicies = ((OntModel)jdsi.getModel()).listIndividuals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#ABACPolicy"));
		
		while(abacPolicies.hasNext())
		{
			Individual policy = abacPolicies.next();
			
			// get combining algorithm of policy
			List<RDFNode> policyCAList = policy.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasPolicyCombiningAlgorithm")).toList();
			if(policyCAList.isEmpty())
			{	// no combining algorithm, cannot do anything
				continue;
			}
			
			// Policies must have exactly one CA and it must be an Individual
			Individual policyCA = null;
			try
			{
				policyCA = policyCAList.get(0).as(Individual.class);
			}
			catch(Exception e)
			{
				continue;
			}
			
			// Get all Rules of Policy
			NodeIterator policyRules = policy.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasABACRule"));
			while(policyRules.hasNext())
			{
				Individual policyRule = policyRules.next().as(Individual.class);
				// get authorisation of rule
				List<RDFNode> ruleAuthorisationList = policyRule.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasAuthorisation")).toList();
				if(ruleAuthorisationList.isEmpty())
				{	// no authorisation in rule
					continue;
				}
				
				// should have exactly one authorisation
				Individual ruleAuthorisation = ruleAuthorisationList.get(0).as(Individual.class);
				
				if(ruleAuthorisation.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#positive")))
				{	// positive authorisation
					if(policyCA.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#permitOverrides")))
					{	// permit overrides
						this.createPriorityInContext(policyRule, 1, policy);						
					}
					else if(policyCA.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#denyOverrides")))
					{	// deny overrides
						this.createPriorityInContext(policyRule, 2, policy);												
					}
				}
				else if(ruleAuthorisation.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#negative")))
				{	// negative authorisation
					if(policyCA.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#permitOverrides")))
					{	// permit overrides
						this.createPriorityInContext(policyRule, 2, policy);						
					}
					else if(policyCA.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#denyOverrides")))
					{	// deny overrides
						this.createPriorityInContext(policyRule, 1, policy);												
					}
				}
			}
			
			// if the combining algorithm is permitUnlessDeny or denyUnlessPermit,
			// add a special rule at its end which subsumes everything above
			if(policyCA.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#permitUnlessDeny")))
			{
				// get special rule
				Individual specialRuleForPermitUnlessDeny = ((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#specialRuleForPermitUnlessDeny").as(Individual.class);
				// add it to policy
				((OntModel)jdsi.getModel()).add(policy, ((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pwd#hasRule"), specialRuleForPermitUnlessDeny);
				// add it with last priority in policy
				this.createPriorityInContext(specialRuleForPermitUnlessDeny, 1000, policy);												
			}
			else if(policyCA.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#denyUnlessPermit")))
			{
				// get special rule
				Individual specialRuleForDenyUnlessPermit = ((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#specialRuleForDenyUnlessPermit").as(Individual.class);
				// add it to policy
				((OntModel)jdsi.getModel()).add(policy, ((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pwd#hasRule"), specialRuleForDenyUnlessPermit);
				// add it with last priority in policy
				this.createPriorityInContext(specialRuleForDenyUnlessPermit, 1000, policy);												
			}
		}
	}

	private void createPriorityInContext(Individual policyRule, int priority, Individual context)
	{
		// create the PriorityInContext individual
		Individual priorityInContextIndividual = ((OntModel)jdsi.getModel()).createIndividual("http://www.paasword.eu/security-policy/seerc/list#PriorityFor_" + policyRule.getLocalName() + "_InContext_" + context.getLocalName(), ((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/list#PriorityInContext"));
		// set its priority 
		this.jdsi.getModel().add(priorityInContextIndividual, this.jdsi.getModel().createProperty("http://www.paasword.eu/security-policy/seerc/list#hasPriority"), String.valueOf(priority));
		// set its context 
		this.jdsi.getModel().add(priorityInContextIndividual, this.jdsi.getModel().createProperty("http://www.paasword.eu/security-policy/seerc/list#inContext"), context);
		
		// add the PriorityInContext to the rule
		this.jdsi.getModel().add(policyRule, this.jdsi.getModel().createProperty("http://www.paasword.eu/security-policy/seerc/list#hasPriorityInContext"), priorityInContextIndividual);
	}

}
