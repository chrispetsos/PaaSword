package org.seerc.paasword.translator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aksw.owl2sparql.OWLAxiomToSPARQLConverter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;

public class AxiomSPARQLTranslator extends OWLAxiomToSPARQLConverter {

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

		for(OWLAxiom axiom:ontology.getAxioms())
		{
			String query = this.convert(axiom);
			System.out.println(axiom.toString());
			System.out.println(query);
			System.out.println();
			queries.add(query);
		}
		return queries;
	}
	
	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
		// process subclass
		OWLClassExpression subClass = axiom.getSubClass();
		if(!subClass.isOWLThing()){// we do not need to convert owl:Thing
			String subClassPattern = expressionConverter.asGroupGraphPattern(subClass, subjectVar);
			sparql += subClassPattern;
		}

		// process superclass
		OWLClassExpression superClass = axiom.getSuperClass();
		boolean needsOuterTriplePattern = subClass.isOWLThing() &&
				(superClass.getClassExpressionType() == ClassExpressionType.OBJECT_COMPLEMENT_OF ||
				superClass.getClassExpressionType() == ClassExpressionType.DATA_ALL_VALUES_FROM ||
				superClass.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM);
		String superClassPattern = expressionConverter.asGroupGraphPattern(superClass, subjectVar,
																		   needsOuterTriplePattern);
		sparql += this.notExists(superClassPattern);
	}

}
