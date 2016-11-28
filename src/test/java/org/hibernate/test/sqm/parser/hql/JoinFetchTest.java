/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.parser.hql;

import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.SqmAttributeJoin;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Steve Ebersole
 */
public class JoinFetchTest extends StandardModelTest {
	@Test
	public void testImplicitJoinReuse() {
		final SqmSelectStatement statement = interpretSelect( "select s from Something s join fetch s.entity" );

		assertThat( statement.getQuerySpec().getFromClause().getFromElementSpaces().size(), is(1) );
		final FromElementSpace space = statement.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 );

		assertThat( space.getJoins().size(), is(1) );

		final SqmAttributeJoin sqmJoin = (SqmAttributeJoin) space.getJoins().get( 0 );
		assertThat( sqmJoin.getFetchParentUniqueIdentifier(), is( space.getRoot().getUniqueIdentifier() ) );

	}
}
