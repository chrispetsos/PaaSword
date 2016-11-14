package org.seerc.paasword.validator.query;

import java.io.InputStream;

import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.seerc.paasword.validator.engine.JenaDataSource;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class JenaDataSourceInferred extends JenaDataSource {

	public JenaDataSourceInferred(OntModel model) {
		super(model);
	}
	
	public JenaDataSourceInferred(InputStream is) 
	{
		super(is);
	}

	@Override
	public void setModel(OntModel model) {
		super.setModel(ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, model));
	}

}
