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
public class MaxIndexSqmExpression implements SqmExpression {
	private final AttributeBinding attributeBinding;

	public MaxIndexSqmExpression(AttributeBinding attributeBinding) {
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
		return walker.visitMaxIndexFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "MAXINDEX(" + attributeBinding.asLoggableText() + ")";
	}
}
