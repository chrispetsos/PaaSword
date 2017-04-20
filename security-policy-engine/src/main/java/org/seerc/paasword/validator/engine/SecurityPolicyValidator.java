package org.seerc.paasword.validator.engine;

import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;

/**
 * The first version of the way of validating security policies.
 * @author Chris Petsos
 *
 */
// TODO: Make this deprecated.
public class SecurityPolicyValidator {

	JenaDataSource jds;
	
	List<RDFNode> abacRules;
	// if these property values of a rule are equal, then rules are the same
	private static String[] abacRuleEqualityProperties={
		"pac:hasControlledObject",
		"pac:hasAction",
		"pac:hasActor",
		"pac:hasContextExpression"
	};
	// if these property values are not equal, then same rules contradict 
	private static String[] abacRuleContradictionProperties={
		"pac:hasAuthorisation"
	};
	
	List<RDFNode> cryptographicRules;
	private static String[] cryptographicRuleEqualityProperties={
		"pbe:hasControlledObject"
	};
	private static String[] cryptographicRuleContradictionProperties={
		"pbe:hasCryptoElement"
	};
	
	List<RDFNode> dfdRules;
	private static String[] dfdRuleEqualityProperties={
		"pbdfd:hasControlledObject"
	};
	private static String[] dfdRuleContradictionProperties={
		"pbdfd:hasDFDElement"
	};
	
	private static final String[] DATE_FORMATS = new String[] {
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd'T'HH:mm:ssz",
        "yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-ddz", "yyyy-MM-ddZ",
        "'T'HH:mm:ss", "-'T'HH:mm:ss"
        };
	
	public SecurityPolicyValidator(String filePath)
	{
		jds = new JenaDataSource(filePath);
		this.extractRules();
	}

	private void extractRules() {
		abacRules = jds.executeQuery("{?var a pac:ABACRule}");
		cryptographicRules = jds.executeQuery("{?var a pbe:BootstrappingCryptoRule}");
		dfdRules = jds.executeQuery("{?var a pbdfd:BootstrappingDFDRule}");
	}

	public SecurityPolicyValidator(InputStream stream) {
		jds = new JenaDataSource(stream);
		this.extractRules();
	}

	public List<ProblematicRules> findContradictingRules()
	{
		List<ProblematicRules> result = new ArrayList<ProblematicRules>();

		result.addAll(this.findContradictingABACRules());
		result.addAll(this.findContradictingCryptographicRules());
		result.addAll(this.findContradictingDFDRules());
		
		return result;
	}
	
	private List<ProblematicRules> findContradictingDFDRules()
	{
		return runContradictionAlgorithm(dfdRules, dfdRuleEqualityProperties, dfdRuleContradictionProperties);
	}

	private List<ProblematicRules> findContradictingCryptographicRules()
	{
		return runContradictionAlgorithm(cryptographicRules, cryptographicRuleEqualityProperties, cryptographicRuleContradictionProperties);
	}

	private List<ProblematicRules> findContradictingABACRules() {
		return runContradictionAlgorithm(abacRules, abacRuleEqualityProperties, abacRuleContradictionProperties);
	}

	private List<ProblematicRules> runContradictionAlgorithm(List<RDFNode> rules, String[] ruleEqualityProperties, String[] ruleContradictionProperties) {
		List<ProblematicRules> result = new ArrayList<ProblematicRules>();
		
		// convert the rules to array
		RDFNode[] rulesArray = rules.toArray(new RDFNode[rules.size()]);
		
		// check rules one by one comparing with the ones after the current
		// in order no to find the same contradiction twice (e.g. {rule1, rule2} and {rule2, rule1})
		for(int i=0;i<rulesArray.length;i++)
		{
			for(int j=i+1;j<rulesArray.length;j++)
			{
				RDFNode rule1 = rulesArray[i];
				RDFNode rule2 = rulesArray[j];
				
				if(this.rulesAreSame(rule1, rule2, ruleEqualityProperties) && this.rulesContradict(rule1, rule2, ruleContradictionProperties))
				{
					result.add(new ProblematicRules("Contradiction", rule1.toString(), rule2.toString()));
				}
			}
		}
		
		return result;
	}

	private boolean rulesContradict(RDFNode rule1, RDFNode rule2, String[] contradictionProperties) {
		for(String property:contradictionProperties)
		{
			RDFNode propertyValue1 = this.getPropertyValue(rule1, property);
			RDFNode propertyValue2 = this.getPropertyValue(rule2, property);
			
			if(!this.haveEqualStructure(propertyValue1, propertyValue2))
			{
				return true;
			}
		}
		return false;
	}

	private boolean rulesAreSame(RDFNode rule1, RDFNode rule2, String[] equalityProperties) {
		for(String property:equalityProperties)
		{
			RDFNode propertyValue1 = this.getPropertyValue(rule1, property);
			RDFNode propertyValue2 = this.getPropertyValue(rule2, property);
			
			if(!this.haveEqualStructure(propertyValue1, propertyValue2))
			{
				return false;
			}
		}
		return true;
	}

	private RDFNode getPropertyValue(RDFNode rule, String property) {
		return jds.executeQuery("{<" + rule.toString() + "> " + property + " ?var}").get(0);
	}

	public List<RDFNode> getABACRules() {
		return this.abacRules;
	}

	public List<String> findPoliciesThatContainContradictions()
	{
		List<String> result = new ArrayList<String>();
		
		result.addAll(findPolicyContradictions("pbe:BootstrappingCryptoPolicy", "pbe:hasBootStrappingCryptoRule", cryptographicRuleEqualityProperties, cryptographicRuleContradictionProperties));
		result.addAll(findPolicyContradictions("pac:ABACPolicy", "pac:hasABACRule", abacRuleEqualityProperties, abacRuleContradictionProperties));
		result.addAll(findPolicyContradictions("pbdfd:BootstrappingDFDPolicy", "pbdfd:hasBootstrappingDFDRule", dfdRuleEqualityProperties, dfdRuleContradictionProperties));
		
		return result;
	}

	private List<String> findPolicyContradictions(String policyType, String ruleConnectionProperty, String[] equalityProperties, String[] contradictionProperties) {
		List<String> result = new ArrayList<String>();
		
		List<RDFNode> policies = jds.executeQuery("{?var a " + policyType + "}");
		for(RDFNode policy:policies)
		{
			List<RDFNode> rulesOfPolicy = jds.executeQuery("{<" + policy.toString() + "> " + ruleConnectionProperty + " ?var}");
			if(!runContradictionAlgorithm(rulesOfPolicy, equalityProperties, contradictionProperties).isEmpty())
			{	// policy has contradiction
				result.add(policy.toString());
			}
		}
		
		return result;
	}
	
	public ValidationReport validate()
	{
		ValidationReport report = new ValidationReport();
		
		String result = "";
		
		List<ProblematicRules> ruleContradictions = this.findContradictingRules();
		report.setContradictingRules(ruleContradictions);
		
		List<String> policyContradictions = this.findPoliciesThatContainContradictions();
		report.setContradictingPolicies(policyContradictions);
		
		List<ProblematicRules> ruleSubsumptions = this.findRuleSubsumptions();
		report.setSubsumptiveRules(ruleSubsumptions);

		if(!ruleContradictions.isEmpty())
		{
			result += "Contradictive Rules (" + ruleContradictions.size() + "):\n";
			for(ProblematicRules ruleContradiction:ruleContradictions)
			{
				result += ruleContradiction.toString() + "\n";
			}
			result += "\n";
		}
		
		if(!policyContradictions.isEmpty())
		{
			result += "Contradictive Policies (" + policyContradictions.size() + "):\n";
			for(String policyContradiction:policyContradictions)
			{
				result += policyContradiction + "\n";
			}
			result += "\n";
		}
		
		if(!ruleSubsumptions.isEmpty())
		{
			result += "Subsumptive Rules (" + ruleSubsumptions.size() + "):\n";
			for(ProblematicRules ruleSubsumption:ruleSubsumptions)
			{
				result += ruleSubsumption.toString() + "\n";
			}
			result += "\n";
		}
		
		if(result.isEmpty())
		{
			result = "OK";
		}
		
		return report;
	}

	public List<ProblematicRules> findRuleSubsumptions()
	{
		List<ProblematicRules> result = new ArrayList<ProblematicRules>();

		// subsumption also covers equality...
		//result.addAll(this.findRulesEquality());
		result.addAll(this.findRulesSubsumptions());
		
		return result;
	}

	private List<ProblematicRules> findRulesSubsumptions() {
		List<ProblematicRules> result = new ArrayList<ProblematicRules>();

		result.addAll(this.findSubsumedRules(dfdRules, dfdRuleEqualityProperties, dfdRuleContradictionProperties));	// DFD
		result.addAll(this.findSubsumedRules(cryptographicRules, cryptographicRuleEqualityProperties, cryptographicRuleContradictionProperties));	// Crypto
		result.addAll(this.findSubsumedRules(abacRules, abacRuleEqualityProperties, abacRuleContradictionProperties));	// ABAC
		
		return result;
	}

	private List<ProblematicRules> findSubsumedRules(List<RDFNode> rules, String[] ruleEqualityProperties, String[] ruleContradictionProperties) {
		List<ProblematicRules> result = new ArrayList<ProblematicRules>();
		
		// convert the rules to array
		RDFNode[] rulesArray = rules.toArray(new RDFNode[rules.size()]);
		
		// We want to check of rules have *all* properties equal. 
		String[] allProperties = (String[])ArrayUtils.addAll(ruleEqualityProperties, ruleContradictionProperties);
		
		// check rules one by one comparing with the ones after the current
		// in order no to find the same contradiction twice (e.g. {rule1, rule2} and {rule2, rule1})
		for(int i=0;i<rulesArray.length;i++)
		{
			for(int j=i+1;j<rulesArray.length;j++)
			{
				RDFNode rule1 = rulesArray[i];
				RDFNode rule2 = rulesArray[j];
				
				if(this.rulesAreSubsumed(rule1, rule2, allProperties))
				{
					result.add(new ProblematicRules("Subsumption", rule1.toString(), rule2.toString()));
				}
			}
		}
		
		return result;
	}

	private boolean rulesAreSubsumed(RDFNode rule1, RDFNode rule2, String[] allProperties) {
		for(String property:allProperties)
		{
			RDFNode propertyValue1 = this.getPropertyValue(rule1, property);
			RDFNode propertyValue2 = this.getPropertyValue(rule2, property);
			
			if(!this.propertiesValuesAreSubsumed(propertyValue1, propertyValue2))
			{
				return false;
			}
		}
		return true;
	}

	private boolean propertiesValuesAreSubsumed(RDFNode node1, RDFNode node2) {
		// We can handle DateTimeInterval
		if(jds.isNodeType(node1, jds.createFromNsAndLocalName("pcm", "DateTimeInterval").toString()) && 
				jds.isNodeType(node2, jds.createFromNsAndLocalName("pcm", "DateTimeInterval").toString()))
		{
			return this.checkDateTimeIntervalSubsumption(node1, node2);
		}
		else
		{	// in every other case compare them based on equal structure
			return this.nodesAreSubsumed(node1, node2);
		}
	}

	private boolean nodesAreSubsumed(RDFNode node1, RDFNode node2)
	{
		if(node1.isResource() && node2.isResource())
		{	// resource nodes
			return resourceNodesAreSubsumed(node1.asResource(), node2.asResource());
		}
		else if(node1.isLiteral() && node2.isLiteral())
		{	// literal nodes
			return compareLiteralNodes(node1.asLiteral(), node2.asLiteral());
		}
		else
		{	// different nodes
			return false;
		}
	}

	private boolean resourceNodesAreSubsumed(Resource resource1, Resource resource2) 
	{
		StmtIterator si1 = resource1.listProperties();
		StmtIterator si2 = resource2.listProperties();
		
		// check if resources have the same count of properties
		int countStatements1 = this.countStmtIterator(resource1.listProperties());
		int countStatements2 = this.countStmtIterator(resource2.listProperties());
		
		if(countStatements1 != countStatements2)
		{
			return false;
		}
		

		/*Anonymous resources are deemed to be equal if they have the same properties
		and property values. If only the type properties are found in an anonymous resource
		then no comparison can take place, so they are not equal.
		Anonymous resources can also be equal (identical).
		TODO: Merge with check below for named resources.
		We also assume that all resources are typed. (type-safety)*/
		if((resource1.isAnon() && countStatements1 == 1 && resource1.listProperties().next().getPredicate().equals(jds.createFromNsAndLocalName("rdf", "type"))) ||
				(resource2.isAnon() && countStatements2 == 1 && resource2.listProperties().next().getPredicate().equals(jds.createFromNsAndLocalName("rdf", "type"))))
		{
			return resource1.toString().equals(resource2.toString());
		}
		
		/*
		 * If named resources do not have properties other than type, then equality is inferred from their name.
		 */
		if((resource1.isURIResource() && countStatements1 == 1 && resource1.listProperties().next().getPredicate().equals(jds.createFromNsAndLocalName("rdf", "type"))) ||
				(resource2.isURIResource() && countStatements2 == 1 && resource2.listProperties().next().getPredicate().equals(jds.createFromNsAndLocalName("rdf", "type"))))
		{
			return resource1.toString().equals(resource2.toString());
		}
		
		
		boolean terminatingResource = true;
		
		while(si1.hasNext())
		{
			terminatingResource = false;
			boolean statementFound = false;
			Statement s1 = si1.next();
			// re-init si2
			si2 = resource2.listProperties();
			while(si2.hasNext())
			{
				Statement s2 = si2.next();
				/*if(resource1.isAnon() || resource2.isAnon())
				{	// one of the resources is anonymous -> check predicate and objects (TODO: is this sensible?)
				*/
				// For all kind of resources (named and anonymous) equality is based on structure and not name.
				if(s1.getPredicate().equals(s2.getPredicate()) && this.propertiesValuesAreSubsumed(s1.getObject(), s2.getObject()))
				{
					statementFound = true;
					break;
				}
				/*}
				else
				{	// named (URI) resources -> check subject, predicate and objects (TODO: is this sensible?)
					if(s1.getSubject().equals(s2.getSubject()) && s1.getPredicate().equals(s2.getPredicate()) && this.haveEqualStructure(s1.getObject(), s2.getObject()))
					{
						statementFound = true;
						break;
					}
				}*/
			}

			if(!statementFound)
			{
				return false;
			}
		}
		
		if(terminatingResource)
		{
			return resource1.equals(resource2);
		}
		
		return true;
	}

	private boolean checkDateTimeIntervalSubsumption(RDFNode dateTime1, RDFNode dateTime2) {
		//Statement hasBeginning1 = dateTime1.asResource().listProperties(new PropertyImpl("http://www.paasword-project.eu/ontologies/casm/2015/11/30#hasBeginning")).next();
		/*StmtIterator si1 = dateTime1.asResource().listProperties();
		while(si1.hasNext())
		{
			Statement s1 = si1.next();
			int i=0;
		}*/
		
		RDFNode beginning1Node = dateTime1.asResource().listProperties(new PropertyImpl(jds.createFromNsAndLocalName("pcm", "hasBeginning").toString())).next().getObject();
		RDFNode end1Node = dateTime1.asResource().listProperties(new PropertyImpl(jds.createFromNsAndLocalName("pcm", "hasEnd").toString())).next().getObject();
		
		RDFNode beginning2Node = dateTime2.asResource().listProperties(new PropertyImpl(jds.createFromNsAndLocalName("pcm", "hasBeginning").toString())).next().getObject();
		RDFNode end2Node = dateTime2.asResource().listProperties(new PropertyImpl(jds.createFromNsAndLocalName("pcm", "hasEnd").toString())).next().getObject();
		
		/*RDFNode beginning1Node = jds.executeQuery("{<" + dateTime1.toString() + "> pcm:hasBeginning ?var}").get(0);
		RDFNode end1Node = jds.executeQuery("{<" + dateTime1.toString() + "> pcm:hasEnd ?var}").get(0);
		
		RDFNode beginning2Node = jds.executeQuery("{<" + dateTime2.toString() + "> pcm:hasBeginning ?var}").get(0);
		RDFNode end2Node = jds.executeQuery("{<" + dateTime2.toString() + "> pcm:hasEnd ?var}").get(0);*/
		
		Date beginning1Date = null;
		Date end1Date = null;
		Date beginning2Date = null;
		Date end2Date = null;
    	beginning1Date = ((XSDDateTime)beginning1Node.asLiteral().getValue()).asCalendar().getTime();
    	beginning2Date = ((XSDDateTime)beginning2Node.asLiteral().getValue()).asCalendar().getTime();
    	end1Date = ((XSDDateTime)end1Node.asLiteral().getValue()).asCalendar().getTime();
    	end2Date = ((XSDDateTime)end2Node.asLiteral().getValue()).asCalendar().getTime();
        /*try {
        	beginning1Date = DateUtils.parseDate(beginning1Node.asLiteral().getValue().toString(), DATE_FORMATS);
        	beginning2Date = DateUtils.parseDate(beginning2Node.toString(), DATE_FORMATS);
        	end1Date = DateUtils.parseDate(end1Node.toString(), DATE_FORMATS);
        	end2Date = DateUtils.parseDate(end2Node.toString(), DATE_FORMATS);
		} catch (ParseException e) {
			e.printStackTrace();
		}*/
        
        // will check both pairs against one another
        if(this.dateRangeIncludesDateRange(beginning1Date, end1Date, beginning2Date, end2Date) ||
        		this.dateRangeIncludesDateRange(beginning2Date, end2Date, beginning1Date, end1Date))
        {
        	return true;
        }
        
		return false;
	}

	private boolean dateRangeIncludesDateRange(Date beginning1Date, Date end1Date, Date beginning2Date, Date end2Date) {
		if((beginning1Date.after(beginning2Date) && end1Date.before(end2Date)) ||
				(beginning1Date.equals(beginning2Date) && end1Date.before(end2Date)) ||
				(beginning1Date.after(beginning2Date) && end1Date.equals(end2Date)) ||
				(beginning1Date.equals(beginning2Date) && end1Date.equals(end2Date)))
		{
			return true;
		}
		
		return false;
	}

	private List<ProblematicRules> findRulesEquality() {
		List<ProblematicRules> result = new ArrayList<ProblematicRules>();

		result.addAll(this.findEqualRules(dfdRules, dfdRuleEqualityProperties, dfdRuleContradictionProperties));	// DFD
		result.addAll(this.findEqualRules(cryptographicRules, cryptographicRuleEqualityProperties, cryptographicRuleContradictionProperties));	// Crypto
		result.addAll(this.findEqualRules(abacRules, abacRuleEqualityProperties, abacRuleContradictionProperties));	// ABAC
		
		return result;
	}

	private List<ProblematicRules> findEqualRules(List<RDFNode> rules, String[] ruleEqualityProperties, String[] ruleContradictionProperties) {
		List<ProblematicRules> result = new ArrayList<ProblematicRules>();
		
		// convert the rules to array
		RDFNode[] rulesArray = rules.toArray(new RDFNode[rules.size()]);
		
		// We want to check of rules have *all* properties equal. 
		String[] allProperties = (String[])ArrayUtils.addAll(ruleEqualityProperties, ruleContradictionProperties);
		
		// check rules one by one comparing with the ones after the current
		// in order no to find the same contradiction twice (e.g. {rule1, rule2} and {rule2, rule1})
		for(int i=0;i<rulesArray.length;i++)
		{
			for(int j=i+1;j<rulesArray.length;j++)
			{
				RDFNode rule1 = rulesArray[i];
				RDFNode rule2 = rulesArray[j];
				
				if(this.rulesAreSame(rule1, rule2, allProperties))
				{
					result.add(new ProblematicRules("Equality", rule1.toString(), rule2.toString()));
				}
			}
		}
		
		return result;
	}

	/*
	 * Compares full structure of two nodes.
	 */
	public boolean haveEqualStructure(RDFNode node1, RDFNode node2) {
		if(node1.isResource() && node2.isResource())
		{	// resource nodes
			return compareResourceNodes(node1.asResource(), node2.asResource());
		}
		else if(node1.isLiteral() && node2.isLiteral())
		{	// literal nodes
			return compareLiteralNodes(node1.asLiteral(), node2.asLiteral());
		}
		else
		{	// different nodes
			return false;
		}
	}

	private boolean compareLiteralNodes(Literal literal1, Literal literal2)
	{
		return literal1.equals(literal2);
	}

	private boolean compareResourceNodes(Resource resource1, Resource resource2) {
		StmtIterator si1 = resource1.listProperties();
		StmtIterator si2 = resource2.listProperties();
		
		// check if resources have the same count of properties
		int countStatements1 = this.countStmtIterator(resource1.listProperties());
		int countStatements2 = this.countStmtIterator(resource2.listProperties());
		
		if(countStatements1 != countStatements2)
		{
			return false;
		}
		

		/*Anonymous resources are deemed to be equal if they have the same properties
		and property values. If only the type properties are found in an anonymous resource
		then no comparison can take place, so they are not equal.
		We also assume that all resources are typed. (type-safety)*/
		if((resource1.isAnon() && countStatements1 == 1 && resource1.listProperties().next().getPredicate().equals(jds.createFromNsAndLocalName("rdf", "type"))) ||
				(resource2.isAnon() && countStatements2 == 1 && resource2.listProperties().next().getPredicate().equals(jds.createFromNsAndLocalName("rdf", "type"))))
		{
			return false;
		}
		
		/*
		 * If named resources do not have properties other than type, then equality is inferred from their name.
		 */
		if((resource1.isURIResource() && countStatements1 == 1 && resource1.listProperties().next().getPredicate().equals(jds.createFromNsAndLocalName("rdf", "type"))) ||
				(resource2.isURIResource() && countStatements2 == 1 && resource2.listProperties().next().getPredicate().equals(jds.createFromNsAndLocalName("rdf", "type"))))
		{
			return resource1.toString().equals(resource2.toString());
		}
		
		
		boolean terminatingResource = true;
		
		while(si1.hasNext())
		{
			terminatingResource = false;
			boolean statementFound = false;
			Statement s1 = si1.next();
			// re-init si2
			si2 = resource2.listProperties();
			while(si2.hasNext())
			{
				Statement s2 = si2.next();
				/*if(resource1.isAnon() || resource2.isAnon())
				{	// one of the resources is anonymous -> check predicate and objects (TODO: is this sensible?)
				*/
				// For all kind of resources (named and anonymous) equality is based on structure and not name.
				if(s1.getPredicate().equals(s2.getPredicate()) && this.haveEqualStructure(s1.getObject(), s2.getObject()))
				{
					statementFound = true;
					break;
				}
				/*}
				else
				{	// named (URI) resources -> check subject, predicate and objects (TODO: is this sensible?)
					if(s1.getSubject().equals(s2.getSubject()) && s1.getPredicate().equals(s2.getPredicate()) && this.haveEqualStructure(s1.getObject(), s2.getObject()))
					{
						statementFound = true;
						break;
					}
				}*/
			}

			if(!statementFound)
			{
				return false;
			}
		}
		
		if(terminatingResource)
		{
			return resource1.equals(resource2);
		}
		
		return true;
	}

	private int countStmtIterator(StmtIterator si)
	{
		int count = 0;
		
		while(si.hasNext())
		{
			si.next();
			count++;
		}
		
		return count;
	}	
}
