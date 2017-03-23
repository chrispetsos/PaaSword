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

/**
 * AxiomSPARQLTranslator uses a AxiomSPARQLOntologyWalkerVisitor internally to convert 
 * an OWL ontology to DCQnot SPARQL Integrity Constraints.
 * 
 * @author Chris Petsos
 *
 */
public class AxiomSPARQLTranslator {

	OWLOntologyManager manager;
	OWLOntology ontology;

	/**
	 * Generates the AxiomSPARQLTranslator and the internal OWL ontology that it
	 * operates on.
	 * 
	 * @param axioms The ontology as an InputStream.
	 */
	public AxiomSPARQLTranslator(InputStream axioms)
	{
		manager = OWLManager.createOWLOntologyManager();
		try {
			ontology = manager.loadOntologyFromOntologyDocument(new StreamDocumentSource(axioms));
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Walks the currently loaded ontology, visits axioms and generates the query
	 * contraints.
	 *  
	 * @return A list of QueryConstraints.
	 */
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
