/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.hql;

import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.domain.SingularAttributeDescriptor.SingularAttributeClassification;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.expression.LiteralIntegerSqmExpression;
import org.hibernate.sqm.query.expression.ParameterSqmExpression;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.SubQuerySqmExpression;
import org.hibernate.sqm.query.expression.domain.SingularAttributeReference;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;
import org.hibernate.test.sqm.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Test various forms of selections
 *
 * @author Christian Beikov
 */
public class LimitOffsetClauseTests {
	private final ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private DomainMetamodel buildMetamodel() {
		ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();

		EntityTypeImpl entityType = metamodel.makeEntityType( "com.acme.Entity" );
		entityType.makeSingularAttribute(
				"basic",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);

		return metamodel;
	}

	@Test
	public void testSimpleLimit() {
		SqmSelectStatement statement = interpret( "select o from Entity o limit 1" );
		SqmExpression limitExpression = statement.getQuerySpec().getLimitOffsetClause().getLimitExpression();
		assertThat( limitExpression, instanceOf( LiteralIntegerSqmExpression.class ) );
		assertEquals( Integer.valueOf( 1 ), LiteralIntegerSqmExpression.class.cast( limitExpression ).getLiteralValue() );
	}

	@Test
	public void testParameterOffset() {
		SqmSelectStatement statement = interpret( "select o from Entity o offset :param" );
		SqmExpression offsetExpression = statement.getQuerySpec().getLimitOffsetClause().getOffsetExpression();
		assertThat( offsetExpression, instanceOf( ParameterSqmExpression.class ) );
		assertEquals( "param", ParameterSqmExpression.class.cast( offsetExpression ).getName() );
	}

	@Test
	public void testSubqueryLimitOffset() {
		SqmSelectStatement statement = interpret( "select o from Entity o where o.basic = ( select oSub from Entity oSub order by oSub.basic limit 1 )" );
		RelationalSqmPredicate predicate = (RelationalSqmPredicate) statement.getQuerySpec().getWhereClause().getPredicate();
		SubQuerySqmExpression subQuery = (SubQuerySqmExpression) predicate.getRightHandExpression();
		SqmExpression sortExpression = subQuery.getQuerySpec().getOrderByClause().getSortSpecifications().get( 0 ).getSortExpression();
		assertEquals( "basic", SingularAttributeReference.class.cast( sortExpression ).getAttribute().getAttributeName() );
		assertEquals( Integer.valueOf( 1 ), LiteralIntegerSqmExpression.class.cast( subQuery.getQuerySpec().getLimitOffsetClause().getLimitExpression() ).getLiteralValue() );
	}

	private SqmSelectStatement interpret(String query) {
		return (SqmSelectStatement) SemanticQueryInterpreter.interpret( query, consumerContext );
	}

}
