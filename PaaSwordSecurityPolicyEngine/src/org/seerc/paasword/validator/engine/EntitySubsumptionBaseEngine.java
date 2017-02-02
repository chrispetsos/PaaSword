package org.seerc.paasword.validator.engine;

import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Base abstract class for all classes that want to add subsumption facilities to a 
 * Jena Model. Uses the "otp" namespace as reference. 
 * 
 * @author Chris Petsos
 *
 */
public abstract class EntitySubsumptionBaseEngine {
	
	// The data source
	protected JenaDataSourceInferred jdsi;

	public EntitySubsumptionBaseEngine(JenaDataSourceInferred jdsi)
	{
		this.jdsi = jdsi;
	}
	
	/*
	 * Enhances the current data source's model by adding the subsumptions that the implementing class has found.
	 */
	public void enhanceModel()
	{
		// Get all the individuals of the otp:TheoremProvingBaseClass.
		List<Individual> individualsIterator = ((OntModel)this.jdsi.getModel()).listIndividuals(this.jdsi.createResourceFromUri("otp:TheoremProvingBaseClass")).toList();
		
		// Iterate over them pair-wise. 
		for(Individual i1:individualsIterator)
		{
			for(Individual i2:individualsIterator)
			{
				// Does the one subsume the other?
				if(this.entitySubsumes(i1.getURI(), i2.getURI()))
				{
					// Add the subsumption inference in the model.
					this.addSubsumption(i1.getURI(), i2.getURI());
				}
			}
		}
	}

	/*
	 * Answers whether resource1 subsumes resource2.
	 */
	protected boolean resourceSubsumes(Resource resource1, Resource resource2) {
		return resource1.listProperties(ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:subsumes").getURI())).toList().contains(
				ResourceFactory.createStatement(resource1, 
				ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:subsumes").getURI()), 
				resource2)
				);
	}
	
	protected abstract boolean entitySubsumes(String entity1Uri, String entity2Uri);

	protected abstract void addSubsumption(String entity1Uri, String entity2Uri);
}
