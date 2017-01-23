package org.seerc.paasword.swrl.builtin.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seerc.paasword.swrl.builtin.SWRLPropositionalBuiltin;
import org.seerc.paasword.validator.engine.JenaDataSourceInferred;

import com.clarkparsia.pellet.rules.builtins.BuiltInRegistry;

import cz.makub.swrl.CustomSWRLBuiltin;

public class SWRLPropositionalBuiltinTest {
	static JenaDataSourceInferred jdsi;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		jdsi = new JenaDataSourceInferred(new FileInputStream(new File("Ontologies/SubclassSubsumption.ttl")));
		BuiltInRegistry.instance.registerBuiltIn("urn:seerc:builtIn#isTautology", new CustomSWRLBuiltin(new SWRLPropositionalBuiltin(jdsi)));
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
	public void testCreate() {
		jdsi.printModel(System.out);
	}

}
