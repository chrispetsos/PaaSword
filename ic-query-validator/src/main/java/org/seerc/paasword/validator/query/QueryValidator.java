package org.seerc.paasword.validator.query;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.seerc.paasword.translator.QueryConstraint;
import org.seerc.paasword.validator.engine.DomainRangeStatementMover;
import org.seerc.paasword.validator.engine.JenaDataSource;
import org.seerc.paasword.validator.engine.PaaSwordDataSource;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Given a constraints ontology and a knowledge base ontology this class can assess
 * whether the KB violates any of the constraints. Uses a DCQnot approach for validation
 * which means that it follows the Closed World Assumptions and the Unique Name
 * Assumption.
 * 
 * @author Chris Petsos
 *
 */
public class QueryValidator {
	
	// Talk to ontologies with Jena.
	JenaDataSource jds;
	
	// The converted query constraints
	List<QueryConstraint> queryConstraints;
	
	/**
	 * Constructs a QueryValidator and extracts constraints from the constraints
	 * ontology. Uses InputStreams for input.
	 * 
	 * @param constraints The contraints ontology.
	 * @param ontologies The ontologies to be used as knowledge base.
	 */
	public QueryValidator(InputStream constraints, InputStream... ontologies)
	{
		this.extractConstraints(constraints, ontologies);
	}

	/**
	 * Uses a TheoremProvingDataSource to talk to Jena.
	 * 
	 * @param constraints The contraints ontology.
	 * @param ontologies The ontologies to be used as knowledge base.
	 */
	public void extractConstraints(InputStream constraints,
			InputStream... ontologies) {
		// Read constraints ontology
		OntModel constraintsModel;
		constraintsModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		constraintsModel.read(constraints, null , "TTL");
		constraintsModel.prepare();
		
		// Combine multiple ontologies in a single InputStream 
		Enumeration<InputStream> enumOnto = Collections.enumeration(Arrays.asList(ontologies));
		SequenceInputStream sis = new SequenceInputStream(enumOnto);

		// move domain/range statements from ontologies to constraints
		DomainRangeStatementMover drsm = new DomainRangeStatementMover();
		InputStream strippedIS = drsm.moveDomainRangeStatements(sis, constraintsModel);
		
		// Create the PaaSwordDataSource
		jds = new PaaSwordDataSource(strippedIS);
		
		// Convert OWL axiom constraints to SPARQL queries.
		queryConstraints = ICAxiomToSPARQLTranslator.translateModelToSPARQL(constraintsModel);
	}

	/**
	 * Constructs a QueryValidator and extracts constraints from the constraints
	 * ontology. Uses String representation for input.
	 * 
	 * @param constraints The contraints ontology.
	 * @param ontologies The ontologies to be used as knowledge base.
	 */
	public QueryValidator(String constraints, String... ontologies)
	{
		// Convert Strings to InputStreams
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
	
	/**
	 * Performs the validation. Executes each covnerted query on the KB. If the query
	 * returns a result then we have a violation.
	 *  
	 * @return A List of QueryValidatorErrors that were found.
	 */
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
