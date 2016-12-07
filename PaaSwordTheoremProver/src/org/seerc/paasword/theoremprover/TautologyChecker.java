package org.seerc.paasword.theoremprover;

import org.seerc.paasword.validator.query.JenaDataSourceInferred;

import com.hp.hpl.jena.rdf.model.Resource;

public class TautologyChecker {

	private JenaDataSourceInferred jdsi;

	public TautologyChecker(JenaDataSourceInferred jdsi)
	{
		this.jdsi = jdsi;
	}

	public boolean isTautology(String ce1, String ce2)
	{
		Resource ce1Resource = jdsi.createResourceFromUri(ce1);
		Resource ce2Resource = jdsi.createResourceFromUri(ce2);

		return false;
	}

}
