/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Navigable;
import org.hibernate.sqm.query.expression.domain.PluralAttributeReference;

/**
 * Represents the {@code SIZE()} function.
 *
 * @author Steve Ebersole
 * @author Gunnar Morling
 */
public class CollectionSizeSqmExpression implements SqmExpression {
	private final PluralAttributeReference pluralAttributeBinding;

	public CollectionSizeSqmExpression(PluralAttributeReference pluralAttributeBinding) {
		this.pluralAttributeBinding = pluralAttributeBinding;
	}

	public PluralAttributeReference getPluralAttributeBinding() {
		return pluralAttributeBinding;
	}

	@Override
	public Navigable getExpressionType() {
		// we'd need some form of "basic type memento" and to be able to ask the
		// consumer for the "basic type memento" for a Long.class
		return null;
	}

	@Override
	public Navigable getInferableType() {
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitPluralAttributeSizeFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "SIZE(" + pluralAttributeBinding.asLoggableText() + ")";
	}
}
