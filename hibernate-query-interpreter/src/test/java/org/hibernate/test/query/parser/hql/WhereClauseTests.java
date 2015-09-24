/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.query.parser.hql;

import org.hibernate.query.parser.SemanticQueryInterpreter;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.expression.CollectionIndexFunction;
import org.hibernate.sqm.query.expression.CollectionSizeFunction;
import org.hibernate.sqm.query.expression.LiteralIntegerExpression;
import org.hibernate.sqm.query.predicate.Predicate;
import org.hibernate.sqm.query.predicate.RelationalPredicate;
import org.hibernate.test.query.parser.ConsumerContextImpl;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for elements of WHERE clauses.
 *
 * @author Gunnar Morling
 */
public class WhereClauseTests {

	private final ConsumerContextImpl consumerContext = new ConsumerContextImpl();

	@Test
	public void testCollectionSizeFunction() {
		SelectStatement statement = interpret( "SELECT t FROM Trip t WHERE SIZE( t.basicCollection ) = 311" );

		Predicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( RelationalPredicate.class ) );
		RelationalPredicate relationalPredicate = ( (RelationalPredicate) predicate );

		assertThat( relationalPredicate.getType(), is( RelationalPredicate.Type.EQUAL ) );

		assertThat( relationalPredicate.getRightHandExpression(), instanceOf( LiteralIntegerExpression.class ) );
		assertThat( ( (LiteralIntegerExpression) relationalPredicate.getRightHandExpression() ).getLiteralValue(), is( 311 ) );

		assertThat( relationalPredicate.getLeftHandExpression(), instanceOf( CollectionSizeFunction.class ) );
		assertThat( ( (CollectionSizeFunction) relationalPredicate.getLeftHandExpression() ).getFromElementAlias(), is( "t" ) );
		assertThat( ( (CollectionSizeFunction) relationalPredicate.getLeftHandExpression() ).getAttributeDescriptor().getName(), is( "basicCollection" ) );
	}

	@Test
	public void testCollectionIndexFunction() {
		SelectStatement statement = interpret( "SELECT l.basicName FROM Trip t JOIN t.indexedCollectionLegs l WHERE INDEX( l ) > 2" );

		Predicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( RelationalPredicate.class ) );
		RelationalPredicate relationalPredicate = ( (RelationalPredicate) predicate );

		assertThat( relationalPredicate.getType(), is( RelationalPredicate.Type.GT ) );

		assertThat( relationalPredicate.getRightHandExpression(), instanceOf( LiteralIntegerExpression.class ) );
		assertThat( ( (LiteralIntegerExpression) relationalPredicate.getRightHandExpression() ).getLiteralValue(), is( 2 ) );

		assertThat( relationalPredicate.getLeftHandExpression(), instanceOf( CollectionIndexFunction.class ) );
		assertThat( ( (CollectionIndexFunction) relationalPredicate.getLeftHandExpression() ).getCollectionAlias(), is( "l" ) );
	}

	private SelectStatement interpret(String query) {
		return (SelectStatement) SemanticQueryInterpreter.interpret( query, consumerContext );
	}
}
