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
import org.hibernate.sqm.query.set.SetClause;

/**
 * @author Steve Ebersole
 */
public class UpdateStatement implements NonSelectStatement, WhereClauseContainer {
	private final RootEntityFromElement entityFromElement;
	private final SetClause setClause = new SetClause();
	private final WhereClause whereClause = new WhereClause();

	public UpdateStatement(RootEntityFromElement entityFromElement) {
		this.entityFromElement = entityFromElement;
	}

	@Override
	public Type getType() {
		return Type.UPDATE;
	}

	public RootEntityFromElement getEntityFromElement() {
		return entityFromElement;
	}

	public SetClause getSetClause() {
		return setClause;
	}

	@Override
	public WhereClause getWhereClause() {
		return whereClause;
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ROOT,
				"update %s %s %s",
				entityFromElement,
				"[no set clause]",
				whereClause
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitUpdateStatement( this );
	}
}
