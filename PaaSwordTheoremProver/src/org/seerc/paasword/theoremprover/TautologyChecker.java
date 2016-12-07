package org.seerc.paasword.theoremprover;

import java.io.ByteArrayInputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.HashMap;

import org.seerc.paasword.validator.query.JenaDataSourceInferred;
import org.snim2.checker.test.CheckerTestHelper;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class TautologyChecker {

	private JenaDataSourceInferred jdsi;
	private CheckerTestHelper checker;
	private HashMap<String, String> resourceVariableMap;

	public TautologyChecker(JenaDataSourceInferred jdsi)
	{
		this.jdsi = jdsi;
		checker = new CheckerTestHelper();
		resourceVariableMap = new HashMap<String, String>();
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

	public String convertToPropositionalExpression(String resourceUri)
	{
		Resource rootResource = jdsi.createResourceFromUri(resourceUri);
		// List<RDFNode> resourceParams = jdsi.executeQuery("{<" + resource.getURI() + "> pac:hasParameter ?var}");
		return rootResource.visitWith(new RDFVisitor() {
			
			@Override
			public Object visitURI(Resource resource, String arg1) {
				StmtIterator resourceParams = resource.listProperties(ResourceFactory.createProperty(jdsi.createResourceFromUri("pac:hasParameter").getURI()));

				String result = "";
				
				if(resource.as(Individual.class).hasOntClass(jdsi.createResourceFromUri("pac:ANDContextExpression").getURI()))
				{	// pac:ANDContextExpression
					// open parentheses
					result += "( ";
					// add first param
					result += resourceParams.next().getObject().visitWith(this);
					// for all other params
					while(resourceParams.hasNext())
					{
						// add AND
						result += " AND ";
						// add next param
						result += resourceParams.next().getObject().visitWith(this);
					}
					result += " )";
				}
				else if(resource.as(Individual.class).hasOntClass(jdsi.createResourceFromUri("pac:ORContextExpression").getURI()))
				{	// pac:ORContextExpression
					// open parentheses
					result += "( ";
					// add first param
					result += resourceParams.next().getObject().visitWith(this);
					// for all other params
					while(resourceParams.hasNext())
					{
						// add AND
						result += " OR ";
						// add next param
						result += resourceParams.next().getObject().visitWith(this);
					}
					result += " )";
				}
				else if(resource.as(Individual.class).hasOntClass(jdsi.createResourceFromUri("pac:XORContextExpression").getURI()))
				{	// pac:XORContextExpression
					// TODO: Add support for XOR
					
				}
				else if(resource.as(Individual.class).hasOntClass(jdsi.createResourceFromUri("pac:NOTContextExpression").getURI()))
				{	// pac:NOTContextExpression
					// TODO: Add support for NOT
				}
				else
				{	// "terminating" param
					return TautologyChecker.this.getVariableFor(arg1);
				}
				
				return result;
			}
			
			@Override
			public Object visitLiteral(Literal arg0) {
				// TODO: Check cases of Literals
				return "";
			}
			
			@Override
			public Object visitBlank(Resource arg0, AnonId arg1) {
				// TODO: Check cases of bnodes
				// pass to URI manipulation
				return this.visitURI(arg0, arg1.toString());
			}
		}).toString();
	}

	protected String getVariableFor(String resourceId)
	{
		// TODO: Should enhance this, so that we not only look for key being the resourceId,
		// but also if the resourceId is subsumed (in any way this is expressed) by some key.
		if(resourceVariableMap.containsKey(resourceId))
		{
			return resourceVariableMap.get(resourceId);
		}
		else
		{
			Resource resource = jdsi.createResourceFromUri(resourceId);
			String newVariable = jdsi.getPrefixes().getKey(resource.getNameSpace()) + resource.getLocalName();
			resourceVariableMap.put(resourceId, newVariable);
			return newVariable;
		}
		
	}

}
