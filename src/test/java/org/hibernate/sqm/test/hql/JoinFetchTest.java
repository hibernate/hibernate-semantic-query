/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.test.hql;

import org.hibernate.query.sqm.tree.SqmSelectStatement;
import org.hibernate.query.sqm.tree.from.SqmFromElementSpace;
import org.hibernate.query.sqm.tree.from.SqmAttributeJoin;

import org.hibernate.sqm.test.domain.StandardModelTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Steve Ebersole
 */
public class JoinFetchTest extends StandardModelTest {
	@Test
	public void testImplicitJoinReuse() {
		final SqmSelectStatement statement = interpretSelect( "select s from Person s join fetch s.mate" );

		assertThat( statement.getQuerySpec().getFromClause().getFromElementSpaces().size(), is(1) );
		final SqmFromElementSpace space = statement.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 );

		assertThat( space.getJoins().size(), is(1) );

		final SqmAttributeJoin sqmJoin = (SqmAttributeJoin) space.getJoins().get( 0 );
		assertThat( sqmJoin.isFetched(), is(true) );
		assertThat(
				sqmJoin.getAttributeBinding().getSourceReference().getExportedFromElement().getUniqueIdentifier(),
				is( space.getRoot().getUniqueIdentifier() )
		);

	}
}
