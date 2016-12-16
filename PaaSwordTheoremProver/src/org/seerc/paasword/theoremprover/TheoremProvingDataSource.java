package org.seerc.paasword.theoremprover;

import java.io.InputStream;

import org.seerc.paasword.validator.engine.JenaDataSourceInferred;

import com.hp.hpl.jena.ontology.OntModel;

public class TheoremProvingDataSource extends JenaDataSourceInferred {
	
	public TheoremProvingDataSource(InputStream is) {
		super(is);
	}

	@Override
	public void setModel(OntModel model) {
		super.setModel(model);
		
		// enhance the model with tautologies
		TautologyChecker tc = new TautologyChecker(this);
		tc.enhanceModel();
	}
}
