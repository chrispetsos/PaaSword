package org.seerc.paasword.theoremprover.test;

import static org.junit.Assert.*;

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
		tp = new TheoremProver();
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
	public void testContextExpressionSubsumption() 
	{
		boolean exprSubsumesExpr2 = tp.contextExpressionSubsumes("http://www.paasword.eu/security-policy/use-cases/car-park#expr", "http://www.paasword.eu/security-policy/use-cases/car-park#expr2");
		assertTrue(exprSubsumesExpr2);
	}

}
