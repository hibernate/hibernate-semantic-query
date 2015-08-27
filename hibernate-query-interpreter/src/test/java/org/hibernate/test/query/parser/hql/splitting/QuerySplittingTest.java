/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.query.parser.hql.splitting;

import org.hibernate.query.parser.QuerySplitter;
import org.hibernate.query.parser.SemanticQueryInterpreter;
import org.hibernate.sqm.domain.ModelMetadata;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.Statement;

import org.hibernate.test.query.parser.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.dynamic.ExplicitModelMetadata;
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
		SelectStatement statement = (SelectStatement) SemanticQueryInterpreter.interpret(
				"from Account",
				consumerContext
		);
		Statement[] split = QuerySplitter.split( statement );
		assertEquals( 1, split.length );
		assertSame( statement, split[0] );

		statement = (SelectStatement) SemanticQueryInterpreter.interpret( "from Fund", consumerContext );
		split = QuerySplitter.split( statement );
		assertEquals( 1, split.length );
		assertSame( statement, split[0] );

		// Now try with an unmapped reference
		statement = (SelectStatement) SemanticQueryInterpreter.interpret(
				// NOTE : we added an import for this too
				"from Auditable",
				consumerContext
		);
		split = QuerySplitter.split( statement );
		assertEquals( 2, split.length );
	}

	private ModelMetadata buildModelMetadata() {
		ExplicitModelMetadata metadata = new ExplicitModelMetadata();
		ExplicitModelMetadata.EntityTypeDescriptorImpl acct = metadata.entity( Account.class );
		ExplicitModelMetadata.EntityTypeDescriptorImpl fund = metadata.entity( Fund.class );
		ExplicitModelMetadata.PolymorphicEntityTypeDescriptorImpl intf = metadata.polymorphicEntity( Auditable.class );
		intf.addImplementor( acct );
		intf.addImplementor( fund );

		return metadata;
	}
}
