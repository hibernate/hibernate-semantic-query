/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import org.hibernate.sqm.domain.SqmNavigable;
import org.hibernate.sqm.query.expression.domain.SqmNavigableBinding;
import org.hibernate.sqm.query.expression.domain.SqmNavigableSourceBinding;
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
public class QuerySpecProcessingStateStandardImpl extends AbstractQuerySpecProcessingState {
	private static final Logger log = Logger.getLogger( QuerySpecProcessingStateStandardImpl.class );

	private final SqmFromClause fromClause;

	private final FromElementBuilder fromElementBuilder;

	public QuerySpecProcessingStateStandardImpl(ParsingContext parsingContext, QuerySpecProcessingState containingQueryState) {
		super( parsingContext, containingQueryState );

		this.fromClause = new SqmFromClause();

		if ( containingQueryState == null ) {
			this.fromElementBuilder = new FromElementBuilder( parsingContext, new AliasRegistry() );
		}
		else {
			this.fromElementBuilder = new FromElementBuilder(
					parsingContext,
					new AliasRegistry( containingQueryState.getFromElementBuilder().getAliasRegistry() )
			);
		}
	}

	public SqmFromClause getFromClause() {
		return fromClause;
	}

	@Override
	public FromElementBuilder getFromElementBuilder() {
		return fromElementBuilder;
	}

	@Override
	public SqmNavigableBinding findNavigableBindingByIdentificationVariable(String identificationVariable) {
		return fromElementBuilder.getAliasRegistry().findFromElementByAlias( identificationVariable );
	}

	@Override
	public SqmNavigableBinding findNavigableBindingExposingAttribute(String name) {
		SqmNavigableBinding found = null;
		for ( FromElementSpace space : fromClause.getFromElementSpaces() ) {
			if ( definesAttribute( space.getRoot().getBinding(), name ) ) {
				if ( found != null ) {
					throw new IllegalStateException( "Multiple from-elements expose unqualified attribute : " + name );
				}
				found = space.getRoot().getBinding();
			}

			for ( SqmJoin join : space.getJoins() ) {
				if ( definesAttribute( join.getBinding(), name ) ) {
					if ( found != null ) {
						throw new IllegalStateException( "Multiple from-elements expose unqualified attribute : " + name );
					}
					found = join.getBinding();
				}
			}
		}

		if ( found == null ) {
			if ( getContainingQueryState() != null ) {
				log.debugf( "Unable to resolve unqualified attribute [%s] in local SqmFromClause; checking containingQueryState" );
				found = getContainingQueryState().findNavigableBindingExposingAttribute( name );
			}
		}

		return found;
	}

	private boolean definesAttribute(SqmNavigableBinding sourceBinding, String name) {
		if ( !SqmNavigableSourceBinding.class.isInstance( sourceBinding ) ) {
			return false;
		}

		final SqmNavigable navigable = ( (SqmNavigableSourceBinding) sourceBinding ).getBoundNavigable().findNavigable( name );
		return navigable != null;
	}

	private boolean definesAttribute(SqmNavigableSourceBinding sourceBinding, String name) {
		final SqmNavigable navigable = sourceBinding.getBoundNavigable().findNavigable( name );
		return navigable != null;
	}

	@Override
	public FromElementLocator getFromElementLocator() {
		return this;
	}
}
