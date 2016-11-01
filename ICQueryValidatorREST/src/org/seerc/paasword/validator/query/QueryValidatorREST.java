package org.seerc.paasword.validator.query;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.seerc.paasword.validator.query.JenaDataSourceInferred;
import org.seerc.paasword.validator.query.QueryValidator;
import org.seerc.paasword.validator.query.QueryValidatorErrors;

@Path("/")
public class QueryValidatorREST {

	@POST
	@Path("/validate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String validate(RESTValidationData data) {
		try
		{
			QueryValidator qv = new QueryValidator(data.getConstraints(), data.getOntologies());
			List<QueryValidatorErrors> errors = qv.validate();
			
			String report = this.generateReport(errors);
			
			return report;
		}
		catch(Exception e)
		{
			return e.toString();
		}
	}
	
	private String generateReport(List<QueryValidatorErrors> errors)
	{
		String report = errors.size() + " validation errors.\n\n";
		int errorCounter = 0;
		
		for(QueryValidatorErrors error:errors)
		{
			errorCounter++;
			// separator
			if(error.getProblematicResources().isEmpty())
			{
				report += "--------------------------------------------------\n";
			}
			else
			{
				report += "XXX Error " + errorCounter + " XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n";
			}
			
			report += "The constraint comprised of the following statements:\n";
			report += error.getQueryConstraint().getConstraintStatements().toString() + "\n";
			report += "which have been translated to the following query:\n";
			report += error.getQueryConstraint().getQuery() + "\n";
			if(error.getProblematicResources().isEmpty())
			{
				report += "has NOT  been violated.\n";
			}
			else
			{
				report += "has been violated by statements related to the following resources.\n";				
				report += error.getProblematicResources().toString().replaceAll(",", ",\n") + "\n";
			}
			
			// separator
			if(error.getProblematicResources().isEmpty())
			{
				report += "--------------------------------------------------\n";
			}
			else
			{
				report += "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n";
			}

			// empty line between constraints
			report += "\n";

		}
		
		if(report.equals(""))
		{
			report = "OK";
		}
		
		return report;
	}

	@POST
	@Path("/query")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String query(QueryData data) {
		try
		{
			Vector<InputStream> ontologiesIs = new Vector<>();
			
			for(String ontology:data.getOntologies())
			{
				ontologiesIs.add(new ByteArrayInputStream(ontology.getBytes(StandardCharsets.UTF_8)));
			}
			SequenceInputStream sis = new SequenceInputStream(ontologiesIs.elements());
			
			JenaDataSourceInferred jdsi = new JenaDataSourceInferred(sis);
			
			String result = jdsi.executeReadyQuery(data.getQuery()).toString();
			
			if(result == null || result.equals(""))
			{
				return "No result.";
			}
			
			return result;
		}
		catch(Exception e)
		{
			return e.toString();
		}
	}
}
