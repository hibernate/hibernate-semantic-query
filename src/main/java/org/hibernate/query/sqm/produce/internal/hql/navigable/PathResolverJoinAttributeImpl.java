/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.produce.internal.hql.navigable;

import org.hibernate.query.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.query.sqm.domain.SqmNavigable;
import org.hibernate.query.sqm.SemanticException;
import org.hibernate.query.sqm.produce.spi.ResolutionContext;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.hibernate.query.sqm.tree.expression.domain.SqmAttributeBinding;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableBinding;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableSourceBinding;
import org.hibernate.query.sqm.tree.from.SqmFromElementSpace;
import org.hibernate.query.sqm.tree.from.SqmFromExporter;

/**
 * PathResolver implementation for resolving path references as part of a
 * SqmFromClause (join paths mainly).
 *
 * @author Steve Ebersole
 */
public class PathResolverJoinAttributeImpl extends PathResolverBasicImpl {
	private final SqmFromElementSpace fromElementSpace;
	private final SqmJoinType joinType;
	private final String alias;
	private final boolean fetched;

	public PathResolverJoinAttributeImpl(
			ResolutionContext resolutionContext,
			SqmFromElementSpace fromElementSpace,
			SqmJoinType joinType,
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
	protected SqmJoinType getIntermediateJoinType() {
		return joinType;
	}

	protected boolean areIntermediateJoinsFetched() {
		return fetched;
	}

	@Override
	protected SqmNavigableBinding resolveTerminalAttributeBinding(
			SqmNavigableSourceBinding sourceBinding,
			String terminalName) {
		final SqmNavigable attribute = resolveNavigable( sourceBinding, terminalName );
		return resolveTerminal( sourceBinding, attribute, null );
	}

	private SqmAttributeBinding resolveTerminal(
			SqmNavigableSourceBinding sourceBinding,
			SqmNavigable navigable,
			SqmExpressableTypeEntity subclassIndicator) {
		final SqmAttributeBinding attributeBinding = (SqmAttributeBinding) context().getParsingContext()
				.findOrCreateNavigableBinding(
						sourceBinding,
						navigable
				);

		if ( attributeBinding.getExportedFromElement() == null ) {
			// create the join and inject it into the binding
			attributeBinding.injectExportedFromElement(
					context().getFromElementBuilder().buildAttributeJoin(
							attributeBinding,
							alias,
							subclassIndicator,
							getIntermediateJoinType(),
							areIntermediateJoinsFetched(),
							canReuseImplicitJoins()
					)
			);
		}

		return attributeBinding;
	}

	@Override
	protected SqmNavigableBinding resolveTreatedTerminal(
			ResolutionContext context,
			SqmNavigableSourceBinding sourceBinding,
			String terminalName,
			SqmExpressableTypeEntity subclassIndicator) {
		final SqmNavigable attribute = resolveNavigable( sourceBinding, terminalName );
		return resolveTerminal( sourceBinding, attribute, subclassIndicator );
	}

	@Override
	protected SqmNavigableBinding resolveFromElementAliasAsTerminal(SqmFromExporter exporter) {
		// this can never be valid...
		throw new SemanticException( "Cannot join to aliased FromElement [" + exporter.getExportedFromElement().getIdentificationVariable() + "]" );
	}
}
