/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql.dml;

import org.hibernate.query.sqm.produce.spi.SemanticQueryProducer;
import org.hibernate.query.sqm.tree.SqmDeleteStatement;
import org.hibernate.query.sqm.tree.SqmStatement;
import org.hibernate.query.sqm.tree.expression.NamedParameterSqmExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmSingularAttributeReference;
import org.hibernate.query.sqm.tree.predicate.RelationalSqmPredicate;
import org.hibernate.sqm.test.ConsumerContextImpl;
import org.hibernate.sqm.test.domain.StandardModelTest;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertSame;

/**
 * @author Steve Ebersole
 */
public class BasicDeleteTests extends StandardModelTest {
	@Test
	public void basicDeleteTest() {
		basicDeleteAssertions( "delete Person where nickName = :something", consumerContext );
		basicDeleteAssertions( "delete from Person where nickName = :something", consumerContext );

		basicDeleteAssertions( "delete Person e where e.nickName = :something", consumerContext );
		basicDeleteAssertions( "delete from Person e where e.nickName = :something", consumerContext );

		basicDeleteAssertions( "delete Person e where nickName = :something", consumerContext );
		basicDeleteAssertions( "delete from Person e where nickName = :something", consumerContext );
	}

	private void basicDeleteAssertions(String query, ConsumerContextImpl consumerContext) {
		final SqmStatement statement = SemanticQueryProducer.interpret( query, consumerContext );

		assertThat( statement, instanceOf( SqmDeleteStatement.class ) );
		SqmDeleteStatement deleteStatement = (SqmDeleteStatement) statement;

		assertThat( deleteStatement.getEntityFromElement().getEntityName(), equalTo( "org.hibernate.sqm.test.domain.Person" ) );

		assertThat( deleteStatement.getWhereClause().getPredicate(), instanceOf( RelationalSqmPredicate.class ) );
		RelationalSqmPredicate predicate = (RelationalSqmPredicate) deleteStatement.getWhereClause().getPredicate();

		assertThat( predicate.getLeftHandExpression(), instanceOf( SqmSingularAttributeReference.class ) );
		SqmSingularAttributeReference binding = (SqmSingularAttributeReference) predicate.getLeftHandExpression();
		assertSame( binding.getSourceReference().getExportedFromElement(), deleteStatement.getEntityFromElement() );

		assertThat( predicate.getRightHandExpression(), instanceOf( NamedParameterSqmExpression.class ) );
	}
}
