package org.seerc.paasword.translator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.examples.RestrictionVisitor;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.owl.owlapi.tutorial.ClosureAxioms;

public class AxiomSPARQLTranslator implements OWLAxiomVisitorEx<String> {

	OWLOntologyManager manager;
	OWLOntology ontology;
	ClosureAxioms closureAxioms;
	
	public AxiomSPARQLTranslator(InputStream axioms)
	{
		OWLOntologyLoaderConfiguration configuration = new OWLOntologyLoaderConfiguration().setStrict(false);
		manager = OWLManager.createOWLOntologyManager();
		try {
			//ontology = manager.loadOntology(new StreamDocumentSource(axioms), configuration);
			//ontology = manager.loadOntologyFromOntologyDocument(new StreamDocumentSource(axioms), configuration);
			ontology = manager.loadOntologyFromOntologyDocument(new StreamDocumentSource(axioms));
			closureAxioms = new ClosureAxioms(manager, ontology);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> convertToSPARQLDCQnot()
	{
		List<String> queries = new ArrayList<String>();

		for(OWLSubClassOfAxiom subclassOfAxiom:ontology.getSubClassAxiomsForSuperClass(OWLDataFactoryImpl.getInstance().getOWLClass(IRI.create("owl:Restriction"))))
		{
			closureAxioms.addClosureAxioms(subclassOfAxiom.getSubClass().asOWLClass());
		}
		
		closureAxioms.addClosureAxioms(OWLDataFactoryImpl.getInstance().getOWLClass(IRI.create("owl:Restriction")));

		for(OWLAxiom axiom:ontology.getAxioms())
		{
			String query = axiom.accept(this);
			queries.add(query);
		}
		
		OWLOntologyWalker walker = new OWLOntologyWalker(Collections.singleton(ontology));
		OWLOntologyWalkerVisitor<String> visitor = new OWLOntologyWalkerVisitor<String>(walker) {

            @Override
            public String visit(OWLObjectSomeValuesFrom ce) {
                // Print out the restriction
                // System.out.println(desc);
                // Print out the axiom where the restriction is used
                // System.out.println(" " + getCurrentAxiom());
                // We don't need to return anything here.
                return "";
            }
        };
        // Now ask the walker to walk over the ontology structure using our
        // visitor instance.
        walker.walkStructure(visitor);
		return queries;
	}

	@Override
	public String visit(OWLAnnotationPropertyDomainAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLSubClassOfAxiom arg0) {
		RestrictionVisitor restrictionVisitor = new RestrictionVisitor(Collections.singleton(ontology));
		arg0.getSuperClass().accept(restrictionVisitor);
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLSubAnnotationPropertyOfAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLAnnotationPropertyRangeAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLNegativeObjectPropertyAssertionAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLAsymmetricObjectPropertyAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLReflexiveObjectPropertyAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLDisjointClassesAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLDataPropertyDomainAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLObjectPropertyDomainAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLEquivalentObjectPropertiesAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLNegativeDataPropertyAssertionAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLDifferentIndividualsAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLDisjointDataPropertiesAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLDisjointObjectPropertiesAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLObjectPropertyRangeAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLObjectPropertyAssertionAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLFunctionalObjectPropertyAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLSubObjectPropertyOfAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLDisjointUnionAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLDeclarationAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLAnnotationAssertionAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLSymmetricObjectPropertyAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLDataPropertyRangeAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLFunctionalDataPropertyAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLEquivalentDataPropertiesAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLClassAssertionAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLEquivalentClassesAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLDataPropertyAssertionAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLTransitiveObjectPropertyAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLIrreflexiveObjectPropertyAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLSubDataPropertyOfAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLInverseFunctionalObjectPropertyAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLSameIndividualAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLSubPropertyChainOfAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLInverseObjectPropertiesAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLHasKeyAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(OWLDatatypeDefinitionAxiom arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}

	@Override
	public String visit(SWRLRule arg0) {
		System.out.println(arg0 + ", " + arg0.getClass().getSimpleName() + ", NOT SUPPORTED");
		return null;
	}
}
