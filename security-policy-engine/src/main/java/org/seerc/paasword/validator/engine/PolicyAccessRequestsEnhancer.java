package org.seerc.paasword.validator.engine;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.ComplementClass;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class PolicyAccessRequestsEnhancer implements JenaModelEnhancer {

	// The data source
	protected JenaDataSourceInferred jdsi;

	public PolicyAccessRequestsEnhancer(JenaDataSourceInferred jdsi)
	{
		this.jdsi = jdsi;
	}

	@Override
	public void enhanceModel() {
		// Get all Policies
		ExtendedIterator<Individual> abacPolicies = ((OntModel)jdsi.getModel()).listIndividuals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#ABACPolicy"));
		
		while(abacPolicies.hasNext())
		{
			Individual policy = abacPolicies.next();
			
			OntClass policyAccessRequestsPositive = this.createAccessRequestsClassForConsequent(policy, ((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#positive"));
			((OntModel)jdsi.getModel()).createIndividual(policyAccessRequestsPositive.getURI(), ((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#AccessRequestClassFor_positive"));
			((OntModel)jdsi.getModel()).add(policy, ((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#hasAccessRequestClassFor_positive"), policyAccessRequestsPositive);
			
			OntClass policyAccessRequestsNegative = this.createAccessRequestsClassForConsequent(policy, ((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#negative"));
			((OntModel)jdsi.getModel()).createIndividual(policyAccessRequestsNegative.getURI(), ((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#AccessRequestClassFor_negative"));
			((OntModel)jdsi.getModel()).add(policy, ((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#hasAccessRequestClassFor_negative"), policyAccessRequestsNegative);
		}
	}

	// R P,x ≡ ⨆ { r ∈ R | con(r) = x } (pre(r) ⊓ ¬⨆ { r′ ∈ R | con(r′) ≠ x ∧ r < r′ } pre(r′ ))
	private OntClass createAccessRequestsClassForConsequent(Individual policy, Resource consequent)
	{
		// Get all Rules of Policy
		List<RDFNode> policyRules = policy.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pwd#hasRule")).toList();
		List<RDFNode> preRIntersectionComplementUnionPreR2List = new ArrayList<RDFNode>();
		for(RDFNode policyRuleNode:policyRules)
		{
			Individual r = policyRuleNode.as(Individual.class);
			
			// find priority of r in context of the policy
			RDFNode rPriority = this.findPriorityInContext(policy, r);
			
			// find authorisation of rule
			Individual authorisationR = r.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasAuthorisation")).next().as(Individual.class);
			if(authorisationR.equals(consequent))
			{	// con(r) = x
				// get antecedent of rule - pre(r)
				RDFNode preR = r.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasAntecedent")).next();
				// Find rules r' that have different consequent and have smaller priority in the same context
				List<RDFNode> r2List = new ArrayList<RDFNode>();
				for(RDFNode r2Node:policyRules)
				{
					Individual r2 = r2Node.as(Individual.class);
					Individual authorisationR2 = r2.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasAuthorisation")).next().as(Individual.class);
					if(!authorisationR2.equals(consequent))
					{	// con(r′) ≠ x
						// find priority of r2 in context of the policy
						RDFNode r2Priority = this.findPriorityInContext(policy, r2);
						if(r2Priority != null && rPriority != null && Integer.valueOf(r2Priority.asLiteral().getValue().toString()) < Integer.valueOf(rPriority.asLiteral().getValue().toString()))
						{	// r < r′
							// get antecedent of r2 - pre(r′)
							RDFNode preR2 = r2.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasAntecedent")).next();
							r2List.add(preR2);
						}
					}
				}
				// create the union of r2List - ⨆ { r′ ∈ R | con(r′) ≠ x ∧ r < r′ } pre(r′ )
				UnionClass r2Union = ((OntModel)jdsi.getModel()).createUnionClass(null, ((OntModel)this.jdsi.getModel()).createList(r2List.iterator()));
				
				// create the complement of the union - ¬⨆ { r′ ∈ R | con(r′) ≠ x ∧ r < r′ } pre(r′ )
				ComplementClass r2ComplementUnion = ((OntModel)jdsi.getModel()).createComplementClass(null, r2Union);
				
				// create the intersection of pre(r) with the complement of the union - pre(r) ⊓ ¬⨆ { r′ ∈ R | con(r′) ≠ x ∧ r < r′ } pre(r′ )
				List<RDFNode> preRIntersectionList = new ArrayList<RDFNode>();
				preRIntersectionList.add(preR);
				preRIntersectionList.add(r2ComplementUnion);
				IntersectionClass preRIntersectionComplementUnionPreR2 = ((OntModel)jdsi.getModel()).createIntersectionClass(null, ((OntModel)this.jdsi.getModel()).createList(preRIntersectionList.iterator()));
				preRIntersectionComplementUnionPreR2List.add(preRIntersectionComplementUnionPreR2);
			}
		}
		
		// return the union of all preRIntersectionComplementUnionPreR2s - ⨆ { r ∈ R | con(r) = x } (pre(r) ⊓ ¬⨆ { r′ ∈ R | con(r′) ≠ x ∧ r < r′ } pre(r′ ))
		return ((OntModel)jdsi.getModel()).createUnionClass("http://www.paasword.eu/security-policy/seerc/pac#" + policy.getLocalName() + "AccessRequestClassFor_" + consequent.getLocalName(), ((OntModel)this.jdsi.getModel()).createList(preRIntersectionComplementUnionPreR2List.iterator()));
	}

	private RDFNode findPriorityInContext(Individual context, Individual r) {
		// first get all priorities in all contexts
		NodeIterator rPrioritiesInContexts = r.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/list#hasPriorityInContext"));
		while(rPrioritiesInContexts.hasNext())
		{
			Individual rPriorityInContext = rPrioritiesInContexts.next().as(Individual.class);
			// get its context
			Individual rInContext = rPriorityInContext.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/list#inContext")).next().as(Individual.class);
			if(rInContext.equals(context))
			{	// we have the correct context, return the priority
				return rPriorityInContext.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/list#hasPriority")).next();
			}
		}
		// couldn't find priority in context
		return null;
	}
}
