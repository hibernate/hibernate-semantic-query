/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql;

import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.expression.domain.SqmEntityBinding;
import org.hibernate.sqm.query.expression.domain.SqmSingularAttributeBinding;
import org.hibernate.sqm.query.from.SqmFromElementSpace;
import org.hibernate.sqm.query.from.SqmRoot;
import org.hibernate.sqm.query.select.SqmSelection;
import org.hibernate.sqm.test.domain.StandardModelTest;

import org.hibernate.test.sqm.domain.FromElementHelper;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Steve Ebersole
 */
public class FromElementContainmentTests extends StandardModelTest {
	@Test
	public void testPathExpression() {
		final String query = "select p.mate from Person p";
		SqmSelectStatement statement = interpretSelect( query );

		assertEquals( 1, statement.getQuerySpec().getFromClause().getFromElementSpaces().size() );
		final SqmFromElementSpace fromElementSpace = statement.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 );
		assertThat( fromElementSpace.getJoins().size(), is(1) );

		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		SqmSelection selection = statement.getQuerySpec().getSelectClause().getSelections().get( 0 );
		assertThat( selection.getExpression(), instanceOf( SqmSingularAttributeBinding.class ) );

		assertSame( fromElementSpace.getJoins().get( 0 ), FromElementHelper.extractExpressionFromElement( selection.getExpression() ) );
	}

	@Test
	public void testFromElementReferenceInSelect() {
		final String query = "select p from Person p";
		SqmSelectStatement statement = interpretSelect( query );

		assertEquals( 1, statement.getQuerySpec().getFromClause().getFromElementSpaces().size() );
		final SqmFromElementSpace fromElementSpace = statement.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 );
		final SqmRoot fromElement = fromElementSpace.getRoot();

		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		SqmSelection selection = statement.getQuerySpec().getSelectClause().getSelections().get( 0 );
		assertThat( selection.getExpression(), instanceOf( SqmEntityBinding.class ) );

		assertSame( fromElement, FromElementHelper.extractExpressionFromElement( selection.getExpression() ) );
	}

	@Test
	public void testFromElementReferenceInOrderBy() {
		final String query = "select p from Person p order by p";
		SqmSelectStatement statement = interpretSelect( query );

		assertEquals( 1, statement.getQuerySpec().getFromClause().getFromElementSpaces().size() );
		SqmRoot fromElement = statement.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 ).getRoot();

		assertEquals( 1, statement.getQuerySpec().getOrderByClause().getSortSpecifications().size() );
		assertThat(
				statement.getQuerySpec().getOrderByClause().getSortSpecifications().get( 0 ).getSortExpression(),
				instanceOf( SqmEntityBinding.class )
		);

		assertSame(
				fromElement,
				FromElementHelper.extractExpressionFromElement( statement.getQuerySpec().getOrderByClause().getSortSpecifications().get( 0 ).getSortExpression() )
		);
	}

	@Test
	public void testCrossSpaceReferencesFail() {
		final String query = "select p from Person p, Person p2 join Person p3 on p3.id = p.id ";
		try {
			interpret( query );
			fail( "Expecting failure" );
		}
		catch (SemanticException e) {
			assertThat( e.getMessage(), startsWith( "Qualified join predicate referred to FromElement [" ) );
			assertThat( e.getMessage(), endsWith( "] outside the FromElementSpace containing the join" ) );
		}
	}
}
