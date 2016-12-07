package org.seerc.paasword.theoremprover.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seerc.paasword.theoremprover.TautologyChecker;
import org.seerc.paasword.validator.query.JenaDataSourceInferred;

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
		tc = new TautologyChecker(new JenaDataSourceInferred(new FileInputStream(new File("Ontologies/SubclassSubsumption.ttl"))));
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
		assertTrue(tc.isTautology("http://www.paasword.eu/security-policy/use-cases/car-park#expr", "http://www.paasword.eu/security-policy/use-cases/car-park#expr2"));
	}

}
