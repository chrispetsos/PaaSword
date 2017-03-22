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
import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.seerc.paasword.validator.engine.DomainRangeStatementMover;
import org.seerc.paasword.validator.engine.JenaDataSource;
import org.seerc.paasword.validator.engine.JenaDataSourceInferred;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class DomainRangeStatementMoverTest {

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
	public void testMoveDomainRangeStatements() throws Exception {
		JenaDataSourceInferred jdsi = new JenaDataSourceInferred(getClass().getResourceAsStream("/Ontologies/test/DomainRangeMoveTest.ttl"));
		
		// we have 4 domain and 4 range statements added by the reasoner for top and bottom object properties
		
		// jds should have 4+3 domain statements
		assertEquals(7, this.getStatementsWithProperty((OntModel) jdsi.getModel(), "http://www.w3.org/2000/01/rdf-schema#domain").size());
		
		// jds should have 4+2 range statements
		assertEquals(6, this.getStatementsWithProperty((OntModel) jdsi.getModel(), "http://www.w3.org/2000/01/rdf-schema#range").size());

		DomainRangeStatementMover drsm = new DomainRangeStatementMover();
		
		OntModel targetModel = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);

		drsm.moveDomainRangeStatements(jdsi, targetModel);
		
		// jds should have no domain/range statements
		assertEquals(0, this.getStatementsWithProperty((OntModel) jdsi.getModel(), "http://www.w3.org/2000/01/rdf-schema#domain").size());
		assertEquals(0, this.getStatementsWithProperty((OntModel) jdsi.getModel(), "http://www.w3.org/2000/01/rdf-schema#range").size());
		
		// Find the domain of ex1:aProperty
		RDFNode domainA = targetModel.listStatements(targetModel.createResource("http://www.paasword.eu/security-policy/use-cases/car-park#aProperty"), targetModel.createProperty("http://www.w3.org/2000/01/rdf-schema#domain"), (RDFNode)null).next().getObject();
		// Create the Union of ex1:ClassA and ex1:ClassB
		RDFList classList = targetModel.createList(new RDFNode[] {targetModel.createClass("http://www.paasword.eu/security-policy/use-cases/car-park#ClassA"), targetModel.createClass("http://www.paasword.eu/security-policy/use-cases/car-park#ClassB")});
		// Domain of ex1:aProperty should be equivalent to the Union
		assertTrue(domainA.as(OntClass.class).hasEquivalentClass(targetModel.createUnionClass("", classList)));

		// Same for domain of pcm:isLocatedIn
		RDFNode domainB = targetModel.listStatements(targetModel.createResource("http://www.paasword-project.eu/ontologies/casm/2015/11/30#isLocatedIn"), targetModel.createProperty("http://www.w3.org/2000/01/rdf-schema#domain"), (RDFNode)null).next().getObject();
		assertTrue(domainB.as(OntClass.class).hasEquivalentClass(targetModel.createClass("http://www.paasword-project.eu/ontologies/casm/2015/11/30#Area")));

		// Same for range of ex1:aProperty
		RDFNode rangeA = targetModel.listStatements(targetModel.createResource("http://www.paasword.eu/security-policy/use-cases/car-park#aProperty"), targetModel.createProperty("http://www.w3.org/2000/01/rdf-schema#range"), (RDFNode)null).next().getObject();
		assertTrue(rangeA.as(OntClass.class).hasEquivalentClass(targetModel.createClass("http://www.paasword.eu/security-policy/use-cases/car-park#ClassC")));

		// And range of pcm:isLocatedIn
		RDFNode rangeB = targetModel.listStatements(targetModel.createResource("http://www.paasword-project.eu/ontologies/casm/2015/11/30#isLocatedIn"), targetModel.createProperty("http://www.w3.org/2000/01/rdf-schema#range"), (RDFNode)null).next().getObject();
		assertTrue(rangeB.as(OntClass.class).hasEquivalentClass(targetModel.createClass("http://www.paasword-project.eu/ontologies/casm/2015/11/30#Area")));
	}

	private List<Statement> getStatementsWithProperty(OntModel model, String property)
	{
		return model.listStatements((Resource)null, model.createProperty(property), (RDFNode)null).toList();
	}
}
