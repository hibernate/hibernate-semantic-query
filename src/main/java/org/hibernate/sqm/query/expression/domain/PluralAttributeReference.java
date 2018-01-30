/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.query.sqm.produce.paths.spi.SemanticPathPart;
import org.hibernate.query.sqm.produce.paths.spi.SemanticPathResolutionContext;
import org.hibernate.sqm.NotYetImplementedException;
import org.hibernate.sqm.domain.PluralAttributeDescriptor;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.from.SqmAttributeJoin;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeReference
		extends AbstractAttributeReference<PluralAttributeDescriptor> {
	public PluralAttributeReference(SqmNavigableReference lhs, PluralAttributeDescriptor attribute) {
		super( lhs, attribute );
	}

	public PluralAttributeReference(
			SqmNavigableReference lhs,
			PluralAttributeDescriptor attribute,
			SqmAttributeJoin join) {
		super( lhs, attribute, join );
	}

	@Override
	public SemanticPathPart resolvePathPart(
			String name,
			String currentContextKey, boolean isTerminal, SemanticPathResolutionContext context) {
		// todo (6.0) - implement
		throw new NotYetImplementedException(  );
	}

	@Override
	public SqmNavigableReference resolveIndexedAccess(
			SqmExpression selector,
			String currentContextKey, boolean isTerminal, SemanticPathResolutionContext context) {
		// todo (6.0) - implement
		throw new NotYetImplementedException(  );
	}
}
