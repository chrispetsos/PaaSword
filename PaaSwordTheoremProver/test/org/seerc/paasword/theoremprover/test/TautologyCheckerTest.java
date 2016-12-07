package org.seerc.paasword.theoremprover.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seerc.paasword.theoremprover.TautologyChecker;

public class TautologyCheckerTest {

	TautologyChecker tc;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		tc = new TautologyChecker();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreate() {
		assertNotNull(tc);
	}

	@Test
	public void testIsTautology() {
		fail("Not yet implemented");
	}

}
