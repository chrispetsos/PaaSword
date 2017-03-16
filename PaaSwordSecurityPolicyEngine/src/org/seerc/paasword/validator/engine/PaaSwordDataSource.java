package org.seerc.paasword.validator.engine;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PaaSwordDataSource extends JenaDataSourceInferred implements JenaModelEnhancer {

	List<JenaModelEnhancer> enhancers;
	
	public PaaSwordDataSource(InputStream is) {
		super(is);
		
		enhancers = new ArrayList<JenaModelEnhancer>();
		
		// NOTE!!! The order of adding these makes a difference !!!

		// register the RuleAntecedentConclusionEnhnacer enhancer
		this.enhancers.add(new RuleAntecedentConclusionEnhnacer(this));
		
		// register the PolicyRulesOrderEnhancer enhancer
		// This is very naive. Replace it with SWRL rules that enforce ordering
		// based on combining algorithm
		//this.enhancers.add(new PolicyRulesOrderEnhancer(this));
		
		// register the SubclassSubsumptionsEngine enhancer
		this.enhancers.add(new SubclassSubsumptionsEngine(this));

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
