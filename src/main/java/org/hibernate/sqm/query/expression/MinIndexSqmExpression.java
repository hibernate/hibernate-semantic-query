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
import org.hibernate.sqm.parser.common.AttributeBinding;

/**
 * @author Steve Ebersole
 */
public class MinIndexSqmExpression implements SqmExpression {
	private final AttributeBinding attributeBinding;

	public MinIndexSqmExpression(AttributeBinding attributeBinding) {
		this.attributeBinding = attributeBinding;
	}

	public AttributeBinding getAttributeBinding() {
		return attributeBinding;
	}

	@Override
	public DomainReference getExpressionType() {
		return ( ( PluralAttributeReference) attributeBinding.getAttribute() ).getIndexReference();
	}

	@Override
	public DomainReference getInferableType() {
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMinIndexFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "MININDEX(" + attributeBinding.asLoggableText() + ")";
	}
}
