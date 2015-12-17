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
import org.hibernate.sqm.path.AttributeBinding;
import org.hibernate.sqm.path.AttributeBindingSource;
import org.hibernate.sqm.path.FromElementBinding;

/**
 * @author Steve Ebersole
 */
public class AttributeReferenceExpression implements AttributeBinding, Expression {
	private final AttributeBindingSource attributeBindingSource;
	private final Attribute boundAttribute;

	public AttributeReferenceExpression(
			AttributeBindingSource attributeBindingSource,
			Attribute boundAttribute) {
		this.attributeBindingSource = attributeBindingSource;
		this.boundAttribute = boundAttribute;
	}

	@Override
	public Attribute getBoundAttribute() {
		return boundAttribute;
	}

	@Override
	public AttributeBindingSource getAttributeBindingSource() {
		return attributeBindingSource;
	}

	@Override
	public Bindable getBoundModelType() {
		return (Bindable) boundAttribute;
	}

	@Override
	public String asLoggableText() {
		return getAttributeBindingSource().asLoggableText() + '.' + getBoundAttribute().getName();
	}

	@Override
	public Type getExpressionType() {
		return getBoundModelType().getBoundType();
	}

	@Override
	public Type getInferableType() {
		return getExpressionType();
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ENGLISH,
				"AttributeReferenceExpression{" +
						"path=%s" +
						", lhs-alias=%s" +
						", attribute-name=%s" +
						", attribute-type=%s" +
						'}',
				asLoggableText(),
				getAttributeBindingSource().getFromElement().getIdentificationVariable(),
				getBoundAttribute().getName(),
				getExpressionType()
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitAttributeReferenceExpression( this );
	}

	@Override
	public FromElementBinding getBoundFromElementBinding() {
		return getAttributeBindingSource().getFromElement();
	}
}
