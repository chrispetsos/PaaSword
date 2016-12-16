package org.seerc.paasword.theoremprover;

import java.io.ByteArrayInputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.seerc.paasword.validator.engine.JenaDataSourceInferred;
import org.snim2.checker.test.CheckerTestHelper;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

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
		String implicationsOfCe2 = this.generateImplications(ce2);
		String allImplications = "";
		
		String propositionToCheck = "";
		
		if(implicationsOfCe1.isEmpty())
		{
			allImplications += implicationsOfCe2;
		}
		else
		{
			if(implicationsOfCe2.isEmpty())
			{
				allImplications += implicationsOfCe1;
			}
			else
			{
				allImplications += implicationsOfCe1 + " AND " + implicationsOfCe2;
			}
		}
		
		if(!allImplications.isEmpty())
		{
			propositionToCheck += "( " + allImplications + " ) => ";
		}
		
		propositionToCheck += "( " + propositionalExpressionCe2 + " => " + propositionalExpressionCe1 + " )";

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
		StmtIterator resourceParams = resource.listProperties(ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:TheoremProvingParameterProperty").getURI()));
		boolean firstImplication = true;
		
		while(resourceParams.hasNext())
		{
			RDFNode param = resourceParams.next().getObject();
			boolean isNestedNode = param.as(Individual.class).hasOntClass(jdsi.createResourceFromUri("otp:TheoremProvingBaseClass").getURI());
			if(!isNestedNode)
			{
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
					result += "( " + implicationVar + " => " + keyVar + " )";
					firstImplication = false;
				}
			}
			else
			{
				String nestedResult = this.generateImplications(param.toString());
				if(!nestedResult.isEmpty() && !firstImplication)
				{
					result += " AND ";
					result += nestedResult;
					firstImplication = false;
				}
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
				StmtIterator resourceParams = resource.listProperties(ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:TheoremProvingParameterProperty").getURI()));

				String result = "";
				
				if(resource.as(Individual.class).hasOntClass(jdsi.createResourceFromUri("otp:ANDTheoremProvingClass").getURI()))
				{	// pac:ANDContextExpression
					result = this.generateBooleanBlock(resourceParams, "AND", result);
				}
				else if(resource.as(Individual.class).hasOntClass(jdsi.createResourceFromUri("otp:ORTheoremProvingClass").getURI()))
				{	// pac:ORContextExpression
					result = this.generateBooleanBlock(resourceParams, "OR", result);
				}
				else if(resource.as(Individual.class).hasOntClass(jdsi.createResourceFromUri("otp:XORTheoremProvingClass").getURI()))
				{	// pac:XORContextExpression
					// TODO: Add support for XOR
					
				}
				else if(resource.as(Individual.class).hasOntClass(jdsi.createResourceFromUri("otp:NOTheoremProvingClass").getURI()))
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

	public void enhanceModel()
	{
		List<Individual> individualsIterator = ((OntModel)this.jdsi.getModel()).listIndividuals(this.jdsi.createResourceFromUri("otp:TheoremProvingBaseClass")).toList();
		for(Individual i1:individualsIterator)
		{
			for(Individual i2:individualsIterator)
			{
				boolean isT = this.isTautology(i1.getURI(), i2.getURI());
				if(isT)
				{
					this.jdsi.getModel().add(
							ResourceFactory.createResource(jdsi.createResourceFromUri(i1.getURI()).getURI()), 
							ResourceFactory.createProperty(jdsi.createResourceFromUri("pac:subsumes").getURI()), 
							ResourceFactory.createResource(jdsi.createResourceFromUri(i2.getURI()).getURI()) 
							);
					//this.jdsi.createResourceFromUri(i1.getURI()).addProperty(ResourceFactory.createProperty(jdsi.createResourceFromUri("pac:subsumes").getURI()), i2.getURI());
				}
			}
		}
	}

}
