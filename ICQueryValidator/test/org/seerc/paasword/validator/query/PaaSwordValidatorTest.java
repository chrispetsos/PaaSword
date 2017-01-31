package org.seerc.paasword.validator.query;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;

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
		pwdv = new PaaSwordValidator(new FileInputStream(new File("Ontologies/final/policies/Car-Park-Security.ttl")));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testValidate() {
		assertNotNull(pwdv);
	}

}
