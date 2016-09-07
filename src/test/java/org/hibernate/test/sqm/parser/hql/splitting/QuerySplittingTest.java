/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.hql.splitting;

import org.hibernate.sqm.QuerySplitter;
import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.query.SqmStatementSelect;
import org.hibernate.sqm.query.SqmStatement;

import org.hibernate.test.sqm.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.PolymorphicEntityTypeImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Steve Ebersole
 */
public class QuerySplittingTest {

	@Test
	public void testQuerySplitting() {
		ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildModelMetadata() );

		// first try directly with the 2 mapped classes
		SqmStatementSelect statement = (SqmStatementSelect) SemanticQueryInterpreter.interpret(
				"from Account",
				consumerContext
		);
		SqmStatement[] split = QuerySplitter.split( statement );
		assertEquals( 1, split.length );
		assertSame( statement, split[0] );

		statement = (SqmStatementSelect) SemanticQueryInterpreter.interpret( "from Fund", consumerContext );
		split = QuerySplitter.split( statement );
		assertEquals( 1, split.length );
		assertSame( statement, split[0] );

		// Now try with an unmapped reference
		statement = (SqmStatementSelect) SemanticQueryInterpreter.interpret(
				// NOTE : we added an import for this too
				"from Auditable",
				consumerContext
		);
		split = QuerySplitter.split( statement );
		assertEquals( 2, split.length );
	}

	private ExplicitDomainMetamodel buildModelMetadata() {
		ExplicitDomainMetamodel metadata = new ExplicitDomainMetamodel();
		EntityTypeImpl acct = metadata.makeEntityType( Account.class );
		EntityTypeImpl fund = metadata.makeEntityType( Fund.class );
		PolymorphicEntityTypeImpl intf = metadata.makePolymorphicEntity( Auditable.class );
		intf.addImplementor( acct );
		intf.addImplementor( fund );
		intf.buildAttributes();

		return metadata;
	}
}
