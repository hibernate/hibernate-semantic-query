/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql;

import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.expression.domain.CollectionElementBinding;
import org.hibernate.sqm.query.select.SqmSelection;

import org.hibernate.sqm.test.domain.StandardModelTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Steve Ebersole
 */
public class CollectionPathExpressionsTest extends StandardModelTest {
	@Test
	public void testMapKeyPath() {
		interpretSelect( "select p from EntityOfMaps p where key( p.basicToBasicMap ) = 'en'" );
	}

	@Test
	public void testCollectionReferenceAsSelection() {
		// essentially, assert that a raw reference to a plural attribute is
		//		implicitly handled as a reference to the elements, in the
		// 		select clause anyway
		final SqmSelectStatement statement = interpretSelect( "select t from EntityOfMaps p join p.basicToBasicMap t" );
		assertThat( statement.getQuerySpec().getSelectClause().getSelections().size(), is(1) );

		final SqmSelection selection = statement.getQuerySpec().getSelectClause().getSelections().get( 0 );
		assertThat( selection.getExpression(), instanceOf( CollectionElementBinding.class ) );
	}

	@Test
	public void testMapIndexedAccess() {
		interpretSelect( "select t.basicToComponentMap['LA'].part1 from EntityOfMaps t" );
	}
}
