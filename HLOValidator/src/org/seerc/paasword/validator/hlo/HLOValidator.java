package org.seerc.paasword.validator.hlo;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.seerc.paasword.validator.engine.JenaDataSource;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class HLOValidator {

	JenaDataSource jds;
	
	public HLOValidator(InputStream... ontologies)
	{
		Enumeration<InputStream> enumOnto = Collections.enumeration(Arrays.asList(ontologies));
		SequenceInputStream sis = new SequenceInputStream(enumOnto);

		jds = new JenaDataSource(sis);
	}

	public List<ValidationError> checkExactlyPropertyConstraints()
	{
		List<ValidationError> errors = new ArrayList<>();
		
		// 2. Find resources (R) which are subclasses of [a hlo:ExactlyPropertyConstraint]
		/*
		 * No need to take things one by one. We can perform complex queries like the 
		 * following. I might need to have a complex structure as result instead of 
		 * just a List<RDFNode>. 
		 */
		List<RDFNode> R = jds.executeQuery("{?var rdfs:subClassOf\n" + 
				"              [ a hlo:ExactlyPropertyConstraint ;\n" + 
				"                hlo:onProperty ?property ;\n" + 
				"                hlo:qualifiedCardinality ?cardinality ;\n" + 
				"                hlo:onClass ?class \n" + 
				"              ]}");
		
		
		// 3. Find (Iepc) hlo:onProperty (P)
		
		int i=0;
		// 4. Find (Iepc) hlo:qualifiedCardinality (C)
		
		// 5. Find (Iepc) hlo:onClass (Cl)
		
		// 6. Find instances (Ir) of class (R)
		
		// 7. Each (Ir) must be connected with exactly (C) (Cl)s via (P)
		
		return errors;
	}

	public void addOntology(String ontology)
	{
		jds.addOntology(ontology);		
	}

}
