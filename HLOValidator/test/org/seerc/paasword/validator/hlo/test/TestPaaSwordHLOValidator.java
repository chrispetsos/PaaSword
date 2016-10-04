package org.seerc.paasword.validator.hlo.test;

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
import org.seerc.paasword.validator.hlo.HLOValidator;
import org.seerc.paasword.validator.hlo.ValidationError;

public class TestPaaSwordHLOValidator {
	HLOValidator hlov;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		InputStream hloOWL = new FileInputStream(new File("Ontologies/hlo/HigherLevelOntologyOWL.ttl"));
		//InputStream hloOWLInstantiation = new FileInputStream(new File("Ontologies/hlo/HigherLevelOntologyOWLInstantiation.ttl"));
		InputStream pwdcm = new FileInputStream(new File("Ontologies/context-aware-security-models/PaaSwordContextModel_v2.ttl"));
		InputStream pwdcpm = new FileInputStream(new File("Ontologies/context-aware-security-models/PaaSwordContextPatternModel_v2.ttl"));
		InputStream pwdddem = new FileInputStream(new File("Ontologies/context-aware-security-models/PaaSwordDDEModel_v2.ttl"));
		InputStream pwdpm = new FileInputStream(new File("Ontologies/context-aware-security-models/PaaSwordPermissionModel_v2.ttl"));
		InputStream pwdPolicyModel = new FileInputStream(new File("Ontologies/policy-models/Security-Policy.ttl"));

		hlov = new HLOValidator(hloOWL/*, hloOWLInstantiation*/, pwdcm, pwdcpm, pwdddem, pwdpm, pwdPolicyModel);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExactlyPropertyConstraint() {
		String exactlyPropertyConstraint = 
				"pac:ABACRule rdfs:subClassOf\n" + 
				"              [ a hlo:ExactlyPropertyConstraint ;\n" + 
				"                hlo:onProperty pac:hasControlledObject ;\n" + 
				"                hlo:qualifiedCardinality \"1\"^^xsd:nonNegativeInteger ;\n" + 
				"                hlo:onClass pcm:Relational\n" + 
				"              ] .";
		
		String policyOntology = 
				"@prefix ex1: <http://www.paasword.eu/security-policy/use-cases/car-park#>.\n" + 
				"ex1:ABACRule_1 a pac:ABACRule;\n" + 
				"	pac:hasControlledObject ex1:PaymentsTable;\n" + 
				"	pac:hasAuthorisation pac:positive;\n" + 
				"	pac:hasAction ex1:Read;\n" + 
				"	pac:hasActor ex1:ParkingEmployee;\n" + 
				"	pac:hasContextExpression ex1:expr.";
		
		hlov.addOntology(exactlyPropertyConstraint);
		hlov.addOntology(policyOntology);
		
		List<ValidationError> validationProblems = hlov.checkExactlyPropertyConstraints();
		assertEquals(1, validationProblems.size());
	}

}
