/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql;

import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.test.domain.StandardModelTest;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Steve Ebersole
 */
public class JpaCollectionJoinTest extends StandardModelTest {
	@Test
	public void basicTest() {
		SqmSelectStatement statement = interpretSelect( "select e from EntityOfMaps e, IN( e.basicToOneToMany ) l" );
		assertThat( statement.getQuerySpec().getFromClause().getFromElementSpaces().size(), is( 1 ) );
		FromElementSpace fromElementSpace = statement.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 );

		assertThat( fromElementSpace.getRoot(), notNullValue() );
		assertThat( fromElementSpace.getJoins().size(), is(1) );
	}
}
