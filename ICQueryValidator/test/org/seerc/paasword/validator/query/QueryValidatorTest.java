package org.seerc.paasword.validator.query;

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
import org.seerc.paasword.theoremprover.TheoremProvingDataSource;

public class QueryValidatorTest {

	QueryValidator qv;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		InputStream constraints = new FileInputStream(new File("Ontologies/constraints/constraints.owl"));

		InputStream pwdcm = new FileInputStream(new File("Ontologies/context-aware-security-models/PaaSwordContextModel_v2.ttl"));
		InputStream pwdcpm = new FileInputStream(new File("Ontologies/context-aware-security-models/PaaSwordContextPatternModel_v2.ttl"));
		InputStream pwdddem = new FileInputStream(new File("Ontologies/context-aware-security-models/PaaSwordDDEModel_v2.ttl"));
		InputStream pwdpm = new FileInputStream(new File("Ontologies/context-aware-security-models/PaaSwordPermissionModel_v2.ttl"));
		InputStream pwdPolicyModel = new FileInputStream(new File("Ontologies/policy-models/Security-Policy.ttl"));
		InputStream pwdSecurityPolicy = new FileInputStream(new File("Ontologies/policy-models/Car-Park-Security-Violating.ttl"));
		
		qv = new QueryValidator(constraints, pwdcm, pwdcpm, pwdddem, pwdpm, pwdPolicyModel, pwdSecurityPolicy);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testValidate() {
		List<QueryValidatorErrors> validationResult = qv.validate();
		assertEquals(0, validationResult.size());
	}

	// TODO: We do not support yet subclass constraints.
	/*@Test
	public void testSubsumption() {
		// Managers must be employees.
		assertNotValid(
				":Manager rdfs:subClassOf :Employee ."
				,
				":Alice a :Manager ."
				);
		
		assertValid(
				":Manager rdfs:subClassOf :Employee ."
				,
				":Alice a :Manager , :Employee ."
				);
	}*/

	@Test
	public void testDomainRange() {
		// Only project leaders can be responsible for projects.
		assertNotValid(
				":is_responsible_for rdfs:domain :Project_Leader ;\n" + 
				"                    rdfs:range :Project ."
				,
				":Alice :is_responsible_for :MyProject .\n" + 
				"\n" + 
				":MyProject a :Project ."
				);
		
		assertNotValid(
				":is_responsible_for rdfs:domain :Project_Leader ;\n" + 
				"                    rdfs:range :Project ."
				,
				":Alice a :Project_Leader ;\n" + 
				"	:is_responsible_for :MyProject ."
				);
		
		assertValid(
				":is_responsible_for rdfs:domain :Project_Leader ;\n" + 
				"                    rdfs:range :Project ."
				,
				":Alice a :Project_Leader ;\n" + 
				"	:is_responsible_for :MyProject .\n" + 
				"\n" + 
				":MyProject a :Project ."
				);
		
		// Only employees can have an SSN.
		assertNotValid(
				":ssn rdfs:domain :Employee ."
				,
				":Bob :ssn \"123-45-6789\" ."
				);
		
		assertValid(
				":ssn rdfs:domain :Employee ."
				,
				":Bob a :Employee ;\n" + 
				"	:ssn \"123-45-6789\" ."
				);

		// A date of birth must be a date.
		assertNotValid(
				":dob rdfs:range xsd:date ."
				,
				":Bob :dob \"1970-01-01\" ."
				);
		
		assertValid(
				":dob rdfs:range xsd:date ."
				,
				":Bob :dob \"1970-01-01\"^^xsd:date ."
				);
	}
	
	@Test
	public void testParticipation() {
		// Each supervisor must supervise at least one employee.
		assertValid(
				":Supervisor rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :supervises ;\n" + 
				"                owl:someValuesFrom :Employee\n" + 
				"              ] ."
				,
				":Alice a owl:Thing ."
				);
		
		assertNotValid(
				":Supervisor rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :supervises ;\n" + 
				"                owl:someValuesFrom :Employee\n" + 
				"              ] ."
				,
				":Alice a :Supervisor ."
				);
		
		assertNotValid(
				":Supervisor rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :supervises ;\n" + 
				"                owl:someValuesFrom :Employee\n" + 
				"              ] ."
				,
				":Alice a :Supervisor ;\n" + 
				"	:supervises :Bob ."
				);
		
		assertValid(
				":Supervisor rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :supervises ;\n" + 
				"                owl:someValuesFrom :Employee\n" + 
				"              ] ."
				,
				":Alice a :Supervisor ;\n" + 
				"	:supervises :Bob .\n" + 
				"\n" + 
				":Bob a :Employee ."
				);
		
		// Each project must have a valid project number.
		assertValid(
				":Project rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :number ;\n" + 
				"                owl:someValuesFrom\n" + 
				"                        [ a rdfs:Datatype ;\n" + 
				"                          owl:onDatatype xsd:integer ;\n" + 
				"                          owl:withRestrictions ([xsd:minInclusive 0] [ xsd:maxExclusive 5000])\n" + 
				"                        ]\n" + 
				"              ] ."
				,
				":Alice a owl:Thing ."
				);
		
		assertNotValid(
				":Project rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :number ;\n" + 
				"                owl:someValuesFrom\n" + 
				"                        [ a rdfs:Datatype ;\n" + 
				"                          owl:onDatatype xsd:integer ;\n" + 
				"                          owl:withRestrictions ([xsd:minInclusive 0] [ xsd:maxExclusive 5000])\n" + 
				"                        ]\n" + 
				"              ] ."
				,
				":MyProject a :Project ."
				);

		assertNotValid(
				":Project rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :number ;\n" + 
				"                owl:someValuesFrom\n" + 
				"                        [ a rdfs:Datatype ;\n" + 
				"                          owl:onDatatype xsd:integer ;\n" + 
				"                          owl:withRestrictions ([xsd:minInclusive 0] [ xsd:maxExclusive 5000])\n" + 
				"                        ]\n" + 
				"              ] ."
				,
				":MyProject a :Project ;\n" + 
				"	:number \"23\" ."
				);

		assertNotValid(
				":Project rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :number ;\n" + 
				"                owl:someValuesFrom\n" + 
				"                        [ a rdfs:Datatype ;\n" + 
				"                          owl:onDatatype xsd:integer ;\n" + 
				"                          owl:withRestrictions ([xsd:minInclusive 0] [ xsd:maxExclusive 5000])\n" + 
				"                        ]\n" + 
				"              ] ."
				,
				":MyProject a :Project ;\n" + 
				"	:number \"6000\"^^xsd:integer ."
				);

		assertValid(
				":Project rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :number ;\n" + 
				"                owl:someValuesFrom\n" + 
				"                        [ a rdfs:Datatype ;\n" + 
				"                          owl:onDatatype xsd:integer ;\n" + 
				"                          owl:withRestrictions ([xsd:minInclusive 0] [ xsd:maxExclusive 5000])\n" + 
				"                        ]\n" + 
				"              ] ."
				,
				":MyProject a :Project ;\n" + 
				"	:number \"23\"^^xsd:integer ."
				);
	}
	
	@Test
	public void testCardinality() {
		// Employees must not work on more than 3 projects.
		assertValid(
				":Employee rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :works_on;\n" + 
				"                owl:maxQualifiedCardinality \"3\"^^xsd:nonNegativeInteger ;\n" + 
				"                owl:onClass :Project\n" + 
				"              ] ."
				,
				":Bob a owl:Thing."
				);
		
		assertValid(
				":Employee rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :works_on;\n" + 
				"                owl:maxQualifiedCardinality \"3\"^^xsd:nonNegativeInteger ;\n" + 
				"                owl:onClass :Project\n" + 
				"              ] ."
				,
				":Bob a :Employee ;\n" + 
				"	:works_on :MyProject .\n" + 
				"\n" + 
				":MyProject a :Project ."
				);
		
		assertNotValid(
				":Employee rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :works_on;\n" + 
				"                owl:maxQualifiedCardinality \"3\"^^xsd:nonNegativeInteger ;\n" + 
				"                owl:onClass :Project\n" + 
				"              ] ."
				,
				":Bob a :Employee ;\n" + 
				"	:works_on :MyProject .\n" + 
				"\n" + 
				":Bob a :Employee ;\n" + 
				"	:works_on :MyProject , :MyProjectFoo , :MyProjectBar , :MyProjectBaz .\n" + 
				"\n" + 
				":MyProject a :Project .\n" + 
				"\n" + 
				":MyProjectFoo a :Project .\n" + 
				"\n" + 
				":MyProjectBar a :Project .\n" + 
				"\n" + 
				":MyProjectBaz a :Project ."
				);
		
		// TODO: inverseOf is not supported yet.
		// Departments must have at least 2 employees.
		/*assertValid(
				":Department rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty [owl:inverseOf :works_in] ;\n" + 
				"                owl:minQualifiedCardinality \"2\"^^xsd:nonNegativeInteger ;\n" + 
				"                owl:onClass :Employee\n" + 
				"              ] ."
				,
				":MyDepartment a owl:NamedIndividual ."
				);
		
		assertNotValid(
				":Department rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty [owl:inverseOf :works_in] ;\n" + 
				"                owl:minQualifiedCardinality \"2\"^^xsd:nonNegativeInteger ;\n" + 
				"                owl:onClass :Employee\n" + 
				"              ] ."
				,
				":MyDepartment a :Department .\n" + 
				"\n" + 
				":Bob a :Employee ;\n" + 
				"	:works_in :MyDepartment ."
				);
		
		assertValid(
				":Department rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty [owl:inverseOf :works_in] ;\n" + 
				"                owl:minQualifiedCardinality \"2\"^^xsd:nonNegativeInteger ;\n" + 
				"                owl:onClass :Employee\n" + 
				"              ] ."
				,
				":MyDepartment a :Department .\n" + 
				"\n" + 
				":Alice a :Employee ;\n" + 
				"	:works_in :MyDepartment .\n" + 
				"\n" + 
				":Bob a :Employee ;\n" + 
				"	:works_in :MyDepartment ."
				);*/
		
		// Managers must manage exactly 1 department.
		assertNotValid(
				":Manager rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :manages ;\n" + 
				"                owl:qualifiedCardinality \"1\"^^xsd:nonNegativeInteger ;\n" + 
				"                owl:onClass :Department\n" + 
				"              ] ."
				,
				":Isabella a :Manager ."
				);
		
		assertNotValid(
				":Manager rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :manages ;\n" + 
				"                owl:qualifiedCardinality \"1\"^^xsd:nonNegativeInteger ;\n" + 
				"                owl:onClass :Department\n" + 
				"              ] ."
				,
				":Isabella a :Manager ;\n" + 
				"	:manages :MyDepartment ."
				);
		
		assertValid(
				":Manager rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :manages ;\n" + 
				"                owl:qualifiedCardinality \"1\"^^xsd:nonNegativeInteger ;\n" + 
				"                owl:onClass :Department\n" + 
				"              ] ."
				,
				":Isabella a :Manager ;\n" + 
				"	:manages :MyDepartment .\n" + 
				"\n" + 
				":MyDepartment a :Department ."
				);
		
		assertNotValid(
				":Manager rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :manages ;\n" + 
				"                owl:qualifiedCardinality \"1\"^^xsd:nonNegativeInteger ;\n" + 
				"                owl:onClass :Department\n" + 
				"              ] ."
				,
				":Isabella a :Manager ;\n" + 
				"	:manages :MyDepartment , :MyDepartment1 .\n" + 
				"\n" + 
				":MyDepartment a :Department .\n" + 
				"\n" + 
				":MyDepartment1 a :Department ."
				);
		
		// TODO: Functional Properties are not supported yet.
		// Entities may have no more than one name.
		/*assertValid(
				":name a owl:FunctionalProperty ."
				,
				":MyDepartment a owl:Thing ."
				);
		
		assertValid(
				":name a owl:FunctionalProperty ."
				,
				":MyDepartment :name \"Human Resources\" ."
				);
		
		assertNotValid(
				":name a owl:FunctionalProperty ."
				,
				":MyDepartment :name \"Human Resources\" , \"Legal\" ."
				);*/
		
	}

	// TODO: We do not support yet subproperty constraints.
	/*@Test
	public void testPropertyConstraints() {
		// The manager of a department must work in that department.
		assertNotValid(
				":manages rdfs:subPropertyOf :works_in ."
				,
				":Bob :manages :MyDepartment ."
				);
		
		assertValid(
				":manages rdfs:subPropertyOf :works_in ."
				,
				":Bob :works_in :MyDepartment ;\n" + 
				"	:manages :MyDepartment ."
				);
		
		// Department managers must supervise all the department’s employees.
		assertNotValid(
				":is_supervisor_of owl:propertyChainAxiom (:manages [owl:inverseOf :works_in]) ."
				,
				":Jose :manages :MyDepartment ;\n" + 
				"	:is_supervisor_of :Maria .\n" + 
				"\n" + 
				":Maria :works_in :MyDepartment .\n" + 
				"\n" + 
				":Diego :works_in :MyDepartment ."
				);
		
		assertValid(
				":is_supervisor_of owl:propertyChainAxiom (:manages [owl:inverseOf :works_in]) ."
				,
				":Jose :manages :MyDepartment ;\n" + 
				"	:is_supervisor_of :Maria , :Diego .\n" + 
				"\n" + 
				":Maria :works_in :MyDepartment .\n" + 
				"\n" + 
				":Diego :works_in :MyDepartment ."
				);
		
	}*/
	
	@Test
	public void testComplexConstraints() {
		// Each employee works on at least one project, or supervises at least one employee 
		// that works on at least one project, or manages at least one department.
		assertNotValid(
				":Employee rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :works_on ;\n" + 
				"                owl:someValuesFrom\n" + 
				"                        [ owl:unionOf (:Project\n" + 
				"                                      [ a owl:Restriction ;\n" + 
				"                                        owl:onProperty :supervises ;\n" + 
				"                                        owl:someValuesFrom\n" + 
				"                                              [ owl:intersectionOf (:Employee\n" + 
				"                                                                    [ a owl:Restriction ;\n" + 
				"                                                                      owl:onProperty :works_on ;\n" + 
				"                                                                      owl:someValuesFrom :Project\n" + 
				"                                                                    ])\n" + 
				"                                              ]\n" + 
				"                                      ]\n" + 
				"                                      [ a owl:Restriction ;\n" + 
				"                                        owl:onProperty :manages ;\n" + 
				"                                        owl:someValuesFrom :Department\n" + 
				"                                      ])\n" + 
				"                        ]\n" + 
				"              ] ."
				,
				":Esteban a :Employee ."
				);
		
		assertNotValid(
				":Employee rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :works_on ;\n" + 
				"                owl:someValuesFrom\n" + 
				"                        [ owl:unionOf (:Project\n" + 
				"                                      [ a owl:Restriction ;\n" + 
				"                                        owl:onProperty :supervises ;\n" + 
				"                                        owl:someValuesFrom\n" + 
				"                                              [ owl:intersectionOf (:Employee\n" + 
				"                                                                    [ a owl:Restriction ;\n" + 
				"                                                                      owl:onProperty :works_on ;\n" + 
				"                                                                      owl:someValuesFrom :Project\n" + 
				"                                                                    ])\n" + 
				"                                              ]\n" + 
				"                                      ]\n" + 
				"                                      [ a owl:Restriction ;\n" + 
				"                                        owl:onProperty :manages ;\n" + 
				"                                        owl:someValuesFrom :Department\n" + 
				"                                      ])\n" + 
				"                        ]\n" + 
				"              ] ."
				,
				":Esteban a :Employee ;\n" + 
				"	:supervises :Lucinda .\n" + 
				"\n" + 
				":Lucinda a :Employee ."
				);
		
		// This was different compared to the result in the manual. But same result with Stardog itself...
		assertNotValid(
				":Employee rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :works_on ;\n" + 
				"                owl:someValuesFrom\n" + 
				"                        [ owl:unionOf (:Project\n" + 
				"                                      [ a owl:Restriction ;\n" + 
				"                                        owl:onProperty :supervises ;\n" + 
				"                                        owl:someValuesFrom\n" + 
				"                                              [ owl:intersectionOf (:Employee\n" + 
				"                                                                    [ a owl:Restriction ;\n" + 
				"                                                                      owl:onProperty :works_on ;\n" + 
				"                                                                      owl:someValuesFrom :Project\n" + 
				"                                                                    ])\n" + 
				"                                              ]\n" + 
				"                                      ]\n" + 
				"                                      [ a owl:Restriction ;\n" + 
				"                                        owl:onProperty :manages ;\n" + 
				"                                        owl:someValuesFrom :Department\n" + 
				"                                      ])\n" + 
				"                        ]\n" + 
				"              ] ."
				,
				":Esteban a :Employee ;\n" + 
				"	:supervises :Lucinda .\n" + 
				"\n" + 
				":Lucinda a :Employee ;\n" + 
				"	:works_on :MyProject .\n" + 
				"\n" + 
				":MyProject a :Project ."
				);
		
		// This was different compared to the result in the manual. But same result with Stardog itself...
		assertNotValid(
				":Employee rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :works_on ;\n" + 
				"                owl:someValuesFrom\n" + 
				"                        [ owl:unionOf (:Project\n" + 
				"                                      [ a owl:Restriction ;\n" + 
				"                                        owl:onProperty :supervises ;\n" + 
				"                                        owl:someValuesFrom\n" + 
				"                                              [ owl:intersectionOf (:Employee\n" + 
				"                                                                    [ a owl:Restriction ;\n" + 
				"                                                                      owl:onProperty :works_on ;\n" + 
				"                                                                      owl:someValuesFrom :Project\n" + 
				"                                                                    ])\n" + 
				"                                              ]\n" + 
				"                                      ]\n" + 
				"                                      [ a owl:Restriction ;\n" + 
				"                                        owl:onProperty :manages ;\n" + 
				"                                        owl:someValuesFrom :Department\n" + 
				"                                      ])\n" + 
				"                        ]\n" + 
				"              ] ."
				,
				":Esteban a :Employee ;\n" + 
				"	:manages :MyDepartment .\n" + 
				"\n" + 
				":MyDepartment a :Department ."
				);
		
		assertValid(
				":Employee rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty :works_on ;\n" + 
				"                owl:someValuesFrom\n" + 
				"                        [ owl:unionOf (:Project\n" + 
				"                                      [ a owl:Restriction ;\n" + 
				"                                        owl:onProperty :supervises ;\n" + 
				"                                        owl:someValuesFrom\n" + 
				"                                              [ owl:intersectionOf (:Employee\n" + 
				"                                                                    [ a owl:Restriction ;\n" + 
				"                                                                      owl:onProperty :works_on ;\n" + 
				"                                                                      owl:someValuesFrom :Project\n" + 
				"                                                                    ])\n" + 
				"                                              ]\n" + 
				"                                      ]\n" + 
				"                                      [ a owl:Restriction ;\n" + 
				"                                        owl:onProperty :manages ;\n" + 
				"                                        owl:someValuesFrom :Department\n" + 
				"                                      ])\n" + 
				"                        ]\n" + 
				"              ] ."
				,
				":Esteban a :Employee ;\n" + 
				"	:manages :MyDepartment ;\n" + 
				"	:works_on :MyProject .\n" + 
				"\n" + 
				":MyDepartment a :Department .\n" + 
				"\n" + 
				":MyProject a :Project ."
				);
		
		// Only employees who are American citizens can work on a project that 
		// receives funds from a US government agency.
		assertValid(
				"[ owl:intersectionOf (:Project\n" + 
				"                       [ a owl:Restriction ;\n" + 
				"                         owl:onProperty :receives_funds_from ;\n" + 
				"                         owl:someValuesFrom :US_Government_Agency\n" + 
				"                       ]) \n" + 
				"] rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty [owl:inverseOf :works_on] ;\n" + 
				"                owl:allValuesFrom [ owl:intersectionOf (:Employee\n" + 
				"                                                        [ a owl:Restriction ;\n" + 
				"                                                          owl:hasValue \"US\" ;\n" + 
				"                                                          owl:onProperty :nationality\n" + 
				"                                                        ])\n" + 
				"                                  ]\n" + 
				"              ] ."
				,
				":MyProject a :Project ;\n" + 
				"	:receives_funds_from :NASA .\n" + 
				"\n" + 
				":NASA a :US_Government_Agency ."
				);
		
		// TODO: This complex constraint is not supported yet. We need support for owl:allValuesFrom and owl:hasValue.
		/*assertNotValid(
				"[ owl:intersectionOf (:Project\n" + 
				"                       [ a owl:Restriction ;\n" + 
				"                         owl:onProperty :receives_funds_from ;\n" + 
				"                         owl:someValuesFrom :US_Government_Agency\n" + 
				"                       ]) \n" + 
				"] rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty [owl:inverseOf :works_on] ;\n" + 
				"                owl:allValuesFrom [ owl:intersectionOf (:Employee\n" + 
				"                                                        [ a owl:Restriction ;\n" + 
				"                                                          owl:hasValue \"US\" ;\n" + 
				"                                                          owl:onProperty :nationality\n" + 
				"                                                        ])\n" + 
				"                                  ]\n" + 
				"              ] ."
				,
				":MyProject a :Project ;\n" + 
				"	:receives_funds_from :NASA .\n" + 
				"\n" + 
				":NASA a :US_Government_Agency .\n" + 
				"\n" + 
				":Andy a :Employee ;\n" + 
				"	:works_on :MyProject ."
				);
		
		assertValid(
				"[ owl:intersectionOf (:Project\n" + 
				"                       [ a owl:Restriction ;\n" + 
				"                         owl:onProperty :receives_funds_from ;\n" + 
				"                         owl:someValuesFrom :US_Government_Agency\n" + 
				"                       ]) \n" + 
				"] rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty [owl:inverseOf :works_on] ;\n" + 
				"                owl:allValuesFrom [ owl:intersectionOf (:Employee\n" + 
				"                                                        [ a owl:Restriction ;\n" + 
				"                                                          owl:hasValue \"US\" ;\n" + 
				"                                                          owl:onProperty :nationality\n" + 
				"                                                        ])\n" + 
				"                                  ]\n" + 
				"              ] ."
				,
				":MyProject a :Project ;\n" + 
				"	:receives_funds_from :NASA .\n" + 
				"\n" + 
				":NASA a :US_Government_Agency .\n" + 
				"\n" + 
				":Andy a :Employee ;\n" + 
				"	:works_on :MyProject ;\n" + 
				"	:nationality \"US\" ."
				);
		
		assertNotValid(
				"[ owl:intersectionOf (:Project\n" + 
				"                       [ a owl:Restriction ;\n" + 
				"                         owl:onProperty :receives_funds_from ;\n" + 
				"                         owl:someValuesFrom :US_Government_Agency\n" + 
				"                       ]) \n" + 
				"] rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty [owl:inverseOf :works_on] ;\n" + 
				"                owl:allValuesFrom [ owl:intersectionOf (:Employee\n" + 
				"                                                        [ a owl:Restriction ;\n" + 
				"                                                          owl:hasValue \"US\" ;\n" + 
				"                                                          owl:onProperty :nationality\n" + 
				"                                                        ])\n" + 
				"                                  ]\n" + 
				"              ] ."
				,
				":MyProject a :Project ;\n" + 
				"	:receives_funds_from :NASA .\n" + 
				"\n" + 
				":NASA a :US_Government_Agency .\n" + 
				"\n" + 
				":Andy a :Employee ;\n" + 
				"	:works_on :MyProject ;\n" + 
				"	:nationality \"US\" .\n" + 
				"\n" + 
				":Heidi a :Supervisor ;\n" + 
				"	:works_on :MyProject ;\n" + 
				"	:nationality \"US\" ."
				);
		
		assertValid(
				"[ owl:intersectionOf (:Project\n" + 
				"                       [ a owl:Restriction ;\n" + 
				"                         owl:onProperty :receives_funds_from ;\n" + 
				"                         owl:someValuesFrom :US_Government_Agency\n" + 
				"                       ]) \n" + 
				"] rdfs:subClassOf\n" + 
				"              [ a owl:Restriction ;\n" + 
				"                owl:onProperty [owl:inverseOf :works_on] ;\n" + 
				"                owl:allValuesFrom [ owl:intersectionOf (:Employee\n" + 
				"                                                        [ a owl:Restriction ;\n" + 
				"                                                          owl:hasValue \"US\" ;\n" + 
				"                                                          owl:onProperty :nationality\n" + 
				"                                                        ])\n" + 
				"                                  ]\n" + 
				"              ] ."
				,
				":MyProject a :Project ;\n" + 
				"	:receives_funds_from :NASA .\n" + 
				"\n" + 
				":NASA a :US_Government_Agency .\n" + 
				"\n" + 
				":Andy a :Employee ;\n" + 
				"	:works_on :MyProject ;\n" + 
				"	:nationality \"US\" .\n" + 
				"\n" + 
				":Heidi a :Supervisor ;\n" + 
				"	:works_on :MyProject ;\n" + 
				"	:nationality \"US\" .\n" + 
				"\n" + 
				":Supervisor rdfs:subClassOf :Employee ."
				);*/
	}
	
	private void assertNotValid(String constraints, String ontology)
	{
		QueryValidator qv = new QueryValidator(
				"@prefix : <http://www.seerc.org/test/pellet-icv#> .\n" + 
				"@prefix xsd: <http://www.w3.org/2001/XMLSchema#>.\n" + 
				"@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" + 
				"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\n" +
				constraints
				, 
				"@prefix : <http://www.seerc.org/test/pellet-icv#> .\n" + 
				"@prefix xsd: <http://www.w3.org/2001/XMLSchema#>.\n" + 
				"@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" + 
				"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\n" +
				ontology
				);
		
		assertNotEquals(0, qv.validate().size());
	}

	private void assertValid(String constraints, String ontology)
	{
		QueryValidator qv = new QueryValidator(
				"@prefix : <http://www.seerc.org/test/pellet-icv#> .\n" + 
				"@prefix xsd: <http://www.w3.org/2001/XMLSchema#>.\n" + 
				"@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" + 
				"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\n" +
				constraints
				, 
				"@prefix : <http://www.seerc.org/test/pellet-icv#> .\n" + 
				"@prefix xsd: <http://www.w3.org/2001/XMLSchema#>.\n" + 
				"@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" + 
				"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\n" +
				ontology
				);
		
		assertEquals(0, qv.validate().size());
	}

	@Test
	public void testAbacRulesViolating1() throws Exception {
		InputStream constraints = new FileInputStream(new File("Ontologies/constraints/rulesConstraints1.ttl"));
		InputStream policy = new FileInputStream(new File("Ontologies/policy-models/abacRulesViolating1.ttl"));
		
		qv = new QueryValidator(constraints, policy);
		
		assertEquals(2, qv.validate().size());
	}	

	@Test
	public void testAbacRulesFull() throws Exception {
		InputStream constraints = new FileInputStream(new File("Ontologies/constraints/abacRulesConstraints.ttl"));
		InputStream policy = new FileInputStream(new File("Ontologies/policy-models/Car-Park-Security-Extracted-Constraints-Full.ttl"));
		
		qv = new QueryValidator(constraints, policy);
		
		assertEquals(0, qv.validate().size());
	}	

	@Test
	public void testAbacRulesSimple() throws Exception {
		InputStream constraints = new FileInputStream(new File("Ontologies/constraints/abacRulesConstraints.ttl"));
		InputStream policy = new FileInputStream(new File("Ontologies/policy-models/Car-Park-Security-Extracted-Constraints-Simple.ttl"));
		
		qv = new QueryValidator(constraints, policy);
		
		assertEquals(0, qv.validate().size());
	}	

	@Test
	public void testAbacRulesSimpleFailing() throws Exception {
		InputStream constraints = new FileInputStream(new File("Ontologies/constraints/abacRulesConstraints.ttl"));
		InputStream policy = new FileInputStream(new File("Ontologies/policy-models/Car-Park-Security-Extracted-Constraints-Simple-Failing.ttl"));
		
		qv = new QueryValidator(constraints, policy);
		
		assertEquals(8, qv.validate().size());
	}	

	@Test
	public void testSubclassSubsumption() throws Exception {
		InputStream constraints = new FileInputStream(new File("Ontologies/constraints/subclassSubsumptionConstraints.ttl"));
		InputStream policy = new FileInputStream(new File("Ontologies/subsumptive/SubclassSubsumption.ttl"));
		
		qv = new QueryValidator(constraints, policy);
		
		assertEquals(1, qv.validate().size());
	}	

	@Test
	public void testPrintSubclassSubsumption() throws Exception {
		InputStream policy = new FileInputStream(new File("Ontologies/subsumptive/SubclassSubsumption.ttl"));
		
		TheoremProvingDataSource tpds = new TheoremProvingDataSource(policy);
		tpds.printModel(System.out);
	}	
}
