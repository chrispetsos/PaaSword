package org.seerc.paasword.translator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aksw.owl2sparql.OWLClassExpressionToSPARQLConverter;
import org.aksw.owl2sparql.OWLObjectPropertyExpressionConverter;
import org.aksw.owl2sparql.util.VarGenerator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
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
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLPropertyExpression;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

public class AxiomSPARQLTranslator {

	OWLOntologyManager manager;
	OWLOntology ontology;
	OWLClassExpressionToSPARQLConverter ceConverter;
	OWLObjectPropertyExpressionConverter opConverter;
	VarGenerator classVarGenerator;
	VarGenerator datatypeVarGenerator;

	private String groupGraphPatternTag = "<groupGraphPattern>";
	private String queryTemplate = 
			  "SELECT DISTINCT *\n"
			+ "WHERE {\n"
			+ groupGraphPatternTag + "\n"
			+ "}";
	
	public AxiomSPARQLTranslator(InputStream axioms)
	{
		manager = OWLManager.createOWLOntologyManager();
		try {
			ontology = manager.loadOntologyFromOntologyDocument(new StreamDocumentSource(axioms));
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		classVarGenerator = new VarGenerator("x");
		datatypeVarGenerator = new VarGenerator("d");
		ceConverter = new OWLClassExpressionToSPARQLConverter();
		opConverter = new OWLObjectPropertyExpressionConverter();
	}
	
	public List<QueryConstraint> convertToSPARQLDCQnot()
	{
		List<QueryConstraint> queries = new ArrayList<QueryConstraint>();

		OWLOntologyWalker walker = new OWLOntologyWalker(Collections.singleton(ontology));
		OWLOntologyWalkerVisitor visitor = new OWLOntologyWalkerVisitor(walker) {

			List<OWLAxiom> visitedAxioms = new ArrayList<OWLAxiom>();
			
            @Override
            public void visit(OWLObjectSomeValuesFrom ce) {
    			if(!checkPreconditions()) return;
    			
    			reset();

        		// create unique names for all used variables
        		String subclassVar = classVarGenerator.newVar();
        		String fillerVar = classVarGenerator.newVar();

        		// create the query's graph pattern
        		String restrictedClassGraphPattern = ceConverter.asGroupGraphPattern(((OWLSubClassOfAxiom) this.getCurrentAxiom()).getSubClass(), subclassVar);
        		String onProperty = "<" + opConverter.visit(ce.getProperty().asOWLObjectProperty()) + ">";
				String fillerGraphPattern = ceConverter.asGroupGraphPattern(ce.getFiller(), fillerVar);
        		
        		String groupGraphPattern = 
        				restrictedClassGraphPattern +
        				  "FILTER NOT EXISTS {\n"
        				+ subclassVar + " " + onProperty + " " + fillerVar + " .\n"
						+ fillerGraphPattern
						+"}";
        		
        		String query = createPrettyQuery(groupGraphPattern);
        		
            	queries.add(new QueryConstraint(ce.toString(), query));
            }

            @Override
            public void visit(OWLDataSomeValuesFrom ce) {
    			if(!checkPreconditions()) return;
    			
    			reset();
    			
        		// create unique names for all used variables
        		String subclassVar = classVarGenerator.newVar();
        		String fillerVar = datatypeVarGenerator.newVar();

        		// create the query's graph pattern
        		String restrictedClassGraphPattern = ceConverter.asGroupGraphPattern(((OWLSubClassOfAxiom) this.getCurrentAxiom()).getSubClass(), subclassVar);
        		String onProperty = "<" + opConverter.visit(ce.getProperty().asOWLDataProperty()) + ">";
				String fillerGraphPattern = ceConverter.asGroupGraphPattern(ce.getFiller(), fillerVar);
        		
        		String groupGraphPattern = 
        				restrictedClassGraphPattern +
        				  "FILTER NOT EXISTS {\n"
        				+ subclassVar + " " + onProperty + " " + fillerVar + " .\n"
        				+ "FILTER ((\n"
						+ fillerGraphPattern + "\n"
						+ "))\n"
						+"}";
        		
        		String query = createPrettyQuery(groupGraphPattern);
        		
            	queries.add(new QueryConstraint(ce.toString(), query));
            }

        	public void visit(OWLObjectMinCardinality ce) {
    			if(!checkPreconditions()) return;

    			reset();

        		// create unique names for all used variables
        		String subclassVar = classVarGenerator.newVar();

        		// create the query's graph pattern
        		String restrictedClassGraphPattern = ceConverter.asGroupGraphPattern(((OWLSubClassOfAxiom) this.getCurrentAxiom()).getSubClass(), subclassVar);
        		String filterNotExistsGraphPattern = "FILTER NOT EXISTS {\n";
        		List<String> freshVars = new ArrayList<String>();
        		filterNotExistsGraphPattern += createFNEPropertyAndTypeGraphPattern(ce.getCardinality(), subclassVar, ce.getProperty(), ce.getFiller(), freshVars);
        		filterNotExistsGraphPattern += createNotEqualVarPairs(freshVars);
        		filterNotExistsGraphPattern += "\n}";
        		
        		String groupGraphPattern = 
        				restrictedClassGraphPattern +
        				filterNotExistsGraphPattern;
        		
        		String query = createPrettyQuery(groupGraphPattern);
        		
            	queries.add(new QueryConstraint(ce.toString(), query));
        	}

			public String createNotEqualVarPairs(List<String> freshVars) {
				String result = "";
				for(int j=0;j<freshVars.size();j++)
        		{
        			for(int i=0;i<j;i++)
        			{
        				result += 
	        					"FILTER (" +
    							freshVars.get(i) + " != " + freshVars.get(j) + 
    							")";
        			}
        		}
				return result;
			}

			public String createFNEPropertyAndTypeGraphPattern(
					int numOfLoops, String subclassVar,
					OWLPropertyExpression property, OWLObject filler, List<String> freshVars) {
				String result = "";
				for(int i=0;i<numOfLoops;i++)
        		{
        			String onProperty = "";
        			if(property.isObjectPropertyExpression())
        			{
            			String freshVar = classVarGenerator.newVar();
            			freshVars.add(freshVar);
        				onProperty = "<" + opConverter.visit(((OWLObjectPropertyExpression) property).asOWLObjectProperty()) + ">";
            			result += subclassVar + " " + onProperty + " " + freshVar + " .\n";
            			result += ceConverter.asGroupGraphPattern((OWLClassExpression) filler, freshVar);
        			}
        			else
        			{	// Datatype property
            			String freshVar = datatypeVarGenerator.newVar();
            			freshVars.add(freshVar);
        				onProperty = "<" + opConverter.visit(((OWLDataPropertyExpression) property).asOWLDataProperty()) + ">";
            			result += subclassVar + " " + onProperty + " " + freshVar + " .\n";
            			result += 	"FILTER (" + 
			    					ceConverter.asGroupGraphPattern((OWLDataRange)filler, freshVar) + "\n" + 
			    					")";
        			}
        		}
				return result;
			}

            @Override
            public void visit(OWLDataMinCardinality ce) {
    			if(!checkPreconditions()) return;

    			reset();

        		// create unique names for all used variables
        		String subclassVar = classVarGenerator.newVar();

        		// create the query's graph pattern
        		String restrictedClassGraphPattern = ceConverter.asGroupGraphPattern(((OWLSubClassOfAxiom) this.getCurrentAxiom()).getSubClass(), subclassVar);
        		String filterNotExistsGraphPattern = "FILTER NOT EXISTS {\n";
        		List<String> freshVars = new ArrayList<String>();
        		filterNotExistsGraphPattern += createFNEPropertyAndTypeGraphPattern(ce.getCardinality(), subclassVar, ce.getProperty(), ce.getFiller(), freshVars);
        		filterNotExistsGraphPattern += createNotEqualVarPairs(freshVars);
        		
        		filterNotExistsGraphPattern += "\n}";
        		
        		String groupGraphPattern = 
        				restrictedClassGraphPattern +
        				filterNotExistsGraphPattern;
        		
        		String query = createPrettyQuery(groupGraphPattern);
        		
            	queries.add(new QueryConstraint(ce.toString(), query));
            }

        	@Override
        	public void visit(OWLObjectMaxCardinality ce) {
    			if(!checkPreconditions()) return;

    			reset();

        		// create unique names for all used variables
        		String subclassVar = classVarGenerator.newVar();

        		// create the query's graph pattern
        		String restrictedClassGraphPattern = ceConverter.asGroupGraphPattern(((OWLSubClassOfAxiom) this.getCurrentAxiom()).getSubClass(), subclassVar);
        		String restOfGraphPattern = "";
        		List<String> freshVars = new ArrayList<String>();
        		restOfGraphPattern += createFNEPropertyAndTypeGraphPattern(ce.getCardinality()+1, subclassVar, ce.getProperty(), ce.getFiller(), freshVars);
        		restOfGraphPattern += createNotEqualVarPairs(freshVars);
        		
        		restOfGraphPattern += "\n";
        		
        		String groupGraphPattern = 
        				restrictedClassGraphPattern +
        				restOfGraphPattern;
        		
        		String query = createPrettyQuery(groupGraphPattern);
        		
            	queries.add(new QueryConstraint(ce.toString(), query));
        	}

            @Override
            public void visit(OWLDataMaxCardinality ce) {
    			if(!checkPreconditions()) return;

    			reset();

        		// create unique names for all used variables
        		String subclassVar = classVarGenerator.newVar();

        		// create the query's graph pattern
        		String restrictedClassGraphPattern = ceConverter.asGroupGraphPattern(((OWLSubClassOfAxiom) this.getCurrentAxiom()).getSubClass(), subclassVar);
        		String restOfGraphPattern = "";
        		List<String> freshVars = new ArrayList<String>();
        		restOfGraphPattern += createFNEPropertyAndTypeGraphPattern(ce.getCardinality()+1, subclassVar, ce.getProperty(), ce.getFiller(), freshVars);
        		restOfGraphPattern += createNotEqualVarPairs(freshVars);
        		restOfGraphPattern += "\n";
        		
        		String groupGraphPattern = 
        				restrictedClassGraphPattern +
        				restOfGraphPattern;
        		
        		String query = createPrettyQuery(groupGraphPattern);
        		
            	queries.add(new QueryConstraint(ce.toString(), query));
            }

        	@Override
        	public void visit(OWLObjectExactCardinality ce) {
    			if(!checkPreconditions()) return;

    			reset();

        		// create unique names for all used variables
        		String subclassVar = classVarGenerator.newVar();

        		// create the query's graph pattern
        		String restrictedClassGraphPattern = ceConverter.asGroupGraphPattern(((OWLSubClassOfAxiom) this.getCurrentAxiom()).getSubClass(), subclassVar);
        		String firstUnionMemberGraphPattern = "{\n";
        		List<String> freshVars = new ArrayList<String>();
        		firstUnionMemberGraphPattern += createFNEPropertyAndTypeGraphPattern(ce.getCardinality()+1, subclassVar, ce.getProperty(), ce.getFiller(), freshVars);
        		firstUnionMemberGraphPattern += createNotEqualVarPairs(freshVars);
        		firstUnionMemberGraphPattern += "\n}";
        		
        		String filterNotExistsGraphPattern = "FILTER NOT EXISTS {\n";
        		freshVars = new ArrayList<String>();
        		filterNotExistsGraphPattern += createFNEPropertyAndTypeGraphPattern(ce.getCardinality(), subclassVar, ce.getProperty(), ce.getFiller(), freshVars);
        		filterNotExistsGraphPattern += createNotEqualVarPairs(freshVars);
        		filterNotExistsGraphPattern += "\n}";
        		
        		String groupGraphPattern = 
        				restrictedClassGraphPattern +
        				firstUnionMemberGraphPattern + "\n" +
        				"UNION\n" +
        				"{\n" +
        				filterNotExistsGraphPattern +
        				"}";
        		
        		String query = createPrettyQuery(groupGraphPattern);
        		
            	queries.add(new QueryConstraint(ce.toString(), query));
        	}

            @Override
            public void visit(OWLDataExactCardinality ce) {
    			if(!checkPreconditions()) return;

    			reset();

        		// create unique names for all used variables
        		String subclassVar = classVarGenerator.newVar();

        		// create the query's graph pattern
        		String restrictedClassGraphPattern = ceConverter.asGroupGraphPattern(((OWLSubClassOfAxiom) this.getCurrentAxiom()).getSubClass(), subclassVar);
        		String onProperty = "<" + opConverter.visit(ce.getProperty().asOWLDataProperty()) + ">";
        		String firstUnionMemberGraphPattern = "{\n";
        		List<String> freshVars = new ArrayList<String>();
        		firstUnionMemberGraphPattern += createFNEPropertyAndTypeGraphPattern(ce.getCardinality()+1, subclassVar, ce.getProperty(), ce.getFiller(), freshVars);
        		firstUnionMemberGraphPattern += createNotEqualVarPairs(freshVars);
        		firstUnionMemberGraphPattern += "\n}";
        		
        		String filterNotExistsGraphPattern = "FILTER NOT EXISTS {\n";
        		freshVars = new ArrayList<String>();
        		filterNotExistsGraphPattern += createFNEPropertyAndTypeGraphPattern(ce.getCardinality(), subclassVar, ce.getProperty(), ce.getFiller(), freshVars);
        		filterNotExistsGraphPattern += createNotEqualVarPairs(freshVars);
        		filterNotExistsGraphPattern += "\n}";
        		
        		String groupGraphPattern = 
        				restrictedClassGraphPattern +
        				firstUnionMemberGraphPattern + "\n" +
        				"UNION\n" +
        				"{\n" +
        				filterNotExistsGraphPattern +
        				"}";
        		
        		String query = createPrettyQuery(groupGraphPattern);
        		
            	queries.add(new QueryConstraint(ce.toString(), query));
        	}

            @Override
        	public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
    			if(!checkPreconditions()) return;

    			reset();

        		String domain = axiom.getDomain().toString();
        		
        		// create unique names for all used variables
        		String domainVar = classVarGenerator.newVar();
        		String freshVar = classVarGenerator.newVar();
        		
        		// create the query's graph pattern
        		String onProperty = "<" + opConverter.visit(axiom.getProperty()) + ">";

        		String groupGraphPattern = 
        				domainVar + " " + onProperty + " " + freshVar + " .\n" +
        				"FILTER NOT EXISTS {\n" + 
        				domainVar + " a <" + domain + "> .\n" + 
        				"}\n";
        		
        		String query = createPrettyQuery(groupGraphPattern);
        		
            	queries.add(new QueryConstraint(axiom.toString(), query));
        	}
            
        	public void visit(OWLObjectPropertyRangeAxiom axiom) {
    			if(!checkPreconditions()) return;

    			reset();

        		// create unique names for all used variables
        		String rangeVar = classVarGenerator.newVar();
        		String freshVar = classVarGenerator.newVar();
        		
        		// create the query's graph pattern
        		String restrictedClassGraphPattern = ceConverter.asGroupGraphPattern(axiom.getRange(), rangeVar);
        		String onProperty = "<" + opConverter.visit(axiom.getProperty().asOWLObjectProperty()) + ">";

        		String groupGraphPattern = 
        				freshVar + " " + onProperty + " " + rangeVar + " .\n" +
        				"FILTER NOT EXISTS {\n" + 
        				restrictedClassGraphPattern + 
        				"}";
        		
        		String query = createPrettyQuery(groupGraphPattern);
        		
            	queries.add(new QueryConstraint(axiom.toString(), query));
        	}

            @Override
            public void visit(OWLDataPropertyRangeAxiom axiom) {
    			if(!checkPreconditions()) return;

    			reset();

        		// create unique names for all used variables
        		String rangeVar = datatypeVarGenerator.newVar();
        		String freshVar = classVarGenerator.newVar();
        		
        		// create the query's graph pattern
        		String restrictedDataRangeGraphPattern = ceConverter.asGroupGraphPattern(axiom.getRange(), rangeVar);
        		String onProperty = "<" + opConverter.visit(axiom.getProperty().asOWLDataProperty()) + ">";

        		String groupGraphPattern = 
        				freshVar + " " + onProperty + " " + rangeVar + " .\n" +
        				"FILTER (\n" + 
        				"!(" + restrictedDataRangeGraphPattern + ")\n" + 
        				")";
        		
        		String query = createPrettyQuery(groupGraphPattern);
        		
            	queries.add(new QueryConstraint(axiom.toString(), query));
            }

			public String createPrettyQuery(String groupGraphPattern) {
				String query = AxiomSPARQLTranslator.this.prettyPrint(queryTemplate.replace(AxiomSPARQLTranslator.this.groupGraphPatternTag, groupGraphPattern));
        		System.out.println(query);
				return query;
			}

			public void reset() {
				// re-init var generators
    			classVarGenerator = new VarGenerator("x");
    			datatypeVarGenerator = new VarGenerator("d");
			}

			public boolean checkPreconditions() {
				return !this.axiomAlreadyVisited();
			}

            @Override
            protected void handleDefault(OWLObject axiom) {
        		System.out.println("NOT SUPPORTED: " + axiom + ", " + axiom.getClass().getSimpleName());
        	}

            public boolean axiomAlreadyVisited() {
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
            
        };
        // Now ask the walker to walk over the ontology structure using our
        // visitor instance.
        walker.walkStructure(visitor);
        
		return queries;
	}

	protected String prettyPrint(String query)
	{
		// build an Apache Jena Query from the String which is already pretty printed.
		Query sparqlQuery = QueryFactory.create(query);
		return sparqlQuery.toString();
	}

}
