package org.seerc.paasword.validator.engine.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seerc.paasword.validator.engine.ProblematicRules;
import org.seerc.paasword.validator.engine.SecurityPolicyValidator;

// TODO: This should be deprecated.
public class SecurityPolicyValidatorTest {

	SecurityPolicyValidator scv;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		scv = new SecurityPolicyValidator("/Ontologies/test/ContradictingRulesExample.ttl");		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExtractABACRules() {
		assertEquals(3, scv.getABACRules().size());
		assertEquals("http://www.paasword.eu/security-policy/test-cases/car-park-contradicting#ABACRule_3", scv.getABACRules().get(0).toString());
		assertEquals("http://www.paasword.eu/security-policy/test-cases/car-park-contradicting#ABACRule_2", scv.getABACRules().get(1).toString());
		assertEquals("http://www.paasword.eu/security-policy/test-cases/car-park-contradicting#ABACRule_1", scv.getABACRules().get(2).toString());
	}
	
	@Test
	public void testFindContradictingRules() {
		List<ProblematicRules> contradictions = scv.findContradictingRules();
		
		assertEquals(3, contradictions.size());
	}

	@Test
	public void testFindPoliciesThatContainContradictions() {
		List<String> contradictions = scv.findPoliciesThatContainContradictions();
		
		assertEquals(2, contradictions.size());
	}

	@Test
	public void testRuleSubsumptions() {
		List<ProblematicRules> subsumptions = scv.findRuleSubsumptions();
		
		assertEquals(3, subsumptions.size());
	}

}
