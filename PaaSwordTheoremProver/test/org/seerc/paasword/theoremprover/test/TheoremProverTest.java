package org.seerc.paasword.theoremprover.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seerc.paasword.theoremprover.TheoremProver;

public class TheoremProverTest {

	TheoremProver tp;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		tp = new TheoremProver(new FileInputStream(new File("Ontologies/SubclassSubsumption.ttl")));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreate() 
	{
		assertNotNull(tp);
	}

	@Test
	public void testContextExpressionEqualSubsumption() 
	{
		boolean exprSubsumesExpr2 = tp.contextExpressionSubsumes("http://www.paasword.eu/security-policy/use-cases/car-park#expr", "http://www.paasword.eu/security-policy/use-cases/car-park#expr2");
		assertTrue(exprSubsumesExpr2);
	}

	@Test
	public void testContextExpressionNotEqualSubsumption() 
	{
		boolean expr3NotSubsumesExpr4 = tp.contextExpressionSubsumes("http://www.paasword.eu/security-policy/use-cases/car-park#expr3", "http://www.paasword.eu/security-policy/use-cases/car-park#expr4");
		assertFalse(expr3NotSubsumesExpr4);
	}

	@Test
	public void testContextExpressionSubsumptionWithNamespace() 
	{
		boolean exprSubsumesExpr2 = tp.contextExpressionSubsumes("ex1:expr", "ex1:expr2");
		assertTrue(exprSubsumesExpr2);
	}

	@Test
	public void testContextExpressionSubsumptionWithLessParams() 
	{
		boolean exprSubsumesExpr2 = tp.contextExpressionSubsumes("ex1:expr5", "ex1:expr6");
		assertTrue(exprSubsumesExpr2);
	}

}
