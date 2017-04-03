package org.seerc.paasword.validator.query;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
		InputStream policy = getClass().getResourceAsStream("/Ontologies/final/policies/Car-Park-Security.ttl");
		InputStream contextModel = getClass().getResourceAsStream("/Ontologies/final/models/PaaSwordContextModel.ttl");
		InputStream payload = new SequenceInputStream(Collections.enumeration(Arrays.asList(contextModel, policy)));

		pwdv = new PaaSwordValidator(payload);
		assertNotNull(pwdv);
		
		List<QueryValidatorErrors> validationResult = pwdv.validate();
		assertEquals(2, validationResult.size());
	}

	@Test
	public void testAbacRulesFull() throws Exception {
		InputStream policy = getClass().getResourceAsStream("/Ontologies/policy-models/Car-Park-Security-Extracted-Constraints-Full.ttl");
		InputStream contextModel = getClass().getResourceAsStream("/Ontologies/final/models/PaaSwordContextModel.ttl");
		InputStream payload = new SequenceInputStream(Collections.enumeration(Arrays.asList(contextModel, policy)));

		pwdv = new PaaSwordValidator(payload);
		
		assertEquals(0, pwdv.validate().size());
	}	

	@Test
	public void testAbacRulesSimple() throws Exception {
		InputStream policy = getClass().getResourceAsStream("/Ontologies/policy-models/Car-Park-Security-Extracted-Constraints-Simple.ttl");
		InputStream contextModel = getClass().getResourceAsStream("/Ontologies/final/models/PaaSwordContextModel.ttl");
		InputStream payload = new SequenceInputStream(Collections.enumeration(Arrays.asList(contextModel, policy)));

		pwdv = new PaaSwordValidator(payload);
		
		assertEquals(0, pwdv.validate().size());
	}	

	@Test
	public void testAbacRulesSimpleFailing() throws Exception {
		InputStream policy = getClass().getResourceAsStream("/Ontologies/policy-models/Car-Park-Security-Extracted-Constraints-Simple-Failing.ttl");
		InputStream contextModel = getClass().getResourceAsStream("/Ontologies/final/models/PaaSwordContextModel.ttl");
		InputStream payload = new SequenceInputStream(Collections.enumeration(Arrays.asList(contextModel, policy)));

		pwdv = new PaaSwordValidator(payload);
		
		assertEquals(12, pwdv.validate().size());
	}	

	@Test
	public void testSubclassSubsumptionContradiction() throws Exception {
		InputStream policy = getClass().getResourceAsStream("/Ontologies/subsumptive/SubclassSubsumption.ttl");
		InputStream contextModel = getClass().getResourceAsStream("/Ontologies/final/models/PaaSwordContextModel.ttl");
		InputStream payload = new SequenceInputStream(Collections.enumeration(Arrays.asList(contextModel, policy)));

		pwdv = new PaaSwordValidator(payload);
		
		assertEquals(4, pwdv.validate().size());
	}
	
	
	@Test
	public void testOldContradictingRulesExample() throws Exception
	{
		InputStream policy = getClass().getResourceAsStream("/Ontologies/test/ContradictingRulesExample.ttl");
		InputStream contextModel = getClass().getResourceAsStream("/Ontologies/final/models/PaaSwordContextModel.ttl");
		InputStream payload = new SequenceInputStream(Collections.enumeration(Arrays.asList(contextModel, policy)));

		pwdv = new PaaSwordValidator(payload);
		
		assertEquals(2, pwdv.validate().size());
	}

	@Test
	public void testPatiniErrors() throws Exception
	{
		InputStream contextModelWithPolicy = getClass().getResourceAsStream("/Ontologies/patini/test-ALL.ttl");
		InputStream payload = new SequenceInputStream(Collections.enumeration(Arrays.asList(contextModelWithPolicy)));

		pwdv = new PaaSwordValidator(payload);
		
		List<QueryValidatorErrors> validationErrors = pwdv.validate();

		this.printValidationReport(validationErrors);
		
		assertEquals(19, validationErrors.size());
	}

	@Test
	public void testPatini2Errors() throws Exception
	{
		InputStream contextModelWithPolicy = getClass().getResourceAsStream("/Ontologies/patini/test2-ALL-corrected.ttl");
		InputStream payload = new SequenceInputStream(Collections.enumeration(Arrays.asList(contextModelWithPolicy)));

		pwdv = new PaaSwordValidator(payload);
		
		List<QueryValidatorErrors> validationErrors = pwdv.validate();

		this.printValidationReport(validationErrors);
		//pwdv.jds.printModel(System.out);
		
		assertEquals(12, validationErrors.size());
	}

	@Test
	public void testPolicySubsumption() throws Exception
	{
		InputStream contextModelWithPolicy = getClass().getResourceAsStream("/Ontologies/subsumptive/PolicySubsumption.ttl");
		InputStream payload = new SequenceInputStream(Collections.enumeration(Arrays.asList(contextModelWithPolicy)));

		pwdv = new PaaSwordValidator(payload);
		
		List<QueryValidatorErrors> validationErrors = pwdv.validate();

		this.printValidationReport(validationErrors);
		
		//pwdv.jds.printModel(new FileOutputStream(new File("testPolicySubsumption.ttl"));
		
		assertEquals(8, validationErrors.size());
	}

	@Test
	public void testPolicySetSubsumption() throws Exception
	{
		InputStream contextModelWithPolicy = getClass().getResourceAsStream("/Ontologies/subsumptive/PolicySetSubsumption.ttl");
		InputStream payload = new SequenceInputStream(Collections.enumeration(Arrays.asList(contextModelWithPolicy)));

		pwdv = new PaaSwordValidator(payload);
		
		List<QueryValidatorErrors> validationErrors = pwdv.validate();

		this.printValidationReport(validationErrors);
		
		//pwdv.jds.printModel(new FileOutputStream(new File("testPolicySetSubsumption.ttl")));
		
		assertEquals(13, validationErrors.size());
	}

	@Test
	public void testContradictingRules() throws Exception
	{
		InputStream contextModelWithPolicy = getClass().getResourceAsStream("/Ontologies/test/ContradictingRulesExample.ttl");
		InputStream payload = new SequenceInputStream(Collections.enumeration(Arrays.asList(contextModelWithPolicy)));

		pwdv = new PaaSwordValidator(payload);
		
		List<QueryValidatorErrors> validationErrors = pwdv.validate();

		this.printValidationReport(validationErrors);
		
		pwdv.jds.printModel(System.out);
		
		assertEquals(13, validationErrors.size());
	}

	private void printValidationReport(List<QueryValidatorErrors> validationErrors)
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		System.out.println(gson.toJson(validationErrors));
	}

}
