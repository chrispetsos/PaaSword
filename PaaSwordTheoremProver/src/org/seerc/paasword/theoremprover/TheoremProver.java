package org.seerc.paasword.theoremprover;

import java.io.InputStream;

import org.seerc.paasword.validator.engine.JenaDataSourceInferred;

/**
 * This class wraps the TautologyChecker. It may not be needed anymore.
 * @author Chris Petsos
 *
 */
// TODO: Consider whether this class can be deprecated.
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
