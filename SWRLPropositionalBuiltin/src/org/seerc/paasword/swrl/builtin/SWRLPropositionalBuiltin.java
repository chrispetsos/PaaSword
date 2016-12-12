package org.seerc.paasword.swrl.builtin;

import org.mindswap.pellet.ABox;
import org.mindswap.pellet.Node;
import org.seerc.paasword.theoremprover.TautologyChecker;
import org.seerc.paasword.validator.engine.JenaDataSourceInferred;

import cz.makub.swrl.CustomSWRLBuiltin.CustomSWRLFunction;

public class SWRLPropositionalBuiltin implements CustomSWRLFunction {

	TautologyChecker tc;
	
	public SWRLPropositionalBuiltin(JenaDataSourceInferred jdsi)
	{
		this.tc = new TautologyChecker(jdsi);
	}
	
	@Override
	public boolean apply(ABox abox, Node[] args) {
		// Accepts (individual1, individual2)
		String individual1Iri = args[0].getNameStr();
		String individual2Iri = args[1].getNameStr();

		return this.tc.isTautology(individual1Iri, individual2Iri);
	}

	@Override
	public boolean isApplicable(boolean[] boundPositions) {
		//applicable only to 2 arguments, both bound
		return boundPositions.length == 2 && boundPositions[0] && boundPositions[1]; 
	}

}
