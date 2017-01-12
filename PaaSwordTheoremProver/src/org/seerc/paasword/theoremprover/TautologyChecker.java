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

/**
 * This class takes a JenaDataSourceInferred and checks for tautologies inside
 * its ontologies, getting instructed by the classes in the "otp" namespace.
 * Having found the tautologies it can infer which OWL entities subsume others.
 * 
 * @author Chris Petsos
 *
 */
public class TautologyChecker {

	// The data source
	private JenaDataSourceInferred jdsi;
	// The encapsulated checker that asserts tautologies
	private CheckerTestHelper checker;
	// A Map from resources to variables.
	private HashMap<String, String> resourceVariableMap;

	/**
	 * Constructs a TautologyChecker object.
	 * 
	 * @param jdsi The data source.
	 */
	public TautologyChecker(JenaDataSourceInferred jdsi)
	{
		this.jdsi = jdsi;
		checker = new CheckerTestHelper();
		resourceVariableMap = new HashMap<String, String>();
	}

	/**
	 * Takes two URIs of OWL resources and replies whether the first subsumes the
	 * second.
	 * 
	 * @param ce1 The first OWL resource.
	 * @param ce2 The second OWL resource.
	 * @return True if the first subsumes the second, otherwise false.
	 */
	public boolean isTautology(String ce1, String ce2)
	{
		// Convert the OWL expressions to propositional statements
		String propositionalExpressionCe1 = this.convertToPropositionalExpression(ce1);
		String propositionalExpressionCe2 = this.convertToPropositionalExpression(ce2);
		
		// Calculate implications based on subsumptions
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
		
		// Build the propositional formulae to check 
		propositionToCheck += "( " + propositionalExpressionCe2 + " => " + propositionalExpressionCe1 + " )";

		try {
			// Check it!
			return checker.checkInputStream(new ByteArrayInputStream(propositionToCheck.getBytes()));
		} catch (IOError | IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * Generates the implications of a resource.
	 */
	private String generateImplications(String resourceUri)
	{
		String result = "";
		
		// Create the Resource object.
		Resource resource = jdsi.createResourceFromUri(resourceUri);
		
		// Get the statements where the Resource is subject of a "otp:TheoremProvingParameterProperty" parameter.
		StmtIterator resourceParams = resource.listProperties(ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:TheoremProvingParameterProperty").getURI()));
		
		// Flag used to assert if the current implication is the first one, as to add binary operator etc. or not.
		boolean firstImplication = true;
		
		// Iterate over the statements
		while(resourceParams.hasNext())
		{
			RDFNode param = resourceParams.next().getObject();
			
			// Is this a nested node?
			boolean isNestedNode = param.as(Individual.class).hasOntClass(jdsi.createResourceFromUri("otp:TheoremProvingBaseClass").getURI());
			if(!isNestedNode)
			{
				// If not, get the statements that describe which nodes this node subusumes.
				StmtIterator subsumedNodes = param.asResource().listProperties(ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:subsumes").getURI()));
				// Iterate over subsumed nodes.
				while(subsumedNodes.hasNext())
				{
					RDFNode subsumedNode = subsumedNodes.next().getObject();
					// Create or get variables for "key" node and subsumed node. 
					String keyVar = this.getVariableFor(param.toString());
					String implicationVar = this.getVariableFor(subsumedNode.toString());
					
					// If this is not the first implication append an "AND" to the already existing one(s).
					if(!firstImplication)
					{
						result += " AND ";
					}
					
					// Add the new implication. Note than, when node1 subsumes node2, this means node2 => node1.
					result += "( " + implicationVar + " => " + keyVar + " )";
					
					firstImplication = false;
				}
			}
			else
			{
				// This is a nested node, generate implications recursively.
				String nestedResult = this.generateImplications(param.toString());
				
				// Again, check whether this is the first implication or the nested result is empty. If not, append an "AND" operator to the already
				// existing implication(s).
				if(!nestedResult.isEmpty() && !firstImplication)
				{
					result += " AND ";
				}
				
				// If the nested result is not empty, append it to the existing implications.
				if(!nestedResult.isEmpty())
				{
					result += nestedResult;
					firstImplication = false;
				}
			}
		}

		return result;
	}

	/**
	 *  Converts a resource to a propositional expression.
	 * @param resourceUri The resource to convert.
	 * @return A String representation of the propositional expression that this resource has been converted to.
	 */
	public String convertToPropositionalExpression(String resourceUri)
	{
		// Create the root resource object.
		Resource rootResource = jdsi.createResourceFromUri(resourceUri);
		// List<RDFNode> resourceParams = jdsi.executeQuery("{<" + resource.getURI() + "> pac:hasParameter ?var}");
		
		// Traverse it with an RDFVisitor recursively.
		return rootResource.visitWith(new RDFVisitor() {
			
			@Override
			public Object visitURI(Resource resource, String arg1) {
				// Resource object.
				// Get the statements where the Resource is subject of a "otp:TheoremProvingParameterProperty" parameter.
				StmtIterator resourceParams = resource.listProperties(ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:TheoremProvingParameterProperty").getURI()));

				String result = "";
				
				// Switch cases of resource
				if(resource.as(Individual.class).hasOntClass(jdsi.createResourceFromUri("otp:ANDTheoremProvingClass").getURI()))
				{	// otp:ANDTheoremProvingClass
					// generate the propositional block for this AND resource
					result = this.generateBooleanBlock(resourceParams, "AND", result);
				}
				else if(resource.as(Individual.class).hasOntClass(jdsi.createResourceFromUri("otp:ORTheoremProvingClass").getURI()))
				{	// otp:ORTheoremProvingClass
					// generate the propositional block for this OR resource
					result = this.generateBooleanBlock(resourceParams, "OR", result);
				}
				else if(resource.as(Individual.class).hasOntClass(jdsi.createResourceFromUri("otp:XORTheoremProvingClass").getURI()))
				{	// otp:XORTheoremProvingClass
					result = this.generateXORBlock(resourceParams, result);					
				}
				else if(resource.as(Individual.class).hasOntClass(jdsi.createResourceFromUri("otp:NOTheoremProvingClass").getURI()))
				{	// otp:NOTheoremProvingClass
					// TODO: Add support for NOT
				}
				else
				{	// "terminating" param
					// Return the variable to which this resource maps.
					return TautologyChecker.this.getVariableFor(arg1);
				}
				
				return result;
			}

			/*
			 * XOR Statements should have exactly two operands. 
			 */
			private String generateXORBlock(StmtIterator resourceParams, String result) {
				// open parentheses
				result += "( ";
				// get first param, visit it recursively with the same visitor.
				String param1 = resourceParams.next().getObject().visitWith(this).toString();
				
				// get second param, visit it recursively with the same visitor.
				String param2 = resourceParams.next().getObject().visitWith(this).toString();
				
				// build XOR as expressed with AND, NOT, OR
				// p XOR q = ( p AND NOT q ) OR ( NOT p AND q )
				result += "( " + param1 + " AND NOT " + param2 + " ) OR ( NOT " + param1 + " AND " + param2 + " )";
				
				// close parentheses
				result += " )";

				return result;
			}

			/*
			 * Given an iterator of statements, an operator and a result, 
			 */
			private String generateBooleanBlock(StmtIterator resourceParams, String operator, String result) {
				// open parentheses
				result += "( ";
				// add first param, visit it recursively with the same visitor.
				// TODO: What it the resourceParams are empty?
				result += resourceParams.next().getObject().visitWith(this);
				// for all other params
				while(resourceParams.hasNext())
				{
					// add operator
					result += " " + operator + " ";
					// add next param, again visit it recursively with the same visitor.
					result += resourceParams.next().getObject().visitWith(this);
				}
				// close parentheses
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

	/*
	 * Get the variable that a resource is mapped to. If such a mapping doesn't exist, creates it.
	 */
	protected String getVariableFor(String resourceId)
	{
		if(resourceVariableMap.containsKey(resourceId))
		{
			return resourceVariableMap.get(resourceId);
		}
		else
		{
			// Currently, when mapping variables, the key is the resourceId and the value it maps to is the resourceId's
			// namespace appended with its local name.
			// This is not very good decision, because the CheckerTestHelper only allows characters and numbers. If the 
			// namespace or the local name of the resource have any special characters (e.g. "_") this will fail.
			// TODO: For debugging purposes leave it like this, but before deploying change this to something more robust
			// e.g. have a standard prefic for variable names ("e.g. "V") and append a counter to that, thus having only
			// characters and numbers in the variable names.
			Resource resource = jdsi.createResourceFromUri(resourceId);
			String newVariable = jdsi.getPrefixes().getKey(resource.getNameSpace()) + resource.getLocalName();
			resourceVariableMap.put(resourceId, newVariable);
			return newVariable;
		}
		
	}

	/*
	 * Enhances the current data source's model by adding the subsumptions that the tautology checker has found.
	 */
	public void enhanceModel()
	{
		// Get all the individuals of the otp:TheoremProvingBaseClass.
		List<Individual> individualsIterator = ((OntModel)this.jdsi.getModel()).listIndividuals(this.jdsi.createResourceFromUri("otp:TheoremProvingBaseClass")).toList();
		
		// Iterate over them pair-wise. 
		for(Individual i1:individualsIterator)
		{
			for(Individual i2:individualsIterator)
			{
				// Is the one a tautology of the other?
				boolean isT = this.isTautology(i1.getURI(), i2.getURI());
				if(isT)
				{
					// Add the subsumption inference in the model.
					this.jdsi.getModel().add(
							ResourceFactory.createResource(jdsi.createResourceFromUri(i1.getURI()).getURI()), 
							ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:subsumes").getURI()), 
							ResourceFactory.createResource(jdsi.createResourceFromUri(i2.getURI()).getURI()) 
							);
					//this.jdsi.createResourceFromUri(i1.getURI()).addProperty(ResourceFactory.createProperty(jdsi.createResourceFromUri("pac:subsumes").getURI()), i2.getURI());
				}
			}
		}
	}

}
