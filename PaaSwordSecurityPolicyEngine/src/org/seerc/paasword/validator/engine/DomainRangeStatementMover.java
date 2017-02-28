package org.seerc.paasword.validator.engine;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class DomainRangeStatementMover {

	public void moveDomainRangeStatements(JenaDataSource dataSource, OntModel targetModel)
	{
		this.movePropertyStatements(dataSource, targetModel, "http://www.w3.org/2000/01/rdf-schema#domain");
		this.movePropertyStatements(dataSource, targetModel, "http://www.w3.org/2000/01/rdf-schema#range");
	}

	private void movePropertyStatements(JenaDataSource dataSource, OntModel targetModel, String propertyUri) {
		// find all subjects of property
		Property property = dataSource.getModel().createProperty(propertyUri);
		ResIterator subjectsOfPropertyStatements = dataSource.getModel().listSubjectsWithProperty(property);
		while(subjectsOfPropertyStatements.hasNext())
		{
			Resource subjectOfProperty = subjectsOfPropertyStatements.next();
			// find all property statements where subjectOfProperty is subject
			StmtIterator propertyStatements = dataSource.getModel().listStatements(subjectOfProperty, property, (RDFNode)null);
			// The RDFList that will hold all objects of the statements
			RDFList propertyList = targetModel.createList();
			while(propertyStatements.hasNext())
			{
				Statement propertyStatement = propertyStatements.next();
				propertyList = propertyList.with(propertyStatement.getObject());
			}
			// create the union class that will hold all statements
			OntClass unionClass = targetModel.createUnionClass(null, propertyList);
			// add the property statement with the union class in targetModel
			targetModel.add(subjectOfProperty, property, unionClass);
			// remove propertyStatements from dataSource
			dataSource.getModel().remove(dataSource.getModel().listStatements(subjectOfProperty, property, (RDFNode)null));
		}
	}

}
