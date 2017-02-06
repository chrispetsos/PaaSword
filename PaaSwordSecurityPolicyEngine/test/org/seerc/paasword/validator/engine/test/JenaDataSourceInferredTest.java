package org.seerc.paasword.validator.engine.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seerc.paasword.validator.engine.JenaDataSource;
import org.seerc.paasword.validator.engine.JenaDataSourceInferred;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class JenaDataSourceInferredTest {

	JenaDataSourceInferred jdsi;
	JenaDataSource jds;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
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
	
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSimpleInferences() throws FileNotFoundException {
		JenaDataSource simpleSource = new JenaDataSource(new FileInputStream(new File("Ontologies/subsumptive/SimpleForInferences.ttl")));
		JenaDataSourceInferred inferredSource = new JenaDataSourceInferred(new FileInputStream(new File("Ontologies/subsumptive/SimpleForInferences.ttl")));
		assertNotEquals(simpleSource.getModelSize(), inferredSource.getModelSize());
	}
	
	@Test
	public void testSubclassSubsumption() throws FileNotFoundException {
		JenaDataSourceInferred jdsi = new JenaDataSourceInferred(
				createStream(
								"Ontologies/subsumptive/SubclassSubsumption.ttl")
				);
		
		assertNotNull(jdsi);

		// Context Expressions
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE2Class", "http://www.paasword.eu/security-policy/use-cases/car-park#CE1Class");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE3", "http://www.paasword.eu/security-policy/use-cases/car-park#CE4");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE5", "http://www.paasword.eu/security-policy/use-cases/car-park#CE6");
		assertEquivalentClasses(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE4", "http://www.paasword.eu/security-policy/use-cases/car-park#CE6");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE3", "http://www.paasword.eu/security-policy/use-cases/car-park#CE6");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE5", "http://www.paasword.eu/security-policy/use-cases/car-park#CE4");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE5", "http://www.paasword.eu/security-policy/use-cases/car-park#CE3");
		assertSubclassOf(jdsi, "http://www.paasword.eu/security-policy/use-cases/car-park#CE5", "http://www.paasword.eu/security-policy/use-cases/car-park#CE6");

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
	
	@Test
	public void testInferredSizes() {
		performInferredTest(548, 3639, 
				"Ontologies/context-aware-security-models/PaaSwordContextModel_v2.ttl", 
				"Ontologies/policy-models/Security-Policy.ttl",
				"Ontologies/subsumptive/ContextExpression1.ttl"
				);
	}

	@Test
	public void testCitySubsumption() {
		performInferredTest(547, 3652, 
				"Ontologies/context-aware-security-models/PaaSwordContextModel_v2.ttl", 
				"Ontologies/policy-models/Security-Policy.ttl",
				"Ontologies/subsumptive/CitySubsumption.ttl"
				);
	}

	@Test
	public void testComplexContextExpression() {
		performInferredTest(-1, -1, 
				"Ontologies/context-aware-security-models/PaaSwordContextModel_v2.ttl", 
				"Ontologies/policy-models/Security-Policy.ttl",
				"Ontologies/subsumptive/ComplexContextExpression.ttl"
				);
	}

	@Test
	public void testDateTimeSubsumption() {
		performInferredTest(-1, -1, 
				"Ontologies/context-aware-security-models/PaaSwordContextModel_v2.ttl", 
				"Ontologies/policy-models/Security-Policy.ttl",
				"Ontologies/subsumptive/DateTimeSubsumption.ttl"
				);
	}

	/*
	 * Performs a print out of ontologies with and without inferrences.
	 * Optionoally it can check the sizes of the two version of the ontologies.
	 */
	private void performInferredTest(int originalSize, int inferredSize, String... ontoPaths) {
		// a Jena data source with inferences
		jdsi = new JenaDataSourceInferred(createStream(ontoPaths));
		
		// a Jena data source with no inferences
		jds = new JenaDataSource(createStream(ontoPaths));
		
		if(originalSize != -1 && inferredSize != -1)
		{
			assertEquals(inferredSize, jdsi.getModelSize());
			assertTrue(jdsi.getModelSize() > jds.getModelSize());
			assertEquals(originalSize, jds.getModelSize());
		}
		
		System.out.println("----------- Original model --------------");
		jds.printModel(System.out);
		System.out.println("-----------------------------------------------\n\n");
		System.out.println("----------- Original model with inferences --------------");
		jdsi.printModel(System.out);
		System.out.println("-----------------------------------------------\n\n");
	}
}
