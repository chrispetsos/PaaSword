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
import org.seerc.paasword.validator.engine.JenaDataSourceInferred;
import org.seerc.paasword.validator.engine.PaaSwordDataSource;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

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

	@Test
	public void testRuleAntecedentSubsumption() throws Exception {
		PaaSwordDataSource pds = new PaaSwordDataSource(new FileInputStream(new File("Ontologies/subsumptive/PolicySubsumption.ttl")));
		
		assertEquivalentClasses(pds, this.getRuleAntecedent(pds, "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_1"), this.getRuleAntecedent(pds, "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_2"));
		
	}

	private String getRuleAntecedent(PaaSwordDataSource pds, String rule) {
		Individual abacRule1 = pds.getModel().getResource(rule).as(Individual.class);
		RDFNode abacRule1Antecedent = abacRule1.getPropertyValue(pds.getModel().createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasAntecedent"));
		return abacRule1Antecedent.toString();
	}

	private void assertSubclassOf(JenaDataSourceInferred jdsi, String class1Uri, String class2Uri) {
		OntClass class1 = jdsi.getModel().getResource(class1Uri).as(OntClass.class);
		OntClass class2 = jdsi.getModel().getResource(class2Uri).as(OntClass.class);
		assertTrue(class2.listSubClasses().toList().contains(class1));
	}
	
	private void assertEquivalentClasses(JenaDataSourceInferred jdsi, String class1Uri, String class2Uri) {
		OntClass class1 = jdsi.getModel().getResource(class1Uri).as(OntClass.class);
		OntClass class2 = jdsi.getModel().getResource(class2Uri).as(OntClass.class);
		assertTrue(class1.listEquivalentClasses().toList().contains(class2));
	}
	
}
