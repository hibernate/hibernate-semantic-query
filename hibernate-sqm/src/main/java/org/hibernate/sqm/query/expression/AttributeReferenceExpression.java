/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import java.util.Locale;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.TypeDescriptor;
import org.hibernate.sqm.path.AttributePathPart;
import org.hibernate.sqm.query.from.FromElement;

/**
 * @author Steve Ebersole
 */
public class AttributeReferenceExpression implements AttributePathPart, Expression {
	private final FromElement source;
	private final AttributeDescriptor attributeDescriptor;

	public AttributeReferenceExpression(FromElement source, String attributeName) {
		this( source, source.getTypeDescriptor().getAttributeDescriptor( attributeName ) );
	}

	public AttributeReferenceExpression(
			FromElement source,
			AttributeDescriptor attributeDescriptor) {
		this.source = source;
		this.attributeDescriptor = attributeDescriptor;
	}

	public FromElement getSource() {
		return source;
	}

	public AttributeDescriptor getAttributeDescriptor() {
		return attributeDescriptor;
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return getAttributeDescriptor().getType();
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
				getAttributeDescriptor().getType().getTypeName()
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
