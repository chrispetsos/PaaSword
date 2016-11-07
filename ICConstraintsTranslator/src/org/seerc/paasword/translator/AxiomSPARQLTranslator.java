package org.seerc.paasword.translator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLOntologyWalker;

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
	
	public List<QueryConstraint> convertToSPARQLDCQnot()
	{
		List<QueryConstraint> queries = new ArrayList<QueryConstraint>();

		OWLOntologyWalker walker = new OWLOntologyWalker(Collections.singleton(ontology));
		AxiomSPARQLOntologyWalkerVisitor visitor = new AxiomSPARQLOntologyWalkerVisitor(walker);
        // Now ask the walker to walk over the ontology structure using our
        // visitor instance.
        walker.walkStructure(visitor);
        
		return visitor.getQueries();
	}
}
