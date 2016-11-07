/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;


import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.domain.PluralAttributeReference;
import org.hibernate.sqm.query.expression.domain.PluralAttributeBinding;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeIndexSqmExpression implements SqmExpression {
	private final PluralAttributeBinding attributeBinding;

	public PluralAttributeIndexSqmExpression(PluralAttributeBinding attributeBinding) {
		this.attributeBinding = attributeBinding;
	}

	public PluralAttributeBinding getAttributeBinding() {
		return attributeBinding;
	}

	private PluralAttributeReference pluralAttributeReference() {
		return attributeBinding.getAttribute();
	}

	@Override
	public DomainReference getExpressionType() {
		return pluralAttributeReference().getIndexReference();
	}

	@Override
	public DomainReference getInferableType() {
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
