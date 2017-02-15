package org.seerc.paasword.validator.engine.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seerc.paasword.validator.engine.PaaSwordDataSource;

import com.hp.hpl.jena.ontology.OntModel;

public class PaaSwordDataSourceTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEnhanceModel() throws Exception {
		PaaSwordDataSource pds = new PaaSwordDataSource(new FileInputStream(new File("Ontologies/subsumptive/PolicySubsumption.ttl")));
		
		// assert that we have 6 RuleAntecedents and 6 RuleConclusions
		assertEquals(7, ((OntModel)pds.getModel()).listIndividuals(((OntModel)pds.getModel()).createClass("http://www.paasword.eu/security-policy/seerc/pac#RuleAntecedent")).toList().size());
		assertEquals(7, ((OntModel)pds.getModel()).listIndividuals(((OntModel)pds.getModel()).createClass("http://www.paasword.eu/security-policy/seerc/pac#RuleConclusion")).toList().size());
	}

}
