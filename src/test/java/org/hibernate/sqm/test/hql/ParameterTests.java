/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql;

import org.hibernate.sqm.domain.SqmSingularAttribute;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.query.SqmParameter;
import org.hibernate.sqm.query.SqmSelectStatement;

import org.hibernate.sqm.test.domain.StandardModelTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Steve Ebersole
 */
public class ParameterTests extends StandardModelTest {
	@Test( expected = SemanticException.class )
	public void testInvalidLegacyPositionalParam() {
		// todo : should we define the rule with the integer as optional and then give a better exception?
		interpret( "select a.nickName from Person a where a.numberOfToes = ?" );
	}

	@Test( expected = SemanticException.class )
	public void testZeroBasedPositionalParam() {
		interpret( "select a.nickName from Person a where a.numberOfToes = ?0" );
	}

	@Test( expected = SemanticException.class )
	public void testNonContiguousPositionalParams() {
		interpret( "select a.nickName from Person a where a.numberOfToes = ?1 or a.numberOfToes = ?3" );

	}

	@Test
	public void testParameterCollection() {
		final SqmSelectStatement sqm = interpretSelect( "select a.nickName from Person a where a.numberOfToes = ?1" );
		assertThat( sqm.getQueryParameters().size(), is(1) );
	}

	@Test
	public void testAnticipatedTypeHandling() {
		final SqmSelectStatement sqm = interpretSelect( "select a.nickName from Person a where a.numberOfToes = ?1" );
		final SqmParameter parameter = sqm.getQueryParameters().iterator().next();
		assertThat( parameter.getAnticipatedType(), is( instanceOf( SqmSingularAttribute.class ) ) );
		assertThat( parameter.allowMultiValuedBinding(), is(false) );
	}

	@Test
	public void testAllowMultiValuedBinding() {
		final SqmSelectStatement sqm = interpretSelect( "select a.nickName from Person a where a.numberOfToes in (?1)" );
		final SqmParameter parameter = sqm.getQueryParameters().iterator().next();
		assertThat( parameter.getAnticipatedType(), is( instanceOf( SqmSingularAttribute.class ) ) );
		assertThat( parameter.allowMultiValuedBinding(), is(true) );
	}
}
