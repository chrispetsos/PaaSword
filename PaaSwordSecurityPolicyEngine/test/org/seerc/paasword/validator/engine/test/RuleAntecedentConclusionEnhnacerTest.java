package org.seerc.paasword.validator.engine.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seerc.paasword.validator.engine.JenaDataSourceInferred;
import org.seerc.paasword.validator.engine.RuleAntecedentConclusionEnhnacer;
import org.seerc.paasword.validator.engine.SubclassSubsumptionsEngine;

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
	}

}
