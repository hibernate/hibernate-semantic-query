/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.hql;

import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.StrictJpaComplianceViolation;
import org.hibernate.sqm.domain.DomainMetamodel;

import org.hibernate.test.sqm.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Testing strict JPQL compliance checking
 *
 * @author Steve Ebersole
 * @author Christian Beikov
 */
public class StrictJpqlComplianceTests {
	@Test
	public void testImplicitSelectClause() {
		final String query = "from Entity";
		ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

		// first test HQL superset is allowed...
		SemanticQueryInterpreter.interpret( query, consumerContext );

		// now enable strict compliance and try again, should lead to error
		consumerContext.enableStrictJpaCompliance();
		try {
			SemanticQueryInterpreter.interpret( query, consumerContext );
			fail( "expected violation" );
		}
		catch (StrictJpaComplianceViolation v) {
			assertEquals( StrictJpaComplianceViolation.Type.IMPLICIT_SELECT , v.getType() );
		}
	}

	@Test
	public void testUnmappedPolymorphicReference() {
		final String query = "select o from PolymorphicEntity o";
		ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

		// first test HQL superset is allowed...
		SemanticQueryInterpreter.interpret( query, consumerContext );

		// now enable strict compliance and try again, should lead to error
		consumerContext.enableStrictJpaCompliance();
		try {
			SemanticQueryInterpreter.interpret( query, consumerContext );
			fail( "expected violation" );
		}
		catch (StrictJpaComplianceViolation v) {
			assertEquals( StrictJpaComplianceViolation.Type.UNMAPPED_POLYMORPHISM , v.getType() );
		}
	}

	@Test
	public void testAliasedFetchJoin() {
		final String query = "select o from Entity o join fetch o.entity e";
		ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

		// first test HQL superset is allowed...
		SemanticQueryInterpreter.interpret( query, consumerContext );

		// now enable strict compliance and try again, should lead to error
		consumerContext.enableStrictJpaCompliance();
		try {
			SemanticQueryInterpreter.interpret( query, consumerContext );
			fail( "expected violation" );
		}
		catch (StrictJpaComplianceViolation v) {
			assertEquals( StrictJpaComplianceViolation.Type.ALIASED_FETCH_JOIN , v.getType() );
		}
	}

	@Test
	public void testNonStandardFunctionCall() {
		final String query = "select o from Entity o where my_func(o.basic) = 1";
		ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

		// first test HQL superset is allowed...
		SemanticQueryInterpreter.interpret( query, consumerContext );

		// now enable strict compliance and try again, should lead to error
		consumerContext.enableStrictJpaCompliance();
		try {
			SemanticQueryInterpreter.interpret( query, consumerContext );
			fail( "expected violation" );
		}
		catch (StrictJpaComplianceViolation v) {
			assertEquals( StrictJpaComplianceViolation.Type.FUNCTION_CALL , v.getType() );
		}
	}

	@Test
	public void testLimitOffset() {
		final String query = "select o from Entity o limit 1 offset 1";
		ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

		// first test HQL superset is allowed...
		SemanticQueryInterpreter.interpret( query, consumerContext );

		// now enable strict compliance and try again, should lead to error
		consumerContext.enableStrictJpaCompliance();
		try {
			SemanticQueryInterpreter.interpret( query, consumerContext );
			fail( "expected violation" );
		}
		catch (StrictJpaComplianceViolation v) {
			assertEquals( StrictJpaComplianceViolation.Type.LIMIT_OFFSET_CLAUSE , v.getType() );
		}
	}

	@Test
	public void testSubqueryOrderBy() {
		final String query = "select o from Entity o where o.basic = ( select oSub from Entity oSub order by oSub.basic limit 1 )";
		ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

		// first test HQL superset is allowed...
		SemanticQueryInterpreter.interpret( query, consumerContext );

		// now enable strict compliance and try again, should lead to error
		consumerContext.enableStrictJpaCompliance();
		try {
			SemanticQueryInterpreter.interpret( query, consumerContext );
			fail( "expected violation" );
		}
		catch (StrictJpaComplianceViolation v) {
			assertEquals( StrictJpaComplianceViolation.Type.SUBQUERY_ORDER_BY , v.getType() );
		}
	}

	private DomainMetamodel buildMetamodel() {
		ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();
		EntityTypeImpl entityType = metamodel.makeEntityType( "com.acme.Entity" );
		entityType.makeSingularAttribute(
				"basic",
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		entityType.makeSingularAttribute(
				"entity",
				entityType
		);

		metamodel.makePolymorphicEntity( "com.acme.PolymorphicEntity" );
		return metamodel;
	}
}
