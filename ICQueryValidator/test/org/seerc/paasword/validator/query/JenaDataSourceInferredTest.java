package org.seerc.paasword.validator.query;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
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
	JenaDataSourceInferred jdsiWithCM;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		// a Jena data source with inferences
		jdsi = new JenaDataSourceInferred(new FileInputStream(new File("Ontologies/subsumptive/ContextExpression1.ttl")));
		// a Jena data source with no inferences
		jds = new JenaDataSource(new FileInputStream(new File("Ontologies/subsumptive/ContextExpression1.ttl")));
		// a Jena data source with inferences and the Context Model inside
		Enumeration<InputStream> enumOnto = Collections.enumeration(Arrays.asList(
				new FileInputStream(new File("Ontologies/context-aware-security-models/PaaSwordContextModel_v2.ttl")), 
				new FileInputStream(new File("Ontologies/subsumptive/ContextExpression1.ttl"))
				));
		SequenceInputStream sis = new SequenceInputStream(enumOnto);
		jdsiWithCM = new JenaDataSourceInferred(sis);
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
		// those without the CM should have equal size models
		assertEquals(9, jdsi.getModel().size());
		assertEquals(jdsi.getModel().size(), jds.getModel().size());
		
		// the one with the CM should be bigger
		assertTrue(jdsiWithCM.getModel().size() > jdsi.getModel().size());
		assertEquals(386, jdsiWithCM.getModel().size());
	}
}
