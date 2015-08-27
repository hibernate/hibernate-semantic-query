/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.query.parser.hql;

import org.hibernate.query.parser.SemanticQueryInterpreter;
import org.hibernate.query.parser.StrictJpaComplianceViolation;

import org.hibernate.test.query.parser.ConsumerContextImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Testing strict JPQL compliance checking
 *
 * @author Steve Ebersole
 */
public class StrictJpqlComplianceTests {
	@Test
	public void testImplicitSelectClause() {
		final String query = "from Entity";
		ConsumerContextImpl consumerContext = new ConsumerContextImpl();

		// first test HQL superset is allowed...
		SemanticQueryInterpreter.interpret( "from Entity", consumerContext );

		// now enable strict compliance and try again, should lead to error
		consumerContext.enableStrictJpaCompliance();
		try {
			SemanticQueryInterpreter.interpret( "from Entity", consumerContext );
			fail( "expected violation" );
		}
		catch (StrictJpaComplianceViolation v) {
			assertEquals( StrictJpaComplianceViolation.Type.IMPLICIT_SELECT , v.getType() );
		}
	}

	@Test
	public void testUnmappedPolymorphicReference() {
		final String query = "select o from PolymorphicEntity o";
		final ConsumerContextImpl consumerContext = new ConsumerContextImpl();

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
		final ConsumerContextImpl consumerContext = new ConsumerContextImpl();

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
		final ConsumerContextImpl consumerContext = new ConsumerContextImpl();

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
}
