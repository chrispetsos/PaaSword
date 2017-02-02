package org.seerc.paasword.validator.engine;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.HasValueRestriction;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class SubclassSubsumptionsEngine extends EntitySubsumptionBaseEngine {

	public SubclassSubsumptionsEngine(JenaDataSourceInferred jdsi) {
		super(jdsi);
		
		// create the translations of statements from the "otp" namespace 
		// to restriction statements
		this.generateRestrictionStatements();
	}

	private void generateRestrictionStatements()
	{
		// Get all the individuals of the otp:TheoremProvingBaseClass.
		List<Individual> individualsIterator = ((OntModel)this.jdsi.getModel()).listIndividuals(this.jdsi.createResourceFromUri("otp:TheoremProvingBaseClass")).toList();
		
		// Iterate over them. 
		for(Individual individual:individualsIterator)
		{
			// Get the reference statement. Should be at most one... 
			Resource individualReference = null;
			StmtIterator referenceStatementIterator = individual.listProperties(ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:TheoremProvingReferenceProperty").getURI()));
			if(referenceStatementIterator.hasNext())
			{
				Statement referenceStatement = referenceStatementIterator.next();
				individualReference = referenceStatement.getObject().asResource();
			}
			
			// Get the statements where the individual is subject of a "otp:TheoremProvingParameterProperty" parameter.
			StmtIterator resourceParams = individual.listProperties(ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:TheoremProvingParameterProperty").getURI()));

			// Switch cases of individual
			if(individual.hasOntClass(jdsi.createResourceFromUri("otp:ANDTheoremProvingClass").getURI()))
			{	// otp:ANDTheoremProvingClass
				this.createIntersectionRestriction(individual, resourceParams);
			}
			else if(individual.hasOntClass(jdsi.createResourceFromUri("otp:ORTheoremProvingClass").getURI()))
			{	// otp:ORTheoremProvingClass

			}
			else if(individual.hasOntClass(jdsi.createResourceFromUri("otp:XORTheoremProvingClass").getURI()))
			{	// otp:XORTheoremProvingClass

			}
			else if(individual.hasOntClass(jdsi.createResourceFromUri("otp:NOTheoremProvingClass").getURI()))
			{	// otp:NOTheoremProvingClass

			}
			else
			{	// "terminating" param

			}
		}
	}

	private void createIntersectionRestriction(Individual individual, StmtIterator resourceParams)
	{
		// the hasValue Restrictions RDFList
		RDFList hasValueRestrictionsRDFList;
		
		// first put them in a List
		List<RDFNode> hasValueRestrictionsList = new ArrayList<RDFNode>();
		while(resourceParams.hasNext())
		{
			RDFNode param = resourceParams.next().getObject();
			HasValueRestriction hvr = ((OntModel)this.jdsi.getModel()).createHasValueRestriction(null, ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:TheoremProvingParameterProperty").getURI()), param);
			hasValueRestrictionsList.add(hvr);
		}

		// then create the RDFList
		hasValueRestrictionsRDFList = ((OntModel)this.jdsi.getModel()).createList(hasValueRestrictionsList.iterator());
		
		// create the intersection class
		IntersectionClass intersection = ((OntModel)this.jdsi.getModel()).createIntersectionClass(null, hasValueRestrictionsRDFList);
		
		// make the individual also a class and equivalent to the intersection
		((OntModel)this.jdsi.getModel()).createClass(individual.getURI()).addEquivalentClass(intersection);
	}

	@Override
	protected boolean entitySubsumes(String entity1Uri, String entity2Uri) {
		return false;
	}

	@Override
	protected void addSubsumption(String entity1Uri, String entity2Uri) {
		
	}

}
