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
import org.seerc.paasword.translator.QueryConstraint;

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
		List<QueryConstraint> queries = ast.convertToSPARQLDCQnot();
		assertEquals(1, queries.size());
		assertEquals(	"SELECT DISTINCT  *\n" + 
						"WHERE\n" + 
						"  { ?x0 <http://www.seerc.org/test/pellet-icv#is_responsible_for> ?x1\n" + 
						"    FILTER NOT EXISTS {?x0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Project_Leader> }\n" + 
						"  }\n" + 
						""
				, queries.get(0).getQuery());
	}

	@Test
	public void testRange() {
		ast  = new AxiomSPARQLTranslator(createFileInputStream("examples/rangeConstraint.owl"));
		List<QueryConstraint> queries = ast.convertToSPARQLDCQnot();
		assertEquals(1, queries.size());
		assertEquals(	"SELECT DISTINCT  *\n" + 
						"WHERE\n" + 
						"  { ?x1 <http://www.seerc.org/test/pellet-icv#is_responsible_for> ?x0\n" + 
						"    FILTER NOT EXISTS {?x0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Project> }\n" + 
						"  }\n" + 
						""
				, queries.get(0).getQuery());
	}

	@Test
	public void testSomeValuesFromRestriction() {
		ast  = new AxiomSPARQLTranslator(createFileInputStream("examples/someValuesFromConstraint.owl"));
		List<QueryConstraint> queries = ast.convertToSPARQLDCQnot();
		assertEquals(1, queries.size());
		assertEquals(	"SELECT DISTINCT  *\n" + 
						"WHERE\n" + 
						"  { ?x0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Supervisor>\n" + 
						"    FILTER NOT EXISTS {?x0 <http://www.seerc.org/test/pellet-icv#supervises> ?x1 .\n" + 
						"      ?x1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Employee>\n" + 
						"    }\n" + 
						"  }\n" + 
						""
				, queries.get(0).getQuery());
	}

	@Test
	public void testSomeValuesFromDatatypeRestriction() {
		ast  = new AxiomSPARQLTranslator(createFileInputStream("examples/someValuesFromDatatypeConstraint.owl"));
		List<QueryConstraint> queries = ast.convertToSPARQLDCQnot();
		assertEquals(1, queries.size());
		assertEquals(	"SELECT DISTINCT  *\n" + 
						"WHERE\n" + 
						"  { ?x0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Project>\n" + 
						"    FILTER NOT EXISTS {?x0 <http://www.seerc.org/test/pellet-icv#number> ?d0\n" + 
						"      FILTER ( ( ( ?d0 >= 0 ) && ( ?d0 < 5000 ) ) && ( datatype(?d0) = <http://www.w3.org/2001/XMLSchema#integer> ) )\n" + 
						"    }\n" + 
						"  }\n" + 
						""
				, queries.get(0).getQuery());
	}

	@Test
	public void testComplexRestriction() {
		ast  = new AxiomSPARQLTranslator(createFileInputStream("examples/complexRestrictionConstraint.owl"));
		List<QueryConstraint> queries = ast.convertToSPARQLDCQnot();
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
				, queries.get(0).getQuery());
	}

	@Test
	public void testMinCardinality() {
		ast  = new AxiomSPARQLTranslator(createFileInputStream("examples/minCardinalityConstraint.owl"));
		List<QueryConstraint> queries = ast.convertToSPARQLDCQnot();
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
				, queries.get(0).getQuery());
	}

	@Test
	public void testMinDatatypeCardinality() {
		ast  = new AxiomSPARQLTranslator(createFileInputStream("examples/minCardinalityDatatypeConstraint.owl"));
		List<QueryConstraint> queries = ast.convertToSPARQLDCQnot();
		assertEquals(1, queries.size());
		assertEquals(	"SELECT DISTINCT  *\n" + 
						"WHERE\n" + 
						"  { ?x0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Employee>\n" + 
						"    FILTER NOT EXISTS {?x0 <http://www.seerc.org/test/pellet-icv#works_on_Project_ID> ?d0\n" + 
						"      FILTER ( ( ( ?d0 >= 0 ) && ( ?d0 < 5000 ) ) && ( datatype(?d0) = <http://www.w3.org/2001/XMLSchema#integer> ) )\n" + 
						"      ?x0 <http://www.seerc.org/test/pellet-icv#works_on_Project_ID> ?d1\n" + 
						"      FILTER ( ( ( ?d1 >= 0 ) && ( ?d1 < 5000 ) ) && ( datatype(?d1) = <http://www.w3.org/2001/XMLSchema#integer> ) )\n" + 
						"      ?x0 <http://www.seerc.org/test/pellet-icv#works_on_Project_ID> ?d2\n" + 
						"      FILTER ( ( ( ?d2 >= 0 ) && ( ?d2 < 5000 ) ) && ( datatype(?d2) = <http://www.w3.org/2001/XMLSchema#integer> ) )\n" + 
						"      FILTER ( ?d0 != ?d1 )\n" + 
						"      FILTER ( ?d0 != ?d2 )\n" + 
						"      FILTER ( ?d1 != ?d2 )\n" + 
						"    }\n" + 
						"  }\n" + 
						""
				, queries.get(0).getQuery());
	}
	
	@Test
	public void testMaxCardinality() {
		ast  = new AxiomSPARQLTranslator(createFileInputStream("examples/maxCardinalityConstraint.owl"));
		List<QueryConstraint> queries = ast.convertToSPARQLDCQnot();
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
				, queries.get(0).getQuery());
	}
	
	@Test
	public void testMaxDatatypeCardinality() {
		ast  = new AxiomSPARQLTranslator(createFileInputStream("examples/maxCardinalityDatatypeConstraint.owl"));
		List<QueryConstraint> queries = ast.convertToSPARQLDCQnot();
		assertEquals(1, queries.size());
		assertEquals(	"SELECT DISTINCT  *\n" + 
						"WHERE\n" + 
						"  { ?x0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Employee> .\n" + 
						"    ?x0 <http://www.seerc.org/test/pellet-icv#works_on_Project_ID> ?d0\n" + 
						"    FILTER ( ( ( ?d0 >= 0 ) && ( ?d0 < 5000 ) ) && ( datatype(?d0) = <http://www.w3.org/2001/XMLSchema#integer> ) )\n" + 
						"    ?x0 <http://www.seerc.org/test/pellet-icv#works_on_Project_ID> ?d1\n" + 
						"    FILTER ( ( ( ?d1 >= 0 ) && ( ?d1 < 5000 ) ) && ( datatype(?d1) = <http://www.w3.org/2001/XMLSchema#integer> ) )\n" + 
						"    ?x0 <http://www.seerc.org/test/pellet-icv#works_on_Project_ID> ?d2\n" + 
						"    FILTER ( ( ( ?d2 >= 0 ) && ( ?d2 < 5000 ) ) && ( datatype(?d2) = <http://www.w3.org/2001/XMLSchema#integer> ) )\n" + 
						"    ?x0 <http://www.seerc.org/test/pellet-icv#works_on_Project_ID> ?d3\n" + 
						"    FILTER ( ( ( ?d3 >= 0 ) && ( ?d3 < 5000 ) ) && ( datatype(?d3) = <http://www.w3.org/2001/XMLSchema#integer> ) )\n" + 
						"    FILTER ( ?d0 != ?d1 )\n" + 
						"    FILTER ( ?d0 != ?d2 )\n" + 
						"    FILTER ( ?d1 != ?d2 )\n" + 
						"    FILTER ( ?d0 != ?d3 )\n" + 
						"    FILTER ( ?d1 != ?d3 )\n" + 
						"    FILTER ( ?d2 != ?d3 )\n" + 
						"  }\n" + 
						""
				, queries.get(0).getQuery());
	}
	
	@Test
	public void testExactCardinality() {
		ast  = new AxiomSPARQLTranslator(createFileInputStream("examples/exactCardinalityConstraint.owl"));
		List<QueryConstraint> queries = ast.convertToSPARQLDCQnot();
		assertEquals(1, queries.size());
		assertEquals(	"SELECT DISTINCT  *\n" + 
						"WHERE\n" + 
						"  { ?x0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Manager>\n" + 
						"      { ?x0 <http://www.seerc.org/test/pellet-icv#manages> ?x1 .\n" + 
						"        ?x1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Department> .\n" + 
						"        ?x0 <http://www.seerc.org/test/pellet-icv#manages> ?x2 .\n" + 
						"        ?x2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Department>\n" + 
						"        FILTER ( ?x1 != ?x2 )\n" + 
						"      }\n" + 
						"    UNION\n" + 
						"      { FILTER NOT EXISTS {?x0 <http://www.seerc.org/test/pellet-icv#manages> ?x3 .\n" + 
						"          ?x3 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Department>\n" + 
						"        }\n" + 
						"      }\n" + 
						"  }\n" + 
						""
				, queries.get(0).getQuery());
	}
	
	@Test
	public void testExactDatatypeCardinality() {
		ast  = new AxiomSPARQLTranslator(createFileInputStream("examples/exactCardinalityDatatypeConstraint.owl"));
		List<QueryConstraint> queries = ast.convertToSPARQLDCQnot();
		assertEquals(1, queries.size());
		assertEquals(	"SELECT DISTINCT  *\n" + 
						"WHERE\n" + 
						"  { ?x0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.seerc.org/test/pellet-icv#Employee>\n" + 
						"      { ?x0 <http://www.seerc.org/test/pellet-icv#works_on_Project_ID> ?d0\n" + 
						"        FILTER ( ( ( ?d0 >= 0 ) && ( ?d0 < 5000 ) ) && ( datatype(?d0) = <http://www.w3.org/2001/XMLSchema#integer> ) )\n" + 
						"        ?x0 <http://www.seerc.org/test/pellet-icv#works_on_Project_ID> ?d1\n" + 
						"        FILTER ( ( ( ?d1 >= 0 ) && ( ?d1 < 5000 ) ) && ( datatype(?d1) = <http://www.w3.org/2001/XMLSchema#integer> ) )\n" + 
						"        ?x0 <http://www.seerc.org/test/pellet-icv#works_on_Project_ID> ?d2\n" + 
						"        FILTER ( ( ( ?d2 >= 0 ) && ( ?d2 < 5000 ) ) && ( datatype(?d2) = <http://www.w3.org/2001/XMLSchema#integer> ) )\n" + 
						"        ?x0 <http://www.seerc.org/test/pellet-icv#works_on_Project_ID> ?d3\n" + 
						"        FILTER ( ( ( ?d3 >= 0 ) && ( ?d3 < 5000 ) ) && ( datatype(?d3) = <http://www.w3.org/2001/XMLSchema#integer> ) )\n" + 
						"        FILTER ( ?d0 != ?d1 )\n" + 
						"        FILTER ( ?d0 != ?d2 )\n" + 
						"        FILTER ( ?d1 != ?d2 )\n" + 
						"        FILTER ( ?d0 != ?d3 )\n" + 
						"        FILTER ( ?d1 != ?d3 )\n" + 
						"        FILTER ( ?d2 != ?d3 )\n" + 
						"      }\n" + 
						"    UNION\n" + 
						"      { FILTER NOT EXISTS {?x0 <http://www.seerc.org/test/pellet-icv#works_on_Project_ID> ?d4\n" + 
						"          FILTER ( ( ( ?d4 >= 0 ) && ( ?d4 < 5000 ) ) && ( datatype(?d4) = <http://www.w3.org/2001/XMLSchema#integer> ) )\n" + 
						"          ?x0 <http://www.seerc.org/test/pellet-icv#works_on_Project_ID> ?d5\n" + 
						"          FILTER ( ( ( ?d5 >= 0 ) && ( ?d5 < 5000 ) ) && ( datatype(?d5) = <http://www.w3.org/2001/XMLSchema#integer> ) )\n" + 
						"          ?x0 <http://www.seerc.org/test/pellet-icv#works_on_Project_ID> ?d6\n" + 
						"          FILTER ( ( ( ?d6 >= 0 ) && ( ?d6 < 5000 ) ) && ( datatype(?d6) = <http://www.w3.org/2001/XMLSchema#integer> ) )\n" + 
						"          FILTER ( ?d4 != ?d5 )\n" + 
						"          FILTER ( ?d4 != ?d6 )\n" + 
						"          FILTER ( ?d5 != ?d6 )\n" + 
						"        }\n" + 
						"      }\n" + 
						"  }\n" + 
						""
				, queries.get(0).getQuery());
	}
	
	@Test
	public void testRangeDatatype() {
		ast  = new AxiomSPARQLTranslator(createFileInputStream("examples/rangeDatatypeConstraint.owl"));
		List<QueryConstraint> queries = ast.convertToSPARQLDCQnot();
		assertEquals(1, queries.size());
		assertEquals(	"SELECT DISTINCT  *\n" + 
						"WHERE\n" + 
						"  { ?x0 <http://www.seerc.org/test/pellet-icv#dob> ?d0\n" + 
						"    FILTER ( ! ( datatype(?d0) = <http://www.w3.org/2001/XMLSchema#date> ) )\n" + 
						"  }\n" + 
						""
				, queries.get(0).getQuery());
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
