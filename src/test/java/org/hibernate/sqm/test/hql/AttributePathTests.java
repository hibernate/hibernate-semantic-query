/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql;

import java.util.List;

import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.domain.SqmNavigableBinding;
import org.hibernate.sqm.query.expression.domain.SqmSingularAttributeBinding;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;
import org.hibernate.sqm.query.select.SqmSelection;

import org.hibernate.sqm.test.domain.Person;
import org.hibernate.sqm.test.domain.StandardModelTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertSame;

/**
 * @author Steve Ebersole
 */
public class AttributePathTests extends StandardModelTest {
	@Test
	public void testImplicitJoinReuse() {
		final SqmSelectStatement statement = interpretSelect( "select s.mate.name.first, s.mate.name.last from Person s" );

		assertThat( statement.getQuerySpec().getFromClause().getFromElementSpaces().size(), is(1) );
		final FromElementSpace space = statement.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 );

		assertThat( space.getJoins().size(), is(1) );

		// from-clause paths
//		assertPropertyPath( space.getRoot(), "com.acme.Something(s)" );
//		assertPropertyPath( space.getJoins().get( 0 ), "com.acme.Something(s).entity" );

		final List<SqmSelection> selections = statement.getQuerySpec().getSelectClause().getSelections();
		assertThat( selections.size(), is(2) );

		// expression paths
		assertPropertyPath( selections.get( 0 ).getExpression(), Person.class.getName() + "(s).mate.name.first" );
		assertPropertyPath( selections.get( 1 ).getExpression(), Person.class.getName() + "(s).mate.name.last" );
	}

	private void assertPropertyPath(SqmExpression expression, String expectedFullPath) {
		assertThat( expression, instanceOf( SqmNavigableBinding.class ) );
		final SqmNavigableBinding domainReferenceBinding = (SqmNavigableBinding) expression;
		assertThat( domainReferenceBinding.getPropertyPath().getFullPath(), is(expectedFullPath) );
	}

	@Test
	public void testImplicitJoinReuse2() {
		final SqmSelectStatement statement = interpretSelect( "select s.mate from Person s where s.mate.name.first = ?1" );

		assertThat( statement.getQuerySpec().getFromClause().getFromElementSpaces().size(), is(1) );
		final FromElementSpace space = statement.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 );

		assertThat( space.getJoins().size(), is(1) );

		final SqmSelection selection = statement.getQuerySpec().getSelectClause().getSelections().get( 0 );
		assertThat( selection.getExpression(), instanceOf( SqmSingularAttributeBinding.class ) );
		final SqmSingularAttributeBinding selectExpression = (SqmSingularAttributeBinding) selection.getExpression();
		assertThat( selectExpression.getExportedFromElement(), notNullValue() );

		final RelationalSqmPredicate predicate = (RelationalSqmPredicate) statement.getQuerySpec().getWhereClause().getPredicate();
		final SqmSingularAttributeBinding predicateLhs = (SqmSingularAttributeBinding) predicate.getLeftHandExpression();
		assertThat( predicateLhs.getSourceBinding().getExportedFromElement(), notNullValue() );


		// from-clause paths
//		assertPropertyPath( space.getRoot(), "com.acme.Something(s)" );
//		assertPropertyPath( space.getJoins().get( 0 ), "com.acme.Something(s).entity" );

		// expression paths
		assertPropertyPath( selection.getExpression(), "org.hibernate.sqm.test.domain.Person(s).mate" );
		assertPropertyPath( predicateLhs, "org.hibernate.sqm.test.domain.Person(s).mate.name.first" );
	}
}
