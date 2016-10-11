/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.internal;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.SqmInsertSelectStatement;
import org.hibernate.sqm.query.SqmQuerySpec;
import org.hibernate.sqm.query.from.SqmRoot;

/**
 * @author Steve Ebersole
 */
public class SqmInsertSelectStatementImpl extends AbstractSqmInsertStatement implements SqmInsertSelectStatement {
	private SqmQuerySpec selectQuery;

	public SqmInsertSelectStatementImpl(SqmRoot insertTarget) {
		super( insertTarget );
	}

	@Override
	public SqmQuerySpec getSelectQuery() {
		return selectQuery;
	}

	public void setSelectQuery(SqmQuerySpec selectQuery) {
		this.selectQuery = selectQuery;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitInsertSelectStatement( this );
	}
}
