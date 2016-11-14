package org.seerc.paasword.validator.query;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seerc.paasword.validator.engine.JenaDataSource;

public class JenaDataSourceInferredTest {

	JenaDataSourceInferred jdsi;
	JenaDataSource jds;
	SequenceInputStream sis;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		resetStream();
		// a Jena data source with inferences
		jdsi = new JenaDataSourceInferred(sis);
		
		resetStream();
		// a Jena data source with no inferences
		jds = new JenaDataSource(sis);
	}

	private void resetStream() throws FileNotFoundException {
		Enumeration<InputStream> enumOnto = Collections.enumeration(Arrays.asList(
				new FileInputStream(new File("Ontologies/context-aware-security-models/PaaSwordContextModel_v2.ttl")), 
				new FileInputStream(new File("Ontologies/policy-models/Security-Policy.ttl")), 
				new FileInputStream(new File("Ontologies/subsumptive/ContextExpression1.ttl"))
				));
		sis = new SequenceInputStream(enumOnto);
	}
	
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreate() {
		assertNotNull(jdsi);
	}

	@Test
	public void testInferredSizes() {
		assertEquals(548, jdsi.getModel().size());
		assertEquals(jdsi.getModel().size(), jds.getModel().size());
		
		System.out.println("----------- Original model --------------");
		jds.printModel(System.out);
		System.out.println("-----------------------------------------------\n\n");
		System.out.println("----------- Original model with inferences --------------");
		jdsi.printModel(System.out);
		System.out.println("-----------------------------------------------\n\n");
	}
}
