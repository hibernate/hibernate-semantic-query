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
import org.hibernate.sqm.query.predicate.SqmWhereClause;
import org.hibernate.sqm.query.predicate.SqmWhereClauseContainer;
import org.hibernate.sqm.query.set.SqmSetClause;

/**
 * @author Steve Ebersole
 */
public class SqmStatementUpdate implements SqmStatementNonSelect, SqmWhereClauseContainer {
	private final RootEntityFromElement entityFromElement;
	private final SqmSetClause setClause = new SqmSetClause();
	private final SqmWhereClause whereClause = new SqmWhereClause();

	public SqmStatementUpdate(RootEntityFromElement entityFromElement) {
		this.entityFromElement = entityFromElement;
	}

	public RootEntityFromElement getEntityFromElement() {
		return entityFromElement;
	}

	public SqmSetClause getSetClause() {
		return setClause;
	}

	@Override
	public SqmWhereClause getWhereClause() {
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
