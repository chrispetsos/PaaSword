package org.seerc.paasword.validator.query;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.seerc.paasword.translator.AxiomSPARQLTranslator;
import org.seerc.paasword.translator.QueryConstraint;

import com.hp.hpl.jena.ontology.OntModel;

/**
 * Class that wraps AxiomSPARQLTranslator. May need to be deprecated... TODO
 * 
 * @author Chris Petsos
 *
 */
public class ICAxiomToSPARQLTranslator {

	public static List<QueryConstraint> translateModelToSPARQL(OntModel constraintsModel)
	{
		// create InputStream from OntModel
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		constraintsModel.write(baos);
		AxiomSPARQLTranslator ast = new AxiomSPARQLTranslator(new ByteArrayInputStream(baos.toByteArray()));
		
		List<QueryConstraint> queries = ast.convertToSPARQLDCQnot();
		
		return queries;
	}
}
