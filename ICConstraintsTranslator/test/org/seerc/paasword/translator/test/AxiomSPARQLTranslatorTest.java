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
	public void testSomeValuesFromRestriction() {
		ast  = new AxiomSPARQLTranslator(createFileInputStream("examples/someValuesFromConstraint.owl"));
		List<String> queries = ast.convertToSPARQLDCQnot();
		assertEquals(1, queries.size());
		assertEquals(	"SELECT DISTINCT  *\n" + 
						"WHERE\n" + 
						"  { ?x0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Supervisor>\n" + 
						"    FILTER NOT EXISTS {?x0 <http://www.seerc.org/test/pellet-icv#supervises> ?x1 .\n" + 
						"      ?x1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Employee>\n" + 
						"    }\n" + 
						"  }\n" + 
						""
				, queries.get(0));
	}

	@Test
	public void testComplexRestriction() {
		ast  = new AxiomSPARQLTranslator(createFileInputStream("examples/complexRestrictionConstraint.owl"));
		List<String> queries = ast.convertToSPARQLDCQnot();
		assertEquals(1, queries.size());
		assertEquals(	"SELECT DISTINCT  *\n" + 
						"WHERE\n" + 
						"  { ?x0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Employee>\n" + 
						"    FILTER NOT EXISTS {?x0 <http://www.seerc.org/test/pellet-icv#works_on> ?x1\n" + 
						"        { ?x1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Project> }\n" + 
						"      UNION\n" + 
						"        { ?x1 <http://www.seerc.org/test/pellet-icv#manages> ?s0 .\n" + 
						"          ?s0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Department>\n" + 
						"        }\n" + 
						"      UNION\n" + 
						"        { ?x1 <http://www.seerc.org/test/pellet-icv#supervises> ?s1 .\n" + 
						"          ?s1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Employee> .\n" + 
						"          ?s1 <http://www.seerc.org/test/pellet-icv#works_on> ?s2 .\n" + 
						"          ?s2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Project>\n" + 
						"        }\n" + 
						"    }\n" + 
						"  }\n" + 
						""
				, queries.get(0));
	}

	@Test
	public void testMinCardinality() {
		ast  = new AxiomSPARQLTranslator(createFileInputStream("examples/minCardinalityConstraint.owl"));
		List<String> queries = ast.convertToSPARQLDCQnot();
		assertEquals(1, queries.size());
		assertEquals(	"SELECT DISTINCT  *\n" + 
						"WHERE\n" + 
						"  { ?x0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Employee>\n" + 
						"    FILTER NOT EXISTS {?x0 <http://www.seerc.org/test/pellet-icv#works_on> ?x1 .\n" + 
						"      ?x1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Project> .\n" + 
						"      ?x0 <http://www.seerc.org/test/pellet-icv#works_on> ?x2 .\n" + 
						"      ?x2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Project> .\n" + 
						"      ?x0 <http://www.seerc.org/test/pellet-icv#works_on> ?x3 .\n" + 
						"      ?x3 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Project>\n" + 
						"      FILTER ( ?x1 != ?x2 )\n" + 
						"      FILTER ( ?x1 != ?x3 )\n" + 
						"      FILTER ( ?x2 != ?x3 )\n" + 
						"    }\n" + 
						"  }\n" + 
						""
				, queries.get(0));
	}

	@Test
	public void testMaxCardinality() {
		ast  = new AxiomSPARQLTranslator(createFileInputStream("examples/maxCardinalityConstraint.owl"));
		List<String> queries = ast.convertToSPARQLDCQnot();
		assertEquals(1, queries.size());
		assertEquals(	"SELECT DISTINCT  *\n" + 
						"WHERE\n" + 
						"  { ?x0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Employee> .\n" + 
						"    ?x0 <http://www.seerc.org/test/pellet-icv#works_on> ?x1 .\n" + 
						"    ?x1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Project> .\n" + 
						"    ?x0 <http://www.seerc.org/test/pellet-icv#works_on> ?x2 .\n" + 
						"    ?x2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Project> .\n" + 
						"    ?x0 <http://www.seerc.org/test/pellet-icv#works_on> ?x3 .\n" + 
						"    ?x3 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Project> .\n" + 
						"    ?x0 <http://www.seerc.org/test/pellet-icv#works_on> ?x4 .\n" + 
						"    ?x4 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Project>\n" + 
						"    FILTER ( ?x1 != ?x2 )\n" + 
						"    FILTER ( ?x1 != ?x3 )\n" + 
						"    FILTER ( ?x2 != ?x3 )\n" + 
						"    FILTER ( ?x1 != ?x4 )\n" + 
						"    FILTER ( ?x2 != ?x4 )\n" + 
						"    FILTER ( ?x3 != ?x4 )\n" + 
						"  }\n" + 
						""
				, queries.get(0));
	}
	
	@Test
	public void testExactCardinality() {
		ast  = new AxiomSPARQLTranslator(createFileInputStream("examples/exactCardinalityConstraint.owl"));
		List<String> queries = ast.convertToSPARQLDCQnot();
		assertEquals(1, queries.size());
		assertEquals(	"SELECT DISTINCT  *\n" + 
						"WHERE\n" + 
						"  { ?x0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Employee>\n" + 
						"    FILTER NOT EXISTS {?x0 <http://www.seerc.org/test/pellet-icv#works_on> ?x1 .\n" + 
						"      ?x1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Project> .\n" + 
						"      ?x0 <http://www.seerc.org/test/pellet-icv#works_on> ?x2 .\n" + 
						"      ?x2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Project> .\n" + 
						"      ?x0 <http://www.seerc.org/test/pellet-icv#works_on> ?x3 .\n" + 
						"      ?x3 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Project>\n" + 
						"      FILTER ( ?x1 != ?x2 )\n" + 
						"      FILTER ( ?x1 != ?x3 )\n" + 
						"      FILTER ( ?x2 != ?x3 )\n" + 
						"    }\n" + 
						"  }\n" + 
						""
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
