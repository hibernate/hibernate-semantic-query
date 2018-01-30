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
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.expression.domain.SqmNavigableReference;
import org.hibernate.sqm.query.from.FromElementSpace;

/**
 * @author Steve Ebersole
 */
public interface NavigableReferenceBuilderContext {
	ParsingContext getParsingContext();

	QuerySpecProcessingState getQuerySpecProcessingState();

	FromElementSpace getFromElementSpace();

	// todo (6.0) : would rather move to SQM production always creating the SqmJoin
	//		SQM consumer would then use semantics to determine whether it should
	//		actually create the corresponding SQL AST TableGroup and maybe even how
	//		much of the TableGroup to create

	boolean forceTerminalJoin();

	/**
	 * What is the alias for the "terminal" join created within this context?
	 */
	String getTerminalJoinAlias();

	/**
	 * What is the join type for joins created within this context?
	 */
	JoinType getJoinType();

	/**
	 * Can joins created within the bounds of this context be shared?
	 */
	boolean canReuseJoins();

	boolean isFetched();

	void validatePathRoot(SqmNavigableReference reference);

	void validateIntermediateAttributeJoin(
			SqmNavigableReference lhs,
			AttributeDescriptor joinedAttribute);

}
