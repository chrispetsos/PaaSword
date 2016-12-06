package org.seerc.paasword.theoremprover;

import java.io.InputStream;
import java.util.List;

import org.seerc.paasword.validator.query.JenaDataSourceInferred;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class TheoremProver {

	JenaDataSourceInferred jdsi;
	
	public TheoremProver(InputStream ontology)
	{
		jdsi = new JenaDataSourceInferred(ontology);
	}
	
	public boolean contextExpressionSubsumes(String ce1, String ce2)
	{
		Resource ce1Resource = jdsi.createResourceFromUri(ce1);
		Resource ce2Resource = jdsi.createResourceFromUri(ce2);
		
		List<RDFNode> ce1Params = jdsi.executeQuery("{<" + ce1Resource.getURI() + "> pac:hasParameter ?var}");
		List<RDFNode> ce2Params = jdsi.executeQuery("{<" + ce2Resource.getURI() + "> pac:hasParameter ?var}");
		
		for(RDFNode ce1Param:ce1Params)
		{
			boolean paramFound = false;
			
			for(RDFNode ce2Param:ce2Params)
			{
				if(ce1Param.equals(ce2Param))
				{	
					paramFound = true;
					break;
				}
			}
			
			if(!paramFound)
			{	// equal param not found, so no subsumption
				return false;
			}
		}
		
		// all params found equal, so subsumption
		return true;
	}

}
