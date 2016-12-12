package org.seerc.paasword.theoremprover;

import java.io.InputStream;

import org.seerc.paasword.validator.engine.JenaDataSourceInferred;

public class TheoremProver {

	JenaDataSourceInferred jdsi;
	TautologyChecker tc;
	
	public TheoremProver(InputStream ontology)
	{
		jdsi = new JenaDataSourceInferred(ontology);
		tc = new TautologyChecker(jdsi);
	}
	
	public boolean contextExpressionSubsumes(String ce1, String ce2)
	{
		return tc.isTautology(ce1, ce2);
	}

}
