/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import org.hibernate.sqm.domain.AttributeReference;
import org.hibernate.sqm.query.expression.domain.DomainReferenceBinding;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.SqmFromClause;
import org.hibernate.sqm.query.from.SqmJoin;

import org.jboss.logging.Logger;

/**
 * Models the state related to parsing a sqm spec.  As a "linked list" to account for
 * subqueries
 *
 * @author Steve Ebersole
 * @author Andrea Boriero
 */
public class QuerySpecProcessingStateStandardImpl implements QuerySpecProcessingState {
	private static final Logger log = Logger.getLogger( QuerySpecProcessingStateStandardImpl.class );

	private final QuerySpecProcessingState parent;

	private final ParsingContext parsingContext;
	private final SqmFromClause fromClause;

	private final FromElementBuilder fromElementBuilder;

	public QuerySpecProcessingStateStandardImpl(ParsingContext parsingContext) {
		this( parsingContext, null );
	}

	public QuerySpecProcessingStateStandardImpl(ParsingContext parsingContext, QuerySpecProcessingState parent) {
		this.parent = parent;

		this.parsingContext = parsingContext;
		this.fromClause = new SqmFromClause();

		if ( parent == null ) {
			this.fromElementBuilder = new FromElementBuilder( parsingContext, new AliasRegistry() );
		}
		else {
			this.fromElementBuilder = new FromElementBuilder(
					parsingContext,
					new AliasRegistry( parent.getFromElementBuilder().getAliasRegistry() )
			);
		}
	}

	public QuerySpecProcessingState getParent() {
		return parent;
	}

	public SqmFromClause getFromClause() {
		return fromClause;
	}

	@Override
	public ParsingContext getParsingContext() {
		return parsingContext;
	}

	@Override
	public FromElementBuilder getFromElementBuilder() {
		return fromElementBuilder;
	}

	@Override
	public DomainReferenceBinding findFromElementByIdentificationVariable(String identificationVariable) {
		return fromElementBuilder.getAliasRegistry().findFromElementByAlias( identificationVariable );
	}

	@Override
	public DomainReferenceBinding findFromElementExposingAttribute(String name) {
		DomainReferenceBinding found = null;
		for ( FromElementSpace space : fromClause.getFromElementSpaces() ) {
			if ( definesAttribute( space.getRoot().getDomainReferenceBinding(), name ) ) {
				if ( found != null ) {
					throw new IllegalStateException( "Multiple from-elements expose unqualified attribute : " + name );
				}
				found = space.getRoot().getDomainReferenceBinding();
			}

			for ( SqmJoin join : space.getJoins() ) {
				if ( definesAttribute( join.getDomainReferenceBinding(), name ) ) {
					if ( found != null ) {
						throw new IllegalStateException( "Multiple from-elements expose unqualified attribute : " + name );
					}
					found = join.getDomainReferenceBinding();
				}
			}
		}

		if ( found == null ) {
			if ( parent != null ) {
				log.debugf( "Unable to resolve unqualified attribute [%s] in local SqmFromClause; checking parent" );
				found = parent.findFromElementExposingAttribute( name );
			}
		}

		return found;
	}

	private boolean definesAttribute(DomainReferenceBinding domainReferenceBinding, String name) {
		final AttributeReference resolvedAttributeReference = getParsingContext().getConsumerContext()
				.getDomainMetamodel()
				.locateAttributeReference( domainReferenceBinding.getBoundDomainReference(), name );
		return resolvedAttributeReference != null;
	}

	@Override
	public FromElementLocator getFromElementLocator() {
		return this;
	}
}
