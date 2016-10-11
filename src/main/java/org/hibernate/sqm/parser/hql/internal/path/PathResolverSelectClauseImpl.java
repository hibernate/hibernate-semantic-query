/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.hql.internal.path;

import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.parser.common.ResolutionContext;
import org.hibernate.sqm.path.AttributeBinding;
import org.hibernate.sqm.path.Binding;

/**
 * @author Steve Ebersole
 */
public class PathResolverSelectClauseImpl extends PathResolverBasicImpl {
	public PathResolverSelectClauseImpl(ResolutionContext context) {
		super( context );
	}

	@Override
	protected AttributeBinding resolveTerminalAttributeBinding(Binding lhs, String terminalName) {
		final Attribute attribute = resolveAttributeDescriptor( lhs, terminalName );

		if ( attribute.getBoundType() instanceof EntityType ) {
			return buildAttributeJoin( resolveLhsFromElement( lhs ), attribute, null );
		}

		return super.resolveTerminalAttributeBinding( lhs, terminalName );
	}
}
