/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql;

import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.query.SqmQuerySpec;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.expression.LiteralIntegerSqmExpression;
import org.hibernate.sqm.query.expression.LiteralLongSqmExpression;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.SqmFromClause;
import org.hibernate.sqm.query.predicate.AndSqmPredicate;
import org.hibernate.sqm.query.predicate.InSubQuerySqmPredicate;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;
import org.hibernate.sqm.test.domain.Person;
import org.hibernate.sqm.test.domain.StandardModelTest;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Steve Ebersole
 */
public class SimpleSemanticQueryBuilderTest extends StandardModelTest {
	@Test
	public void simpleIntegerLiteralsTest() {
		SqmSelectStatement selectStatement = interpretSelect( "select a.nickName from Person a where 1=2" );
		assertThat( selectStatement.getQuerySpec().getWhereClause().getPredicate(), instanceOf( RelationalSqmPredicate.class ) );
		final RelationalSqmPredicate predicate = (RelationalSqmPredicate) selectStatement.getQuerySpec().getWhereClause().getPredicate();

		SqmExpression lhs = predicate.getLeftHandExpression();
		assertNotNull( lhs );
		assertTrue( lhs instanceof LiteralIntegerSqmExpression );
		assertEquals( 1, ((LiteralIntegerSqmExpression) lhs).getLiteralValue().intValue() );

		Object rhs = predicate.getRightHandExpression();
		assertNotNull( rhs );
		assertTrue( rhs instanceof LiteralIntegerSqmExpression );
		assertEquals( 2, ((LiteralIntegerSqmExpression) rhs).getLiteralValue().intValue() );
	}

	@Test
	public void simpleLongLiteralsTest() {
		SqmSelectStatement selectStatement = interpretSelect( "select a.nickName from Person a where 1L=2L" );
		assertThat( selectStatement.getQuerySpec().getWhereClause().getPredicate(), instanceOf( RelationalSqmPredicate.class ) );
		final RelationalSqmPredicate predicate = (RelationalSqmPredicate) selectStatement.getQuerySpec().getWhereClause().getPredicate();

		SqmExpression lhs = predicate.getLeftHandExpression();
		assertNotNull( lhs );
		assertTrue( lhs instanceof LiteralLongSqmExpression );
		assertEquals( 1L, ((LiteralLongSqmExpression) lhs).getLiteralValue().longValue() );

		Object rhs = predicate.getRightHandExpression();
		assertNotNull( rhs );
		assertTrue( rhs instanceof LiteralLongSqmExpression );
		assertEquals( 2L, ( (LiteralLongSqmExpression) rhs ).getLiteralValue().longValue() );

	}

	@Test
	public void testAttributeJoinWithOnPredicate() throws Exception {
		final String query = "select a from Person a left outer join a.mate c on c.numberOfToes > 5 and c.numberOfToes < 20";
		final SqmSelectStatement selectStatement = interpretSelect( query );
		SqmQuerySpec querySpec = selectStatement.getQuerySpec();
		assertNotNull( querySpec );
	}

	@Test
	public void testSimpleUncorrelatedSubQuery() throws Exception {
		final String query = "select a from Person a where a.mate IN (select e from Person e where e.numberOfToes = 5 )";
		final SqmSelectStatement selectStatement = interpretSelect( query );

		SqmFromClause fromClause = selectStatement.getQuerySpec().getFromClause();
		assertNotNull( fromClause );
		assertThat( fromClause.getFromElementSpaces().size(), is( 1 ) );

		FromElementSpace fromElementSpace = fromClause.getFromElementSpaces().get( 0 );
		assertThat( fromElementSpace.getRoot(), notNullValue() );
		assertThat( fromElementSpace.getJoins().size(), is( 0 ) );

		assertThat( fromElementSpace.getRoot().getEntityName(), is( "org.hibernate.sqm.test.domain.Person" ) );
		assertThat( fromElementSpace.getRoot().getIdentificationVariable(), is( "a" ) );

		// assertions against the root sqm predicate that defines the sub-sqm
		assertThat( selectStatement.getQuerySpec().getWhereClause().getPredicate(), notNullValue() );
		assertThat(
				selectStatement.getQuerySpec().getWhereClause().getPredicate(),
				is( instanceOf( InSubQuerySqmPredicate.class ) )
		);

		InSubQuerySqmPredicate subQueryPredicate = (InSubQuerySqmPredicate) selectStatement.getQuerySpec()
				.getWhereClause()
				.getPredicate();
		SqmFromClause subqueryFromClause = subQueryPredicate.getSubQueryExpression().getQuerySpec().getFromClause();
		assertNotNull( subqueryFromClause );
		assertThat( subqueryFromClause.getFromElementSpaces().size(), is( 1 ) );

		FromElementSpace subqueryFromElementSpace = subqueryFromClause.getFromElementSpaces().get( 0 );
		assertThat( subqueryFromElementSpace.getRoot(), notNullValue() );

		assertThat( subqueryFromElementSpace.getJoins().size(), is( 0 ) );

		assertThat(
				subqueryFromElementSpace.getRoot().getEntityName(),
				is( Person.class.getName() )
		);
		assertThat( subqueryFromElementSpace.getRoot().getIdentificationVariable(), is( "e" ) );
	}

	@Test
	public void testUncorrelatedSubQueries() throws Exception {
		final String query = "select a from Person a where a.mate IN (select e from Person e where e.numberOfToes IN (select e.numberOfToes+2 from Person b where b.nickName = 'polydactyl' ))";
		final SqmSelectStatement selectStatement = interpretSelect( query );

		SqmFromClause fromClause = selectStatement.getQuerySpec().getFromClause();
		assertNotNull( fromClause );
		assertThat( fromClause.getFromElementSpaces().size(), is( 1 ) );

		FromElementSpace fromElementSpace = fromClause.getFromElementSpaces().get( 0 );
		assertThat( fromElementSpace.getRoot(), notNullValue() );
		assertThat( fromElementSpace.getJoins().size(), is( 0 ) );

		assertThat( fromElementSpace.getRoot().getEntityName(), is( "org.hibernate.sqm.test.domain.Person" ) );
		assertThat( fromElementSpace.getRoot().getIdentificationVariable(), is( "a" ) );

		// assertions against the root sqm predicate that defines the sub-sqm
		assertThat( selectStatement.getQuerySpec().getWhereClause().getPredicate(), notNullValue() );
		assertThat(
				selectStatement.getQuerySpec().getWhereClause().getPredicate(),
				is( instanceOf( InSubQuerySqmPredicate.class ) )
		);

		InSubQuerySqmPredicate subQueryPredicate = (InSubQuerySqmPredicate) selectStatement.getQuerySpec()
				.getWhereClause()
				.getPredicate();
		SqmFromClause subqueryFromClause = subQueryPredicate.getSubQueryExpression().getQuerySpec().getFromClause();
		assertNotNull( subqueryFromClause );
		assertThat( subqueryFromClause.getFromElementSpaces().size(), is( 1 ) );

		FromElementSpace subqueryFromElementSpace = subqueryFromClause.getFromElementSpaces().get( 0 );
		assertThat( subqueryFromElementSpace.getRoot(), notNullValue() );

		assertThat( subqueryFromElementSpace.getJoins().size(), is( 0 ) );

		assertThat(
				subqueryFromElementSpace.getRoot().getEntityName(),
				is( "org.hibernate.sqm.test.domain.Person" )
		);
		assertThat( subqueryFromElementSpace.getRoot().getIdentificationVariable(), is( "e" ) );

		// assertions against the root sqm predicate that defines the su-sqm of sub-sqm
		InSubQuerySqmPredicate subSubqueryPredicate = (InSubQuerySqmPredicate) subQueryPredicate.getSubQueryExpression()
				.getQuerySpec()
				.getWhereClause()
				.getPredicate();

		SqmFromClause subSubqueryFromClause = subSubqueryPredicate.getSubQueryExpression().getQuerySpec().getFromClause();
		assertNotNull( subSubqueryFromClause );

		FromElementSpace subSubqueryFromElementSpace = subSubqueryFromClause.getFromElementSpaces().get( 0 );
		assertThat( subSubqueryFromElementSpace.getRoot(), notNullValue() );

		assertThat( subSubqueryFromElementSpace.getJoins().size(), is( 0 ) );

		assertThat(
				subSubqueryFromElementSpace.getRoot().getEntityName(),
				is( "org.hibernate.sqm.test.domain.Person" )
		);
		assertThat( subSubqueryFromElementSpace.getRoot().getIdentificationVariable(), is( "b" ) );
	}

	@Test
	public void testUncorrelatedSubQueriesInAndPredicate() throws Exception {
		final String query = "Select a from Person a where a.mate in ( select b from Person b where b.numberOfToes = 5) and a.mate in (select c from Person c where c.numberOfToes = 6)";
		final SqmSelectStatement selectStatement = interpretSelect( query );

		SqmFromClause fromClause = selectStatement.getQuerySpec().getFromClause();
		assertNotNull( fromClause );
		assertThat( fromClause.getFromElementSpaces().size(), is( 1 ) );

		FromElementSpace fromElementSpace = fromClause.getFromElementSpaces().get( 0 );
		assertThat( fromElementSpace.getRoot(), notNullValue() );
		assertThat( fromElementSpace.getJoins().size(), is( 0 ) );

		assertThat( fromElementSpace.getRoot().getEntityName(), is( "org.hibernate.sqm.test.domain.Person" ) );
		assertThat( fromElementSpace.getRoot().getIdentificationVariable(), is( "a" ) );

		// assertions against the root sqm predicate that defines the sub-sqm
		assertThat( selectStatement.getQuerySpec().getWhereClause().getPredicate(), notNullValue() );
		assertThat(
				selectStatement.getQuerySpec().getWhereClause().getPredicate(),
				is( instanceOf( AndSqmPredicate.class ) )
		);

		AndSqmPredicate andPredicate = (AndSqmPredicate) selectStatement.getQuerySpec()
				.getWhereClause()
				.getPredicate();

		assertThat( andPredicate.getLeftHandPredicate(), is( instanceOf( InSubQuerySqmPredicate.class ) ) );

		assertThat( andPredicate.getRightHandPredicate(), is( instanceOf( InSubQuerySqmPredicate.class ) ) );

		InSubQuerySqmPredicate leftHandPredicate = (InSubQuerySqmPredicate) andPredicate.getLeftHandPredicate();

		SqmFromClause leftHandPredicateFromClause = leftHandPredicate.getSubQueryExpression()
				.getQuerySpec()
				.getFromClause();
		assertNotNull( leftHandPredicateFromClause );

		FromElementSpace leftHandPredicateFromElementSpace = leftHandPredicateFromClause.getFromElementSpaces()
				.get( 0 );
		assertThat( leftHandPredicateFromElementSpace.getRoot(), notNullValue() );

		assertThat( leftHandPredicateFromElementSpace.getJoins().size(), is( 0 ) );

		assertThat(
				leftHandPredicateFromElementSpace.getRoot().getEntityName(),
				is( "org.hibernate.sqm.test.domain.Person" )
		);
		assertThat( leftHandPredicateFromElementSpace.getRoot().getIdentificationVariable(), is( "b" ) );

		InSubQuerySqmPredicate rightHandPredicate = (InSubQuerySqmPredicate) andPredicate.getRightHandPredicate();

		SqmFromClause rightHandPredicateFromClause = rightHandPredicate.getSubQueryExpression()
				.getQuerySpec()
				.getFromClause();
		assertNotNull( rightHandPredicateFromClause );

		FromElementSpace rightHandPredicateFromElementSpace = rightHandPredicateFromClause.getFromElementSpaces()
				.get( 0 );
		assertThat( rightHandPredicateFromElementSpace.getRoot(), notNullValue() );

		assertThat( rightHandPredicateFromElementSpace.getJoins().size(), is( 0 ) );

		assertThat(
				rightHandPredicateFromElementSpace.getRoot().getEntityName(),
				is( "org.hibernate.sqm.test.domain.Person" )
		);
		assertThat( rightHandPredicateFromElementSpace.getRoot().getIdentificationVariable(), is( "c" ) );
	}


	@Test
	public void testInvalidOnPredicateWithImplicitJoin() throws Exception {
		final String query = "select a from Person a left outer join a.mate c on c.mate.numberOfToes > 5 and c.numberOfToes < 20";
		try {
			interpretSelect( query );
			fail();
		}
		catch (SemanticException expected) {
		}
	}


	@Test
	public void testSimpleDynamicInstantiation() throws Exception {
		final String query = "select new org.hibernate.sqm.test.hql.SimpleSemanticQueryBuilderTest$DTO(a.id as id, a.nickName as name) from Person a";
		final SqmSelectStatement selectStatement = interpretSelect( query );
		SqmQuerySpec querySpec = selectStatement.getQuerySpec();
		assertNotNull( querySpec );
	}

	private static class DTO {
	}
}
