package org.seerc.paasword.translator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aksw.owl2sparql.OWLClassExpressionToSPARQLConverter;
import org.aksw.owl2sparql.OWLObjectPropertyExpressionConverter;
import org.aksw.owl2sparql.util.VarGenerator;
import org.aksw.owl2sparql.util.VariablesMapping;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

public class AxiomSPARQLTranslator {

	OWLOntologyManager manager;
	OWLOntology ontology;
	OWLClassExpressionToSPARQLConverter ceConverter;
	OWLObjectPropertyExpressionConverter opConverter;
	VarGenerator classVarGenerator;
	
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
		ceConverter = new OWLClassExpressionToSPARQLConverter();
		opConverter = new OWLObjectPropertyExpressionConverter();
	}
	
	public List<String> convertToSPARQLDCQnot()
	{
		List<String> queries = new ArrayList<String>();

		OWLOntologyWalker walker = new OWLOntologyWalker(Collections.singleton(ontology));
		OWLOntologyWalkerVisitor visitor = new OWLOntologyWalkerVisitor(walker) {

			List<OWLAxiom> visitedAxioms = new ArrayList<OWLAxiom>();
			
            @Override
            public void visit(OWLObjectSomeValuesFrom ce) {
        		System.out.println("Got a " + ce + ", " + ce.getClass().getSimpleName() + " !!!");
        		OWLSubClassOfAxiom axiom = null;
        		try
        		{
        			axiom = (OWLSubClassOfAxiom) this.getCurrentAxiom();
        			// if current axiom has been visited
        			if(visitedAxioms.contains(axiom))
        			{	// this means that we are visiting an "internal" restriction of an outer "complex" restriction
        				// which has been already converted.
                		System.out.println("The current axiom is considered as an \"internal\" restriction of an outer \"complex\" restriction, ignoring...");
        				return;
        			}
        			// new axiom, add it to visitedAxioms and continue
        			visitedAxioms.add(axiom);
        		}
        		catch(Exception e)
        		{
            		System.out.println(ce + " is not correctly used as a restriction in a " + OWLSubClassOfAxiom.class.getSimpleName() + " axiom !!!");
        			return;
        		}
        		
        		// create unique names for all used variables
        		String subclassVar = classVarGenerator.newVar();
        		String fillerVar = classVarGenerator.newVar();

        		// create the query's graph pattern
        		String restrictedClassGraphPattern = ceConverter.asGroupGraphPattern(axiom.getSubClass(), subclassVar);
        		String onProperty = "<" + opConverter.visit(ce.getProperty().asOWLObjectProperty()) + ">";
				String fillerGraphPattern = ceConverter.asGroupGraphPattern(ce.getFiller(), fillerVar);
        		System.out.println("Restricted class graph pattern: " + restrictedClassGraphPattern);
        		System.out.println("On property: " + onProperty);
        		System.out.println("Filler class graph pattern: " + fillerGraphPattern);
        		
        		String groupGraphPattern = 
        				restrictedClassGraphPattern +
        				  "FILTER NOT EXISTS {\n"
        				+ subclassVar + " " + onProperty + " " + fillerVar + " .\n"
						+ fillerGraphPattern
						+"}";
        		
        		String query = AxiomSPARQLTranslator.this.prettyPrint(queryTemplate.replace(AxiomSPARQLTranslator.this.groupGraphPatternTag, groupGraphPattern));
        		System.out.println(query);
        		
            	queries.add(query);
            }
            
            @Override
        	public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
        		System.out.println("Got a " + axiom + ", " + axiom.getClass().getSimpleName() + " !!!");
        		
        		String property = axiom.getProperty().toString();
        		String domain = axiom.getDomain().toString();
        		System.out.println("Property: " + property);
        		System.out.println("Domain: " + domain);
        		
            	queries.add(null);
        	}
            
            @Override
            protected void handleDefault(OWLObject axiom) {
        		System.out.println("NOT SUPPORTED: " + axiom + ", " + axiom.getClass().getSimpleName());
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
