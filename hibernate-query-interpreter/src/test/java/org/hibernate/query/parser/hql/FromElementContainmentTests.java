/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.hql;

import org.hibernate.query.parser.SemanticQueryInterpreter;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.expression.FromElementReferenceExpression;
import org.hibernate.sqm.query.select.SelectList;
import org.hibernate.sqm.query.select.SelectListItem;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Steve Ebersole
 */
public class FromElementContainmentTests {
	@Test
	public void testFromElementReferenceInSelect() {
		final String query = "select o from Entity o";
		final ConsumerContextTestingImpl consumerContext = new ConsumerContextTestingImpl();
		SelectStatement statement = (SelectStatement) SemanticQueryInterpreter.interpret( query, consumerContext );
		assertThat( statement.getQuerySpec().getSelectClause().getSelection(), instanceOf( SelectList.class ) );
		SelectList selectList = (SelectList) statement.getQuerySpec().getSelectClause().getSelection();
		assertEquals( 1, selectList.getSelectListItems().size() );
		SelectListItem item = selectList.getSelectListItems().get( 0 );
		assertThat( item.getSelectedExpression(), instanceOf( FromElementReferenceExpression.class ) );
	}

	@Test
	public void testFromElementReferenceInOrderBy() {
		final String query = "select o from Entity o order by o";
		final ConsumerContextTestingImpl consumerContext = new ConsumerContextTestingImpl();
		SelectStatement statement = (SelectStatement) SemanticQueryInterpreter.interpret( query, consumerContext );
		assertEquals( 1, statement.getOrderByClause().getSortSpecifications().size() );
		assertThat(
				statement.getOrderByClause().getSortSpecifications().get( 0 ).getSortExpression(),
				instanceOf( FromElementReferenceExpression.class )
		);
	}
}
