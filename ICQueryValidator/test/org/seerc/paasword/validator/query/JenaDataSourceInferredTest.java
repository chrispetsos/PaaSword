package org.seerc.paasword.validator.query;

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
		int i=0;
	}

	private void performInferredTest(int originalSize, int inferredSize, String... ontoPaths) {
		// a Jena data source with inferences
		jdsi = new JenaDataSourceInferred(createStream(ontoPaths));
		
		// a Jena data source with no inferences
		jds = new JenaDataSource(createStream(ontoPaths));
		
		assertEquals(inferredSize, jdsi.getModelSize());
		assertTrue(jdsi.getModelSize() > jds.getModelSize());
		assertEquals(originalSize, jds.getModelSize());
		
		System.out.println("----------- Original model --------------");
		jds.printModel(System.out);
		System.out.println("-----------------------------------------------\n\n");
		System.out.println("----------- Original model with inferences --------------");
		jdsi.printModel(System.out);
		System.out.println("-----------------------------------------------\n\n");
	}
}
