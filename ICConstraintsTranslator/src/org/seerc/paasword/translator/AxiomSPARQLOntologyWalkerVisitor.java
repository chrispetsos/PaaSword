package org.seerc.paasword.translator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aksw.owl2sparql.OWLClassExpressionToSPARQLConverter;
import org.aksw.owl2sparql.OWLObjectPropertyExpressionConverter;
import org.aksw.owl2sparql.util.VarGenerator;
import org.semanticweb.owlapi.apibinding.OWLManager;
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

public class AxiomSPARQLOntologyWalkerVisitor extends OWLOntologyWalkerVisitor {

	List<OWLAxiom> visitedAxioms = new ArrayList<OWLAxiom>();
	VarGenerator classVarGenerator;
	VarGenerator datatypeVarGenerator;
	OWLClassExpressionToSPARQLConverter ceConverter;
	OWLObjectPropertyExpressionConverter opConverter;
	
    OWLDataFactory factory;

	List<QueryConstraint> queries = new ArrayList<QueryConstraint>();

	private String groupGraphPatternTag = "<groupGraphPattern>";
	private String queryTemplate = 
			  "SELECT DISTINCT *\n"
			+ "WHERE {\n"
			+ groupGraphPatternTag + "\n"
			+ "}";
	
	public AxiomSPARQLOntologyWalkerVisitor(OWLOntologyWalker walker) 
	{
		super(walker);
		ceConverter = new OWLClassExpressionToSPARQLConverter();
		opConverter = new OWLObjectPropertyExpressionConverter();

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    factory = manager.getOWLDataFactory();
	}

	@Override
    public void visit(OWLObjectSomeValuesFrom ce) {
		processQuantifiedRestriction(ce, 1);
    }

    @Override
    public void visit(OWLDataSomeValuesFrom ce) {
    	processQuantifiedRestriction(ce, 1);
    }

	public void visit(OWLObjectMinCardinality ce) {
		processMinCardinalityRestriction(ce);
	}

    @Override
    public void visit(OWLDataMinCardinality ce) {
		processMinCardinalityRestriction(ce);
    }

	@Override
	public void visit(OWLObjectMaxCardinality ce) {
		processMaxCardinalityRestriction(ce);
	}

    @Override
    public void visit(OWLDataMaxCardinality ce) {
		processMaxCardinalityRestriction(ce);
    }

	@Override
	public void visit(OWLObjectExactCardinality ce) {
		processExactCardinalityRestriction(ce);
	}

    @Override
    public void visit(OWLDataExactCardinality ce) {
		processExactCardinalityRestriction(ce);
	}

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
		
		postProcess(axiom, groupGraphPattern);
	}

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
		
		postProcess(axiom, groupGraphPattern);
	}

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
		
		postProcess(axiom, groupGraphPattern);
    }

    private <T extends OWLCardinalityRestriction, OWLQuantifiedRestriction> void processExactCardinalityRestriction(T ce) {
		if(!preprocess()) return;

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
		
		postProcess(ce, groupGraphPattern);
	}
    
    private <T extends OWLCardinalityRestriction, OWLQuantifiedRestriction> void processMaxCardinalityRestriction(T ce) {
		if(!preprocess()) return;

		// create unique names for all used variables
		String subclassVar = classVarGenerator.newVar();

		// create the query's graph pattern
		String groupGraphPattern = 
  				  createClassExpressionGraphPattern(((OWLSubClassOfAxiom) this.getCurrentAxiom()).getSubClass(), subclassVar)
  				+ createPropertyTypeAndNotEqualVarPairsGraphPattern(ce.getCardinality()+1, subclassVar, ce.getProperty(), ce.getFiller());
		
		postProcess(ce, groupGraphPattern);
	}
    
    private <T extends OWLCardinalityRestriction, OWLQuantifiedRestriction> void processMinCardinalityRestriction(T ce) {
		this.processQuantifiedRestriction(ce, ce.getCardinality());
	}
    
    private void processQuantifiedRestriction(OWLQuantifiedRestriction ce, int cardinality) {
		if(!preprocess()) return;

		// create unique names for all used variables
		String subclassVar = classVarGenerator.newVar();

		// create the query's graph pattern
		String groupGraphPattern = 
  				  createClassExpressionGraphPattern(((OWLSubClassOfAxiom) this.getCurrentAxiom()).getSubClass(), subclassVar)
  				+ openFNEBlock()
  				+ createPropertyTypeAndNotEqualVarPairsGraphPattern(cardinality, subclassVar, ce.getProperty(), ce.getFiller())
  				+ closeBlock();
		
		postProcess(ce, groupGraphPattern);
	}
    
	private void postProcess(Object axiom, String groupGraphPattern)
	{
		String query = createPrettyQuery(groupGraphPattern);
    	queries.add(new QueryConstraint(axiom.toString(), query));
	}

	private boolean preprocess()
	{
		reset();
		return checkPreconditions();
	}
    
    private String createPropertyTypeAndNotEqualVarPairsGraphPattern(int numOfLoops, String subclassVar, OWLPropertyExpression property, OWLObject filler)
	{
		List<String> freshVars = new ArrayList<String>();
		String result = 	createPropertyAndTypeGraphPattern(numOfLoops, subclassVar, property, filler, freshVars)
  							+ createNotEqualVarPairs(freshVars);
		return result;
	}
    
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
	
	private String openUnionBlock()
	{
		return "UNION {\n";
	}

	private String openBlock() {
		return "{";
	}

	private String closeBlock() {
		return "}";
	}

	private String closeParentheses() {
		return ")";
	}

	private String openFNEBlock()
	{
		return "FILTER NOT EXISTS {\n";
	}

	private String openFilterBlock()
	{
		return "FILTER (\n";
	}

	private String createClassExpressionGraphPattern(OWLClassExpression classExpression, String projectionVariable) {
		return ceConverter.asGroupGraphPattern(classExpression, projectionVariable);
	}
	
	private String createDataRangeGraphPattern(OWLDataRange dataRange, String projectionVariable) {
		return ceConverter.asGroupGraphPattern(dataRange, projectionVariable);
	}
	
	private String createPropertyExpressionGraphPattern(String domain, OWLProperty property, String range) {
		return domain + " <" + property.toStringID() + "> " + range + " .\n";
	}
	
	private String createPrettyQuery(String groupGraphPattern) {
		String query = prettyPrint(queryTemplate.replace(this.groupGraphPatternTag, groupGraphPattern));
		System.out.println(query);
		return query;
	}

	private void reset() {
		// re-init var generators
		classVarGenerator = new VarGenerator("x");
		datatypeVarGenerator = new VarGenerator("d");
	}

	private boolean checkPreconditions() {
		return !this.axiomAlreadyVisited();
	}

    @Override
    protected void handleDefault(OWLObject axiom) {
		System.out.println("NOT SUPPORTED: " + axiom + ", " + axiom.getClass().getSimpleName());
	}

    private boolean axiomAlreadyVisited() {
		// if current axiom has been visited
		if(visitedAxioms.contains(this.getCurrentAxiom()))
		{	// this means that we are visiting an "internal" restriction of an outer "complex" restriction
			// which has been already converted.
			System.out.println("The current axiom is considered as an \"internal\" expression of an outer \"complex\" expression, ignoring...");
			return true;
		}
		// new axiom, add it to visitedAxioms and continue
		visitedAxioms.add(this.getCurrentAxiom());
		return false;
	}
    
	protected String prettyPrint(String query)
	{
		// build an Apache Jena Query from the String which is already pretty printed.
		Query sparqlQuery = QueryFactory.create(query);
		return sparqlQuery.toString();
	}

	public List<QueryConstraint> getQueries()
	{
		return this.queries;
	}

}
