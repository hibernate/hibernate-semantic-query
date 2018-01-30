/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.sqm.produce.navigable.spi;

import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.parser.common.ParsingContext;
import org.hibernate.sqm.parser.common.QuerySpecProcessingState;
import org.hibernate.query.sqm.produce.spi.SemanticQueryBuilder;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.expression.domain.SqmNavigableReference;
import org.hibernate.sqm.query.from.FromElementSpace;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractNavigableReferenceBuilderContext implements NavigableReferenceBuilderContext {
	private final SemanticQueryBuilder semanticQueryBuilder;

	public AbstractNavigableReferenceBuilderContext(SemanticQueryBuilder semanticQueryBuilder) {
		this.semanticQueryBuilder = semanticQueryBuilder;
	}

	@Override
	public ParsingContext getParsingContext() {
		return semanticQueryBuilder.getParsingContext();
	}

	@Override
	public QuerySpecProcessingState getQuerySpecProcessingState() {
		return semanticQueryBuilder.getQuerySpecProcessingState();
	}

	@Override
	public FromElementSpace getFromElementSpace() {
		return semanticQueryBuilder.getCurrentFromElementSpace();
	}

	@Override
	public boolean forceTerminalJoin() {
		return false;
	}

	@Override
	public String getTerminalJoinAlias() {
		return null;
	}

	@Override
	public JoinType getJoinType() {
		return JoinType.INNER;
	}

	@Override
	public boolean canReuseJoins() {
		return false;
	}

	@Override
	public boolean isFetched() {
		return false;
	}

	@Override
	public void validatePathRoot(SqmNavigableReference reference) {
		// by default, most contexts have no restrictions/validations
	}

	@Override
	public void validateIntermediateAttributeJoin(SqmNavigableReference lhs, AttributeDescriptor joinedAttribute) {
		// by default, most contexts have no restrictions/validations

	}
}
