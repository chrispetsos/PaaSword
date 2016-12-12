package org.seerc.paasword.theoremprover;

import java.io.ByteArrayInputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.HashMap;

import org.seerc.paasword.validator.engine.JenaDataSourceInferred;
import org.snim2.checker.test.CheckerTestHelper;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
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
		String implicationsOfCe1 = this.generateImplications(ce1);
		
		String propositionToCheck = "";
		
		if(!implicationsOfCe1.isEmpty())
		{
			propositionToCheck += "( " + implicationsOfCe1 + " ) => ";
		}
		
		propositionToCheck += "( " + propositionalExpressionCe1 + " => " + propositionalExpressionCe2 + " )";

		try {
			return checker.checkInputStream(new ByteArrayInputStream(propositionToCheck.getBytes()));
		} catch (IOError | IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private String generateImplications(String resourceUri)
	{
		String result = "";
		Resource resource = jdsi.createResourceFromUri(resourceUri);
		StmtIterator resourceParams = resource.listProperties(ResourceFactory.createProperty(jdsi.createResourceFromUri("pac:hasParameter").getURI()));
		boolean firstImplication = true;
		
		while(resourceParams.hasNext())
		{
			RDFNode param = resourceParams.next().getObject();
			StmtIterator subsumedNodes = param.asResource().listProperties(ResourceFactory.createProperty(jdsi.createResourceFromUri("pac:subsumes").getURI()));
			while(subsumedNodes.hasNext())
			{
				RDFNode subsumedNode = subsumedNodes.next().getObject();
				String keyVar = this.getVariableFor(param.toString());
				String implicationVar = this.getVariableFor(subsumedNode.toString());
				if(!firstImplication)
				{
					result += " AND ";
				}
				result += "( " + keyVar + " => " + implicationVar + " )";
				firstImplication = false;
			}
		}

		return result;
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
					result = this.generateBooleanBlock(resourceParams, "AND", result);
				}
				else if(resource.as(Individual.class).hasOntClass(jdsi.createResourceFromUri("pac:ORContextExpression").getURI()))
				{	// pac:ORContextExpression
					result = this.generateBooleanBlock(resourceParams, "OR", result);
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

			private String generateBooleanBlock(StmtIterator resourceParams, String operator, String result) {
				// open parentheses
				result += "( ";
				// add first param
				result += resourceParams.next().getObject().visitWith(this);
				// for all other params
				while(resourceParams.hasNext())
				{
					// add AND
					result += " " + operator + " ";
					// add next param
					result += resourceParams.next().getObject().visitWith(this);
				}
				result += " )";
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
