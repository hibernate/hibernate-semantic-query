/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.hql.internal.path;

import org.hibernate.sqm.domain.AttributeReference;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.parser.common.AttributeBinding;
import org.hibernate.sqm.parser.common.DomainReferenceBinding;
import org.hibernate.sqm.parser.common.ResolutionContext;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.from.FromElementSpace;

/**
 * PathResolver implementation for resolving path references as part of a
 * SqmFromClause (join paths mainly).
 *
 * @author Steve Ebersole
 */
public class PathResolverJoinAttributeImpl extends PathResolverBasicImpl {
	private final FromElementSpace fromElementSpace;
	private final JoinType joinType;
	private final String alias;
	private final boolean fetched;

	public PathResolverJoinAttributeImpl(
			ResolutionContext resolutionContext,
			FromElementSpace fromElementSpace,
			JoinType joinType,
			String alias,
			boolean fetched) {
		super( resolutionContext );
		this.fromElementSpace = fromElementSpace;
		this.joinType = joinType;
		this.alias = alias;
		this.fetched = fetched;
	}

	@Override
	public boolean canReuseImplicitJoins() {
		return false;
	}

	@Override
	protected JoinType getIntermediateJoinType() {
		return joinType;
	}

	protected boolean areIntermediateJoinsFetched() {
		return fetched;
	}

	@Override
	protected AttributeBinding resolveTerminalAttributeBinding(
			DomainReferenceBinding lhs,
			String terminalName) {
		final AttributeReference attribute = resolveAttributeDescriptor( lhs.getFromElement(), terminalName );
		return resolveTerminal( lhs, terminalName, attribute, null );
	}

	private AttributeBinding resolveTerminal(
			DomainReferenceBinding lhs,
			String terminalName,
			AttributeReference attribute,
			EntityReference subclassIndicator) {
		AttributeBinding attributeBinding = context().getParsingContext().findOrCreateAttributeBinding(
				lhs,
				resolveAttributeDescriptor( lhs.getFromElement(), terminalName )
		);

		if ( attributeBinding.getFromElement() == null ) {
			// create the join and inject it into the binding
			attributeBinding.injectAttributeJoin(
					context().getFromElementBuilder().buildAttributeJoin(
							attributeBinding,
							alias,
							subclassIndicator,
							lhs.getFromElement().asLoggableText() + '.' + attribute.getAttributeName(),
							getIntermediateJoinType(),
							areIntermediateJoinsFetched(),
							canReuseImplicitJoins()
					)
			);
		}

		return attributeBinding;
	}

	@Override
	protected DomainReferenceBinding resolveTreatedTerminal(
			ResolutionContext context,
			DomainReferenceBinding lhs,
			String terminalName,
			EntityReference subclassIndicator) {
		final AttributeReference attribute = resolveAttributeDescriptor( lhs.getFromElement(), terminalName );
		return resolveTerminal( lhs, terminalName, attribute, subclassIndicator );
	}

	@Override
	protected DomainReferenceBinding resolveFromElementAliasAsTerminal(DomainReferenceBinding aliasedBinding) {
		// this can never be valid...
		throw new SemanticException( "Cannot join to aliased FromElement [" + aliasedBinding.getFromElement().getIdentificationVariable() + "]" );
	}
}
