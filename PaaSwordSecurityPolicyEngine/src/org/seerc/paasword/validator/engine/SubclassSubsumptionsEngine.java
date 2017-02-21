package org.seerc.paasword.validator.engine;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.HasValueRestriction;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class SubclassSubsumptionsEngine extends EntitySubsumptionBaseEngine {

	public SubclassSubsumptionsEngine(JenaDataSourceInferred jdsi) {
		super(jdsi);
	}

	/*
	 * generates restriction and subclass statements and then calls enhanceModel()
	 * of super-class.
	 * 
	 * (non-Javadoc)
	 * @see org.seerc.paasword.validator.engine.EntitySubsumptionBaseEngine#enhanceModel()
	 */
	public void enhanceModel()
	{
		// create the translations of statements from the "otp" namespace 
		// to restriction statements
		this.generateRestrictionStatements();
		
		// create subclass statement for entities that are connected with "otp:subsumes"
		this.createSubclassStatements();

		super.enhanceModel();
	}
	
	/*
	 * Generated restriction statements for all individuals of otp:TheoremProvingBaseClass.
	 */
	private void generateRestrictionStatements()
	{
		// Get all the individuals of the otp:TheoremProvingBaseClass.
		List<Individual> individualsIterator = ((OntModel)this.jdsi.getModel()).listIndividuals(this.jdsi.createResourceFromUri("otp:TheoremProvingBaseClass")).toList();
		
		// Iterate over them. 
		for(Individual individual:individualsIterator)
		{
			// the class, either intersection or union etc., with the parameter restrictions.
			OntClass parameterRestrictionClass = createParameterRestrictionClass(individual);
			
			// the class to which this individual will become equivalent
			OntClass equivalentClass = parameterRestrictionClass;
			
			// Get the reference statement iterator. Should have at most one element... 
			StmtIterator referenceStatementIterator = individual.listProperties(ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:TheoremProvingReferenceProperty").getURI()));
			
			if(referenceStatementIterator.hasNext())
			{	// this expression has reference
				// create intersection of parameterRestrictionClass and referenceStatement
				Statement referenceStatement = referenceStatementIterator.next();
				equivalentClass = this.createReferenceIntersection(equivalentClass, referenceStatement);
			}
			
			// make the individual also a class and equivalent to the desired class
			((OntModel)this.jdsi.getModel()).createClass(individual.toString()).addEquivalentClass(equivalentClass);

		}
	}

	/*
	 * Creates a parameter restriction class based on the type of the individual.
	 */
	private OntClass createParameterRestrictionClass(Individual individual) {
		// Switch cases of individual
		if(individual.hasOntClass(jdsi.createResourceFromUri("otp:ANDTheoremProvingClass").getURI()))
		{	// otp:ANDTheoremProvingClass
			return this.createIntersectionRestriction(individual);
		}
		else if(individual.hasOntClass(jdsi.createResourceFromUri("otp:ORTheoremProvingClass").getURI()))
		{	// otp:ORTheoremProvingClass
			return this.createUnionRestriction(individual);
		}
		else if(individual.hasOntClass(jdsi.createResourceFromUri("otp:XORTheoremProvingClass").getURI()))
		{	// otp:XORTheoremProvingClass
			return this.createXORRestriction(individual);
		}
		else if(individual.hasOntClass(jdsi.createResourceFromUri("otp:NOTheoremProvingClass").getURI()))
		{	// otp:NOTheoremProvingClass
			return this.createComplementRestriction(individual);
		}
		else if(individual.hasOntClass(jdsi.createResourceFromUri("otp:TheoremProvingBaseClass").getURI()))
		{	// otp:TheoremProvingBaseClass
			return this.createIntersectionRestriction(individual);
		}
		else
		{	// terminating param
			return this.createHasValueRestriction(individual);
		}
	}

	private OntClass createIntersectionRestriction(Individual individual)
	{
		// the RDFList with the restrictions, either hasValue params or nested expression restrictions etc.
		RDFList restrictionsRDFList = this.createParamRestrictionRDFList(individual);
		
		// create the intersection class with the restrictions and return it
		return ((OntModel)this.jdsi.getModel()).createIntersectionClass(null, restrictionsRDFList);
	}

	private OntClass createUnionRestriction(Individual individual)
	{
		// the RDFList with the restrictions, either hasValue params or nested expression restrictions etc.
		RDFList restrictionsRDFList = this.createParamRestrictionRDFList(individual);
		
		// create the union class and return it
		return ((OntModel)this.jdsi.getModel()).createUnionClass(null, restrictionsRDFList);
	}

	/*
	 * Helper method to create a RDFList from the parameters of an individual by recursively
	 * building the restrictions.
	 */
	private RDFList createParamRestrictionRDFList(Individual individual) {
		RDFList restrictionsRDFList;
		// first put them in a List
		List<RDFNode> restrictionsList = new ArrayList<RDFNode>();

		// Get the statements where the individual is subject of a "otp:TheoremProvingParameterProperty" parameter.
		StmtIterator resourceParams = individual.listProperties(ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:TheoremProvingParameterProperty").getURI()));
		while(resourceParams.hasNext())
		{	// create the param restriction class recursively
			Statement param = resourceParams.next();
			restrictionsList.add(this.createParameterRestrictionClass(param.getObject().as(Individual.class)));
		}
		
		// build the restrictionsRDFList from restrictionsList
		restrictionsRDFList = ((OntModel)this.jdsi.getModel()).createList(restrictionsList.iterator());
		
		return restrictionsRDFList;
	}

	private OntClass createComplementRestriction(Individual individual)
	{
		// Get the statement (should be exactly one) where the individual is subject of a "otp:TheoremProvingParameterProperty" parameter.
		Statement notParam = individual.listProperties(ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:TheoremProvingParameterProperty").getURI())).next();
		
		return ((OntModel)this.jdsi.getModel()).createComplementClass(null, this.createParameterRestrictionClass(notParam.getObject().as(Individual.class)));
	}

	private OntClass createXORRestriction(Individual individual)
	{
		// Get the statements (should be exactly two) where the individual is subject of a "otp:TheoremProvingParameterProperty" parameter.
		StmtIterator xorParams = individual.listProperties(ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:TheoremProvingParameterProperty").getURI()));
		Individual xorParam1 = xorParams.next().getObject().as(Individual.class);
		Individual xorParam2 = xorParams.next().getObject().as(Individual.class);
		
		// p XOR q   =             ( p AND NOT q )  OR     ( NOT p AND q )
		
		List<RDFNode> pAndNotQList = new ArrayList<RDFNode>();
		pAndNotQList.add(this.createParameterRestrictionClass(xorParam1));
		pAndNotQList.add(((OntModel)this.jdsi.getModel()).createComplementClass(null, this.createParameterRestrictionClass(xorParam2)));
		RDFList pAndNotQRDFList = ((OntModel)this.jdsi.getModel()).createList(pAndNotQList.iterator());
		OntClass pAndNotQClass = ((OntModel)this.jdsi.getModel()).createIntersectionClass(null, pAndNotQRDFList);
		
		List<RDFNode> notPAndQList = new ArrayList<RDFNode>();
		notPAndQList.add(((OntModel)this.jdsi.getModel()).createComplementClass(null, this.createParameterRestrictionClass(xorParam1)));
		notPAndQList.add(this.createParameterRestrictionClass(xorParam2));
		RDFList notPAndQRDFList = ((OntModel)this.jdsi.getModel()).createList(notPAndQList.iterator());
		OntClass notPAndQClass = ((OntModel)this.jdsi.getModel()).createIntersectionClass(null, notPAndQRDFList);
		
		List<RDFNode> orList = new ArrayList<RDFNode>();
		orList.add(pAndNotQClass);
		orList.add(notPAndQClass);
		RDFList orRDFList = ((OntModel)this.jdsi.getModel()).createList(orList.iterator());
		OntClass orClass = ((OntModel)this.jdsi.getModel()).createUnionClass(null, orRDFList);
		
		return orClass;
	}

	/*
	 * Create an intersection of the parameterRestrictionClass with a hasValue restriction of the referenceStatement's object
	 */
	private OntClass createReferenceIntersection(OntClass parameterRestrictionClass, Statement referenceStatement)
	{
		List<RDFNode> referenceIntersectionList = new ArrayList<RDFNode>();
		referenceIntersectionList.add(parameterRestrictionClass);
		HasValueRestriction referenceHasValueRestriction = this.createHasValueRestriction(referenceStatement.getObject());
		referenceIntersectionList.add(referenceHasValueRestriction);
		
		RDFList referenceIntersectionRDFList = ((OntModel)this.jdsi.getModel()).createList(referenceIntersectionList.iterator());
		
		return ((OntModel)this.jdsi.getModel()).createIntersectionClass(null, referenceIntersectionRDFList);
	}

	/*
	 * This methods creates subsumptions for entities whose subsumption has been
	 * denoted by using the "otp:subsumes" property.
	 */
	private void createSubclassStatements()
	{
		StmtIterator subsumesStatements = ((OntModel)this.jdsi.getModel()).listStatements((Resource)null, ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:subsumes").getURI()), (RDFNode)null);
		while(subsumesStatements.hasNext())
		{
			Statement subsumption = subsumesStatements.next();
			this.addSubsumption(subsumption.getSubject().toString(), subsumption.getObject().asResource().toString());
		}
	}

	/*
	 * return true if either entity1Uri resourceSubsumes entity2Uri OR
	 * if entity2Uri is a subclass of entity1Uri
	 * 
	 * Note that this is triggered in super-class only for individuals of 
	 * "otp:TheoremProvingBaseClass", not for paramteres; thus the reason for
	 * createSubclassStatements().
	 */
	@Override
	protected boolean entitySubsumes(String entity1Uri, String entity2Uri)
	{
		Resource resource1 = ((OntModel)this.jdsi.getModel()).createResource(entity1Uri);
		Resource resource2 = ((OntModel)this.jdsi.getModel()).createResource(entity2Uri);
		return 	this.resourceSubsumes(resource1, resource2)
				|| resource1.as(OntClass.class).hasSubClass(resource2);
	}

	/*
	 * Adds a subclass statement of the form,
	 * 
	 * 	[a owl:Restriction ;
		owl:onProperty pac:hasParameter ;
		owl:hasValue entity2Uri] 
			rdfs:subClassOf
				[a owl:Restriction ;
				owl:onProperty pac:hasParameter ;
				owl:hasValue entity1Uri] .
	 * 
	 * (non-Javadoc)
	 * @see org.seerc.paasword.validator.engine.EntitySubsumptionBaseEngine#addSubsumption(java.lang.String, java.lang.String)
	 */
	@Override
	protected void addSubsumption(String entity1Uri, String entity2Uri)
	{
		HasValueRestriction subClassHasValueRestriction = this.createHasValueRestriction(((OntModel)this.jdsi.getModel()).createResource(entity2Uri));
		HasValueRestriction superClassHasValueRestriction = this.createHasValueRestriction(((OntModel)this.jdsi.getModel()).createResource(entity1Uri));
		subClassHasValueRestriction.addSuperClass(superClassHasValueRestriction);
	}

	/*
	 * Creates a hasValue restriction with value the rdfNode.
	 */
	private HasValueRestriction createHasValueRestriction(RDFNode rdfNode) {
		HasValueRestriction hvr = ((OntModel)this.jdsi.getModel()).createHasValueRestriction(null, ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:TheoremProvingParameterProperty").getURI()), rdfNode);
		return hvr;
	}

}
