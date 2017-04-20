package org.seerc.paasword.validator.query;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

public class PaaSwordValidator extends QueryValidator {
	//static InputStream pwdcm = Thread.currentThread().getContextClassLoader().getResourceAsStream("models/PaaSwordContextModel.ttl");
	static InputStream pwdListModel = PaaSwordValidator.class.getResourceAsStream("/Ontologies/final/models/list.ttl");
	static InputStream pwdRuleAntecedentConclusionModel = PaaSwordValidator.class.getResourceAsStream("/Ontologies/final/models/RuleAntecedentConclusion.ttl");
	static InputStream pwdPolicyModel = PaaSwordValidator.class.getResourceAsStream("/Ontologies/final/models/Security-Policy-Model.ttl");
	static InputStream theoremProvingModel = PaaSwordValidator.class.getResourceAsStream("/Ontologies/final/models/Theorem-Proving.ttl");
	static byte[] baModelOntologies;

	static InputStream constraints = PaaSwordValidator.class.getResourceAsStream("/Ontologies/final/constraints/allConstraints.ttl");
	static byte[] baConstraintsOntologies;

	// cache the statically referenced ontologies into a byte array,
	// so we can create input streams multiple times.
	static{
		Enumeration<InputStream> enumOnto = Collections.enumeration(Arrays.asList(new InputStream[]{pwdListModel, pwdRuleAntecedentConclusionModel, pwdPolicyModel, theoremProvingModel}));
		SequenceInputStream sis = new SequenceInputStream(enumOnto);
		ByteArrayOutputStream osModelOntologies = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int n = 0;
		try {
			while ((n = sis.read(buf)) >= 0)
			{
				osModelOntologies.write(buf, 0, n);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		baModelOntologies = osModelOntologies.toByteArray();
		
		ByteArrayOutputStream osConstraintsOntologies = new ByteArrayOutputStream();
		buf = new byte[1024];
		n = 0;
		try {
			while ((n = constraints.read(buf)) >= 0)
			{
				osConstraintsOntologies.write(buf, 0, n);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		baConstraintsOntologies = osConstraintsOntologies.toByteArray();
	}
	
	public PaaSwordValidator(InputStream securityPolicy) {
		super(new ByteArrayInputStream(baConstraintsOntologies), new ByteArrayInputStream(baModelOntologies), securityPolicy);
	}

}
