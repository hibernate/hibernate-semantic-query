/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.hql.internal;

import org.hibernate.sqm.domain.SqmNavigable;
import org.hibernate.sqm.domain.SqmNavigableSource;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.parser.common.FromElementBuilder;
import org.hibernate.sqm.parser.common.FromElementLocator;
import org.hibernate.sqm.parser.common.ParsingContext;
import org.hibernate.sqm.parser.common.ResolutionContext;
import org.hibernate.sqm.query.expression.domain.SqmNavigableBinding;
import org.hibernate.sqm.query.from.SqmFromElementSpace;
import org.hibernate.sqm.query.from.SqmFrom;
import org.hibernate.sqm.query.from.SqmFromClause;
import org.hibernate.sqm.query.from.SqmJoin;
import org.hibernate.sqm.query.select.SqmSelectClause;

/**
 * @author Steve Ebersole
 */
public class OrderByResolutionContext implements ResolutionContext, FromElementLocator {
	private final ParsingContext parsingContext;
	private final SqmFromClause fromClause;
	private final SqmSelectClause selectClause;

	public OrderByResolutionContext(ParsingContext parsingContext, SqmFromClause fromClause, SqmSelectClause selectClause) {
		this.parsingContext = parsingContext;
		this.fromClause = fromClause;
		this.selectClause = selectClause;
	}

	@Override
	public SqmNavigableBinding findNavigableBindingByIdentificationVariable(String identificationVariable) {
		for ( SqmFromElementSpace fromElementSpace : fromClause.getFromElementSpaces() ) {
			if ( fromElementSpace.getRoot().getIdentificationVariable().equals( identificationVariable ) ) {
				return fromElementSpace.getRoot().getBinding();
			}

			for ( SqmJoin joinedFromElement : fromElementSpace.getJoins() ) {
				if ( joinedFromElement.getIdentificationVariable().equals( identificationVariable ) ) {
					return joinedFromElement.getBinding();
				}
			}
		}

		// otherwise there is none
		return null;
	}

	@Override
	public SqmNavigableBinding findNavigableBindingExposingAttribute(String attributeName) {
		for ( SqmFromElementSpace fromElementSpace : fromClause.getFromElementSpaces() ) {
			if ( exposesAttribute( fromElementSpace.getRoot(), attributeName ) ) {
				return fromElementSpace.getRoot().getBinding();
			}

			for ( SqmJoin joinedFromElement : fromElementSpace.getJoins() ) {
				if ( exposesAttribute( joinedFromElement, attributeName ) ) {
					return joinedFromElement.getBinding();
				}
			}
		}

		// otherwise there is none
		return null;
	}

	private boolean exposesAttribute(SqmFrom sqmFrom, String attributeName) {
		final SqmNavigable navigable = sqmFrom.getBinding().getBoundNavigable();
		return SqmNavigableSource.class.isInstance( navigable )
				&& ( (SqmNavigableSource) navigable ).findNavigable( attributeName ) != null;
	}

	@Override
	public FromElementLocator getFromElementLocator() {
		return this;
	}

	@Override
	public FromElementBuilder getFromElementBuilder() {
		throw new SemanticException( "order-by clause cannot define implicit joins" );
	}

	@Override
	public ParsingContext getParsingContext() {
		return parsingContext;
	}
}
