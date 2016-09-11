/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.internal;

import java.util.Locale;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.SqmDeleteStatement;
import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.predicate.SqmWhereClause;

/**
 * @author Steve Ebersole
 */
public class SqmDeleteStatementImpl extends AbstractSqmStatement implements SqmDeleteStatement {
	private final RootEntityFromElement entityFromElement;
	private final SqmWhereClause whereClause = new SqmWhereClause();

	public SqmDeleteStatementImpl(RootEntityFromElement entityFromElement) {
		this.entityFromElement = entityFromElement;
	}

	@Override
	public RootEntityFromElement getEntityFromElement() {
		return entityFromElement;
	}

	@Override
	public SqmWhereClause getWhereClause() {
		return whereClause;
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ROOT,
				"delete %s %s",
				entityFromElement,
				whereClause
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitDeleteStatement( this );
	}
}
