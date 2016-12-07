package org.seerc.paasword.theoremprover.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seerc.paasword.theoremprover.TautologyChecker;
import org.seerc.paasword.validator.query.JenaDataSourceInferred;
import org.snim2.checker.test.CheckerTestHelper;

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
		assertTrue(tc.isTautology("ex1:expr", "ex1:expr2"));
	}
	
	@Test
	public void testConvertToPropositionalExpression() {
		checkOWLResourceToProposition("ex1:expr", "ex1EmployeeWorkingHours AND (ex1Parking1 OR ex1Parking2)");
	}

	private void checkOWLResourceToProposition(String resourceUri, String desiredProposition) {
		String propositionalExpression = tc.convertToPropositionalExpression(resourceUri);
		CheckerTestHelper checker = new CheckerTestHelper();
		try {
			assertTrue(checker.checkInputStream(new ByteArrayInputStream((propositionalExpression + " <=> " + desiredProposition).getBytes())));
		} catch (IOError | IOException e) {
			e.printStackTrace();
		}
	}
}
