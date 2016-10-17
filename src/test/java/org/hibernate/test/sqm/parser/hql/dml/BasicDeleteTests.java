/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.hql.dml;

import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.domain.SingularAttributeReference.SingularAttributeClassification;
import org.hibernate.sqm.parser.common.AttributeBinding;
import org.hibernate.sqm.query.SqmDeleteStatement;
import org.hibernate.sqm.query.SqmStatement;
import org.hibernate.sqm.query.expression.NamedParameterSqmExpression;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;

import org.hibernate.test.sqm.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertSame;

/**
 * @author Steve Ebersole
 */
public class BasicDeleteTests {
	@Test
	public void basicDeleteTest() {
		ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

		basicDeleteAssertions( "delete Entity1 where basic1 = :something", consumerContext );
		basicDeleteAssertions( "delete from Entity1 where basic1 = :something", consumerContext );

		basicDeleteAssertions( "delete Entity1 e where e.basic1 = :something", consumerContext );
		basicDeleteAssertions( "delete from Entity1 e where e.basic1 = :something", consumerContext );

		basicDeleteAssertions( "delete Entity1 e where basic1 = :something", consumerContext );
		basicDeleteAssertions( "delete from Entity1 e where basic1 = :something", consumerContext );
	}

	private void basicDeleteAssertions(String query, ConsumerContextImpl consumerContext) {
		final SqmStatement statement = SemanticQueryInterpreter.interpret( query, consumerContext );

		assertThat( statement, instanceOf( SqmDeleteStatement.class ) );
		SqmDeleteStatement deleteStatement = (SqmDeleteStatement) statement;

		assertThat( deleteStatement.getEntityFromElement().getEntityName(), equalTo( "com.acme.Entity1" ) );

		assertThat( deleteStatement.getWhereClause().getPredicate(), instanceOf( RelationalSqmPredicate.class ) );
		RelationalSqmPredicate predicate = (RelationalSqmPredicate) deleteStatement.getWhereClause().getPredicate();

		assertThat( predicate.getLeftHandExpression(), instanceOf( AttributeBinding.class ) );
		AttributeBinding binding = (AttributeBinding) predicate.getLeftHandExpression();
		assertSame( binding.getLhs().getFromElement(), deleteStatement.getEntityFromElement() );

		assertThat( predicate.getRightHandExpression(), instanceOf( NamedParameterSqmExpression.class ) );
	}

	private DomainMetamodel buildMetamodel() {
		ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();
		EntityTypeImpl entityType = metamodel.makeEntityType( "com.acme.Entity1" );
		entityType.makeSingularAttribute(
				"basic1",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		return metamodel;
	}
}
