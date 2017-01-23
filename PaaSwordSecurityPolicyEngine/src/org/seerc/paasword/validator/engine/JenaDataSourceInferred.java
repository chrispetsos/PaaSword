package org.seerc.paasword.validator.engine;

import java.io.InputStream;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Class that extends JenaDataSource and uses the Pellet reasoner for inferences.
 * 
 * @author Chris Petsos
 *
 */
public class JenaDataSourceInferred extends JenaDataSource {

	public JenaDataSourceInferred(OntModel model) {
		super(model);
	}
	
	public JenaDataSourceInferred(InputStream is) 
	{
		super(is);
	}

	/**
	 * When setting the model wrap it with one that uses the Pellet reasoner.
	 */
	@Override
	public void setModel(OntModel model) {
		super.setModel(ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, model));
	}

}
