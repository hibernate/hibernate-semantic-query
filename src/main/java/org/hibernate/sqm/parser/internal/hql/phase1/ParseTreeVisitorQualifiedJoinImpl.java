/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.internal.hql.phase1;

import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.parser.internal.FromClauseIndex;
import org.hibernate.sqm.parser.internal.FromElementBuilder;
import org.hibernate.sqm.parser.internal.ParsingContext;
import org.hibernate.sqm.parser.internal.hql.AbstractHqlParseTreeVisitor;
import org.hibernate.sqm.parser.internal.hql.antlr.HqlParser;
import org.hibernate.sqm.parser.internal.path.resolution.PathResolver;
import org.hibernate.sqm.parser.internal.path.resolution.PathResolverJoinPredicateImpl;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.QualifiedJoinedFromElement;
import org.hibernate.sqm.query.predicate.Predicate;

/**
 * An HqlParseTreeVisitor implementation for dealing with FromClause structures.
 *
 * @author Steve Ebersole
 */
class ParseTreeVisitorQualifiedJoinImpl extends AbstractHqlParseTreeVisitor {
	private final FromElementBuilder fromElementBuilder;
	private final FromClauseIndex fromClauseIndex;
	private final ParsingContext parsingContext;
	private final FromElementSpace fromElementSpace;
	private final FromClauseStackNode currentFromClauseNode;

	private QualifiedJoinedFromElement currentJoinRhs;

	ParseTreeVisitorQualifiedJoinImpl(
			FromElementBuilder fromElementBuilder,
			FromClauseIndex fromClauseIndex,
			ParsingContext parsingContext,
			FromElementSpace fromElementSpace,
			FromClauseStackNode fromClauseNode,
			JoinType joinType,
			String alias,
			boolean fetched) {
		super( parsingContext, fromClauseIndex );
		this.fromElementBuilder = fromElementBuilder;
		this.fromClauseIndex = fromClauseIndex;
		this.parsingContext = parsingContext;
		this.fromElementSpace = fromElementSpace;
		this.currentFromClauseNode = fromClauseNode;
		this.pathResolverStack.push(
				new PathResolverJoinAttributeImpl(
						fromElementBuilder,
						fromClauseIndex,
						currentFromClauseNode,
						parsingContext,
						fromElementSpace,
						joinType,
						alias,
						fetched
				)
		);
	}

	@Override
	public FromClause getCurrentFromClause() {
		return fromElementSpace.getFromClause();
	}

	@Override
	public FromElementBuilder getFromElementBuilder() {
		return fromElementBuilder;
	}

	@Override
	public FromClauseStackNode getCurrentFromClauseNode() {
		return currentFromClauseNode;
	}

	@Override
	public PathResolver getCurrentPathResolver() {
		return pathResolverStack.getCurrent();
	}

	public void setCurrentJoinRhs(QualifiedJoinedFromElement currentJoinRhs) {
		this.currentJoinRhs = currentJoinRhs;
	}

	@Override
	public Predicate visitQualifiedJoinPredicate(HqlParser.QualifiedJoinPredicateContext ctx) {
		if ( currentJoinRhs == null ) {
			throw new ParsingException( "Expecting join RHS to be set" );
		}

		pathResolverStack.push(
				new PathResolverJoinPredicateImpl(
						fromElementBuilder,
						fromClauseIndex,
						parsingContext,
						getCurrentFromClauseNode(),
						currentJoinRhs
				)
		);
		try {
			return super.visitQualifiedJoinPredicate( ctx );
		}
		finally {
			pathResolverStack.pop();
		}
	}
}
