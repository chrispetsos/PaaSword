package org.seerc.paasword.validator.engine.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seerc.paasword.validator.engine.JenaDataSourceInferred;
import org.seerc.paasword.validator.engine.RuleAntecedentConclusionEnhnacer;
import org.seerc.paasword.validator.engine.SubclassSubsumptionsEngine;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class RuleAntecedentConclusionEnhnacerTest {

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
	public void testEnhanceModel() throws Exception {
		JenaDataSourceInferred jdsi = new JenaDataSourceInferred(new FileInputStream(new File("Ontologies/test/RuleAntecedentConclusion.ttl")));
		
		assertNotNull(jdsi);

		RuleAntecedentConclusionEnhnacer race = new RuleAntecedentConclusionEnhnacer(jdsi);
		
		assertNotNull(race);
		
		race.enhanceModel();
		
		List<Individual> abacRuleIndividuals = ((OntModel)jdsi.getModel()).listIndividuals(((OntModel)jdsi.getModel()).createClass("http://www.paasword.eu/security-policy/seerc/pac#ABACRule")).toList();
		assertEquals(2, abacRuleIndividuals.size());
		
		Individual abacRule1 = abacRuleIndividuals.get(0);
		Individual abacRule2 = abacRuleIndividuals.get(1);

		Individual abacRule1Antecedent = abacRule1.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasAntecedent")).next().as(Individual.class);
		assertNotNull(abacRule1Antecedent);

		Individual abacRule1Conclusion = abacRule1.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasConclusion")).next().as(Individual.class);
		assertNotNull(abacRule1Conclusion);

		// abacRule1Antecedent should have all properties of abacRule1 except hasAuthorisation
		assertEqualProperties(jdsi, abacRule1, abacRule1Antecedent, "http://www.paasword.eu/security-policy/seerc/pac#hasControlledObject");
		assertNotEqualProperties(jdsi, abacRule1, abacRule1Antecedent, "http://www.paasword.eu/security-policy/seerc/pac#hasAuthorisation");
		assertEqualProperties(jdsi, abacRule1, abacRule1Antecedent, "http://www.paasword.eu/security-policy/seerc/pac#hasAction");
		assertEqualProperties(jdsi, abacRule1, abacRule1Antecedent, "http://www.paasword.eu/security-policy/seerc/pac#hasActor");
		assertEqualProperties(jdsi, abacRule1, abacRule1Antecedent, "http://www.paasword.eu/security-policy/seerc/pac#hasContextExpression");
		
		// abacRule2Antecedent should only have the hasAuthorisation property of abacRule1
		assertNotEqualProperties(jdsi, abacRule1, abacRule1Conclusion, "http://www.paasword.eu/security-policy/seerc/pac#hasControlledObject");
		assertEqualProperties(jdsi, abacRule1, abacRule1Conclusion, "http://www.paasword.eu/security-policy/seerc/pac#hasAuthorisation");
		assertNotEqualProperties(jdsi, abacRule1, abacRule1Conclusion, "http://www.paasword.eu/security-policy/seerc/pac#hasAction");
		assertNotEqualProperties(jdsi, abacRule1, abacRule1Conclusion, "http://www.paasword.eu/security-policy/seerc/pac#hasActor");
		assertNotEqualProperties(jdsi, abacRule1, abacRule1Conclusion, "http://www.paasword.eu/security-policy/seerc/pac#hasContextExpression");
		
		// repeat for rule 2
		Individual abacRule2Antecedent = abacRule2.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasAntecedent")).next().as(Individual.class);
		assertNotNull(abacRule2Antecedent);

		Individual abacRule2Conclusion = abacRule2.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/pac#hasConclusion")).next().as(Individual.class);
		assertNotNull(abacRule2Conclusion);

		assertEqualProperties(jdsi, abacRule2, abacRule2Antecedent, "http://www.paasword.eu/security-policy/seerc/pac#hasControlledObject");
		assertNotEqualProperties(jdsi, abacRule2, abacRule2Antecedent, "http://www.paasword.eu/security-policy/seerc/pac#hasAuthorisation");
		assertEqualProperties(jdsi, abacRule2, abacRule2Antecedent, "http://www.paasword.eu/security-policy/seerc/pac#hasAction");
		assertEqualProperties(jdsi, abacRule2, abacRule2Antecedent, "http://www.paasword.eu/security-policy/seerc/pac#hasActor");
		assertEqualProperties(jdsi, abacRule2, abacRule2Antecedent, "http://www.paasword.eu/security-policy/seerc/pac#hasContextExpression");
		
		assertNotEqualProperties(jdsi, abacRule2, abacRule2Conclusion, "http://www.paasword.eu/security-policy/seerc/pac#hasControlledObject");
		assertEqualProperties(jdsi, abacRule2, abacRule2Conclusion, "http://www.paasword.eu/security-policy/seerc/pac#hasAuthorisation");
		assertNotEqualProperties(jdsi, abacRule2, abacRule2Conclusion, "http://www.paasword.eu/security-policy/seerc/pac#hasAction");
		assertNotEqualProperties(jdsi, abacRule2, abacRule2Conclusion, "http://www.paasword.eu/security-policy/seerc/pac#hasActor");
		assertNotEqualProperties(jdsi, abacRule2, abacRule2Conclusion, "http://www.paasword.eu/security-policy/seerc/pac#hasContextExpression");
		
	}

	private void assertEqualProperties(JenaDataSourceInferred jdsi, Individual abacRule, Individual abacRuleAntecedent, String property) {
		assertEquals(
				abacRule.getPropertyValue(((OntModel)jdsi.getModel()).createProperty(property))
				, 
				abacRuleAntecedent.getPropertyValue(((OntModel)jdsi.getModel()).createProperty(property)));
		;
	}

	private void assertNotEqualProperties(JenaDataSourceInferred jdsi, Individual abacRule, Individual abacRuleAntecedent, String property) {
		assertNotEquals(
				abacRule.getPropertyValue(((OntModel)jdsi.getModel()).createProperty(property))
				, 
				abacRuleAntecedent.getPropertyValue(((OntModel)jdsi.getModel()).createProperty(property)));
		;
	}

}
