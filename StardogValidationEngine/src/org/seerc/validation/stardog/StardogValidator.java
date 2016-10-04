package org.seerc.validation.stardog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.rio.RDFFormat;
import org.seerc.paasword.validator.engine.JenaDataSource;

import com.complexible.common.rdf.model.Values;
import com.complexible.stardog.ContextSets;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.admin.AdminConnection;
import com.complexible.stardog.api.admin.AdminConnectionConfiguration;
import com.complexible.stardog.api.reasoning.ReasoningConnection;
import com.complexible.stardog.icv.api.ICVConnection;
import com.complexible.stardog.reasoning.Proof;
import com.complexible.stardog.reasoning.ProofWriter;

public class StardogValidator {

	AdminConnection aAdminConnection = null;
	ReasoningConnection reasoningConn = null;
	private String tempDatabaseName = "stardog_" + UUID.randomUUID().toString();

	public StardogValidator()
	{
		aAdminConnection = AdminConnectionConfiguration.toEmbeddedServer()
				.credentials("admin", "admin")
				.connect();
	}

	public String validate(InputStream constraints, InputStream... ontologies)
	{
		try
		{
			ByteArrayOutputStream[] baos = convertInputStreamToBAOS(ontologies);

			ByteArrayInputStream[] bais = this.reuseInputStreams(baos);

			// create temp DB
			aAdminConnection.createMemory(tempDatabaseName);

			// take a reasoning connection
			reasoningConn = ConnectionConfiguration
					.to(tempDatabaseName)
					.reasoning(true)
					.credentials("admin", "admin")
					.connect().as(ReasoningConnection.class);

			// add ontologies to DB
			this.addOntologiesToDB(bais);

			ICVConnection aValidator = reasoningConn.as(ICVConnection.class);

			// add constraints
			aValidator.addConstraints()
			.format(RDFFormat.TURTLE)
			.stream(constraints);

			// check if valid
			boolean isValid = aValidator.isValid(ContextSets.DEFAULT_ONLY);
			System.out.println("The data " + (isValid ? "is" : "is NOT") + " valid!");

			// create proof if exists
			Proof aProof = aValidator.explain().proof();
			if(aProof != null)
			{
				bais = this.reuseInputStreams(baos);
				String proofString = this.replaceNamespacesWithPrefixes(bais, ProofWriter.toString(aProof));
				System.out.println(proofString);
				return proofString;
			}

			return "OK";
		}
		finally {
			// now drop the temp database
			aAdminConnection.drop(tempDatabaseName);
		}
	}

	private String replaceNamespacesWithPrefixes(InputStream[] ontologies, String statements) {
		// replace <, >
		statements = statements.replace("<", "").replace(">", "");
		
		// use a JenaDataSource to replace namespaces
		Enumeration<InputStream> enumOnto = Collections.enumeration(Arrays.asList(ontologies));
		SequenceInputStream sis = new SequenceInputStream(enumOnto);
		JenaDataSource jds = new JenaDataSource(sis);
		for(String namespace:jds.getPrefixes().values())
		{
			statements = statements.replace(namespace, jds.getPrefixes().getKey(namespace) + ":");
		}

		return statements;
	}

	private void addOntologiesToDB(InputStream... ontologies) {
		reasoningConn.begin();
		for(InputStream ontology:ontologies)
		{
			reasoningConn.add().io()
			.format(RDFFormat.TURTLE)
			.stream(ontology);
		}
		reasoningConn.commit();
	}

	public String validate(String constraints, String[] ontologies)
	{
		InputStream constraintsIs = new ByteArrayInputStream(constraints.getBytes(StandardCharsets.UTF_8));
		InputStream[] ontologiesIs = new ByteArrayInputStream[ontologies.length];

		int i=0;
		for(String ontology:ontologies)
		{
			ontologiesIs[i] = new ByteArrayInputStream(ontology.getBytes(StandardCharsets.UTF_8));
			i++;
		}

		return this.validate(constraintsIs, ontologiesIs);
	}

	public String explain(InputStream[] ontologies, String statement) 
	{
		try
		{
			ByteArrayOutputStream[] baos = convertInputStreamToBAOS(ontologies);

			ByteArrayInputStream[] bais = this.reuseInputStreams(baos);

			// replace namespaces
			statement = this.replacePrefixesWithNamespaces(bais, statement);

			// re-init ByteArrayInputStreams to reuse them
			bais = this.reuseInputStreams(baos);

			// create temp DB
			aAdminConnection.createMemory(tempDatabaseName);

			// take a reasoning connection
			reasoningConn = ConnectionConfiguration
					.to(tempDatabaseName)
					.reasoning(true)
					.credentials("admin", "admin")
					.connect().as(ReasoningConnection.class);

			// add ontologies to DB
			this.addOntologiesToDB(bais);

			// create Statement
			String[] spo = statement.split(" ");
			String s = spo[0];
			String p = spo[1];
			String o = spo[2];
			Statement stm = SimpleValueFactory.getInstance().createStatement(Values.iri(s), Values.iri(p), Values.iri(o));

			// create explanation
			Proof aExplanation = reasoningConn.explain(stm).proof();

			if(aExplanation != null)
			{
				System.out.println("Explain inference: ");
				// re-init ByteArrayInputStreams to reuse them
				bais = this.reuseInputStreams(baos);
				String explanationString = this.replaceNamespacesWithPrefixes(bais, ProofWriter.toString(aExplanation));
				System.out.println(explanationString);
				return explanationString;
			}
			else
			{
				return null;
			}
		}
		finally {
			// now drop the temp database
			aAdminConnection.drop(tempDatabaseName);
		}
	}

	private ByteArrayOutputStream[] convertInputStreamToBAOS(
			InputStream[] ontologies) {
		// store ontologies in ByteArrayOutputStream in order to be re-used 
		ByteArrayOutputStream[] baos = new ByteArrayOutputStream[ontologies.length];
		int i=0;
		for(InputStream ontology:ontologies)
		{
			baos[i] = new ByteArrayOutputStream();
			try {
				IOUtils.copy(ontology, baos[i]);
			} catch (IOException e) {
				e.printStackTrace();
			}

			i++;
		}
		return baos;
	}

	private ByteArrayInputStream[] reuseInputStreams(ByteArrayOutputStream[] baos) {
		// create ByteArrayInputStreams
		ByteArrayInputStream[] bais = new ByteArrayInputStream[baos.length];
		int i=0;
		for(ByteArrayOutputStream stream:baos)
		{
			bais[i] = new ByteArrayInputStream(stream.toByteArray());
			i++;
		}
		return bais;
	}

	private String replacePrefixesWithNamespaces(InputStream[] ontologies, String statement)
	{
		// OK, also replace some constants
		statement = statement.replace(" a ", " rdf:type ");

		// use a JenaDataSource to replace prefixes
		Enumeration<InputStream> enumOnto = Collections.enumeration(Arrays.asList(ontologies));
		SequenceInputStream sis = new SequenceInputStream(enumOnto);
		JenaDataSource jds = new JenaDataSource(sis);
		for(String prefix:jds.getPrefixes().keySet())
		{
			statement = statement.replace(prefix + ":", jds.getPrefixes().get(prefix));
		}

		return statement;
	}

	public String explain(String[] ontologies, String statement)
	{
		InputStream[] ontologiesIs = new ByteArrayInputStream[ontologies.length];

		int i=0;
		for(String ontology:ontologies)
		{
			ontologiesIs[i] = new ByteArrayInputStream(ontology.getBytes(StandardCharsets.UTF_8));
			i++;
		}
		return this.explain(ontologiesIs, statement);
	}
}
