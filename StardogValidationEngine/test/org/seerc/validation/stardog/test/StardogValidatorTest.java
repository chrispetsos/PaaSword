package org.seerc.validation.stardog.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seerc.validation.stardog.StardogValidator;

import com.complexible.common.protocols.server.Server;
import com.complexible.common.protocols.server.ServerException;
import com.complexible.stardog.Stardog;
import com.complexible.stardog.protocols.snarl.SNARLProtocolConstants;

public class StardogValidatorTest {

	static Server aServer = null;

	StardogValidator sv;
	InputStream constraints;
	InputStream[] ontologies;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			aServer = Stardog
					.buildServer()
					.bind(SNARLProtocolConstants.EMBEDDED_ADDRESS)
					.start();
		} catch (ServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		aServer.stop();
	}

	@Before
	public void setUp() throws Exception {
		sv = new StardogValidator();
		constraints = new FileInputStream(new File("data/PaasWordConstraints.ttl"));
		ArrayList<InputStream> ontologiesList = new ArrayList<>();
		ontologiesList.add(new FileInputStream(new File("data/ContradictingRulesExample.ttl")));
		ontologiesList.add(new FileInputStream(new File("data/PaaSwordContextModel_v2.ttl")));
		ontologies = ontologiesList.toArray(new InputStream[0]);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testValidate() {
		String validationResult = sv.validate(constraints, ontologies);
		
		assertEquals("OK", validationResult);
	}

	@Test
	public void testExplain() {
		String explanation = sv.explain(ontologies, "http://www.paasword.eu/security-policy/test-cases/car-park-contradicting#PaymentsTable http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.paasword-project.eu/ontologies/casm/2016/05/20#Object");
		
		assertNotNull(explanation);
	}

	@Test
	public void testExplainWithNamespaces() {
		String explanation = sv.explain(ontologies, "test1:PaymentsTable a pcm:Object");
		
		assertNotNull(explanation);
	}
}
