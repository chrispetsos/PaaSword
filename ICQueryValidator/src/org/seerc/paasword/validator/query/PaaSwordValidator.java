package org.seerc.paasword.validator.query;

import java.io.InputStream;

public class PaaSwordValidator extends QueryValidator {
	static InputStream constraints = Thread.currentThread().getContextClassLoader().getResourceAsStream("constraints/allConstraints.ttl");
	static InputStream pwdcm = Thread.currentThread().getContextClassLoader().getResourceAsStream("models/PaaSword-Context-Model.ttl");
	static InputStream pwdcpm = Thread.currentThread().getContextClassLoader().getResourceAsStream("models/PaaSword-Context-Pattern-Model.ttl");
	static InputStream pwdddem = Thread.currentThread().getContextClassLoader().getResourceAsStream("models/PaaSword-Data-Distribution-Encryption-Model.ttl");
	static InputStream pwdpm = Thread.currentThread().getContextClassLoader().getResourceAsStream("models/PaaSword-Permissions-Model.ttl");
	static InputStream pwdPolicyModel = Thread.currentThread().getContextClassLoader().getResourceAsStream("models/Security-Policy-Model.ttl");
	static InputStream theoremProvingModel = Thread.currentThread().getContextClassLoader().getResourceAsStream("models/Theorem-Proving.ttl");

	public PaaSwordValidator(InputStream securityPolicy) {
		super(constraints, pwdcm, pwdcpm, pwdddem, pwdpm, pwdPolicyModel, theoremProvingModel, securityPolicy);
	}

}
