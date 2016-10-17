/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.hql;

import org.hibernate.sqm.query.SqmSelectStatement;

import org.junit.Test;

/**
 * @author Steve Ebersole
 */
public class EntityTypeExpressionTests extends StandardModelTest {
	@Test
	public void testEntityTypeExpressionByAlias() {
		SqmSelectStatement statement = interpretSelect( "select a from Something a where type(a) = Something" );

	}
}
