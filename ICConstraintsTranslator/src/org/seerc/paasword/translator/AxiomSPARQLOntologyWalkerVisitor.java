package org.seerc.paasword.translator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aksw.owl2sparql.OWLClassExpressionToSPARQLConverter;
import org.aksw.owl2sparql.OWLObjectPropertyExpressionConverter;
import org.aksw.owl2sparql.util.VarGenerator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLQuantifiedRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

/**
 * AxiomSPARQLOntologyWalkerVisitor is responsible for visiting expressions in an OWL ontology
 * and transforming OWL axioms to Integrity Constraints, expressed as DCQnot queries
 * using the Negation-As-Failure pattern, modeled in SPARQL.
 * It currently supports object and datatype qualified cardinalities (some, min, max,
 * exact) and property domain and range restrictions.
 *  
 * @author Chris Petsos
 *
 */
public class AxiomSPARQLOntologyWalkerVisitor extends OWLOntologyWalkerVisitor {

	// We need visited axioms cached, so they are not visited multiple times
	// in cases of nested expressions.
	List<OWLAxiom> visitedAxioms = new ArrayList<OWLAxiom>();
	
	// Variable generators for classes and datatypes.
	VarGenerator classVarGenerator;
	VarGenerator datatypeVarGenerator;
	
	// These do perform the magic of converting OWL expressions to SPARQL. 
	OWLClassExpressionToSPARQLConverter ceConverter;
	OWLObjectPropertyExpressionConverter opConverter;
	
	// Our OWL Data Factory
    OWLDataFactory factory;

    // This list is populated and returned as parting of the visitor execution.
	List<QueryConstraint> queries = new ArrayList<QueryConstraint>();

	// This is the SPARQL query pattern we use to construct the queries.
	// The groupGraphPatternTag is replaced with the appropriate DCQnot
	// conversion.
	private String groupGraphPatternTag = "<groupGraphPattern>";
	private String queryTemplate = 
			  "SELECT DISTINCT *\n"
			+ "WHERE {\n"
			+ groupGraphPatternTag + "\n"
			+ "}";
	
	/**
	 * Public constructor. Needs an OWLOntologyWalker to start operating on.
	 * @param walker
	 */
	public AxiomSPARQLOntologyWalkerVisitor(OWLOntologyWalker walker) 
	{
		super(walker);
		ceConverter = new OWLClassExpressionToSPARQLConverter();
		opConverter = new OWLObjectPropertyExpressionConverter();

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    factory = manager.getOWLDataFactory();
	}

	/**
	 * Generates an object some-values-from quantified cardinality restriction.
	 */
	@Override
    public void visit(OWLObjectSomeValuesFrom ce) {
		processQuantifiedRestriction(ce, 1);
    }

	/**
	 * Generates a datatype some-values-from quantified cardinality restriction.
	 */
    @Override
    public void visit(OWLDataSomeValuesFrom ce) {
    	processQuantifiedRestriction(ce, 1);
    }

    /**
	 * Generates an object minimum quantified cardinality restriction.
     */
	public void visit(OWLObjectMinCardinality ce) {
		processMinCardinalityRestriction(ce);
	}

	/**
	 * Generates a datatype minimum quantified cardinality restriction.
	 */
    @Override
    public void visit(OWLDataMinCardinality ce) {
		processMinCardinalityRestriction(ce);
    }

    /**
	 * Generates an object maximum quantified cardinality restriction.
     */
	@Override
	public void visit(OWLObjectMaxCardinality ce) {
		processMaxCardinalityRestriction(ce);
	}

	/**
	 * Generates a datatype maximum quantified cardinality restriction.
	 */
    @Override
    public void visit(OWLDataMaxCardinality ce) {
		processMaxCardinalityRestriction(ce);
    }

    /**
	 * Generates an object exact quantified cardinality restriction.
     */
	@Override
	public void visit(OWLObjectExactCardinality ce) {
		processExactCardinalityRestriction(ce);
	}

	/**
	 * Generates a datatype exact quantified cardinality restriction.
	 */
    @Override
    public void visit(OWLDataExactCardinality ce) {
		processExactCardinalityRestriction(ce);
	}

    /**
	 * Generates a property domain restriction.
     */
    @Override
	public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
		if(!preprocess()) return;

		OWLClassExpression ce = factory.getOWLClass(axiom.getDomain());

		// create unique names for all used variables
		String domainVar = classVarGenerator.newVar();
		String freshVar = classVarGenerator.newVar();
		
		// create the query's graph pattern
		String groupGraphPattern = 
				createPropertyExpressionGraphPattern(domainVar, axiom.getProperty(), freshVar) +
				openFNEBlock() + 
				createClassExpressionGraphPattern(ce, domainVar) + 
				closeBlock();
		
		postProcess(this.createAnnotatedAxiom(axiom, "Invalid domain of property " + axiom.getProperty() + ".", "error"), groupGraphPattern);
	}

    /*
     * Annotates an axiom with a rdfs:comment description and a rdfs:label constraintLevel.
     */
	private OWLAxiom createAnnotatedAxiom(OWLAxiom axiom, String constraintDescription, String constraintLevel) {
		OWLAxiom result = this.createCommentAnnotatedAxiom(axiom, constraintDescription);
		result = this.createLabelAnnotatedAxiom(result, constraintLevel);
		return result;
	}

	private OWLAxiom createCommentAnnotatedAxiom(OWLAxiom axiom, String labelValue) {
		return this.createAxiomAnnotation(axiom, factory.getRDFSComment(), labelValue);
	}

	private OWLAxiom createLabelAnnotatedAxiom(OWLAxiom axiom, String constraintLevelValue) {
		return this.createAxiomAnnotation(axiom, factory.getRDFSLabel(), constraintLevelValue);
	}

	private OWLAxiom createAxiomAnnotation(OWLAxiom axiom, OWLAnnotationProperty annotationProperty, String labelValue) {
		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
		OWLAnnotation annotation = factory.getOWLAnnotation(annotationProperty, factory.getOWLLiteral(labelValue));
		annotations.add(annotation);
		return axiom.getAnnotatedAxiom(annotations);
	}

    /**
	 * Generates an object property range restriction.
     */
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		if(!preprocess()) return;

		// create unique names for all used variables
		String rangeVar = classVarGenerator.newVar();
		String freshVar = classVarGenerator.newVar();
		
		// create the query's graph pattern
		String groupGraphPattern = 
				createPropertyExpressionGraphPattern(freshVar, axiom.getProperty().asOWLObjectProperty(), rangeVar) +
				openFNEBlock() + 
				createClassExpressionGraphPattern(axiom.getRange(), rangeVar) + 
				closeBlock();
		
		postProcess(this.createAnnotatedAxiom(axiom, "Invalid range of object property " + axiom.getProperty().asOWLObjectProperty() + ".", "error"), groupGraphPattern);
	}

	/**
	 * Generates a datatype property domain restriction.
	 */
    @Override
    public void visit(OWLDataPropertyRangeAxiom axiom) {
		if(!preprocess()) return;

		// create unique names for all used variables
		String rangeVar = datatypeVarGenerator.newVar();
		String freshVar = classVarGenerator.newVar();
		
		// create the query's graph pattern
		String groupGraphPattern = 
				createPropertyExpressionGraphPattern(freshVar, axiom.getProperty().asOWLDataProperty(), rangeVar) +
				openFilterBlock() + 
				"!(" + createDataRangeGraphPattern(axiom.getRange(), rangeVar) + ")" + 
				closeParentheses();
		
		postProcess(this.createAnnotatedAxiom(axiom, "Invalid range of data property " + axiom.getProperty().asOWLDataProperty() + ".", "error"), groupGraphPattern);
    }

    /**
     * All other axioms are unsupported.
     */
    @Override
    protected void handleDefault(OWLObject axiom) {
		System.out.println("NOT SUPPORTED: " + axiom + ", " + axiom.getClass().getSimpleName());
	}

    /*
     * Given an exact cardinality class expression, this converts it to a DCQnot group graph pattern. 
     */
    private <T extends OWLCardinalityRestriction, OWLQuantifiedRestriction> void processExactCardinalityRestriction(T ce) {
		if(!preprocess()) return;

		if(this.isNestedExpressionInRestriction(ce)) return;
		
		// create unique names for all used variables
		String subclassVar = classVarGenerator.newVar();

		// create the query's graph pattern
		String groupGraphPattern = 
  				  createClassExpressionGraphPattern(((OWLSubClassOfAxiom) this.getCurrentAxiom()).getSubClass(), subclassVar)
  				+ openBlock()
  				+ createPropertyTypeAndNotEqualVarPairsGraphPattern(ce.getCardinality()+1, subclassVar, ce.getProperty(), ce.getFiller())
  				+ closeBlock() 
  				+ openUnionBlock()
  				+ openFNEBlock()
  				+ createPropertyTypeAndNotEqualVarPairsGraphPattern(ce.getCardinality(), subclassVar, ce.getProperty(), ce.getFiller())
  				+ closeBlock()
  				+ closeBlock();
		
		postProcess(this.getCurrentAxiom(), groupGraphPattern);
	}
    
    /*
     * Given a max cardinality class expression, this converts it to a DCQnot group graph pattern. 
     */
    private <T extends OWLCardinalityRestriction, OWLQuantifiedRestriction> void processMaxCardinalityRestriction(T ce) {
		if(!preprocess()) return;

		if(this.isNestedExpressionInRestriction(ce)) return;
		
		// create unique names for all used variables
		String subclassVar = classVarGenerator.newVar();

		// create the query's graph pattern
		String groupGraphPattern = 
  				  createClassExpressionGraphPattern(((OWLSubClassOfAxiom) this.getCurrentAxiom()).getSubClass(), subclassVar)
  				+ createPropertyTypeAndNotEqualVarPairsGraphPattern(ce.getCardinality()+1, subclassVar, ce.getProperty(), ce.getFiller());
		
		postProcess(this.getCurrentAxiom(), groupGraphPattern);
	}
    
    /*
     * Given a min cardinality class expression, this converts it to a DCQnot group graph pattern. 
     */

    private <T extends OWLCardinalityRestriction, OWLQuantifiedRestriction> void processMinCardinalityRestriction(T ce) {
		this.processQuantifiedRestriction(ce, ce.getCardinality());
	}
    
    /*
     * Helper refactored method which is reused in many cardinality restriction conversion cases.
     */
    private void processQuantifiedRestriction(OWLQuantifiedRestriction ce, int cardinality) {
		if(!preprocess()) return;

		if(this.isNestedExpressionInRestriction(ce)) return;
		
		// create unique names for all used variables
		String subclassVar = classVarGenerator.newVar();

		// create the query's graph pattern
		String groupGraphPattern = 
  				  createClassExpressionGraphPattern(((OWLSubClassOfAxiom) this.getCurrentAxiom()).getSubClass(), subclassVar)
  				+ openFNEBlock()
  				+ createPropertyTypeAndNotEqualVarPairsGraphPattern(cardinality, subclassVar, ce.getProperty(), ce.getFiller())
  				+ closeBlock();
		
		postProcess(this.getCurrentAxiom(), groupGraphPattern);
	}

	private boolean isNestedExpressionInRestriction(OWLObject ce) {
		// If the current axiom's super-class is not equal to the current class expression then this is a nested expression.
		if(!ce.equals(((OWLSubClassOfAxiom) this.getCurrentAxiom()).getSuperClass()))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
    
    /*
     * Creates the property type and the vars-not-equal part of a graph pattern.
     */
    private String createPropertyTypeAndNotEqualVarPairsGraphPattern(int numOfLoops, String subclassVar, OWLPropertyExpression property, OWLObject filler)
	{
		List<String> freshVars = new ArrayList<String>();
		String result = 	createPropertyAndTypeGraphPattern(numOfLoops, subclassVar, property, filler, freshVars)
  							+ createNotEqualVarPairs(freshVars);
		return result;
	}

    /*
     * Creates the property type part of a graph pattern.
     */
    private String createPropertyAndTypeGraphPattern(int numOfLoops, String subclassVar, OWLPropertyExpression property, OWLObject filler, List<String> freshVars) {
		String result = "";
		for(int i=0;i<numOfLoops;i++)
		{
			if(property.isObjectPropertyExpression())
			{
    			String freshVar = classVarGenerator.newVar();
    			freshVars.add(freshVar);
				result += 	  createPropertyExpressionGraphPattern(subclassVar, ((OWLObjectPropertyExpression) property).asOWLObjectProperty(), freshVar)
							+ createClassExpressionGraphPattern((OWLClassExpression) filler, freshVar);
			}
			else
			{	// Datatype property
    			String freshVar = datatypeVarGenerator.newVar();
    			freshVars.add(freshVar);
				result += 	  createPropertyExpressionGraphPattern(subclassVar, ((OWLDataPropertyExpression) property).asOWLDataProperty(), freshVar)
							+ openFilterBlock()
							+ createDataRangeGraphPattern((OWLDataRange)filler, freshVar)
							+ closeParentheses();
			}
		}
		return result;
	}
	
    /*
     * Creates the vars-not-equal part of a graph pattern.
     */
    private String createNotEqualVarPairs(List<String> freshVars) {
		String result = "";
		for(int j=0;j<freshVars.size();j++)
		{
			for(int i=0;i<j;i++)
			{
				result += 
    					openFilterBlock() +
						freshVars.get(i) + " != " + freshVars.get(j) + 
						closeParentheses();
			}
		}
		return result;
	}

    /*
     * Helper for opening a SPARQL block.
     */
	private String openBlock() {
		return "{";
	}

    /*
     * Helper for closing a SPARQL block.
     */
	private String closeBlock() {
		return "}";
	}

    /*
     * Helper for opening a FILTER NOT EXISTS block.
     */
	private String openFNEBlock()
	{
		return "FILTER NOT EXISTS {\n";
	}

    /*
     * Helper for opening a UNION block.
     */
	private String openUnionBlock()
	{
		return "UNION {\n";
	}

    /*
     * Helper for opening a FILTER block.
     */
	private String openFilterBlock()
	{
		return "FILTER (\n";
	}

    /*
     * Helper for closing parentheses.
     */
	private String closeParentheses() {
		return ")";
	}

	/*
	 * Use the ceConverter to convert a classExpression to SPARQL using a projectionVariable.
	 */
	private String createClassExpressionGraphPattern(OWLClassExpression classExpression, String projectionVariable) {
		return ceConverter.asGroupGraphPattern(classExpression, projectionVariable);
	}
	
	/*
	 * Use the ceConverter to convert a data range to SPARQL using a projectionVariable.
	 */
	private String createDataRangeGraphPattern(OWLDataRange dataRange, String projectionVariable) {
		return ceConverter.asGroupGraphPattern(dataRange, projectionVariable);
	}
	
	/*
	 * Helper to create the property expression part of a graph pattern.
	 */
	private String createPropertyExpressionGraphPattern(String domain, OWLProperty property, String range) {
		return domain + " <" + property.toStringID() + "> " + range + " .\n";
	}
	
	/*
	 * This runs before every OWL to query conversion.
	 */
	private boolean preprocess()
	{
		reset();
		return true;
	}

	/*
	 * Initializes the var generators.
	 */
	private void reset() {
		// re-init var generators
		classVarGenerator = new VarGenerator("x");
		datatypeVarGenerator = new VarGenerator("d");
	}

    /*
     * After successful conversion, add query (along with the corresponding converted axiom and axiom description if there is one) in a prettified form inside the results. 
     */
    private void postProcess(OWLAxiom axiom, String groupGraphPattern)
	{
		String query = createPrettyQuery(groupGraphPattern);
		String axiomDescription = this.getAxiomAnnotation(axiom, factory.getRDFSComment());
		String axiomConstraintLevel = this.getAxiomAnnotation(axiom, factory.getRDFSLabel());

    	queries.add(new QueryConstraint(axiom.toString(), query, axiomDescription, axiomConstraintLevel));
	}

	private String getAxiomAnnotation(OWLAxiom axiom, OWLAnnotationProperty annotationProperty) {
		// get all annotationProperty annotations of axiom
		Iterator<OWLAnnotation> axiomAnnotationPropertyIterator = axiom.getAnnotations(annotationProperty).iterator();
		// if there is one
		if(axiomAnnotationPropertyIterator.hasNext())
		{	// take the first as axiom annotation
			return ((OWLLiteral)axiomAnnotationPropertyIterator.next().getValue()).getLiteral();
		}
		
		// otherwise return null
		return null;
	}
    
    private String createPrettyQuery(String groupGraphPattern) {
    	// replace groupGraphPatternTag withe the one generated and pretty print.
		String query = prettyPrint(queryTemplate.replace(this.groupGraphPatternTag, groupGraphPattern));
		System.out.println(query);
		return query;
	}

    /*
     * Creates a Jena Query and returns its String representation.
     * TODO: Non-optimal! Find another way to pretty print.
     */
	protected String prettyPrint(String query)
	{
		// build an Apache Jena Query from the String which is already pretty printed.
		Query sparqlQuery = QueryFactory.create(query);
		return sparqlQuery.toString();
	}

	/**
	 * @return The converted queries of this visitor.
	 */
	public List<QueryConstraint> getQueries()
	{
		return this.queries;
	}

}
