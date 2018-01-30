/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.sqm.produce.navigable.internal;

import org.hibernate.query.sqm.produce.navigable.spi.AbstractNavigableReferenceBuilderContext;
import org.hibernate.query.sqm.produce.spi.SemanticQueryBuilder;
import org.hibernate.sqm.query.JoinType;

/**
 * @author Steve Ebersole
 */
public class FromElementNavigableReferenceBuilderContextImpl extends AbstractNavigableReferenceBuilderContext {
	private final String alias;
	private final JoinType joinType;
	private final boolean fetched;

	public FromElementNavigableReferenceBuilderContextImpl(
			String alias,
			JoinType joinType,
			boolean fetched,
			SemanticQueryBuilder semanticQueryBuilder) {
		super( semanticQueryBuilder );
		this.alias = alias;
		this.joinType = joinType;
		this.fetched = fetched;
	}

	@Override
	public boolean forceTerminalJoin() {
		return true;
	}

	@Override
	public String getTerminalJoinAlias() {
		return alias;
	}

	@Override
	public JoinType getJoinType() {
		return joinType;
	}

	@Override
	public boolean isFetched() {
		return fetched;
	}

	@Override
	public boolean canReuseJoins() {
		return false;
	}
}
