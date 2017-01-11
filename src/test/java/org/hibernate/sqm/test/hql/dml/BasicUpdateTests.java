/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql.dml;

import org.hibernate.sqm.query.SqmStatement;
import org.hibernate.sqm.query.SqmUpdateStatement;
import org.hibernate.sqm.query.expression.LiteralCharacterSqmExpression;
import org.hibernate.sqm.query.expression.NamedParameterSqmExpression;
import org.hibernate.sqm.query.expression.domain.SqmSingularAttributeBinding;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;
import org.hibernate.sqm.query.set.SqmAssignment;
import org.hibernate.sqm.test.domain.Person;
import org.hibernate.sqm.test.domain.StandardModelTest;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Steve Ebersole
 */
public class BasicUpdateTests extends StandardModelTest {
	@Test
	public void basicUpdateTest() {
		basicUpdateAssertions( "update Person set nickName = 'a' where nickName = :something" );
		basicUpdateAssertions( "update from Person set nickName = 'a' where nickName = :something" );

		basicUpdateAssertions( "update Person e set nickName = 'a' where nickName = :something" );
		basicUpdateAssertions( "update from Person set nickName = 'a' where nickName = :something" );
	}

	private void basicUpdateAssertions(String query) {
		final SqmStatement statement = interpret( query );

		assertThat( statement, instanceOf( SqmUpdateStatement.class ) );
		SqmUpdateStatement updateStatement = (SqmUpdateStatement) statement;

		assertThat( updateStatement.getEntityFromElement().getEntityName(), equalTo( Person.class.getName() ) );

		assertThat( updateStatement.getWhereClause().getPredicate(), instanceOf( RelationalSqmPredicate.class ) );
		RelationalSqmPredicate predicate = (RelationalSqmPredicate) updateStatement.getWhereClause().getPredicate();

		assertThat( predicate.getLeftHandExpression(), instanceOf( SqmSingularAttributeBinding.class ) );
		SqmSingularAttributeBinding binding = (SqmSingularAttributeBinding) predicate.getLeftHandExpression();
		assertSame( binding.getSourceBinding().getExportedFromElement(), updateStatement.getEntityFromElement() );

		assertThat( predicate.getRightHandExpression(), instanceOf( NamedParameterSqmExpression.class ) );

		assertEquals( 1, updateStatement.getSetClause().getAssignments().size() );

		SqmAssignment assignment = updateStatement.getSetClause().getAssignments().get( 0 );
		assertSame( assignment.getStateField().getSourceBinding().getExportedFromElement(), updateStatement.getEntityFromElement() );

		assertThat( assignment.getValue(), instanceOf( LiteralCharacterSqmExpression.class ) );
	}
}
