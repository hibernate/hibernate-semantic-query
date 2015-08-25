/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser.hql;

import java.util.Collection;

import org.hibernate.query.parser.internal.hql.antlr.HqlParser;
import org.hibernate.query.parser.internal.hql.antlr.HqlParser.EqualityPredicateContext;
import org.hibernate.query.parser.internal.hql.antlr.HqlParser.LiteralExpressionContext;
import org.hibernate.query.parser.internal.hql.antlr.HqlParserBaseVisitor;
import org.hibernate.query.parser.internal.hql.HqlParseTreeBuilder;

import org.junit.Test;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Simple tests to make sure the basics are working and to see a visual of the parse tree.
 *
 * @author Steve Ebersole
 */
public class HqlParserTest {
	@Test
	public void justTestIt() throws Exception {
		HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql( "select a.b from Something a where a.c = '1'" );

		Collection<ParseTree> fromClauses = XPath.findAll( parser.statement(), "//fromClause", parser );
		assertEquals( 1, fromClauses.size() );
	}

	@Test
	public void testIndexAccess() throws Exception {
		final String qry = "select o from Order o where o.items[0].id = 1234";

		HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql( qry );

		Collection<ParseTree> fromClauses = XPath.findAll( parser.statement(), "//fromClause", parser );
		assertEquals( 1, fromClauses.size() );
	}

	@Test
	public void testTimestampLiterals() throws Exception {
		validateDateTimeLiteralInEqualityPredicate(
				HqlParseTreeBuilder.INSTANCE.parseHql(
						"select a.b from Something a where a.c = {ts '2015-01-09 20:11:11.123455'}"
				),
				DateTimeLiteralType.TIMESTAMP
		);
	}

	@Test
	public void testDateLiterals() throws Exception {
		validateDateTimeLiteralInEqualityPredicate(
				HqlParseTreeBuilder.INSTANCE.parseHql(
						"select a.b from Something a where a.c = {d '2015-01-09'}"
				),
				DateTimeLiteralType.DATE
		);
	}

	@Test
	public void testTimeLiterals() throws Exception {
		validateDateTimeLiteralInEqualityPredicate(
				HqlParseTreeBuilder.INSTANCE.parseHql(
						"select a.b from Something a where a.c = {t '20:11:11'}"
				),
				DateTimeLiteralType.TIME
		);
	}

	/**
	 * Pass in a parse tree that has exactly one equality predicate with the right hand operand being the expected literal
	 */
	private void validateDateTimeLiteralInEqualityPredicate(HqlParser parser, DateTimeLiteralType expectedType) {
		Collection<ParseTree> predicates = XPath.findAll( parser.statement(), "//predicate", parser );
		assertEquals( 1, predicates.size() );

		EqualityPredicateContext predicate = (EqualityPredicateContext) predicates.iterator().next();
		// predicate[1] -> the literal
		assertTrue( predicate.expression( 1 ) instanceof LiteralExpressionContext );

		LiteralExpressionContext lec = (LiteralExpressionContext) predicate.expression( 1 );
		DateTimeLiteralType found = lec.literal().accept( new DateTimeLiteralCategorizingVisitor() );

		assertEquals( expectedType, found );
	}

	enum DateTimeLiteralType {
		TIMESTAMP,
		DATE,
		TIME,
		NOT_A_DATE_TIME
	}

	public class DateTimeLiteralCategorizingVisitor extends HqlParserBaseVisitor<DateTimeLiteralType> {
		@Override
		public DateTimeLiteralType visitLiteral(HqlParser.LiteralContext ctx) {
			DateTimeLiteralType result = super.visitLiteral( ctx );
			if ( result == null ) {
				result = DateTimeLiteralType.NOT_A_DATE_TIME;
			}

			return result;
		}

		@Override
		public DateTimeLiteralType visitTimestampLiteral(HqlParser.TimestampLiteralContext ctx) {
			return DateTimeLiteralType.TIMESTAMP;
		}

		@Override
		public DateTimeLiteralType visitDateLiteral(HqlParser.DateLiteralContext ctx) {
			return DateTimeLiteralType.DATE;
		}

		@Override
		public DateTimeLiteralType visitTimeLiteral(HqlParser.TimeLiteralContext ctx) {
			return DateTimeLiteralType.TIME;
		}
	}
}
