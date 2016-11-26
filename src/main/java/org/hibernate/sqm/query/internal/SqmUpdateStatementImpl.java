/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.internal;

import java.util.Locale;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.SqmUpdateStatement;
import org.hibernate.sqm.query.from.SqmRoot;
import org.hibernate.sqm.query.predicate.SqmWhereClause;
import org.hibernate.sqm.query.set.SqmSetClause;

/**
 * @author Steve Ebersole
 */
public class SqmUpdateStatementImpl extends AbstractSqmDmlStatement implements SqmUpdateStatement {
	private SqmSetClause setClause;
	private SqmWhereClause whereClause;

	public SqmUpdateStatementImpl() {
	}

	public void setSetClause(SqmSetClause setClause) {
		this.setClause = setClause;
	}

	public void setWhereClause(SqmWhereClause whereClause) {
		this.whereClause = whereClause;
	}

	@Override
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
				getEntityFromElement(),
				"[no set clause]",
				whereClause
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitUpdateStatement( this );
	}
}
