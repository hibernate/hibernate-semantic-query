/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser.hql;

import java.util.Collection;

import org.hibernate.query.parser.SemanticException;
import org.hibernate.query.parser.SemanticQueryInterpreter;
import org.hibernate.query.parser.internal.hql.HqlParseTreeBuilder;
import org.hibernate.query.parser.internal.hql.antlr.HqlParser;
import org.hibernate.query.parser.internal.hql.phase1.FromClauseProcessor;
import org.hibernate.query.parser.internal.hql.phase2.SemanticQueryBuilder;
import org.hibernate.sqm.query.QuerySpec;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.expression.LiteralIntegerExpression;
import org.hibernate.sqm.query.expression.LiteralLongExpression;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.predicate.AndPredicate;
import org.hibernate.sqm.query.predicate.InSubQueryPredicate;

import org.junit.Test;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.xpath.XPath;

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
	@Test
	public void simpleIntegerLiteralsTest() {
		final ParsingContextTestingImpl parsingContext = new ParsingContextTestingImpl();

		final HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql( "select a.basic from Something a where 1=2" );

		final FromClauseProcessor fromClauseProcessor = new FromClauseProcessor( new ParsingContextTestingImpl() );
		ParseTreeWalker.DEFAULT.walk( fromClauseProcessor, parser.statement() );

		parser.reset();

		Collection<ParseTree> logicalExpressions = XPath.findAll( parser.statement(), "//predicate", parser );
		assertEquals( 1, logicalExpressions.size() );
		ParseTree logicalExpression = logicalExpressions.iterator().next();
		// 3 -> the 2 expressions, plus the operand (=)
		assertEquals( 3, logicalExpression.getChildCount() );

		SemanticQueryBuilder semanticQueryBuilder = new SemanticQueryBuilder( parsingContext, fromClauseProcessor );
		Object lhs = logicalExpression.getChild( 0 ).accept( semanticQueryBuilder );
		assertNotNull( lhs );
		assertTrue( lhs instanceof LiteralIntegerExpression );
		assertEquals( 1, ((LiteralIntegerExpression) lhs).getLiteralValue().intValue() );

		Object rhs = logicalExpression.getChild( 2 ).accept( semanticQueryBuilder );
		assertNotNull( rhs );
		assertTrue( rhs instanceof LiteralIntegerExpression );
		assertEquals( 2, ((LiteralIntegerExpression) rhs).getLiteralValue().intValue() );

		parser.reset();

		semanticQueryBuilder = new SemanticQueryBuilder( parsingContext, fromClauseProcessor );
		SelectStatement selectStatement = semanticQueryBuilder.visitSelectStatement( parser.selectStatement() );
		selectStatement.getQuerySpec();
	}

	@Test
	public void simpleLongLiteralsTest() {
		final ParsingContextTestingImpl parsingContext = new ParsingContextTestingImpl();

		final HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql( "select a.basic from Something a where 1L=2L" );

		final FromClauseProcessor fromClauseProcessor = new FromClauseProcessor( new ParsingContextTestingImpl() );
		ParseTreeWalker.DEFAULT.walk( fromClauseProcessor, parser.statement() );

		parser.reset();

		Collection<ParseTree> logicalExpressions = XPath.findAll( parser.statement(), "//predicate", parser );
		assertEquals( 1, logicalExpressions.size() );
		ParseTree logicalExpression = logicalExpressions.iterator().next();
		// 3 -> the 2 expressions, plus the operand (=)
		assertEquals( 3, logicalExpression.getChildCount() );

		SemanticQueryBuilder semanticQueryBuilder = new SemanticQueryBuilder( parsingContext, fromClauseProcessor );
		Object lhs = logicalExpression.getChild( 0 ).accept( semanticQueryBuilder );
		assertNotNull( lhs );
		assertTrue( lhs instanceof LiteralLongExpression );
		assertEquals( 1L, ((LiteralLongExpression) lhs).getLiteralValue().longValue() );

		Object rhs = logicalExpression.getChild( 2 ).accept( semanticQueryBuilder );
		assertNotNull( rhs );
		assertTrue( rhs instanceof LiteralLongExpression );
		assertEquals( 2L, ((LiteralLongExpression) rhs).getLiteralValue().longValue() );

	}

	@Test
	public void testAttributeJoinWithOnPredicate() throws Exception {
		final String query = "select a from Something a left outer join a.entity c on c.basic1 > 5 and c.basic2 < 20";
		final SelectStatement selectStatement = (SelectStatement) SemanticQueryInterpreter.interpret(
				query,
				new ConsumerContextTestingImpl()
		);
		QuerySpec querySpec = selectStatement.getQuerySpec();
		assertNotNull( querySpec );
	}

	@Test
	public void testSimpleUncorrelatedSubQuery() throws Exception {
		final String query = "select a from Something a where a.entity IN (select e from SomethingElse e where e.basic1 = 5 )";
		final SelectStatement selectStatement = (SelectStatement) SemanticQueryInterpreter.interpret(
				query,
				new ConsumerContextTestingImpl()
		);

		FromClause fromClause = selectStatement.getQuerySpec().getFromClause();
		assertNotNull( fromClause );
		assertThat( fromClause.getFromElementSpaces().size(), is( 1 ) );

		FromElementSpace fromElementSpace = fromClause.getFromElementSpaces().get( 0 );
		assertThat( fromElementSpace.getRoot(), notNullValue() );
		assertThat( fromElementSpace.getJoins().size(), is( 0 ) );

		assertThat( fromElementSpace.getRoot().getTypeDescriptor().getTypeName(), is( "com.acme.Something" ) );
		assertThat( fromElementSpace.getRoot().getAlias(), is( "a" ) );

		// assertions against the root query predicate that defines the sub-query
		assertThat( selectStatement.getQuerySpec().getWhereClause().getPredicate(), notNullValue() );
		assertThat(
				selectStatement.getQuerySpec().getWhereClause().getPredicate(),
				is( instanceOf( InSubQueryPredicate.class ) )
		);

		InSubQueryPredicate subQueryPredicate = (InSubQueryPredicate) selectStatement.getQuerySpec()
				.getWhereClause()
				.getPredicate();
		FromClause subqueryFromClause = subQueryPredicate.getSubQueryExpression().getQuerySpec().getFromClause();
		assertNotNull( subqueryFromClause );
		assertThat( subqueryFromClause.getFromElementSpaces().size(), is( 1 ) );

		FromElementSpace subqueryFromElementSpace = subqueryFromClause.getFromElementSpaces().get( 0 );
		assertThat( subqueryFromElementSpace.getRoot(), notNullValue() );

		assertThat( subqueryFromElementSpace.getJoins().size(), is( 0 ) );

		assertThat(
				subqueryFromElementSpace.getRoot().getTypeDescriptor().getTypeName(),
				is( "com.acme.SomethingElse" )
		);
		assertThat( subqueryFromElementSpace.getRoot().getAlias(), is( "e" ) );
	}

	@Test
	public void testUncorrelatedSubQueries() throws Exception {
		final String query = "select a from Something a where a.entity IN (select e from SomethingElse e where e.basic1 IN(select e from SomethingElse2 b where b.basic2 = 2 ))";
		final SelectStatement selectStatement = (SelectStatement) SemanticQueryInterpreter.interpret(
				query,
				new ConsumerContextTestingImpl()
		);

		FromClause fromClause = selectStatement.getQuerySpec().getFromClause();
		assertNotNull( fromClause );
		assertThat( fromClause.getFromElementSpaces().size(), is( 1 ) );

		FromElementSpace fromElementSpace = fromClause.getFromElementSpaces().get( 0 );
		assertThat( fromElementSpace.getRoot(), notNullValue() );
		assertThat( fromElementSpace.getJoins().size(), is( 0 ) );

		assertThat( fromElementSpace.getRoot().getTypeDescriptor().getTypeName(), is( "com.acme.Something" ) );
		assertThat( fromElementSpace.getRoot().getAlias(), is( "a" ) );

		// assertions against the root query predicate that defines the sub-query
		assertThat( selectStatement.getQuerySpec().getWhereClause().getPredicate(), notNullValue() );
		assertThat(
				selectStatement.getQuerySpec().getWhereClause().getPredicate(),
				is( instanceOf( InSubQueryPredicate.class ) )
		);

		InSubQueryPredicate subQueryPredicate = (InSubQueryPredicate) selectStatement.getQuerySpec()
				.getWhereClause()
				.getPredicate();
		FromClause subqueryFromClause = subQueryPredicate.getSubQueryExpression().getQuerySpec().getFromClause();
		assertNotNull( subqueryFromClause );
		assertThat( subqueryFromClause.getFromElementSpaces().size(), is( 1 ) );

		FromElementSpace subqueryFromElementSpace = subqueryFromClause.getFromElementSpaces().get( 0 );
		assertThat( subqueryFromElementSpace.getRoot(), notNullValue() );

		assertThat( subqueryFromElementSpace.getJoins().size(), is( 0 ) );

		assertThat(
				subqueryFromElementSpace.getRoot().getTypeDescriptor().getTypeName(),
				is( "com.acme.SomethingElse" )
		);
		assertThat( subqueryFromElementSpace.getRoot().getAlias(), is( "e" ) );

		// assertions against the root query predicate that defines the su-query of sub-query
		InSubQueryPredicate subSubqueryPredicate = (InSubQueryPredicate) subQueryPredicate.getSubQueryExpression()
				.getQuerySpec()
				.getWhereClause()
				.getPredicate();

		FromClause subSubqueryFromClause = subSubqueryPredicate.getSubQueryExpression().getQuerySpec().getFromClause();
		assertNotNull( subSubqueryFromClause );

		FromElementSpace subSubqueryFromElementSpace = subSubqueryFromClause.getFromElementSpaces().get( 0 );
		assertThat( subSubqueryFromElementSpace.getRoot(), notNullValue() );

		assertThat( subSubqueryFromElementSpace.getJoins().size(), is( 0 ) );

		assertThat(
				subSubqueryFromElementSpace.getRoot().getTypeDescriptor().getTypeName(),
				is( "com.acme.SomethingElse2" )
		);
		assertThat( subSubqueryFromElementSpace.getRoot().getAlias(), is( "b" ) );
	}

	@Test
	public void testUncorrelatedSubQueriesInAndPredicate() throws Exception {
		final String query = "Select a from Something a where a.b in ( select b from SomethingElse b where b.basic = 5) and a.c in ( select c from SomethingElse2 c where c.basic1 = 6)";
		final SelectStatement selectStatement = (SelectStatement) SemanticQueryInterpreter.interpret(
				query,
				new ConsumerContextTestingImpl()
		);

		FromClause fromClause = selectStatement.getQuerySpec().getFromClause();
		assertNotNull( fromClause );
		assertThat( fromClause.getFromElementSpaces().size(), is( 1 ) );

		FromElementSpace fromElementSpace = fromClause.getFromElementSpaces().get( 0 );
		assertThat( fromElementSpace.getRoot(), notNullValue() );
		assertThat( fromElementSpace.getJoins().size(), is( 0 ) );

		assertThat( fromElementSpace.getRoot().getTypeDescriptor().getTypeName(), is( "com.acme.Something" ) );
		assertThat( fromElementSpace.getRoot().getAlias(), is( "a" ) );

		// assertions against the root query predicate that defines the sub-query
		assertThat( selectStatement.getQuerySpec().getWhereClause().getPredicate(), notNullValue() );
		assertThat(
				selectStatement.getQuerySpec().getWhereClause().getPredicate(),
				is( instanceOf( AndPredicate.class ) )
		);

		AndPredicate andPredicate = (AndPredicate) selectStatement.getQuerySpec()
				.getWhereClause()
				.getPredicate();

		assertThat( andPredicate.getLeftHandPredicate(), is( instanceOf( InSubQueryPredicate.class ) ) );

		assertThat( andPredicate.getRightHandPredicate(), is( instanceOf( InSubQueryPredicate.class ) ) );

		InSubQueryPredicate leftHandPredicate = (InSubQueryPredicate) andPredicate.getLeftHandPredicate();

		FromClause leftHandPredicateFromClause = leftHandPredicate.getSubQueryExpression()
				.getQuerySpec()
				.getFromClause();
		assertNotNull( leftHandPredicateFromClause );

		FromElementSpace leftHandPredicateFromElementSpace = leftHandPredicateFromClause.getFromElementSpaces()
				.get( 0 );
		assertThat( leftHandPredicateFromElementSpace.getRoot(), notNullValue() );

		assertThat( leftHandPredicateFromElementSpace.getJoins().size(), is( 0 ) );

		assertThat(
				leftHandPredicateFromElementSpace.getRoot().getTypeDescriptor().getTypeName(),
				is( "com.acme.SomethingElse" )
		);
		assertThat( leftHandPredicateFromElementSpace.getRoot().getAlias(), is( "b" ) );

		InSubQueryPredicate rightHandPredicate = (InSubQueryPredicate) andPredicate.getRightHandPredicate();

		FromClause rightHandPredicateFromClause = rightHandPredicate.getSubQueryExpression()
				.getQuerySpec()
				.getFromClause();
		assertNotNull( rightHandPredicateFromClause );

		FromElementSpace rightHandPredicateFromElementSpace = rightHandPredicateFromClause.getFromElementSpaces()
				.get( 0 );
		assertThat( rightHandPredicateFromElementSpace.getRoot(), notNullValue() );

		assertThat( rightHandPredicateFromElementSpace.getJoins().size(), is( 0 ) );

		assertThat(
				rightHandPredicateFromElementSpace.getRoot().getTypeDescriptor().getTypeName(),
				is( "com.acme.SomethingElse2" )
		);
		assertThat( rightHandPredicateFromElementSpace.getRoot().getAlias(), is( "c" ) );
	}


	@Test
	public void testInvalidOnPredicateWithImplicitJoin() throws Exception {
		final String query = "select a from Something a left outer join a.entity c on c.entity.basic1 > 5 and c.basic2 < 20";
		try {
			SemanticQueryInterpreter.interpret( query, new ConsumerContextTestingImpl() );
			fail();
		}
		catch (SemanticException expected) {
		}
	}


	@Test
	public void testSimpleDynamicInstantiation() throws Exception {
		final String query = "select new org.hibernate.query.parser.hql.SimpleSemanticQueryBuilderTest$DTO(a.basic1 as id, a.basic2 as name) from Something a";
		final SelectStatement selectStatement = (SelectStatement) SemanticQueryInterpreter.interpret(
				query,
				new ConsumerContextTestingImpl()
		);
		QuerySpec querySpec = selectStatement.getQuerySpec();
		assertNotNull( querySpec );
	}

	private static class DTO {
	}
}
