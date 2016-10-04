package org.semanticweb.owlapi.examples;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

/**
 * Visits existential restrictions and collects the properties which are
 * restricted.
 */
public class RestrictionVisitor extends OWLClassExpressionVisitorAdapter {

	@Nonnull
    private final Set<OWLClass> processedClasses;
    private final Set<OWLOntology> onts;

    public RestrictionVisitor(Set<OWLOntology> onts) {
        processedClasses = new HashSet<OWLClass>();
        this.onts = onts;
    }

    @Override
    public void visit(OWLClass ce) {
        if (!processedClasses.contains(ce)) {
            // If we are processing inherited restrictions then we
            // recursively visit named supers. Note that we need to keep
            // track of the classes that we have processed so that we don't
            // get caught out by cycles in the taxonomy
            processedClasses.add(ce);
            for (OWLOntology ont : onts) {
                for (OWLSubClassOfAxiom ax : ont.getSubClassAxiomsForSubClass(ce)) {
                    ax.getSuperClass().accept(this);
                }
            }
        }
    }

    @Override
    public void visit(@Nonnull OWLObjectSomeValuesFrom ce) {
        // This method gets called when a class expression is an existential
        // (someValuesFrom) restriction and it asks us to visit it
    	int i=0;
    }
}
