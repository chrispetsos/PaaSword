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
import org.seerc.paasword.validator.engine.PolicyRulesOrderEnhancer;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.NodeIterator;

public class PolicyRulesOrderEnhancerTest {

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
		JenaDataSourceInferred jdsi = new JenaDataSourceInferred(new FileInputStream(new File("Ontologies/subsumptive/PolicySubsumption.ttl")));
		
		assertNotNull(jdsi);

		PolicyRulesOrderEnhancer proe = new PolicyRulesOrderEnhancer(jdsi);

		assertNotNull(proe);
		
		proe.enhanceModel();
		
		Individual r1 = ((OntModel)jdsi.getModel()).getResource("http://www.paasword.eu/security-policy/use-cases/car-park#R1").as(Individual.class);
		Individual r2 = ((OntModel)jdsi.getModel()).getResource("http://www.paasword.eu/security-policy/use-cases/car-park#R2").as(Individual.class);
		Individual r3 = ((OntModel)jdsi.getModel()).getResource("http://www.paasword.eu/security-policy/use-cases/car-park#R3").as(Individual.class);
		Individual r4 = ((OntModel)jdsi.getModel()).getResource("http://www.paasword.eu/security-policy/use-cases/car-park#R4").as(Individual.class);
		Individual r5 = ((OntModel)jdsi.getModel()).getResource("http://www.paasword.eu/security-policy/use-cases/car-park#R5").as(Individual.class);
		
		assertTrue(this.hasNext(jdsi, r1, r2));
		assertTrue(this.hasNext(jdsi, r2, r3));
		assertFalse(this.hasNext(jdsi, r3, r4));
		assertTrue(this.hasNext(jdsi, r4, r5));
		
		assertFalse(this.hasNext(jdsi, r3));
		assertFalse(this.hasNext(jdsi, r5));
		
		assertTrue(this.hasPrevious(jdsi, r2, r1));
		assertTrue(this.hasPrevious(jdsi, r3, r2));
		assertFalse(this.hasPrevious(jdsi, r4, r3));		
		assertTrue(this.hasPrevious(jdsi, r5, r4));

		assertFalse(this.hasPrevious(jdsi, r1));
		assertFalse(this.hasPrevious(jdsi, r4));
	}

	private boolean hasNext(JenaDataSourceInferred jdsi, Individual r1, Individual r2)
	{
		NodeIterator nextElements = r1.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/list#hasNext"));
		while(nextElements.hasNext())
		{
			if(nextElements.next().equals(r2))
			{
				return true;
			}
		}

		return false;
	}

	private boolean hasNext(JenaDataSourceInferred jdsi, Individual r)
	{
		NodeIterator nextElements = r.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/list#hasNext"));
		
		return nextElements.hasNext();
	}

	private boolean hasPrevious(JenaDataSourceInferred jdsi, Individual r1, Individual r2)
	{
		NodeIterator previousElements = r1.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/list#hasPrevious"));
		while(previousElements.hasNext())
		{
			if(previousElements.next().equals(r2))
			{
				return true;
			}
		}

		return false;
	}

	private boolean hasPrevious(JenaDataSourceInferred jdsi, Individual r)
	{
		NodeIterator previousElements = r.listPropertyValues(((OntModel)jdsi.getModel()).createProperty("http://www.paasword.eu/security-policy/seerc/list#hasPrevious"));
		
		return previousElements.hasNext();
	}

}
