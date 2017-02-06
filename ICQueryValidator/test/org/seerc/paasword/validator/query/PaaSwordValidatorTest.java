package org.seerc.paasword.validator.query;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PaaSwordValidatorTest {

	PaaSwordValidator pwdv;
	
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
	public void testValidate() throws Exception {
		pwdv = new PaaSwordValidator(new FileInputStream(new File("Ontologies/final/policies/Car-Park-Security.ttl")));
		assertNotNull(pwdv);
		
		List<QueryValidatorErrors> validationResult = pwdv.validate();
		assertEquals(2, validationResult.size());
	}

	@Test
	public void testAbacRulesFull() throws Exception {
		InputStream policy = new FileInputStream(new File("Ontologies/policy-models/Car-Park-Security-Extracted-Constraints-Full.ttl"));
		
		pwdv = new PaaSwordValidator(policy);
		
		assertEquals(0, pwdv.validate().size());
	}	

	@Test
	public void testAbacRulesSimple() throws Exception {
		InputStream policy = new FileInputStream(new File("Ontologies/policy-models/Car-Park-Security-Extracted-Constraints-Simple.ttl"));
		
		pwdv = new PaaSwordValidator(policy);
		
		assertEquals(0, pwdv.validate().size());
	}	

	@Test
	public void testAbacRulesSimpleFailing() throws Exception {
		InputStream policy = new FileInputStream(new File("Ontologies/policy-models/Car-Park-Security-Extracted-Constraints-Simple-Failing.ttl"));
		
		pwdv = new PaaSwordValidator(policy);
		
		assertEquals(9, pwdv.validate().size());
	}	

	@Test
	public void testSubclassSubsumptionContradiction() throws Exception {
		InputStream policy = new FileInputStream(new File("Ontologies/subsumptive/SubclassSubsumption.ttl"));
		
		pwdv = new PaaSwordValidator(policy);
		
		assertEquals(4, pwdv.validate().size());
	}
}
