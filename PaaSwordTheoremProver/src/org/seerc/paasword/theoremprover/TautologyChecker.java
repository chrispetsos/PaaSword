package org.seerc.paasword.theoremprover;

import java.io.ByteArrayInputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.List;

import org.seerc.paasword.validator.query.JenaDataSourceInferred;
import org.snim2.checker.test.CheckerTestHelper;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

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
		// List<RDFNode> resourceParams = jdsi.executeQuery("{<" + resource.getURI() + "> pac:hasParameter ?var}");
		resource.visitWith(new RDFVisitor() {
			
			@Override
			public Object visitURI(Resource arg0, String arg1) {
				StmtIterator resourceParams = arg0.listProperties(ResourceFactory.createProperty(jdsi.createResourceFromUri("pac:hasParameter").getURI()));
				while(resourceParams.hasNext())
				{
					RDFNode param = resourceParams.next().getObject();
					int i=0;
				}
				return null;
			}
			
			@Override
			public Object visitLiteral(Literal arg0) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Object visitBlank(Resource arg0, AnonId arg1) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		return "";
	}

}
