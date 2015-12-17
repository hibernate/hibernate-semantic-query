/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.internal.path.resolution;

import org.hibernate.sqm.parser.internal.FromClauseIndex;
import org.hibernate.sqm.parser.internal.FromElementBuilder;
import org.hibernate.sqm.parser.internal.ParsingContext;
import org.hibernate.sqm.parser.internal.hql.phase1.FromClauseStackNode;
import org.hibernate.sqm.query.from.FromElement;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class PathResolverBasicImpl extends PathResolverStandardTemplate {
	private static final Logger log = Logger.getLogger( PathResolverBasicImpl.class );

	private final FromElementBuilder fromElementBuilder;
	private final FromClauseIndex fromClauseIndex;
	private final FromClauseStackNode fromClause;
	private final ParsingContext parsingContext;

	public PathResolverBasicImpl(
			FromElementBuilder fromElementBuilder,
			FromClauseIndex fromClauseIndex,
			ParsingContext parsingContext,
			FromClauseStackNode fromClause) {
		this.fromElementBuilder = fromElementBuilder;
		this.fromClauseIndex = fromClauseIndex;
		this.fromClause = fromClause;
		this.parsingContext = parsingContext;
	}

	@Override
	protected FromElementBuilder fromElementBuilder() {
		return fromElementBuilder;
	}

	@Override
	protected ParsingContext parsingContext() {
		return parsingContext;
	}

	protected FromElement findFromElementByAlias(String alias) {
		return fromElementBuilder.getAliasRegistry().findFromElementByAlias( alias );
	}

	protected FromElement findFromElementWithAttribute(String attributeName) {
		return fromClauseIndex.findFromElementWithAttribute( fromClause, attributeName );
	}
}
