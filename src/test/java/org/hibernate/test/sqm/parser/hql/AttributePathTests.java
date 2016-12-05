/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.hql;

import java.util.List;

import org.hibernate.sqm.query.PropertyPath;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.domain.DomainReferenceBinding;
import org.hibernate.sqm.query.expression.domain.SingularAttributeBinding;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.SqmFrom;
import org.hibernate.sqm.query.from.SqmRoot;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;
import org.hibernate.sqm.query.select.SqmSelection;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Steve Ebersole
 */
public class AttributePathTests extends StandardModelTest {
	@Test
	public void testImplicitJoinReuse() {
		final SqmSelectStatement statement = interpretSelect( "select s.entity.basic1, s.entity.basic2 from Something s" );

		assertThat( statement.getQuerySpec().getFromClause().getFromElementSpaces().size(), is(1) );
		final FromElementSpace space = statement.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 );

		assertThat( space.getJoins().size(), is(1) );

		// from-clause paths
		assertPropertyPath( space.getRoot(), "com.acme.Something(s)" );
		assertPropertyPath( space.getJoins().get( 0 ), "com.acme.Something(s).entity" );

		final List<SqmSelection> selections = statement.getQuerySpec().getSelectClause().getSelections();
		assertThat( selections.size(), is(2) );

		// expression paths
		assertPropertyPath( selections.get( 0 ).getExpression(), "com.acme.Something(s).entity.basic1" );
		assertPropertyPath( selections.get( 1 ).getExpression(), "com.acme.Something(s).entity.basic2" );
	}

	private void assertPropertyPath(SqmFrom fromElement, String expectedFullPath) {
		assertThat( fromElement.getPropertyPath().getFullPath(), is(expectedFullPath) );
	}

	private void assertPropertyPath(SqmExpression expression, String expectedFullPath) {
		assertThat( expression, instanceOf( DomainReferenceBinding.class ) );
		final DomainReferenceBinding domainReferenceBinding = (DomainReferenceBinding) expression;
		assertThat( domainReferenceBinding.getPropertyPath().getFullPath(), is(expectedFullPath) );
	}

	@Test
	public void testImplicitJoinReuse2() {
		final SqmSelectStatement statement = interpretSelect( "select s.entity from Something s where s.entity.basic2 = ?1" );

		assertThat( statement.getQuerySpec().getFromClause().getFromElementSpaces().size(), is(1) );
		final FromElementSpace space = statement.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 );

		assertThat( space.getJoins().size(), is(1) );

		final SqmSelection selection = statement.getQuerySpec().getSelectClause().getSelections().get( 0 );
		assertThat( selection.getExpression(), instanceOf( SingularAttributeBinding.class ) );
		final SingularAttributeBinding selectExpression = (SingularAttributeBinding) selection.getExpression();
		assertThat( selectExpression.getFromElement(), notNullValue() );

		final RelationalSqmPredicate predicate = (RelationalSqmPredicate) statement.getQuerySpec().getWhereClause().getPredicate();
		final SingularAttributeBinding predicateLhs = (SingularAttributeBinding) predicate.getLeftHandExpression();
		assertThat( predicateLhs.getLhs().getFromElement(), notNullValue() );

		assertThat( predicateLhs.getLhs().getFromElement(), sameInstance( selectExpression.getFromElement() ) );


		// from-clause paths
		assertPropertyPath( space.getRoot(), "com.acme.Something(s)" );
		assertPropertyPath( space.getJoins().get( 0 ), "com.acme.Something(s).entity" );

		// expression paths
		assertPropertyPath( selection.getExpression(), "com.acme.Something(s).entity" );
		assertPropertyPath( predicateLhs, "com.acme.Something(s).entity.basic2" );
	}
}
