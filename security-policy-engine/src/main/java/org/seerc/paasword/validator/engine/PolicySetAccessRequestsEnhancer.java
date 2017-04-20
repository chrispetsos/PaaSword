package org.seerc.paasword.validator.engine;

import java.util.ArrayList;
import java.util.Iterator;
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
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.rdf.model.impl.StmtIteratorImpl;
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
			((OntModel)jdsi.getModel()).createIndividual(policySetAccessRequestsPositive.getURI(), ((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#PolicySetAccessRequestClassFor_positive"));
			((OntModel)jdsi.getModel()).add(policySet, ((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#hasPolicySetAccessRequestClassFor_positive"), policySetAccessRequestsPositive);
			
			OntClass policySetAccessRequestsNegative = this.createAccessRequestsClassForConsequent(policySet, ((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#negative"));
			((OntModel)jdsi.getModel()).createIndividual(policySetAccessRequestsNegative.getURI(), ((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#PolicySetAccessRequestClassFor_negative"));
			((OntModel)jdsi.getModel()).add(policySet, ((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#hasPolicySetAccessRequestClassFor_negative"), policySetAccessRequestsNegative);
		}
	}

	private OntClass createAccessRequestsClassForConsequent(Individual policySet, Resource consequent)
	{
		// find combining algorithm of policy set
		List<RDFNode> policySetCAList = policySet.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasPolicySetCombiningAlgorithm")).toList();
		if(policySetCAList.isEmpty())
		{	// no combining algorithm, cannot do anything
			return ((OntModel)jdsi.getModel()).createClass(policySet.toString() + "NoCombiningAlgorithmError");
		}
		
		// Policy Sets must have exactly one CA and it must be an Individual
		Individual policySetCA = null;
		try
		{
			policySetCA = policySetCAList.get(0).as(Individual.class);
		}
		catch(Exception e)
		{
			return ((OntModel)jdsi.getModel()).createClass(policySet.toString() + "InvalidCombiningAlgorithmError");
		}
		
		// find policies that belong to this policy set
		StmtIterator abacPolicies = ((OntModel)jdsi.getModel()).listStatements(null, ((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pac#belongsToABACPolicySet"), policySet);
		return this.createAccessRequestsClassFor(policySet, policySetCA, abacPolicies, consequent);
	}

	private OntClass createAccessRequestsClassFor(Individual policySet, Individual policySetCA, ExtendedIterator<Statement> abacPolicies, Resource consequent)
	{
		// if policySetCA is permitUnlessDeny or denyUnlessPermit, add special policies in abacPolicies to be used later
		if(policySetCA.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#denyUnlessPermit")))
		{
			ArrayList<Statement> specialPolicy = new ArrayList<Statement>();
			specialPolicy.add(new StatementImpl(
					((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#specialPolicyForDenyUnlessPermit"), 
					((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pac#belongsToABACPolicySet"), 
					policySet));
			abacPolicies = abacPolicies.andThen(specialPolicy.iterator());
		}
		else if(policySetCA.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#permitUnlessDeny")))
		{
			ArrayList<Statement> specialPolicy = new ArrayList<Statement>();
			specialPolicy.add(new StatementImpl(
					((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#specialPolicyForPermitUnlessDeny"), 
					((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pac#belongsToABACPolicySet"), 
					policySet));
			abacPolicies = abacPolicies.andThen(specialPolicy.iterator());
		}
		
		if(		policySetCA.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#permitOverrides")) ||
				policySetCA.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#denyUnlessPermit")))
		{
			return this.createAccessRequestsClassForPermitOverrides(policySet, abacPolicies, consequent);
		}
		else if(policySetCA.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#denyOverries")) ||
				policySetCA.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#permitUnlessDeny")))
		{
			return this.createAccessRequestsClassForDenyOverrides(policySet, abacPolicies, consequent);
		}
		else if(policySetCA.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#onlyOneApplicable")))
		{
			return this.createAccessRequestsClassForOnlyOneApplicable(policySet, abacPolicies, consequent);
		}
		
		return ((OntModel)jdsi.getModel()).createClass(policySet.toString() + "UnsupportedCombiningAlgorithmError");
	}

	// R PS OOA ,x ≡ ⨆ i≤n (R i,x ⨅ ¬⨆ k≤n ∧ k≠i (R k,Deny ⊔ R k,Permit ))
	private OntClass createAccessRequestsClassForOnlyOneApplicable(Individual policySet, ExtendedIterator<Statement> abacPolicies, Resource consequent) {
		if(consequent.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#positive")))
		{	// positive consequent
			return this.createAccessRequestsClassForOnlyOneApplicablePositive(policySet, abacPolicies);
		}
		else
		{	// negative consequent
			return this.createAccessRequestsClassForOnlyOneApplicableNegative(policySet, abacPolicies);
		}
	}

	private OntClass createAccessRequestsClassForOnlyOneApplicablePositive(Individual policySet, ExtendedIterator<Statement> abacPolicies)
	{
		return createAccessRequestsClassForOnlyOneApplicableWithConsequentSuffix(policySet, abacPolicies, "positive");
	}

	private OntClass createAccessRequestsClassForOnlyOneApplicableNegative(Individual policySet, ExtendedIterator<Statement> abacPolicies)
	{
		return createAccessRequestsClassForOnlyOneApplicableWithConsequentSuffix(policySet, abacPolicies, "negative");
	}
	
	private OntClass createAccessRequestsClassForOnlyOneApplicableWithConsequentSuffix(Individual policySet, ExtendedIterator<Statement> abacPolicies, String consequentSuffix) {
		List<OntClass> externalUnionClassList = new ArrayList<OntClass>();
		while(abacPolicies.hasNext())
		{
			ExtendedIterator<Statement> secondIterator = new StmtIteratorImpl(abacPolicies);
			Individual abacPolicy = abacPolicies.next().getSubject().as(Individual.class);
			// R i,x
			OntClass policyAccessRequestsForConsequent = abacPolicy.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#hasAccessRequestClassFor_" + consequentSuffix)).toList().get(0).as(OntClass.class);
			List<OntClass> unionOfOtherPoliciesRequestsList = new ArrayList<OntClass>();
			while(secondIterator.hasNext())
			{
				Individual otherPolicy = secondIterator.next().getSubject().as(Individual.class);
				if(!otherPolicy.equals(abacPolicy))
				{	// k≠i
					OntClass otherPolicyAccessRequestsForPositive = otherPolicy.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#hasAccessRequestClassFor_positive")).toList().get(0).as(OntClass.class);
					OntClass otherPolicyAccessRequestsForNegative = otherPolicy.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/combiningAlgorithms#hasAccessRequestClassFor_negative")).toList().get(0).as(OntClass.class);
					unionOfOtherPoliciesRequestsList.add(otherPolicyAccessRequestsForPositive);
					unionOfOtherPoliciesRequestsList.add(otherPolicyAccessRequestsForNegative);
				}
			}
			// ¬⨆ k≤n ∧ k≠i (R k,Deny ⊔ R k,Permit )
			OntClass complementOfUnionOfOtherPoliciesRequests = ((OntModel)jdsi.getModel()).createComplementClass(null, ((OntModel)jdsi.getModel()).createUnionClass(null, ((OntModel)jdsi.getModel()).createList(unionOfOtherPoliciesRequestsList.iterator())));
			
			List<OntClass> intersectionOfRequestsList = new ArrayList<OntClass>();
			intersectionOfRequestsList.add(policyAccessRequestsForConsequent);
			intersectionOfRequestsList.add(complementOfUnionOfOtherPoliciesRequests);
			// R i,x ⨅ ¬⨆ k≤n ∧ k≠i (R k,Deny ⊔ R k,Permit )
			OntClass intersectionOfRequests = ((OntModel)jdsi.getModel()).createIntersectionClass(null, ((OntModel)jdsi.getModel()).createList(intersectionOfRequestsList.iterator()));
			
			externalUnionClassList.add(intersectionOfRequests);
		}
		
		OntClass result = ((OntModel)jdsi.getModel()).createClass("http://www.paasword.eu/security-policy/seerc/pac#" + policySet.getLocalName() + "AccessRequestClassFor_" + consequentSuffix);
		result.addEquivalentClass(((OntModel)jdsi.getModel()).createUnionClass(null, ((OntModel)jdsi.getModel()).createList(externalUnionClassList.iterator())));

		return result;
	}

	private OntClass createAccessRequestsClassForPermitOverrides(Individual policySet, ExtendedIterator<Statement> abacPolicies, Resource consequent)
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

	private OntClass createAccessRequestsClassForDenyOverrides(Individual policySet, ExtendedIterator<Statement> abacPolicies, Resource consequent)
	{
		if(consequent.equals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#negative")))
		{	// negative consequent - R PS DO ,Deny ≡ ⨆ i≤n R i,Deny
			OntClass result = ((OntModel)jdsi.getModel()).createClass("http://www.paasword.eu/security-policy/seerc/pac#" + policySet.getLocalName() + "AccessRequestClassFor_negative");
			result.addEquivalentClass(this.createUnionOfDenyPolicyAccessRequests(policySet, abacPolicies));
			return result;
			
		}
		else
		{	// positive consequent - R PS DO ,Permit ≡ (⨆ i≤n R i,Permit ) ⨅ ¬R PS DO ,Deny
			OntClass unionOfPermitPolicyAccessRequests = this.createUnionOfPermitPolicyAccessRequests(policySet, abacPolicies);
			OntClass unionOfDenyPolicyAccessRequests = this.createUnionOfDenyPolicyAccessRequests(policySet, abacPolicies);
			List<RDFNode> policyAccessRequestsForPositiveList = new ArrayList<RDFNode>();
			policyAccessRequestsForPositiveList.add(unionOfPermitPolicyAccessRequests);
			policyAccessRequestsForPositiveList.add(((OntModel)jdsi.getModel()).createComplementClass(null, unionOfDenyPolicyAccessRequests));

			OntClass result = ((OntModel)jdsi.getModel()).createClass("http://www.paasword.eu/security-policy/seerc/pac#" + policySet.getLocalName() + "AccessRequestClassFor_positive");
			result.addEquivalentClass(((OntModel)jdsi.getModel()).createIntersectionClass(null, ((OntModel)this.jdsi.getModel()).createList(policyAccessRequestsForPositiveList.iterator())));
			
			return result;
		}
	}

	private OntClass createUnionOfPermitPolicyAccessRequests(Individual policySet, ExtendedIterator<Statement> abacPolicies)
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

	private OntClass createUnionOfDenyPolicyAccessRequests(Individual policySet, ExtendedIterator<Statement> abacPolicies)
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
}
