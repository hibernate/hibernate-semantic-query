/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.query.parser.hql;

import org.hibernate.sqm.parser.SemanticQueryInterpreter;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.expression.FromElementReferenceExpression;
import org.hibernate.sqm.query.select.Selection;

import org.hibernate.test.query.parser.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
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
		final ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );
		SelectStatement statement = (SelectStatement) SemanticQueryInterpreter.interpret( query, consumerContext );
		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		Selection selection = statement.getQuerySpec().getSelectClause().getSelections().get( 0 );
		assertThat( selection.getExpression(), instanceOf( FromElementReferenceExpression.class ) );
	}

	@Test
	public void testFromElementReferenceInOrderBy() {
		final String query = "select o from Entity o order by o";
		final ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );
		SelectStatement statement = (SelectStatement) SemanticQueryInterpreter.interpret( query, consumerContext );
		assertEquals( 1, statement.getOrderByClause().getSortSpecifications().size() );
		assertThat(
				statement.getOrderByClause().getSortSpecifications().get( 0 ).getSortExpression(),
				instanceOf( FromElementReferenceExpression.class )
		);
	}

	private DomainMetamodel buildMetamodel() {
		ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();
		EntityTypeImpl entityType = metamodel.makeEntityType( "com.acme.Entity" );
		return metamodel;
	}
}
