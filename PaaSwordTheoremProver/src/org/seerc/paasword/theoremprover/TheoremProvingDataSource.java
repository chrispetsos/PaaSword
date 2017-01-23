package org.seerc.paasword.theoremprover;

import java.io.InputStream;

import org.seerc.paasword.validator.engine.JenaDataSourceInferred;

import com.hp.hpl.jena.ontology.OntModel;

/**
 * This class extends the JenaDataSourceInferred and after setting the model, it also
 * adds the subsumption that a TautologyChecker has found. Note that this is assuming
 * that the model behind this instance is immutable. This means that if the model
 * changes after an object of a class has been created then no new potential
 * subsumptions will be inferred. 
 * @author Chris Petsos
 *
 */
public class TheoremProvingDataSource extends JenaDataSourceInferred {
	
	/**
	 * Create a TheoremProvingDataSource with an InputStream ontology.
	 * @param is The ontology.
	 */
	public TheoremProvingDataSource(InputStream is) {
		super(is);
	}

	/**
	 * Upon setting the model, uses a TautologyChecker to add subsumptions.
	 */
	@Override
	public void setModel(OntModel model) {
		super.setModel(model);
		
		// enhance the model with tautologies
		TautologyChecker tc = new TautologyChecker(this);
		tc.enhanceModel();
	}
}
