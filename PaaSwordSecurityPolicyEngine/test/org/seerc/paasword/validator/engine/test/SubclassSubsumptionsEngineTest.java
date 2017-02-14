package org.seerc.paasword.validator.engine.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seerc.paasword.validator.engine.JenaDataSourceInferred;
import org.seerc.paasword.validator.engine.SubclassSubsumptionsEngine;

import com.hp.hpl.jena.ontology.OntClass;

public class SubclassSubsumptionsEngineTest {

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
	public void testEnhanceModel() throws FileNotFoundException {
		JenaDataSourceInferred jdsi = new JenaDataSourceInferred(
				createStream(
								"Ontologies/subsumptive/SubclassSubsumptionWithoutInferences.ttl")
				);
		
		assertNotNull(jdsi);

		SubclassSubsumptionsEngine sse = new SubclassSubsumptionsEngine(jdsi);
		
		assertNotNull(sse);
		
		sse.enhanceModel();

		// Context Expressions
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE2", "http://www.paasword.eu/security-policy/use-cases/car-park#CE1");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE3", "http://www.paasword.eu/security-policy/use-cases/car-park#CE4");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE5", "http://www.paasword.eu/security-policy/use-cases/car-park#CE6");
		assertEquivalentClasses(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE4", "http://www.paasword.eu/security-policy/use-cases/car-park#CE6");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE3", "http://www.paasword.eu/security-policy/use-cases/car-park#CE6");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE5", "http://www.paasword.eu/security-policy/use-cases/car-park#CE4");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE5", "http://www.paasword.eu/security-policy/use-cases/car-park#CE3");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE5", "http://www.paasword.eu/security-policy/use-cases/car-park#CE6");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE15", "http://www.paasword.eu/security-policy/use-cases/car-park#CE16");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE17", "http://www.paasword.eu/security-policy/use-cases/car-park#CE18");

		// Complex CEs
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE7", "http://www.paasword.eu/security-policy/use-cases/car-park#CE8");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE9", "http://www.paasword.eu/security-policy/use-cases/car-park#CE10");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE12", "http://www.paasword.eu/security-policy/use-cases/car-park#CE11");

		// Plain CEs with only one parameter
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE12", "http://www.paasword.eu/security-policy/use-cases/car-park#CE14");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE14", "http://www.paasword.eu/security-policy/use-cases/car-park#CE13");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE12", "http://www.paasword.eu/security-policy/use-cases/car-park#CE13");

		// Rules
		assertEquivalentClasses(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_1", "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_2");
		assertEquivalentClasses(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_2", "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_3");
		assertEquivalentClasses(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_1", "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_3");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_4", "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_3");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_4", "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_1");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_4", "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_2");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_6", "http://www.paasword.eu/security-policy/use-cases/car-park#ABACRule_5");
		
		/*List<? extends OntResource> ce1Instances = contextExpression1.listInstances().toList();
		List<? extends OntResource> ce2Instances = contextExpression2.listInstances().toList();
		assertEquals(1, ce1Instances.size());
		assertEquals(0, ce2Instances.size());*/
	}
	
	@Test
	public void testPolicySubsumption() throws FileNotFoundException {
		JenaDataSourceInferred jdsi = new JenaDataSourceInferred(
				createStream(
								"Ontologies/subsumptive/PolicySubsumption.ttl")
				);
		
		assertNotNull(jdsi);

		SubclassSubsumptionsEngine sse = new SubclassSubsumptionsEngine(jdsi);
		
		assertNotNull(sse);
		
		sse.enhanceModel();
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
	
	private InputStream createStream(String... paths) {
		List<InputStream> enumOnto = new ArrayList<InputStream>();
		try {
			for(String path:paths)
			{
				enumOnto.add(new FileInputStream(new File(path)));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		SequenceInputStream sis = new SequenceInputStream(Collections.enumeration(enumOnto));
		
		return sis;
	}
}
