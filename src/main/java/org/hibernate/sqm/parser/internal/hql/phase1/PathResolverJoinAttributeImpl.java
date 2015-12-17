/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.internal.hql.phase1;

import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.PluralAttribute;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.parser.internal.FromClauseIndex;
import org.hibernate.sqm.parser.internal.FromElementBuilder;
import org.hibernate.sqm.parser.internal.ParsingContext;
import org.hibernate.sqm.parser.internal.path.resolution.PathResolverBasicImpl;
import org.hibernate.sqm.path.AttributeBinding;
import org.hibernate.sqm.path.AttributeBindingSource;
import org.hibernate.sqm.path.FromElementBinding;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;

/**
 * PathResolver implementation for resolving path references as part of a
 * FromClause (join paths mainly).
 *
 * @author Steve Ebersole
 */
class PathResolverJoinAttributeImpl extends PathResolverBasicImpl {
	private final FromElementBuilder fromElementBuilder;
	private final FromElementSpace fromElementSpace;
	private final JoinType joinType;
	private final String alias;
	private final boolean fetched;

	PathResolverJoinAttributeImpl(
			FromElementBuilder fromElementBuilder,
			FromClauseIndex fromClauseIndex,
			FromClauseStackNode currentFromClauseNode,
			ParsingContext parsingContext,
			FromElementSpace fromElementSpace,
			JoinType joinType,
			String alias,
			boolean fetched) {
		super( fromElementBuilder, fromClauseIndex, parsingContext, currentFromClauseNode );
		this.fromElementBuilder = fromElementBuilder;
		this.fromElementSpace = fromElementSpace;
		this.joinType = joinType;
		this.alias = alias;
		this.fetched = fetched;
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
			AttributeBindingSource lhs,
			String terminalName) {
		final Attribute attribute = resolveAttributeDescriptor( lhs, terminalName );
		final EntityType subclassType = resolveBoundEntityType( attribute );
		return resolveTerminal( lhs, terminalName, attribute, subclassType );
	}

	private EntityType resolveBoundEntityType(Attribute attribute) {
		if ( attribute instanceof PluralAttribute ) {
			final PluralAttribute pluralAttribute = (PluralAttribute) attribute;
			if ( pluralAttribute.getElementType() instanceof EntityType ) {
				return (EntityType) pluralAttribute.getElementType();
			}
		}
		else if ( attribute instanceof SingularAttribute ) {
			final SingularAttribute singularAttribute = (SingularAttribute) attribute;
			if ( singularAttribute.getType() instanceof EntityType ) {
				return (EntityType) singularAttribute.getType();
			}
		}

		return null;
	}

	private QualifiedAttributeJoinFromElement resolveTerminal(
			AttributeBindingSource lhs,
			String terminalName,
			Attribute attribute,
			EntityType subclassType) {
		return fromElementBuilder.buildAttributeJoin(
				fromElementSpace,
				alias,
				attribute,
				subclassType,
				lhs.asLoggableText() + '.' + terminalName,
				joinType,
				lhs.getFromElement(),
				fetched
		);
	}

	@Override
	protected FromElementBinding resolveTreatedTerminal(
			AttributeBindingSource lhs,
			String terminalName,
			EntityType subclassIndicator) {
		final Attribute attribute = resolveAttributeDescriptor( lhs, terminalName );
		return resolveTerminal( lhs, terminalName, attribute, subclassIndicator );
	}

	@Override
	protected FromElementBinding resolveFromElementAliasAsTerminal(FromElement aliasedFromElement) {
		// this can never be valid...
		throw new SemanticException( "Cannot join to aliased FromElement [" + aliasedFromElement.getIdentificationVariable() + "]" );
	}
}
