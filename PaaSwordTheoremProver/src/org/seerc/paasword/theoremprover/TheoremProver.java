package org.seerc.paasword.theoremprover;

import java.io.InputStream;

import org.seerc.paasword.validator.query.JenaDataSourceInferred;

import com.hp.hpl.jena.rdf.model.Resource;

public class TheoremProver {

	JenaDataSourceInferred jdsi;
	TautologyChecker tc;
	
	public TheoremProver(InputStream ontology)
	{
		jdsi = new JenaDataSourceInferred(ontology);
		tc = new TautologyChecker();
	}
	
	public boolean contextExpressionSubsumes(String ce1, String ce2)
	{
		Resource ce1Resource = jdsi.createResourceFromUri(ce1);
		Resource ce2Resource = jdsi.createResourceFromUri(ce2);
		
		return tc.isTautology(ce1Resource, ce2Resource);
	}

}
