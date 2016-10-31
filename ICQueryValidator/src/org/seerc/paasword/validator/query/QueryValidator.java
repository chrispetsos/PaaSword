package org.seerc.paasword.validator.query;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import org.seerc.paasword.translator.QueryConstraint;
import org.seerc.paasword.validator.engine.JenaDataSource;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class QueryValidator {
	
	JenaDataSource jds;
	List<QueryConstraint> queryConstraints;
	
	public QueryValidator(InputStream constraints, InputStream... ontologies)
	{
		this.extractConstraints(constraints, ontologies);
	}

	public void extractConstraints(InputStream constraints,
			InputStream... ontologies) {
		Enumeration<InputStream> enumOnto = Collections.enumeration(Arrays.asList(ontologies));
		SequenceInputStream sis = new SequenceInputStream(enumOnto);

		jds = new JenaDataSourceInferred(sis);
		
		OntModel constraintsModel;
		constraintsModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		constraintsModel.read(constraints, null , "TTL");
		constraintsModel.prepare();
		
		queryConstraints = ICAxiomToSPARQLTranslator.translateModelToSPARQL(constraintsModel);
	}
	
	public QueryValidator(String constraints, String... ontologies)
	{
		InputStream constraintsIs = new ByteArrayInputStream(constraints.getBytes(StandardCharsets.UTF_8));
		InputStream[] ontologiesIs = new ByteArrayInputStream[ontologies.length];

		int i=0;
		for(String ontology:ontologies)
		{
			ontologiesIs[i] = new ByteArrayInputStream(ontology.getBytes(StandardCharsets.UTF_8));
			i++;
		}

		this.extractConstraints(constraintsIs, ontologiesIs);
	}
	
	public List<QueryValidatorErrors> validate()
	{
		List<QueryValidatorErrors> errors = new ArrayList<>();
		for(QueryConstraint queryConstraint:queryConstraints)
		{
			List<String> result = jds.executeReadyQuery(queryConstraint.getQuery());
			
			if(result != null && !result.isEmpty())
			{
				errors.add(new QueryValidatorErrors(queryConstraint, result));
			}
		}
		return errors;
	}
}
