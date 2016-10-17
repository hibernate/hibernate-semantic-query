/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.hql;

import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.parser.common.AttributeBinding;
import org.hibernate.sqm.parser.common.EntityBinding;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.SqmFrom;
import org.hibernate.sqm.query.from.SqmRoot;
import org.hibernate.sqm.query.select.SqmSelection;

import org.hibernate.test.sqm.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hibernate.sqm.SemanticQueryInterpreter.interpret;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Steve Ebersole
 */
public class FromElementContainmentTests {
	final ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

	@Test
	public void testPathExpression() {
		final String query = "select o.other from Entity o";
		SqmSelectStatement statement = (SqmSelectStatement) interpret( query, consumerContext );

		assertEquals( 1, statement.getQuerySpec().getFromClause().getFromElementSpaces().size() );
		final FromElementSpace fromElementSpace = statement.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 );
		assertThat( fromElementSpace.getJoins().size(), is(1) );

		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		SqmSelection selection = statement.getQuerySpec().getSelectClause().getSelections().get( 0 );
		assertThat( selection.getExpression(), instanceOf( AttributeBinding.class ) );

		assertSame( fromElementSpace.getJoins().get( 0 ), ( (AttributeBinding) selection.getExpression() ).getFromElement() );
	}

	@Test
	public void testFromElementReferenceInSelect() {
		final String query = "select o from Entity o";
		SqmSelectStatement statement = (SqmSelectStatement) interpret( query, consumerContext );

		assertEquals( 1, statement.getQuerySpec().getFromClause().getFromElementSpaces().size() );
		final FromElementSpace fromElementSpace = statement.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 );
		final SqmRoot fromElement = fromElementSpace.getRoot();

		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		SqmSelection selection = statement.getQuerySpec().getSelectClause().getSelections().get( 0 );
		assertThat( selection.getExpression(), instanceOf( EntityBinding.class ) );

		assertSame( fromElement, ( (EntityBinding) selection.getExpression() ).getFromElement() );
	}

	@Test
	public void testFromElementReferenceInOrderBy() {
		final String query = "select o from Entity o order by o";
		SqmSelectStatement statement = (SqmSelectStatement) interpret( query, consumerContext );

		assertEquals( 1, statement.getQuerySpec().getFromClause().getFromElementSpaces().size() );
		SqmRoot fromElement = statement.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 ).getRoot();

		assertEquals( 1, statement.getOrderByClause().getSortSpecifications().size() );
		assertThat(
				statement.getOrderByClause().getSortSpecifications().get( 0 ).getSortExpression(),
				instanceOf( EntityBinding.class )
		);

		assertSame(
				fromElement,
				( (EntityBinding) statement.getOrderByClause().getSortSpecifications().get( 0 ).getSortExpression() ).getFromElement()
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
		entityType.makeSingularAttribute( "other", entityType );
		return metamodel;
	}
}
