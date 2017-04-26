/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql;

import org.hibernate.query.sqm.tree.SqmSelectStatement;
import org.hibernate.query.sqm.tree.expression.domain.SqmEntityReference;
import org.hibernate.query.sqm.tree.select.SqmSelection;
import org.hibernate.sqm.test.domain.StandardModelTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Steve Ebersole
 */
public class SinglePassSmokeTest extends StandardModelTest {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testSimpleAliasSelection() {
		SqmSelectStatement statement = interpretSelect( "select o from Person o" );
		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		SqmSelection selection = statement.getQuerySpec().getSelectClause().getSelections().get( 0 );
		assertThat( selection.getExpression(), instanceOf( SqmEntityReference.class ) );
	}
}
