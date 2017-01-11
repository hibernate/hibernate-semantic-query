/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql.splitting;

import org.hibernate.sqm.QuerySplitter;
import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.SqmStatement;
import org.hibernate.sqm.test.domain.StandardModelTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Steve Ebersole
 */
public class QuerySplittingTest extends StandardModelTest {

	@Test
	public void testQuerySplitting() {
		// first try directly with the 2 mapped classes
		SqmSelectStatement statement = (SqmSelectStatement) SemanticQueryInterpreter.interpret(
				"from Person",
				consumerContext
		);
		SqmStatement[] split = QuerySplitter.split( statement );
		assertEquals( 1, split.length );
		assertSame( statement, split[0] );

		statement = (SqmSelectStatement) SemanticQueryInterpreter.interpret( "from Person", consumerContext );
		split = QuerySplitter.split( statement );
		assertEquals( 1, split.length );
		assertSame( statement, split[0] );

		// Now try with an unmapped reference
		statement = (SqmSelectStatement) SemanticQueryInterpreter.interpret(
				// NOTE : we added an import for this too
				"from java.lang.Object",
				consumerContext
		);
		split = QuerySplitter.split( statement );
		assertEquals( 8, split.length );
	}
}
