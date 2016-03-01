/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.query.parser.hql.dml;

import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.query.Statement;
import org.hibernate.sqm.query.UpdateStatement;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.LiteralCharacterExpression;
import org.hibernate.sqm.query.expression.NamedParameterExpression;
import org.hibernate.sqm.query.predicate.RelationalPredicate;
import org.hibernate.sqm.query.set.Assignment;

import org.hibernate.test.query.parser.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Steve Ebersole
 */
public class BasicUpdateTests {
	@Test
	public void basicUpdateTest() {
		basicUpdateAssertions( "update Entity1 set basic2 = 'a' where basic1 = :something" );
		basicUpdateAssertions( "update from Entity1 set basic2 = 'a' where basic1 = :something" );

//		basicUpdateAssertions( "update Entity1 e set e.basic2 = 'a' where e.basic1 = :something" );
//		basicUpdateAssertions( "update from Entity1 set e.basic2 = 'a' where e.basic1 = :something" );

		basicUpdateAssertions( "update Entity1 e set basic2 = 'a' where basic1 = :something" );
		basicUpdateAssertions( "update from Entity1 set basic2 = 'a' where basic1 = :something" );
	}

	private void basicUpdateAssertions(String query) {
		ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

		final Statement statement = SemanticQueryInterpreter.interpret( query, consumerContext );

		assertThat( statement, instanceOf( UpdateStatement.class ) );
		UpdateStatement updateStatement = (UpdateStatement) statement;

		assertThat( updateStatement.getEntityFromElement().getEntityName(), equalTo( "com.acme.Entity1" ) );

		assertThat( updateStatement.getWhereClause().getPredicate(), instanceOf( RelationalPredicate.class ) );
		RelationalPredicate predicate = (RelationalPredicate) updateStatement.getWhereClause().getPredicate();

		assertThat( predicate.getLeftHandExpression(), instanceOf( AttributeReferenceExpression.class ) );
		AttributeReferenceExpression attributeReferenceExpression = (AttributeReferenceExpression) predicate.getLeftHandExpression();
		assertSame( attributeReferenceExpression.getBoundFromElementBinding().getFromElement(), updateStatement.getEntityFromElement() );

		assertThat( predicate.getRightHandExpression(), instanceOf( NamedParameterExpression.class ) );

		assertEquals( 1, updateStatement.getSetClause().getAssignments().size() );

		Assignment assignment = updateStatement.getSetClause().getAssignments().get( 0 );
		assertSame( assignment.getStateField().getBoundFromElementBinding().getFromElement(), updateStatement.getEntityFromElement() );

		assertThat( assignment.getValue(), instanceOf( LiteralCharacterExpression.class ) );
	}

	private DomainMetamodel buildMetamodel() {
		ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();
		EntityTypeImpl entityType = metamodel.makeEntityType( "com.acme.Entity1" );
		entityType.makeSingularAttribute(
				"basic1",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		entityType.makeSingularAttribute(
				"basic2",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		return metamodel;
	}
}
