package org.seerc.paasword.validator.query;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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
		// a Jena data source with inferences
		jdsi = new JenaDataSourceInferred(new FileInputStream(new File("Ontologies/subsumptive/ContextExpression1.ttl")));
		// a Jena data source with no inferences
		jds = new JenaDataSource(new FileInputStream(new File("Ontologies/subsumptive/ContextExpression1.ttl")));
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
		// should have equal size models
		assertEquals(jdsi.getModel().size(), jds.getModel().size());
	}
}
