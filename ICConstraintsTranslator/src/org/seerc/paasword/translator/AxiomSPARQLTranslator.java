package org.seerc.paasword.translator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;

public class AxiomSPARQLTranslator {

	OWLOntologyManager manager;
	OWLOntology ontology;
	
	public AxiomSPARQLTranslator(InputStream axioms)
	{
		manager = OWLManager.createOWLOntologyManager();
		try {
			ontology = manager.loadOntologyFromOntologyDocument(new StreamDocumentSource(axioms));
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> convertToSPARQLDCQnot()
	{
		List<String> queries = new ArrayList<String>();

		OWLOntologyWalker walker = new OWLOntologyWalker(Collections.singleton(ontology));
		OWLOntologyWalkerVisitor visitor = new OWLOntologyWalkerVisitor(walker) {

            @Override
            public void visit(OWLObjectSomeValuesFrom ce) {
        		System.out.println("Got a " + ce + ", " + ce.getClass().getSimpleName() + " !!!");
        		OWLSubClassOfAxiom axiom = null;
        		try
        		{
        			axiom = (OWLSubClassOfAxiom) this.getCurrentAxiom();
        		}
        		catch(Exception e)
        		{
            		System.out.println(ce + " is not correctly used as a restriction in a " + OWLSubClassOfAxiom.class.getSimpleName() + " axiom !!!");
        			return;
        		}
        		
        		String restrictedClass = axiom.getSubClass().toString();
        		String onProperty = ce.getProperty().toString();
        		String filler = ce.getFiller().toString();
        		System.out.println("Restricted class: " + restrictedClass);
        		System.out.println("On property: " + onProperty);
        		System.out.println("Filler class: " + filler);
        		
            	queries.add(null);
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
        		System.out.println(axiom + ", " + axiom.getClass().getSimpleName() + ", NOT SUPPORTED");
        	}
        };
        // Now ask the walker to walk over the ontology structure using our
        // visitor instance.
        walker.walkStructure(visitor);
        
		return queries;
	}

}
