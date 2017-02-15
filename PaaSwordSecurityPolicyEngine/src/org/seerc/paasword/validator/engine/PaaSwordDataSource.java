package org.seerc.paasword.validator.engine;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PaaSwordDataSource extends JenaDataSourceInferred implements JenaModelEnhancer {

	List<JenaModelEnhancer> enhancers;
	
	public PaaSwordDataSource(InputStream is) {
		super(is);
		
		enhancers = new ArrayList<JenaModelEnhancer>();
		
		// register the SubclassSubsumptionsEngine enhancer
		this.enhancers.add(new SubclassSubsumptionsEngine(this));
		// register the RuleAntecedentConclusionEnhnacer enhancer
		this.enhancers.add(new RuleAntecedentConclusionEnhnacer(this));

		// enhance model
		this.enhanceModel();
	}

	@Override
	public void enhanceModel() {
		// enhance the model with all enhancers
		for(JenaModelEnhancer jme:this.enhancers)
		{
			jme.enhanceModel();
		}
	}

}
