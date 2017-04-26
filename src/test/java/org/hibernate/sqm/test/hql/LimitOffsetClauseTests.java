/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql;

import org.hibernate.query.sqm.tree.SqmSelectStatement;
import org.hibernate.query.sqm.tree.expression.LiteralIntegerSqmExpression;
import org.hibernate.query.sqm.tree.expression.ParameterSqmExpression;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.SubQuerySqmExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmSingularAttributeReference;
import org.hibernate.query.sqm.tree.predicate.RelationalSqmPredicate;
import org.hibernate.sqm.test.domain.StandardModelTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Test various forms of selections
 *
 * @author Christian Beikov
 */
public class LimitOffsetClauseTests extends StandardModelTest {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testSimpleLimit() {
		SqmSelectStatement statement = interpretSelect( "select p from Person p limit 1" );
		SqmExpression limitExpression = statement.getQuerySpec().getLimitOffsetClause().getLimitExpression();
		assertThat( limitExpression, instanceOf( LiteralIntegerSqmExpression.class ) );
		assertEquals( Integer.valueOf( 1 ), LiteralIntegerSqmExpression.class.cast( limitExpression ).getLiteralValue() );
	}

	@Test
	public void testParameterOffset() {
		SqmSelectStatement statement = interpretSelect( "select p from Person p offset :param" );
		SqmExpression offsetExpression = statement.getQuerySpec().getLimitOffsetClause().getOffsetExpression();
		assertThat( offsetExpression, instanceOf( ParameterSqmExpression.class ) );
		assertEquals( "param", ParameterSqmExpression.class.cast( offsetExpression ).getName() );
	}

	@Test
	public void testSubqueryLimitOffset() {
		SqmSelectStatement statement = interpretSelect( "select p from Person p where p.nickName = ( select pSub.nickName from Person pSub order by pSub.numberOfToes limit 1 )" );
		RelationalSqmPredicate predicate = (RelationalSqmPredicate) statement.getQuerySpec().getWhereClause().getPredicate();
		SubQuerySqmExpression subQuery = (SubQuerySqmExpression) predicate.getRightHandExpression();
		SqmExpression sortExpression = subQuery.getQuerySpec().getOrderByClause().getSortSpecifications().get( 0 ).getSortExpression();
		assertEquals( "numberOfToes", SqmSingularAttributeReference.class.cast( sortExpression ).getReferencedNavigable().getAttributeName() );
		assertEquals( Integer.valueOf( 1 ), LiteralIntegerSqmExpression.class.cast( subQuery.getQuerySpec().getLimitOffsetClause().getLimitExpression() ).getLiteralValue() );
	}

}
