/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.query.parser.hql;

import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.StrictJpaComplianceViolation;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.parser.InterpretationException;

import org.hibernate.test.query.parser.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hibernate.sqm.SemanticQueryInterpreter.interpret;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Steve Ebersole
 */
public class KeywordAsIdentifierTest {
	@Test
	public void testKeywordAsIdentificationVariable() {
		ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

		final String queryIllegal = "select abs.basic from Entity abs";
		final String queryLegal = "select abs.basic from Entity as abs";

		try {
			interpret( queryIllegal, consumerContext );
			fail( "exception failure" );
		}
		catch (InterpretationException ignore) {
		}

		// first test HQL superset is allowed...
		interpret( queryLegal, consumerContext );

		// now enable strict compliance and try again, should lead to error
		consumerContext.enableStrictJpaCompliance();
		try {
			interpret( queryLegal, consumerContext );
			fail( "expected violation" );
		}
		catch (StrictJpaComplianceViolation v) {
			assertEquals( StrictJpaComplianceViolation.Type.RESERVED_WORD_USED_AS_ALIAS , v.getType() );
		}
	}

	@Test
	public void testKeywordAsResultVariable() {
		ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

		final String queryIllegal = "select e.basic abs from Entity e";
		final String queryLegal = "select e.basic as abs from Entity as e";

		try {
			interpret( queryIllegal, consumerContext );
		}
		catch (Exception ignore) {
		}

		// first test HQL superset is allowed...
		interpret( queryLegal, consumerContext );

		// now enable strict compliance and try again, should lead to error
		consumerContext.enableStrictJpaCompliance();
		try {
			interpret( queryLegal, consumerContext );
			fail( "expected violation" );
		}
		catch (StrictJpaComplianceViolation v) {
			assertEquals( StrictJpaComplianceViolation.Type.RESERVED_WORD_USED_AS_ALIAS , v.getType() );
		}
	}

	@Test
	public void testKeywordAsAttributeNameInSelect() {
		ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

		final String query = "select e.from from Entity e";

		interpret( query, consumerContext );

		consumerContext.enableStrictJpaCompliance();
		interpret( query, consumerContext );
	}

	private DomainMetamodel buildMetamodel() {
		ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();

		EntityTypeImpl entityType = metamodel.makeEntityType( "com.acme.Entity" );
		entityType.makeSingularAttribute(
				"basic",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		entityType.makeSingularAttribute(
				"basic1",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		entityType.makeSingularAttribute(
				"basic2",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		entityType.makeSingularAttribute(
				"basic3",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		entityType.makeSingularAttribute(
				"basic4",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		entityType.makeSingularAttribute(
				"from",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		entityType.makeSingularAttribute(
				"select",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);

		EntityTypeImpl entity2Type = metamodel.makeEntityType( "com.acme.Entity2" );
		entity2Type.makeSingularAttribute(
				"basic1",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
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
