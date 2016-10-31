/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.hql;

import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.expression.EntityTypeLiteralSqmExpression;
import org.hibernate.sqm.query.expression.domain.EntityTypeSqmExpression;
import org.hibernate.sqm.query.expression.ParameterizedEntityTypeSqmExpression;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Steve Ebersole
 */
public class EntityTypeExpressionTests extends StandardModelTest {
	@Test
	public void testEntityTypeExpressionByAlias() {
		SqmSelectStatement statement = interpretSelect( "select a from Something a where type(a) = Something" );

		final RelationalSqmPredicate predicate = (RelationalSqmPredicate) statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate.getLeftHandExpression(), instanceOf( EntityTypeSqmExpression.class ) );
		assertThat( predicate.getRightHandExpression(), instanceOf( EntityTypeLiteralSqmExpression.class ) );
	}

	@Test
	public void testEntityTypeExpressionByParam() {
		SqmSelectStatement statement = interpretSelect( "select a from Something a where type(?1) = Something" );

		final RelationalSqmPredicate predicate = (RelationalSqmPredicate) statement.getQuerySpec().getWhereClause().getPredicate();

		assertThat( predicate.getLeftHandExpression(), instanceOf( ParameterizedEntityTypeSqmExpression.class ) );
		final ParameterizedEntityTypeSqmExpression lhe = (ParameterizedEntityTypeSqmExpression) predicate.getLeftHandExpression();
		assertThat( lhe.getExpressionType(), nullValue() );

		assertThat( predicate.getRightHandExpression(), instanceOf( EntityTypeLiteralSqmExpression.class ) );
	}
}
