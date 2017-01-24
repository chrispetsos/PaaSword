package org.seerc.paasword.theoremprover;

import java.io.ByteArrayInputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
import com.hp.hpl.jena.rdf.model.Statement;
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
	// A Map from pairs of resource/baseReference to variables.
	private HashMap<SimpleEntry<Resource, Resource>, String> resourceVariableMap;
	// A Map from resources to references.
	private HashMap<Resource, Resource> resourceReferenceMap;
	// A List with variable implications 
	private List<SimpleEntry<String, String>> implications;
	
	private static long variableCounter = 0;

	/**
	 * Constructs a TautologyChecker object.
	 * 
	 * @param jdsi The data source.
	 */
	public TautologyChecker(JenaDataSourceInferred jdsi)
	{
		this.jdsi = jdsi;
		checker = new CheckerTestHelper();
		resourceVariableMap = new HashMap<SimpleEntry<Resource, Resource>, String>();
		resourceReferenceMap = new HashMap<Resource, Resource>();
		implications = new ArrayList<AbstractMap.SimpleEntry<String,String>>();
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
		String propositionToCheck = "";
		String allImplications = "";
		
		// Convert the OWL expressions to propositional statements
		String propositionalExpressionCe1 = this.convertToPropositionalExpression(ce1);
		String propositionalExpressionCe2 = this.convertToPropositionalExpression(ce2);
		
		// Calculate implications based on subsumptions
		for(SimpleEntry<String, String> implication:implications)
		{
			if(!implications.get(0).equals(implication))
			{	// not first, add AND
				allImplications += " AND ";
			}
			allImplications += "( " + implication.getValue() + " => " + implication.getKey() + " )";
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

	/**
	 *  Converts a resource to a propositional expression.
	 * @param resourceUri The resource to convert.
	 * @return A String representation of the propositional expression that this resource has been converted to.
	 */
	public String convertToPropositionalExpression(String resourceUri)
	{
		// Create the root resource object.
		Resource rootResource = jdsi.createResourceFromUri(resourceUri);
		
		// Traverse it with an RDFVisitor recursively.
		return rootResource.visitWith(new RDFVisitor() {
			
			Deque<Resource> resourceStack = new ArrayDeque<Resource>();
			
			@Override
			public Object visitURI(Resource resource, String arg1) {
				// Resource object.
				// Get the statements where the Resource is subject of a "otp:TheoremProvingParameterProperty" parameter.
				StmtIterator resourceParams = resource.listProperties(ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:TheoremProvingParameterProperty").getURI()));

				String result = "";
				
				// Get the reference statement. Should be at most one... 
				StmtIterator referenceStatementIterator = resource.listProperties(ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:TheoremProvingReferenceProperty").getURI()));
				if(referenceStatementIterator.hasNext())
				{
					Statement referenceStatement = referenceStatementIterator.next();
					resourceReferenceMap.put(resource, referenceStatement.getObject().asResource());
				}
				
				// Switch cases of resource
				if(resource.as(Individual.class).hasOntClass(jdsi.createResourceFromUri("otp:ANDTheoremProvingClass").getURI()))
				{	// otp:ANDTheoremProvingClass
					// generate the propositional block for this AND resource
					resourceStack.push(resource);
					result = this.generateBooleanBlock(resourceParams, "AND", result);
					resourceStack.pop();
				}
				else if(resource.as(Individual.class).hasOntClass(jdsi.createResourceFromUri("otp:ORTheoremProvingClass").getURI()))
				{	// otp:ORTheoremProvingClass
					// generate the propositional block for this OR resource
					resourceStack.push(resource);
					result = this.generateBooleanBlock(resourceParams, "OR", result);
					resourceStack.pop();
				}
				else if(resource.as(Individual.class).hasOntClass(jdsi.createResourceFromUri("otp:XORTheoremProvingClass").getURI()))
				{	// otp:XORTheoremProvingClass
					resourceStack.push(resource);
					result = this.generateXORBlock(resourceParams, result);					
					resourceStack.pop();
				}
				else if(resource.as(Individual.class).hasOntClass(jdsi.createResourceFromUri("otp:NOTheoremProvingClass").getURI()))
				{	// otp:NOTheoremProvingClass
					resourceStack.push(resource);
					result = this.generateNOTBlock(resourceParams, result);					
					resourceStack.pop();
				}
				else
				{	// "terminating" param
					// Return the variable to which this resource maps.
					// use the last resource pushed
					Resource baseResource = resourceStack.peek();
					return TautologyChecker.this.getVariableFor(resource, baseResource);
				}
				
				return result;
			}

			/*
			 * NOT Statements should have exactly one operand. 
			 */
			private String generateNOTBlock(StmtIterator resourceParams, String result) {
				// open parentheses
				result += "( ";
				
				// get param, visit it recursively with the same visitor.
				String param = resourceParams.next().getObject().visitWith(this).toString();
				
				// build NOT statement
				result += "NOT " + param;
				
				// close parentheses
				result += " )";

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
	protected String getVariableFor(Resource resource, Resource baseResource)
	{
		// where does the base of this resource refers to?
		Resource referenceOfBaseOfNewResource = resourceReferenceMap.get(baseResource);
		// this will be the key of the current resource/reference
		SimpleEntry<Resource, Resource> key = new AbstractMap.SimpleEntry<Resource, Resource>(resource, referenceOfBaseOfNewResource);
		
		if(resourceVariableMap.containsKey(key))
		{	// contains key as it is, return it
			return resourceVariableMap.get(key);
		}

		// will be a new mapping, create a new var
		variableCounter++;
		String newVariable = "V" + String.valueOf(variableCounter);

		// if the resource with current entry in resourceVariableMap are equal or subsuming and 
		// the referenceOfBaseOfNewResource is subsumed by the reference of current entry or they are equal 
		// return the current entry's var.
		for(SimpleEntry<Resource, Resource> entryKey:resourceVariableMap.keySet())
		{
			if(
				resource.equals(entryKey.getKey()) ||
				entryKey.getKey().listProperties(ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:subsumes").getURI())).toList().contains(
						ResourceFactory.createStatement(entryKey.getKey(), 
						ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:subsumes").getURI()), 
						resource)
						)
			)
			{	// equal or subsuming resources
				
				// both have no reference
				if(entryKey.getValue() == null && referenceOfBaseOfNewResource == null)
				{	// add implication
					implications.add(new SimpleEntry<String, String>(resourceVariableMap.get(entryKey), newVariable));
				}
				
				// one of the two has null reference, thus they are different
				if(entryKey.getValue() == null || referenceOfBaseOfNewResource == null)
				{	// no implication
					continue;
				}
				
				// Does current entry's reference subsumes referenceOfBaseOfNewResource or are they equal?
				if(
					entryKey.getValue().equals(referenceOfBaseOfNewResource) ||
					entryKey.getValue().listProperties(ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:subsumes").getURI())).toList().contains(
							ResourceFactory.createStatement(entryKey.getValue(), 
							ResourceFactory.createProperty(jdsi.createResourceFromUri("otp:subsumes").getURI()), 
							referenceOfBaseOfNewResource)
							)
				)
				{	// add implication
					implications.add(new SimpleEntry<String, String>(resourceVariableMap.get(entryKey), newVariable));
				}
			}
		}

		// Reaching here means that we could not find the resource/reference pair neither as it is or with a 
		// subsuming entry. Go on and create a new entry.
		
		// Currently, when mapping variables, the key is the resourceId and the value it maps to is the resourceId's
		// namespace appended with its local name.
		// This is not very good decision, because the CheckerTestHelper only allows characters and numbers. If the 
		// namespace or the local name of the resource have any special characters (e.g. "_") this will fail.
		// For debugging purposes leave it like this, but before deploying change this to something more robust
		// e.g. have a standard prefix for variable names ("e.g. "V") and append a counter to that, thus having only
		// characters and numbers in the variable names.
		//
		// DONE
		// 
		// Leave it here just in case we need to temporarily 'revive' it at some point for debugging purposes.
		/*Resource resource = jdsi.createResourceFromUri(resourceId);
		String newVariable = jdsi.getPrefixes().getKey(resource.getNameSpace()) + resource.getLocalName();*/
		
		resourceVariableMap.put(key, newVariable);
		return newVariable;
		
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
