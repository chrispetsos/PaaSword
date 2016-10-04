package org.seerc.validation.stardog.rest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.seerc.validation.stardog.StardogExplanationData;
import org.seerc.validation.stardog.StardogValidationData;
import org.seerc.validation.stardog.StardogValidator;

@Path("/")
public class StardogRESTValidator {

	@POST
	@Path("/validate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String validate(StardogValidationData data) {
		try
		{
			StardogValidator scv = new StardogValidator();
			
			return scv.validate(data.getConstraints(), data.getOntologies());
		}
		catch(Exception e)
		{
			return e.toString();
		}
	}
	
	@POST
	@Path("/explain")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String explain(StardogExplanationData data) {
		try
		{
			StardogValidator scv = new StardogValidator();
			
			String explanation = scv.explain(data.getOntologies(), data.getStatement());
			if(explanation == null)
				return "No explanation...";
			
			return explanation;
		}
		catch(Exception e)
		{
			return e.toString();
		}
	}
}
