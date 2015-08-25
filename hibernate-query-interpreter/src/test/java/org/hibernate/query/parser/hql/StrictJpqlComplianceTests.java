/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser.hql;

import org.hibernate.query.parser.SemanticQueryInterpreter;
import org.hibernate.query.parser.StrictJpaComplianceViolation;

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
		ConsumerContextTestingImpl consumerContext = new ConsumerContextTestingImpl();

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
		final ConsumerContextTestingImpl consumerContext = new ConsumerContextTestingImpl();

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
		final ConsumerContextTestingImpl consumerContext = new ConsumerContextTestingImpl();

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
}
