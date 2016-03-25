/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.hql;

import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.query.QuerySpec;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.LiteralIntegerSqmExpression;
import org.hibernate.sqm.query.expression.LiteralLongSqmExpression;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.predicate.AndSqmPredicate;
import org.hibernate.sqm.query.predicate.InSubQuerySqmPredicate;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;

import org.hibernate.test.sqm.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;
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
public class SimpleSemanticQueryBuilderTest {
	private final ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

	@Test
	public void simpleIntegerLiteralsTest() {
		SelectStatement selectStatement = interpret( "select a.basic from Something a where 1=2" );
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

	private SelectStatement interpret(String query) {
		return (SelectStatement) SemanticQueryInterpreter.interpret( query, consumerContext );
	}

	@Test
	public void simpleLongLiteralsTest() {
		SelectStatement selectStatement = interpret( "select a.basic from Something a where 1L=2L" );
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
		final String query = "select a from Something a left outer join a.entity c on c.basic1 > 5 and c.basic2 < 20";
		final SelectStatement selectStatement = (SelectStatement) SemanticQueryInterpreter.interpret(
				query,
				consumerContext
		);
		QuerySpec querySpec = selectStatement.getQuerySpec();
		assertNotNull( querySpec );
	}

	@Test
	public void testSimpleUncorrelatedSubQuery() throws Exception {
		final String query = "select a from Something a where a.entity IN (select e from SomethingElse e where e.basic1 = 5 )";
		final SelectStatement selectStatement = (SelectStatement) SemanticQueryInterpreter.interpret(
				query,
				consumerContext
		);

		FromClause fromClause = selectStatement.getQuerySpec().getFromClause();
		assertNotNull( fromClause );
		assertThat( fromClause.getFromElementSpaces().size(), is( 1 ) );

		FromElementSpace fromElementSpace = fromClause.getFromElementSpaces().get( 0 );
		assertThat( fromElementSpace.getRoot(), notNullValue() );
		assertThat( fromElementSpace.getJoins().size(), is( 0 ) );

		assertThat( fromElementSpace.getRoot().getEntityName(), is( "com.acme.Something" ) );
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
		FromClause subqueryFromClause = subQueryPredicate.getSubQueryExpression().getQuerySpec().getFromClause();
		assertNotNull( subqueryFromClause );
		assertThat( subqueryFromClause.getFromElementSpaces().size(), is( 1 ) );

		FromElementSpace subqueryFromElementSpace = subqueryFromClause.getFromElementSpaces().get( 0 );
		assertThat( subqueryFromElementSpace.getRoot(), notNullValue() );

		assertThat( subqueryFromElementSpace.getJoins().size(), is( 0 ) );

		assertThat(
				subqueryFromElementSpace.getRoot().getEntityName(),
				is( "com.acme.SomethingElse" )
		);
		assertThat( subqueryFromElementSpace.getRoot().getIdentificationVariable(), is( "e" ) );
	}

	@Test
	public void testUncorrelatedSubQueries() throws Exception {
		final String query = "select a from Something a where a.entity IN (select e from SomethingElse e where e.basic1 IN(select e from SomethingElse2 b where b.basic2 = 2 ))";
		final SelectStatement selectStatement = (SelectStatement) SemanticQueryInterpreter.interpret(
				query,
				consumerContext
		);

		FromClause fromClause = selectStatement.getQuerySpec().getFromClause();
		assertNotNull( fromClause );
		assertThat( fromClause.getFromElementSpaces().size(), is( 1 ) );

		FromElementSpace fromElementSpace = fromClause.getFromElementSpaces().get( 0 );
		assertThat( fromElementSpace.getRoot(), notNullValue() );
		assertThat( fromElementSpace.getJoins().size(), is( 0 ) );

		assertThat( fromElementSpace.getRoot().getEntityName(), is( "com.acme.Something" ) );
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
		FromClause subqueryFromClause = subQueryPredicate.getSubQueryExpression().getQuerySpec().getFromClause();
		assertNotNull( subqueryFromClause );
		assertThat( subqueryFromClause.getFromElementSpaces().size(), is( 1 ) );

		FromElementSpace subqueryFromElementSpace = subqueryFromClause.getFromElementSpaces().get( 0 );
		assertThat( subqueryFromElementSpace.getRoot(), notNullValue() );

		assertThat( subqueryFromElementSpace.getJoins().size(), is( 0 ) );

		assertThat(
				subqueryFromElementSpace.getRoot().getEntityName(),
				is( "com.acme.SomethingElse" )
		);
		assertThat( subqueryFromElementSpace.getRoot().getIdentificationVariable(), is( "e" ) );

		// assertions against the root sqm predicate that defines the su-sqm of sub-sqm
		InSubQuerySqmPredicate subSubqueryPredicate = (InSubQuerySqmPredicate) subQueryPredicate.getSubQueryExpression()
				.getQuerySpec()
				.getWhereClause()
				.getPredicate();

		FromClause subSubqueryFromClause = subSubqueryPredicate.getSubQueryExpression().getQuerySpec().getFromClause();
		assertNotNull( subSubqueryFromClause );

		FromElementSpace subSubqueryFromElementSpace = subSubqueryFromClause.getFromElementSpaces().get( 0 );
		assertThat( subSubqueryFromElementSpace.getRoot(), notNullValue() );

		assertThat( subSubqueryFromElementSpace.getJoins().size(), is( 0 ) );

		assertThat(
				subSubqueryFromElementSpace.getRoot().getEntityName(),
				is( "com.acme.SomethingElse2" )
		);
		assertThat( subSubqueryFromElementSpace.getRoot().getIdentificationVariable(), is( "b" ) );
	}

	@Test
	public void testUncorrelatedSubQueriesInAndPredicate() throws Exception {
		final String query = "Select a from Something a where a.b in ( select b from SomethingElse b where b.basic1 = 5) and a.c in ( select c from SomethingElse2 c where c.basic1 = 6)";
		final SelectStatement selectStatement = (SelectStatement) SemanticQueryInterpreter.interpret(
				query,
				consumerContext
		);

		FromClause fromClause = selectStatement.getQuerySpec().getFromClause();
		assertNotNull( fromClause );
		assertThat( fromClause.getFromElementSpaces().size(), is( 1 ) );

		FromElementSpace fromElementSpace = fromClause.getFromElementSpaces().get( 0 );
		assertThat( fromElementSpace.getRoot(), notNullValue() );
		assertThat( fromElementSpace.getJoins().size(), is( 0 ) );

		assertThat( fromElementSpace.getRoot().getEntityName(), is( "com.acme.Something" ) );
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

		FromClause leftHandPredicateFromClause = leftHandPredicate.getSubQueryExpression()
				.getQuerySpec()
				.getFromClause();
		assertNotNull( leftHandPredicateFromClause );

		FromElementSpace leftHandPredicateFromElementSpace = leftHandPredicateFromClause.getFromElementSpaces()
				.get( 0 );
		assertThat( leftHandPredicateFromElementSpace.getRoot(), notNullValue() );

		assertThat( leftHandPredicateFromElementSpace.getJoins().size(), is( 0 ) );

		assertThat(
				leftHandPredicateFromElementSpace.getRoot().getEntityName(),
				is( "com.acme.SomethingElse" )
		);
		assertThat( leftHandPredicateFromElementSpace.getRoot().getIdentificationVariable(), is( "b" ) );

		InSubQuerySqmPredicate rightHandPredicate = (InSubQuerySqmPredicate) andPredicate.getRightHandPredicate();

		FromClause rightHandPredicateFromClause = rightHandPredicate.getSubQueryExpression()
				.getQuerySpec()
				.getFromClause();
		assertNotNull( rightHandPredicateFromClause );

		FromElementSpace rightHandPredicateFromElementSpace = rightHandPredicateFromClause.getFromElementSpaces()
				.get( 0 );
		assertThat( rightHandPredicateFromElementSpace.getRoot(), notNullValue() );

		assertThat( rightHandPredicateFromElementSpace.getJoins().size(), is( 0 ) );

		assertThat(
				rightHandPredicateFromElementSpace.getRoot().getEntityName(),
				is( "com.acme.SomethingElse2" )
		);
		assertThat( rightHandPredicateFromElementSpace.getRoot().getIdentificationVariable(), is( "c" ) );
	}


	@Test
	public void testInvalidOnPredicateWithImplicitJoin() throws Exception {
		final String query = "select a from Something a left outer join a.entity c on c.entity.basic1 > 5 and c.basic2 < 20";
		try {
			SemanticQueryInterpreter.interpret( query, consumerContext );
			fail();
		}
		catch (SemanticException expected) {
		}
	}


	@Test
	public void testSimpleDynamicInstantiation() throws Exception {
		final String query = "select new org.hibernate.test.sqm.parser.hql.SimpleSemanticQueryBuilderTest$DTO(a.basic1 as id, a.basic2 as name) from Something a";
		final SelectStatement selectStatement = (SelectStatement) SemanticQueryInterpreter.interpret(
				query,
				consumerContext
		);
		QuerySpec querySpec = selectStatement.getQuerySpec();
		assertNotNull( querySpec );
	}

	private static class DTO {
	}

	private DomainMetamodel buildMetamodel() {
		ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();

		EntityTypeImpl relatedType = metamodel.makeEntityType( "com.acme.Related" );
		relatedType.makeSingularAttribute(
				"basic1",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		relatedType.makeSingularAttribute(
				"basic2",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		relatedType.makeSingularAttribute(
				"entity",
				relatedType
		);

		EntityTypeImpl somethingType = metamodel.makeEntityType( "com.acme.Something" );
		somethingType.makeSingularAttribute(
				"b",
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		somethingType.makeSingularAttribute(
				"c",
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		somethingType.makeSingularAttribute(
				"basic",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingType.makeSingularAttribute(
				"basic1",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingType.makeSingularAttribute(
				"basic2",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingType.makeSingularAttribute(
				"entity",
				relatedType
		);

		EntityTypeImpl somethingElseType = metamodel.makeEntityType( "com.acme.SomethingElse" );
		somethingElseType.makeSingularAttribute(
				"basic1",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingElseType.makeSingularAttribute(
				"related1",
				relatedType
		);
		somethingElseType.makeSingularAttribute(
				"related2",
				relatedType
		);

		EntityTypeImpl somethingElse2Type = metamodel.makeEntityType( "com.acme.SomethingElse2" );
		somethingElse2Type.makeSingularAttribute(
				"basic1",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingElse2Type.makeSingularAttribute(
				"basic2",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);

		return metamodel;
	}
}
