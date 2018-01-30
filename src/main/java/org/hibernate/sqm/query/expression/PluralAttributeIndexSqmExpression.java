/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;


import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Navigable;
import org.hibernate.sqm.domain.PluralAttributeDescriptor;
import org.hibernate.sqm.query.expression.domain.PluralAttributeReference;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeIndexSqmExpression implements SqmExpression {
	private final PluralAttributeReference attributeBinding;

	public PluralAttributeIndexSqmExpression(PluralAttributeReference attributeBinding) {
		this.attributeBinding = attributeBinding;
	}

	public PluralAttributeReference getAttributeBinding() {
		return attributeBinding;
	}

	private PluralAttributeDescriptor pluralAttributeReference() {
		return attributeBinding.getAttribute();
	}

	@Override
	public Navigable getExpressionType() {
		return pluralAttributeReference().getIndexReference();
	}

	@Override
	public Navigable getInferableType() {
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitPluralAttributeIndexFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "INDEX(" + attributeBinding.asLoggableText() + ")";
	}
}
