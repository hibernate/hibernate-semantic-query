/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Navigable;
import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * Represents the ENTRY() function for obtaining the map entries from a {@code Map}-typed association.
 *
 * @author Gunnar Morling
 * @author Steve Ebersole
 */
public class MapEntrySqmExpression implements SqmExpression {
	private final PluralAttributeReference attributeBinding;

	public MapEntrySqmExpression(PluralAttributeReference attributeBinding) {
		this.attributeBinding = attributeBinding;
	}

	public PluralAttributeReference getAttributeBinding() {
		return attributeBinding;
	}

	@Override
	public Navigable getExpressionType() {
		return null;
	}

	@Override
	public Navigable getInferableType() {
		return null;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMapEntryFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "ENTRY(" + attributeBinding.asLoggableText() + ")";
	}
}
