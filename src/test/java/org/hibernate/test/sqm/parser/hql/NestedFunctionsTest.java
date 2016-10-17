/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.hql;

import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.domain.SingularAttributeReference.SingularAttributeClassification;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.expression.function.ConcatFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.SubstringFunctionSqmExpression;
import org.hibernate.sqm.query.select.SqmSelection;

import org.hibernate.test.sqm.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hibernate.sqm.SemanticQueryInterpreter.interpret;

/**
 * @author Steve Ebersole
 */
public class NestedFunctionsTest {
	final ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

	@Test
	public void testSubstrInsideConcat() {
		final SqmSelectStatement statement = (SqmSelectStatement) interpret( "select concat('111', substring('222222', 1, 3)) from Entity", consumerContext );
		assertThat( statement.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		final SqmSelection selection = statement.getQuerySpec().getSelectClause().getSelections().get( 0 );
		assertThat( selection.getExpression(), instanceOf( ConcatFunctionSqmExpression.class ) );
		final ConcatFunctionSqmExpression concatFunction = (ConcatFunctionSqmExpression) selection.getExpression();
		assertThat( concatFunction.getExpressions().size(), is(2) );
		// check that the second expression/argument is the substr function
		assertThat( concatFunction.getExpressions().get( 1 ), instanceOf( SubstringFunctionSqmExpression.class ) );
	}

	private DomainMetamodel buildMetamodel() {
		ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();

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
		entityType.makeSingularAttribute(
				"from",
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
