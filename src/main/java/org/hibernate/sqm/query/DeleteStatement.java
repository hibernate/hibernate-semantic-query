/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query;

import java.util.Locale;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.predicate.WhereClause;
import org.hibernate.sqm.query.predicate.WhereClauseContainer;

/**
 * @author Steve Ebersole
 */
public class DeleteStatement implements NonSelectStatement, WhereClauseContainer {
	private final RootEntityFromElement entityFromElement;
	private final WhereClause whereClause = new WhereClause();

	public DeleteStatement(RootEntityFromElement entityFromElement) {
		this.entityFromElement = entityFromElement;
	}

	@Override
	public Type getType() {
		return Type.DELETE;
	}

	public RootEntityFromElement getEntityFromElement() {
		return entityFromElement;
	}

	@Override
	public WhereClause getWhereClause() {
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
