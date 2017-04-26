/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql.dml;

import org.hibernate.query.sqm.tree.SqmInsertSelectStatement;
import org.hibernate.query.sqm.tree.SqmStatement;
import org.hibernate.query.sqm.tree.expression.domain.SqmSingularAttributeReference;
import org.hibernate.sqm.test.domain.Person;
import org.hibernate.sqm.test.domain.StandardModelTest;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertSame;

/**
 * @author Steve Ebersole
 */
public class BasicInsertTests extends StandardModelTest {
	@Test
	public void basicUpdateTest() {
		basicInsertAssertions( "insert into Person (nickName) select e.nickName + 'a' from Person e" );
	}

	private void basicInsertAssertions(String query) {
		final SqmStatement statement = interpret( query );

		assertThat( statement, instanceOf( SqmInsertSelectStatement.class ) );
		SqmInsertSelectStatement insertStatement = (SqmInsertSelectStatement) statement;

		assertThat( insertStatement.getInsertTarget().getEntityName(), equalTo( Person.class.getName() ) );

		for ( SqmSingularAttributeReference stateField : insertStatement.getStateFields() ) {
			assertSame( insertStatement.getInsertTarget(), stateField.getSourceReference().getExportedFromElement() );
		}
	}
}
