/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.query.parser.hql;

import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.path.FromElementBinding;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.select.Selection;

import org.hibernate.test.query.parser.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hibernate.sqm.SemanticQueryInterpreter.interpret;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Steve Ebersole
 */
public class FromElementContainmentTests {
	final ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

	@Test
	public void testFromElementReferenceInSelect() {
		final String query = "select o from Entity o";
		SelectStatement statement = (SelectStatement) interpret( query, consumerContext );
		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		Selection selection = statement.getQuerySpec().getSelectClause().getSelections().get( 0 );
		assertThat( selection.getExpression(), instanceOf( FromElementBinding.class ) );
	}

	@Test
	public void testFromElementReferenceInOrderBy() {
		final String query = "select o from Entity o order by o";
		SelectStatement statement = (SelectStatement) interpret( query, consumerContext );
		assertEquals( 1, statement.getOrderByClause().getSortSpecifications().size() );
		assertThat(
				statement.getOrderByClause().getSortSpecifications().get( 0 ).getSortExpression(),
				instanceOf( FromElementBinding.class )
		);
	}

	@Test
	public void testCrossSpaceReferencesFail() {
		final String query = "select e from Entity e, Entity e2 join Entity e3 on e3.id = e.id ";
		try {
			interpret( query, consumerContext );
			fail( "Expecting failure" );
		}
		catch (SemanticException e) {
			assertThat( e.getMessage(), startsWith( "Qualified join predicate referred to FromElement [" ) );
			assertThat( e.getMessage(), endsWith( "] outside the FromElementSpace containing the join" ) );
		}
	}

	private DomainMetamodel buildMetamodel() {
		ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();
		EntityTypeImpl entityType = metamodel.makeEntityType( "com.acme.Entity" );
		return metamodel;
	}
}
