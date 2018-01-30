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
 * @author Steve Ebersole
 */
public class MinElementSqmExpression implements SqmExpression {
	private final PluralAttributeReference attributeBinding;

	public MinElementSqmExpression(PluralAttributeReference attributeBinding) {
		this.attributeBinding = attributeBinding;
	}

	public PluralAttributeReference getAttributeBinding() {
		return attributeBinding;
	}

	@Override
	public Navigable getExpressionType() {
		return attributeBinding.getAttribute().getElementReference();
	}

	@Override
	public Navigable getInferableType() {
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMinElementFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "MINELEMENT(" + attributeBinding.asLoggableText() + ")";
	}
}