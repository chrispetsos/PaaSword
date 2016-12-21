package org.seerc.paasword.validator.rest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.seerc.paasword.validator.engine.SecurityPolicyValidator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This uses the old SecurityPolicyValidator.
 * @author Chris Petsos
 *
 */
@Path("/")
// TODO: This should be deprecated.
public class SecurityPolicyRESTValidator {

	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	@POST
	@Path("/validateSecurityPolicy")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String validateSecurityPolicy(String securityPolicy) {
		try
		{
			InputStream stream = new ByteArrayInputStream(securityPolicy.getBytes(StandardCharsets.UTF_8));
	
			SecurityPolicyValidator scv = new SecurityPolicyValidator(stream);
			
			return gson.toJson(scv.validate());
		}
		catch(Exception e)
		{
			return gson.toJson(e);
		}
	}
}
