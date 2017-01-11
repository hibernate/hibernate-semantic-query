/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql;

import org.hibernate.sqm.StrictJpaComplianceViolation;
import org.hibernate.sqm.parser.InterpretationException;

import org.hibernate.sqm.parser.UnknownEntityException;
import org.hibernate.sqm.test.domain.StandardModelTest;

import org.junit.Test;

import static org.hibernate.sqm.SemanticQueryInterpreter.interpret;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Steve Ebersole
 */
public class KeywordAsIdentifierTest extends StandardModelTest {
	@Test
	public void testKeywordAsIdentificationVariable() {
		final String queryIllegal = "select abs.name from KeywordCrazyEntity abs";
		final String queryLegal = "select abs.name from KeywordCrazyEntity as abs";

		try {
			interpret( queryIllegal );
			fail( "exception failure" );
		}
		catch (UnknownEntityException ignore) {
		}

		// first test HQL superset is allowed...
		interpret( queryLegal );

		// now enable strict compliance and try again, should lead to error
		consumerContext.enableStrictJpaCompliance();
		try {
			interpret( queryLegal );
			fail( "expected violation" );
		}
		catch (StrictJpaComplianceViolation v) {
			assertEquals( StrictJpaComplianceViolation.Type.RESERVED_WORD_USED_AS_ALIAS , v.getType() );
		}
	}

	@Test
	public void testKeywordAsResultVariable() {
		final String queryIllegal = "select e.name abs from KeywordCrazyEntity e";
		final String queryLegal = "select e.name as abs from KeywordCrazyEntity as e";

		try {
			interpret( queryIllegal );
		}
		catch (Exception ignore) {
		}

		// first test HQL superset is allowed...
		interpret( queryLegal );

		// now enable strict compliance and try again, should lead to error
		consumerContext.enableStrictJpaCompliance();
		try {
			interpret( queryLegal );
			fail( "expected violation" );
		}
		catch (StrictJpaComplianceViolation v) {
			assertEquals( StrictJpaComplianceViolation.Type.RESERVED_WORD_USED_AS_ALIAS , v.getType() );
		}
	}

	@Test
	public void testKeywordAsAttributeNameInSelect() {
		final String query = "select e.from from KeywordCrazyEntity e";

		interpret( query );

		consumerContext.enableStrictJpaCompliance();
		interpret( query );
	}
}
