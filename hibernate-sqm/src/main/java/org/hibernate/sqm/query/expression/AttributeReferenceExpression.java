/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import java.util.Locale;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.Bindable;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.path.AttributePathPart;
import org.hibernate.sqm.query.from.FromElement;

/**
 * @author Steve Ebersole
 */
public class AttributeReferenceExpression implements AttributePathPart, Expression {
	private final FromElement source;
	private final Attribute attributeDescriptor;
	private final Type type;

	public AttributeReferenceExpression(
			FromElement source,
			Attribute attributeDescriptor,
			Type type) {
		this.source = source;
		this.attributeDescriptor = attributeDescriptor;
		this.type = type;
	}

	public FromElement getSource() {
		return source;
	}

	public Attribute getAttributeDescriptor() {
		return attributeDescriptor;
	}

	@Override
	public Bindable getBindableModelDescriptor() {
		return (Bindable) getAttributeDescriptor();
	}

	@Override
	public Type getExpressionType() {
		return type;
	}

	@Override
	public Type getInferableType() {
		return type;
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ENGLISH,
				"AttributeReferenceExpression{" +
						"source=%s" +
						", attribute-name=%s" +
						", attribute-type=%s" +
						'}',
				getSource().getAlias(),
				getAttributeDescriptor().getName(),
				type
		);
	}

	@Override
	public FromElement getUnderlyingFromElement() {
		// todo : not sure this is correct in all cases..
		return source;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitAttributeReferenceExpression( this );
	}
}
