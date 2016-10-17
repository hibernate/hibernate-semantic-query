/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.hql.internal;

import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.parser.common.DomainReferenceBinding;
import org.hibernate.sqm.parser.common.FromElementBuilder;
import org.hibernate.sqm.parser.common.FromElementLocator;
import org.hibernate.sqm.parser.common.ParsingContext;
import org.hibernate.sqm.parser.common.ResolutionContext;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.SqmFrom;
import org.hibernate.sqm.query.from.SqmJoin;

/**
 * @author Steve Ebersole
 */
public class OrderByResolutionContext implements ResolutionContext, FromElementLocator {
	private final ParsingContext parsingContext;
	private final SqmSelectStatement selectStatement;

	public OrderByResolutionContext(ParsingContext parsingContext, SqmSelectStatement selectStatement) {
		this.parsingContext = parsingContext;
		this.selectStatement = selectStatement;
	}

	@Override
	public DomainReferenceBinding findFromElementByIdentificationVariable(String identificationVariable) {
		for ( FromElementSpace fromElementSpace : selectStatement.getQuerySpec()
				.getFromClause()
				.getFromElementSpaces() ) {
			if ( fromElementSpace.getRoot().getIdentificationVariable().equals( identificationVariable ) ) {
				return fromElementSpace.getRoot().getDomainReferenceBinding();
			}

			for ( SqmJoin joinedFromElement : fromElementSpace.getJoins() ) {
				if ( joinedFromElement.getIdentificationVariable().equals( identificationVariable ) ) {
					return joinedFromElement.getDomainReferenceBinding();
				}
			}
		}

		// otherwise there is none
		return null;
	}

	@Override
	public DomainReferenceBinding findFromElementExposingAttribute(String attributeName) {
		for ( FromElementSpace fromElementSpace : selectStatement.getQuerySpec()
				.getFromClause()
				.getFromElementSpaces() ) {
			if ( exposesAttribute( fromElementSpace.getRoot(), attributeName ) ) {
				return fromElementSpace.getRoot().getDomainReferenceBinding();
			}

			for ( SqmJoin joinedFromElement : fromElementSpace.getJoins() ) {
				if ( exposesAttribute( joinedFromElement, attributeName ) ) {
					return joinedFromElement.getDomainReferenceBinding();
				}
			}
		}

		// otherwise there is none
		return null;
	}

	private boolean exposesAttribute(SqmFrom sqmFrom, String attributeName) {
		return parsingContext.getConsumerContext().getDomainMetamodel().resolveAttributeReference( sqmFrom.getDomainReferenceBinding().getBoundDomainReference(), attributeName ) != null;
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
