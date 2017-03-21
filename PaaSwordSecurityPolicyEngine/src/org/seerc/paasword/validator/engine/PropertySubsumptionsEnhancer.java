package org.seerc.paasword.validator.engine;

import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class PropertySubsumptionsEnhancer implements JenaModelEnhancer {

	// The data source
	protected JenaDataSourceInferred jdsi;

	public PropertySubsumptionsEnhancer(JenaDataSourceInferred jdsi)
	{
		this.jdsi = jdsi;
	}

	/*
	 * Generates otp:subsumes connections for given classes' individuals' uris which are
	 * subclasses.
	 * 
	 * (non-Javadoc)
	 * @see org.seerc.paasword.validator.engine.JenaModelEnhancer#enhanceModel()
	 */
	public void enhanceModel()
	{
		this.generatePropertySubsumptionStatements("pac:RuleAntecedent");
	}
	
	/*
	 * Generated restriction statements for all individuals of uris.
	 */
	private void generatePropertySubsumptionStatements(String... uris)
	{
		for(String uri:uris)
		{
			// Get all the individuals of the uri class.
			List<Individual> individualsIterator = ((OntModel)this.jdsi.getModel()).listIndividuals(this.jdsi.createResourceFromUri(uri)).toList();
			
			// Iterate over them. 
			for(int i=0;i<individualsIterator.size();i++)
			{
				Individual i1 = individualsIterator.get(i);
				// declare it as class - TODO: is this needed?
				//((OntModel)this.jdsi.getModel()).add(i1, ResourceFactory.createProperty(jdsi.createResourceFromUri("rdf:type").getURI()), jdsi.createResourceFromUri("owl:Class"));
				for(int j=0;j<individualsIterator.size();j++)
				{
					Individual i2 = individualsIterator.get(j);
					// declare it as class - TODO: is this needed?
					//((OntModel)this.jdsi.getModel()).add(i2, ResourceFactory.createProperty(jdsi.createResourceFromUri("rdf:type").getURI()), jdsi.createResourceFromUri("owl:Class"));
					
					// if i1 is subclass of i2, add otp:subsumes connection
					if(i1.canAs(OntClass.class) && i2.canAs(OntClass.class) && i1.asClass().hasSuperClass(i2))
					{
						((OntModel)this.jdsi.getModel()).add(i1, ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:subsumes").getURI()), i2);
					}
				}
			}
		}
		
	}
}
