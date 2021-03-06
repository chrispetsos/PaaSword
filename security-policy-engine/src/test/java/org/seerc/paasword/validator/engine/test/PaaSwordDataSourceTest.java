package org.seerc.paasword.validator.engine.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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
		PaaSwordDataSource pds = new PaaSwordDataSource(getClass().getResourceAsStream("/Ontologies/subsumptive/PolicySubsumption.ttl"));
		
		// assert that we have 13 RuleAntecedents and 13 RuleConclusions
		assertEquals(13, ((OntModel)pds.getModel()).listIndividuals(((OntModel)pds.getModel()).createClass("http://www.paasword.eu/security-policy/seerc/pac#RuleAntecedent")).toList().size());
		assertEquals(13, ((OntModel)pds.getModel()).listIndividuals(((OntModel)pds.getModel()).createClass("http://www.paasword.eu/security-policy/seerc/pac#RuleConclusion")).toList().size());
	}

	@Test
	public void testRuleAntecedentSubsumption() throws Exception {
		PaaSwordDataSource pds = new PaaSwordDataSource(getClass().getResourceAsStream("/Ontologies/subsumptive/PolicySubsumption.ttl"));
		
		assertAntecedentEquivalentClasses(pds, "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_1", "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_2");
		assertAntecedentEquivalentClasses(pds, "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_2", "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_3");
		assertAntecedentEquivalentClasses(pds, "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_1", "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_3");
		assertAntecedentSubclassOf(pds, "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_4", "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_3");
		assertAntecedentSubclassOf(pds, "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_4", "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_1");
		assertAntecedentSubclassOf(pds, "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_4", "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_2");
		assertAntecedentSubclassOf(pds, "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_6", "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_5");

		// rule 7 has different authorization from rule 1, but it should still have equivalent
		// antecedent
		assertAntecedentEquivalentClasses(pds, "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_1", "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_7");
	}

	private RDFNode getRuleAntecedent(JenaDataSourceInferred jdsi, String rule) {
		Individual abacRule1 = jdsi.getModel().getResource(rule).as(Individual.class);
		RDFNode abacRule1Antecedent = abacRule1.getPropertyValue(jdsi.getModel().createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasAntecedent"));
		return abacRule1Antecedent;
	}

	private void assertAntecedentSubclassOf(JenaDataSourceInferred jdsi, String class1Uri, String class2Uri) {
		OntClass class1 = this.getRuleAntecedent(jdsi, jdsi.getModel().getResource(class1Uri).toString()).as(OntClass.class);
		OntClass class2 = this.getRuleAntecedent(jdsi, jdsi.getModel().getResource(class2Uri).toString()).as(OntClass.class);
		assertTrue(class2.hasSubClass(class1));
	}
	
	private void assertAntecedentEquivalentClasses(JenaDataSourceInferred jdsi, String class1Uri, String class2Uri) {
		OntClass class1 = this.getRuleAntecedent(jdsi, jdsi.getModel().getResource(class1Uri).toString()).as(OntClass.class);
		OntClass class2 = this.getRuleAntecedent(jdsi, jdsi.getModel().getResource(class2Uri).toString()).as(OntClass.class);
		assertTrue(class2.hasEquivalentClass(class1));
	}
	
	@Test
	public void testPolicySubsumption() throws FileNotFoundException {
		PaaSwordDataSource pds = new PaaSwordDataSource(getClass().getResourceAsStream("/Ontologies/subsumptive/PolicySubsumption.ttl"));

		pds.printModel(new FileOutputStream(new File("testPolicySubsumption.ttl")));
		
		assertSubclassOf(pds, "http://www.paasword.eu/security-policy/use-cases/car-park#ABACPolicy_1AccessRequestClassFor_negative", "http://www.paasword.eu/security-policy/use-cases/car-park#ABACPolicy_2AccessRequestClassFor_negative");
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
	
	private void assertDisjointClasses(JenaDataSourceInferred jdsi, String class1Uri, String class2Uri) {
		OntClass class1 = jdsi.getModel().getResource(class1Uri).as(OntClass.class);
		OntClass class2 = jdsi.getModel().getResource(class2Uri).as(OntClass.class);
		assertTrue(class1.listDisjointWith().toList().contains(class2));
	}

}
