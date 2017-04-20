package org.seerc.paasword.validator.engine;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

/**
 * An interface to ontology data usign Jena.
 * 
 * @author Chris Petsos
 *
 */
public class JenaDataSource {
	// This is populated with the prefixes currently existing in the loaded ontology.
	// TODO: Why is this static?
	private static String[] neededPrefixesForQueries; 

	// The Jena OntModel.
	OntModel model;

	/**
	 * Constructs a JenaDataSource using an OntModel.
	 * @param model
	 */
	public JenaDataSource(OntModel model)
	{
		this.populatePrefixMappings(model);
		setModel(model);
	}

	/**
	 * Sets the OntModel of the current JenaDataSource.
	 * @param model
	 */
	public void setModel(OntModel model) {
		this.model = model;
	}

	/**
	 * Gets the OntModel of the current JenaDataSource.
	 * @return
	 */
	public Model getModel()
	{
		return this.model;
	}
	
	/**
	 * Constructs a JenaDataSource using a file path.
	 * @param filePath The path to the file that contains the ontology.
	 */
	public JenaDataSource(String filePath)
	{
		OntModel model = null;
		
		InputStream is = getClass().getResourceAsStream(filePath);
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		model.read(is, null, "TTL");
		this.populatePrefixMappings(model);
		
		setModel(model);
	}

	/**
	 * Constructs a JenaDataSource using an InpuStream.
	 * @param stream
	 */
	public JenaDataSource(InputStream stream) 
	{
		OntModel model = null;
		
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		model.read(stream, null, "TTL");
		this.populatePrefixMappings(model);
		
		setModel(model);
	}

	/*
	 * Populates the neededPrefixesForQueries with the prefixes contained in the
	 * loaded ontology. 
	 */
	private void populatePrefixMappings(OntModel model) {
		Map<String, String> pm = model.getNsPrefixMap();
		neededPrefixesForQueries = new String[pm.keySet().size()];
		int i=0;
		for(String key:pm.keySet())
		{
			neededPrefixesForQueries[i] = key + ": <" + pm.get(key) + ">";
			i++;
		}
	}

	/**
	 * Executes a SPARQL query on the ontology given only the WHERE part. It expects 
	 * that the WHERE part will have a projection variable with the name "?var". 
	 * @param wherePart The WHERE part to be used for the query. 
	 * @return A List of RDFNodes that where returned by the query execution.
	 */
	// TODO: Refactor this so that it uses the executeReadyQuery() method.
	public List<RDFNode> executeQuery(String wherePart) {
		QueryExecution qexec = returnQueryExecObject("SELECT ?var WHERE " + wherePart);
		List<RDFNode> result = new ArrayList<RDFNode>();
		try {
			ResultSet rs = qexec.execSelect();

			while (rs.hasNext()) {
				QuerySolution soln = rs.nextSolution();
				result.add(soln.get("?var"));
			}
		} finally {
			qexec.close();
		}
		return result;
	}

	/**
	 * Executes a SPARQL query on the ontology. 
	 * @param query The query to be executed. 
	 * @return A List of String that where returned by the query execution. Those are
	 * the URIs of the RDFNodes that where returned by the query.
	 */
	// TODO: Adjsut this so it can be reused by the executeQuery() method.
	public List<String> executeReadyQuery(String query) {
		QueryExecution qexec = returnQueryExecObject(query);
		List<String> result = new ArrayList<String>();
		try {
			ResultSet rs = qexec.execSelect();

			while (rs.hasNext()) {
				QuerySolution soln = rs.nextSolution();
				result.add(soln.toString());
			}
		} finally {
			qexec.close();
		}
		return result;
	}

	/*
	 * Creates a QueryExecution object given a String query.
	 * It prepends the neededPrefixesForQueries to the query passed as argument.
	 */
	private QueryExecution returnQueryExecObject(String coreQuery) {
		StringBuffer queryStr = new StringBuffer();
		// Establish Prefixes
		for(String prefix:neededPrefixesForQueries)
		{
			queryStr.append("prefix " + prefix);
		}

		queryStr.append(coreQuery);

		Query query = QueryFactory.create(queryStr.toString());
		QueryExecution qexec = QueryExecutionFactory.create(query, this.model);

		return qexec;
	}

	/**
	 * Answers of "node" is of type "type"
	 * @param node
	 * @param type
	 * @return
	 */
	// TODO: Implement this is a more elegant way using Jena's facilities.
	public Boolean isNodeType(RDFNode node, String type)
	{
		if(!node.isResource())
		{
			return false;
		}
		
		StmtIterator si =  node.asResource().listProperties();
		while(si.hasNext())
		{
			Statement nextStatement = si.next();
			if(nextStatement.getPredicate().equals(this.createFromNsAndLocalName("rdf", "type")) &&
					nextStatement.getObject().toString().equals(type))
			{
				return true;
			}
		}

		/*List<RDFNode> nodeTypes = this.executeQuery("{<" + node.toString() + "> a ?var}");
		for(RDFNode nodeType:nodeTypes)
		{
			if(nodeType.toString().equals(type))
			{
				return true;
			}
		}*/
		
		return false;
	}
	
	/**
	 * Creates a resource given a namespace and a local name.
	 * @param nameSpace
	 * @param localName
	 * @return
	 */
	// TODO: Create the resource in the model rather than instantiating a ResourceImpl.
	public Resource createFromNsAndLocalName(String nameSpace, String localName)
	{
		return new ResourceImpl(model.getNsPrefixMap().get(nameSpace), localName);
	}

	/**
	 * Gets a reference to a Resource in the model of the given "uri".
	 * If it is already in the model it returns it. Otherwise, it creates it in the model
	 * and then returns it.
	 * @param uri
	 * @return
	 */
	public Resource createResourceFromUri(String uri)
	{
		uri = this.replaceNamespacePrefixes(uri);
		return this.model.createResource(uri);
	}

	/**
	 * Creates a bi-directional map of the model's prefixes.
	 * 
	 * @return The bi-directional map. 
	 */
	public BidiMap<String, String> getPrefixes() {
		BidiMap<String, String> bidiMap = new DualHashBidiMap<String, String>(model.getNsPrefixMap());
		return bidiMap;
	}

	/**
	 * Adds an ontology to the current model on-the-fly.
	 * 
	 * @param ontology The ontology to be added
	 */
	// TODO: Where is this used? Could it be that it breaks the immutability
	// that theorem proving demands? 
	public void addOntology(String ontology)
	{
		// add current prefixes first
		String prefixes = "";
		for(String prefixDeclaration:this.neededPrefixesForQueries)
		{
			prefixes = prefixes + "@prefix " + prefixDeclaration + ".\n";
		}
		prefixes += "\n";
		
		ontology = prefixes + ontology;
		
		// add new ontology to model
		this.model.read(new ByteArrayInputStream(ontology.getBytes(StandardCharsets.UTF_8)), null, "TTL");
		
		// re-populate prefix mappings
		this.populatePrefixMappings(model);
	}

	/*public List<Individual> findInstances(String classUri)
	{
		return this.model.listIndividuals(this.createResourceFromUri(classUri)).toList();
	}*/

	/*
	 * Given a classUri, this replaces namespaces with prefixes in that exist in the
	 * current ontology.
	 */
	private String replaceNamespacePrefixes(String classUri)
	{
		BidiMap<String, String> prefixes = this.getPrefixes();
		for(String prefix:prefixes.keySet())
		{
			classUri = classUri.replace(prefix + ":", prefixes.get(prefix));
		}

		return classUri;
	}
	
	/**
	 * Prints the model to an OutputStream.
	 * @param os The OutputStream to print the model to.
	 */
	public void printModel(OutputStream os)
	{
		RDFDataMgr.write(os, model, Lang.TURTLE) ;
	}
	
	/**
	 * Returns the actual size of the model. Note that this method might be time-
	 * consuming when executed. 
	 * @return
	 */
	public int getModelSize()
	{
		return this.model.listStatements().toList().size();
	}
}
