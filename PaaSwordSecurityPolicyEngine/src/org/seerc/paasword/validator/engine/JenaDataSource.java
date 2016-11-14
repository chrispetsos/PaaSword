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

public class JenaDataSource {
	private static String[] neededPrefixesForQueries; 
			
	OntModel model;

	public JenaDataSource(OntModel model)
	{
		this.populatePrefixMappings(model);
		setModel(model);
	}

	public void setModel(OntModel model) {
		this.model = model;
	}

	public Model getModel()
	{
		return this.model;
	}
	
	public JenaDataSource(String filePath)
	{
		OntModel model = null;
		
		try {
			InputStream is = new FileInputStream(new File(filePath));
			model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
			model.read(is, null, "TTL");
			this.populatePrefixMappings(model);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		setModel(model);
	}

	public JenaDataSource(InputStream stream) 
	{
		OntModel model = null;
		
		model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		model.read(stream, null, "TTL");
		this.populatePrefixMappings(model);
		
		setModel(model);
	}

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
	
	public Resource createFromNsAndLocalName(String nameSpace, String localName)
	{
		return new ResourceImpl(model.getNsPrefixMap().get(nameSpace), localName);
	}

	public Resource createResourceFromUri(String uri)
	{
		uri = this.replaceNamespacePrefixes(uri);
		return new ResourceImpl(uri);
	}

	public BidiMap<String, String> getPrefixes() {
		BidiMap<String, String> bidiMap = new DualHashBidiMap<String, String>(model.getNsPrefixMap());
		return bidiMap;
	}

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
		
		this.model.read(new ByteArrayInputStream(ontology.getBytes(StandardCharsets.UTF_8)), null, "TTL");
		
		this.populatePrefixMappings(model);
	}

	/*public List<Individual> findInstances(String classUri)
	{
		return this.model.listIndividuals(this.createResourceFromUri(classUri)).toList();
	}*/

	private String replaceNamespacePrefixes(String classUri)
	{
		BidiMap<String, String> prefixes = this.getPrefixes();
		for(String prefix:prefixes.keySet())
		{
			classUri = classUri.replace(prefix + ":", prefixes.get(prefix));
		}

		return classUri;
	}
	
	public void printModel(OutputStream os)
	{
		model.write(os, "TTL") ;
	}
	
	public int getModelSize()
	{
		return this.model.listStatements().toList().size();
	}
}
