/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql;

import org.hibernate.query.sqm.tree.SqmSelectStatement;
import org.hibernate.query.sqm.tree.expression.EntityTypeLiteralSqmExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmEntityTypeSqmExpression;
import org.hibernate.query.sqm.tree.expression.ParameterizedEntityTypeSqmExpression;
import org.hibernate.query.sqm.tree.predicate.RelationalSqmPredicate;

import org.hibernate.sqm.test.domain.StandardModelTest;
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
		SqmSelectStatement statement = interpretSelect( "select a from Person a where type(a) = Person" );

		final RelationalSqmPredicate predicate = (RelationalSqmPredicate) statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate.getLeftHandExpression(), instanceOf( SqmEntityTypeSqmExpression.class ) );
		assertThat( predicate.getRightHandExpression(), instanceOf( EntityTypeLiteralSqmExpression.class ) );
	}

	@Test
	public void testEntityTypeExpressionByParam() {
		SqmSelectStatement statement = interpretSelect( "select a from Person a where type(?1) = Person" );

		final RelationalSqmPredicate predicate = (RelationalSqmPredicate) statement.getQuerySpec().getWhereClause().getPredicate();

		assertThat( predicate.getLeftHandExpression(), instanceOf( ParameterizedEntityTypeSqmExpression.class ) );
		final ParameterizedEntityTypeSqmExpression lhe = (ParameterizedEntityTypeSqmExpression) predicate.getLeftHandExpression();
		assertThat( lhe.getExpressionType(), nullValue() );

		assertThat( predicate.getRightHandExpression(), instanceOf( EntityTypeLiteralSqmExpression.class ) );
	}
}
