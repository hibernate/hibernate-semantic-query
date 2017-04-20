/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql;

import org.hibernate.query.sqm.StrictJpaComplianceViolation;
import org.hibernate.sqm.test.domain.StandardModelTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Testing strict JPQL compliance checking
 *
 * @author Steve Ebersole
 * @author Christian Beikov
 */
public class StrictJpqlComplianceTests extends StandardModelTest {
	@Test
	public void testImplicitSelectClause() {
		final String query = "from Person";

		// first test HQL superset is allowed...
		interpret( query );

		// now enable strict compliance and try again, should lead to error
		consumerContext.enableStrictJpaCompliance();
		try {
			interpret( query );
			fail( "expected violation" );
		}
		catch (StrictJpaComplianceViolation v) {
			assertEquals( StrictJpaComplianceViolation.Type.IMPLICIT_SELECT , v.getType() );
		}
	}

	@Test
	public void testUnmappedPolymorphicReference() {
		final String query = "select o from java.lang.Object o";

		// first test HQL superset is allowed...
		interpret( query );

		// now enable strict compliance and try again, should lead to error
		consumerContext.enableStrictJpaCompliance();
		try {
			interpret( query );
			fail( "expected violation" );
		}
		catch (StrictJpaComplianceViolation v) {
			assertEquals( StrictJpaComplianceViolation.Type.UNMAPPED_POLYMORPHISM , v.getType() );
		}
	}

	@Test
	public void testAliasedFetchJoin() {
		final String query = "select o from Person o join fetch o.mate e";

		// first test HQL superset is allowed...
		interpret( query );

		// now enable strict compliance and try again, should lead to error
		consumerContext.enableStrictJpaCompliance();
		try {
			interpret( query );
			fail( "expected violation" );
		}
		catch (StrictJpaComplianceViolation v) {
			assertEquals( StrictJpaComplianceViolation.Type.ALIASED_FETCH_JOIN , v.getType() );
		}
	}

	@Test
	public void testNonStandardFunctionCall() {
		final String query = "select o from Person o where my_func(o.nickName) = 1";

		// first test HQL superset is allowed...
		interpret( query );

		// now enable strict compliance and try again, should lead to error
		consumerContext.enableStrictJpaCompliance();
		try {
			interpret( query );
			fail( "expected violation" );
		}
		catch (StrictJpaComplianceViolation v) {
			assertEquals( StrictJpaComplianceViolation.Type.FUNCTION_CALL , v.getType() );
		}
	}

	@Test
	public void testLimitOffset() {
		final String query = "select o from Person o limit 1 offset 1";

		// first test HQL superset is allowed...
		interpret( query );

		// now enable strict compliance and try again, should lead to error
		consumerContext.enableStrictJpaCompliance();
		try {
			interpret( query );
			fail( "expected violation" );
		}
		catch (StrictJpaComplianceViolation v) {
			assertEquals( StrictJpaComplianceViolation.Type.LIMIT_OFFSET_CLAUSE , v.getType() );
		}
	}

	@Test
	public void testSubqueryOrderBy() {
		final String query = "select o from Person o where o.mate = ( select oSub from Person oSub order by oSub.nickName limit 1 )";

		// first test HQL superset is allowed...
		interpret( query );

		// now enable strict compliance and try again, should lead to error
		consumerContext.enableStrictJpaCompliance();
		try {
			interpret( query );
			fail( "expected violation" );
		}
		catch (StrictJpaComplianceViolation v) {
			assertEquals( StrictJpaComplianceViolation.Type.SUBQUERY_ORDER_BY , v.getType() );
		}
	}
}
