/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.hql.internal.path;

import org.hibernate.sqm.query.expression.domain.AttributeBinding;
import org.hibernate.sqm.query.expression.domain.SingularAttributeBinding;
import org.hibernate.sqm.query.expression.domain.DomainReferenceBinding;
import org.hibernate.sqm.parser.common.ResolutionContext;

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
	protected AttributeBinding resolveTerminalAttributeBinding(DomainReferenceBinding lhs, String terminalName) {
		AttributeBinding attrBinding = context().getParsingContext().findOrCreateAttributeBinding( lhs, terminalName );
		resolveAttributeJoinIfNot( attrBinding );
		return attrBinding;
	}
}
