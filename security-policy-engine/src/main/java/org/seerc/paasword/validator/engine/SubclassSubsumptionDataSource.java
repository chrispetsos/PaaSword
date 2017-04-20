package org.seerc.paasword.validator.engine;

import java.io.InputStream;

import org.seerc.paasword.validator.engine.JenaDataSourceInferred;

import com.hp.hpl.jena.ontology.OntModel;

/**
 * This class extends the JenaDataSourceInferred and after setting the model, it also
 * adds the subsumptions that a SubclassSubsumptionsEngine has found. That is, it
 * basically translates expressions in the otp namespace to OWL Classes and performs
 * binary operations on those classes (intersection, union etc).
 * @author Chris Petsos
 *
 */
public class SubclassSubsumptionDataSource extends JenaDataSourceInferred {
	
	/**
	 * Create a SubclassSubsumptionDataSource with an InputStream ontology.
	 * @param is The ontology.
	 */
	public SubclassSubsumptionDataSource(InputStream is) {
		super(is);
	}

	/**
	 * Upon setting the model, uses a SubclassSubsumptionsEngine to add subsumptions.
	 */
	@Override
	public void setModel(OntModel model) {
		super.setModel(model);
		
		// enhance the model with subclass subsumptions
		SubclassSubsumptionsEngine sse = new SubclassSubsumptionsEngine(this);
		sse.enhanceModel();
	}
}
