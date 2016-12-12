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
import org.seerc.paasword.validator.engine.JenaDataSourceInferred;
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
		assertTrue(tc.isTautology("ex1:expr2", "ex1:expr"));
		assertTrue(tc.isTautology("ex1:expr7", "ex1:expr8"));
		assertFalse(tc.isTautology("ex1:expr8", "ex1:expr7"));
		assertTrue(tc.isTautology("ex1:expr9", "ex1:expr10"));
		assertFalse(tc.isTautology("ex1:expr10", "ex1:expr9"));
		assertFalse(tc.isTautology("ex1:expr11", "ex1:expr12"));
		assertFalse(tc.isTautology("ex1:expr12", "ex1:expr11"));
	}
	
	@Test
	public void testConvertToPropositionalExpression() {
		checkOWLResourceToProposition("ex1:expr", "ex1EmployeeWorkingHours AND (ex1Parking1 OR ex1Parking2)");
		checkOWLResourceToProposition("ex1:expr1", "ex1Parking1 OR ex1Parking2");
		checkOWLResourceToProposition("ex1:expr2", "(ex1Parking1 OR ex1Parking2) AND ex1EmployeeWorkingHours");
		checkOWLResourceToProposition("ex1:expr3", "(ex1EmployeeWorkingHours AND ex1Parking1) OR (ex1EmployeeWorkingHours AND ex1Parking2)");
		checkOWLResourceToProposition("ex1:expr4", "ex1EmployeeWorkingHours AND ex1PaymentsTable");
		checkOWLResourceToProposition("ex1:expr5", "ex1EmployeeWorkingHours AND (ex1Parking1 OR ex1Parking2)");
		checkOWLResourceToProposition("ex1:expr6", "ex1EmployeeWorkingHours");
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
