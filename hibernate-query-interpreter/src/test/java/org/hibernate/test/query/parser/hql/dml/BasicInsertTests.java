/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.query.parser.hql.dml;

import org.hibernate.query.parser.SemanticQueryInterpreter;
import org.hibernate.sqm.query.InsertSelectStatement;
import org.hibernate.sqm.query.Statement;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;

import org.hibernate.test.query.parser.ConsumerContextImpl;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertSame;

/**
 * @author Steve Ebersole
 */
public class BasicInsertTests {
	@Test
	public void basicUpdateTest() {
		basicInsertAssertions( "insert into Entity1 (basic1, basic2) select 'a', e.basic2+1 from Entity2 e" );
	}

	private void basicInsertAssertions(String query) {
		ConsumerContextImpl consumerContext = new ConsumerContextImpl();

		final Statement statement = SemanticQueryInterpreter.interpret( query, consumerContext );

		assertThat( statement, instanceOf( InsertSelectStatement.class ) );
		InsertSelectStatement insertStatement = (InsertSelectStatement) statement;

		assertThat( insertStatement.getInsertTarget().getEntityName(), equalTo( "com.acme.Entity1" ) );

		for ( AttributeReferenceExpression stateField : insertStatement.getStateFields() ) {
			assertSame( insertStatement.getInsertTarget(), stateField.getUnderlyingFromElement() );
		}
	}
}
