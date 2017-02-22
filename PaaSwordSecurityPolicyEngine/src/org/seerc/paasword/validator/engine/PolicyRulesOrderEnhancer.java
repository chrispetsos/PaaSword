package org.seerc.paasword.validator.engine;

import java.util.Collections;
import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class PolicyRulesOrderEnhancer implements JenaModelEnhancer {

	// The data source
	protected JenaDataSourceInferred jdsi;

	public PolicyRulesOrderEnhancer(JenaDataSourceInferred jdsi)
	{
		this.jdsi = jdsi;
	}

	@Override
	public void enhanceModel() {
		// Get all Policies
		ExtendedIterator<Individual> abacPolicies = ((OntModel)jdsi.getModel()).listIndividuals(((OntModel)jdsi.getModel()).createResource("http://www.paasword.eu/security-policy/seerc/pac#ABACPolicy"));
		
		while(abacPolicies.hasNext())
		{
			// Get all Rules of Policies
			List<RDFNode> policyRules = abacPolicies.next().listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasABACRule")).toList();
			
			/*
			 * OK, Jena seems to return the elements from within the RDF serialization
			 * in the reverse order that they are added inside the RDF document.
			 * So, theoretically, reversing the returned list will put the elements
			 * in the order that they are actually added inside the RDF document. 
			 * 
			 * TODO: Is this always the case???
			 */
			Collections.reverse(policyRules);
			
			// Add hasNext relations between Rules
			// iterate until the previous to last
			for(int i=0;i<policyRules.size()-1;i++)
			{
				this.jdsi.getModel().add(policyRules.get(i).asResource(), this.jdsi.getModel().createProperty("http://www.paasword.eu/security-policy/seerc/list#hasNext"), policyRules.get(i+1));				
			}
		}
	}

}
