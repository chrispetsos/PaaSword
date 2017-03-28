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
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class PolicySetAccessRequestsEnhancer implements JenaModelEnhancer {

	// The data source
	protected JenaDataSourceInferred jdsi;

	public PolicySetAccessRequestsEnhancer(JenaDataSourceInferred jdsi)
	{
		this.jdsi = jdsi;
	}

	@Override
	public void enhanceModel() {
		// Get all Policies
		ExtendedIterator<Individual> abacPolicySets = ((OntModel)jdsi.getModel()).listIndividuals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#ABACPolicySet"));
		
		while(abacPolicySets.hasNext())
		{
			Individual policySet = abacPolicySets.next();
			
			OntClass policySetAccessRequestsPositive = this.createAccessRequestsClassForConsequent(policySet, ((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#positive"));
			((OntModel)jdsi.getModel()).createIndividual(policySetAccessRequestsPositive.getURI(), ((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#AccessRequestClassFor_positive"));
			((OntModel)jdsi.getModel()).add(policySet, ((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#hasAccessRequestClassFor_positive"), policySetAccessRequestsPositive);
			
			OntClass policySetAccessRequestsNegative = this.createAccessRequestsClassForConsequent(policySet, ((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#negative"));
			((OntModel)jdsi.getModel()).createIndividual(policySetAccessRequestsNegative.getURI(), ((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#AccessRequestClassFor_negative"));
			((OntModel)jdsi.getModel()).add(policySet, ((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#hasAccessRequestClassFor_negative"), policySetAccessRequestsNegative);
		}
	}

	private OntClass createAccessRequestsClassForConsequent(Individual policySet, Resource consequent)
	{
		// find combining algorithm of policy set
		List<RDFNode> policySetCAList = policySet.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasPolicySetCombiningAlgorithm")).toList();
		if(policySetCAList.isEmpty())
		{	// no combining algorithm, cannot do anything
			throw new RuntimeException("Policy set " + policySet.toString() + " does not have combining algorithm attached.");
		}
		
		// Policy Sets must have exactly one CA and it must be an Individual
		Individual policySetCA = null;
		try
		{
			policySetCA = policySetCAList.get(0).as(Individual.class);
		}
		catch(Exception e)
		{
			throw new RuntimeException("Policy set " + policySet.toString() + " must have exactly one valid combining algorithm attached.");
		}
		
		// find policies that belong to this policy set
		StmtIterator abacPolicies = ((OntModel)jdsi.getModel()).listStatements(null, ((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pac#belongsToABACPolicySet"), policySet);
		return this.createAccessRequestsClassFor(policySet, policySetCA, abacPolicies, consequent);
		/*while(abacPolicies.hasNext())
		{
			Individual abacPolicy = abacPolicies.next().getSubject().as(Individual.class);
			if(consequent.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#positive")))
			{	// positive consequent
				if(policySetCA.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#permitOverrides")))
				{	// permit overrides
					this.createAccessRequestsClassFor(abacPolicy, 1, policy);						
				}
			}
			else if(consequent.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#negative")))
			{	// negative consequent
				if(policySetCA.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#permitOverrides")))
				{	// permit overrides
					this.createPriorityInContext(abacPolicy, 2, policy);						
				}
			}
		}*/
		
		// return null;
		
		/*
		// Get all Rules of Policy
		List<RDFNode> policyRules = policySet.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pwd#hasRule")).toList();
		List<RDFNode> preRIntersectionComplementUnionPreR2List = new ArrayList<RDFNode>();
		for(RDFNode policyRuleNode:policyRules)
		{
			Individual r = policyRuleNode.as(Individual.class);
			
			// find priority of r in context of the policy
			RDFNode rPriority = this.findPriorityInContext(policySet, r);
			
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
						RDFNode r2Priority = this.findPriorityInContext(policySet, r2);
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
		return ((OntModel)jdsi.getModel()).createUnionClass("http://www.paasword.eu/security-policy/seerc/pac#" + policySet.getLocalName() + "AccessRequestClassFor_" + consequent.getLocalName(), ((OntModel)this.jdsi.getModel()).createList(preRIntersectionComplementUnionPreR2List.iterator()));*/
	}

	private OntClass createAccessRequestsClassFor(Individual policySet, Individual policySetCA, StmtIterator abacPolicies, Resource consequent)
	{
		if(policySetCA.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#permitOverrides")))
		{
			return this.createAccessRequestsClassForPermitOverrides(policySet, abacPolicies, consequent);
		}
		
		throw new RuntimeException("Unknown combining algorithm " + policySetCA.toString());
	}

	private OntClass createAccessRequestsClassForPermitOverrides(Individual policySet, StmtIterator abacPolicies, Resource consequent)
	{
		if(consequent.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#positive")))
		{	// positive consequent - R PS PO ,Permit ≡ ⨆ i≤n R i,Permit
			OntClass result = ((OntModel)jdsi.getModel()).createClass("http://www.paasword.eu/security-policy/seerc/pac#" + policySet.getLocalName() + "AccessRequestClassFor_positive");
			result.addEquivalentClass(this.createUnionOfPermitPolicyAccessRequests(policySet, abacPolicies));
			return result;
			
		}
		else
		{	// negative consequent - R PS PO ,Deny ≡ (⨆ i≤n R i,Deny ) ⨅ ¬R PS PO ,Permit
			OntClass unionOfPermitPolicyAccessRequests = this.createUnionOfPermitPolicyAccessRequests(policySet, abacPolicies);
			OntClass unionOfDenyPolicyAccessRequests = this.createUnionOfDenyPolicyAccessRequests(policySet, abacPolicies);
			List<RDFNode> policyAccessRequestsForNegativeList = new ArrayList<RDFNode>();
			policyAccessRequestsForNegativeList.add(unionOfDenyPolicyAccessRequests);
			policyAccessRequestsForNegativeList.add(((OntModel)jdsi.getModel()).createComplementClass(null, unionOfPermitPolicyAccessRequests));

			OntClass result = ((OntModel)jdsi.getModel()).createClass("http://www.paasword.eu/security-policy/seerc/pac#" + policySet.getLocalName() + "AccessRequestClassFor_negative");
			result.addEquivalentClass(((OntModel)jdsi.getModel()).createIntersectionClass(null, ((OntModel)this.jdsi.getModel()).createList(policyAccessRequestsForNegativeList.iterator())));
			
			return result;
		}
	}

	private OntClass createUnionOfPermitPolicyAccessRequests(Individual policySet, StmtIterator abacPolicies)
	{
		List<RDFNode> policyAccessRequestsForPositiveList = new ArrayList<RDFNode>();
		while(abacPolicies.hasNext())
		{
			Individual abacPolicy = abacPolicies.next().getSubject().as(Individual.class);
			OntClass policyAccessRequestsForPositive = abacPolicy.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#hasAccessRequestClassFor_positive")).toList().get(0).as(OntClass.class);
			policyAccessRequestsForPositiveList.add(policyAccessRequestsForPositive);
		}
		return ((OntModel)jdsi.getModel()).createUnionClass(null, ((OntModel)this.jdsi.getModel()).createList(policyAccessRequestsForPositiveList.iterator()));
	}

	private OntClass createUnionOfDenyPolicyAccessRequests(Individual policySet, StmtIterator abacPolicies)
	{
		List<RDFNode> policyAccessRequestsForNegativeList = new ArrayList<RDFNode>();
		while(abacPolicies.hasNext())
		{
			Individual abacPolicy = abacPolicies.next().getSubject().as(Individual.class);
			OntClass policyAccessRequestsForNegative = abacPolicy.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#hasAccessRequestClassFor_negative")).toList().get(0).as(OntClass.class);
			policyAccessRequestsForNegativeList.add(policyAccessRequestsForNegative);
		}
		return ((OntModel)jdsi.getModel()).createUnionClass("http://www.paasword.eu/security-policy/seerc/pac#" + policySet.getLocalName() + "AccessRequestClassFor_negative", ((OntModel)this.jdsi.getModel()).createList(policyAccessRequestsForNegativeList.iterator()));
	}

	/*private RDFNode findPriorityInContext(Individual context, Individual r) {
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
	}*/
}
