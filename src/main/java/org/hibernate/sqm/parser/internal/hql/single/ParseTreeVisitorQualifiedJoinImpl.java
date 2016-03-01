/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.internal.hql.single;

import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.parser.internal.FromElementBuilder;
import org.hibernate.sqm.parser.internal.ParsingContext;
import org.hibernate.sqm.parser.internal.hql.AbstractHqlParseTreeVisitor;
import org.hibernate.sqm.parser.internal.hql.antlr.HqlParser;
import org.hibernate.sqm.parser.internal.hql.path.PathResolver;
import org.hibernate.sqm.parser.internal.hql.path.PathResolverJoinAttributeImpl;
import org.hibernate.sqm.parser.internal.hql.path.PathResolverJoinPredicateImpl;
import org.hibernate.sqm.parser.internal.hql.path.ResolutionContext;
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
	private final ResolutionContext resolutionContext;
	private final FromElementSpace fromElementSpace;

	private QualifiedJoinedFromElement currentJoinRhs;

	ParseTreeVisitorQualifiedJoinImpl(
			ParsingContext parsingContext,
			ResolutionContext resolutionContext,
			FromElementSpace fromElementSpace,
			JoinType joinType,
			String alias,
			boolean fetched) {
		super( parsingContext );
		this.resolutionContext = resolutionContext;
		this.fromElementSpace = fromElementSpace;
		this.pathResolverStack.push(
				new PathResolverJoinAttributeImpl(
						resolutionContext,
						fromElementSpace,
						joinType,
						alias,
						fetched
				)
		);
	}

	@Override
	protected ResolutionContext buildPathResolutionContext() {
		return resolutionContext;
	}

	@Override
	public FromClause getCurrentFromClause() {
		return fromElementSpace.getFromClause();
	}

	@Override
	public FromElementBuilder getFromElementBuilder() {
		return resolutionContext.getFromElementBuilder();
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
				new PathResolverJoinPredicateImpl( resolutionContext, currentJoinRhs )
		);
		try {
			return super.visitQualifiedJoinPredicate( ctx );
		}
		finally {

			pathResolverStack.pop();
		}
	}
}
