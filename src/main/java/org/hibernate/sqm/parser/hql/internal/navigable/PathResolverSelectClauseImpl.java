/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.parser.hql.internal.navigable;

import org.hibernate.sqm.parser.common.ResolutionContext;
import org.hibernate.sqm.query.expression.domain.SqmNavigableBinding;
import org.hibernate.sqm.query.expression.domain.SqmNavigableSourceBinding;

/**
 * @author Steve Ebersole
 */
public class PathResolverSelectClauseImpl extends PathResolverBasicImpl {
	public PathResolverSelectClauseImpl(ResolutionContext context) {
		super( context );
	}

	@Override
	protected boolean shouldRenderTerminalAttributeBindingAsJoin() {
		return true;
	}

	@Override
	protected SqmNavigableBinding resolveTerminalAttributeBinding(SqmNavigableSourceBinding sourceBinding, String terminalName) {
		SqmNavigableBinding attrBinding = context().getParsingContext().findOrCreateNavigableBinding( sourceBinding, terminalName );
		resolveAttributeJoinIfNot( attrBinding );
		return attrBinding;
	}
}
