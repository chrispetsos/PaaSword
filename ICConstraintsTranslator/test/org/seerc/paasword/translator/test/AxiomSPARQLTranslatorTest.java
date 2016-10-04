package org.seerc.paasword.translator.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seerc.paasword.translator.AxiomSPARQLTranslator;

public class AxiomSPARQLTranslatorTest {

	AxiomSPARQLTranslator ast;
	
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
	public void testDomain() {
		ast  = new AxiomSPARQLTranslator(createFileInputStream("examples/domainConstraint.owl"));
		List<String> queries = ast.convertToSPARQLDCQnot();
		assertEquals(1, queries.size());
		assertEquals(""
				, queries.get(0));
	}

	@Test
	public void testRestriction() {
		ast  = new AxiomSPARQLTranslator(createFileInputStream("examples/restrictionConstraint.owl"));
		List<String> queries = ast.convertToSPARQLDCQnot();
		assertEquals(1, queries.size());
		assertEquals(""
				, queries.get(0));
	}

	public FileInputStream createFileInputStream(String filePath) {
		try {
			return new FileInputStream(new File(filePath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
