/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.select;

import javax.persistence.criteria.Root;

import org.hibernate.sqm.ConsumerContext;
import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.domain.SingularAttributeReference.SingularAttributeClassification;
import org.hibernate.sqm.query.SqmSelectStatement;

import org.hibernate.test.sqm.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;
import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;
import org.hibernate.test.sqm.parser.criteria.tree.CriteriaQueryImpl;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Steve Ebersole
 */
public class CriteriaTranslationSmokeTests {


	@Test
	public void smokeTest() {
		final ConsumerContext consumerContext = new ConsumerContextImpl( buildMetamodel() );
		final CriteriaBuilderImpl criteriaBuilder = new CriteriaBuilderImpl( consumerContext );

		// Build a simple criteria ala `select e from Entity e`
		final CriteriaQueryImpl<Object> criteria = (CriteriaQueryImpl<Object>) criteriaBuilder.createQuery();
		Root root = criteria.from( "com.acme.Entity2" );
		criteria.select( root );

		// now ask the interpreter to convert the criteria into SQM...
		final SqmSelectStatement sqm = SemanticQueryInterpreter.interpret( criteria, consumerContext );
		assertThat( sqm.getQuerySpec().getFromClause().getFromElementSpaces().size(), is(1) ) ;
	}


	private DomainMetamodel buildMetamodel() {
		ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();

		EntityTypeImpl entity2Type = metamodel.makeEntityType( "com.acme.Entity2" );
		entity2Type.makeSingularAttribute(
				"basic1",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);

		EntityTypeImpl entityType = metamodel.makeEntityType( "com.acme.Entity" );
		entityType.makeSingularAttribute(
				"basic",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		entityType.makeSingularAttribute(
				"basic1",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		entityType.makeSingularAttribute(
				"basic2",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		entityType.makeSingularAttribute(
				"basic3",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		entityType.makeSingularAttribute(
				"basic4",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);

		EntityTypeImpl legType = metamodel.makeEntityType( "com.acme.Leg" );

		EntityTypeImpl tripType = metamodel.makeEntityType( "com.acme.Trip" );
		tripType.makeMapAttribute(
				"mapLegs",
				StandardBasicTypeDescriptors.INSTANCE.STRING,
				legType
		);
		tripType.makeListAttribute(
				"collectionLegs",
				StandardBasicTypeDescriptors.INSTANCE.INTEGER,
				legType
		);

		return metamodel;
	}

}
