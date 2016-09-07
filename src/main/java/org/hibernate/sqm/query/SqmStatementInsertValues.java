/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query;

import org.hibernate.sqm.NotYetImplementedException;
import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.from.RootEntityFromElement;

/**
 * @author Steve Ebersole
 */
public class SqmStatementInsertValues extends AbstractSqmStatementInsert {
	public SqmStatementInsertValues(RootEntityFromElement insertTarget) {
		super( insertTarget );
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		throw new NotYetImplementedException( "Support for INSERT-VALUES style SQM statements not yet implemented" );
	}
}
