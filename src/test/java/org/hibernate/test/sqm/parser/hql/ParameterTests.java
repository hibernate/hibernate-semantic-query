/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.hql;

import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.query.Parameter;
import org.hibernate.sqm.query.SqmSelectStatement;

import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Steve Ebersole
 */
public class ParameterTests extends StandardModelTest {
	@Test( expected = NumberFormatException.class )
	public void testInvalidLegacyPositionalParam() {
		// todo : should we define the rule with the integer as optional and then give a better exception?
		interpret( "select a.basic from Something a where a.b = ?" );
	}

	@Test( expected = SemanticException.class )
	public void testZeroBasedPositionalParam() {
		interpret( "select a.basic from Something a where a.b = ?0" );
	}

	@Test( expected = SemanticException.class )
	public void testNonContiguousPositionalParams() {
		interpret( "select a.basic from Something a where a.b = ?1 or a.b = ?3" );
	}

	@Test
	public void testParameterCollection() {
		final SqmSelectStatement sqm = (SqmSelectStatement) interpret( "select a.basic from Something a where a.b = ?1" );
		assertThat( sqm.getQueryParameters().size(), is(1) );
	}

	@Test
	public void testAnticipatedTypeHandling() {
		final SqmSelectStatement sqm = (SqmSelectStatement) interpret( "select a.basic from Something a where a.b = ?1" );
		final Parameter parameter = sqm.getQueryParameters().iterator().next();
		assertThat( parameter.getAnticipatedType(), is( StandardBasicTypeDescriptors.INSTANCE.STRING ) );
		assertThat( parameter.allowMultiValuedBinding(), is(false) );
	}
}
