package org.seerc.paasword.theoremprover;

import java.io.ByteArrayInputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.List;

import org.seerc.paasword.validator.query.JenaDataSourceInferred;
import org.snim2.checker.test.CheckerTestHelper;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class TautologyChecker {

	private JenaDataSourceInferred jdsi;
	private CheckerTestHelper checker;

	public TautologyChecker(JenaDataSourceInferred jdsi)
	{
		this.jdsi = jdsi;
		checker = new CheckerTestHelper();
	}

	public boolean isTautology(String ce1, String ce2)
	{
		String propositionalExpressionCe1 = this.convertToPropositionalExpression(ce1);
		String propositionalExpressionCe2 = this.convertToPropositionalExpression(ce2);
		
		String propositionToCheck = propositionalExpressionCe1 + " => " + propositionalExpressionCe2;

		try {
			return checker.checkInputStream(new ByteArrayInputStream(propositionToCheck.getBytes()));
		} catch (IOError | IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private String convertToPropositionalExpression(String resourceUri)
	{
		Resource resource = jdsi.createResourceFromUri(resourceUri);
		List<RDFNode> resourceParams = jdsi.executeQuery("{<" + resource.getURI() + "> pac:hasParameter ?var}");
		return "";
	}

}
